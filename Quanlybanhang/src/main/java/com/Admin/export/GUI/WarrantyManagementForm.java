package com.Admin.export.GUI;

import com.Admin.export.BUS.BUS_ExportBill;
import com.Admin.export.DTO.DTO_WarrantyInfo;
import com.ComponentandDatabase.Components.CustomDialog;
import com.ComponentandDatabase.Components.MyButton;
import com.ComponentandDatabase.Components.MyPanel;
import com.ComponentandDatabase.Components.MyTable;
import static com.ComponentandDatabase.Components.UIConstants.*;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.List;

public class WarrantyManagementForm extends JPanel {
    private JTable tableWarranty;
    private BUS_ExportBill busExportBill;
    private DefaultTableModel model;
    
    public WarrantyManagementForm() {
        initComponents();
        init();
    }
    
    private void initComponents() {
        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(1200, 700));
        setBackground(BG_WHITE);
    }
    
    private void init() {
        // Main panel
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(null);
        mainPanel.setPreferredSize(new Dimension(1200, 800));
        mainPanel.setBackground(Color.WHITE);
        
        // Title
        JLabel lblTitle = new JLabel("WARRANTY MANAGEMENT");
        lblTitle.setFont(FONT_TITLE_LARGE);
        lblTitle.setForeground(PRIMARY_COLOR);
        lblTitle.setBounds(20, 10, 400, 40);
        mainPanel.add(lblTitle);
        
        // Search panel
        JPanel searchPanel = new MyPanel(Color.WHITE);
        searchPanel.setLayout(null);
        searchPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(PRIMARY_COLOR, 2),
            "Search & Filter",
            0, 0,
            FONT_TITLE_SMALL,
            PRIMARY_COLOR
        ));
        searchPanel.setBounds(20, 60, 1160, 80);
        
        // Search components
        JLabel lblSearch = new JLabel("Search:");
        lblSearch.setFont(FONT_CONTENT_MEDIUM);
        lblSearch.setBounds(20, 30, 60, 25);
        searchPanel.add(lblSearch);
        
        JTextField txtSearch = new JTextField();
        txtSearch.setBounds(90, 30, 200, 25);
        searchPanel.add(txtSearch);
        
        MyButton btnSearch = new MyButton("Search", 20);
        stylePrimaryButton(btnSearch);
        btnSearch.setBounds(300, 30, 100, 25);
        btnSearch.addActionListener(e -> performSearch(txtSearch.getText()));
        searchPanel.add(btnSearch);
        
        MyButton btnRefresh = new MyButton("Refresh", 20);
        styleInfoButton(btnRefresh);
        btnRefresh.setBounds(410, 30, 100, 25);
        btnRefresh.addActionListener(e -> refreshData());
        searchPanel.add(btnRefresh);
        
        mainPanel.add(searchPanel);
        
        // Table panel
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBounds(20, 160, 1160, 500);
        tablePanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(PRIMARY_COLOR, 2),
            "Warranty Information",
            0, 0,
            FONT_TITLE_SMALL,
            PRIMARY_COLOR
        ));
        
        // Create table
        String[] columns = {
            "Invoice No", "Customer Name", "Product Name", "Quantity", 
            "Date Exported", "Start Date", "End Date", "Warranty Status", "Warranty Months"
        };
        model = new DefaultTableModel(columns, 0);
        tableWarranty = createStyledTable(model);
        
        JScrollPane scrollPane = new JScrollPane(tableWarranty);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        
        tablePanel.add(scrollPane, BorderLayout.CENTER);
        mainPanel.add(tablePanel);
        
        // Add scroll pane to main panel
        JScrollPane mainScrollPane = new JScrollPane(mainPanel);
        mainScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        mainScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        
        add(mainScrollPane, BorderLayout.CENTER);
        
        // Initialize data
        busExportBill = new BUS_ExportBill();
        refreshData();
    }
    
    private void refreshData() {
        try {
            List<DTO_WarrantyInfo> warrantyList = busExportBill.getWarrantyInformation();
            model.setRowCount(0);
            
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
            
            for (DTO_WarrantyInfo warranty : warrantyList) {
                Object[] row = {
                    warranty.getInvoiceNo(),
                    warranty.getCustomerName(),
                    warranty.getProductName(),
                    warranty.getSoldQuantity(),
                    warranty.getDateExported() != null ? dateFormat.format(warranty.getDateExported()) : "N/A",
                    warranty.getStartDate() != null ? dateFormat.format(warranty.getStartDate()) : "N/A",
                    warranty.getEndDate() != null ? dateFormat.format(warranty.getEndDate()) : "N/A",
                    warranty.getWarrantyStatus(),
                    warranty.getWarrantyMonths() + " tháng"
                };
                model.addRow(row);
            }
            
            // Adjust column widths
            if (tableWarranty instanceof MyTable) {
                ((MyTable) tableWarranty).adjustColumnWidths();
            }
            
        } catch (Exception e) {
            CustomDialog.showError("Failed to load warranty data: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void performSearch(String keyword) {
        if (keyword.trim().isEmpty()) {
            refreshData();
            return;
        }
        
        try {
            List<DTO_WarrantyInfo> warrantyList = busExportBill.getWarrantyInformation();
            model.setRowCount(0);
            
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
            keyword = keyword.toLowerCase();
            
            for (DTO_WarrantyInfo warranty : warrantyList) {
                // Search in invoice no, customer name, product name
                if (warranty.getInvoiceNo().toLowerCase().contains(keyword) ||
                    warranty.getCustomerName().toLowerCase().contains(keyword) ||
                    warranty.getProductName().toLowerCase().contains(keyword)) {
                    
                    Object[] row = {
                        warranty.getInvoiceNo(),
                        warranty.getCustomerName(),
                        warranty.getProductName(),
                        warranty.getSoldQuantity(),
                        warranty.getDateExported() != null ? dateFormat.format(warranty.getDateExported()) : "N/A",
                        warranty.getStartDate() != null ? dateFormat.format(warranty.getStartDate()) : "N/A",
                        warranty.getEndDate() != null ? dateFormat.format(warranty.getEndDate()) : "N/A",
                        warranty.getWarrantyStatus(),
                        warranty.getWarrantyMonths() + " tháng"
                    };
                    model.addRow(row);
                }
            }
            
            if (tableWarranty instanceof MyTable) {
                ((MyTable) tableWarranty).adjustColumnWidths();
            }
            
        } catch (Exception e) {
            CustomDialog.showError("Search failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private MyTable createStyledTable(DefaultTableModel model) {
        return new MyTable(
            model,
            Color.WHITE,                    // Nền bảng
            TEXT_PRIMARY,                   // Chữ bảng
            Color.decode("#E8F5E9"),        // Nền dòng chọn
            Color.BLACK,                    // Chữ dòng chọn
            PRIMARY_COLOR,                  // Nền tiêu đề
            Color.WHITE,                    // Chữ tiêu đề
            FONT_TABLE_CONTENT,             // Font nội dung
            FONT_TABLE_HEADER               // Font tiêu đề
        );
    }
    
    private void stylePrimaryButton(MyButton btn) {
        btn.setBackgroundColor(PRIMARY_COLOR);
        btn.setHoverColor(PRIMARY_HOVER);
        btn.setPressedColor(PRIMARY_HOVER.darker());
        btn.setFont(FONT_BUTTON_MEDIUM);
        btn.setForeground(Color.WHITE);
    }
    
    private void styleInfoButton(MyButton btn) {
        btn.setBackgroundColor(INFO_COLOR);
        btn.setHoverColor(INFO_HOVER);
        btn.setPressedColor(INFO_HOVER.darker());
        btn.setFont(FONT_BUTTON_MEDIUM);
        btn.setForeground(Color.WHITE);
    }
}
