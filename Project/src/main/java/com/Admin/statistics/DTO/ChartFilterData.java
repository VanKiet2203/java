package com.Admin.statistics.DTO;

import java.time.LocalDate;

/** Dữ liệu filter cho biểu đồ */
public class ChartFilterData {
    private TimeFilterType timeFilterType;
    private int year;
    private int quarter;   // 1..4
    private int month;     // 1..12
    private ChartFilterType dataType;

    public ChartFilterData() {
        this.timeFilterType = TimeFilterType.ALL_TIME;
        this.year = LocalDate.now().getYear();
        this.quarter = 1;
        this.month = LocalDate.now().getMonthValue();
        this.dataType = ChartFilterType.PRODUCT_REVENUE;
    }

    public TimeFilterType getTimeFilterType() { return timeFilterType; }
    public void setTimeFilterType(TimeFilterType t) { this.timeFilterType = t; }

    public int getYear() { return year; }
    public void setYear(int y) { this.year = y; }

    public int getQuarter() { return quarter; }
    public void setQuarter(int q) { this.quarter = Math.max(1, Math.min(4, q)); }

    public int getMonth() { return month; }
    public void setMonth(int m) { this.month = Math.max(1, Math.min(12, m)); }

    public ChartFilterType getDataType() { return dataType; }
    public void setDataType(ChartFilterType d) { this.dataType = d; }

    public boolean hasTimeFilter() { return timeFilterType != TimeFilterType.ALL_TIME; }

    /** WHERE cho alias mặc định "bed" (Bill_Exported_Details) */
    public String getTimeWhereClause() {
        return getTimeWhereClause("bed");
    }

    /** WHERE theo alias tuỳ chỉnh (mặc định lọc theo bed.Date_Exported) */
    public String getTimeWhereClause(String tableAlias) {
        LocalDate now = LocalDate.now();

        switch (timeFilterType) {
            case YEAR:
                return " AND YEAR(" + tableAlias + ".Date_Exported) = " + year +
                       " AND " + tableAlias + ".Date_Exported <= CAST(GETDATE() AS date)";
            case QUARTER: {
                int startMonth = (quarter - 1) * 3 + 1; // 1,4,7,10
                int endMonth = startMonth + 2;
                return " AND YEAR(" + tableAlias + ".Date_Exported) = " + year +
                       " AND MONTH(" + tableAlias + ".Date_Exported) BETWEEN " + startMonth + " AND " + endMonth +
                       " AND " + tableAlias + ".Date_Exported <= CAST(GETDATE() AS date)";
            }
            case MONTH:
                return " AND YEAR(" + tableAlias + ".Date_Exported) = " + year +
                       " AND MONTH(" + tableAlias + ".Date_Exported) = " + month +
                       " AND " + tableAlias + ".Date_Exported <= CAST(GETDATE() AS date)";
            case ALL_TIME:
            default:
                return " AND " + tableAlias + ".Date_Exported <= CAST(GETDATE() AS date)";
        }
    }
}
