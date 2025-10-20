package com.Admin.insurance.GUI;

import com.ComponentandDatabase.Components.MyButton;
import com.ComponentandDatabase.Components.MyTextField;
import com.ComponentandDatabase.Components.CustomDialog;
import com.toedter.calendar.JDateChooser;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

import static com.ComponentandDatabase.Components.UIConstants.*;

public class ProductWarrantyDialog extends JDialog {
    
    private MyTextField txtProductId, txtProductName, txtQuantity, txtUnitPrice;
    private JSpinner spnWarrantyMonths;
    private JDateChooser startDate, endDate;
    private MyButton btnCalculate, btnSave, btnCancel;
    
    private String productId;
    private String productName;
    private int quantity;
    private double unitPrice;
    private int warrantyMonths;
    private LocalDate warrantyStartDate;
    private LocalDate warrantyEndDate;
    
    private boolean saved = false;
    
    public ProductWarrantyDialog(JFrame parent, String productId, String productName, 
                                int quantity, double unitPrice) {
        super(parent, "Set Warranty Period", true);
        this.productId = productId;
        this.productName = productName;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        
        initComponents();
        init();
        loadData();
    }
    
    private void initComponents() {
        setSize(500, 400);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        setResizable(false);
    }
    
    private void init() {
        // Header
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(PRIMARY_COLOR);
        headerPanel.setPreferredSize(new Dimension(500, 50));
        headerPanel.setLayout(new BorderLayout());
        
        JLabel lblTitle = new JLabel("Product Warranty Configuration");
        lblTitle.setFont(FONT_TITLE_MEDIUM);
        lblTitle.setForeground(Color.WHITE);
        lblTitle.setHorizontalAlignment(SwingConstants.CENTER);
        headerPanel.add(lblTitle, BorderLayout.CENTER);
        
        add(headerPanel, BorderLayout.NORTH);
        
        // Main content
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new GridBagLayout());
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.WEST;
        
        // Product Information Section
        JLabel lblProductInfo = new JLabel("Product Information");
        lblProductInfo.setFont(FONT_TITLE_SMALL);
        lblProductInfo.setForeground(PRIMARY_COLOR);
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        mainPanel.add(lblProductInfo, gbc);
        
        // Product ID
        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 1;
        mainPanel.add(new JLabel("Product ID:"), gbc);
        gbc.gridx = 1;
        txtProductId = new MyTextField();
        txtProductId.setPreferredSize(new Dimension(200, 30));
        txtProductId.setLocked(true);
        txtProductId.setBackgroundColor(Color.decode("#F5F5F5"));
        mainPanel.add(txtProductId, gbc);
        
        // Product Name
        gbc.gridx = 0; gbc.gridy = 2;
        mainPanel.add(new JLabel("Product Name:"), gbc);
        gbc.gridx = 1;
        txtProductName = new MyTextField();
        txtProductName.setPreferredSize(new Dimension(200, 30));
        txtProductName.setLocked(true);
        txtProductName.setBackgroundColor(Color.decode("#F5F5F5"));
        mainPanel.add(txtProductName, gbc);
        
        // Quantity
        gbc.gridx = 0; gbc.gridy = 3;
        mainPanel.add(new JLabel("Quantity:"), gbc);
        gbc.gridx = 1;
        txtQuantity = new MyTextField();
        txtQuantity.setPreferredSize(new Dimension(200, 30));
        txtQuantity.setLocked(true);
        txtQuantity.setBackgroundColor(Color.decode("#F5F5F5"));
        mainPanel.add(txtQuantity, gbc);
        
        // Unit Price
        gbc.gridx = 0; gbc.gridy = 4;
        mainPanel.add(new JLabel("Unit Price:"), gbc);
        gbc.gridx = 1;
        txtUnitPrice = new MyTextField();
        txtUnitPrice.setPreferredSize(new Dimension(200, 30));
        txtUnitPrice.setLocked(true);
        txtUnitPrice.setBackgroundColor(Color.decode("#F5F5F5"));
        mainPanel.add(txtUnitPrice, gbc);
        
        // Warranty Configuration Section
        JLabel lblWarrantyInfo = new JLabel("Warranty Configuration");
        lblWarrantyInfo.setFont(FONT_TITLE_SMALL);
        lblWarrantyInfo.setForeground(PRIMARY_COLOR);
        gbc.gridx = 0; gbc.gridy = 5; gbc.gridwidth = 2;
        mainPanel.add(lblWarrantyInfo, gbc);
        
        // Warranty Period
        gbc.gridx = 0; gbc.gridy = 6; gbc.gridwidth = 1;
        mainPanel.add(new JLabel("Warranty Period (months):"), gbc);
        gbc.gridx = 1;
        spnWarrantyMonths = new JSpinner(new SpinnerNumberModel(12, 1, 60, 1));
        spnWarrantyMonths.setPreferredSize(new Dimension(100, 30));
        mainPanel.add(spnWarrantyMonths, gbc);
        
        // Calculate button
        gbc.gridx = 2;
        btnCalculate = new MyButton("Calculate", 15);
        btnCalculate.setBackgroundColor(INFO_COLOR);
        btnCalculate.setHoverColor(INFO_HOVER);
        btnCalculate.setForeground(Color.WHITE);
        btnCalculate.setPreferredSize(new Dimension(100, 30));
        btnCalculate.addActionListener(e -> calculateWarrantyDates());
        mainPanel.add(btnCalculate, gbc);
        
        // Start Date
        gbc.gridx = 0; gbc.gridy = 7;
        mainPanel.add(new JLabel("Start Date:"), gbc);
        gbc.gridx = 1;
        startDate = new JDateChooser();
        startDate.setPreferredSize(new Dimension(150, 30));
        startDate.setDateFormatString("dd/MM/yyyy");
        startDate.setDate(new Date());
        mainPanel.add(startDate, gbc);
        
        // End Date
        gbc.gridx = 0; gbc.gridy = 8;
        mainPanel.add(new JLabel("End Date:"), gbc);
        gbc.gridx = 1;
        endDate = new JDateChooser();
        endDate.setPreferredSize(new Dimension(150, 30));
        endDate.setDateFormatString("dd/MM/yyyy");
        mainPanel.add(endDate, gbc);
        
        // Buttons
        gbc.gridx = 0; gbc.gridy = 9; gbc.gridwidth = 3;
        gbc.anchor = GridBagConstraints.CENTER;
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        buttonPanel.setBackground(Color.WHITE);
        
        btnSave = new MyButton("Save", 20);
        btnSave.setBackgroundColor(PRIMARY_COLOR);
        btnSave.setHoverColor(PRIMARY_HOVER);
        btnSave.setForeground(Color.WHITE);
        btnSave.setPreferredSize(new Dimension(100, 35));
        btnSave.addActionListener(e -> saveWarranty());
        buttonPanel.add(btnSave);
        
        btnCancel = new MyButton("Cancel", 20);
        btnCancel.setBackgroundColor(Color.decode("#6C757D"));
        btnCancel.setHoverColor(Color.decode("#5A6268"));
        btnCancel.setForeground(Color.WHITE);
        btnCancel.setPreferredSize(new Dimension(100, 35));
        btnCancel.addActionListener(e -> dispose());
        buttonPanel.add(btnCancel);
        
        mainPanel.add(buttonPanel, gbc);
        
        add(mainPanel, BorderLayout.CENTER);
    }
    
    private void loadData() {
        txtProductId.setText(productId);
        txtProductName.setText(productName);
        txtQuantity.setText(String.valueOf(quantity));
        txtUnitPrice.setText(String.format("%,.0f VND", unitPrice));
        
        // Set default warranty period
        spnWarrantyMonths.setValue(12);
        calculateWarrantyDates();
    }
    
    private void calculateWarrantyDates() {
        Date startDateValue = startDate.getDate();
        if (startDateValue == null) {
            startDate.setDate(new Date());
            startDateValue = startDate.getDate();
        }
        
        int months = (Integer) spnWarrantyMonths.getValue();
        
        // Calculate end date
        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.setTime(startDateValue);
        cal.add(java.util.Calendar.MONTH, months);
        
        endDate.setDate(cal.getTime());
    }
    
    private void saveWarranty() {
        if (!validateInput()) {
            return;
        }
        
        // Save the warranty configuration
        warrantyMonths = (Integer) spnWarrantyMonths.getValue();
        warrantyStartDate = startDate.getDate().toInstant()
            .atZone(ZoneId.systemDefault()).toLocalDate();
        warrantyEndDate = endDate.getDate().toInstant()
            .atZone(ZoneId.systemDefault()).toLocalDate();
        
        saved = true;
        CustomDialog.showSuccess("Warranty configuration saved successfully!");
        dispose();
    }
    
    private boolean validateInput() {
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
    
    // Getters for the warranty configuration
    public boolean isSaved() {
        return saved;
    }
    
    public int getWarrantyMonths() {
        return warrantyMonths;
    }
    
    public LocalDate getWarrantyStartDate() {
        return warrantyStartDate;
    }
    
    public LocalDate getWarrantyEndDate() {
        return warrantyEndDate;
    }
}
