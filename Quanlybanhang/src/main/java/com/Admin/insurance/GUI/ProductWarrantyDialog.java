package com.Admin.insurance.GUI;

import com.ComponentandDatabase.Components.MyButton;
import com.ComponentandDatabase.Components.MyTable;
import com.ComponentandDatabase.Components.CustomDialog;
import com.Admin.export.BUS.BUS_ExportBill;
import com.Admin.export.DTO.DTO_BillExportedDetail;
import static com.ComponentandDatabase.Components.UIConstants.*;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Date;
import com.toedter.calendar.JDateChooser;

public class ProductWarrantyDialog extends JDialog {
    private String invoiceNo;
    private String adminId;
    private String customerId;
    private List<ProductWarrantyInfo> warrantyInfoList;
    
    private JPanel mainPanel;
    private MyTable tableProducts;
    private DefaultTableModel modelProducts;
    private MyButton btnSave, btnCancel;
    
    public ProductWarrantyDialog(JFrame parent, String invoiceNo, String adminId, String customerId) {
        super(parent, "Set Product Warranty", true);
        this.invoiceNo = invoiceNo;
        this.adminId = adminId;
        this.customerId = customerId;
        this.warrantyInfoList = new ArrayList<>();
        
        initComponents();
        loadProductData();
    }
    
    private void initComponents() {
        setSize(800, 600);
        setLocationRelativeTo(getParent());
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        
        mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(Color.WHITE);
        add(mainPanel);
        
        // Title
        JLabel lblTitle = new JLabel("Set Warranty Period for Products");
        lblTitle.setFont(FONT_TITLE_MEDIUM);
        lblTitle.setForeground(PRIMARY_COLOR);
        lblTitle.setHorizontalAlignment(SwingConstants.CENTER);
        lblTitle.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        mainPanel.add(lblTitle, BorderLayout.NORTH);
        
        // Product table
        createProductTable();
        
        // Button panel
        createButtonPanel();
    }
    
    private void createProductTable() {
        String[] columnNames = {
            "Product ID", "Product Name", "Quantity", "Unit Price", 
            "Warranty Period (months)", "Start Date", "End Date", "Description"
        };
        
        modelProducts = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column >= 4; // Only warranty period, dates and description are editable
            }
            
            @Override
            public Class<?> getColumnClass(int column) {
                if (column == 2 || column == 4) return Integer.class; // Quantity and Warranty Period
                if (column == 3) return Double.class; // Unit Price
                return String.class;
            }
        };
        
        tableProducts = new MyTable(
            modelProducts,
            Color.WHITE,
            TEXT_PRIMARY,
            Color.decode("#E8F5E9"),
            Color.BLACK,
            PRIMARY_COLOR,
            Color.WHITE,
            FONT_TABLE_CONTENT,
            FONT_TABLE_HEADER
        );
        
        tableProducts.setRowHeight(30);
        
        // Add cell editor for warranty period
        JSpinner spinner = new JSpinner(new SpinnerNumberModel(12, 1, 60, 1));
        tableProducts.getColumnModel().getColumn(4).setCellEditor(new SpinnerEditor(spinner));
        
        // Add cell editor for start date
        tableProducts.getColumnModel().getColumn(5).setCellEditor(new DateFieldEditor());
        
        // Add cell editor for end date
        tableProducts.getColumnModel().getColumn(6).setCellEditor(new DateFieldEditor());
        
        // Add cell editor for description
        JTextField descriptionField = new JTextField();
        tableProducts.getColumnModel().getColumn(7).setCellEditor(new DefaultCellEditor(descriptionField));
        
        JScrollPane scrollPane = new JScrollPane(tableProducts);
        scrollPane.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(PRIMARY_COLOR, 2),
            "Products from Invoice: " + invoiceNo,
            0, 0,
            FONT_TITLE_SMALL,
            PRIMARY_COLOR
        ));
        
        mainPanel.add(scrollPane, BorderLayout.CENTER);
    }
    
    private void createButtonPanel() {
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        
        btnSave = new MyButton("Save Warranty", 20);
        stylePrimaryButton(btnSave);
        btnSave.addActionListener(e -> saveWarrantyInfo());
        buttonPanel.add(btnSave);
        
        btnCancel = new MyButton("Cancel", 20);
        styleDangerButton(btnCancel);
        btnCancel.addActionListener(e -> dispose());
        buttonPanel.add(btnCancel);
        
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
    }
    
    private void loadProductData() {
        try {
            BUS_ExportBill busExportBill = new BUS_ExportBill();
            List<DTO_BillExportedDetail> productDetails = busExportBill.getAllBillDetails();
            
            modelProducts.setRowCount(0);
            
            if (productDetails != null && !productDetails.isEmpty()) {
                for (DTO_BillExportedDetail detail : productDetails) {
                    if (detail.getInvoiceNo().equals(invoiceNo) && detail.getAdminId().equals(adminId)) {
                        Object[] row = {
                            detail.getProductId(),
                            "Product Name", // Will be loaded from Product table
                            detail.getQuantity(),
                            detail.getUnitPrice(),
                            12, // Default warranty period
                            LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                            LocalDate.now().plusMonths(12).format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                            "Standard warranty coverage"
                        };
                        modelProducts.addRow(row);
                    }
                }
            }
            
            tableProducts.adjustColumnWidths();
            
        } catch (Exception e) {
            CustomDialog.showError("Failed to load product data: " + e.getMessage());
        }
    }
    
    private void saveWarrantyInfo() {
        try {
            warrantyInfoList.clear();
            
            for (int i = 0; i < modelProducts.getRowCount(); i++) {
                String productId = modelProducts.getValueAt(i, 0).toString();
                String productName = modelProducts.getValueAt(i, 1).toString();
                int quantity = (Integer) modelProducts.getValueAt(i, 2);
                double unitPrice = (Double) modelProducts.getValueAt(i, 3);
                int warrantyPeriod = (Integer) modelProducts.getValueAt(i, 4);
                String startDateStr = modelProducts.getValueAt(i, 5).toString();
                String endDateStr = modelProducts.getValueAt(i, 6).toString();
                String description = modelProducts.getValueAt(i, 7).toString();
                
                // Validate warranty period
                if (warrantyPeriod < 1 || warrantyPeriod > 60) {
                    CustomDialog.showError("Warranty period must be between 1 and 60 months for product: " + productId);
                    return;
                }
                
                // Validate dates
                if (startDateStr.isEmpty() || endDateStr.isEmpty()) {
                    CustomDialog.showError("Please set start and end dates for product: " + productId);
                    return;
                }
        
                ProductWarrantyInfo warrantyInfo = new ProductWarrantyInfo(
                    productId,
                    productName,
                    quantity,
                    unitPrice,
                    warrantyPeriod,
                    startDateStr,
                    endDateStr,
                    description
                );
                
                warrantyInfoList.add(warrantyInfo);
            }
            
            if (warrantyInfoList.isEmpty()) {
                CustomDialog.showError("No products to set warranty for!");
                return;
            }
        
            CustomDialog.showSuccess("Warranty information saved successfully!\n" +
                                   "Products: " + warrantyInfoList.size() + "\n" +
                                   "Invoice: " + invoiceNo);
            
            dispose();
            
        } catch (Exception e) {
            CustomDialog.showError("Failed to save warranty information: " + e.getMessage());
        }
    }
    
    public List<ProductWarrantyInfo> getWarrantyInfoList() {
        return warrantyInfoList;
    }
    
    // Button styling methods
    private void stylePrimaryButton(MyButton button) {
        button.setBackgroundColor(PRIMARY_COLOR);
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
    
    // Inner class for warranty information
    public static class ProductWarrantyInfo {
        private String productId;
        private String productName;
        private int quantity;
        private double unitPrice;
        private int warrantyPeriod;
        private String startDate;
        private String endDate;
        private String description;
        
        public ProductWarrantyInfo(String productId, String productName, int quantity, 
                                 double unitPrice, int warrantyPeriod, String startDate, 
                                 String endDate, String description) {
            this.productId = productId;
            this.productName = productName;
            this.quantity = quantity;
            this.unitPrice = unitPrice;
            this.warrantyPeriod = warrantyPeriod;
            this.startDate = startDate;
            this.endDate = endDate;
            this.description = description;
        }
        
        // Getters
        public String getProductId() { return productId; }
        public String getProductName() { return productName; }
        public int getQuantity() { return quantity; }
        public double getUnitPrice() { return unitPrice; }
        public int getWarrantyPeriod() { return warrantyPeriod; }
        public String getStartDate() { return startDate; }
        public String getEndDate() { return endDate; }
        public String getDescription() { return description; }
    }
    
    // Custom cell editors
    private static class SpinnerEditor extends DefaultCellEditor {
        public SpinnerEditor(JSpinner spinner) {
            super(new JTextField());
            this.spinner = spinner;
        }
        
        private JSpinner spinner;
        
        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            spinner.setValue(value);
            return spinner;
        }
        
        @Override
        public Object getCellEditorValue() {
            return spinner.getValue();
        }
    }
    
    private static class DateFieldEditor extends DefaultCellEditor {
        private JTextField textField;
        private JDateChooser dateChooser;
        private JDialog dateDialog;
        
        public DateFieldEditor() {
            super(new JTextField());
            this.textField = (JTextField) getComponent();
            this.textField.setEditable(false);
            this.textField.setPreferredSize(new Dimension(120, 25));
            this.textField.setBorder(BorderFactory.createLineBorder(Color.GRAY));
            
            // Create date chooser dialog
            dateChooser = new JDateChooser();
            dateChooser.setDateFormatString("dd/MM/yyyy");
            dateChooser.setPreferredSize(new Dimension(200, 30));
            
            // Add click listener to open date picker
            textField.addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseClicked(java.awt.event.MouseEvent e) {
                    openDatePicker();
                }
            });
        }
        
        private void openDatePicker() {
            // Create dialog for date picker
            dateDialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(textField), "Select Date", true);
            dateDialog.setSize(300, 150);
            dateDialog.setLocationRelativeTo(textField);
            
            JPanel panel = new JPanel(new BorderLayout());
            panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
            
            // Set current date if text field has value
            if (!textField.getText().isEmpty()) {
                try {
                    java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy");
                    Date currentDate = sdf.parse(textField.getText());
                    dateChooser.setDate(currentDate);
                } catch (Exception e) {
                    dateChooser.setDate(new Date());
                }
            } else {
                dateChooser.setDate(new Date());
            }
            
            panel.add(dateChooser, BorderLayout.CENTER);
            
            // Add buttons
            JPanel buttonPanel = new JPanel(new FlowLayout());
            MyButton btnOK = new MyButton("OK", 20);
            btnOK.setBackgroundColor(PRIMARY_COLOR);
            btnOK.setForeground(Color.WHITE);
            btnOK.addActionListener(e -> {
                if (dateChooser.getDate() != null) {
                    java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy");
                    textField.setText(sdf.format(dateChooser.getDate()));
                }
                dateDialog.dispose();
            });
            
            MyButton btnCancel = new MyButton("Cancel", 20);
            btnCancel.setBackgroundColor(Color.decode("#F44336"));
            btnCancel.setForeground(Color.WHITE);
            btnCancel.addActionListener(e -> dateDialog.dispose());
            
            buttonPanel.add(btnOK);
            buttonPanel.add(btnCancel);
            panel.add(buttonPanel, BorderLayout.SOUTH);
            
            dateDialog.add(panel);
            dateDialog.setVisible(true);
        }
        
        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            textField.setText(value != null ? value.toString() : "");
            return textField;
        }
        
        @Override
        public Object getCellEditorValue() {
            return textField.getText();
        }
    }
}