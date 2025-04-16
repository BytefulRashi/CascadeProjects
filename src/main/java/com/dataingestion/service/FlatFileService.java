package com.dataingestion.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

@Slf4j
@Service
public class FlatFileService {
    
    public List<String> getColumns(MultipartFile file, char delimiter) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            CSVFormat format = CSVFormat.DEFAULT.withDelimiter(delimiter).withHeader().withSkipHeaderRecord(false);
            CSVParser parser = format.parse(reader);
            List<String> headers = new ArrayList<>(parser.getHeaderMap().keySet());
            log.info("Found {} columns in file {}", headers.size(), file.getOriginalFilename());
            return headers;
        }
    }
    
    public List<Map<String, String>> previewData(MultipartFile file, char delimiter, List<String> selectedColumns) throws IOException {
        List<Map<String, String>> preview = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            CSVFormat format = CSVFormat.DEFAULT.withDelimiter(delimiter).withHeader();
            CSVParser parser = format.parse(reader);
            
            int count = 0;
            for (CSVRecord record : parser) {
                if (count >= 100) break;
                
                Map<String, String> row = new HashMap<>();
                for (String column : selectedColumns) {
                    row.put(column, record.get(column));
                }
                preview.add(row);
                count++;
            }
            log.info("Generated preview with {} rows from file {}", preview.size(), file.getOriginalFilename());
            return preview;
        }
    }
    
    public Path saveToFile(List<Map<String, Object>> data, List<String> columns, String outputFile) throws IOException {
        Path outputPath = Path.of(outputFile);
        try (CSVPrinter printer = new CSVPrinter(Files.newBufferedWriter(outputPath), CSVFormat.DEFAULT)) {
            // Write header
            printer.printRecord(columns);
            
            // Write data
            for (Map<String, Object> row : data) {
                List<Object> values = new ArrayList<>();
                for (String column : columns) {
                    values.add(row.get(column));
                }
                printer.printRecord(values);
            }
        }
        log.info("Saved {} records to file {}", data.size(), outputFile);
        return outputPath;
    }
    
    public int getRecordCount(Path filePath, char delimiter) throws IOException {
        try (BufferedReader reader = Files.newBufferedReader(filePath);
             CSVParser parser = CSVFormat.DEFAULT.withDelimiter(delimiter).withHeader().parse(reader)) {
            int count = 0;
            for (CSVRecord record : parser) {
                count++;
            }
            return count;
        }
    }
    
    public void validateFile(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File is required");
        }
        
        String contentType = file.getContentType();
        if (contentType != null && !contentType.equals("text/csv") && !contentType.equals("application/vnd.ms-excel")) {
            throw new IllegalArgumentException("Only CSV files are supported");
        }
        
        // Check file size (e.g., limit to 100MB)
        if (file.getSize() > 100 * 1024 * 1024) {
            throw new IllegalArgumentException("File size exceeds maximum limit of 100MB");
        }
    }
}