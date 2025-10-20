
package com.Admin.insurance.GUI;

import com.Admin.dashboard_admin.GUI.Dashboard_ad;
import com.ComponentandDatabase.Components.MyButton;
import com.ComponentandDatabase.Components.MyPanel;
import com.ComponentandDatabase.Components.MyTextField;
import com.ComponentandDatabase.Components.MyCombobox;
import com.ComponentandDatabase.Components.MyTable;
import com.toedter.calendar.JDateChooser;
import com.Admin.insurance.BUS.BUS_Warranty;
import com.Admin.export.BUS.BUS_ExportBill;
import com.Admin.export.DTO.DTO_BillExport;
import com.Admin.export.DTO.DTO_BillExported;
import com.Admin.export.DTO.DTO_BillExportedDetail;
import com.Admin.insurance.DTO.DTO_Insurance;
import com.Admin.insurance.DTO.DTO_InsuranceDetails;
import com.ComponentandDatabase.Components.CustomDialog;
import javax.swing.JPanel;
import java.text.SimpleDateFormat; // Định dạng ngày tháng
import java.util.Random;
import javax.swing.JLabel;
import java.awt.Color;
import java.awt.Font;
import java.time.LocalDate;
import javax.swing.SwingConstants;
import java.awt.Dimension;
import javax.swing.*;
import java.util.Date;
import java.util.Calendar;
import java.awt.*;
import java.awt.BorderLayout;
import java.io.File;
import javax.swing.border.LineBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import java.util.List;
import java.util.ArrayList;
import java.time.format.DateTimeFormatter;
import static com.ComponentandDatabase.Components.UIConstants.*;

public class Form_Insurance extends JPanel {
    private JPanel panel, panelSearch, billBody;
    private MyPanel panelBill, panelTitle;
    private JLabel lblAdminID, lblAdminName, lblInvoice, lblDescription, lblStartDate, lblEndDate;
    private MyButton bntSearch, bntExportFile, bntDetails, bntRefresh, bntAddBill, bntExport;
    private MyTextField txtSearch, txtAdminID, txtAdminName; 
    private MyCombobox<String> cmbSearch;
    private MyTable tableExportedBills, tableInsurance, tableProducts;
    private JTextArea txtDescription;
    private JDateChooser startDate, endDate;
    private BUS_Warranty busWarranty;
    private BUS_ExportBill busExportBill;
    private DefaultTableModel modelExportedBills, modelInsurance, modelProducts;
    private String warrantyNo;
    public Form_Insurance() {
        initComponents();
        init();
       
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(1200, 700)); // Giảm kích thước cho màn hình nhỏ
        setBackground(Color.WHITE);
    }

    private void init() {
        // Tạo main panel với scroll
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(null);
        mainPanel.setPreferredSize(new Dimension(1200, 1000)); // Kích thước lớn hơn để scroll
        mainPanel.setBackground(Color.WHITE);
        
        // Tạo scroll pane
        JScrollPane scrollPane = new JScrollPane(mainPanel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setBorder(null);
        
        add(scrollPane, BorderLayout.CENTER);
        panel = mainPanel; // Gán panel để sử dụng trong các method khác

        // Title
        JLabel lblTitle = new JLabel("MANAGE INSURANCE");
        lblTitle.setFont(FONT_TITLE_LARGE);
        lblTitle.setForeground(PRIMARY_COLOR);
        lblTitle.setBounds(20, 10, 400, 40);
        panel.add(lblTitle);
        
        // Tạo panelSearch với màu nền trắng
        panelSearch = new MyPanel(Color.WHITE);
        panelSearch.setLayout(null);
        panelSearch.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(PRIMARY_COLOR, 2),
            "Insurance Management",
            0, 0,
            FONT_TITLE_SMALL,
            PRIMARY_COLOR
        ));
        panelSearch.setBounds(20, 60, 1160, 80);
        
        // Search section - Inside panelSearch
        String[] itemsSearch = {"Invoice.No", "Customer.ID", "Product.ID", "Date Exported"};
        cmbSearch = new MyCombobox<>(itemsSearch);
        cmbSearch.setBounds(20, 30, 150, 35);
        cmbSearch.setCustomFont(FONT_CONTENT_MEDIUM);
        cmbSearch.setCustomColors(Color.WHITE, Color.GRAY, Color.BLACK);
        cmbSearch.repaint();
        cmbSearch.revalidate();

        SwingUtilities.invokeLater(() -> {
            cmbSearch.repaint();
            cmbSearch.revalidate();
        });

        panelSearch.add(cmbSearch);
        
        txtSearch = new MyTextField();
        txtSearch.setHint("Search something...");
        txtSearch.setBounds(180, 30, 300, 35);
        txtSearch.setTextFont(FONT_CONTENT_MEDIUM);
        txtSearch.setHintFont(FONT_CONTENT_SMALL);
        txtSearch.setBackgroundColor(Color.decode("#F5FFFA"));
        panelSearch.add(txtSearch);
       
        bntSearch = new MyButton("Search", 20);
        stylePrimaryButton(bntSearch);
        bntSearch.setButtonIcon("src\\main\\resources\\Icons\\Admin_icon\\search.png", 25, 25, 5, SwingConstants.RIGHT, SwingConstants.CENTER);     
        bntSearch.setBounds(490, 30, 120, 35);
        bntSearch.addActionListener(e -> {
           // TODO: Implement search functionality
           busExportBill = new BUS_ExportBill();
           List<DTO_BillExport> searchResults = new ArrayList<>(); // Placeholder

           // Gọi phương thức hiển thị kết quả lên table
           displaySearchResults(searchResults);
       });
        panelSearch.add(bntSearch);
        
        bntRefresh = new MyButton("Refresh", 20);
        styleInfoButton(bntRefresh);
        bntRefresh.setBounds(620, 30, 120, 35);
        bntRefresh.setButtonIcon("src\\main\\resources\\Icons\\Admin_icon\\refresh.png", 25, 25, 10, SwingConstants.RIGHT, SwingConstants.CENTER);
        bntRefresh.addActionListener((e) -> {
            Refresh();
        });
        panelSearch.add(bntRefresh);
        
        bntDetails = new MyButton("Details", 20);
        styleInfoButton(bntDetails);
        bntDetails.setButtonIcon("src\\main\\resources\\Icons\\Admin_icon\\bill_export.png", 25, 25, 5, SwingConstants.RIGHT, SwingConstants.CENTER);    
        bntDetails.setBounds(750, 30, 120, 35);
        bntDetails.addActionListener((e) -> {
            WarrantyDetails details= new WarrantyDetails();
            details.setVisible(true);
        });
        panelSearch.add(bntDetails);
        
        bntExportFile = new MyButton("Export", 20);
        bntExportFile.setBackgroundColor(Color.WHITE);
        bntExportFile.setPressedColor(Color.decode("#D3D3D3"));
        bntExportFile.setHoverColor(Color.decode("#EEEEEE"));
        bntExportFile.setForeground(Color.BLACK);
        bntExportFile.setFont(FONT_BUTTON_MEDIUM);
        bntExportFile.setBounds(880, 30, 120, 35);
        bntExportFile.setButtonIcon("src\\main\\resources\\Icons\\Admin_icon\\Excel.png", 30, 30, 10, SwingConstants.RIGHT, SwingConstants.CENTER);
        bntExportFile.addActionListener(e -> {
            busWarranty= new BUS_Warranty();
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Save Excel File");
            fileChooser.setSelectedFile(new File("Warranty_Report.xlsx"));
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
                   CustomDialog.showSuccess("File exported successfully !");
                }
            }
        });
        
        panelSearch.add(bntExportFile);
        
        panel.add(panelSearch);
       
        // Admin Info section moved to insurance form area
        
        // Create table for exported bills available for insurance
        String[] columnNamesExported = {
            "Invoice.No", "Customer.ID", "Customer Name", "Total Products", 
            "Date Exported", "Insurance Status", "Action"
        };
        
        modelExportedBills = new DefaultTableModel(columnNamesExported, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 6; // Only Action column is editable
            }
        };
        tableExportedBills = createStyledTable(modelExportedBills);
        
        JScrollPane scrollPaneExported = MyTable.createScrollPane(tableExportedBills, 20, 160, 1490, 200);
        scrollPaneExported.getVerticalScrollBar().setPreferredSize(new Dimension(15, Integer.MAX_VALUE));
        scrollPaneExported.getHorizontalScrollBar().setPreferredSize(new Dimension(Integer.MAX_VALUE, 15));
        
        panel.add(scrollPaneExported);
        
        // Create table for insurance records
        String[] columnNamesInsurance = {
            "Insurance.No", "Invoice.No", "Customer Name", "Start Date", 
            "End Date", "Status", "View Details"
        };
        
        modelInsurance = new DefaultTableModel(columnNamesInsurance, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 6; // Only View Details column is editable
            }
        };
        tableInsurance = createStyledTable(modelInsurance);
        
        JScrollPane scrollPaneInsurance = MyTable.createScrollPane(tableInsurance, 20, 380, 1490, 200);
        scrollPaneInsurance.getVerticalScrollBar().setPreferredSize(new Dimension(15, Integer.MAX_VALUE));
        scrollPaneInsurance.getHorizontalScrollBar().setPreferredSize(new Dimension(Integer.MAX_VALUE, 15));
        
        panel.add(scrollPaneInsurance);
        
        // Create table for product details in selected invoice
        String[] columnNamesProducts = {
            "Product.ID", "Product Name", "Quantity", "Unit Price", "Warranty Period (months)", "Action"
        };
        
        modelProducts = new DefaultTableModel(columnNamesProducts, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 4 || column == 5; // Warranty Period and Action columns are editable
            }
        };
        tableProducts = createStyledTable(modelProducts);
        
        JScrollPane scrollPaneProducts = MyTable.createScrollPane(tableProducts, 20, 600, 1490, 150);
        scrollPaneProducts.getVerticalScrollBar().setPreferredSize(new Dimension(15, Integer.MAX_VALUE));
        scrollPaneProducts.getHorizontalScrollBar().setPreferredSize(new Dimension(Integer.MAX_VALUE, 15));
        
        panel.add(scrollPaneProducts);
        
        // Add selection listener for exported bills table
        tableExportedBills.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int selectedRow = tableExportedBills.getSelectedRow();
                if (selectedRow >= 0) {
                    String invoiceNo = tableExportedBills.getValueAt(selectedRow, 0).toString();
                    String customerID = tableExportedBills.getValueAt(selectedRow, 1).toString();
                    String insuranceStatus = tableExportedBills.getValueAt(selectedRow, 5).toString();
                    
                    if ("Available for Insurance".equals(insuranceStatus)) {
                        loadInvoiceDetailsForInsurance(invoiceNo, customerID);
                    } else {
                        CustomDialog.showError("This invoice has already been insured!");
                    }
                }
            }
        });
        
        // Load data
        SwingUtilities.invokeLater(() -> {
            loadExportedBillsToTable();
            loadInsuranceToTable();
            tableExportedBills.adjustColumnWidths();
            tableInsurance.adjustColumnWidths();
            
            // Initially disable insurance form until an invoice is selected
            enableInsuranceForm(false);
        });
        
        // Insurance Form Section - Below the product details table
        JLabel lblInsuranceForm = new JLabel("INSURANCE FORM");
        lblInsuranceForm.setFont(FONT_TITLE_MEDIUM);
        lblInsuranceForm.setForeground(PRIMARY_COLOR);
        lblInsuranceForm.setBounds(20, 770, 300, 30);
        panel.add(lblInsuranceForm);
        
        lblDescription= new JLabel("Description");
        lblDescription.setFont(FONT_CONTENT_MEDIUM);
        lblDescription.setForeground(TEXT_PRIMARY);
        lblDescription.setBounds(20, 810, 130, 35);
        panel.add(lblDescription);
        
        // Admin Info section - To the right of Description
         lblAdminID= new JLabel("Admin.ID");
         lblAdminID.setFont(FONT_CONTENT_MEDIUM);
         lblAdminID.setForeground(TEXT_PRIMARY);
        lblAdminID.setBounds(530, 800, 100, 25);
        panel.add(lblAdminID);
       
          txtAdminID = new MyTextField();
          txtAdminID.setBorder(BorderFactory.createLineBorder(Color.GRAY));
          txtAdminID.setTextColor(Color.RED);
          txtAdminID.setLocked(true);
          txtAdminID.setTextFont(FONT_CONTENT_MEDIUM);
          txtAdminID.setBackgroundColor(Color.WHITE);
        txtAdminID.setBounds(530, 825, 120, 35);
          txtAdminID.setText(Dashboard_ad.adminID);
        panel.add(txtAdminID);
          
         lblAdminName= new JLabel("Admin Name");
         lblAdminName.setFont(FONT_CONTENT_MEDIUM);
         lblAdminName.setForeground(TEXT_PRIMARY);
        lblAdminName.setBounds(660, 800, 100, 25);
        panel.add(lblAdminName);
         
         txtAdminName = new MyTextField();
         txtAdminName.setBorder(BorderFactory.createLineBorder(Color.GRAY));
         txtAdminName.setTextColor(Color.BLUE);
         txtAdminName.setLocked(true);
         txtAdminName.setTextFont(FONT_CONTENT_MEDIUM);
         txtAdminName.setBackgroundColor(Color.WHITE);
        txtAdminName.setBounds(660, 825, 120, 35);
         txtAdminName.setText(Dashboard_ad.getAdminName(txtAdminID.getText().strip()));
        panel.add(txtAdminName);
          
        lblStartDate= new JLabel("Start Date");
        lblStartDate.setFont(FONT_CONTENT_MEDIUM);
        lblStartDate.setForeground(TEXT_PRIMARY);
        lblStartDate.setBounds(20, 870, 130, 35);
        panel.add(lblStartDate);
          
        lblEndDate= new JLabel("End Date");
        lblEndDate.setFont(FONT_CONTENT_MEDIUM);
        lblEndDate.setForeground(TEXT_PRIMARY);
        lblEndDate.setBounds(20, 930, 130, 35);
        panel.add(lblEndDate);
        
        // Remove unused product fields - we only need description and dates for insurance
                
        
        // Bill preview panel removed - PDF will be generated directly from data
       
        // IMEI UI removed
       
        txtDescription = new JTextArea();
        txtDescription.setFont(FONT_CONTENT_MEDIUM);
        txtDescription.setLineWrap(true);
        txtDescription.setWrapStyleWord(true);
        txtDescription.setBackground(Color.WHITE);
        txtDescription.setBorder(new LineBorder(Color.GRAY));

        JScrollPane scrollDescription= new JScrollPane(txtDescription);
        scrollDescription.setBorder(new LineBorder(Color.GRAY));
        scrollDescription.setBounds(150, 810, 360, 50);
        panel.add(scrollDescription);
       
        startDate = new JDateChooser();
        startDate.setFont(FONT_CONTENT_MEDIUM);
        startDate.setDateFormatString("dd/MM/yyyy");
        startDate.setBounds(150, 870, 160, 35);
        startDate.setBackground(Color.WHITE);
        panel.add(startDate);
        
        endDate = new JDateChooser();
        endDate.setFont(FONT_CONTENT_MEDIUM);
        endDate.setDateFormatString("dd/MM/yyyy");
        endDate.setBounds(150, 930, 160, 35);
        endDate.setBackground(Color.WHITE);
        panel.add(endDate);
       
          
        bntAddBill = new MyButton("Add Bill", 20);
        stylePrimaryButton(bntAddBill);
        bntAddBill.setBounds(20, 990, 110, 35);
        bntAddBill.addActionListener((e) -> {
            if (!validateFields()) {
                return; // Nếu dữ liệu không hợp lệ, dừng lại
            }
            // Logic for adding bill preview
        });

        panel.add(bntAddBill);
        
        bntExport = new MyButton("Save Bill", 20);
        stylePrimaryButton(bntExport);
        bntExport.setFont(FONT_BUTTON_LARGE);
        bntExport.setBounds(950, 970, 200, 50);
        bntExport.addActionListener((e) -> {
            // Kiểm tra các trường nhập liệu trước khi xuất
            if (!validateFields()) {
                return; // Dừng thực thi nếu có trường không hợp lệ
            }
            boolean confirm = CustomDialog.showOptionPane(
                "Confirm Exportation",
                "Are you sure want to export bill?",
                UIManager.getIcon("OptionPane.questionIcon"),
                Color.decode("#FF6666")
            );

          if(confirm){
             try {
                // Khởi tạo BUS để xử lý nghiệp vụ
                busWarranty = new BUS_Warranty();

                String adminID = txtAdminID.getText().strip();
                String adminName = txtAdminName.getText().strip();
                Date startDateValue = startDate.getDate();
                Date endDateValue = endDate.getDate();
                String description = txtDescription.getText();

                // Kiểm tra ngày hợp lệ
                if (endDateValue.before(startDateValue)) {
                    CustomDialog.showError("End date must be after start date!");
                    return;
                }

                // Get selected invoice number from table
                String selectedInvoiceNo = getSelectedInvoiceNo();
                if (selectedInvoiceNo == null || selectedInvoiceNo.isEmpty()) {
                    CustomDialog.showError("Please select an export bill first!");
                    return;
                }

                // Get customer ID from selected invoice
                String selectedCustomerID = getSelectedCustomerID();
                
                // Generate insurance number
                warrantyNo = String.format("INS%010d", System.currentTimeMillis() % 1_000_000_000);

                // Tạo DTO cho hóa đơn bảo hành với liên kết đến hóa đơn bán
                DTO_Insurance insurance = new DTO_Insurance(
                    warrantyNo,  // Sử dụng biến toàn cục thay vì tạo mới
                    adminID,
                    selectedCustomerID, // Customer ID from selected invoice
                    selectedInvoiceNo, // Liên kết với hóa đơn bán
                    description, // Thêm describleCustomer
                    startDateValue.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate(),
                    endDateValue.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate()
                );

                // Tạo DTO cho chi tiết hóa đơn bảo hành
                DTO_InsuranceDetails insuranceDetails = new DTO_InsuranceDetails(
                    warrantyNo, // Sử dụng biến toàn cục thay vì tạo mới
                    adminID,
                    selectedCustomerID, // Customer ID from selected invoice
                    selectedInvoiceNo, // Liên kết với hóa đơn bán
                    "ALL_PRODUCTS", // All products from the invoice
                    description,
                    LocalDate.now(), 
                    java.time.LocalTime.now()
                );

                // Thêm hóa đơn bảo hành vào database
                boolean warrantyInserted = busWarranty.insertBillWarranty(insurance);
                boolean detailsInserted = busWarranty.insertBillWarrantyDetails(insuranceDetails);

                if (warrantyInserted && detailsInserted) {
                    CustomDialog.showSuccess("Insurance created successfully and saved in database!\n" +
                                           "Insurance No: " + warrantyNo + "\n" +
                                           "Linked to Invoice: " + selectedInvoiceNo);
                    
                    // Refresh tables to show updated data
                    Refresh();

                // Xuất PDF
                PDF_Insurance pdfExporter = new PDF_Insurance(
                        null,
                    adminID, 
                    adminName, 
                        null, 
                        null,
                    startDateValue, 
                    endDateValue, 
                    description
                );
                pdfExporter.exportToPDF();

                } else {
                    CustomDialog.showError("Failed to save insurance invoice!");
                    return;
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                CustomDialog.showError("Error while exporting warranty invoice: " + ex.getMessage());
            }
         }
          
        });

        panel.add(bntExport);  
   }
    
    public void Refresh(){
        SwingUtilities.invokeLater(() -> {
            loadExportedBillsToTable();
            loadInsuranceToTable();
            modelProducts.setRowCount(0); // Clear product details table
            tableExportedBills.adjustColumnWidths();
            tableInsurance.adjustColumnWidths();
            tableProducts.adjustColumnWidths();
        });
        cmbSearch.setSelectedIndex(0);
        txtSearch.setText("");
        txtDescription.setText("");
        
        // Clear and reset form
        // Product fields removed - only description and dates remain
        
        // Bill preview removed - no need to clear
    
        // Disable form until new invoice is selected
        enableInsuranceForm(false);
    }
    
    private void loadExportedBillsToTable() {
        busExportBill = new BUS_ExportBill();
        List<DTO_BillExport> exportedBills = busExportBill.getAllAvailableExportBillsForInsurance();
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        modelExportedBills.setRowCount(0);

        if (exportedBills != null && !exportedBills.isEmpty()) {
            for (DTO_BillExport bill : exportedBills) {
                Object[] row = {
                    bill.getInvoiceNo(),
                    bill.getCustomerId(),
                    "Customer Name", // Will be filled with actual customer name from database
                    bill.getTotalProduct(),
                    LocalDate.now().format(dateFormatter), // Will be filled with actual date
                    "Available for Insurance",
                    "Select" // Action button
                };
                modelExportedBills.addRow(row);
            }
        }
    }
    
    private void loadInsuranceToTable() {
        busWarranty = new BUS_Warranty();
        List<DTO_Insurance> insuranceList = busWarranty.getAllInsuranceWithExportInfo();
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        modelInsurance.setRowCount(0);

        if (insuranceList != null && !insuranceList.isEmpty()) {
            for (DTO_Insurance insurance : insuranceList) {
                Object[] row = {
                    insurance.getInsuranceNo(),
                    insurance.getInvoiceNo(), // Show linked invoice number
                    "Customer Name", // Will be filled with actual customer name from database
                    insurance.getStartDateInsurance().format(dateFormatter),
                    insurance.getEndDateInsurance().format(dateFormatter),
                    "Active",
                    "View" // Action button
                };
                modelInsurance.addRow(row);
            }
        }
    }
    
    private void displaySearchResults(List<DTO_BillExport> results) {
        modelExportedBills.setRowCount(0);
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        for (DTO_BillExport bill : results) {
            Object[] rowData = {
                bill.getInvoiceNo(),
                bill.getCustomerId(),
                "Customer Name", // Will be filled with actual customer name
                bill.getTotalProduct(),
                LocalDate.now().format(dateFormatter),
                "Available for Insurance",
                "Select" // Action button
            };
            modelExportedBills.addRow(rowData);
        }
    }
    
    private void loadInvoiceDetailsForInsurance(String invoiceNo, String customerID) {
        try {
            // Get export bill details from database
            DTO_BillExported billDetails = busExportBill.getExportBillDetailsForInsurance(invoiceNo, txtAdminID.getText().strip());
            
            if (billDetails != null) {
                // Show success message with bill details
                CustomDialog.showSuccess("Selected Invoice: " + invoiceNo + 
                                       "\nCustomer: " + customerID + 
                                       "\nTotal Products: " + billDetails.getTotalProduct() +
                                       "\n\nPlease fill in the insurance details below.");
                
                // Clear previous data
                txtDescription.setText("");
                
                // Set default dates (start date = today, end date = 1 year from now)
                startDate.setDate(new Date());
                Calendar cal = Calendar.getInstance();
                cal.add(Calendar.YEAR, 1);
                endDate.setDate(cal.getTime());
                
                // Load product details from selected invoice
                loadProductDetailsFromInvoice(invoiceNo, txtAdminID.getText().strip());
                
                // Enable the form for input
                enableInsuranceForm(true);
            } else {
                CustomDialog.showError("Could not load invoice details for: " + invoiceNo);
            }
            
        } catch (Exception e) {
            CustomDialog.showError("Error loading invoice details: " + e.getMessage());
        }
    }
    
    private void enableInsuranceForm(boolean enabled) {
        txtDescription.setEnabled(enabled);
        startDate.setEnabled(enabled);
        endDate.setEnabled(enabled);
        bntAddBill.setEnabled(enabled);
        bntExport.setEnabled(enabled);
    }
    
    private void loadProductDetailsFromInvoice(String invoiceNo, String adminID) {
        try {
            // Clear existing product details
            modelProducts.setRowCount(0);
            
            // Get product details from export bill details
            List<DTO_BillExportedDetail> productDetails = busExportBill.getAllBillDetails();
            
            if (productDetails != null) {
                for (DTO_BillExportedDetail detail : productDetails) {
                    if (detail.getInvoiceNo().equals(invoiceNo) && detail.getAdminId().equals(adminID)) {
                        Object[] row = {
                            detail.getProductId(),
                            "Product Name", // Will be loaded from Product table
                            detail.getQuantity(),
                            detail.getUnitPrice(),
                            12, // Default warranty period in months
                            "Set Warranty" // Action button
                        };
                        modelProducts.addRow(row);
                    }
                }
            }
            
            // Adjust column widths
            tableProducts.adjustColumnWidths();
            
        } catch (Exception e) {
            CustomDialog.showError("Error loading product details: " + e.getMessage());
        }
    }
    
    private void createInsuranceBillPreview(String invoiceNo, String customerID) {
        try {
            billBody = getBillBody();
            if (billBody == null) {
                CustomDialog.showError("Bill display area not found!");
                return;
            }

            // Clear existing components
            billBody.removeAll();
            billBody.setLayout(new BoxLayout(billBody, BoxLayout.Y_AXIS));
            billBody.setBackground(Color.WHITE);

            // Generate insurance number
            warrantyNo = String.format("INS%010d", new Random().nextInt(1_000_000_000));

            // ===== 0. Insurance Invoice No =====
            lblInvoice = new JLabel("INSURANCE INVOICE No: " + warrantyNo, SwingConstants.CENTER);
            lblInvoice.setFont(new Font("Arial", Font.BOLD, 16));
            lblInvoice.setAlignmentX(Component.CENTER_ALIGNMENT);
            billBody.add(lblInvoice);
            addVerticalSpace(15);

            // ===== 1. Admin Information =====
            String adminID = txtAdminID.getText().strip();
            String adminName = txtAdminName.getText().strip();

            JPanel adminPanel = createSectionPanel("ADMIN INFORMATION");
            addInfoRow(adminPanel, "Admin ID:", adminID);
            addInfoRow(adminPanel, "Admin Name:", adminName);
            billBody.add(adminPanel);
            addSeparatorWithSpace();

            // ===== 2. Export Bill Information =====
            JPanel exportPanel = createSectionPanel("EXPORT BILL INFORMATION");
            addInfoRow(exportPanel, "Invoice No:", invoiceNo);
            addInfoRow(exportPanel, "Customer ID:", customerID);
            billBody.add(exportPanel);
            addSeparatorWithSpace();

            // ===== 3. Insurance Details =====
            JPanel insurancePanel = createSectionPanel("INSURANCE DETAILS");
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");

            String startDateStr = (startDate != null && startDate.getDate() != null) ? 
                                 dateFormat.format(startDate.getDate()) : "N/A";
            addInfoRow(insurancePanel, "Start Date:", startDateStr);

            String endDateStr = (endDate != null && endDate.getDate() != null) ? 
                               dateFormat.format(endDate.getDate()) : "N/A";
            addInfoRow(insurancePanel, "End Date:", endDateStr);

            String description = txtDescription != null ? txtDescription.getText() : "";
            addInfoRow(insurancePanel, "Issue Description:", description);

            billBody.add(insurancePanel);
            addSeparatorWithSpace();

            // ===== 4. Terms & Conditions =====
            JPanel termsPanel = createSectionPanel("TERMS & CONDITIONS");
            JTextArea termsArea = new JTextArea(
                "1. Warranty covers manufacturing defects only.\n" +
                "2. Warranty does not cover physical damage or liquid damage.\n" +
                "3. ID card must be presented for warranty claims.\n" +
                "4. Warranty is non-transferable.\n" +
                "5. Warranty period starts from the date of purchase."
            );
            termsArea.setEditable(false);
            termsArea.setFont(new Font("Arial", Font.PLAIN, 12));
            termsArea.setBackground(Color.WHITE);
            termsArea.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

            termsPanel.add(termsArea);
            billBody.add(termsPanel);

            // Refresh UI
            billBody.revalidate();
            billBody.repaint();

        } catch (Exception e) {
            e.printStackTrace();
            CustomDialog.showError("Error creating insurance bill preview: " + e.getMessage());
        }
    }
    
    private String getSelectedInvoiceNo() {
        int selectedRow = tableExportedBills.getSelectedRow();
        if (selectedRow >= 0) {
            return tableExportedBills.getValueAt(selectedRow, 0).toString();
        }
        return null;
    }
    
    private String getSelectedCustomerID() {
        int selectedRow = tableExportedBills.getSelectedRow();
        if (selectedRow >= 0) {
            return tableExportedBills.getValueAt(selectedRow, 1).toString();
        }
        return null;
    }
    
    
    private JLabel createSeparator() {
           JLabel separator = new JLabel("===================================================");
           separator.setForeground(Color.GRAY);
           separator.setAlignmentX(Component.CENTER_ALIGNMENT);
           return separator;
       }

    // Hàm thêm dòng thông tin
    private void addInfoRow(JPanel panel, String label, String value) {
        JPanel rowPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        rowPanel.setBackground(Color.WHITE);
        
        JLabel lblLabel = new JLabel(label);
        lblLabel.setFont(new Font("Arial", Font.BOLD, 12));
        
        JLabel lblValue = new JLabel(value);
        lblValue.setFont(new Font("Arial", Font.PLAIN, 12));
        
        rowPanel.add(lblLabel);
        rowPanel.add(lblValue);
        panel.add(rowPanel);
    }


    // Helper methods để tránh lặp code và đảm bảo thêm component đúng cách
    private void addVerticalSpace(int height) {
        billBody.add(Box.createVerticalStrut(height));
    }

    private void addSeparatorWithSpace() {
        billBody.add(createSeparator());
        addVerticalSpace(10);
    }
  
    private JPanel createSectionPanel(String title) {
        JPanel panelcreate = new JPanel();
        panelcreate.setLayout(new BoxLayout(panelcreate, BoxLayout.Y_AXIS));
        panelcreate.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panelcreate.setBackground(Color.WHITE);

        JLabel titleLabel = new JLabel(title, SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 14));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JSeparator separator = new JSeparator();
        separator.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));

        panelcreate.add(titleLabel);
        panelcreate.add(Box.createVerticalStrut(5));
        panelcreate.add(separator);
        panelcreate.add(Box.createVerticalStrut(5));

        return panelcreate;
    }
    
     private JPanel getBillBody() {
            // Kiểm tra cấu trúc panel theo đúng cách bạn đã thiết kế
            if (panelBill.getComponentCount() > 0) {
                // Lấy component CENTER (index 1 nếu có cả NORTH và CENTER)
                if (panelBill.getComponentCount() > 1) {
                    Component centerComp = panelBill.getComponent(1);
                    if (centerComp instanceof JPanel) {
                        JPanel billContent = (JPanel) centerComp;
                        if (billContent.getComponentCount() > 0) {
                            Component scrollComp = billContent.getComponent(0);
                            if (scrollComp instanceof JScrollPane) {
                                JScrollPane scrollPane = (JScrollPane) scrollComp;
                                return (JPanel) scrollPane.getViewport().getView();
                            }
                        }
                    }
                }
            }

            // Fallback: tạo mới nếu không tìm thấy (đảm bảo không bao giờ null)
            JPanel newBody = new JPanel();
            newBody.setLayout(new BoxLayout(newBody, BoxLayout.Y_AXIS));
            newBody.setBackground(Color.WHITE);

            // Tạo lại cấu trúc scroll pane nếu cần
            JScrollPane scrollPane = new JScrollPane(newBody);
            scrollPane.setBorder(null);

            // Tạo lại cấu trúc billContent
            JPanel billContent = new JPanel(new BorderLayout());
            billContent.add(scrollPane, BorderLayout.CENTER);

            // Cập nhật lại panelBill
            panelBill.removeAll();
            panelBill.add(panelTitle, BorderLayout.NORTH);
            panelBill.add(billContent, BorderLayout.CENTER);
            panelBill.revalidate();

            return newBody;
         }
    
     private boolean validateFields() {
        if (txtDescription.getText().strip().isEmpty()) {
            CustomDialog.showError("Please enter a warranty description!");
            return false;
        }
        if (startDate.getDate() == null) {
            CustomDialog.showError("Please select a start date for the warranty!");
            return false;
        }
        if (endDate.getDate() == null) {
            CustomDialog.showError("Please select an end date for the warranty!");
            return false;
        }
         return true; // Trả về `true` nếu tất cả các trường hợp hợp lệ
     }
   
     // ============================================
     // HELPER METHODS FOR UI STYLING
     // ============================================

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
}
