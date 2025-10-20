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
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.io.File;

import static com.ComponentandDatabase.Components.UIConstants.*;

public class Form_Insurance_Optimized extends JPanel {
    
    // Main components
    private JTabbedPane tabbedPane;
    private JPanel searchPanel, insurancePanel, detailsPanel;
    
    // Search components
    private MyTextField txtSearch;
    private MyCombobox<String> cmbSearch;
    private MyButton btnSearch, btnRefresh;
    private MyTable tableExportedBills;
    private DefaultTableModel modelExportedBills;
    
    // Pagination components
    private JLabel lblPageInfo;
    private MyButton btnFirst, btnPrev, btnNext, btnLast;
    private JSpinner spnPageSize;
    private int currentPage = 1;
    private int pageSize = 10;
    private int totalPages = 1;
    
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
    
    public Form_Insurance_Optimized() {
        initComponents();
        init();
        loadData();
    }
    
    private void initComponents() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        setPreferredSize(new Dimension(750, 400)); // Very compact for 16" screens
    }
    
    private void init() {
        // Create tabbed interface
        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(FONT_CONTENT_SMALL);
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
        searchPanel.setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));
        
        // Compact search section
        JPanel searchSection = createCompactSearchSection();
        searchPanel.add(searchSection, BorderLayout.NORTH);
        
        // Compact table section with pagination
        JPanel tableSection = createCompactTableSectionWithPagination();
        searchPanel.add(tableSection, BorderLayout.CENTER);
        
        tabbedPane.addTab("📋 Bills", searchPanel);
    }
    
    private JPanel createCompactSearchSection() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 3, 3));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(PRIMARY_COLOR, 1),
            "Search",
            TitledBorder.LEFT,
            TitledBorder.TOP,
            FONT_CONTENT_SMALL,
            PRIMARY_COLOR
        ));
        
        // Very compact search components
        String[] searchOptions = {"Invoice", "Customer", "Product"};
        cmbSearch = new MyCombobox<>(searchOptions);
        cmbSearch.setPreferredSize(new Dimension(80, 20));
        cmbSearch.setCustomFont(FONT_CONTENT_SMALL);
        panel.add(cmbSearch);
        
        txtSearch = new MyTextField();
        txtSearch.setHint("Search...");
        txtSearch.setPreferredSize(new Dimension(120, 20));
        txtSearch.setTextFont(FONT_CONTENT_SMALL);
        panel.add(txtSearch);
        
        btnSearch = new MyButton("Search", 8);
        stylePrimaryButton(btnSearch);
        btnSearch.setPreferredSize(new Dimension(60, 20));
        btnSearch.addActionListener(e -> performSearch());
        panel.add(btnSearch);
        
        btnRefresh = new MyButton("Refresh", 8);
        styleInfoButton(btnRefresh);
        btnRefresh.setPreferredSize(new Dimension(60, 20));
        btnRefresh.addActionListener(e -> refreshData());
        panel.add(btnRefresh);
        
        return panel;
    }
    
    private JPanel createCompactTableSectionWithPagination() {
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
        
        // Very compact table with fewer columns
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
        scrollPane.setPreferredSize(new Dimension(700, 120)); // Very compact
        panel.add(scrollPane, BorderLayout.CENTER);
        
        // Pagination controls
        JPanel paginationPanel = createPaginationPanel();
        panel.add(paginationPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private JPanel createPaginationPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 2, 2));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(Color.GRAY, 1),
            "Pagination",
            TitledBorder.LEFT,
            TitledBorder.TOP,
            FONT_CONTENT_SMALL,
            Color.GRAY
        ));
        
        // Page size selector
        panel.add(new JLabel("Page Size:"));
        spnPageSize = new JSpinner(new SpinnerNumberModel(10, 5, 50, 5));
        spnPageSize.setPreferredSize(new Dimension(50, 18));
        spnPageSize.addChangeListener(e -> {
            pageSize = (Integer) spnPageSize.getValue();
            currentPage = 1;
            loadExportedBills();
        });
        panel.add(spnPageSize);
        
        // Navigation buttons
        btnFirst = new MyButton("<<", 8);
        btnFirst.setPreferredSize(new Dimension(30, 18));
        btnFirst.addActionListener(e -> goToFirstPage());
        panel.add(btnFirst);
        
        btnPrev = new MyButton("<", 8);
        btnPrev.setPreferredSize(new Dimension(25, 18));
        btnPrev.addActionListener(e -> goToPreviousPage());
        panel.add(btnPrev);
        
        // Page info
        lblPageInfo = new JLabel("Page 1 of 1");
        lblPageInfo.setFont(FONT_CONTENT_SMALL);
        panel.add(lblPageInfo);
        
        btnNext = new MyButton(">", 8);
        btnNext.setPreferredSize(new Dimension(25, 18));
        btnNext.addActionListener(e -> goToNextPage());
        panel.add(btnNext);
        
        btnLast = new MyButton(">>", 8);
        btnLast.setPreferredSize(new Dimension(30, 18));
        btnLast.addActionListener(e -> goToLastPage());
        panel.add(btnLast);
        
        return panel;
    }
    
    private void createInsuranceTab() {
        insurancePanel = new JPanel(new BorderLayout());
        insurancePanel.setBackground(Color.WHITE);
        insurancePanel.setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));
        
        // Very compact header
        JLabel lblHeader = new JLabel("Create Insurance");
        lblHeader.setFont(FONT_CONTENT_SMALL);
        lblHeader.setForeground(PRIMARY_COLOR);
        lblHeader.setBorder(BorderFactory.createEmptyBorder(0, 0, 5, 0));
        insurancePanel.add(lblHeader, BorderLayout.NORTH);
        
        // Main content with scroll
        JScrollPane scrollPane = new JScrollPane(createVeryCompactInsuranceForm());
        scrollPane.setBorder(null);
        insurancePanel.add(scrollPane, BorderLayout.CENTER);
        
        tabbedPane.addTab("📝 Create", insurancePanel);
    }
    
    private JPanel createVeryCompactInsuranceForm() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));
        
        // Very compact Invoice Information
        JPanel invoicePanel = createVeryCompactInvoiceInfoPanel();
        panel.add(invoicePanel);
        panel.add(Box.createVerticalStrut(5));
        
        // Very compact Admin Information
        JPanel adminPanel = createVeryCompactAdminInfoPanel();
        panel.add(adminPanel);
        panel.add(Box.createVerticalStrut(5));
        
        // Very compact Products Table
        JPanel productsPanel = createVeryCompactProductsPanel();
        panel.add(productsPanel);
        panel.add(Box.createVerticalStrut(5));
        
        // Very compact Insurance Details
        JPanel insuranceDetailsPanel = createVeryCompactInsuranceDetailsPanel();
        panel.add(insuranceDetailsPanel);
        panel.add(Box.createVerticalStrut(5));
        
        // Very compact Action Buttons
        JPanel buttonPanel = createVeryCompactActionButtonsPanel();
        panel.add(buttonPanel);
        
        return panel;
    }
    
    private JPanel createVeryCompactInvoiceInfoPanel() {
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
        gbc.insets = new Insets(1, 1, 1, 1);
        gbc.anchor = GridBagConstraints.WEST;
        
        // Invoice No
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Invoice:"), gbc);
        gbc.gridx = 1;
        txtInvoiceNo = new MyTextField();
        txtInvoiceNo.setPreferredSize(new Dimension(100, 18));
        txtInvoiceNo.setLocked(true);
        txtInvoiceNo.setBackgroundColor(Color.decode("#F5F5F5"));
        panel.add(txtInvoiceNo, gbc);
        
        // Customer ID
        gbc.gridx = 2; gbc.gridy = 0;
        panel.add(new JLabel("Customer:"), gbc);
        gbc.gridx = 3;
        txtCustomerID = new MyTextField();
        txtCustomerID.setPreferredSize(new Dimension(100, 18));
        txtCustomerID.setLocked(true);
        txtCustomerID.setBackgroundColor(Color.decode("#F5F5F5"));
        panel.add(txtCustomerID, gbc);
        
        return panel;
    }
    
    private JPanel createVeryCompactAdminInfoPanel() {
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
        gbc.insets = new Insets(1, 1, 1, 1);
        gbc.anchor = GridBagConstraints.WEST;
        
        // Admin ID
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Admin ID:"), gbc);
        gbc.gridx = 1;
        txtAdminID = new MyTextField();
        txtAdminID.setPreferredSize(new Dimension(80, 18));
        txtAdminID.setLocked(true);
        txtAdminID.setText(Dashboard_ad.adminID);
        txtAdminID.setBackgroundColor(Color.decode("#F5F5F5"));
        panel.add(txtAdminID, gbc);
        
        // Admin Name
        gbc.gridx = 2; gbc.gridy = 0;
        panel.add(new JLabel("Admin Name:"), gbc);
        gbc.gridx = 3;
        txtAdminName = new MyTextField();
        txtAdminName.setPreferredSize(new Dimension(120, 18));
        txtAdminName.setLocked(true);
        txtAdminName.setText(Dashboard_ad.getAdminName(Dashboard_ad.adminID));
        txtAdminName.setBackgroundColor(Color.decode("#F5F5F5"));
        panel.add(txtAdminName, gbc);
        
        return panel;
    }
    
    private JPanel createVeryCompactProductsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(PRIMARY_COLOR, 1),
            "Products (Set Warranty)",
            TitledBorder.LEFT,
            TitledBorder.TOP,
            FONT_CONTENT_SMALL,
            PRIMARY_COLOR
        ));
        
        // Very compact products table
        String[] columns = {"Product ID", "Name", "Qty", "Price", "Warranty", "Action"};
        modelProducts = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 4 || column == 5; // Warranty period and Action columns
            }
        };
        
        tableProducts = createStyledTable(modelProducts);
        
        JScrollPane scrollPane = new JScrollPane(tableProducts);
        scrollPane.setPreferredSize(new Dimension(650, 80)); // Very compact
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createVeryCompactInsuranceDetailsPanel() {
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
        gbc.insets = new Insets(1, 1, 1, 1);
        gbc.anchor = GridBagConstraints.WEST;
        
        // Description
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Description:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 3; gbc.fill = GridBagConstraints.HORIZONTAL;
        txtDescription = new JTextArea(2, 25);
        txtDescription.setFont(FONT_CONTENT_SMALL);
        txtDescription.setLineWrap(true);
        txtDescription.setWrapStyleWord(true);
        txtDescription.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        JScrollPane descScroll = new JScrollPane(txtDescription);
        descScroll.setPreferredSize(new Dimension(250, 35));
        panel.add(descScroll, gbc);
        
        // Start Date
        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 1; gbc.fill = GridBagConstraints.NONE;
        panel.add(new JLabel("Start:"), gbc);
        gbc.gridx = 1;
        startDate = new JDateChooser();
        startDate.setPreferredSize(new Dimension(80, 18));
        startDate.setDateFormatString("dd/MM/yyyy");
        panel.add(startDate, gbc);
        
        // End Date
        gbc.gridx = 2; gbc.gridy = 1;
        panel.add(new JLabel("End:"), gbc);
        gbc.gridx = 3;
        endDate = new JDateChooser();
        endDate.setPreferredSize(new Dimension(80, 18));
        endDate.setDateFormatString("dd/MM/yyyy");
        panel.add(endDate, gbc);
        
        return panel;
    }
    
    private JPanel createVeryCompactActionButtonsPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 3));
        panel.setBackground(Color.WHITE);
        
        btnCreateInsurance = new MyButton("Create", 10);
        stylePrimaryButton(btnCreateInsurance);
        btnCreateInsurance.setPreferredSize(new Dimension(60, 20));
        btnCreateInsurance.addActionListener(e -> createInsurance());
        panel.add(btnCreateInsurance);
        
        btnSaveInsurance = new MyButton("Save & PDF", 10);
        styleSuccessButton(btnSaveInsurance);
        btnSaveInsurance.setPreferredSize(new Dimension(80, 20));
        btnSaveInsurance.addActionListener(e -> saveInsurance());
        panel.add(btnSaveInsurance);
        
        return panel;
    }
    
    private void createDetailsTab() {
        detailsPanel = new JPanel(new BorderLayout());
        detailsPanel.setBackground(Color.WHITE);
        detailsPanel.setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));
        
        // Very compact header
        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 3, 3));
        headerPanel.setBackground(Color.WHITE);
        
        JLabel lblHeader = new JLabel("Insurance Records");
        lblHeader.setFont(FONT_CONTENT_SMALL);
        lblHeader.setForeground(PRIMARY_COLOR);
        headerPanel.add(lblHeader);
        
        btnViewDetails = new MyButton("Details", 8);
        styleInfoButton(btnViewDetails);
        btnViewDetails.setPreferredSize(new Dimension(50, 18));
        btnViewDetails.addActionListener(e -> viewInsuranceDetails());
        headerPanel.add(btnViewDetails);
        
        btnExportExcel = new MyButton("Export", 8);
        styleSuccessButton(btnExportExcel);
        btnExportExcel.setPreferredSize(new Dimension(50, 18));
        btnExportExcel.addActionListener(e -> exportToExcel());
        headerPanel.add(btnExportExcel);
        
        detailsPanel.add(headerPanel, BorderLayout.NORTH);
        
        // Very compact table
        String[] columns = {"Insurance No", "Invoice No", "Customer", "Start", "End", "Status", "Action"};
        modelInsurance = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 6; // Only Action column is editable
            }
        };
        
        tableInsurance = createStyledTable(modelInsurance);
        JScrollPane scrollPane = new JScrollPane(tableInsurance);
        scrollPane.setPreferredSize(new Dimension(700, 150)); // Very compact
        detailsPanel.add(scrollPane, BorderLayout.CENTER);
        
        tabbedPane.addTab("📊 Records", detailsPanel);
    }
    
    // Pagination methods
    private void goToFirstPage() {
        currentPage = 1;
        loadExportedBills();
    }
    
    private void goToPreviousPage() {
        if (currentPage > 1) {
            currentPage--;
            loadExportedBills();
        }
    }
    
    private void goToNextPage() {
        if (currentPage < totalPages) {
            currentPage++;
            loadExportedBills();
        }
    }
    
    private void goToLastPage() {
        currentPage = totalPages;
        loadExportedBills();
    }
    
    private void updatePaginationControls() {
        lblPageInfo.setText("Page " + currentPage + " of " + totalPages);
        btnFirst.setEnabled(currentPage > 1);
        btnPrev.setEnabled(currentPage > 1);
        btnNext.setEnabled(currentPage < totalPages);
        btnLast.setEnabled(currentPage < totalPages);
    }
    
    // Data loading methods with pagination
    private void loadData() {
        SwingUtilities.invokeLater(() -> {
            loadExportedBills();
            loadInsuranceRecords();
        });
    }
    
    private void loadExportedBills() {
        try {
            busExportBill = new BUS_ExportBill();
            List<DTO_BillExport> allBills = busExportBill.getAllAvailableExportBillsForInsurance();
            
            if (allBills != null) {
                // Calculate pagination
                int totalItems = allBills.size();
                totalPages = (int) Math.ceil((double) totalItems / pageSize);
                if (totalPages == 0) totalPages = 1;
                
                // Ensure current page is valid
                if (currentPage > totalPages) currentPage = totalPages;
                if (currentPage < 1) currentPage = 1;
                
                // Get items for current page
                int startIndex = (currentPage - 1) * pageSize;
                int endIndex = Math.min(startIndex + pageSize, totalItems);
                
                modelExportedBills.setRowCount(0);
                
                for (int i = startIndex; i < endIndex; i++) {
                    DTO_BillExport bill = allBills.get(i);
                    Object[] row = {
                        bill.getInvoiceNo(),
                        bill.getCustomerId(),
                        bill.getTotalProduct(),
                        "Available for Insurance",
                        "Select"
                    };
                    modelExportedBills.addRow(row);
                }
                
                updatePaginationControls();
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
        
        // TODO: Implement actual search logic with pagination
        CustomDialog.showSuccess("Search functionality will be implemented");
    }
    
    private void refreshData() {
        currentPage = 1;
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
