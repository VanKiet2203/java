package com.Admin.statistics.DTO;

/** Enum for time filter types */
public enum TimeFilterType {
    YEAR("By Year", "year"),
    QUARTER("By Quarter", "quarter"),
    MONTH("By Month", "month"),
    ALL_TIME("All Time", "all_time");

    private final String displayName;
    private final String value;

    TimeFilterType(String displayName, String value) {
        this.displayName = displayName;
        this.value = value;
    }
    public String getDisplayName() { return displayName; }
    public String getValue() { return value; }

    @Override public String toString() { return displayName; }
}
