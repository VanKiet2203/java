package com.Admin.statistics.DAO;

import com.ComponentandDatabase.Database_Connection.DatabaseConnection;
import com.Admin.statistics.DTO.ChartFilterData;
import java.sql.*;
import java.sql.ResultSetMetaData;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import java.awt.Color;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.data.category.DefaultCategoryDataset;

public class DAO_barchart {
    private DefaultCategoryDataset dataset;
    private JFreeChart chart;
    private ChartPanel chartPanel;
    private ScheduledExecutorService scheduler;
    private static final int REFRESH_INTERVAL = 30;
    private volatile boolean isRunning = true;
    private Connection activeConnection;
    private volatile ChartFilterData currentFilter;

    public DAO_barchart() {
        dataset = new DefaultCategoryDataset();
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
        chart = ChartFactory.createBarChart(
            "Thống kê",
            "Sản phẩm",
            "Giá trị",
            dataset,
            PlotOrientation.VERTICAL,
            true, true, false
        );

        CategoryPlot plot = (CategoryPlot) chart.getPlot();
        plot.setBackgroundPaint(Color.WHITE);

        NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
        rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
        rangeAxis.setAutoRangeIncludesZero(true);

        applyDistinctColors();

        chart.setBackgroundPaint(Color.WHITE);

        // Tạo ChartPanel với chế độ chỉ xem
        chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new java.awt.Dimension(800, 600));
        chartPanel.setBackground(Color.WHITE);
        chartPanel.setMouseWheelEnabled(false);  // 🚫 Vô hiệu hóa cuộn bằng chuột
        chartPanel.setDomainZoomable(false);     // 🚫 Không cho phép zoom theo trục ngang
        chartPanel.setRangeZoomable(false);      // 🚫 Không cho phép zoom theo trục dọc
    }


    private synchronized void refreshData() {
        try {
            Map<String, Integer> newData = fetchDataFromDatabase(currentFilter);
            if (!newData.isEmpty()) {
                updateDataset(newData);
                updateChartTitle(currentFilter);
            }
        } catch (SQLException e) {
            handleDatabaseError(e);
        }
    }
    
    private Map<String, Integer> fetchDataFromDatabase(ChartFilterData filterData) throws SQLException {
        Map<String, Integer> data = new LinkedHashMap<>();
        
        // Xây dựng SQL query dựa trên loại dữ liệu và filter
        String sql = buildSQLQuery(filterData);

        try {
            if (activeConnection == null || activeConnection.isClosed()) {
                activeConnection = DatabaseConnection.connect();
                activeConnection.setNetworkTimeout(Executors.newSingleThreadExecutor(), 5000);
            }

            try (PreparedStatement pst = activeConnection.prepareStatement(sql);
                 ResultSet rs = pst.executeQuery()) {

                ResultSetMetaData metaData = rs.getMetaData();
                int columnCount = metaData.getColumnCount();
                
                while (rs.next()) {
                    // Get label based on query type - check which column exists
                    String label = null;
                    int count = 0;
                    
                    // Check columns to find the label column
                    for (int i = 1; i <= columnCount; i++) {
                        String columnName = metaData.getColumnName(i);
                        if (columnName.equals("Product_Name")) {
                            label = rs.getString(i);
                            break;
                        }
                    }
                    
                    // Get count value
                    for (int i = 1; i <= columnCount; i++) {
                        String columnName = metaData.getColumnName(i);
                        if (columnName.equals("count")) {
                            count = rs.getInt(i);
                            break;
                        }
                    }
                    
                    if (label != null) {
                        data.put(label, count);
                    }
                }
            }
        } catch (SQLException e) {
            closeActiveConnection();
            throw e;
        }

        return data;
    }

    private void updateDataset(Map<String, Integer> newData) {
        dataset.clear();

        newData.forEach((productName, value) -> dataset.addValue(value, productName, "Sản phẩm"));

        if (chart != null) {
            chart.fireChartChanged();
            applyDistinctColors();
        }
    }

    private void applyDistinctColors() {
        if (chart == null) return;

        CategoryPlot plot = (CategoryPlot) chart.getPlot();
        BarRenderer renderer = (BarRenderer) plot.getRenderer();

        Color[] distinctColors = {
            new Color(31, 119, 180), new Color(255, 127, 14),
            new Color(44, 160, 44), new Color(214, 39, 40),
            new Color(148, 103, 189), new Color(140, 86, 75),
            new Color(227, 119, 194), new Color(127, 127, 127),
            new Color(188, 189, 34), new Color(23, 190, 207)
        };

        for (int i = 0; i < dataset.getRowCount(); i++) {
            renderer.setSeriesPaint(i, distinctColors[i % distinctColors.length]);
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
            System.err.println("⚠ Error closing connection: " + e.getMessage());
        }
        activeConnection = null;
    }

    private String buildSQLQuery(ChartFilterData filterData) {
        StringBuilder sql = new StringBuilder();
        String timeFilter = filterData.getTimeWhereClause();
        
        switch (filterData.getDataType()) {
            case REVENUE:
                sql.append("SELECT p.Product_Name, SUM(bed.Total_Price_After) as count FROM Bill_Exported_Details bed ")
                   .append("JOIN Product p ON bed.Product_ID = p.Product_ID ")
                   .append("WHERE bed.Status = 'Available' AND p.Status = 'Available' ")
                   .append(timeFilter)
                   .append(" GROUP BY p.Product_Name ORDER BY count DESC");
                break;
                
            case QUANTITY_SOLD:
                sql.append("SELECT p.Product_Name, SUM(bed.Sold_Quantity) as count FROM Bill_Exported_Details bed ")
                   .append("JOIN Product p ON bed.Product_ID = p.Product_ID ")
                   .append("WHERE bed.Status = 'Available' AND p.Status = 'Available' ")
                   .append(timeFilter)
                   .append(" GROUP BY p.Product_Name ORDER BY count DESC");
                break;
                
            default:
                // Default to revenue
                sql.append("SELECT p.Product_Name, SUM(bed.Total_Price_After) as count FROM Bill_Exported_Details bed ")
                   .append("JOIN Product p ON bed.Product_ID = p.Product_ID ")
                   .append("WHERE bed.Status = 'Available' AND p.Status = 'Available' ")
                   .append(timeFilter)
                   .append(" GROUP BY p.Product_Name ORDER BY count DESC");
                break;
        }
        
        return sql.toString();
    }
    
    private void updateChartWithFilter(ChartFilterData filterData) {
        try {
            Map<String, Integer> newData = fetchDataFromDatabase(filterData);
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
