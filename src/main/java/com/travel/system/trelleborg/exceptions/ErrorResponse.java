package com.travel.system.trelleborg.exceptions;

import lombok.Data;

@Data
public class ErrorResponse {
    private int code;
    private String description;
    private String detail;

    public ErrorResponse(int code, String description, String detail) {
        this.code = code;
        this.description = description;
        this.detail = detail;
    }
}
