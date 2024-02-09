package com.travel.system.trelleborg.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class TripProcessResult {

    private List<Trips> tripData;
    private List<Trips> unprocessableTrip;
    private List<TripsSummary> tripSummary;
}
