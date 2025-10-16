package com.Admin.inventory.GUI;

import com.Admin.dashboard_admin.GUI.Dashboard_ad;
import com.ComponentandDatabase.Components.MyButton;
import com.ComponentandDatabase.Components.MyPanel;

import javax.swing.*;
import java.awt.*;

public class InventoryManagement extends JFrame {
    private Form_Inventory formInventory;
    
    public InventoryManagement() {
        initComponents();
        init();
    }
    
    private void initComponents() {
        setTitle("Inventory Management System");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(1550, 900);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
    }
    
    private void init() {
        // Header panel
        JPanel headerPanel = new MyPanel(Color.decode("#1976D2"));
        headerPanel.setLayout(new BorderLayout());
        headerPanel.setPreferredSize(new Dimension(1550, 60));
        
        JLabel lblTitle = new JLabel("INVENTORY MANAGEMENT SYSTEM", JLabel.CENTER);
        lblTitle.setFont(new Font("Arial", Font.BOLD, 24));
        lblTitle.setForeground(Color.WHITE);
        headerPanel.add(lblTitle, BorderLayout.CENTER);
        
        // Admin info
        JPanel adminPanel = new JPanel();
        adminPanel.setBackground(Color.decode("#1976D2"));
        adminPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
        
        JLabel lblAdmin = new JLabel("Admin: " + Dashboard_ad.getAdminName(Dashboard_ad.adminID));
        lblAdmin.setFont(new Font("Arial", Font.BOLD, 14));
        lblAdmin.setForeground(Color.WHITE);
        adminPanel.add(lblAdmin);
        
        MyButton btnLogout = new MyButton("Logout", 20);
        btnLogout.setBackgroundColor(Color.decode("#F44336"));
        btnLogout.setHoverColor(Color.decode("#D32F2F"));
        btnLogout.setPressedColor(Color.decode("#C62828"));
        btnLogout.setFont(new Font("Arial", Font.BOLD, 12));
        btnLogout.setForeground(Color.WHITE);
        btnLogout.addActionListener(e -> {
            dispose();
            // Return to login or main dashboard
        });
        adminPanel.add(btnLogout);
        
        headerPanel.add(adminPanel, BorderLayout.EAST);
        
        add(headerPanel, BorderLayout.NORTH);
        
        // Main content
        formInventory = new Form_Inventory();
        add(formInventory, BorderLayout.CENTER);
        
        // Footer panel
        JPanel footerPanel = new MyPanel(Color.decode("#424242"));
        footerPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
        footerPanel.setPreferredSize(new Dimension(1550, 40));
        
        JLabel lblFooter = new JLabel("© 2024 Inventory Management System - All Rights Reserved");
        lblFooter.setFont(new Font("Arial", Font.PLAIN, 12));
        lblFooter.setForeground(Color.WHITE);
        footerPanel.add(lblFooter);
        
        add(footerPanel, BorderLayout.SOUTH);
    }
}
