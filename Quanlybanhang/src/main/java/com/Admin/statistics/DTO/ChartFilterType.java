package com.Admin.statistics.DTO;

/**
 * Enum định nghĩa các loại dữ liệu có thể hiển thị trên biểu đồ
 */
public enum ChartFilterType {
    REVENUE("Doanh thu", "revenue"),
    QUANTITY_SOLD("Số lượng bán theo sản phẩm", "quantity_sold");
    
    private final String displayName;
    private final String value;
    
    ChartFilterType(String displayName, String value) {
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
