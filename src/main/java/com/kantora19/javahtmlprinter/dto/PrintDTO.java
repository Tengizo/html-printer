package com.kantora19.javahtmlprinter.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PrintDTO {
    private String html;
    private int height;
    private int width;
    private String printerName;

}
