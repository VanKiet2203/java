package com.Admin.statistics.DAO;

import com.ComponentandDatabase.Database_Connection.DatabaseConnection;
import com.Admin.statistics.DTO.ChartFilterData;
import com.Admin.statistics.DTO.ChartFilterType;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.axis.NumberAxis;
import java.text.DecimalFormat;

import java.awt.*;
import java.sql.*;
import java.util.LinkedHashMap;
import java.util.Map;

public class DAO_barchart {
    private ChartFilterData currentFilter = new ChartFilterData();
    private DefaultCategoryDataset dataset = new DefaultCategoryDataset();
    private JFreeChart chart;
    private ChartPanel chartPanel;

    public DAO_barchart() {
        createChart();
    }

    public void setFilter(ChartFilterData filter) {
        this.currentFilter = (filter != null) ? filter : new ChartFilterData();
    }

    public ChartPanel getChartPanel() {
        updateChartWithFilter(currentFilter);
        return chartPanel;
    }

    private void createChart() {
        chart = ChartFactory.createBarChart(
                "Bar chart", "Group", "Revenue (Million VND)", dataset
        );
        CategoryPlot plot = (CategoryPlot) chart.getPlot();
        plot.setBackgroundPaint(Color.WHITE);
        plot.setDomainGridlinePaint(Color.LIGHT_GRAY);
        plot.setRangeGridlinePaint(Color.LIGHT_GRAY);

        // Format Y-axis with million VND
        NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
        DecimalFormat df = new DecimalFormat("#,##0.0");
        rangeAxis.setNumberFormatOverride(df);
        rangeAxis.setLabel("Revenue (Million VND)");

        chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new Dimension(1150, 520));
        chartPanel.setMouseWheelEnabled(true);
    }

    private String buildSQL(ChartFilterData f) {
        String where = f.getTimeWhereClause("bed") + " AND bed.Status='Available'";
        // Fallback tổng tiền
        String revenueExpr = "SUM(COALESCE(bed.Total_Price_After, bed.Unit_Price_Sell_After * bed.Sold_Quantity))";

        switch (f.getDataType()) {
            case PRODUCT_REVENUE:
                return "SELECT p.Product_Name AS label, " + revenueExpr + " AS value " +
                       "FROM dbo.Bill_Exported_Details bed " +
                       "JOIN dbo.Product p ON p.Product_ID = bed.Product_ID " +
                       "WHERE p.Status='Available' " + where +
                       " GROUP BY p.Product_Name ORDER BY value DESC";

            case CATEGORY_REVENUE:
                return "SELECT ISNULL(p.Category_ID, 'UNCAT') AS label, " + revenueExpr + " AS value " +
                       "FROM dbo.Bill_Exported_Details bed " +
                       "JOIN dbo.Product p ON p.Product_ID = bed.Product_ID " +
                       "WHERE p.Status='Available' " + where +
                       " GROUP BY ISNULL(p.Category_ID, 'UNCAT') ORDER BY value DESC";

            case SUPPLIER_REVENUE:
                return "SELECT ISNULL(ps.Sup_ID,'Unknown') AS label, " + revenueExpr + " AS value " +
                       "FROM dbo.Bill_Exported_Details bed " +
                       "JOIN dbo.Product p ON p.Product_ID = bed.Product_ID " +
                       "LEFT JOIN dbo.Product_Stock ps ON ps.Warehouse_Item_ID = p.Warehouse_Item_ID " +
                       "WHERE (p.Status='Available') " + where +
                       " GROUP BY ISNULL(ps.Sup_ID,'Unknown') ORDER BY value DESC";

            case TOTAL_REVENUE_TREND:
            default:
                // Nhóm theo tháng (yyyy-MM)
                return "SELECT FORMAT(bed.Date_Exported, 'yyyy-MM') AS label, " + revenueExpr + " AS value " +
                       "FROM dbo.Bill_Exported_Details bed " +
                       "JOIN dbo.Product p ON p.Product_ID = bed.Product_ID " +
                       "WHERE p.Status='Available' " + where +
                       " GROUP BY FORMAT(bed.Date_Exported, 'yyyy-MM') ORDER BY label";
        }
    }

    private Map<String, Double> fetch(Connection c, ChartFilterData f) throws SQLException {
        Map<String, Double> out = new LinkedHashMap<>();
        String sql = buildSQL(f);
        try (PreparedStatement ps = c.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) out.put(rs.getString("label"), rs.getDouble("value"));
        }
        return out;
    }

    private void updateDataset(Map<String, Double> data, String series) {
        dataset.clear();
        // Chuyển đổi từ VND sang triệu VND (chia cho 1,000,000)
        data.forEach((k,v)-> dataset.addValue(v / 1_000_000.0, series, k));
    }

    private void updateTitle() {
        String title = switch (currentFilter.getDataType()) {
            case PRODUCT_REVENUE -> "Revenue by Product";
            case CATEGORY_REVENUE -> "Revenue by Category";
            case SUPPLIER_REVENUE -> "Revenue by Supplier";
            case TOTAL_REVENUE_TREND -> "Total Revenue Trend Over Time";
        };
        chart.setTitle(title);
    }

    private void updateChartWithFilter(ChartFilterData f) {
        try (Connection c = DatabaseConnection.connect()) {
            Map<String, Double> data = fetch(c, f);
            String series = (f.getTimeFilterType()==null) ? "All" : f.getTimeFilterType().getDisplayName();
            updateDataset(data, series);
            updateTitle();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
