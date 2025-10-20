package com.Admin.inventory.GUI;

import com.Admin.inventory.DTO.DTOInventory;
import com.ComponentandDatabase.Components.MyButton;
import com.ComponentandDatabase.Components.MyTextField;
import com.ComponentandDatabase.Components.CustomDialog;
import static com.ComponentandDatabase.Components.UIConstants.*;

import javax.swing.*;
import java.awt.*;
import java.math.BigDecimal;

public class ProductDetailsDialog extends JDialog {
    private DTOInventory inventoryItem;
    private MyTextField txtWarehouseId, txtProductName, txtCategory, txtSupplier;
    private MyTextField txtQuantity, txtUnitPrice;
    private MyButton btnSave, btnCancel;
    private boolean isEditMode;
    
    public ProductDetailsDialog(JFrame parent, DTOInventory item, boolean isEdit) {
        super(parent, isEdit ? "Edit Product" : "View Product Details", true);
        this.inventoryItem = item;
        this.isEditMode = isEdit;
        
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
        
        JLabel lblTitle = new JLabel(isEditMode ? "Edit Product Details" : "Product Details");
        lblTitle.setFont(FONT_TITLE_MEDIUM);
        lblTitle.setForeground(Color.WHITE);
        lblTitle.setHorizontalAlignment(SwingConstants.CENTER);
        headerPanel.add(lblTitle, BorderLayout.CENTER);
        
        add(headerPanel, BorderLayout.NORTH);
        
        // Main content
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(null);
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Form fields
        createFormFields(mainPanel);
        
        // Buttons
        createButtons(mainPanel);
        
        add(mainPanel, BorderLayout.CENTER);
    }
    
    private void createFormFields(JPanel panel) {
        int y = 20;
        int labelWidth = 120;
        int fieldWidth = 300;
        int fieldHeight = 35;
        int spacing = 50;
        
        // Warehouse ID
        JLabel lblWarehouseId = new JLabel("Warehouse ID:");
        lblWarehouseId.setBounds(20, y, labelWidth, 25);
        lblWarehouseId.setFont(FONT_CONTENT_MEDIUM);
        panel.add(lblWarehouseId);
        
        txtWarehouseId = new MyTextField();
        txtWarehouseId.setBounds(150, y, fieldWidth, fieldHeight);
        txtWarehouseId.setEnabled(false); // Always disabled for viewing
        panel.add(txtWarehouseId);
        y += spacing;
        
        // Product Name
        JLabel lblProductName = new JLabel("Product Name:");
        lblProductName.setBounds(20, y, labelWidth, 25);
        lblProductName.setFont(FONT_CONTENT_MEDIUM);
        panel.add(lblProductName);
        
        txtProductName = new MyTextField();
        txtProductName.setBounds(150, y, fieldWidth, fieldHeight);
        txtProductName.setEnabled(isEditMode);
        panel.add(txtProductName);
        y += spacing;
        
        // Category
        JLabel lblCategory = new JLabel("Category:");
        lblCategory.setBounds(20, y, labelWidth, 25);
        lblCategory.setFont(FONT_CONTENT_MEDIUM);
        panel.add(lblCategory);
        
        txtCategory = new MyTextField();
        txtCategory.setBounds(150, y, fieldWidth, fieldHeight);
        txtCategory.setEnabled(isEditMode);
        panel.add(txtCategory);
        y += spacing;
        
        // Supplier
        JLabel lblSupplier = new JLabel("Supplier:");
        lblSupplier.setBounds(20, y, labelWidth, 25);
        lblSupplier.setFont(FONT_CONTENT_MEDIUM);
        panel.add(lblSupplier);
        
        txtSupplier = new MyTextField();
        txtSupplier.setBounds(150, y, fieldWidth, fieldHeight);
        txtSupplier.setEnabled(isEditMode);
        panel.add(txtSupplier);
        y += spacing;
        
        // Quantity
        JLabel lblQuantity = new JLabel("Quantity:");
        lblQuantity.setBounds(20, y, labelWidth, 25);
        lblQuantity.setFont(FONT_CONTENT_MEDIUM);
        panel.add(lblQuantity);
        
        txtQuantity = new MyTextField();
        txtQuantity.setBounds(150, y, fieldWidth, fieldHeight);
        txtQuantity.setEnabled(isEditMode);
        panel.add(txtQuantity);
        y += spacing;
        
        // Unit Price
        JLabel lblUnitPrice = new JLabel("Unit Price:");
        lblUnitPrice.setBounds(20, y, labelWidth, 25);
        lblUnitPrice.setFont(FONT_CONTENT_MEDIUM);
        panel.add(lblUnitPrice);
        
        txtUnitPrice = new MyTextField();
        txtUnitPrice.setBounds(150, y, fieldWidth, fieldHeight);
        txtUnitPrice.setEnabled(isEditMode);
        panel.add(txtUnitPrice);
    }
    
    private void createButtons(JPanel panel) {
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 10));
        buttonPanel.setBackground(Color.WHITE);
        
        btnSave = new MyButton("Save", 20);
        btnSave.setBackgroundColor(Color.decode("#4CAF50"));
        btnSave.setHoverColor(Color.decode("#45A049"));
        btnSave.setPressedColor(Color.decode("#3D8B40"));
        btnSave.setForeground(Color.WHITE);
        btnSave.setFont(FONT_CONTENT_MEDIUM);
        btnSave.setVisible(isEditMode);
        btnSave.addActionListener(e -> saveProduct());
        
        btnCancel = new MyButton("Close", 20);
        btnCancel.setBackgroundColor(Color.decode("#F44336"));
        btnCancel.setHoverColor(Color.decode("#D32F2F"));
        btnCancel.setPressedColor(Color.decode("#C62828"));
        btnCancel.setForeground(Color.WHITE);
        btnCancel.setFont(FONT_CONTENT_MEDIUM);
        btnCancel.addActionListener(e -> dispose());
        
        buttonPanel.add(btnSave);
        buttonPanel.add(btnCancel);
        
        panel.add(buttonPanel);
        buttonPanel.setBounds(0, 350, 460, 50);
    }
    
    private void loadData() {
        if (inventoryItem != null) {
            txtWarehouseId.setText(inventoryItem.getWarehouseItemId());
            txtProductName.setText(inventoryItem.getProductName());
            txtCategory.setText(inventoryItem.getCategoryId());
            txtSupplier.setText(inventoryItem.getSupId());
            txtQuantity.setText(String.valueOf(inventoryItem.getQuantityStock()));
            txtUnitPrice.setText(inventoryItem.getUnitPriceImport().toString());
        }
    }
    
    private void saveProduct() {
        try {
            // Validate input
            if (txtProductName.getText().trim().isEmpty()) {
                CustomDialog.showError("Product name is required!");
                return;
            }
            
            if (txtQuantity.getText().trim().isEmpty()) {
                CustomDialog.showError("Quantity is required!");
                return;
            }
            
            if (txtUnitPrice.getText().trim().isEmpty()) {
                CustomDialog.showError("Unit price is required!");
                return;
            }
            
            int quantity = Integer.parseInt(txtQuantity.getText().trim());
            BigDecimal unitPrice = new BigDecimal(txtUnitPrice.getText().trim());
            
            if (quantity < 0) {
                CustomDialog.showError("Quantity must be non-negative!");
                return;
            }
            
            if (unitPrice.compareTo(BigDecimal.ZERO) <= 0) {
                CustomDialog.showError("Unit price must be positive!");
                return;
            }
            
            // Update inventory item
            inventoryItem.setProductName(txtProductName.getText().trim());
            inventoryItem.setQuantityStock(quantity);
            inventoryItem.setUnitPriceImport(unitPrice);
            
            // Here you would call your BUS layer to save the changes
            // busInventory.updateInventoryItem(inventoryItem);
            
            CustomDialog.showSuccess("Product updated successfully!");
            dispose();
            
        } catch (NumberFormatException e) {
            CustomDialog.showError("Please enter valid numbers for quantity and unit price!");
        } catch (Exception e) {
            CustomDialog.showError("Failed to save product: " + e.getMessage());
        }
    }
}
