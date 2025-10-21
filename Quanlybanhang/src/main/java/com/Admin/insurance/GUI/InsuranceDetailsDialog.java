package com.Admin.insurance.GUI;

import com.ComponentandDatabase.Components.MyButton;
import com.ComponentandDatabase.Components.MyTable;
import com.ComponentandDatabase.Components.CustomDialog;
import com.Admin.insurance.BUS.BUS_Warranty;
import com.Admin.insurance.DTO.DTO_Insurance;
import com.Admin.insurance.DTO.DTO_InsuranceDetails;
import static com.ComponentandDatabase.Components.UIConstants.*;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class InsuranceDetailsDialog extends JDialog {
    private String insuranceNo;
    private DTO_Insurance insurance;
    private List<DTO_InsuranceDetails> insuranceDetails;
    
    private JPanel mainPanel;
    private MyTable tableDetails;
    private DefaultTableModel modelDetails;
    private MyButton btnClose, btnExportPDF;
    
    public InsuranceDetailsDialog(JFrame parent, String insuranceNo) {
        super(parent, "Insurance Details", true);
        this.insuranceNo = insuranceNo;
        initComponents();
        loadInsuranceDetails();
    }
    
    private void initComponents() {
        setSize(800, 600);
        setLocationRelativeTo(getParent());
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        
        mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(Color.WHITE);
        add(mainPanel);
        
        // Title
        JLabel lblTitle = new JLabel("Insurance Details - " + insuranceNo);
        lblTitle.setFont(FONT_TITLE_MEDIUM);
        lblTitle.setForeground(PRIMARY_COLOR);
        lblTitle.setHorizontalAlignment(SwingConstants.CENTER);
        lblTitle.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        mainPanel.add(lblTitle, BorderLayout.NORTH);
        
        // Insurance info panel
        createInsuranceInfoPanel();
        
        // Details table
        createDetailsTable();
        
        // Button panel
        createButtonPanel();
    }
    
    private void createInsuranceInfoPanel() {
        JPanel infoPanel = new JPanel(new GridLayout(0, 2, 10, 5));
        infoPanel.setBackground(Color.WHITE);
        infoPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(PRIMARY_COLOR, 2),
            "Insurance Information",
            0, 0,
            FONT_TITLE_SMALL,
            PRIMARY_COLOR
        ));
        
        // Add info labels (will be populated when data is loaded)
        infoPanel.add(new JLabel("Insurance No:"));
        infoPanel.add(new JLabel(""));
        infoPanel.add(new JLabel("Invoice No:"));
        infoPanel.add(new JLabel(""));
        infoPanel.add(new JLabel("Customer ID:"));
        infoPanel.add(new JLabel(""));
        infoPanel.add(new JLabel("Start Date:"));
        infoPanel.add(new JLabel(""));
        infoPanel.add(new JLabel("End Date:"));
        infoPanel.add(new JLabel(""));
        infoPanel.add(new JLabel("Status:"));
        infoPanel.add(new JLabel(""));
        
        mainPanel.add(infoPanel, BorderLayout.NORTH);
    }
    
    private void createDetailsTable() {
        String[] columnNames = {
            "Product ID", "Product Name", "Description", "Date Insurance", 
            "Time Insurance", "Status"
        };
        
        modelDetails = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Read-only table
            }
        };
        
        tableDetails = new MyTable(
            modelDetails,
            Color.WHITE,
            TEXT_PRIMARY,
            Color.decode("#E8F5E9"),
            Color.BLACK,
            PRIMARY_COLOR,
            Color.WHITE,
            FONT_TABLE_CONTENT,
            FONT_TABLE_HEADER
        );
        
        tableDetails.setRowHeight(30);
        
        JScrollPane scrollPane = new JScrollPane(tableDetails);
        scrollPane.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(PRIMARY_COLOR, 2),
            "Insurance Details",
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
        
        btnExportPDF = new MyButton("Export PDF", 20);
        styleDangerButton(btnExportPDF);
        btnExportPDF.addActionListener(e -> exportToPDF());
        buttonPanel.add(btnExportPDF);
        
        btnClose = new MyButton("Close", 20);
        stylePrimaryButton(btnClose);
        btnClose.addActionListener(e -> dispose());
        buttonPanel.add(btnClose);
        
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
    }
    
    private void loadInsuranceDetails() {
        try {
            BUS_Warranty busWarranty = new BUS_Warranty();
            
            // Load insurance info
            insurance = busWarranty.getInsuranceByNo(insuranceNo);
            if (insurance != null) {
                updateInsuranceInfo();
            }
            
            // Load insurance details
            insuranceDetails = busWarranty.getInsuranceDetailsByNo(insuranceNo);
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");
            
            modelDetails.setRowCount(0);
            
            if (insuranceDetails != null && !insuranceDetails.isEmpty()) {
                for (DTO_InsuranceDetails detail : insuranceDetails) {
                    Object[] row = {
                        detail.getProductId(),
                        "Product Name", // Will be loaded from Product table
                        detail.getDescription(),
                        detail.getDateInsurance().format(dateFormatter),
                        detail.getTimeInsurance().format(timeFormatter),
                        "Active"
                    };
                    modelDetails.addRow(row);
                }
            }
            
            tableDetails.adjustColumnWidths();
            
        } catch (Exception e) {
            CustomDialog.showError("Failed to load insurance details: " + e.getMessage());
        }
    }
    
    private void updateInsuranceInfo() {
        if (insurance != null) {
            // Update the info panel with actual data
            JPanel infoPanel = (JPanel) mainPanel.getComponent(0);
            infoPanel.removeAll();
            infoPanel.setLayout(new GridLayout(0, 2, 10, 5));
            
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            
            infoPanel.add(new JLabel("Insurance No:"));
            infoPanel.add(new JLabel(insurance.getInsuranceNo()));
            infoPanel.add(new JLabel("Invoice No:"));
            infoPanel.add(new JLabel(insurance.getInvoiceNo()));
            infoPanel.add(new JLabel("Customer ID:"));
            infoPanel.add(new JLabel(insurance.getCustomerId()));
            infoPanel.add(new JLabel("Start Date:"));
            infoPanel.add(new JLabel(insurance.getStartDateInsurance().format(dateFormatter)));
            infoPanel.add(new JLabel("End Date:"));
            infoPanel.add(new JLabel(insurance.getEndDateInsurance().format(dateFormatter)));
            infoPanel.add(new JLabel("Status:"));
            infoPanel.add(new JLabel("Active"));
            
            infoPanel.revalidate();
            infoPanel.repaint();
        }
    }
    
    private void exportToPDF() {
        try {
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
    
    private void styleDangerButton(MyButton button) {
        button.setBackgroundColor(Color.decode("#F44336"));
        button.setHoverColor(Color.decode("#D32F2F"));
        button.setPressedColor(Color.decode("#C62828"));
        button.setForeground(Color.WHITE);
        button.setFont(FONT_CONTENT_MEDIUM);
    }
}
