package com.kantora19.javahtmlprinter.service;

import com.github.anastaciocintra.escpos.EscPos;
import com.github.anastaciocintra.escpos.EscPosConst;
import com.github.anastaciocintra.escpos.image.*;
import com.github.anastaciocintra.output.PrinterOutputStream;
import com.kantora19.javahtmlprinter.dto.PrintDTO;
import com.kantora19.javahtmlprinter.dto.Pulse;
import com.kantora19.javahtmlprinter.dto.Response;
import com.kantora19.javahtmlprinter.exception.AppException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.xhtmlrenderer.simple.Graphics2DRenderer;

import javax.print.PrintService;
import javax.print.attribute.Attribute;
import javax.print.attribute.AttributeSet;
import javax.print.attribute.standard.PrinterIsAcceptingJobs;
import javax.print.attribute.standard.PrinterStateReason;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@Slf4j
public class PrinterService {
    private final Bitonal algorithm = new BitonalThreshold(127);


    public Response print(PrintDTO printDTO) {
        log.info("Got Print request:  " + printDTO.toString());
        BufferedImage bi = null;
        try {
            bi = convertHtmlToImage(printDTO);
        } catch (IOException e) {
            log.error("Error occurred during", e);
        }
        doPrint(bi, printDTO);
        return Response.builder().status(1).build();
    }

    public List<String> getAvailablePrinters() {
        return List.of(PrinterOutputStream.getListPrintServicesNames());
    }

    public Pulse checkStatus(String printerName) {
        PrintService printService = PrinterOutputStream.getPrintServiceByName(printerName);
        AttributeSet attributes = printService.getAttributes();
        Attribute printerIsAcceptingJobs = attributes.get(PrinterIsAcceptingJobs.class);
        Attribute psr = attributes.get(PrinterStateReason.class);

        if (!Objects.isNull(attributes.get(PrinterStateReason.class))) {
            return Pulse.builder().health(psr.toString()).status(-1).build();
        }
        if (printerIsAcceptingJobs != null) {
            return Pulse.builder()
                    .status(1)
                    .health(printerIsAcceptingJobs.toString())
                    .build();
        }
        return Pulse.builder().health("UNKNOWN").status(1).build();
    }

    private void doPrint(BufferedImage bi, PrintDTO printDTO) {
        String printer = printDTO.getPrinterName();

        log.info("Executing Print on: " + printer);
        try {
            EscPos escpos = getEscPos(printer);

            EscPosImage escposImage = new EscPosImage(new CoffeeImageImpl(bi), algorithm);
            RasterBitImageWrapper imageWrapper = new RasterBitImageWrapper();
            imageWrapper.setJustification(EscPosConst.Justification.Center);
            escpos.write(imageWrapper, escposImage);
            feedCutClose(escpos);

        } catch (IOException ex) {
            log.error("error occurred during print: ", ex);
            throw new AppException("Error during the print: " + ex.getMessage(), 0);
        }

    }

    private BufferedImage convertHtmlToImage(PrintDTO printDTO) throws IOException {
        File temp = getTempFile(printDTO)
                .orElseThrow(() -> new AppException("Unable to create File", 500));
        String url = temp.toURI().toURL().toExternalForm();
        BufferedImage image = Graphics2DRenderer.renderToImageAutoSize(url, printDTO.getWidth(), BufferedImage.TYPE_INT_ARGB);
        if (temp.delete()) {
            log.info("temporary file deleted successfully");
        }
        return image;

    }

    private Optional<File> getTempFile(PrintDTO printDTO) {
        try {
            File temp = File.createTempFile("image/bill", ".html");
            temp.deleteOnExit();

            // Write to temp file
            BufferedWriter out = new BufferedWriter(new FileWriter(temp));
            String logoImage =
                    ClassLoader.getSystemResource("static/kantora-t.png").toString();
            String georgia =
                    ClassLoader.getSystemResource("static/geo.png").toString();
            String html = printDTO.getHtml().replace("${{LOGO_IMAGE_SOURCE}}", logoImage);
            html = html.replace("${{GEO_MAP_SOURCE}}", georgia);
            out.write(html);
            out.close();
            return Optional.of(temp);
        } catch (IOException e) {
            log.error("Exception occured during temp file creation", e);
        }
        return Optional.empty();
    }

    private EscPos getEscPos(String printerName) throws IOException {
        PrintService printService = PrinterOutputStream.getPrintServiceByName(printerName);
        return new EscPos(new PrinterOutputStream(printService));
    }

    private void feedCutClose(EscPos escpos) throws IOException {
        escpos.feed(5);
        escpos.cut(EscPos.CutMode.FULL);
        escpos.close();
    }

    private static void sendCommand(EscPos escpos, int... args) throws IOException {
        byte[] bts = getBytes(args);
        escpos.write(bts, 0, bts.length);
    }

    private static byte[] getBytes(int... args) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        Arrays.stream(args).forEach(byteArrayOutputStream::write);
        return byteArrayOutputStream.toByteArray();
    }
}
