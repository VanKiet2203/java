package com.Admin.statistics.DAO;

import com.ComponentandDatabase.Database_Connection.DatabaseConnection;
import com.Admin.statistics.DTO.ChartFilterData;
import com.Admin.statistics.DTO.ChartFilterType;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.chart.labels.PieSectionLabelGenerator;
import org.jfree.chart.labels.StandardPieSectionLabelGenerator;
import org.jfree.chart.plot.PiePlot;

import java.awt.*;
import java.sql.*;
import java.text.DecimalFormat;
import java.util.LinkedHashMap;
import java.util.Map;

public class DAO_piechart {
    private ChartFilterData currentFilter = new ChartFilterData();
    private DefaultPieDataset<String> dataset = new DefaultPieDataset<>();
    private JFreeChart chart;
    private ChartPanel chartPanel;

    public DAO_piechart() {
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
        chart = ChartFactory.createPieChart("Pie chart", dataset, true, true, false);
        PiePlot plot = (PiePlot) chart.getPlot();
        plot.setBackgroundPaint(Color.WHITE);
        plot.setCircular(true);
        plot.setLabelFont(new Font("SansSerif", Font.PLAIN, 12));
        PieSectionLabelGenerator lg = new StandardPieSectionLabelGenerator(
                "{0}: {1} ({2})", new DecimalFormat("0"), new DecimalFormat("0.0%")
        );
        plot.setLabelGenerator(lg);

        chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new Dimension(900, 600));
        chartPanel.setMouseWheelEnabled(true);
    }

    private String buildSQL(ChartFilterData f) {
        String where = f.getTimeWhereClause("bed") + " AND bed.Status='Available'";
        String revenueExpr = "SUM(COALESCE(bed.Total_Price_After, bed.Unit_Price_Sell_After * bed.Sold_Quantity))";

        switch (f.getDataType()) {
            case PRODUCT_REVENUE:
                return "SELECT TOP 10 p.Product_Name AS label, " + revenueExpr + " AS value " +
                       "FROM dbo.Bill_Exported_Details bed " +
                       "JOIN dbo.Product p ON p.Product_ID = bed.Product_ID " +
                       "WHERE p.Status='Available' " + where +
                       " GROUP BY p.Product_Name ORDER BY value DESC";
            case CATEGORY_REVENUE:
                return "SELECT ISNULL(p.Category_ID,'UNCAT') AS label, " + revenueExpr + " AS value " +
                       "FROM dbo.Bill_Exported_Details bed " +
                       "JOIN dbo.Product p ON p.Product_ID = bed.Product_ID " +
                       "WHERE p.Status='Available' " + where +
                       " GROUP BY ISNULL(p.Category_ID,'UNCAT') ORDER BY value DESC";
            case SUPPLIER_REVENUE:
            default:
                return "SELECT ISNULL(ps.Sup_ID,'Unknown') AS label, " + revenueExpr + " AS value " +
                       "FROM dbo.Bill_Exported_Details bed " +
                       "JOIN dbo.Product p ON p.Product_ID = bed.Product_ID " +
                       "LEFT JOIN dbo.Product_Stock ps ON ps.Warehouse_Item_ID = p.Warehouse_Item_ID " +
                       "WHERE p.Status='Available' " + where +
                       " GROUP BY ISNULL(ps.Sup_ID,'Unknown') ORDER BY value DESC";
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

    private void updateDataset(Map<String, Double> data) {
        dataset.clear();
        data.forEach(dataset::setValue);
    }

    private void updateTitle() {
        String title = switch (currentFilter.getDataType()) {
            case PRODUCT_REVENUE -> "Revenue Share by Product (Top 10)";
            case CATEGORY_REVENUE -> "Revenue Share by Category";
            case SUPPLIER_REVENUE -> "Revenue Share by Supplier";
            case TOTAL_REVENUE_TREND -> "Not suitable for pie chart – please select another type";
        };
        chart.setTitle(title);
    }

    private void updateChartWithFilter(ChartFilterData f) {
        try (Connection c = DatabaseConnection.connect()) {
            Map<String, Double> data = fetch(c, f);
            updateDataset(data);
            updateTitle();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
