package com.travel.system.trelleborg.service;

import com.travel.system.trelleborg.components.FileProcessor;
import com.travel.system.trelleborg.dto.TouchRecord;
import com.travel.system.trelleborg.dto.TripProcessResult;
import com.travel.system.trelleborg.service.BusFeeService;
import com.travel.system.trelleborg.service.TripsService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TripsServiceTest {

    @Mock
    private FileProcessor fileProcessor;

    @Mock
    private BusFeeService busFeeService;

    @InjectMocks
    private TripsService tripsService;

   /* @Test
    void process_ValidFile_ReturnsTripProcessResult() {
        // Prepare test data
        MockMultipartFile mockFile = new MockMultipartFile("file", new byte[0]);
        List<String[]> rawData = new ArrayList<>();
        // Add some test data to rawData

        // Mock behavior
        when(fileProcessor.readRawData(mockFile)).thenReturn(rawData);
        when(busFeeService.getBusFees()).thenReturn(Map.of("STOP1_TO_STOP2", BigDecimal.TEN));

        // Call the method
        TripProcessResult result = tripsService.process(mockFile);

        // Assertions
        // Add assertions based on the expected behavior
    }*/

   /* @Test
    void mapInfo_ValidRawData_ReturnsTouchRecords() {
        // Prepare test data
        List<String[]> rawData = Arrays.asList(
                new String[]{"1", "2024-02-09 08:00:00", "TOUCH_ON", "STOP1", "COMPANY1", "BUS1", "PAN1"},
                new String[]{"2", "2024-02-09 09:00:00", "TOUCH_OFF", "STOP2", "COMPANY2", "BUS2", "PAN2"}
        );

        // Call the method
        List<TouchRecord> touchRecords = tripsService.mapInfo(rawData);

        // Assertions
        assertEquals(2, touchRecords.size());
        // Add more assertions to validate the mapping of data
    }*/

    // Add more test methods for other public methods in TripsService

}