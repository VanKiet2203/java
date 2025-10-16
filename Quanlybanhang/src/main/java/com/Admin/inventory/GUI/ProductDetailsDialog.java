package com.Admin.inventory.GUI;

import com.Admin.inventory.DTO.DTOInventory;
import com.ComponentandDatabase.Components.MyButton;

import javax.swing.*;
import java.awt.*;

public class ProductDetailsDialog extends JDialog {
    private DTOInventory product;
    
    public ProductDetailsDialog(JFrame parent, DTOInventory product) {
        super(parent, "Product Details - " + product.getProductId(), true);
        this.product = product;
        
        initComponents();
        loadProductDetails();
    }
    
    private void initComponents() {
        setLayout(new BorderLayout());
        setSize(600, 500);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        
        // Header panel
        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
        headerPanel.setBackground(Color.decode("#1976D2"));
        headerPanel.setPreferredSize(new Dimension(600, 60));
        
        JLabel lblTitle = new JLabel("PRODUCT DETAILS", JLabel.CENTER);
        lblTitle.setFont(new Font("Arial", Font.BOLD, 20));
        lblTitle.setForeground(Color.WHITE);
        headerPanel.add(lblTitle);
        
        add(headerPanel, BorderLayout.NORTH);
        
        // Content panel
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new GridBagLayout());
        contentPanel.setBackground(Color.WHITE);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.WEST;
        
        // Product ID
        gbc.gridx = 0; gbc.gridy = 0;
        contentPanel.add(createLabel("Product ID:"), gbc);
        gbc.gridx = 1;
        contentPanel.add(createValueLabel(product.getProductId()), gbc);
        
        // Product Name
        gbc.gridx = 0; gbc.gridy = 1;
        contentPanel.add(createLabel("Product Name:"), gbc);
        gbc.gridx = 1;
        contentPanel.add(createValueLabel(product.getProductName()), gbc);
        
        // Price
        gbc.gridx = 0; gbc.gridy = 2;
        contentPanel.add(createLabel("Price:"), gbc);
        gbc.gridx = 1;
        contentPanel.add(createValueLabel(String.format("%,.0f VND", product.getPrice().doubleValue())), gbc);
        
        // Category
        gbc.gridx = 0; gbc.gridy = 3;
        contentPanel.add(createLabel("Category:"), gbc);
        gbc.gridx = 1;
        contentPanel.add(createValueLabel(product.getCategoryId()), gbc);
        
        // Brand
        gbc.gridx = 0; gbc.gridy = 4;
        contentPanel.add(createLabel("Brand:"), gbc);
        gbc.gridx = 1;
        contentPanel.add(createValueLabel(product.getBrandId()), gbc);
        
        // Stock Quantity
        gbc.gridx = 0; gbc.gridy = 5;
        contentPanel.add(createLabel("Stock Quantity:"), gbc);
        gbc.gridx = 1;
        contentPanel.add(createValueLabel(String.valueOf(product.getQuantityInStock())), gbc);
        
        // Has Image
        gbc.gridx = 0; gbc.gridy = 6;
        contentPanel.add(createLabel("Has Image:"), gbc);
        gbc.gridx = 1;
        contentPanel.add(createValueLabel(product.getImage() != null ? "Yes" : "No"), gbc);
        
        // Color
        gbc.gridx = 0; gbc.gridy = 7;
        contentPanel.add(createLabel("Color:"), gbc);
        gbc.gridx = 1;
        contentPanel.add(createValueLabel(product.getColor() != null ? product.getColor() : "Not specified"), gbc);
        
        // Speed
        gbc.gridx = 0; gbc.gridy = 8;
        contentPanel.add(createLabel("Speed:"), gbc);
        gbc.gridx = 1;
        contentPanel.add(createValueLabel(product.getSpeed() != null ? product.getSpeed() : "Not specified"), gbc);
        
        // Battery Capacity
        gbc.gridx = 0; gbc.gridy = 9;
        contentPanel.add(createLabel("Battery Capacity:"), gbc);
        gbc.gridx = 1;
        contentPanel.add(createValueLabel(product.getBatteryCapacity() != null ? product.getBatteryCapacity() : "Not specified"), gbc);
        
        add(contentPanel, BorderLayout.CENTER);
        
        // Button panel
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.setBackground(Color.WHITE);
        
        MyButton btnClose = new MyButton("Close", 20);
        btnClose.setBackgroundColor(Color.decode("#757575"));
        btnClose.setHoverColor(Color.decode("#616161"));
        btnClose.setPressedColor(Color.decode("#424242"));
        btnClose.setFont(new Font("Arial", Font.BOLD, 12));
        btnClose.setForeground(Color.WHITE);
        btnClose.addActionListener(e -> dispose());
        buttonPanel.add(btnClose);
        
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    private JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Arial", Font.BOLD, 14));
        label.setForeground(Color.decode("#333333"));
        return label;
    }
    
    private JLabel createValueLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Arial", Font.PLAIN, 14));
        label.setForeground(Color.decode("#666666"));
        label.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        label.setBackground(Color.decode("#F5F5F5"));
        label.setOpaque(true);
        return label;
    }
    
    private void loadProductDetails() {
        // Product details are already loaded in constructor
        // This method can be used for additional processing if needed
    }
}
