package com.kantora19.javahtmlprinter.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Response {
    private int status;

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }
}
