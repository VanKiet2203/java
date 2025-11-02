package com.Admin.statistics.Components;

import com.Admin.statistics.DTO.ChartFilterData;
import com.Admin.statistics.DTO.ChartFilterType;
import com.Admin.statistics.DTO.TimeFilterType;
import com.ComponentandDatabase.Components.MyButton;
import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Component filter panel cho các biểu đồ thống kê
 */
public class FilterPanel extends JPanel {
    private JComboBox<TimeFilterType> cmbTimeFilter;
    private JComboBox<Integer> cmbYear;
    private JComboBox<Integer> cmbQuarter;
    private JComboBox<Integer> cmbMonth;
    private JComboBox<ChartFilterType> cmbDataType;
    private MyButton btnApply;
    private MyButton btnReset;
    
    private ChartFilterData currentFilter;
    private List<FilterChangeListener> listeners;
    
    public interface FilterChangeListener {
        void onFilterChanged(ChartFilterData filterData);
    }
    
    public FilterPanel() {
        this.currentFilter = new ChartFilterData();
        this.listeners = new ArrayList<>();
        initComponents();
        setupEventHandlers();
        updateFilterData();
    }
    
    private void initComponents() {
        setLayout(new FlowLayout(FlowLayout.LEFT, 10, 5));
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createTitledBorder("Bộ lọc dữ liệu"));
        
        // Time Filter Type
        JLabel lblTimeFilter = new JLabel("Thời gian:");
        lblTimeFilter.setFont(new Font("Arial", Font.BOLD, 12));
        add(lblTimeFilter);
        
        cmbTimeFilter = new JComboBox<>(TimeFilterType.values());
        cmbTimeFilter.setPreferredSize(new Dimension(120, 30));
        cmbTimeFilter.setFont(new Font("Arial", Font.PLAIN, 11));
        add(cmbTimeFilter);
        
        // Year
        JLabel lblYear = new JLabel("Năm:");
        lblYear.setFont(new Font("Arial", Font.BOLD, 12));
        add(lblYear);
        
        cmbYear = new JComboBox<>();
        populateYearComboBox();
        cmbYear.setPreferredSize(new Dimension(80, 30));
        cmbYear.setFont(new Font("Arial", Font.PLAIN, 11));
        add(cmbYear);
        
        // Quarter
        JLabel lblQuarter = new JLabel("Quý:");
        lblQuarter.setFont(new Font("Arial", Font.BOLD, 12));
        add(lblQuarter);
        
        cmbQuarter = new JComboBox<>();
        for (int i = 1; i <= 4; i++) {
            cmbQuarter.addItem(i);
        }
        cmbQuarter.setPreferredSize(new Dimension(60, 30));
        cmbQuarter.setFont(new Font("Arial", Font.PLAIN, 11));
        add(cmbQuarter);
        
        // Month
        JLabel lblMonth = new JLabel("Tháng:");
        lblMonth.setFont(new Font("Arial", Font.BOLD, 12));
        add(lblMonth);
        
        cmbMonth = new JComboBox<>();
        for (int i = 1; i <= 12; i++) {
            cmbMonth.addItem(i);
        }
        cmbMonth.setPreferredSize(new Dimension(60, 30));
        cmbMonth.setFont(new Font("Arial", Font.PLAIN, 11));
        add(cmbMonth);
        
        // Data Type
        JLabel lblDataType = new JLabel("Loại dữ liệu:");
        lblDataType.setFont(new Font("Arial", Font.BOLD, 12));
        add(lblDataType);
        
        cmbDataType = new JComboBox<>(ChartFilterType.values());
        cmbDataType.setPreferredSize(new Dimension(180, 30));
        cmbDataType.setFont(new Font("Arial", Font.PLAIN, 11));
        add(cmbDataType);
        
        // Apply Button
        btnApply = new MyButton("Áp dụng", 20);
        btnApply.setBackgroundColor(Color.decode("#4CAF50"));
        btnApply.setPressedColor(Color.decode("#45a049"));
        btnApply.setHoverColor(Color.decode("#66bb6a"));
        btnApply.setPreferredSize(new Dimension(100, 35));
        btnApply.setFont(new Font("Arial", Font.BOLD, 12));
        btnApply.setForeground(Color.WHITE);
        add(btnApply);
        
        // Reset Button
        btnReset = new MyButton("Đặt lại", 20);
        btnReset.setBackgroundColor(Color.decode("#f44336"));
        btnReset.setPressedColor(Color.decode("#da190b"));
        btnReset.setHoverColor(Color.decode("#ef5350"));
        btnReset.setPreferredSize(new Dimension(100, 35));
        btnReset.setFont(new Font("Arial", Font.BOLD, 12));
        btnReset.setForeground(Color.WHITE);
        add(btnReset);
        
        // Set initial values
        setInitialValues();
    }
    
    private void populateYearComboBox() {
        int currentYear = LocalDate.now().getYear();
        for (int year = currentYear - 5; year <= currentYear + 1; year++) {
            cmbYear.addItem(year);
        }
        cmbYear.setSelectedItem(currentYear);
    }
    
    private void setInitialValues() {
        cmbTimeFilter.setSelectedItem(TimeFilterType.ALL_TIME);
        cmbYear.setSelectedItem(LocalDate.now().getYear());
        cmbQuarter.setSelectedItem(1);
        cmbMonth.setSelectedItem(1);
        cmbDataType.setSelectedItem(ChartFilterType.REVENUE);
    }
    
    private void setupEventHandlers() {
        // Time filter change handler - chỉ cập nhật visibility, không áp dụng filter
        cmbTimeFilter.addActionListener(e -> {
            updateTimeFilterVisibility();
        });
        
        // Year, Quarter, Month, DataType change handlers - không tự động áp dụng
        // Chỉ cập nhật khi nhấn nút Áp dụng
        
        // Apply button handler - áp dụng filter và thông báo cho listeners
        btnApply.addActionListener(e -> applyFilter());
        
        // Reset button handler
        btnReset.addActionListener(e -> resetFilter());
        
        // Initial visibility update
        updateTimeFilterVisibility();
    }
    
    private void updateTimeFilterVisibility() {
        TimeFilterType selectedType = (TimeFilterType) cmbTimeFilter.getSelectedItem();
        
        // Show/hide components based on time filter type
        boolean showYear = selectedType != TimeFilterType.ALL_TIME;
        boolean showQuarter = selectedType == TimeFilterType.QUARTER;
        boolean showMonth = selectedType == TimeFilterType.MONTH;
        
        // Update visibility
        for (Component comp : getComponents()) {
            if (comp instanceof JLabel) {
                JLabel label = (JLabel) comp;
                if (label.getText().equals("Năm:")) {
                    label.setVisible(showYear);
                } else if (label.getText().equals("Quý:")) {
                    label.setVisible(showQuarter);
                } else if (label.getText().equals("Tháng:")) {
                    label.setVisible(showMonth);
                }
            } else if (comp instanceof JComboBox) {
                JComboBox<?> comboBox = (JComboBox<?>) comp;
                if (comboBox == cmbYear) {
                    comboBox.setVisible(showYear);
                } else if (comboBox == cmbQuarter) {
                    comboBox.setVisible(showQuarter);
                } else if (comboBox == cmbMonth) {
                    comboBox.setVisible(showMonth);
                }
            }
        }
        
        revalidate();
        repaint();
    }
    
    private void updateFilterData() {
        currentFilter.setTimeFilterType((TimeFilterType) cmbTimeFilter.getSelectedItem());
        currentFilter.setYear((Integer) cmbYear.getSelectedItem());
        currentFilter.setQuarter((Integer) cmbQuarter.getSelectedItem());
        currentFilter.setMonth((Integer) cmbMonth.getSelectedItem());
        currentFilter.setDataType((ChartFilterType) cmbDataType.getSelectedItem());
    }
    
    private void applyFilter() {
        updateFilterData();
        notifyListeners();
    }
    
    private void resetFilter() {
        setInitialValues();
        updateTimeFilterVisibility();
        updateFilterData();
        notifyListeners();
    }
    
    private void notifyListeners() {
        for (FilterChangeListener listener : listeners) {
            listener.onFilterChanged(new ChartFilterData(currentFilter));
        }
    }
    
    public void addFilterChangeListener(FilterChangeListener listener) {
        listeners.add(listener);
    }
    
    public void removeFilterChangeListener(FilterChangeListener listener) {
        listeners.remove(listener);
    }
    
    public ChartFilterData getCurrentFilter() {
        return new ChartFilterData(currentFilter);
    }
    
    public void setFilter(ChartFilterData filterData) {
        if (filterData != null) {
            cmbTimeFilter.setSelectedItem(filterData.getTimeFilterType());
            cmbYear.setSelectedItem(filterData.getYear());
            cmbQuarter.setSelectedItem(filterData.getQuarter());
            cmbMonth.setSelectedItem(filterData.getMonth());
            cmbDataType.setSelectedItem(filterData.getDataType());
            updateTimeFilterVisibility();
            updateFilterData();
        }
    }
}
