package com.Admin.statistics.BUS;

import com.Admin.statistics.DTO.ChartFilterData;
import com.Admin.statistics.DAO.DAO_linechart;
import com.Admin.statistics.DAO.DAO_barchart;
import com.Admin.statistics.DAO.DAO_piechart;
import org.jfree.chart.ChartPanel;

public class BUS_Chart {
    private final DAO_linechart lineDAO = new DAO_linechart();
    private final DAO_barchart barDAO = new DAO_barchart();
    private final DAO_piechart pieDAO = new DAO_piechart();

    public ChartPanel getLine(ChartFilterData f) {
        lineDAO.setFilter(f);
        return lineDAO.getChartPanel();
    }
    public ChartPanel getBar(ChartFilterData f) {
        barDAO.setFilter(f);
        return barDAO.getChartPanel();
    }
    public ChartPanel getPie(ChartFilterData f) {
        pieDAO.setFilter(f);
        return pieDAO.getChartPanel();
    }
}
