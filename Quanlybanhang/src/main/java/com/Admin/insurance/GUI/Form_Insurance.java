package com.Admin.insurance.GUI;

import com.Admin.dashboard_admin.GUI.Dashboard_ad;
import com.ComponentandDatabase.Components.MyButton;
import com.ComponentandDatabase.Components.MyPanel;
import com.ComponentandDatabase.Components.MyTextField;
import com.ComponentandDatabase.Components.MyCombobox;
import com.ComponentandDatabase.Components.MyTable;
import com.ComponentandDatabase.Components.CustomDialog;
import com.Admin.insurance.BUS.BUS_Warranty;
import com.Admin.export.BUS.BUS_ExportBill;
import com.Admin.export.DTO.DTO_BillExport;
import com.Admin.insurance.DTO.DTO_Insurance;
import com.Admin.insurance.DTO.DTO_InsuranceDetails;
import static com.ComponentandDatabase.Components.UIConstants.*;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.util.List;
import java.util.ArrayList;
import java.time.format.DateTimeFormatter;
import java.time.LocalDate;

public class Form_Insurance extends JPanel {
    private JPanel panel, panelSearch;
    private JLabel lblTitle;
    private MyButton bntSearch, bntExportFile, bntRefresh, bntExportPDF;
    private MyTextField txtSearch; 
    private MyCombobox<String> cmbSearch;
    private MyTable tableExportBills, tableInsurance;
    private BUS_Warranty busWarranty;
    private BUS_ExportBill busExportBill;
    private DefaultTableModel modelExportBills, modelInsurance;
    
    public Form_Insurance() {
        initComponents();
        init();
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(1200, 700));
        setBackground(Color.WHITE);
    }

    private void init() {
        // Tạo main panel với scroll
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(null);
        mainPanel.setPreferredSize(new Dimension(1200, 1000));
        mainPanel.setBackground(Color.WHITE);
        
        // Tạo scroll pane
        JScrollPane scrollPane = new JScrollPane(mainPanel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setBorder(null);
        
        add(scrollPane, BorderLayout.CENTER);
        panel = mainPanel;

        // Title
        lblTitle = new JLabel("INSURANCE MANAGEMENT");
        lblTitle.setFont(FONT_TITLE_LARGE);
        lblTitle.setForeground(PRIMARY_COLOR);
        lblTitle.setBounds(20, 10, 400, 40);
        panel.add(lblTitle);
        
        // Search Panel
        createSearchPanel();
        
        // Action Buttons Panel
        createActionButtonsPanel();
        
        // Export Bills Table
        createExportBillsTable();
        
        // Insurance Table
        createInsuranceTable();
        
        // Load data
        SwingUtilities.invokeLater(() -> {
            loadExportBillsData();
            loadInsuranceData();
        });
    }
    
    private void createSearchPanel() {
        panelSearch = new MyPanel(Color.WHITE);
        panelSearch.setLayout(null);
        panelSearch.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(PRIMARY_COLOR, 2),
            "Search & Filter",
            0, 0,
            FONT_TITLE_SMALL,
            PRIMARY_COLOR
        ));
        panelSearch.setBounds(20, 60, 1160, 80);
        
        // Search components
        String[] itemsSearch = {"Invoice No", "Customer ID", "Insurance No", "Product ID"};
        cmbSearch = new MyCombobox<>(itemsSearch);
        cmbSearch.setBounds(20, 30, 150, 35);
        cmbSearch.setCustomFont(FONT_CONTENT_MEDIUM);
        panelSearch.add(cmbSearch);
        
        txtSearch = new MyTextField();
        txtSearch.setHint("Search records...");
        txtSearch.setBounds(180, 30, 300, 35);
        txtSearch.setTextFont(FONT_CONTENT_MEDIUM);
        txtSearch.setHintFont(FONT_CONTENT_SMALL);
        panelSearch.add(txtSearch);
       
        bntSearch = new MyButton("Search", 20);
        stylePrimaryButton(bntSearch);
        bntSearch.setBounds(490, 30, 120, 35);
        bntSearch.addActionListener(e -> performSearch());
        panelSearch.add(bntSearch);
        
        bntRefresh = new MyButton("Refresh", 20);
        styleInfoButton(bntRefresh);
        bntRefresh.setBounds(620, 30, 120, 35);
        bntRefresh.addActionListener(e -> refreshData());
        panelSearch.add(bntRefresh);
        
        panel.add(panelSearch);
    }
    
    private void createActionButtonsPanel() {
        JPanel actionPanel = new MyPanel(Color.WHITE);
        actionPanel.setLayout(null);
        actionPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(PRIMARY_COLOR, 2),
            "Actions",
            0, 0,
            FONT_TITLE_SMALL,
            PRIMARY_COLOR
        ));
        actionPanel.setBounds(20, 150, 1160, 80);
        
        // Export Excel button
        bntExportFile = new MyButton("Export Excel", 20);
        styleInfoButton(bntExportFile);
        bntExportFile.setBounds(20, 30, 150, 35);
        bntExportFile.addActionListener(e -> exportToExcel());
        actionPanel.add(bntExportFile);
        
        // Export PDF button
        bntExportPDF = new MyButton("Export PDF", 20);
        styleDangerButton(bntExportPDF);
        bntExportPDF.setBounds(180, 30, 150, 35);
        bntExportPDF.addActionListener(e -> exportToPDF());
        actionPanel.add(bntExportPDF);
        
        panel.add(actionPanel);
    }
    
    private void createExportBillsTable() {
        String[] columnNames = {
            "Invoice No", "Customer ID", "Customer Name", "Total Products", 
            "Date Exported", "Insurance Status", "Action"
        };
        
        modelExportBills = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 6; // Only Action column is editable
            }
        };
        
        tableExportBills = new MyTable(
            modelExportBills,
            Color.WHITE,
            TEXT_PRIMARY,
            Color.decode("#E8F5E9"),
            Color.BLACK,
            PRIMARY_COLOR,
            Color.WHITE,
            FONT_TABLE_CONTENT,
            FONT_TABLE_HEADER
        );
        
        tableExportBills.setRowHeight(30);
        
        // Add selection listener
        tableExportBills.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int selectedRow = tableExportBills.getSelectedRow();
                if (selectedRow >= 0) {
                    String insuranceStatus = tableExportBills.getValueAt(selectedRow, 5).toString();
                    if ("Available for Insurance".equals(insuranceStatus)) {
                        openWarrantyDialog(selectedRow);
                    } else {
                        CustomDialog.showError("This invoice has already been insured!");
                    }
                }
            }
        });
        
        JScrollPane scrollPane = new JScrollPane(tableExportBills);
        scrollPane.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(PRIMARY_COLOR, 2),
            "Export Bills Available for Insurance",
            0, 0,
            FONT_TITLE_SMALL,
            PRIMARY_COLOR
        ));
        scrollPane.setBounds(20, 240, 1160, 200);
        
        panel.add(scrollPane);
    }
    
    private void createInsuranceTable() {
        String[] columnNames = {
            "Insurance No", "Invoice No", "Customer Name", "Start Date", 
            "End Date", "Status", "View Details", "Export PDF"
        };
        
        modelInsurance = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column >= 6; // Only View Details and Export PDF columns are editable
            }
        };
        
        tableInsurance = new MyTable(
            modelInsurance,
            Color.WHITE,
            TEXT_PRIMARY,
            Color.decode("#E8F5E9"),
            Color.BLACK,
            PRIMARY_COLOR,
            Color.WHITE,
            FONT_TABLE_CONTENT,
            FONT_TABLE_HEADER
        );
        
        tableInsurance.setRowHeight(30);
        
        // Add selection listener
        tableInsurance.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int selectedRow = tableInsurance.getSelectedRow();
                if (selectedRow >= 0) {
                    bntExportPDF.setEnabled(true);
                }
            }
        });
        
        // Add cell click listener for action buttons
        tableInsurance.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                int row = tableInsurance.rowAtPoint(e.getPoint());
                int col = tableInsurance.columnAtPoint(e.getPoint());
                
                if (row >= 0 && col >= 0) {
                    String columnName = tableInsurance.getColumnName(col);
                    if ("View Details".equals(columnName)) {
                        viewInsuranceDetails(row);
                    } else if ("Export PDF".equals(columnName)) {
                        exportInsurancePDF(row);
                    }
                }
            }
        });
        
        JScrollPane scrollPane = new JScrollPane(tableInsurance);
        scrollPane.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(PRIMARY_COLOR, 2),
            "Insurance Records",
            0, 0,
            FONT_TITLE_SMALL,
            PRIMARY_COLOR
        ));
        scrollPane.setBounds(20, 460, 1160, 200);
        
        panel.add(scrollPane);
    }
    
    private void loadExportBillsData() {
        try {
        busExportBill = new BUS_ExportBill();
        List<DTO_BillExport> exportedBills = busExportBill.getAllAvailableExportBillsForInsurance();
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            
            modelExportBills.setRowCount(0);

        if (exportedBills != null && !exportedBills.isEmpty()) {
            for (DTO_BillExport bill : exportedBills) {
                Object[] row = {
                    bill.getInvoiceNo(),
                    bill.getCustomerId(),
                        "Customer Name", // Will be loaded from database
                    bill.getTotalProduct(),
                        LocalDate.now().format(dateFormatter), // Will be loaded from database
                    "Available for Insurance",
                        "Select"
                };
                    modelExportBills.addRow(row);
                }
            }
            
        } catch (Exception e) {
            CustomDialog.showError("Failed to load export bills: " + e.getMessage());
        }
    }
    
    private void loadInsuranceData() {
        try {
        busWarranty = new BUS_Warranty();
        List<DTO_Insurance> insuranceList = busWarranty.getAllInsuranceWithExportInfo();
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            
        modelInsurance.setRowCount(0);

        if (insuranceList != null && !insuranceList.isEmpty()) {
            for (DTO_Insurance insurance : insuranceList) {
                Object[] row = {
                    insurance.getInsuranceNo(),
                        insurance.getInvoiceNo(),
                        "Customer Name", // Will be loaded from database
                    insurance.getStartDateInsurance().format(dateFormatter),
                    insurance.getEndDateInsurance().format(dateFormatter),
                    "Active",
                        "View",
                        "Export"
                };
                modelInsurance.addRow(row);
                }
            }
            
        } catch (Exception e) {
            CustomDialog.showError("Failed to load insurance data: " + e.getMessage());
        }
    }
    
    private void openWarrantyDialog(int selectedRow) {
        try {
            String invoiceNo = tableExportBills.getValueAt(selectedRow, 0).toString();
            String customerId = tableExportBills.getValueAt(selectedRow, 1).toString();
            String adminId = Dashboard_ad.adminID;
            
            // Open product warranty dialog
            ProductWarrantyDialog warrantyDialog = new ProductWarrantyDialog(
                (JFrame) SwingUtilities.getWindowAncestor(this),
                invoiceNo,
                adminId,
                customerId
            );
            warrantyDialog.setVisible(true);
            
            // After setting warranty, create insurance
            List<ProductWarrantyDialog.ProductWarrantyInfo> warrantyInfoList = warrantyDialog.getWarrantyInfoList();
            
            if (!warrantyInfoList.isEmpty()) {
                createInsuranceFromWarrantyInfo(invoiceNo, adminId, customerId, warrantyInfoList);
            }
            
        } catch (Exception e) {
            CustomDialog.showError("Failed to open warranty dialog: " + e.getMessage());
        }
    }
    
    private void createInsuranceFromWarrantyInfo(String invoiceNo, String adminId, String customerId, 
                                               List<ProductWarrantyDialog.ProductWarrantyInfo> warrantyInfoList) {
        try {
            busWarranty = new BUS_Warranty();
            
            // Generate insurance number
            String insuranceNo = String.format("INS%010d", System.currentTimeMillis() % 1_000_000_000);
            
            // Create insurance record
            DTO_Insurance insurance = new DTO_Insurance(
                insuranceNo,
                adminId,
                customerId,
                invoiceNo,
                "Insurance for products from invoice " + invoiceNo,
                java.time.LocalDate.now(),
                java.time.LocalDate.now().plusYears(1)
            );
            
            // Create insurance details for each product
            List<DTO_InsuranceDetails> detailsList = new ArrayList<>();
            for (ProductWarrantyDialog.ProductWarrantyInfo warrantyInfo : warrantyInfoList) {
                DTO_InsuranceDetails detail = new DTO_InsuranceDetails(
                    insuranceNo,
                    adminId,
                    customerId,
                    invoiceNo,
                    warrantyInfo.getProductId(),
                    warrantyInfo.getDescription(),
                    java.time.LocalDate.now(),
                    java.time.LocalTime.now()
                );
                detailsList.add(detail);
            }
            
            boolean success = busWarranty.createInsuranceWithProducts(insurance, detailsList);
            
            if (success) {
                CustomDialog.showSuccess("Insurance created successfully!\nInsurance No: " + insuranceNo);
                refreshData();
            } else {
                CustomDialog.showError("Failed to create insurance!");
            }
            
        } catch (Exception e) {
            CustomDialog.showError("Error creating insurance: " + e.getMessage());
        }
    }
    
    private void performSearch() {
        String keyword = txtSearch.getText().trim();
        
        if (keyword.isEmpty()) {
            CustomDialog.showError("Please enter search keyword!");
            return;
        }
        
        // TODO: Implement search functionality
        CustomDialog.showSuccess("Search functionality will be implemented");
    }
    
    private void refreshData() {
        loadExportBillsData();
        loadInsuranceData();
        txtSearch.setText("");
        cmbSearch.setSelectedIndex(0);
    }
    
    private void exportToExcel() {
        try {
            busWarranty = new BUS_Warranty();
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Save Excel File");
            fileChooser.setSelectedFile(new File("Insurance_Report.xlsx"));
            fileChooser.setFileFilter(new FileNameExtensionFilter("Excel Files (*.xlsx)", "xlsx"));

            int userSelection = fileChooser.showSaveDialog(null);

            if (userSelection == JFileChooser.APPROVE_OPTION) {
                File fileToSave = fileChooser.getSelectedFile();
                String filePath = fileToSave.getAbsolutePath();

                if (!filePath.endsWith(".xlsx")) {
                    filePath += ".xlsx";
                }

                boolean success = busWarranty.exportToExcel(filePath);

                if (success) {
                    CustomDialog.showSuccess("File exported successfully!");
                } else {
                    CustomDialog.showError("Failed to export file!");
                }
            }
        } catch (Exception e) {
            CustomDialog.showError("Export failed: " + e.getMessage());
        }
    }
    
    private void exportToPDF() {
        int selectedRow = tableInsurance.getSelectedRow();
        if (selectedRow < 0) {
            CustomDialog.showError("Please select an insurance record first!");
            return;
        }
        
        exportInsurancePDF(selectedRow);
    }
    
    private void viewInsuranceDetails(int row) {
        try {
            String insuranceNo = tableInsurance.getValueAt(row, 0).toString();
            
            InsuranceDetailsDialog detailsDialog = new InsuranceDetailsDialog(
                (JFrame) SwingUtilities.getWindowAncestor(this),
                insuranceNo
            );
            detailsDialog.setVisible(true);
            
        } catch (Exception e) {
            CustomDialog.showError("Failed to open insurance details: " + e.getMessage());
        }
    }
    
    private void exportInsurancePDF(int row) {
        try {
            String insuranceNo = tableInsurance.getValueAt(row, 0).toString();
            
            // Use InsurancePDFExporter to export PDF
            InsurancePDFExporter pdfExporter = new InsurancePDFExporter();
            pdfExporter.exportInsurancePDF(insuranceNo);
            
        } catch (Exception e) {
            CustomDialog.showError("PDF export failed: " + e.getMessage());
        }
    }
    
    // Button styling methods
    private void stylePrimaryButton(MyButton button) {
        button.setBackgroundColor(PRIMARY_COLOR);
        button.setHoverColor(Color.decode("#1976D2"));
        button.setPressedColor(Color.decode("#1565C0"));
        button.setForeground(Color.WHITE);
        button.setFont(FONT_CONTENT_MEDIUM);
    }
    
    private void styleInfoButton(MyButton button) {
        button.setBackgroundColor(Color.decode("#2196F3"));
        button.setHoverColor(Color.decode("#1976D2"));
        button.setPressedColor(Color.decode("#1565C0"));
        button.setForeground(Color.WHITE);
        button.setFont(FONT_CONTENT_MEDIUM);
    }
    
    private void styleDangerButton(MyButton button) {
        button.setBackgroundColor(Color.decode("#F44336"));
        button.setHoverColor(Color.decode("#D32F2F"));
        button.setPressedColor(Color.decode("#C62828"));
        button.setForeground(Color.WHITE);
        button.setFont(FONT_CONTENT_MEDIUM);
    }
}