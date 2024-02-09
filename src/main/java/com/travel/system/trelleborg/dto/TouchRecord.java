package com.travel.system.trelleborg.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class TouchRecord {
    private int id;
    private String dateTimeUTC;
    private String touchType;
    private String stopId;
    private String companyID;
    private String busId;
    private String pan;
}
