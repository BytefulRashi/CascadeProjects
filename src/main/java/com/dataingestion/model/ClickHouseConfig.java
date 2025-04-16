package com.dataingestion.model;

import lombok.Data;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Min;

@Data
public class ClickHouseConfig {
    @NotBlank(message = "Host is required")
    private String host;

    @NotNull(message = "Port is required")
    @Min(value = 1, message = "Port must be greater than 0")
    private Integer port;

    @NotBlank(message = "Database is required")
    private String database;

    @NotBlank(message = "User is required")
    private String user;

    @NotBlank(message = "JWT token is required")
    private String jwtToken;

    private String table;

    public String getJdbcUrl() {
        return String.format("jdbc:clickhouse://%s:%d/%s", host, port, database);
    }
}