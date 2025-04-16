package com.dataingestion.controller;

import com.dataingestion.model.ClickHouseConfig;
import com.dataingestion.service.ClickHouseService;
import com.dataingestion.service.FlatFileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.nio.file.Path;
import java.util.*;

@Slf4j
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Validated
public class IngestionController {
    
    private final ClickHouseService clickHouseService;
    private final FlatFileService flatFileService;
    
    @PostMapping("/clickhouse/test")
    public ResponseEntity<?> testConnection(@Valid @RequestBody ClickHouseConfig config) {
        try {
            clickHouseService.getConnection(config);
            return ResponseEntity.ok(Map.of("message", "Connection successful"));
        } catch (Exception e) {
            log.error("Connection test failed", e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    @PostMapping("/clickhouse/tables")
    public ResponseEntity<?> getTables(@Valid @RequestBody ClickHouseConfig config) {
        try {
            List<String> tables = clickHouseService.getTables(config);
            return ResponseEntity.ok(tables);
        } catch (Exception e) {
            log.error("Failed to get tables", e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    @PostMapping("/clickhouse/columns")
    public ResponseEntity<?> getColumns(@Valid @RequestBody ClickHouseConfig config) {
        try {
            List<String> columns = clickHouseService.getColumns(config);
            return ResponseEntity.ok(columns);
        } catch (Exception e) {
            log.error("Failed to get columns", e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    @PostMapping("/clickhouse/preview")
    public ResponseEntity<?> previewClickHouseData(
            @Valid @RequestBody ClickHouseConfig config,
            @RequestParam List<String> columns) {
        try {
            List<Map<String, Object>> preview = clickHouseService.previewData(config, columns);
            return ResponseEntity.ok(preview);
        } catch (Exception e) {
            log.error("Failed to preview data", e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    @PostMapping("/file/columns")
    public ResponseEntity<?> getFileColumns(
            @RequestParam("file") MultipartFile file,
            @RequestParam(defaultValue = ",") char delimiter) {
        try {
            flatFileService.validateFile(file);
            List<String> columns = flatFileService.getColumns(file, delimiter);
            return ResponseEntity.ok(columns);
        } catch (Exception e) {
            log.error("Failed to get file columns", e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    @PostMapping("/file/preview")
    public ResponseEntity<?> previewFileData(
            @RequestParam("file") MultipartFile file,
            @RequestParam(defaultValue = ",") char delimiter,
            @RequestParam List<String> columns) {
        try {
            flatFileService.validateFile(file);
            List<Map<String, String>> preview = flatFileService.previewData(file, delimiter, columns);
            return ResponseEntity.ok(preview);
        } catch (Exception e) {
            log.error("Failed to preview file data", e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    @PostMapping("/ingest/clickhouse-to-file")
    public ResponseEntity<?> ingestToFile(
            @Valid @RequestBody ClickHouseConfig config,
            @RequestParam List<String> columns) {
        try {
            // Export data from ClickHouse
            String outputFile = "export_" + System.currentTimeMillis() + ".csv";
            int recordCount = clickHouseService.exportToFile(config, columns, outputFile);
            
            return ResponseEntity.ok(Map.of(
                "message", "Data exported successfully",
                "recordCount", recordCount,
                "outputFile", outputFile
            ));
        } catch (Exception e) {
            log.error("Failed to export data to file", e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    @PostMapping("/ingest/file-to-clickhouse")
    public ResponseEntity<?> ingestToClickHouse(
            @RequestParam("file") MultipartFile file,
            @RequestParam(defaultValue = ",") char delimiter,
            @Valid @RequestBody ClickHouseConfig config,
            @RequestParam List<String> columns) {
        try {
            flatFileService.validateFile(file);
            
            // Import data to ClickHouse
            int recordCount = clickHouseService.importFromFile(config, file, columns);
            
            return ResponseEntity.ok(Map.of(
                "message", "Data imported successfully",
                "recordCount", recordCount
            ));
        } catch (Exception e) {
            log.error("Failed to import data to ClickHouse", e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}