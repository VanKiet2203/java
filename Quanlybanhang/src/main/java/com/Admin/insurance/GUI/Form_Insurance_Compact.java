package com.Admin.insurance.GUI;

import com.Admin.dashboard_admin.GUI.Dashboard_ad;
import com.ComponentandDatabase.Components.MyButton;
import com.ComponentandDatabase.Components.MyTextField;
import com.ComponentandDatabase.Components.MyCombobox;
import com.ComponentandDatabase.Components.MyTable;
import com.ComponentandDatabase.Components.CustomDialog;
import com.Admin.insurance.BUS.BUS_Warranty;
import com.Admin.export.BUS.BUS_ExportBill;
import com.Admin.export.DTO.DTO_BillExport;
import com.Admin.insurance.DTO.DTO_Insurance;
import com.toedter.calendar.JDateChooser;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import java.io.File;

import static com.ComponentandDatabase.Components.UIConstants.*;

public class Form_Insurance_Compact extends JPanel {
    
    // Main components
    private JTabbedPane tabbedPane;
    private JPanel searchPanel, insurancePanel, detailsPanel;
    
    // Search components
    private MyTextField txtSearch;
    private MyCombobox<String> cmbSearch;
    private MyButton btnSearch, btnRefresh;
    private MyTable tableExportedBills;
    private DefaultTableModel modelExportedBills;
    
    // Insurance form components
    private MyTextField txtAdminID, txtAdminName, txtInvoiceNo, txtCustomerID;
    private JTextArea txtDescription;
    private JDateChooser startDate, endDate;
    private MyTable tableProducts;
    private DefaultTableModel modelProducts;
    private MyButton btnCreateInsurance, btnSaveInsurance;
    
    // Details components
    private MyTable tableInsurance;
    private DefaultTableModel modelInsurance;
    private MyButton btnViewDetails, btnExportExcel;
    
    // Business logic
    private BUS_Warranty busWarranty;
    private BUS_ExportBill busExportBill;
    private String selectedInvoiceNo;
    private String selectedCustomerID;
    
    public Form_Insurance_Compact() {
        initComponents();
        init();
        loadData();
    }
    
    private void initComponents() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        setPreferredSize(new Dimension(800, 450)); // Compact size for 16" screens
    }
    
    private void init() {
        // Create tabbed interface
        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(FONT_CONTENT_MEDIUM); // Smaller font
        tabbedPane.setBackground(Color.WHITE);
        
        // Tab 1: Search & Select Bills
        createSearchTab();
        
        // Tab 2: Create Insurance
        createInsuranceTab();
        
        // Tab 3: View Insurance Details
        createDetailsTab();
        
        add(tabbedPane, BorderLayout.CENTER);
    }
    
    private void createSearchTab() {
        searchPanel = new JPanel(new BorderLayout());
        searchPanel.setBackground(Color.WHITE);
        searchPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        
        // Compact search section
        JPanel searchSection = createCompactSearchSection();
        searchPanel.add(searchSection, BorderLayout.NORTH);
        
        // Compact table section
        JPanel tableSection = createCompactTableSection();
        searchPanel.add(tableSection, BorderLayout.CENTER);
        
        tabbedPane.addTab("📋 Bills", searchPanel);
    }
    
    private JPanel createCompactSearchSection() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(PRIMARY_COLOR, 1),
            "Search Export Bills",
            TitledBorder.LEFT,
            TitledBorder.TOP,
            FONT_CONTENT_SMALL,
            PRIMARY_COLOR
        ));
        
        // Compact search components
        String[] searchOptions = {"Invoice No", "Customer ID", "Product ID"};
        cmbSearch = new MyCombobox<>(searchOptions);
        cmbSearch.setPreferredSize(new Dimension(100, 25));
        cmbSearch.setCustomFont(FONT_CONTENT_SMALL);
        panel.add(cmbSearch);
        
        txtSearch = new MyTextField();
        txtSearch.setHint("Search...");
        txtSearch.setPreferredSize(new Dimension(150, 25));
        txtSearch.setTextFont(FONT_CONTENT_SMALL);
        panel.add(txtSearch);
        
        btnSearch = new MyButton("Search", 10);
        stylePrimaryButton(btnSearch);
        btnSearch.setPreferredSize(new Dimension(70, 25));
        btnSearch.addActionListener(e -> performSearch());
        panel.add(btnSearch);
        
        btnRefresh = new MyButton("Refresh", 10);
        styleInfoButton(btnRefresh);
        btnRefresh.setPreferredSize(new Dimension(70, 25));
        btnRefresh.addActionListener(e -> refreshData());
        panel.add(btnRefresh);
        
        return panel;
    }
    
    private JPanel createCompactTableSection() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(PRIMARY_COLOR, 1),
            "Available Export Bills",
            TitledBorder.LEFT,
            TitledBorder.TOP,
            FONT_CONTENT_SMALL,
            PRIMARY_COLOR
        ));
        
        // Compact table with fewer columns
        String[] columns = {"Invoice No", "Customer ID", "Products", "Status", "Action"};
        modelExportedBills = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 4; // Only Action column is editable
            }
        };
        
        tableExportedBills = createStyledTable(modelExportedBills);
        tableExportedBills.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                handleBillSelection();
            }
        });
        
        JScrollPane scrollPane = new JScrollPane(tableExportedBills);
        scrollPane.setPreferredSize(new Dimension(750, 180)); // Compact size
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private void createInsuranceTab() {
        insurancePanel = new JPanel(new BorderLayout());
        insurancePanel.setBackground(Color.WHITE);
        insurancePanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        
        // Compact header
        JLabel lblHeader = new JLabel("Create Insurance Policy");
        lblHeader.setFont(FONT_TITLE_SMALL);
        lblHeader.setForeground(PRIMARY_COLOR);
        lblHeader.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        insurancePanel.add(lblHeader, BorderLayout.NORTH);
        
        // Main content with scroll
        JScrollPane scrollPane = new JScrollPane(createCompactInsuranceForm());
        scrollPane.setBorder(null);
        insurancePanel.add(scrollPane, BorderLayout.CENTER);
        
        tabbedPane.addTab("📝 Create", insurancePanel);
    }
    
    private JPanel createCompactInsuranceForm() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        
        // Compact Invoice Information
        JPanel invoicePanel = createCompactInvoiceInfoPanel();
        panel.add(invoicePanel);
        panel.add(Box.createVerticalStrut(10));
        
        // Compact Admin Information
        JPanel adminPanel = createCompactAdminInfoPanel();
        panel.add(adminPanel);
        panel.add(Box.createVerticalStrut(10));
        
        // Compact Products Table
        JPanel productsPanel = createCompactProductsPanel();
        panel.add(productsPanel);
        panel.add(Box.createVerticalStrut(10));
        
        // Compact Insurance Details
        JPanel insuranceDetailsPanel = createCompactInsuranceDetailsPanel();
        panel.add(insuranceDetailsPanel);
        panel.add(Box.createVerticalStrut(10));
        
        // Compact Action Buttons
        JPanel buttonPanel = createCompactActionButtonsPanel();
        panel.add(buttonPanel);
        
        return panel;
    }
    
    private JPanel createCompactInvoiceInfoPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(PRIMARY_COLOR, 1),
            "Invoice Info",
            TitledBorder.LEFT,
            TitledBorder.TOP,
            FONT_CONTENT_SMALL,
            PRIMARY_COLOR
        ));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(2, 2, 2, 2);
        gbc.anchor = GridBagConstraints.WEST;
        
        // Invoice No
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Invoice No:"), gbc);
        gbc.gridx = 1;
        txtInvoiceNo = new MyTextField();
        txtInvoiceNo.setPreferredSize(new Dimension(120, 20));
        txtInvoiceNo.setLocked(true);
        txtInvoiceNo.setBackgroundColor(Color.decode("#F5F5F5"));
        panel.add(txtInvoiceNo, gbc);
        
        // Customer ID
        gbc.gridx = 2; gbc.gridy = 0;
        panel.add(new JLabel("Customer ID:"), gbc);
        gbc.gridx = 3;
        txtCustomerID = new MyTextField();
        txtCustomerID.setPreferredSize(new Dimension(120, 20));
        txtCustomerID.setLocked(true);
        txtCustomerID.setBackgroundColor(Color.decode("#F5F5F5"));
        panel.add(txtCustomerID, gbc);
        
        return panel;
    }
    
    private JPanel createCompactAdminInfoPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(PRIMARY_COLOR, 1),
            "Admin Info",
            TitledBorder.LEFT,
            TitledBorder.TOP,
            FONT_CONTENT_SMALL,
            PRIMARY_COLOR
        ));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(2, 2, 2, 2);
        gbc.anchor = GridBagConstraints.WEST;
        
        // Admin ID
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Admin ID:"), gbc);
        gbc.gridx = 1;
        txtAdminID = new MyTextField();
        txtAdminID.setPreferredSize(new Dimension(100, 20));
        txtAdminID.setLocked(true);
        txtAdminID.setText(Dashboard_ad.adminID);
        txtAdminID.setBackgroundColor(Color.decode("#F5F5F5"));
        panel.add(txtAdminID, gbc);
        
        // Admin Name
        gbc.gridx = 2; gbc.gridy = 0;
        panel.add(new JLabel("Admin Name:"), gbc);
        gbc.gridx = 3;
        txtAdminName = new MyTextField();
        txtAdminName.setPreferredSize(new Dimension(150, 20));
        txtAdminName.setLocked(true);
        txtAdminName.setText(Dashboard_ad.getAdminName(Dashboard_ad.adminID));
        txtAdminName.setBackgroundColor(Color.decode("#F5F5F5"));
        panel.add(txtAdminName, gbc);
        
        return panel;
    }
    
    private JPanel createCompactProductsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(PRIMARY_COLOR, 1),
            "Products (Set Warranty Periods)",
            TitledBorder.LEFT,
            TitledBorder.TOP,
            FONT_CONTENT_SMALL,
            PRIMARY_COLOR
        ));
        
        // Compact products table with fewer columns
        String[] columns = {"Product ID", "Product Name", "Qty", "Price", "Warranty (months)", "Action"};
        modelProducts = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 4 || column == 5; // Warranty period and Action columns
            }
        };
        
        tableProducts = createStyledTable(modelProducts);
        
        JScrollPane scrollPane = new JScrollPane(tableProducts);
        scrollPane.setPreferredSize(new Dimension(700, 100)); // Very compact
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createCompactInsuranceDetailsPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(PRIMARY_COLOR, 1),
            "Insurance Details",
            TitledBorder.LEFT,
            TitledBorder.TOP,
            FONT_CONTENT_SMALL,
            PRIMARY_COLOR
        ));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(2, 2, 2, 2);
        gbc.anchor = GridBagConstraints.WEST;
        
        // Description
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Description:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 3; gbc.fill = GridBagConstraints.HORIZONTAL;
        txtDescription = new JTextArea(2, 30);
        txtDescription.setFont(FONT_CONTENT_SMALL);
        txtDescription.setLineWrap(true);
        txtDescription.setWrapStyleWord(true);
        txtDescription.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        JScrollPane descScroll = new JScrollPane(txtDescription);
        descScroll.setPreferredSize(new Dimension(300, 40));
        panel.add(descScroll, gbc);
        
        // Start Date
        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 1; gbc.fill = GridBagConstraints.NONE;
        panel.add(new JLabel("Start Date:"), gbc);
        gbc.gridx = 1;
        startDate = new JDateChooser();
        startDate.setPreferredSize(new Dimension(100, 20));
        startDate.setDateFormatString("dd/MM/yyyy");
        panel.add(startDate, gbc);
        
        // End Date
        gbc.gridx = 2; gbc.gridy = 1;
        panel.add(new JLabel("End Date:"), gbc);
        gbc.gridx = 3;
        endDate = new JDateChooser();
        endDate.setPreferredSize(new Dimension(100, 20));
        endDate.setDateFormatString("dd/MM/yyyy");
        panel.add(endDate, gbc);
        
        return panel;
    }
    
    private JPanel createCompactActionButtonsPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        panel.setBackground(Color.WHITE);
        
        btnCreateInsurance = new MyButton("Create", 15);
        stylePrimaryButton(btnCreateInsurance);
        btnCreateInsurance.setPreferredSize(new Dimension(80, 25));
        btnCreateInsurance.addActionListener(e -> createInsurance());
        panel.add(btnCreateInsurance);
        
        btnSaveInsurance = new MyButton("Save & PDF", 15);
        styleSuccessButton(btnSaveInsurance);
        btnSaveInsurance.setPreferredSize(new Dimension(100, 25));
        btnSaveInsurance.addActionListener(e -> saveInsurance());
        panel.add(btnSaveInsurance);
        
        return panel;
    }
    
    private void createDetailsTab() {
        detailsPanel = new JPanel(new BorderLayout());
        detailsPanel.setBackground(Color.WHITE);
        detailsPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        
        // Compact header
        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
        headerPanel.setBackground(Color.WHITE);
        
        JLabel lblHeader = new JLabel("Insurance Records");
        lblHeader.setFont(FONT_TITLE_SMALL);
        lblHeader.setForeground(PRIMARY_COLOR);
        headerPanel.add(lblHeader);
        
        btnViewDetails = new MyButton("Details", 10);
        styleInfoButton(btnViewDetails);
        btnViewDetails.setPreferredSize(new Dimension(60, 20));
        btnViewDetails.addActionListener(e -> viewInsuranceDetails());
        headerPanel.add(btnViewDetails);
        
        btnExportExcel = new MyButton("Export", 10);
        styleSuccessButton(btnExportExcel);
        btnExportExcel.setPreferredSize(new Dimension(60, 20));
        btnExportExcel.addActionListener(e -> exportToExcel());
        headerPanel.add(btnExportExcel);
        
        detailsPanel.add(headerPanel, BorderLayout.NORTH);
        
        // Compact table
        String[] columns = {"Insurance No", "Invoice No", "Customer", "Start Date", "End Date", "Status", "Action"};
        modelInsurance = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 6; // Only Action column is editable
            }
        };
        
        tableInsurance = createStyledTable(modelInsurance);
        JScrollPane scrollPane = new JScrollPane(tableInsurance);
        scrollPane.setPreferredSize(new Dimension(750, 200)); // Compact size
        detailsPanel.add(scrollPane, BorderLayout.CENTER);
        
        tabbedPane.addTab("📊 Records", detailsPanel);
    }
    
    // Simplified methods (same logic as Form_Insurance_New but with compact UI)
    private void loadData() {
        SwingUtilities.invokeLater(() -> {
            loadExportedBills();
            loadInsuranceRecords();
        });
    }
    
    private void loadExportedBills() {
        try {
            busExportBill = new BUS_ExportBill();
            List<DTO_BillExport> bills = busExportBill.getAllAvailableExportBillsForInsurance();
            modelExportedBills.setRowCount(0);
            
            if (bills != null) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                for (DTO_BillExport bill : bills) {
                    Object[] row = {
                        bill.getInvoiceNo(),
                        bill.getCustomerId(),
                        bill.getTotalProduct(),
                        "Available for Insurance",
                        "Select"
                    };
                    modelExportedBills.addRow(row);
                }
            }
        } catch (Exception e) {
            CustomDialog.showError("Error loading export bills: " + e.getMessage());
        }
    }
    
    private void loadInsuranceRecords() {
        try {
            busWarranty = new BUS_Warranty();
            List<DTO_Insurance> insuranceList = busWarranty.getAllInsuranceWithExportInfo();
            modelInsurance.setRowCount(0);
            
            if (insuranceList != null) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                for (DTO_Insurance insurance : insuranceList) {
                    Object[] row = {
                        insurance.getInsuranceNo(),
                        insurance.getInvoiceNo(),
                        "Customer Name", // TODO: Load actual customer name
                        insurance.getStartDateInsurance().format(formatter),
                        insurance.getEndDateInsurance().format(formatter),
                        "Active",
                        "View"
                    };
                    modelInsurance.addRow(row);
                }
            }
        } catch (Exception e) {
            CustomDialog.showError("Error loading insurance records: " + e.getMessage());
        }
    }
    
    private void handleBillSelection() {
        int selectedRow = tableExportedBills.getSelectedRow();
        if (selectedRow >= 0) {
            selectedInvoiceNo = tableExportedBills.getValueAt(selectedRow, 0).toString();
            selectedCustomerID = tableExportedBills.getValueAt(selectedRow, 1).toString();
            
            // Update form fields
            txtInvoiceNo.setText(selectedInvoiceNo);
            txtCustomerID.setText(selectedCustomerID);
            
            // Load products for this invoice
            loadProductsForInvoice(selectedInvoiceNo);
            
            // Switch to insurance tab
            tabbedPane.setSelectedIndex(1);
            
            CustomDialog.showSuccess("Selected Invoice: " + selectedInvoiceNo);
        }
    }
    
    private void loadProductsForInvoice(String invoiceNo) {
        try {
            modelProducts.setRowCount(0);
            
            // TODO: Load actual product details from database
            // For now, add sample data
            Object[] sampleProduct1 = {
                "PROD001", "Xe đạp điện NIJIA", 1, 15000000.0, 12, "Edit"
            };
            Object[] sampleProduct2 = {
                "PROD002", "Xe máy điện TAILG", 1, 25000000.0, 24, "Edit"
            };
            
            modelProducts.addRow(sampleProduct1);
            modelProducts.addRow(sampleProduct2);
            
        } catch (Exception e) {
            CustomDialog.showError("Error loading products: " + e.getMessage());
        }
    }
    
    private void performSearch() {
        String keyword = txtSearch.getText().trim();
        
        if (keyword.isEmpty()) {
            loadExportedBills();
            return;
        }
        
        // TODO: Implement actual search logic
        CustomDialog.showSuccess("Search functionality will be implemented");
    }
    
    private void refreshData() {
        loadExportedBills();
        loadInsuranceRecords();
        CustomDialog.showSuccess("Data refreshed successfully!");
    }
    
    private void createInsurance() {
        if (!validateInsuranceForm()) {
            return;
        }
        
        // TODO: Implement insurance creation logic
        CustomDialog.showSuccess("Insurance created successfully!");
    }
    
    private void saveInsurance() {
        if (!validateInsuranceForm()) {
            return;
        }
        
        boolean confirm = CustomDialog.showOptionPane(
            "Confirm Save",
            "Are you sure you want to save this insurance policy?",
            UIManager.getIcon("OptionPane.questionIcon"),
            PRIMARY_COLOR
        );
        
        if (confirm) {
            // TODO: Implement save logic
            CustomDialog.showSuccess("Insurance saved and PDF exported successfully!");
        }
    }
    
    private void viewInsuranceDetails() {
        int selectedRow = tableInsurance.getSelectedRow();
        if (selectedRow >= 0) {
            String insuranceNo = tableInsurance.getValueAt(selectedRow, 0).toString();
            // TODO: Open details dialog
            CustomDialog.showSuccess("Viewing details for: " + insuranceNo);
        } else {
            CustomDialog.showError("Please select an insurance record to view details.");
        }
    }
    
    private void exportToExcel() {
        try {
            busWarranty = new BUS_Warranty();
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Save Excel File");
            fileChooser.setSelectedFile(new File("Insurance_Report.xlsx"));
            fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Excel Files (*.xlsx)", "xlsx"));
            
            if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                File file = fileChooser.getSelectedFile();
                String filePath = file.getAbsolutePath();
                if (!filePath.endsWith(".xlsx")) {
                    filePath += ".xlsx";
                }
                
                boolean success = busWarranty.exportToExcel(filePath);
                if (success) {
                    CustomDialog.showSuccess("File exported successfully!");
                }
            }
        } catch (Exception e) {
            CustomDialog.showError("Export failed: " + e.getMessage());
        }
    }
    
    private boolean validateInsuranceForm() {
        if (selectedInvoiceNo == null || selectedInvoiceNo.isEmpty()) {
            CustomDialog.showError("Please select an invoice first!");
            return false;
        }
        
        if (txtDescription.getText().trim().isEmpty()) {
            CustomDialog.showError("Please enter a description!");
            return false;
        }
        
        if (startDate.getDate() == null) {
            CustomDialog.showError("Please select a start date!");
            return false;
        }
        
        if (endDate.getDate() == null) {
            CustomDialog.showError("Please select an end date!");
            return false;
        }
        
        if (endDate.getDate().before(startDate.getDate())) {
            CustomDialog.showError("End date must be after start date!");
            return false;
        }
        
        return true;
    }
    
    // Helper methods for styling
    private void stylePrimaryButton(MyButton btn) {
        btn.setBackgroundColor(PRIMARY_COLOR);
        btn.setHoverColor(PRIMARY_HOVER);
        btn.setPressedColor(PRIMARY_HOVER.darker());
        btn.setFont(FONT_CONTENT_SMALL);
        btn.setForeground(Color.WHITE);
    }
    
    private void styleInfoButton(MyButton btn) {
        btn.setBackgroundColor(INFO_COLOR);
        btn.setHoverColor(INFO_HOVER);
        btn.setPressedColor(INFO_HOVER.darker());
        btn.setFont(FONT_CONTENT_SMALL);
        btn.setForeground(Color.WHITE);
    }
    
    private void styleSuccessButton(MyButton btn) {
        btn.setBackgroundColor(Color.decode("#28A745"));
        btn.setHoverColor(Color.decode("#218838"));
        btn.setPressedColor(Color.decode("#1E7E34"));
        btn.setFont(FONT_CONTENT_SMALL);
        btn.setForeground(Color.WHITE);
    }
    
    private MyTable createStyledTable(DefaultTableModel model) {
        return new MyTable(
            model,
            Color.WHITE,
            TEXT_PRIMARY,
            Color.decode("#E8F5E9"),
            Color.BLACK,
            PRIMARY_COLOR,
            Color.WHITE,
            FONT_CONTENT_SMALL,
            FONT_CONTENT_SMALL
        );
    }
}
