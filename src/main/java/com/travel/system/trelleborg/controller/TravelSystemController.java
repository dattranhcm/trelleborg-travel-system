package com.travel.system.trelleborg.controller;

import com.travel.system.trelleborg.components.FileProcessor;
import com.travel.system.trelleborg.dto.CSVFile;
import com.travel.system.trelleborg.dto.TripProcessResult;
import com.travel.system.trelleborg.service.TripsService;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.util.List;

@RestController
@RequestMapping("/api")
public class TravelSystemController {

    @Autowired
    private TripsService tripsService;

    @Autowired
    private FileProcessor fileProcessor;

    @GetMapping("/trips-data")
    @SneakyThrows
    public ResponseEntity<byte[]> generateTripsCSVFiles(@RequestParam("file") MultipartFile file) {
        TripProcessResult result = tripsService.process(file);
        CSVFile[] csvFiles = tripsService.generateCSVTripResultFiles(result);
        ByteArrayOutputStream byteArrayOutputStream = fileProcessor.addCsvFilesToZip(csvFiles);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", "multiple_csv_files.zip");
        return new ResponseEntity<>(byteArrayOutputStream.toByteArray(), headers, HttpStatus.OK);
    }
}
