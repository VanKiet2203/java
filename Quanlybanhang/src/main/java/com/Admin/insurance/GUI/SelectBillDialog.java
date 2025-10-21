package com.Admin.insurance.GUI;

import com.ComponentandDatabase.Components.MyButton;
import com.ComponentandDatabase.Components.MyTable;
import com.ComponentandDatabase.Components.CustomDialog;
import com.Admin.export.BUS.BUS_ExportBill;
import com.Admin.export.DTO.DTO_BillExport;
import com.Admin.dashboard_admin.GUI.Dashboard_ad;
import static com.ComponentandDatabase.Components.UIConstants.*;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.time.LocalDate;
import java.util.List;

public class SelectBillDialog extends JDialog {
    private String selectedInvoiceNo;
    private String selectedCustomerId;
    private String selectedAdminId;
    
    private JPanel mainPanel;
    private MyTable tableBills;
    private DefaultTableModel modelBills;
    private MyButton btnSelect, btnCancel;
    
    public SelectBillDialog(JFrame parent) {
        super(parent, "Select Export Bill for Insurance", true);
        initComponents();
        loadBillData();
    }
    
    private void initComponents() {
        setSize(900, 500);
        setLocationRelativeTo(getParent());
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        
        mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(Color.WHITE);
        add(mainPanel);
        
        // Title
        JLabel lblTitle = new JLabel("Select Export Bill for Insurance");
        lblTitle.setFont(FONT_TITLE_MEDIUM);
        lblTitle.setForeground(PRIMARY_COLOR);
        lblTitle.setHorizontalAlignment(SwingConstants.CENTER);
        lblTitle.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        mainPanel.add(lblTitle, BorderLayout.NORTH);
        
        // Bill table
        createBillTable();
        
        // Button panel
        createButtonPanel();
    }
    
    private void createBillTable() {
        String[] columnNames = {
            "Invoice No", "Customer ID", "Customer Name", "Total Products", 
            "Date Exported", "Insurance Status", "Select"
        };
        
        modelBills = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 6; // Only Select column is editable
            }
        };
        
        tableBills = new MyTable(
            modelBills,
            Color.WHITE,
            TEXT_PRIMARY,
            Color.decode("#E8F5E9"),
            Color.BLACK,
            PRIMARY_COLOR,
            Color.WHITE,
            FONT_TABLE_CONTENT,
            FONT_TABLE_HEADER
        );
        
        tableBills.setRowHeight(30);
        
        // Add selection listener
        tableBills.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int selectedRow = tableBills.getSelectedRow();
                if (selectedRow >= 0) {
                    String insuranceStatus = tableBills.getValueAt(selectedRow, 5).toString();
                    btnSelect.setEnabled("Available for Insurance".equals(insuranceStatus));
                }
            }
        });
        
        JScrollPane scrollPane = new JScrollPane(tableBills);
        scrollPane.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(PRIMARY_COLOR, 2),
            "Available Export Bills",
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
        
        btnSelect = new MyButton("Select Bill", 20);
        stylePrimaryButton(btnSelect);
        btnSelect.setEnabled(false);
        btnSelect.addActionListener(e -> selectBill());
        buttonPanel.add(btnSelect);
        
        btnCancel = new MyButton("Cancel", 20);
        styleDangerButton(btnCancel);
        btnCancel.addActionListener(e -> dispose());
        buttonPanel.add(btnCancel);
        
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
    }
    
    private void loadBillData() {
        try {
            BUS_ExportBill busExportBill = new BUS_ExportBill();
            List<DTO_BillExport> exportedBills = busExportBill.getAllAvailableExportBillsForInsurance();
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            
            modelBills.setRowCount(0);
            
            if (exportedBills != null && !exportedBills.isEmpty()) {
                for (DTO_BillExport bill : exportedBills) {
                    Object[] row = {
                        bill.getInvoiceNo(),
                        bill.getCustomerId(),
                        "Customer Name", // Will be loaded from database
                        bill.getTotalProduct(),
                        LocalDate.now().format(dateFormatter), // Will be loaded from database
                        "Available for Insurance",
                        "Select"
                    };
                    modelBills.addRow(row);
                }
            }
            
            tableBills.adjustColumnWidths();
            
        } catch (Exception e) {
            CustomDialog.showError("Failed to load export bills: " + e.getMessage());
        }
    }
    
    private void selectBill() {
        int selectedRow = tableBills.getSelectedRow();
        if (selectedRow >= 0) {
            selectedInvoiceNo = tableBills.getValueAt(selectedRow, 0).toString();
            selectedCustomerId = tableBills.getValueAt(selectedRow, 1).toString();
            selectedAdminId = Dashboard_ad.adminID; // Get from current session
            
            CustomDialog.showSuccess("Bill selected successfully!\n" +
                                   "Invoice No: " + selectedInvoiceNo + "\n" +
                                   "Customer ID: " + selectedCustomerId);
            
            dispose();
        } else {
            CustomDialog.showError("Please select a bill first!");
        }
    }
    
    public String getSelectedInvoiceNo() {
        return selectedInvoiceNo;
    }
    
    public String getSelectedCustomerId() {
        return selectedCustomerId;
    }
    
    public String getSelectedAdminId() {
        return selectedAdminId;
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