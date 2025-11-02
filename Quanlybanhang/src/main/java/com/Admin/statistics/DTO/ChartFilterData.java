package com.Admin.statistics.DTO;

/**
 * Class chứa dữ liệu filter cho biểu đồ
 */
public class ChartFilterData {
    private TimeFilterType timeFilterType;
    private int year;
    private int quarter;
    private int month;
    private ChartFilterType dataType;
    
    public ChartFilterData() {
        this.timeFilterType = TimeFilterType.ALL_TIME;
        this.year = java.time.LocalDate.now().getYear();
        this.quarter = 1;
        this.month = 1;
        this.dataType = ChartFilterType.REVENUE;
    }
    
    public ChartFilterData(TimeFilterType timeFilterType, int year, int quarter, int month, ChartFilterType dataType) {
        this.timeFilterType = timeFilterType;
        this.year = year;
        this.quarter = quarter;
        this.month = month;
        this.dataType = dataType;
    }
    
    // Copy constructor
    public ChartFilterData(ChartFilterData other) {
        if (other != null) {
            this.timeFilterType = other.timeFilterType;
            this.year = other.year;
            this.quarter = other.quarter;
            this.month = other.month;
            this.dataType = other.dataType;
        } else {
            this.timeFilterType = TimeFilterType.ALL_TIME;
            this.year = java.time.LocalDate.now().getYear();
            this.quarter = 1;
            this.month = 1;
            this.dataType = ChartFilterType.REVENUE;
        }
    }
    
    // Getters and Setters
    public TimeFilterType getTimeFilterType() {
        return timeFilterType;
    }
    
    public void setTimeFilterType(TimeFilterType timeFilterType) {
        this.timeFilterType = timeFilterType;
    }
    
    public int getYear() {
        return year;
    }
    
    public void setYear(int year) {
        this.year = year;
    }
    
    public int getQuarter() {
        return quarter;
    }
    
    public void setQuarter(int quarter) {
        this.quarter = quarter;
    }
    
    public int getMonth() {
        return month;
    }
    
    public void setMonth(int month) {
        this.month = month;
    }
    
    public ChartFilterType getDataType() {
        return dataType;
    }
    
    public void setDataType(ChartFilterType dataType) {
        this.dataType = dataType;
    }
    
    /**
     * Kiểm tra xem có cần filter theo thời gian không
     */
    public boolean hasTimeFilter() {
        return timeFilterType != TimeFilterType.ALL_TIME;
    }
    
    /**
     * Lấy điều kiện WHERE cho SQL query
     */
    public String getTimeWhereClause() {
        if (!hasTimeFilter()) {
            return "";
        }
        
        switch (timeFilterType) {
            case YEAR:
                return String.format("AND YEAR(bed.Date_Exported) = %d", year);
            case QUARTER:
                return String.format("AND YEAR(bed.Date_Exported) = %d AND DATEPART(QUARTER, bed.Date_Exported) = %d", year, quarter);
            case MONTH:
                return String.format("AND YEAR(bed.Date_Exported) = %d AND MONTH(bed.Date_Exported) = %d", year, month);
            default:
                return "";
        }
    }
    
    @Override
    public String toString() {
        return String.format("ChartFilterData{timeFilter=%s, year=%d, quarter=%d, month=%d, dataType=%s}", 
                           timeFilterType.getDisplayName(), year, quarter, month, dataType.getDisplayName());
    }
}
