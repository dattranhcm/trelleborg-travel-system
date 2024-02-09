package com.travel.system.trelleborg.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class Trips implements CSVConvertible {
    private String started;
    private String finished;
    private long durationSec;
    private String fromStopId;
    private String toStopId;
    private BigDecimal chargeAmount;
    private String companyId;
    private String busId;
    private String hashedPan;
    private String status;

    @Override
    public String[] toCSV() {
        return new String[]{started, finished, String.valueOf(durationSec), fromStopId, toStopId, String.valueOf(chargeAmount), companyId, busId, hashedPan, status};
    }

    @Override
    public String[] getFieldNames() {
        return new String[]{"started", "finished", "durationSec", "fromStopId", "toStopId", "chargeAmount", "companyId", "busId", "hashedPan", "status"};
    }
}
