package com.Admin.insurance.GUI;

import com.ComponentandDatabase.Components.MyButton;
import com.ComponentandDatabase.Components.MyTable;
import com.ComponentandDatabase.Components.CustomDialog;
import com.Admin.insurance.BUS.BUS_Warranty;
import com.Admin.insurance.DTO.DTO_Insurance;
import com.Admin.insurance.DTO.DTO_InsuranceDetails;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static com.ComponentandDatabase.Components.UIConstants.*;

public class InsuranceDetailsDialog extends JDialog {
    
    private MyTable tableInsuranceDetails;
    private DefaultTableModel modelInsuranceDetails;
    private MyButton btnClose, btnExportPDF;
    private String insuranceNo;
    private BUS_Warranty busWarranty;
    
    public InsuranceDetailsDialog(JFrame parent, String insuranceNo) {
        super(parent, "Insurance Details", true);
        this.insuranceNo = insuranceNo;
        this.busWarranty = new BUS_Warranty();
        
        initComponents();
        init();
        loadInsuranceDetails();
    }
    
    private void initComponents() {
        setSize(800, 600);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        setResizable(true);
    }
    
    private void init() {
        // Header
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(PRIMARY_COLOR);
        headerPanel.setPreferredSize(new Dimension(800, 60));
        headerPanel.setLayout(new BorderLayout());
        
        JLabel lblTitle = new JLabel("Insurance Details - " + insuranceNo);
        lblTitle.setFont(FONT_TITLE_MEDIUM);
        lblTitle.setForeground(Color.WHITE);
        lblTitle.setHorizontalAlignment(SwingConstants.CENTER);
        headerPanel.add(lblTitle, BorderLayout.CENTER);
        
        add(headerPanel, BorderLayout.NORTH);
        
        // Main content
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Insurance summary
        JPanel summaryPanel = createSummaryPanel();
        mainPanel.add(summaryPanel, BorderLayout.NORTH);
        
        // Products table
        JPanel tablePanel = createTablePanel();
        mainPanel.add(tablePanel, BorderLayout.CENTER);
        
        // Buttons
        JPanel buttonPanel = createButtonPanel();
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        add(mainPanel, BorderLayout.CENTER);
    }
    
    private JPanel createSummaryPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(PRIMARY_COLOR, 1),
            "Insurance Summary",
            javax.swing.border.TitledBorder.LEFT,
            javax.swing.border.TitledBorder.TOP,
            FONT_TITLE_SMALL,
            PRIMARY_COLOR
        ));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        
        // Load insurance data
        try {
            DTO_Insurance insurance = busWarranty.getInsuranceByNo(insuranceNo);
            if (insurance != null) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                
                // Insurance No
                gbc.gridx = 0; gbc.gridy = 0;
                panel.add(new JLabel("Insurance No:"), gbc);
                gbc.gridx = 1;
                panel.add(new JLabel(insurance.getInsuranceNo()), gbc);
                
                // Invoice No
                gbc.gridx = 2; gbc.gridy = 0;
                panel.add(new JLabel("Invoice No:"), gbc);
                gbc.gridx = 3;
                panel.add(new JLabel(insurance.getInvoiceNo()), gbc);
                
                // Start Date
                gbc.gridx = 0; gbc.gridy = 1;
                panel.add(new JLabel("Start Date:"), gbc);
                gbc.gridx = 1;
                panel.add(new JLabel(insurance.getStartDateInsurance().format(formatter)), gbc);
                
                // End Date
                gbc.gridx = 2; gbc.gridy = 1;
                panel.add(new JLabel("End Date:"), gbc);
                gbc.gridx = 3;
                panel.add(new JLabel(insurance.getEndDateInsurance().format(formatter)), gbc);
                
                // Description
                gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 4;
                panel.add(new JLabel("Description: " + insurance.getDescribleCustomer()), gbc);
            }
        } catch (Exception e) {
            panel.add(new JLabel("Error loading insurance details: " + e.getMessage()), gbc);
        }
        
        return panel;
    }
    
    private JPanel createTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(PRIMARY_COLOR, 1),
            "Insured Products",
            javax.swing.border.TitledBorder.LEFT,
            javax.swing.border.TitledBorder.TOP,
            FONT_TITLE_SMALL,
            PRIMARY_COLOR
        ));
        
        // Create table
        String[] columns = {"Product ID", "Product Name", "Warranty Period", 
                           "Start Date", "End Date", "Status"};
        modelInsuranceDetails = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Read-only table
            }
        };
        
        tableInsuranceDetails = createStyledTable(modelInsuranceDetails);
        JScrollPane scrollPane = new JScrollPane(tableInsuranceDetails);
        scrollPane.setPreferredSize(new Dimension(750, 300));
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        panel.setBackground(Color.WHITE);
        
        btnExportPDF = new MyButton("Export PDF", 20);
        btnExportPDF.setBackgroundColor(PRIMARY_COLOR);
        btnExportPDF.setHoverColor(PRIMARY_HOVER);
        btnExportPDF.setForeground(Color.WHITE);
        btnExportPDF.setPreferredSize(new Dimension(120, 35));
        btnExportPDF.addActionListener(e -> exportToPDF());
        panel.add(btnExportPDF);
        
        btnClose = new MyButton("Close", 20);
        btnClose.setBackgroundColor(Color.decode("#6C757D"));
        btnClose.setHoverColor(Color.decode("#5A6268"));
        btnClose.setForeground(Color.WHITE);
        btnClose.setPreferredSize(new Dimension(100, 35));
        btnClose.addActionListener(e -> dispose());
        panel.add(btnClose);
        
        return panel;
    }
    
    private void loadInsuranceDetails() {
        try {
            List<DTO_InsuranceDetails> details = busWarranty.getInsuranceDetailsByNo(insuranceNo);
            modelInsuranceDetails.setRowCount(0);
            
            if (details != null) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                for (DTO_InsuranceDetails detail : details) {
                    Object[] row = {
                        detail.getProductId(),
                        "Product Name", // TODO: Load actual product name
                        "12 months", // TODO: Calculate actual warranty period
                        detail.getDateInsurance().format(formatter),
                        "End Date", // TODO: Calculate end date
                        "Active"
                    };
                    modelInsuranceDetails.addRow(row);
                }
            }
        } catch (Exception e) {
            CustomDialog.showError("Error loading insurance details: " + e.getMessage());
        }
    }
    
    private void exportToPDF() {
        try {
            // TODO: Implement PDF export
            CustomDialog.showSuccess("PDF export functionality will be implemented!");
        } catch (Exception e) {
            CustomDialog.showError("Export failed: " + e.getMessage());
        }
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
            FONT_TABLE_CONTENT,
            FONT_TABLE_HEADER
        );
    }
}
