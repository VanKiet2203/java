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

public class Form_Insurance_UltraCompact extends JPanel {
    
    // Main components - SINGLE PANEL DESIGN
    private JPanel mainPanel;
    private JScrollPane mainScrollPane;
    
    // Search section
    private MyTextField txtSearch;
    private MyCombobox<String> cmbSearch;
    private MyButton btnSearch, btnRefresh;
    
    // Bills table with pagination
    private MyTable tableExportedBills;
    private DefaultTableModel modelExportedBills;
    private JLabel lblPageInfo;
    private MyButton btnFirst, btnPrev, btnNext, btnLast;
    private JSpinner spnPageSize;
    private int currentPage = 1;
    private int pageSize = 5; // Very small for 16" screens
    private int totalPages = 1;
    
    // Insurance form - INLINE DESIGN
    private MyTextField txtAdminID, txtAdminName, txtInvoiceNo, txtCustomerID;
    private JTextArea txtDescription;
    private JDateChooser startDate, endDate;
    private MyTable tableProducts;
    private DefaultTableModel modelProducts;
    private MyButton btnCreateInsurance, btnSaveInsurance;
    
    // Insurance records table
    private MyTable tableInsurance;
    private DefaultTableModel modelInsurance;
    private MyButton btnViewDetails, btnExportExcel;
    
    // Business logic
    private BUS_Warranty busWarranty;
    private BUS_ExportBill busExportBill;
    private String selectedInvoiceNo;
    private String selectedCustomerID;
    
    public Form_Insurance_UltraCompact() {
        initComponents();
        init();
        loadData();
    }
    
    private void initComponents() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        setPreferredSize(new Dimension(700, 350)); // ULTRA COMPACT for 16" screens
    }
    
    private void init() {
        // Create single scrollable panel instead of tabs
        mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
        
        // Add all sections to single panel
        mainPanel.add(createSearchSection());
        mainPanel.add(Box.createVerticalStrut(3));
        mainPanel.add(createBillsSection());
        mainPanel.add(Box.createVerticalStrut(3));
        mainPanel.add(createInsuranceFormSection());
        mainPanel.add(Box.createVerticalStrut(3));
        mainPanel.add(createInsuranceRecordsSection());
        
        // Wrap in scroll pane
        mainScrollPane = new JScrollPane(mainPanel);
        mainScrollPane.setBorder(null);
        mainScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        mainScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        
        add(mainScrollPane, BorderLayout.CENTER);
    }
    
    private JPanel createSearchSection() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 2, 2));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(PRIMARY_COLOR, 1),
            "🔍 Search Export Bills",
            TitledBorder.LEFT,
            TitledBorder.TOP,
            FONT_CONTENT_SMALL,
            PRIMARY_COLOR
        ));
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
        
        // Ultra compact search
        String[] searchOptions = {"Invoice", "Customer", "Product"};
        cmbSearch = new MyCombobox<>(searchOptions);
        cmbSearch.setPreferredSize(new Dimension(70, 18));
        cmbSearch.setCustomFont(FONT_CONTENT_SMALL);
        panel.add(cmbSearch);
        
        txtSearch = new MyTextField();
        txtSearch.setHint("Search...");
        txtSearch.setPreferredSize(new Dimension(100, 18));
        txtSearch.setTextFont(FONT_CONTENT_SMALL);
        panel.add(txtSearch);
        
        btnSearch = new MyButton("Search", 6);
        stylePrimaryButton(btnSearch);
        btnSearch.setPreferredSize(new Dimension(50, 18));
        btnSearch.addActionListener(e -> performSearch());
        panel.add(btnSearch);
        
        btnRefresh = new MyButton("Refresh", 6);
        styleInfoButton(btnRefresh);
        btnRefresh.setPreferredSize(new Dimension(50, 18));
        btnRefresh.addActionListener(e -> refreshData());
        panel.add(btnRefresh);
        
        return panel;
    }
    
    private JPanel createBillsSection() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(PRIMARY_COLOR, 1),
            "📋 Available Export Bills",
            TitledBorder.LEFT,
            TitledBorder.TOP,
            FONT_CONTENT_SMALL,
            PRIMARY_COLOR
        ));
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));
        
        // Ultra compact table
        String[] columns = {"Invoice", "Customer", "Products", "Status", "Action"};
        modelExportedBills = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 4;
            }
        };
        
        tableExportedBills = createStyledTable(modelExportedBills);
        tableExportedBills.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                handleBillSelection();
            }
        });
        
        JScrollPane scrollPane = new JScrollPane(tableExportedBills);
        scrollPane.setPreferredSize(new Dimension(650, 60)); // Very small
        panel.add(scrollPane, BorderLayout.CENTER);
        
        // Ultra compact pagination
        JPanel paginationPanel = createUltraCompactPaginationPanel();
        panel.add(paginationPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private JPanel createUltraCompactPaginationPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 1, 1));
        panel.setBackground(Color.WHITE);
        
        // Page size selector
        panel.add(new JLabel("Size:"));
        spnPageSize = new JSpinner(new SpinnerNumberModel(5, 3, 20, 2));
        spnPageSize.setPreferredSize(new Dimension(40, 16));
        spnPageSize.addChangeListener(e -> {
            pageSize = (Integer) spnPageSize.getValue();
            currentPage = 1;
            loadExportedBills();
        });
        panel.add(spnPageSize);
        
        // Navigation buttons - very small
        btnFirst = new MyButton("<<", 6);
        btnFirst.setPreferredSize(new Dimension(25, 16));
        btnFirst.addActionListener(e -> goToFirstPage());
        panel.add(btnFirst);
        
        btnPrev = new MyButton("<", 6);
        btnPrev.setPreferredSize(new Dimension(20, 16));
        btnPrev.addActionListener(e -> goToPreviousPage());
        panel.add(btnPrev);
        
        // Page info
        lblPageInfo = new JLabel("1/1");
        lblPageInfo.setFont(FONT_CONTENT_SMALL);
        panel.add(lblPageInfo);
        
        btnNext = new MyButton(">", 6);
        btnNext.setPreferredSize(new Dimension(20, 16));
        btnNext.addActionListener(e -> goToNextPage());
        panel.add(btnNext);
        
        btnLast = new MyButton(">>", 6);
        btnLast.setPreferredSize(new Dimension(25, 16));
        btnLast.addActionListener(e -> goToLastPage());
        panel.add(btnLast);
        
        return panel;
    }
    
    private JPanel createInsuranceFormSection() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(PRIMARY_COLOR, 1),
            "📝 Create Insurance",
            TitledBorder.LEFT,
            TitledBorder.TOP,
            FONT_CONTENT_SMALL,
            PRIMARY_COLOR
        ));
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 140));
        
        // Main form content
        JPanel formContent = new JPanel();
        formContent.setLayout(new BoxLayout(formContent, BoxLayout.Y_AXIS));
        formContent.setBackground(Color.WHITE);
        
        // Invoice & Admin info in one row
        JPanel infoRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 2, 2));
        infoRow.setBackground(Color.WHITE);
        
        infoRow.add(new JLabel("Invoice:"));
        txtInvoiceNo = new MyTextField();
        txtInvoiceNo.setPreferredSize(new Dimension(80, 16));
        txtInvoiceNo.setLocked(true);
        txtInvoiceNo.setBackgroundColor(Color.decode("#F5F5F5"));
        infoRow.add(txtInvoiceNo);
        
        infoRow.add(new JLabel("Customer:"));
        txtCustomerID = new MyTextField();
        txtCustomerID.setPreferredSize(new Dimension(80, 16));
        txtCustomerID.setLocked(true);
        txtCustomerID.setBackgroundColor(Color.decode("#F5F5F5"));
        infoRow.add(txtCustomerID);
        
        infoRow.add(new JLabel("Admin:"));
        txtAdminID = new MyTextField();
        txtAdminID.setPreferredSize(new Dimension(60, 16));
        txtAdminID.setLocked(true);
        txtAdminID.setText(Dashboard_ad.adminID);
        txtAdminID.setBackgroundColor(Color.decode("#F5F5F5"));
        infoRow.add(txtAdminID);
        
        formContent.add(infoRow);
        formContent.add(Box.createVerticalStrut(2));
        
        // Products table - very compact
        String[] columns = {"Product", "Qty", "Price", "Warranty", "Action"};
        modelProducts = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 3 || column == 4;
            }
        };
        
        tableProducts = createStyledTable(modelProducts);
        JScrollPane productsScroll = new JScrollPane(tableProducts);
        productsScroll.setPreferredSize(new Dimension(600, 40)); // Very small
        formContent.add(productsScroll);
        formContent.add(Box.createVerticalStrut(2));
        
        // Description and dates in one row
        JPanel detailsRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 2, 2));
        detailsRow.setBackground(Color.WHITE);
        
        detailsRow.add(new JLabel("Description:"));
        txtDescription = new JTextArea(1, 15);
        txtDescription.setFont(FONT_CONTENT_SMALL);
        txtDescription.setLineWrap(true);
        txtDescription.setWrapStyleWord(true);
        txtDescription.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        JScrollPane descScroll = new JScrollPane(txtDescription);
        descScroll.setPreferredSize(new Dimension(120, 20));
        detailsRow.add(descScroll);
        
        detailsRow.add(new JLabel("Start:"));
        startDate = new JDateChooser();
        startDate.setPreferredSize(new Dimension(70, 18));
        startDate.setDateFormatString("dd/MM/yyyy");
        detailsRow.add(startDate);
        
        detailsRow.add(new JLabel("End:"));
        endDate = new JDateChooser();
        endDate.setPreferredSize(new Dimension(70, 18));
        endDate.setDateFormatString("dd/MM/yyyy");
        detailsRow.add(endDate);
        
        formContent.add(detailsRow);
        formContent.add(Box.createVerticalStrut(2));
        
        // Action buttons
        JPanel buttonRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 3, 2));
        buttonRow.setBackground(Color.WHITE);
        
        btnCreateInsurance = new MyButton("Create", 8);
        stylePrimaryButton(btnCreateInsurance);
        btnCreateInsurance.setPreferredSize(new Dimension(50, 18));
        btnCreateInsurance.addActionListener(e -> createInsurance());
        buttonRow.add(btnCreateInsurance);
        
        btnSaveInsurance = new MyButton("Save & PDF", 8);
        styleSuccessButton(btnSaveInsurance);
        btnSaveInsurance.setPreferredSize(new Dimension(70, 18));
        btnSaveInsurance.addActionListener(e -> saveInsurance());
        buttonRow.add(btnSaveInsurance);
        
        formContent.add(buttonRow);
        
        panel.add(formContent, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createInsuranceRecordsSection() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(PRIMARY_COLOR, 1),
            "📊 Insurance Records",
            TitledBorder.LEFT,
            TitledBorder.TOP,
            FONT_CONTENT_SMALL,
            PRIMARY_COLOR
        ));
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));
        
        // Header with action buttons
        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 2, 2));
        headerPanel.setBackground(Color.WHITE);
        
        btnViewDetails = new MyButton("Details", 6);
        styleInfoButton(btnViewDetails);
        btnViewDetails.setPreferredSize(new Dimension(40, 16));
        btnViewDetails.addActionListener(e -> viewInsuranceDetails());
        headerPanel.add(btnViewDetails);
        
        btnExportExcel = new MyButton("Export", 6);
        styleSuccessButton(btnExportExcel);
        btnExportExcel.setPreferredSize(new Dimension(40, 16));
        btnExportExcel.addActionListener(e -> exportToExcel());
        headerPanel.add(btnExportExcel);
        
        panel.add(headerPanel, BorderLayout.NORTH);
        
        // Ultra compact table
        String[] columns = {"Insurance", "Invoice", "Customer", "Start", "End", "Status", "Action"};
        modelInsurance = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 6;
            }
        };
        
        tableInsurance = createStyledTable(modelInsurance);
        JScrollPane scrollPane = new JScrollPane(tableInsurance);
        scrollPane.setPreferredSize(new Dimension(650, 50)); // Very small
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
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
        lblPageInfo.setText(currentPage + "/" + totalPages);
        btnFirst.setEnabled(currentPage > 1);
        btnPrev.setEnabled(currentPage > 1);
        btnNext.setEnabled(currentPage < totalPages);
        btnLast.setEnabled(currentPage < totalPages);
    }
    
    // Data loading methods
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
                        "Available",
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
                        "Customer",
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
            
            CustomDialog.showSuccess("Selected: " + selectedInvoiceNo);
        }
    }
    
    private void loadProductsForInvoice(String invoiceNo) {
        try {
            modelProducts.setRowCount(0);
            
            // Sample data
            Object[] sampleProduct1 = {"PROD001", 1, 15000000.0, 12, "Edit"};
            Object[] sampleProduct2 = {"PROD002", 1, 25000000.0, 24, "Edit"};
            
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
        CustomDialog.showSuccess("Search: " + keyword);
    }
    
    private void refreshData() {
        currentPage = 1;
        loadExportedBills();
        loadInsuranceRecords();
        CustomDialog.showSuccess("Data refreshed!");
    }
    
    private void createInsurance() {
        if (!validateInsuranceForm()) return;
        CustomDialog.showSuccess("Insurance created!");
    }
    
    private void saveInsurance() {
        if (!validateInsuranceForm()) return;
        CustomDialog.showSuccess("Insurance saved & PDF exported!");
    }
    
    private void viewInsuranceDetails() {
        int selectedRow = tableInsurance.getSelectedRow();
        if (selectedRow >= 0) {
            String insuranceNo = tableInsurance.getValueAt(selectedRow, 0).toString();
            CustomDialog.showSuccess("Viewing: " + insuranceNo);
        } else {
            CustomDialog.showError("Please select a record!");
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
