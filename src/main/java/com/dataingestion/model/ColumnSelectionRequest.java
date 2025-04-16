package com.dataingestion.model;

import lombok.Data;
import java.util.List;

@Data
public class ColumnSelectionRequest {
    private List<String> columns;
}
