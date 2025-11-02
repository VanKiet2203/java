package com.Admin.statistics.DTO;

/**
 * Enum định nghĩa các loại filter thời gian
 */
public enum TimeFilterType {
    YEAR("Theo năm", "year"),
    QUARTER("Theo quý", "quarter"),
    MONTH("Theo tháng", "month"),
    ALL_TIME("Tất cả thời gian", "all_time");
    
    private final String displayName;
    private final String value;
    
    TimeFilterType(String displayName, String value) {
        this.displayName = displayName;
        this.value = value;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public String getValue() {
        return value;
    }
    
    @Override
    public String toString() {
        return displayName;
    }
}
