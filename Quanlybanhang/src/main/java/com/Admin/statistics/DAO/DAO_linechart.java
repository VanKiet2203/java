package com.Admin.statistics.DAO;

import com.ComponentandDatabase.Database_Connection.DatabaseConnection;
import com.Admin.statistics.DTO.ChartFilterData;
import java.awt.BasicStroke;
import java.awt.Color;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.text.DecimalFormat;
import org.jfree.chart.axis.NumberAxis;
import java.math.BigDecimal;
import java.util.concurrent.*;
import org.jfree.chart.*;
import org.jfree.chart.axis.*;
import org.jfree.chart.plot.*;
import org.jfree.chart.renderer.xy.*;
import org.jfree.data.time.*;

public class DAO_linechart {
    private TimeSeriesCollection dataset;
    private JFreeChart chart;
    private ChartPanel chartPanel;
    private ScheduledExecutorService scheduler;
    private static final int REFRESH_INTERVAL = 60;
    private String statisticsType = "month";
    private volatile boolean isRunning = true;
    private Connection activeConnection;
    private volatile ChartFilterData currentFilter;

    public DAO_linechart() {
        dataset = new TimeSeriesCollection();
        currentFilter = new ChartFilterData();
        createChart();
        initAutoRefresh();
    }

    private void initAutoRefresh() {
        scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(() -> {
            if (isRunning) {
                refreshData();
            }
        }, 0, REFRESH_INTERVAL, TimeUnit.SECONDS);
    }
    
    public void setFilter(ChartFilterData filterData) {
        this.currentFilter = filterData != null ? new ChartFilterData(filterData) : new ChartFilterData();
    }

    public void setStatisticsType(String type) {
        if (Arrays.asList("day", "month", "year").contains(type)) {
            this.statisticsType = type;
            refreshData();
        }
    }

    public ChartPanel getChartPanel() {
        return getChartPanel(new ChartFilterData());
    }
    
    public ChartPanel getChartPanel(ChartFilterData filterData) {
        if (filterData != null) {
            setFilter(filterData);
        }
        updateChartWithFilter(currentFilter);
        return chartPanel;
    }

    private void createChart() {
        chart = ChartFactory.createTimeSeriesChart(
            "Revenue Statistics (" + statisticsType + ")",
            statisticsType.equals("day") ? "Date" : 
                (statisticsType.equals("month") ? "Month" : "Year"),
            "Revenue (VND)",
            dataset,
            true,
            true,
            false
        );

        XYPlot plot = (XYPlot) chart.getPlot();
        plot.setBackgroundPaint(Color.WHITE);
        plot.setDomainGridlinePaint(Color.LIGHT_GRAY);
        plot.setRangeGridlinePaint(Color.LIGHT_GRAY);

        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
        renderer.setSeriesPaint(0, new Color(0, 102, 204));
        renderer.setSeriesStroke(0, new BasicStroke(2.5f));
        renderer.setSeriesShapesVisible(0, true);
        plot.setRenderer(renderer);

        DateAxis axis = (DateAxis) plot.getDomainAxis();
        axis.setDateFormatOverride(new SimpleDateFormat(
            statisticsType.equals("day") ? "dd MMM yyyy" : 
                (statisticsType.equals("month") ? "MMM yyyy" : "yyyy"),
            Locale.ENGLISH
        ));

        // 🔹 Fix Y-axis formatting (disable scientific notation)
        NumberAxis yAxis = (NumberAxis) plot.getRangeAxis();
        yAxis.setNumberFormatOverride(new DecimalFormat("#,###.00")); // Ensures numbers like 10,000,000.00

        chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new java.awt.Dimension(800, 500));
        chartPanel.setMouseWheelEnabled(false);
        chartPanel.setDomainZoomable(false);
        chartPanel.setRangeZoomable(false);
    }
    private synchronized void refreshData() {
        try {
            Map<String, BigDecimal> revenueData = fetchDataFromDatabase(currentFilter);
            if (!revenueData.isEmpty()) {
                updateDataset(revenueData, currentFilter);
                updateChartTitle(currentFilter);
            }
        } catch (SQLException e) {
            handleDatabaseError(e);
        }
    }
    
    private Map<String, BigDecimal> fetchDataFromDatabase(ChartFilterData filterData) throws SQLException {
        Map<String, BigDecimal> data = new TreeMap<>();
        
        // Xác định date format dựa trên time filter
        String dateFormat = getDateFormat(filterData);
        String sql = buildLineChartSQLQuery(filterData, dateFormat);

        try {
            // Use active connection or create new one
            if (activeConnection == null || activeConnection.isClosed()) {
                activeConnection = DatabaseConnection.connect();
                // Set timeout to prevent hanging
                activeConnection.setNetworkTimeout(Executors.newSingleThreadExecutor(), 5000);
            }

            try (PreparedStatement pst = activeConnection.prepareStatement(sql);
                 ResultSet rs = pst.executeQuery()) {

                while (rs.next()) {
                    String period = rs.getString("period");
                    BigDecimal revenue = null;
                    
                    // Try to get total_revenue first, then total_quantity
                    revenue = rs.getBigDecimal("total_revenue");
                    if (revenue == null) {
                        BigDecimal quantity = rs.getBigDecimal("total_quantity");
                        revenue = quantity != null ? quantity : BigDecimal.ZERO;
                    }
                    
                    if (period != null && revenue != null) {
                        data.put(period, revenue);
                    }
                }
            }
        } catch (SQLException e) {
            closeActiveConnection();
            throw e;
        }

        return data;
    }

    private void handleDatabaseError(SQLException e) {
        if (e.getMessage() != null && e.getMessage().contains("Socket closed")) {
            closeActiveConnection();
            return;
        }
        System.err.println("Database error: " + e.getMessage());
        e.printStackTrace();
    }

    private void closeActiveConnection() {
        try {
            if (activeConnection != null && !activeConnection.isClosed()) {
                activeConnection.close();
            }
        } catch (SQLException e) {
            System.err.println("Error closing connection: " + e.getMessage());
        }
        activeConnection = null;
    }

    private String getDateFormat(ChartFilterData filterData) {
        if (!filterData.hasTimeFilter()) {
            return "yyyy-MM"; // Default to month
        }
        
        switch (filterData.getTimeFilterType()) {
            case YEAR:
                return "yyyy";
            case QUARTER:
                return "yyyy-Q"; // Custom format for quarters
            case MONTH:
                return "yyyy-MM";
            default:
                return "yyyy-MM";
        }
    }
    
    private String buildLineChartSQLQuery(ChartFilterData filterData, String dateFormat) {
        StringBuilder sql = new StringBuilder();
        String timeFilter = filterData.getTimeWhereClause();
        
        switch (filterData.getDataType()) {
            case REVENUE:
                if (dateFormat.equals("yyyy-Q")) {
                    sql.append("SELECT CONCAT(YEAR(bd.Date_Exported), '-Q', DATEPART(QUARTER, bd.Date_Exported)) AS period, ")
                       .append("SUM(bd.Total_Price_After) AS total_revenue ")
                       .append("FROM Bill_Exported_Details bd ")
                       .append("WHERE bd.Status = 'Available' ")
                       .append(timeFilter)
                       .append(" GROUP BY YEAR(bd.Date_Exported), DATEPART(QUARTER, bd.Date_Exported) ")
                       .append("ORDER BY YEAR(bd.Date_Exported), DATEPART(QUARTER, bd.Date_Exported)");
                } else {
                    sql.append("SELECT FORMAT(bd.Date_Exported, '").append(dateFormat).append("') AS period, ")
                       .append("SUM(bd.Total_Price_After) AS total_revenue ")
                       .append("FROM Bill_Exported_Details bd ")
                       .append("WHERE bd.Status = 'Available' ")
                       .append(timeFilter)
                       .append(" GROUP BY FORMAT(bd.Date_Exported, '").append(dateFormat).append("') ")
                       .append("ORDER BY period");
                }
                break;
                
            case QUANTITY_SOLD:
                if (dateFormat.equals("yyyy-Q")) {
                    sql.append("SELECT CONCAT(YEAR(bd.Date_Exported), '-Q', DATEPART(QUARTER, bd.Date_Exported)) AS period, ")
                       .append("SUM(bd.Sold_Quantity) AS total_quantity ")
                       .append("FROM Bill_Exported_Details bd ")
                       .append("WHERE bd.Status = 'Available' ")
                       .append(timeFilter)
                       .append(" GROUP BY YEAR(bd.Date_Exported), DATEPART(QUARTER, bd.Date_Exported) ")
                       .append("ORDER BY YEAR(bd.Date_Exported), DATEPART(QUARTER, bd.Date_Exported)");
                } else {
                    sql.append("SELECT FORMAT(bd.Date_Exported, '").append(dateFormat).append("') AS period, ")
                       .append("SUM(bd.Sold_Quantity) AS total_quantity ")
                       .append("FROM Bill_Exported_Details bd ")
                       .append("WHERE bd.Status = 'Available' ")
                       .append(timeFilter)
                       .append(" GROUP BY FORMAT(bd.Date_Exported, '").append(dateFormat).append("') ")
                       .append("ORDER BY period");
                }
                break;
                
            default:
                // Default to revenue
                if (dateFormat.equals("yyyy-Q")) {
                    sql.append("SELECT CONCAT(YEAR(bd.Date_Exported), '-Q', DATEPART(QUARTER, bd.Date_Exported)) AS period, ")
                       .append("SUM(bd.Total_Price_After) AS total_revenue ")
                       .append("FROM Bill_Exported_Details bd ")
                       .append("WHERE bd.Status = 'Available' ")
                       .append(timeFilter)
                       .append(" GROUP BY YEAR(bd.Date_Exported), DATEPART(QUARTER, bd.Date_Exported) ")
                       .append("ORDER BY YEAR(bd.Date_Exported), DATEPART(QUARTER, bd.Date_Exported)");
                } else {
                    sql.append("SELECT FORMAT(bd.Date_Exported, '").append(dateFormat).append("') AS period, ")
                       .append("SUM(bd.Total_Price_After) AS total_revenue ")
                       .append("FROM Bill_Exported_Details bd ")
                       .append("WHERE bd.Status = 'Available' ")
                       .append(timeFilter)
                       .append(" GROUP BY FORMAT(bd.Date_Exported, '").append(dateFormat).append("') ")
                       .append("ORDER BY period");
                }
                break;
        }
        
        return sql.toString();
    }
    
    private void updateChartWithFilter(ChartFilterData filterData) {
        try {
            Map<String, BigDecimal> newData = fetchDataFromDatabase(filterData);
            if (!newData.isEmpty()) {
                updateDataset(newData, filterData);
                updateChartTitle(filterData);
            }
        } catch (SQLException e) {
            handleDatabaseError(e);
        }
    }
    
    private void updateDataset(Map<String, BigDecimal> data, ChartFilterData filterData) {
        dataset.removeAllSeries();
        TimeSeries series = new TimeSeries(getSeriesName(filterData));
        
        String dateFormat = getDateFormat(filterData);
        
        data.forEach((period, value) -> {
            String[] parts = period.split("-");
            int year = Integer.parseInt(parts[0]);
            
            if (dateFormat.equals("yyyy")) {
                series.add(new Year(year), value);
            } else if (dateFormat.equals("yyyy-Q")) {
                int quarter = Integer.parseInt(parts[1].substring(1)); // Remove 'Q' prefix
                series.add(new Quarter(quarter, year), value);
            } else if (dateFormat.equals("yyyy-MM")) {
                int month = Integer.parseInt(parts[1]);
                series.add(new Month(month, year), value);
            } else {
                // Default to month
                int month = Integer.parseInt(parts[1]);
                series.add(new Month(month, year), value);
            }
        });
        
        dataset.addSeries(series);
        if (chart != null) {
            chart.fireChartChanged();
        }
    }
    
    private String getSeriesName(ChartFilterData filterData) {
        return filterData.getDataType().getDisplayName();
    }
    
    private void updateChartTitle(ChartFilterData filterData) {
        if (chart != null) {
            String title = getChartTitle(filterData);
            chart.setTitle(title);
            chart.fireChartChanged();
        }
    }
    
    private String getChartTitle(ChartFilterData filterData) {
        String baseTitle = filterData.getDataType().getDisplayName();
        String timeInfo = "";
        
        if (filterData.hasTimeFilter()) {
            switch (filterData.getTimeFilterType()) {
                case YEAR:
                    timeInfo = " - Năm " + filterData.getYear();
                    break;
                case QUARTER:
                    timeInfo = " - Quý " + filterData.getQuarter() + " năm " + filterData.getYear();
                    break;
                case MONTH:
                    timeInfo = " - Tháng " + filterData.getMonth() + " năm " + filterData.getYear();
                    break;
                case ALL_TIME:
                    // Không cần thêm thông tin thời gian
                    break;
            }
        }
        
        return baseTitle + timeInfo;
    }

    public void shutdown() {
        isRunning = false;
        if (scheduler != null) {
            scheduler.shutdownNow();
        }
        closeActiveConnection();
    }
}