package com.Admin.statistics.Components;

import com.Admin.statistics.DTO.ChartFilterData;
import com.Admin.statistics.DTO.ChartFilterType;
import com.Admin.statistics.DTO.TimeFilterType;
import com.ComponentandDatabase.Components.MyButton;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;

public class FilterPanel extends JPanel {
    private final JComboBox<TimeFilterType> cmbTimeType;
    private final JComboBox<Integer> cmbYear;
    private final JComboBox<Integer> cmbQuarter;
    private final JComboBox<Integer> cmbMonth;
    private final JComboBox<ChartFilterType> cmbDataType;
    private final MyButton btnApply;

    public interface ApplyListener {
        void onApply(ChartFilterData data);
    }

    public FilterPanel(ApplyListener listener) {
        setLayout(new FlowLayout(FlowLayout.LEFT, 8, 5));
        setBackground(Color.WHITE);

        // Time type
        cmbTimeType = new JComboBox<>(TimeFilterType.values());
        cmbTimeType.setPreferredSize(new Dimension(130, 28));
        JLabel lblTime = new JLabel("Time:");
        lblTime.setFont(new Font("Arial", Font.PLAIN, 11));
        add(lblTime);
        add(cmbTimeType);

        // Year
        JLabel lblYear = new JLabel("Year:");
        lblYear.setFont(new Font("Arial", Font.PLAIN, 11));
        add(lblYear);
        cmbYear = new JComboBox<>();
        int curYear = LocalDate.now().getYear();
        for (int y = curYear - 5; y <= curYear + 1; y++) cmbYear.addItem(y);
        cmbYear.setSelectedItem(curYear);
        cmbYear.setPreferredSize(new Dimension(80, 28));
        add(cmbYear);

        // Quarter
        JLabel lblQuarter = new JLabel("Quarter:");
        lblQuarter.setFont(new Font("Arial", Font.PLAIN, 11));
        add(lblQuarter);
        cmbQuarter = new JComboBox<>(new Integer[]{1,2,3,4});
        cmbQuarter.setPreferredSize(new Dimension(60, 28));
        add(cmbQuarter);

        // Month
        JLabel lblMonth = new JLabel("Month:");
        lblMonth.setFont(new Font("Arial", Font.PLAIN, 11));
        add(lblMonth);
        Integer[] months = new Integer[12];
        for (int i=0;i<12;i++) months[i]=i+1;
        cmbMonth = new JComboBox<>(months);
        cmbMonth.setSelectedItem(LocalDate.now().getMonthValue());
        cmbMonth.setPreferredSize(new Dimension(70, 28));
        add(cmbMonth);

        // Data type
        JLabel lblDataType = new JLabel("Compare:");
        lblDataType.setFont(new Font("Arial", Font.PLAIN, 11));
        add(lblDataType);
        cmbDataType = new JComboBox<>(ChartFilterType.values());
        cmbDataType.setPreferredSize(new Dimension(200, 28));
        add(cmbDataType);

        // Apply button (optional, keep for manual refresh)
        btnApply = new MyButton("Apply", 15);
        btnApply.setPreferredSize(new Dimension(90, 30));
        btnApply.setForeground(Color.WHITE);
        btnApply.setBackgroundColor(new Color(0x4CAF50));
        btnApply.setHoverColor(new Color(0x66BB6A));
        btnApply.setPressedColor(new Color(0x388E3C));
        add(btnApply);

        // Helper method to apply filter
        Runnable applyFilter = () -> {
            if (listener != null) {
                ChartFilterData data = new ChartFilterData();
                data.setTimeFilterType((TimeFilterType) cmbTimeType.getSelectedItem());
                data.setYear((Integer) cmbYear.getSelectedItem());
                data.setQuarter((Integer) cmbQuarter.getSelectedItem());
                data.setMonth((Integer) cmbMonth.getSelectedItem());
                data.setDataType((ChartFilterType) cmbDataType.getSelectedItem());
                listener.onApply(data);
            }
        };

        // Show/Hide month/quarter controls by type
        Runnable updateVisibility = () -> {
            TimeFilterType t = (TimeFilterType) cmbTimeType.getSelectedItem();
            boolean showYear = t != TimeFilterType.ALL_TIME;
            boolean showQuarter = t == TimeFilterType.QUARTER;
            boolean showMonth = t == TimeFilterType.MONTH;
            cmbYear.setEnabled(showYear);
            cmbQuarter.setEnabled(showQuarter);
            cmbMonth.setEnabled(showMonth);
        };

        // Auto-apply when filter changes
        cmbTimeType.addActionListener(e -> {
            updateVisibility.run();
            applyFilter.run();
        });
        cmbYear.addActionListener(e -> applyFilter.run());
        cmbQuarter.addActionListener(e -> applyFilter.run());
        cmbMonth.addActionListener(e -> applyFilter.run());
        cmbDataType.addActionListener(e -> applyFilter.run());
        
        // Manual apply button
        btnApply.addActionListener(e -> applyFilter.run());
        
        updateVisibility.run();
    }
}
