package com.travel.system.trelleborg.dto;

public class CSVFile {
    private String name;
    private String content;

    public CSVFile(String name, String content) {
        this.name = name;
        this.content = content;
    }

    public String getName() {
        return name;
    }

    public String getContent() {
        return content;
    }
}
