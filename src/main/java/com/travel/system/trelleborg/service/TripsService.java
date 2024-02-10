package com.travel.system.trelleborg.service;

import com.travel.system.trelleborg.common.Constants;
import com.travel.system.trelleborg.common.HashUtils;
import com.travel.system.trelleborg.components.FileProcessor;
import com.travel.system.trelleborg.dto.*;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class TripsService {
    @Autowired
    private FileProcessor fileProcessor;

    @Autowired
    private BusFeeService busFeeService;

    @Value("${salt}")
    private String hashSalt;

    @SneakyThrows
    public TripProcessResult process(MultipartFile file) {
        List<String[]> rawData = fileProcessor.readRawData(file);
        List<TouchRecord> touchRecords = mapInfo(rawData);

        List<Trips> trips = new ArrayList<>();
        List<Trips> unprocessableTrips = new ArrayList<>();

        List<TouchRecord> processableTouchData = filterAndSortProcessableTouchData(touchRecords, unprocessableTrips);
        Map<String, List<TouchRecord>> touchRecordGrouping = groupingTouchRecordByPAN(processableTouchData);

        for (Map.Entry<String, List<TouchRecord>> touchRecord : touchRecordGrouping.entrySet()) {
            trips.addAll(calculateATripCost(touchRecord.getValue(), unprocessableTrips));
        }

        List<TripsSummary> tripsSummary = generateTripSummaries(trips);

        return TripProcessResult.builder()
                .tripSummary(tripsSummary)
                .unprocessableTrip(unprocessableTrips)
                .tripData(trips)
                .build();
    }

    private List<TouchRecord> mapInfo(List<String[]> rawData) {
        List<TouchRecord> touchRecords = new ArrayList<>();
        rawData.forEach(values -> {
            TouchRecord touchRecord = new TouchRecord();
            touchRecord.setId(Integer.valueOf(getCellValue(values[0])));
            touchRecord.setDateTimeUTC(getCellValue(values[1]));
            touchRecord.setTouchType(getCellValue(values[2]));
            touchRecord.setStopId(getCellValue(values[3]));
            touchRecord.setCompanyID(getCellValue(values[4]));
            touchRecord.setBusId(getCellValue(values[5]));
            touchRecord.setPan(getCellValue(values[6]));

            touchRecords.add(touchRecord);
        });
        return touchRecords;
    }

    private String getCellValue(String cellValue) {
        return (!Objects.isNull(cellValue) ? cellValue.trim() : null);
    }

    @SneakyThrows
    private List<TouchRecord> filterAndSortProcessableTouchData(List<TouchRecord> touchRecords, List<Trips> unprocessableTrip) {
        List<TouchRecord> processableTouchData = new ArrayList<>();
        for (TouchRecord record : touchRecords) {
            if (record.getDateTimeUTC() != null && record.getTouchType() != null && record.getPan() != null &&
                    !record.getDateTimeUTC().isEmpty() && !record.getTouchType().isEmpty() && !record.getPan().isEmpty()) {
                processableTouchData.add(record);
            } else {
                unprocessableTrip.add(Trips.builder()
                        .started(record.getDateTimeUTC())
                        .finished(record.getDateTimeUTC())
                        .companyId(record.getCompanyID())
                        .status(Constants.EMPTY_FIELD_MESSAGE)
                        .busId(record.getBusId())
                        .durationSec(0)
                        .fromStopId(record.getStopId())
                        .toStopId(record.getStopId())
                        .chargeAmount(BigDecimal.valueOf(0.00))
                        .hashedPan(HashUtils.hashMD5(String.format("%s%s",record.getPan(), hashSalt)))
                        .build());
            }
        }
        sortByIdAndTime(processableTouchData);
        return processableTouchData;
    }

    public void sortByIdAndTime(List<TouchRecord> tripData) {
        Collections.sort(tripData, Comparator.comparing(TouchRecord::getPan).thenComparing(TouchRecord::getDateTimeUTC));
    }

    @SneakyThrows
    public static Map<String, List<TouchRecord>> groupingTouchRecordByPAN(List<TouchRecord> tripData) {
        Map<String, List<TouchRecord>> tripDataMap = new HashMap<>();
        for (TouchRecord trip : tripData) {
            List<TouchRecord> dataList = tripDataMap.getOrDefault(trip.getPan(), new ArrayList<>());
            dataList.add(trip);
            tripDataMap.put(trip.getPan(), dataList);
        }
        return tripDataMap;
    }

    @SneakyThrows
    public static List<TripsSummary> generateTripSummaries(List<Trips> tripsList) {
        Map<String, TripsSummary> summaryMap = new HashMap<>();

        for (Trips trips : tripsList) {
            String key = trips.getStarted().substring(0, 10) + "_" + trips.getCompanyId() + "_" + trips.getBusId();
            TripsSummary summaryDto = summaryMap.getOrDefault(key, TripsSummary.builder().build());
            summaryDto.setDate(trips.getStarted().substring(0, 10));
            summaryDto.setCompanyId(trips.getCompanyId());
            summaryDto.setBusId(trips.getBusId());
            summaryDto.setCompleteTripCount(summaryDto.getCompleteTripCount() != null ? summaryDto.getCompleteTripCount() : 0);
            summaryDto.setIncompleteTripCount(summaryDto.getIncompleteTripCount() != null ? summaryDto.getIncompleteTripCount() : 0);
            summaryDto.setCancelledTripCount(summaryDto.getCancelledTripCount() != null ? summaryDto.getCancelledTripCount() : 0);
            summaryDto.setTotalCharges(summaryDto.getTotalCharges() != null ? summaryDto.getTotalCharges() : BigDecimal.valueOf(0));

            switch (trips.getStatus()) {
                case "COMPLETE":
                    summaryDto.setCompleteTripCount(summaryDto.getCompleteTripCount() + 1);
                    break;
                case "INCOMPLETE":
                    summaryDto.setIncompleteTripCount(summaryDto.getIncompleteTripCount() + 1);
                    break;
                case "CANCELLED":
                    summaryDto.setCancelledTripCount(summaryDto.getCancelledTripCount() + 1);
                    break;
            }
            summaryDto.setTotalCharges(summaryDto.getTotalCharges().add(trips.getChargeAmount()));
            summaryMap.put(key, summaryDto);
        }

        List<TripsSummary> summaryList = new ArrayList<>(summaryMap.values());
        summaryList.sort(Comparator.comparing(TripsSummary::getDate)
                .thenComparing(TripsSummary::getCompanyId)
                .thenComparing(TripsSummary::getBusId));

        return summaryList;
    }

    @SneakyThrows
    private List<Trips> calculateATripCost(List<TouchRecord> processableTouchRecords, List<Trips> unprocessableTrip) {
        List<Trips> result = new ArrayList<>();
        int startTripIndex = 0;
        while (startTripIndex < processableTouchRecords.size()) {
            TouchRecord touchOnRecord = processableTouchRecords.get(startTripIndex);
            if (touchOnRecord.getTouchType().equalsIgnoreCase(Constants.TOUCH_OFF)) {
                unprocessableTrip.add(getUnprocessableTrip(touchOnRecord));
                startTripIndex++;
            } else if (touchOnRecord.getTouchType().equalsIgnoreCase(Constants.TOUCH_ON) && startTripIndex + 1 < processableTouchRecords.size()) {
                TouchRecord touchOffRecord = processableTouchRecords.get(startTripIndex + 1);
                if (touchOffRecord.getTouchType().equalsIgnoreCase(Constants.TOUCH_OFF)) {
                    String tripName = String.format("%s_TO_%s", touchOnRecord.getStopId(), touchOffRecord.getStopId());
                    BigDecimal cost;
                    String status;
                    if (touchOnRecord.getStopId().equalsIgnoreCase(touchOffRecord.getStopId())) {
                        cost = new BigDecimal("0.00");
                        status = Constants.CANCELLED_TRIP;
                    } else {
                        Map<String, BigDecimal> busFees = busFeeService.getBusFees();
                        cost = busFees.get(tripName);
                        status = Constants.COMPLETED_TRIP;
                    }
                    result.add(getProcessableTrip(touchOnRecord, touchOffRecord, cost, status));
                    startTripIndex = startTripIndex + 2;
                } else if (touchOffRecord.getTouchType().equalsIgnoreCase(Constants.TOUCH_ON)) {
                    Map<String, BigDecimal> busFees = busFeeService.filterKeys(touchOnRecord.getStopId());
                    BigDecimal maxCost = busFeeService.findHighestValue(busFees);
                    result.add(getTripHasNoTouchOffRecord(touchOnRecord, touchOffRecord, maxCost));
                    startTripIndex = startTripIndex + 1;
                }
            } else if (touchOnRecord.getTouchType().equalsIgnoreCase(Constants.TOUCH_ON) && startTripIndex + 1 >= processableTouchRecords.size()) {
                Map<String, BigDecimal> busFees = busFeeService.filterKeys(touchOnRecord.getStopId());
                BigDecimal maxCost = busFeeService.findHighestValue(busFees);
                result.add(getTripLastTouchNoTouchOff(touchOnRecord, maxCost));
                startTripIndex = startTripIndex + 1;
            }
        }

        return result;
    }

    private Trips getUnprocessableTrip(TouchRecord touchOnRecord) {
        return Trips.builder()
                .started(Constants.UNIDENTIFIED_STATE)
                .finished(touchOnRecord.getDateTimeUTC())
                .companyId(touchOnRecord.getCompanyID())
                .status(Constants.NO_TOUCH_ON_MESSAGE)
                .busId(touchOnRecord.getBusId())
                .durationSec(0)
                .fromStopId(Constants.UNIDENTIFIED_STATE)
                .toStopId(touchOnRecord.getStopId())
                .chargeAmount(BigDecimal.valueOf(0))
                .hashedPan(HashUtils.hashMD5(String.format("%s%s",touchOnRecord.getPan(), hashSalt)))
                .build();
    }

    private Trips getProcessableTrip(TouchRecord touchOnRecord, TouchRecord touchOffRecord, BigDecimal cost, String status) {
        return Trips.builder()
                .started(touchOnRecord.getDateTimeUTC())
                .finished(touchOffRecord.getDateTimeUTC())
                .durationSec(calculateDurationSecond(touchOnRecord.getDateTimeUTC(), touchOffRecord.getDateTimeUTC()))
                .fromStopId(touchOnRecord.getStopId())
                .toStopId(touchOffRecord.getStopId())
                .chargeAmount(cost)
                .companyId(touchOnRecord.getCompanyID())
                .busId(touchOnRecord.getBusId())
                .hashedPan(HashUtils.hashMD5(String.format("%s%s",touchOnRecord.getPan(), hashSalt)))
                .status(status)
                .build();
    }

    private Trips getTripHasNoTouchOffRecord(TouchRecord touchOnRecord, TouchRecord touchOffRecord, BigDecimal maxCost) {
        return Trips.builder()
                .started(touchOnRecord.getDateTimeUTC())
                .finished(Constants.UNIDENTIFIED_STATE)
                .durationSec(calculateDurationSecond(touchOnRecord.getDateTimeUTC(), touchOffRecord.getDateTimeUTC()))
                .fromStopId(touchOnRecord.getStopId())
                .toStopId(Constants.UNIDENTIFIED_STATE)
                .chargeAmount(maxCost)
                .companyId(touchOnRecord.getCompanyID())
                .busId(touchOnRecord.getBusId())
                .hashedPan(HashUtils.hashMD5(String.format("%s%s",touchOnRecord.getPan(), hashSalt)))
                .status(Constants.INCOMPLETED_TRIP)
                .build();
    }

    private Trips getTripLastTouchNoTouchOff(TouchRecord touchOnRecord, BigDecimal maxCost) {
        return Trips.builder()
                .started(touchOnRecord.getDateTimeUTC())
                .finished(Constants.UNIDENTIFIED_STATE)
                .durationSec(0)
                .fromStopId(touchOnRecord.getStopId())
                .toStopId(Constants.UNIDENTIFIED_STATE)
                .chargeAmount(maxCost)
                .companyId(touchOnRecord.getCompanyID())
                .busId(touchOnRecord.getBusId())
                .hashedPan(HashUtils.hashMD5(String.format("%s%s",touchOnRecord.getPan(), hashSalt)))
                .status(Constants.INCOMPLETED_TRIP)
                .build();
    }

    private long calculateDurationSecond(String startedString, String finishedString) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(Constants.DATE_TIME_FORMAT);
        try {
            Date startedDate = dateFormat.parse(startedString);
            Date finishedDate = dateFormat.parse(finishedString);
            return  (finishedDate.getTime() - startedDate.getTime()) / 1000;
        } catch (ParseException e) {
            return 0;
        }
    }

    @SneakyThrows
    public CSVFile[] generateCSVTripResultFiles(TripProcessResult result) {
        CSVFile[] csvFiles = new CSVFile[3];
        csvFiles[0] = new CSVFile(Constants.TRIP_CSV_FILE_NAME, fileProcessor.generateCSVContent(result.getTripData()));
        csvFiles[1] = new CSVFile(Constants.UNPROCESSABLE_CSV_FILE_NAME, fileProcessor.generateCSVContent(result.getUnprocessableTrip()));
        csvFiles[2] = new CSVFile(Constants.SUMMARY_CSV_FILE_NAME, fileProcessor.generateCSVContent(result.getTripSummary()));
        return csvFiles;
    }
}
