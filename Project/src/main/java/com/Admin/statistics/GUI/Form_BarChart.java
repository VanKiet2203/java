package com.Admin.statistics.GUI;

import com.ComponentandDatabase.Components.MyButton;
import com.Admin.statistics.BUS.BUS_Chart;
import com.Admin.statistics.Components.FilterPanel;
import com.Admin.statistics.DTO.ChartFilterData;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import org.jfree.chart.ChartPanel;
import java.awt.BorderLayout;

public class Form_BarChart extends JPanel {
    private JPanel panel;
    private JLabel lblTitle;
    private MyButton bntRefresh;
    private BUS_Chart busChart;
    private ChartPanel chartPanel;
    private FilterPanel filterPanel;
    private ChartFilterData currentFilter;

    public Form_BarChart() {
        initComponents();
        init();
    }

    private void initComponents() {
        setLayout(null);
        setPreferredSize(new Dimension(1530, 860));
        setBackground(Color.WHITE);
    }

    private void init() {
        setLayout(new BorderLayout());
        
        // Tạo FilterPanel với listener
        currentFilter = new ChartFilterData();
        filterPanel = new FilterPanel(this::onFilterApplied);
        add(filterPanel, BorderLayout.NORTH);
        
        // Tạo panel chính cho chart
        panel = new JPanel();
        panel.setLayout(null);
        panel.setBackground(Color.WHITE);
        panel.setBorder(null);
        add(panel, BorderLayout.CENTER);

        // Khởi tạo BUS
        busChart = new BUS_Chart();
        
        // Lấy ChartPanel từ BUS với filter mặc định
        chartPanel = busChart.getBar(currentFilter);
        chartPanel.setBounds(50, 50, 1170, 500);
        panel.add(chartPanel);

        // Title
        lblTitle = new JLabel("Revenue Statistics");
        lblTitle.setFont(new Font("Arial", Font.BOLD, 20));
        lblTitle.setHorizontalAlignment(SwingConstants.CENTER);
        lblTitle.setBounds(50, 10, 1170, 30);
        panel.add(lblTitle);

        // Nút Refresh
        bntRefresh = new MyButton("Refresh", 20);
        bntRefresh.setBackgroundColor(Color.WHITE);
        bntRefresh.setPressedColor(Color.decode("#D3D3D3"));
        bntRefresh.setHoverColor(Color.decode("#EEEEEE"));
        bntRefresh.setBounds(1100, 10, 140, 35);
        bntRefresh.setFont(new Font("sansserif", Font.BOLD, 16));
        bntRefresh.setForeground(Color.BLACK);
        bntRefresh.setButtonIcon("src\\main\\resources\\Icons\\Admin_icon\\refresh.png", 
                               25, 25, 10, SwingConstants.RIGHT, SwingConstants.CENTER);
        
        // Xử lý sự kiện refresh
        bntRefresh.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                refreshChart();
            }
        });
        
        panel.add(bntRefresh);
    }

    private void onFilterApplied(ChartFilterData filterData) {
        currentFilter = filterData;
        refreshChart();
    }

    private void refreshChart() {
        // Xóa chartPanel cũ
        panel.remove(chartPanel);
        
        // Tạo lại chartPanel mới với filter hiện tại
        chartPanel = busChart.getBar(currentFilter);
        chartPanel.setBounds(50, 50, 1170, 500);
        panel.add(chartPanel);
        
        // Cập nhật giao diện
        panel.revalidate();
        panel.repaint();
    }
}