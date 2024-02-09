package com.travel.system.trelleborg.dto;

public interface CSVConvertible {
    String[] toCSV();
    String[] getFieldNames();
}
