package com.travel.system.trelleborg.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
@Data
@Builder
public class TripsSummary implements CSVConvertible{
    private String date;
    private String companyId;
    private String busId;
    private Integer completeTripCount;
    private Integer incompleteTripCount;
    private Integer cancelledTripCount;
    private BigDecimal totalCharges;

    public String toCsvString() {
        return String.format("%s,%s,%s,%d,%d,%d,%.2f\n",
                date, companyId, busId, completeTripCount, incompleteTripCount, cancelledTripCount, totalCharges);
    }

    @Override
    public String[] toCSV() {
        return new String[]{date, companyId, busId, String.valueOf(completeTripCount), String.valueOf(incompleteTripCount),
                String.valueOf(cancelledTripCount), String.valueOf(totalCharges)};
    }

    @Override
    public String[] getFieldNames() {
        return new String[]{"date", "companyId", "busId", "completeTripCount", "incompleteTripCount", "cancelledTripCount", "totalCharges"};
    }
}
