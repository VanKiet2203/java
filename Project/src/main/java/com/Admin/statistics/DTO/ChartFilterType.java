package com.Admin.statistics.DTO;

/** Data type comparison displayed on chart */
public enum ChartFilterType {
    PRODUCT_REVENUE("Revenue by Product", "product_revenue"),
    CATEGORY_REVENUE("Revenue by Category", "category_revenue"),
    SUPPLIER_REVENUE("Revenue by Supplier", "supplier_revenue"),
    TOTAL_REVENUE_TREND("Total Revenue Trend", "total_revenue_trend");

    private final String displayName;
    private final String value;

    ChartFilterType(String displayName, String value) {
        this.displayName = displayName;
        this.value = value;
    }
    public String getDisplayName() { return displayName; }
    public String getValue() { return value; }
    @Override public String toString() { return displayName; }
}
