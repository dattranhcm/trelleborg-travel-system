package com.travel.system.trelleborg.components;

import com.travel.system.trelleborg.dto.CSVConvertible;
import com.travel.system.trelleborg.dto.CSVFile;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
public class FileProcessor {
    @SneakyThrows
    public List<String[]> readRawData(MultipartFile file) {
        List<String[]> rawData = new ArrayList<>();
        BufferedReader br = new BufferedReader(new InputStreamReader(file.getInputStream()));
        String line;
        String headerLine = br.readLine(); // Read header line
        if(Objects.isNull(headerLine)) {
            throw new RuntimeException("Invalid input csv file");
        }
        String[] columnNames = headerLine.split(",");
        while ((line = br.readLine()) != null) {
            String[] values = line.split(",", -1);
            if (values.length < columnNames.length) {
                String[] extendedValues = new String[columnNames.length];
                System.arraycopy(values, 0, extendedValues, 0, values.length);
                for (int i = values.length; i < extendedValues.length; i++) {
                    extendedValues[i] = null; // Set missing values to null
                }
                values = extendedValues;
            }
            rawData.add(values);
        }
        return rawData;
    }

    @SneakyThrows
    public String generateCSVContent(List<?> data) {
        if (data.isEmpty()) {
            return "";
        }
        StringWriter writer = new StringWriter();
        // Write header line
        writer.write(String.join(",", ((CSVConvertible) data.get(0)).getFieldNames()));
        writer.write("\n");
        // Write data rows
        for (Object obj : data) {
            writer.write(String.join(",", ((CSVConvertible) obj).toCSV()));
            writer.write("\n");
        }
        writer.close();
        return writer.toString();
    }

    public ByteArrayOutputStream addCsvFilesToZip(CSVFile[] csvFiles) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try (ZipOutputStream zipOut = new ZipOutputStream(byteArrayOutputStream)) {
            Arrays.asList(csvFiles).forEach(file -> addCsvFileToZip(zipOut, file.getName(), file.getContent()));
        } catch (IOException ex) {
            throw ex;
        }
        return byteArrayOutputStream;
    }

    @SneakyThrows(IOException.class)
    private void addCsvFileToZip(ZipOutputStream zipOut, String fileName, String content) {
        zipOut.putNextEntry(new ZipEntry(fileName));
        zipOut.write(content.getBytes());
        zipOut.closeEntry();
    }
}
