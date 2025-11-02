package com.Admin.statistics.DAO;

import com.ComponentandDatabase.Database_Connection.DatabaseConnection;
import com.Admin.statistics.DTO.ChartFilterData;
import java.awt.Color;
import java.sql.*;
import java.sql.ResultSetMetaData;
import java.util.*;
import java.util.concurrent.*;
import org.jfree.chart.*;
import org.jfree.chart.axis.*;
import org.jfree.chart.plot.*;
import org.jfree.chart.renderer.category.*;
import org.jfree.data.category.*;

public class DAO_heatmap {
    private DefaultCategoryDataset dataset;
    private JFreeChart chart;
    private ChartPanel chartPanel;
    private ScheduledExecutorService scheduler;
    private static final int REFRESH_INTERVAL = 60;
    private volatile boolean isRunning = true;
    private Connection activeConnection;
    private volatile ChartFilterData currentFilter;

    public DAO_heatmap() {
        currentFilter = new ChartFilterData();
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

    public ChartPanel getChartPanel() {
        return getChartPanel(new ChartFilterData());
    }
    
    public ChartPanel getChartPanel(ChartFilterData filterData) {
        if (chartPanel == null) {
            createChart();
        }
        if (filterData != null) {
            setFilter(filterData);
        }
        updateChartWithFilter(currentFilter);
        return chartPanel;
    }

    private void createChart() {
        dataset = new DefaultCategoryDataset();
        
        CategoryAxis xAxis = new CategoryAxis("Months");
        NumberAxis yAxis = new NumberAxis("Sales Quantity");

        AreaRenderer renderer = new AreaRenderer();
        renderer.setSeriesPaint(0, new Color(0, 102, 204));
        renderer.setSeriesPaint(1, new Color(255, 153, 0));
        renderer.setSeriesPaint(2, new Color(204, 0, 102));

        CategoryPlot plot = new CategoryPlot(dataset, xAxis, yAxis, renderer);
        plot.setBackgroundPaint(Color.WHITE);
        plot.setDomainGridlinesVisible(true);
        plot.setRangeGridlinesVisible(true);

        chart = new JFreeChart("Sales Trend (Area Chart)", JFreeChart.DEFAULT_TITLE_FONT, plot, true);
        chart.setBackgroundPaint(Color.WHITE);
        chartPanel = new ChartPanel(chart);
        chartPanel.setDomainZoomable(false);     // 🚫 Không cho phép zoom theo trục ngang
        chartPanel.setRangeZoomable(false);
    }

    private synchronized void refreshData() {
        try {
            Map<String, Map<String, Integer>> newData = fetchDataFromDatabase(currentFilter);
            if (!newData.isEmpty()) {
                updateDataset(newData);
                updateChartTitle(currentFilter);
            }
        } catch (SQLException e) {
            handleDatabaseError(e);
        }
    }
    
    private Map<String, Map<String, Integer>> fetchDataFromDatabase(ChartFilterData filterData) throws SQLException {
        Map<String, Map<String, Integer>> data = new LinkedHashMap<>();
        String sql = buildHeatmapSQLQuery(filterData);

        try {
            // Use active connection or create new one
            if (activeConnection == null || activeConnection.isClosed()) {
                activeConnection = DatabaseConnection.connect();
                // Set timeout to prevent hanging
                activeConnection.setNetworkTimeout(Executors.newSingleThreadExecutor(), 5000);
            }

            try (PreparedStatement pst = activeConnection.prepareStatement(sql);
                 ResultSet rs = pst.executeQuery()) {

                ResultSetMetaData metaData = rs.getMetaData();
                int columnCount = metaData.getColumnCount();
                
                while (rs.next()) {
                    // Get the first column (month) and second column (name/category) depending on query
                    String month = rs.getString("month");
                    String categoryName = null;
                    int totalSold = 0;
                    
                    // Check columns to find the name column
                    for (int i = 1; i <= columnCount; i++) {
                        String columnName = metaData.getColumnName(i);
                        if (columnName.equals("Product_Name")) {
                            categoryName = rs.getString(i);
                            break;
                        }
                    }
                    
                    // Get total_sold or total_count
                    for (int i = 1; i <= columnCount; i++) {
                        String columnName = metaData.getColumnName(i);
                        if (columnName.equals("total_sold") || columnName.equals("total_count")) {
                            totalSold = rs.getInt(i);
                            break;
                        }
                    }
                    
                    if (categoryName != null && month != null) {
                        data.computeIfAbsent(month, k -> new LinkedHashMap<>()).put(categoryName, totalSold);
                    }
                }
            }
        } catch (SQLException e) {
            closeActiveConnection();
            throw e;
        }

        return data;
    }

    private void updateDataset(Map<String, Map<String, Integer>> newData) {
        if (dataset == null) {
            dataset = new DefaultCategoryDataset();
        }
        dataset.clear();

        for (Map.Entry<String, Map<String, Integer>> monthEntry : newData.entrySet()) {
            String month = monthEntry.getKey();
            for (Map.Entry<String, Integer> productEntry : monthEntry.getValue().entrySet()) {
                dataset.addValue(productEntry.getValue(), productEntry.getKey(), month);
            }
        }

        if (chart != null) {
            chart.fireChartChanged();
        }
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

    private String buildHeatmapSQLQuery(ChartFilterData filterData) {
        StringBuilder sql = new StringBuilder();
        String timeFilter = filterData.getTimeWhereClause();
        
        switch (filterData.getDataType()) {
            case REVENUE:
                sql.append("SELECT FORMAT(bed.Date_Exported, 'yyyy-MM') AS month, ")
                   .append("p.Product_Name, SUM(bed.Total_Price_After) AS total_sold ")
                   .append("FROM Bill_Exported_Details bed ")
                   .append("JOIN Product p ON bed.Product_ID = p.Product_ID ")
                   .append("WHERE bed.Status = 'Available' AND p.Status = 'Available' ")
                   .append(timeFilter)
                   .append(" GROUP BY p.Product_Name, FORMAT(bed.Date_Exported, 'yyyy-MM') ")
                   .append("ORDER BY FORMAT(bed.Date_Exported, 'yyyy-MM')");
                break;
                
            case QUANTITY_SOLD:
                sql.append("SELECT FORMAT(bed.Date_Exported, 'yyyy-MM') AS month, ")
                   .append("p.Product_Name, SUM(bed.Sold_Quantity) AS total_sold ")
                   .append("FROM Bill_Exported_Details bed ")
                   .append("JOIN Product p ON bed.Product_ID = p.Product_ID ")
                   .append("WHERE bed.Status = 'Available' AND p.Status = 'Available' ")
                   .append(timeFilter)
                   .append(" GROUP BY p.Product_Name, FORMAT(bed.Date_Exported, 'yyyy-MM') ")
                   .append("ORDER BY FORMAT(bed.Date_Exported, 'yyyy-MM')");
                break;
                
            default:
                // Default to revenue
                sql.append("SELECT FORMAT(bed.Date_Exported, 'yyyy-MM') AS month, ")
                   .append("p.Product_Name, SUM(bed.Total_Price_After) AS total_sold ")
                   .append("FROM Bill_Exported_Details bed ")
                   .append("JOIN Product p ON bed.Product_ID = p.Product_ID ")
                   .append("WHERE bed.Status = 'Available' AND p.Status = 'Available' ")
                   .append(timeFilter)
                   .append(" GROUP BY p.Product_Name, FORMAT(bed.Date_Exported, 'yyyy-MM') ")
                   .append("ORDER BY FORMAT(bed.Date_Exported, 'yyyy-MM')");
                break;
        }
        
        return sql.toString();
    }
    
    private void updateChartWithFilter(ChartFilterData filterData) {
        try {
            Map<String, Map<String, Integer>> newData = fetchDataFromDatabase(filterData);
            if (!newData.isEmpty()) {
                updateDataset(newData);
                updateChartTitle(filterData);
            }
        } catch (SQLException e) {
            handleDatabaseError(e);
        }
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