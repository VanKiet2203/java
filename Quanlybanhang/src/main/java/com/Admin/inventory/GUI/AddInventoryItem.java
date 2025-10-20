package com.Admin.inventory.GUI;

import com.Admin.inventory.BUS.BUSInventory;
import com.Admin.inventory.DTO.DTOInventory;
import com.Admin.category.BUS.BusCategory;
import com.Admin.category.DTO.DTOCategory;
import com.ComponentandDatabase.Components.MyButton;
import com.ComponentandDatabase.Components.MyPanel;
import com.ComponentandDatabase.Components.MyTextField;
import com.ComponentandDatabase.Components.MyCombobox;
import com.ComponentandDatabase.Components.CustomDialog;
import static com.ComponentandDatabase.Components.UIConstants.*;

import javax.swing.*;
import java.awt.*;
import java.math.BigDecimal;
import java.util.List;

public class AddInventoryItem extends JDialog {
    private MyTextField txtWarehouseId, txtProductName, txtQuantity, txtUnitPrice;
    private MyCombobox<String> cmbCategory, cmbSupplier;
    private MyButton btnSave, btnCancel, btnGenerateId;
    private BUSInventory busInventory;
    private BusCategory busCategory;
    
    public AddInventoryItem(JFrame parent) {
        super(parent, "Add New Inventory Item", true);
        initComponents();
        init();
        loadData();
    }
    
    private void initComponents() {
        setSize(600, 500);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        setResizable(false);
    }
    
    private void init() {
        // Header
        JPanel headerPanel = new MyPanel(PRIMARY_COLOR);
        headerPanel.setPreferredSize(new Dimension(600, 60));
        headerPanel.setLayout(new BorderLayout());
        
        JLabel lblTitle = new JLabel("Add New Inventory Item");
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
        
        // Warehouse ID
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.EAST;
        JLabel lblWarehouseId = new JLabel("Warehouse ID:");
        lblWarehouseId.setFont(FONT_CONTENT_MEDIUM);
        mainPanel.add(lblWarehouseId, gbc);
        
        gbc.gridx = 1; gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        JPanel warehouseIdPanel = new JPanel(new BorderLayout());
        txtWarehouseId = new MyTextField();
        txtWarehouseId.setHint("Auto-generated or enter manually");
        txtWarehouseId.setTextFont(FONT_CONTENT_MEDIUM);
        warehouseIdPanel.add(txtWarehouseId, BorderLayout.CENTER);
        
        btnGenerateId = new MyButton("Generate", 15);
        btnGenerateId.setBackgroundColor(Color.decode("#2196F3"));
        btnGenerateId.setHoverColor(Color.decode("#1976D2"));
        btnGenerateId.setForeground(Color.WHITE);
        btnGenerateId.setFont(FONT_CONTENT_SMALL);
        btnGenerateId.addActionListener(e -> generateWarehouseId());
        warehouseIdPanel.add(btnGenerateId, BorderLayout.EAST);
        mainPanel.add(warehouseIdPanel, gbc);
        
        // Product Name
        gbc.gridx = 0; gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.fill = GridBagConstraints.NONE;
        JLabel lblProductName = new JLabel("Product Name:");
        lblProductName.setFont(FONT_CONTENT_MEDIUM);
        mainPanel.add(lblProductName, gbc);
        
        gbc.gridx = 1; gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        txtProductName = new MyTextField();
        txtProductName.setHint("Enter product name");
        txtProductName.setTextFont(FONT_CONTENT_MEDIUM);
        mainPanel.add(txtProductName, gbc);
        
        // Category
        gbc.gridx = 0; gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.fill = GridBagConstraints.NONE;
        JLabel lblCategory = new JLabel("Category:");
        lblCategory.setFont(FONT_CONTENT_MEDIUM);
        mainPanel.add(lblCategory, gbc);
        
        gbc.gridx = 1; gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        cmbCategory = new MyCombobox<>();
        cmbCategory.setCustomFont(FONT_CONTENT_MEDIUM);
        mainPanel.add(cmbCategory, gbc);
        
        // Supplier
        gbc.gridx = 0; gbc.gridy = 3;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.fill = GridBagConstraints.NONE;
        JLabel lblSupplier = new JLabel("Supplier:");
        lblSupplier.setFont(FONT_CONTENT_MEDIUM);
        mainPanel.add(lblSupplier, gbc);
        
        gbc.gridx = 1; gbc.gridy = 3;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        cmbSupplier = new MyCombobox<>();
        cmbSupplier.setCustomFont(FONT_CONTENT_MEDIUM);
        mainPanel.add(cmbSupplier, gbc);
        
        // Quantity
        gbc.gridx = 0; gbc.gridy = 4;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.fill = GridBagConstraints.NONE;
        JLabel lblQuantity = new JLabel("Quantity:");
        lblQuantity.setFont(FONT_CONTENT_MEDIUM);
        mainPanel.add(lblQuantity, gbc);
        
        gbc.gridx = 1; gbc.gridy = 4;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        txtQuantity = new MyTextField();
        txtQuantity.setHint("Enter quantity");
        txtQuantity.setTextFont(FONT_CONTENT_MEDIUM);
        mainPanel.add(txtQuantity, gbc);
        
        // Unit Price
        gbc.gridx = 0; gbc.gridy = 5;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.fill = GridBagConstraints.NONE;
        JLabel lblUnitPrice = new JLabel("Unit Price:");
        lblUnitPrice.setFont(FONT_CONTENT_MEDIUM);
        mainPanel.add(lblUnitPrice, gbc);
        
        gbc.gridx = 1; gbc.gridy = 5;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        txtUnitPrice = new MyTextField();
        txtUnitPrice.setHint("Enter unit price");
        txtUnitPrice.setTextFont(FONT_CONTENT_MEDIUM);
        mainPanel.add(txtUnitPrice, gbc);
        
        // Buttons
        gbc.gridx = 0; gbc.gridy = 6;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.NONE;
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        buttonPanel.setBackground(Color.WHITE);
        
        btnSave = new MyButton("Save to Inventory", 20);
        btnSave.setBackgroundColor(Color.decode("#4CAF50"));
        btnSave.setHoverColor(Color.decode("#45A049"));
        btnSave.setPressedColor(Color.decode("#3D8B40"));
        btnSave.setForeground(Color.WHITE);
        btnSave.setFont(FONT_CONTENT_MEDIUM);
        btnSave.addActionListener(e -> saveInventoryItem());
        
        btnCancel = new MyButton("Cancel", 20);
        btnCancel.setBackgroundColor(Color.decode("#F44336"));
        btnCancel.setHoverColor(Color.decode("#D32F2F"));
        btnCancel.setPressedColor(Color.decode("#C62828"));
        btnCancel.setForeground(Color.WHITE);
        btnCancel.setFont(FONT_CONTENT_MEDIUM);
        btnCancel.addActionListener(e -> dispose());
        
        buttonPanel.add(btnSave);
        buttonPanel.add(btnCancel);
        mainPanel.add(buttonPanel, gbc);
        
        add(mainPanel, BorderLayout.CENTER);
    }
    
    private void loadData() {
        busInventory = new BUSInventory();
        busCategory = new BusCategory();
        
        // Ensure sample data exists
        busInventory.ensureSampleDataExists();
        
        // Load categories
        try {
            List<DTOCategory> categories = busCategory.getAllCategories();
            cmbCategory.removeAllItems();
            for (DTOCategory category : categories) {
                cmbCategory.addItem(category.getCategoryID() + " - " + category.getCategoryName());
            }
        } catch (Exception e) {
            e.printStackTrace();
            CustomDialog.showError("Failed to load categories: " + e.getMessage());
        }
        
        // Load suppliers
        try {
            List<String> suppliers = busInventory.getAllSuppliers();
            cmbSupplier.removeAllItems();
            for (String supplier : suppliers) {
                cmbSupplier.addItem(supplier);
            }
        } catch (Exception e) {
            e.printStackTrace();
            CustomDialog.showError("Failed to load suppliers: " + e.getMessage());
        }
    }
    
    private void generateWarehouseId() {
        try {
            String newId = busInventory.generateWarehouseId();
            txtWarehouseId.setText(newId);
        } catch (Exception e) {
            CustomDialog.showError("Failed to generate ID: " + e.getMessage());
        }
    }
    
    private void saveInventoryItem() {
        String warehouseId = txtWarehouseId.getText().trim();
        String productName = txtProductName.getText().trim();
        String categoryId = cmbCategory.getSelectedItem() != null ? 
            cmbCategory.getSelectedItem().toString().split(" - ")[0] : "";
        String supplierId = cmbSupplier.getSelectedItem() != null ? 
            cmbSupplier.getSelectedItem().toString().split(" - ")[0] : "";
        String quantityStr = txtQuantity.getText().trim();
        String unitPriceStr = txtUnitPrice.getText().trim();
        
        // Validation
        if (warehouseId.isEmpty() || productName.isEmpty() || 
            categoryId.isEmpty() || supplierId.isEmpty() || quantityStr.isEmpty() || unitPriceStr.isEmpty()) {
            CustomDialog.showError("Please fill in all fields!");
            return;
        }
        
        int quantity;
        BigDecimal unitPrice;
        
        try {
            quantity = Integer.parseInt(quantityStr);
            if (quantity <= 0) {
                CustomDialog.showError("Quantity must be greater than 0!");
                return;
            }
        } catch (NumberFormatException e) {
            CustomDialog.showError("Invalid quantity format!");
            return;
        }
        
        try {
            unitPrice = new BigDecimal(unitPriceStr);
            if (unitPrice.compareTo(BigDecimal.ZERO) <= 0) {
                CustomDialog.showError("Unit price must be greater than 0!");
                return;
            }
        } catch (NumberFormatException e) {
            CustomDialog.showError("Invalid unit price format!");
            return;
        }
        
        try {
            // Create inventory item
            DTOInventory inventoryItem = new DTOInventory();
            inventoryItem.setWarehouseItemId(warehouseId);
            inventoryItem.setProductName(productName);
            inventoryItem.setCategoryId(categoryId);
            inventoryItem.setSupId(supplierId);
            inventoryItem.setQuantityStock(quantity);
            inventoryItem.setUnitPriceImport(unitPrice);
            
            // Save to database
            boolean success = busInventory.addInventoryItem(inventoryItem);
            
            if (success) {
                CustomDialog.showSuccess("Inventory item added successfully!");
                dispose();
            } else {
                CustomDialog.showError("Failed to add inventory item!");
            }
        } catch (Exception e) {
            e.printStackTrace();
            CustomDialog.showError("Error adding inventory item: " + e.getMessage());
        }
    }
}
