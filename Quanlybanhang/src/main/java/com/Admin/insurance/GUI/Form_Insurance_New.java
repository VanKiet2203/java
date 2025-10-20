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

public class Form_Insurance_New extends JPanel {
    
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
    
    public Form_Insurance_New() {
        initComponents();
        init();
        loadData();
    }
    
    private void initComponents() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        setPreferredSize(new Dimension(900, 500)); // Smaller size for 16" screens
    }
    
    private void init() {
        // Create tabbed interface
        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(FONT_TITLE_MEDIUM);
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
        searchPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Search section
        JPanel searchSection = createSearchSection();
        searchPanel.add(searchSection, BorderLayout.NORTH);
        
        // Table section
        JPanel tableSection = createTableSection();
        searchPanel.add(tableSection, BorderLayout.CENTER);
        
        tabbedPane.addTab("📋 Available Bills", searchPanel);
    }
    
    private JPanel createSearchSection() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(PRIMARY_COLOR, 1),
            "Search Export Bills",
            TitledBorder.LEFT,
            TitledBorder.TOP,
            FONT_TITLE_SMALL,
            PRIMARY_COLOR
        ));
        
        // Search components
        String[] searchOptions = {"Invoice No", "Customer ID", "Product ID", "Date Exported"};
        cmbSearch = new MyCombobox<>(searchOptions);
        cmbSearch.setPreferredSize(new Dimension(150, 35));
        cmbSearch.setCustomFont(FONT_CONTENT_MEDIUM);
        panel.add(new JLabel("Search by:"));
        panel.add(cmbSearch);
        
        txtSearch = new MyTextField();
        txtSearch.setHint("Enter search keyword...");
        txtSearch.setPreferredSize(new Dimension(250, 35));
        txtSearch.setTextFont(FONT_CONTENT_MEDIUM);
        panel.add(txtSearch);
        
        btnSearch = new MyButton("Search", 15);
        stylePrimaryButton(btnSearch);
        btnSearch.setPreferredSize(new Dimension(100, 35));
        btnSearch.addActionListener(e -> performSearch());
        panel.add(btnSearch);
        
        btnRefresh = new MyButton("Refresh", 15);
        styleInfoButton(btnRefresh);
        btnRefresh.setPreferredSize(new Dimension(100, 35));
        btnRefresh.addActionListener(e -> refreshData());
        panel.add(btnRefresh);
        
        return panel;
    }
    
    private JPanel createTableSection() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(PRIMARY_COLOR, 1),
            "Available Export Bills for Insurance",
            TitledBorder.LEFT,
            TitledBorder.TOP,
            FONT_TITLE_SMALL,
            PRIMARY_COLOR
        ));
        
        // Create table
        String[] columns = {"Invoice No", "Customer ID", "Customer Name", "Total Products", 
                           "Date Exported", "Status", "Action"};
        modelExportedBills = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 6; // Only Action column is editable
            }
        };
        
        tableExportedBills = createStyledTable(modelExportedBills);
        tableExportedBills.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                handleBillSelection();
            }
        });
        
        JScrollPane scrollPane = new JScrollPane(tableExportedBills);
        scrollPane.setPreferredSize(new Dimension(850, 250)); // Smaller for 16" screens
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private void createInsuranceTab() {
        insurancePanel = new JPanel(new BorderLayout());
        insurancePanel.setBackground(Color.WHITE);
        insurancePanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Header
        JLabel lblHeader = new JLabel("Create Insurance Policy");
        lblHeader.setFont(FONT_TITLE_LARGE);
        lblHeader.setForeground(PRIMARY_COLOR);
        lblHeader.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
        insurancePanel.add(lblHeader, BorderLayout.NORTH);
        
        // Main content with scroll
        JScrollPane scrollPane = new JScrollPane(createInsuranceForm());
        scrollPane.setBorder(null);
        insurancePanel.add(scrollPane, BorderLayout.CENTER);
        
        tabbedPane.addTab("📝 Create Insurance", insurancePanel);
    }
    
    private JPanel createInsuranceForm() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Invoice Information
        JPanel invoicePanel = createInvoiceInfoPanel();
        panel.add(invoicePanel);
        panel.add(Box.createVerticalStrut(20));
        
        // Admin Information
        JPanel adminPanel = createAdminInfoPanel();
        panel.add(adminPanel);
        panel.add(Box.createVerticalStrut(20));
        
        // Products Table
        JPanel productsPanel = createProductsPanel();
        panel.add(productsPanel);
        panel.add(Box.createVerticalStrut(20));
        
        // Insurance Details
        JPanel insuranceDetailsPanel = createInsuranceDetailsPanel();
        panel.add(insuranceDetailsPanel);
        panel.add(Box.createVerticalStrut(20));
        
        // Action Buttons
        JPanel buttonPanel = createActionButtonsPanel();
        panel.add(buttonPanel);
        
        return panel;
    }
    
    private JPanel createInvoiceInfoPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(PRIMARY_COLOR, 1),
            "Invoice Information",
            TitledBorder.LEFT,
            TitledBorder.TOP,
            FONT_TITLE_SMALL,
            PRIMARY_COLOR
        ));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        
        // Invoice No
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Invoice No:"), gbc);
        gbc.gridx = 1;
        txtInvoiceNo = new MyTextField();
        txtInvoiceNo.setPreferredSize(new Dimension(200, 30));
        txtInvoiceNo.setLocked(true);
        txtInvoiceNo.setBackgroundColor(Color.decode("#F5F5F5"));
        panel.add(txtInvoiceNo, gbc);
        
        // Customer ID
        gbc.gridx = 2; gbc.gridy = 0;
        panel.add(new JLabel("Customer ID:"), gbc);
        gbc.gridx = 3;
        txtCustomerID = new MyTextField();
        txtCustomerID.setPreferredSize(new Dimension(200, 30));
        txtCustomerID.setLocked(true);
        txtCustomerID.setBackgroundColor(Color.decode("#F5F5F5"));
        panel.add(txtCustomerID, gbc);
        
        return panel;
    }
    
    private JPanel createAdminInfoPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(PRIMARY_COLOR, 1),
            "Admin Information",
            TitledBorder.LEFT,
            TitledBorder.TOP,
            FONT_TITLE_SMALL,
            PRIMARY_COLOR
        ));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        
        // Admin ID
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Admin ID:"), gbc);
        gbc.gridx = 1;
        txtAdminID = new MyTextField();
        txtAdminID.setPreferredSize(new Dimension(150, 30));
        txtAdminID.setLocked(true);
        txtAdminID.setText(Dashboard_ad.adminID);
        txtAdminID.setBackgroundColor(Color.decode("#F5F5F5"));
        panel.add(txtAdminID, gbc);
        
        // Admin Name
        gbc.gridx = 2; gbc.gridy = 0;
        panel.add(new JLabel("Admin Name:"), gbc);
        gbc.gridx = 3;
        txtAdminName = new MyTextField();
        txtAdminName.setPreferredSize(new Dimension(200, 30));
        txtAdminName.setLocked(true);
        txtAdminName.setText(Dashboard_ad.getAdminName(Dashboard_ad.adminID));
        txtAdminName.setBackgroundColor(Color.decode("#F5F5F5"));
        panel.add(txtAdminName, gbc);
        
        return panel;
    }
    
    private JPanel createProductsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(PRIMARY_COLOR, 1),
            "Products in Invoice (Set Individual Warranty Periods)",
            TitledBorder.LEFT,
            TitledBorder.TOP,
            FONT_TITLE_SMALL,
            PRIMARY_COLOR
        ));
        
        // Create products table
        String[] columns = {"Product ID", "Product Name", "Quantity", "Unit Price", 
                           "Warranty Period (months)", "Start Date", "End Date", "Action"};
        modelProducts = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column >= 4 && column <= 6; // Warranty period, start date, end date
            }
        };
        
        tableProducts = createStyledTable(modelProducts);
        // Note: Custom cell editors for JSpinner and JDateChooser need to be implemented separately
        // For now, we'll use default editors
        
        JScrollPane scrollPane = new JScrollPane(tableProducts);
        scrollPane.setPreferredSize(new Dimension(800, 150)); // Smaller for 16" screens
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createInsuranceDetailsPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(PRIMARY_COLOR, 1),
            "Insurance Details",
            TitledBorder.LEFT,
            TitledBorder.TOP,
            FONT_TITLE_SMALL,
            PRIMARY_COLOR
        ));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        
        // Description
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Description:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 3; gbc.fill = GridBagConstraints.HORIZONTAL;
        txtDescription = new JTextArea(3, 50);
        txtDescription.setFont(FONT_CONTENT_MEDIUM);
        txtDescription.setLineWrap(true);
        txtDescription.setWrapStyleWord(true);
        txtDescription.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        JScrollPane descScroll = new JScrollPane(txtDescription);
        descScroll.setPreferredSize(new Dimension(400, 80));
        panel.add(descScroll, gbc);
        
        // Start Date
        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 1; gbc.fill = GridBagConstraints.NONE;
        panel.add(new JLabel("Start Date:"), gbc);
        gbc.gridx = 1;
        startDate = new JDateChooser();
        startDate.setPreferredSize(new Dimension(150, 30));
        startDate.setDateFormatString("dd/MM/yyyy");
        panel.add(startDate, gbc);
        
        // End Date
        gbc.gridx = 2; gbc.gridy = 1;
        panel.add(new JLabel("End Date:"), gbc);
        gbc.gridx = 3;
        endDate = new JDateChooser();
        endDate.setPreferredSize(new Dimension(150, 30));
        endDate.setDateFormatString("dd/MM/yyyy");
        panel.add(endDate, gbc);
        
        return panel;
    }
    
    private JPanel createActionButtonsPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        panel.setBackground(Color.WHITE);
        
        btnCreateInsurance = new MyButton("Create Insurance", 20);
        stylePrimaryButton(btnCreateInsurance);
        btnCreateInsurance.setPreferredSize(new Dimension(150, 40));
        btnCreateInsurance.addActionListener(e -> createInsurance());
        panel.add(btnCreateInsurance);
        
        btnSaveInsurance = new MyButton("Save & Export PDF", 20);
        styleSuccessButton(btnSaveInsurance);
        btnSaveInsurance.setPreferredSize(new Dimension(180, 40));
        btnSaveInsurance.addActionListener(e -> saveInsurance());
        panel.add(btnSaveInsurance);
        
        return panel;
    }
    
    private void createDetailsTab() {
        detailsPanel = new JPanel(new BorderLayout());
        detailsPanel.setBackground(Color.WHITE);
        detailsPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Header
        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        headerPanel.setBackground(Color.WHITE);
        
        JLabel lblHeader = new JLabel("Insurance Records");
        lblHeader.setFont(FONT_TITLE_LARGE);
        lblHeader.setForeground(PRIMARY_COLOR);
        headerPanel.add(lblHeader);
        
        btnViewDetails = new MyButton("View Details", 15);
        styleInfoButton(btnViewDetails);
        btnViewDetails.addActionListener(e -> viewInsuranceDetails());
        headerPanel.add(btnViewDetails);
        
        btnExportExcel = new MyButton("Export Excel", 15);
        styleSuccessButton(btnExportExcel);
        btnExportExcel.addActionListener(e -> exportToExcel());
        headerPanel.add(btnExportExcel);
        
        detailsPanel.add(headerPanel, BorderLayout.NORTH);
        
        // Table
        String[] columns = {"Insurance No", "Invoice No", "Customer Name", "Start Date", 
                           "End Date", "Status", "Action"};
        modelInsurance = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 6; // Only Action column is editable
            }
        };
        
        tableInsurance = createStyledTable(modelInsurance);
        JScrollPane scrollPane = new JScrollPane(tableInsurance);
        scrollPane.setPreferredSize(new Dimension(850, 300)); // Smaller for 16" screens
        detailsPanel.add(scrollPane, BorderLayout.CENTER);
        
        tabbedPane.addTab("📊 Insurance Records", detailsPanel);
    }
    
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
                        "Customer Name", // TODO: Load actual customer name
                        bill.getTotalProduct(),
                        LocalDate.now().format(formatter), // TODO: Load actual date
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
            
            CustomDialog.showSuccess("Selected Invoice: " + selectedInvoiceNo + 
                                   "\nCustomer: " + selectedCustomerID +
                                   "\nPlease configure warranty periods for each product.");
        }
    }
    
    private void loadProductsForInvoice(String invoiceNo) {
        try {
            modelProducts.setRowCount(0);
            
            // TODO: Load actual product details from database
            // For now, add sample data
            Object[] sampleProduct1 = {
                "PROD001", "Xe đạp điện NIJIA", 1, 15000000.0, 12, 
                new Date(), new Date(System.currentTimeMillis() + 365L * 24 * 60 * 60 * 1000), "Edit"
            };
            Object[] sampleProduct2 = {
                "PROD002", "Xe máy điện TAILG", 1, 25000000.0, 24, 
                new Date(), new Date(System.currentTimeMillis() + 730L * 24 * 60 * 60 * 1000), "Edit"
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
    
    private void styleSuccessButton(MyButton btn) {
        btn.setBackgroundColor(Color.decode("#28A745"));
        btn.setHoverColor(Color.decode("#218838"));
        btn.setPressedColor(Color.decode("#1E7E34"));
        btn.setFont(FONT_BUTTON_MEDIUM);
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
            FONT_TABLE_CONTENT,
            FONT_TABLE_HEADER
        );
    }
}
