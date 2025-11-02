package com.Admin.statistics.BUS;

import com.Admin.statistics.DAO.DAO_barchart;
import com.Admin.statistics.DAO.DAO_piechart; 
import com.Admin.statistics.DAO.DAO_heatmap;
import com.Admin.statistics.DAO.DAO_linechart;
import com.Admin.statistics.DTO.ChartFilterData;
import org.jfree.chart.ChartPanel;

public class BUS_Chart {
    private DAO_barchart daoBarChart;
    private DAO_piechart daoPieChart; // Thêm instance cho pie chart
    private DAO_heatmap daoHeatmap;
    private DAO_linechart daoLineChart;
    private ChartFilterData currentFilter;
            
    public BUS_Chart() {
        daoBarChart = new DAO_barchart();
        daoPieChart = new DAO_piechart(); // Khởi tạo pie chart
        daoHeatmap= new DAO_heatmap();
        daoLineChart= new DAO_linechart();
        currentFilter = new ChartFilterData();
    }

    // Phương thức cho bar chart
    public ChartPanel getBarChartPanel() {
        return daoBarChart.getChartPanel(currentFilter);
    }

    // Phương thức mới cho pie chart
    public ChartPanel getPieChartPanel() {
        return daoPieChart.getChartPanel(currentFilter);
    }
    
    public ChartPanel getHeatmap(){
        return daoHeatmap.getChartPanel(currentFilter);
    }
   
    public ChartPanel getLineChart(){
        return daoLineChart.getChartPanel(currentFilter);
    }
    
    // Phương thức cập nhật filter
    public void updateFilter(ChartFilterData filterData) {
        if (filterData != null) {
            this.currentFilter = new ChartFilterData(filterData);
            // Cập nhật filter vào tất cả các DAO
            daoBarChart.setFilter(this.currentFilter);
            daoPieChart.setFilter(this.currentFilter);
            daoHeatmap.setFilter(this.currentFilter);
            daoLineChart.setFilter(this.currentFilter);
        }
    }
    
    // Phương thức lấy filter hiện tại
    public ChartFilterData getCurrentFilter() {
        return currentFilter;
    }

    public void shutdownChartUpdaters() {
        daoBarChart.shutdown();
        daoPieChart.shutdown(); // Đóng cả pie chart
        daoHeatmap.shutdown();
        daoLineChart.shutdown();
    }
}