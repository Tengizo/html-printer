package com.kantora19.javahtmlprinter.dto;

public enum PrinterStatus {
    ACCEPTING_JOBS("accepting-jobs");

    private final String statusName;

    PrinterStatus(String statusName) {
        this.statusName = statusName;
    }

    public String getStatusName() {
        return statusName;
    }
}
