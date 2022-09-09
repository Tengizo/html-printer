package com.kantora19.javahtmlprinter.controller;

import com.kantora19.javahtmlprinter.dto.PrintDTO;
import com.kantora19.javahtmlprinter.dto.Pulse;
import com.kantora19.javahtmlprinter.dto.Response;
import com.kantora19.javahtmlprinter.exception.AppException;
import com.kantora19.javahtmlprinter.service.PrinterService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;


/**
 * REST controller for managing the current user's account.
 */
@RestController
@RequestMapping("")
public class PrintController {

    private final PrinterService printerService;

    public PrintController(PrinterService printerService) {
        this.printerService = printerService;
    }


    @PostMapping("/print")
    public ResponseEntity<Response> print(@RequestBody PrintDTO printDTO) {
        Response response = printerService.print(printDTO);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/health")
    public ResponseEntity<Pulse> health(@RequestParam(value = "printerName") String printerName) {
        if (ObjectUtils.isEmpty(printerName)) {
            throw new AppException("Printer Name shouldn't be empty", 0);
        }
        return new ResponseEntity<>(printerService.checkStatus(printerName), HttpStatus.OK);
    }

    @GetMapping("/printers")
    public ResponseEntity<List<String>> printers() {
        return new ResponseEntity<>(printerService.getAvailablePrinters(), HttpStatus.OK);
    }


}
