package com.kantora19.javahtmlprinter.exception;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

@EqualsAndHashCode(callSuper = true)
@Data
public class AppException extends RuntimeException {
    private String message;
    private int status;


    public AppException(String message, int status) {
        super(message);
        this.status = status;
    }
}
