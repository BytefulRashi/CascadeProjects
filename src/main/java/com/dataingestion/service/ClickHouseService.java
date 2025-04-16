package com.dataingestion.service;

import com.dataingestion.model.ClickHouseConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.sql.*;
import java.util.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
public class ClickHouseService {
    
    public Connection getConnection(ClickHouseConfig config) throws SQLException {
        Properties properties = new Properties();
        properties.setProperty("user", config.getUser());
        properties.setProperty("password", config.getJwtToken());
        
        log.info("Connecting to ClickHouse at {}", config.getJdbcUrl());
        return DriverManager.getConnection(config.getJdbcUrl(), properties);
    }
    
    public List<String> getTables(ClickHouseConfig config) throws SQLException {
        List<String> tables = new ArrayList<>();
        try (Connection conn = getConnection(config)) {
            DatabaseMetaData metaData = conn.getMetaData();
            try (ResultSet rs = metaData.getTables(config.getDatabase(), null, "%", new String[]{"TABLE"})) {
                while (rs.next()) {
                    tables.add(rs.getString("TABLE_NAME"));
                }
            }
        }
        log.info("Found {} tables in database {}", tables.size(), config.getDatabase());
        return tables;
    }
    
    public List<String> getColumns(ClickHouseConfig config) throws SQLException {
        List<String> columns = new ArrayList<>();
        try (Connection conn = getConnection(config)) {
            DatabaseMetaData metaData = conn.getMetaData();
            try (ResultSet rs = metaData.getColumns(config.getDatabase(), null, config.getTable(), "%")) {
                while (rs.next()) {
                    columns.add(rs.getString("COLUMN_NAME"));
                }
            }
        }
        log.info("Found {} columns in table {}", columns.size(), config.getTable());
        return columns;
    }
    
    public List<Map<String, Object>> previewData(ClickHouseConfig config, List<String> selectedColumns) throws SQLException {
        List<Map<String, Object>> preview = new ArrayList<>();
        String columnList = String.join(", ", selectedColumns);
        String query = String.format("SELECT %s FROM %s LIMIT 100", columnList, config.getTable());
        
        try (Connection conn = getConnection(config);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();
            
            while (rs.next()) {
                Map<String, Object> row = new HashMap<>();
                for (int i = 1; i <= columnCount; i++) {
                    row.put(metaData.getColumnName(i), rs.getObject(i));
                }
                preview.add(row);
            }
        }
        log.info("Retrieved {} preview rows from table {}", preview.size(), config.getTable());
        return preview;
    }
    
    public int exportToFile(ClickHouseConfig config, List<String> selectedColumns, String outputFile) throws SQLException {
        String columnList = String.join(", ", selectedColumns);
        String query = String.format("SELECT %s FROM %s INTO OUTFILE '%s' FORMAT CSV",
            columnList, config.getTable(), outputFile);
            
        try (Connection conn = getConnection(config);
             Statement stmt = conn.createStatement()) {
            int result = stmt.executeUpdate(query);
            log.info("Exported {} records to file {}", result, outputFile);
            return result;
        }
    }
    
    public int importFromFile(ClickHouseConfig config, MultipartFile file, List<String> columns) throws SQLException, IOException {
        String columnList = String.join(", ", columns);
        String createTableQuery = generateCreateTableQuery(config.getTable(), columns);
        String insertQuery = String.format("INSERT INTO %s (%s) FORMAT CSV", config.getTable(), columnList);
        
        try (Connection conn = getConnection(config);
             Statement stmt = conn.createStatement();
             BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            
            // Create table if not exists
            stmt.execute(createTableQuery);
            
            // Import data
            StringBuilder data = new StringBuilder();
            String line;
            int count = 0;
            while ((line = reader.readLine()) != null) {
                data.append(line).append("\n");
                count++;
                
                // Batch insert every 1000 records
                if (count % 1000 == 0) {
                    stmt.execute(insertQuery + " " + data.toString());
                    data = new StringBuilder();
                }
            }
            
            // Insert remaining records
            if (data.length() > 0) {
                stmt.execute(insertQuery + " " + data.toString());
            }
            
            log.info("Imported {} records into table {}", count, config.getTable());
            return count;
        }
    }
    
    private String generateCreateTableQuery(String tableName, List<String> columns) {
        StringBuilder query = new StringBuilder();
        query.append("CREATE TABLE IF NOT EXISTS ").append(tableName).append(" (");
        
        for (int i = 0; i < columns.size(); i++) {
            query.append(columns.get(i)).append(" String");  // Using String as default type
            if (i < columns.size() - 1) {
                query.append(", ");
            }
        }
        
        query.append(") ENGINE = MergeTree() ORDER BY tuple()");
        return query.toString();
    }
}