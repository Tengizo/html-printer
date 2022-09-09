package com.kantora19.javahtmlprinter;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

@SpringBootApplication
public class JavaHtmlPrinterApplication {

    public static void main(String[] args) {
        SpringApplicationBuilder builder = new SpringApplicationBuilder(JavaHtmlPrinterApplication.class);

        builder.headless(false);
        builder.run(args);
    }


}
