package com.Admin.inventory.GUI;

import com.Admin.inventory.BUS.BUSInventory;
import com.Admin.inventory.DAO.DAOInventory;
import com.ComponentandDatabase.Components.MyButton;
import com.ComponentandDatabase.Components.MyPanel;
import com.ComponentandDatabase.Components.MyTextField;
import com.ComponentandDatabase.Components.CustomDialog;
import static com.ComponentandDatabase.Components.UIConstants.*;

import javax.swing.*;
import java.awt.*;
import java.math.BigDecimal;

/**
 * ReimportItemDialog - Giao diện nhập lại hàng vào kho (Reimport)
 * - Giao diện đẹp, nhất quán với các form khác
 * - Hiển thị đầy đủ thông tin sản phẩm
 * - Sử dụng BUS layer để xử lý logic
 */
public class ReimportItemDialog extends JDialog {
    
    // Info display fields (read-only)
    private MyTextField txtWarehouseId, txtProductName, txtCategory, txtSupplier, txtCountry;
    private MyTextField txtCurrentQuantity, txtCurrentPrice, txtProductionYear;
    
    // Input fields
    private JSpinner spnQuantity;
    private MyTextField txtUnitPrice;
    
    // Buttons
    private MyButton btnSubmit, btnCancel;
    
    // Business logic
    private BUSInventory busInventory;
    private DAOInventory.InventoryItemInfo itemInfo;
    
    public ReimportItemDialog(JFrame parent, String warehouseId, String productName, int currentQuantity) {
        super(parent, "Reimport Warehouse Item", true);
        busInventory = new BUSInventory();
        
        // Load full item information from database
        if (warehouseId == null || warehouseId.trim().isEmpty()) {
            CustomDialog.showError("Warehouse ID cannot be empty!");
            dispose();
            return;
        }
        
        itemInfo = busInventory.getInventoryItemFullInfo(warehouseId);
        if (itemInfo == null) {
            CustomDialog.showError("Cannot load inventory item information!\nWarehouse ID: " + warehouseId);
            dispose();
            return;
        }
        
        initComponents();
        loadItemData();
    }
    
    private void initComponents() {
        setSize(850, 800);
        setLocationRelativeTo(getParent());
        setLayout(new BorderLayout());
        setResizable(true);
        
        // Header
        JPanel headerPanel = new MyPanel(PRIMARY_COLOR);
        headerPanel.setPreferredSize(new Dimension(850, 60));
        headerPanel.setLayout(new BorderLayout());
        
        JLabel lblTitle = new JLabel("Reimport Warehouse Item");
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
        
        // Section: Product Information (Read-only)
        JLabel lblInfoSection = new JLabel("Product Information");
        lblInfoSection.setFont(FONT_TITLE_SMALL);
        lblInfoSection.setForeground(PRIMARY_COLOR);
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.WEST;
        mainPanel.add(lblInfoSection, gbc);
        
        // Warehouse ID
        gbc.gridwidth = 1;
        gbc.gridx = 0; gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.EAST;
        JLabel lblWarehouseId = new JLabel("Warehouse ID:");
        lblWarehouseId.setFont(FONT_CONTENT_MEDIUM);
        mainPanel.add(lblWarehouseId, gbc);
        
        gbc.gridx = 1; gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        txtWarehouseId = new MyTextField();
        txtWarehouseId.setLocked(true);
        txtWarehouseId.setTextFont(FONT_CONTENT_MEDIUM);
        mainPanel.add(txtWarehouseId, gbc);
        
        // Product Name
        gbc.gridx = 0; gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.fill = GridBagConstraints.NONE;
        JLabel lblProductName = new JLabel("Product Name:");
        lblProductName.setFont(FONT_CONTENT_MEDIUM);
        mainPanel.add(lblProductName, gbc);
        
        gbc.gridx = 1; gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        txtProductName = new MyTextField();
        txtProductName.setLocked(true);
        txtProductName.setTextFont(FONT_CONTENT_MEDIUM);
        mainPanel.add(txtProductName, gbc);
        
        // Category
        gbc.gridx = 0; gbc.gridy = 3;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.fill = GridBagConstraints.NONE;
        JLabel lblCategory = new JLabel("Category:");
        lblCategory.setFont(FONT_CONTENT_MEDIUM);
        mainPanel.add(lblCategory, gbc);
        
        gbc.gridx = 1; gbc.gridy = 3;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        txtCategory = new MyTextField();
        txtCategory.setLocked(true);
        txtCategory.setTextFont(FONT_CONTENT_MEDIUM);
        mainPanel.add(txtCategory, gbc);
        
        // Supplier
        gbc.gridx = 0; gbc.gridy = 4;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.fill = GridBagConstraints.NONE;
        JLabel lblSupplier = new JLabel("Supplier:");
        lblSupplier.setFont(FONT_CONTENT_MEDIUM);
        mainPanel.add(lblSupplier, gbc);
        
        gbc.gridx = 1; gbc.gridy = 4;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        txtSupplier = new MyTextField();
        txtSupplier.setLocked(true);
        txtSupplier.setTextFont(FONT_CONTENT_MEDIUM);
        mainPanel.add(txtSupplier, gbc);
        
        // Country
        gbc.gridx = 0; gbc.gridy = 5;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.fill = GridBagConstraints.NONE;
        JLabel lblCountry = new JLabel("Origin:");
        lblCountry.setFont(FONT_CONTENT_MEDIUM);
        mainPanel.add(lblCountry, gbc);
        
        gbc.gridx = 1; gbc.gridy = 5;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        txtCountry = new MyTextField();
        txtCountry.setLocked(true);
        txtCountry.setTextFont(FONT_CONTENT_MEDIUM);
        mainPanel.add(txtCountry, gbc);
        
        // Production Year
        gbc.gridx = 0; gbc.gridy = 6;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.fill = GridBagConstraints.NONE;
        JLabel lblProductionYear = new JLabel("Production Year:");
        lblProductionYear.setFont(FONT_CONTENT_MEDIUM);
        mainPanel.add(lblProductionYear, gbc);
        
        gbc.gridx = 1; gbc.gridy = 6;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        txtProductionYear = new MyTextField();
        txtProductionYear.setLocked(true);
        txtProductionYear.setTextFont(FONT_CONTENT_MEDIUM);
        mainPanel.add(txtProductionYear, gbc);
        
        // Current Quantity
        gbc.gridx = 0; gbc.gridy = 7;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.fill = GridBagConstraints.NONE;
        JLabel lblCurrentQuantity = new JLabel("Current Stock:");
        lblCurrentQuantity.setFont(FONT_CONTENT_MEDIUM);
        mainPanel.add(lblCurrentQuantity, gbc);
        
        gbc.gridx = 1; gbc.gridy = 7;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        txtCurrentQuantity = new MyTextField();
        txtCurrentQuantity.setLocked(true);
        txtCurrentQuantity.setTextFont(FONT_CONTENT_MEDIUM);
        txtCurrentQuantity.setForeground(PRIMARY_COLOR);
        mainPanel.add(txtCurrentQuantity, gbc);
        
        // Current Unit Price
        gbc.gridx = 0; gbc.gridy = 8;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.fill = GridBagConstraints.NONE;
        JLabel lblCurrentPrice = new JLabel("Current Import Price:");
        lblCurrentPrice.setFont(FONT_CONTENT_MEDIUM);
        mainPanel.add(lblCurrentPrice, gbc);
        
        gbc.gridx = 1; gbc.gridy = 8;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        txtCurrentPrice = new MyTextField();
        txtCurrentPrice.setLocked(true);
        txtCurrentPrice.setTextFont(FONT_CONTENT_MEDIUM);
        txtCurrentPrice.setForeground(PRIMARY_COLOR);
        mainPanel.add(txtCurrentPrice, gbc);
        
        // Separator
        gbc.gridx = 0; gbc.gridy = 9;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(15, 10, 10, 10);
        JSeparator separator = new JSeparator();
        mainPanel.add(separator, gbc);
        
        // Section: Reimport Information
        JLabel lblReimportSection = new JLabel("Reimport Information");
        lblReimportSection.setFont(FONT_TITLE_SMALL);
        lblReimportSection.setForeground(PRIMARY_COLOR);
        gbc.gridx = 0; gbc.gridy = 10;
        gbc.anchor = GridBagConstraints.WEST;
        mainPanel.add(lblReimportSection, gbc);
        
        // Additional Quantity
        gbc.gridwidth = 1;
        gbc.gridx = 0; gbc.gridy = 11;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.fill = GridBagConstraints.NONE;
        gbc.insets = new Insets(10, 10, 10, 10);
        JLabel lblQuantity = new JLabel("Additional Quantity:");
        lblQuantity.setFont(FONT_CONTENT_MEDIUM);
        mainPanel.add(lblQuantity, gbc);
        
        gbc.gridx = 1; gbc.gridy = 11;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        spnQuantity = new JSpinner(new SpinnerNumberModel(1, 1, 1_000_000, 1));
        spnQuantity.setFont(FONT_CONTENT_MEDIUM);
        spnQuantity.setPreferredSize(new Dimension(200, 35));
        JComponent editor = spnQuantity.getEditor();
        if (editor instanceof JSpinner.DefaultEditor) {
            ((JSpinner.DefaultEditor) editor).getTextField().setFont(FONT_CONTENT_MEDIUM);
        }
        mainPanel.add(spnQuantity, gbc);
        
        // Unit Price (Optional)
        gbc.gridx = 0; gbc.gridy = 12;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.fill = GridBagConstraints.NONE;
        JLabel lblUnitPrice = new JLabel("New Import Price (Optional):");
        lblUnitPrice.setFont(FONT_CONTENT_MEDIUM);
        mainPanel.add(lblUnitPrice, gbc);
        
        gbc.gridx = 1; gbc.gridy = 12;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        txtUnitPrice = new MyTextField();
        txtUnitPrice.setHint("Leave blank to keep current price (e.g., 1000000.00)");
        txtUnitPrice.setTextFont(FONT_CONTENT_MEDIUM);
        mainPanel.add(txtUnitPrice, gbc);
        
        // Buttons
        gbc.gridx = 0; gbc.gridy = 13;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.NONE;
        gbc.insets = new Insets(20, 10, 10, 10);
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        buttonPanel.setBackground(Color.WHITE);
        
        btnSubmit = new MyButton("Reimport", 20);
        btnSubmit.setBackgroundColor(Color.decode("#4CAF50"));
        btnSubmit.setHoverColor(Color.decode("#45A049"));
        btnSubmit.setPressedColor(Color.decode("#3D8B40"));
        btnSubmit.setForeground(Color.WHITE);
        btnSubmit.setFont(FONT_CONTENT_MEDIUM);
        btnSubmit.addActionListener(e -> onSubmit());
        
        btnCancel = new MyButton("Cancel", 20);
        btnCancel.setBackgroundColor(Color.decode("#F44336"));
        btnCancel.setHoverColor(Color.decode("#D32F2F"));
        btnCancel.setPressedColor(Color.decode("#C62828"));
        btnCancel.setForeground(Color.WHITE);
        btnCancel.setFont(FONT_CONTENT_MEDIUM);
        btnCancel.addActionListener(e -> dispose());
        
        buttonPanel.add(btnSubmit);
        buttonPanel.add(btnCancel);
        mainPanel.add(buttonPanel, gbc);
        
        add(mainPanel, BorderLayout.CENTER);
    }
    
    private void loadItemData() {
        if (itemInfo == null) return;
        
        txtWarehouseId.setText(itemInfo.warehouseItemId);
        txtProductName.setText(itemInfo.productName != null ? itemInfo.productName : "N/A");
        txtCategory.setText(itemInfo.categoryName != null ? itemInfo.categoryName : "N/A");
        txtSupplier.setText(itemInfo.supName != null ? itemInfo.supName : "N/A");
        txtCountry.setText(itemInfo.country != null ? itemInfo.country : "N/A");
        txtProductionYear.setText(itemInfo.productionYear != null ? String.valueOf(itemInfo.productionYear) : "N/A");
        txtCurrentQuantity.setText(String.valueOf(itemInfo.quantityStock));
        txtCurrentPrice.setText(itemInfo.unitPriceImport != null ? 
            String.format("%,.2f VNĐ", itemInfo.unitPriceImport.doubleValue()) : "N/A");
    }
    
    private void onSubmit() {
        if (itemInfo == null) {
            CustomDialog.showError("Invalid inventory item!");
            return;
        }
        
        int additionalQuantity = (int) spnQuantity.getValue();
        if (additionalQuantity <= 0) {
            CustomDialog.showError("Additional quantity must be greater than 0!");
            return;
        }
        
        // Parse unit price (optional)
        BigDecimal unitPrice = null;
        String priceStr = txtUnitPrice.getText().trim();
        if (!priceStr.isEmpty()) {
            try {
                unitPrice = new BigDecimal(priceStr);
                if (unitPrice.compareTo(BigDecimal.ZERO) < 0) {
                    CustomDialog.showError("Import price must be >= 0!");
                    txtUnitPrice.requestFocus();
                    return;
                }
                if (unitPrice.compareTo(BigDecimal.ZERO) == 0) {
                    int result = JOptionPane.showConfirmDialog(
                        this,
                        "Import price is 0. Do you want to keep current price instead?",
                        "Zero Price Warning",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.WARNING_MESSAGE
                    );
                    if (result == JOptionPane.YES_OPTION) {
                        unitPrice = null; // Keep current price
                    } else {
                        txtUnitPrice.requestFocus();
                        return;
                    }
                }
            } catch (NumberFormatException e) {
                CustomDialog.showError("Invalid price format! Please enter a valid number (e.g., 1000000 or 1000000.00)");
                txtUnitPrice.requestFocus();
                return;
            }
        }
        
        // Confirm dialog
        String confirmMsg = String.format(
            "Confirm Reimport:\n\n" +
            "Warehouse ID: %s\n" +
            "Product: %s\n" +
            "Additional Quantity: %d\n" +
            "New Total Stock: %d\n" +
            "New Import Price: %s\n\n" +
            "Do you want to proceed?",
            itemInfo.warehouseItemId,
            itemInfo.productName,
            additionalQuantity,
            itemInfo.quantityStock + additionalQuantity,
            unitPrice != null ? String.format("%,.2f VNĐ", unitPrice.doubleValue()) : "Keep current price"
        );
        
        int confirm = JOptionPane.showConfirmDialog(
            this,
            confirmMsg,
            "Confirm Reimport",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE
        );
        
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }
        
        // Call BUS to reimport
        try {
            boolean success = busInventory.reimportWarehouseItem(
                itemInfo.warehouseItemId,
                additionalQuantity,
                unitPrice
            );
            
            if (success) {
                CustomDialog.showSuccess(
                    "Reimport successful!\n\n" +
                    "Warehouse ID: " + itemInfo.warehouseItemId + "\n" +
                    "Product: " + itemInfo.productName + "\n" +
                    "Additional Quantity: " + additionalQuantity + "\n" +
                    "Previous Stock: " + itemInfo.quantityStock + "\n" +
                    "New Total Stock: " + (itemInfo.quantityStock + additionalQuantity) + "\n" +
                    (unitPrice != null ? "New Import Price: " + String.format("%,.2f VNĐ", unitPrice.doubleValue()) : "Import Price: Kept current price")
                );
                dispose();
            } else {
                // Show additional error details if available
                CustomDialog.showError(
                    "Reimport failed!\n\n" +
                    "Please check:\n" +
                    "1. Warehouse ID exists: " + itemInfo.warehouseItemId + "\n" +
                    "2. Additional quantity is valid: " + additionalQuantity + "\n" +
                    "3. Database connection is available"
                );
            }
        } catch (Exception e) {
            CustomDialog.showError("Error during reimport: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
