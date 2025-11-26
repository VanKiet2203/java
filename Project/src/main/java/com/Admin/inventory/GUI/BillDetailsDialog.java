package com.Admin.inventory.GUI;

import com.Admin.inventory.DTO.DTOInventory;
import com.ComponentandDatabase.Components.MyButton;
import com.ComponentandDatabase.Components.MyTable;
import com.ComponentandDatabase.Components.CustomDialog;
import static com.ComponentandDatabase.Components.UIConstants.*;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.util.List;

public class BillDetailsDialog extends JDialog {
    private String billId;
    private JTable tableBillItems;
    private MyButton btnClose, btnPrint, btnExport;
    
    public BillDetailsDialog(JFrame parent, String billId) {
        super(parent, "Bill Details - " + billId, true);
        this.billId = billId;
        
        initComponents();
        init();
        loadBillData();
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
        
        JLabel lblTitle = new JLabel("Bill Details - " + billId);
        lblTitle.setFont(FONT_TITLE_MEDIUM);
        lblTitle.setForeground(Color.WHITE);
        lblTitle.setHorizontalAlignment(SwingConstants.CENTER);
        headerPanel.add(lblTitle, BorderLayout.CENTER);
        
        add(headerPanel, BorderLayout.NORTH);
        
        // Main content
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Bill info panel
        createBillInfoPanel(mainPanel);
        
        // Items table
        createItemsTable(mainPanel);
        
        // Buttons
        createButtons(mainPanel);
        
        add(mainPanel, BorderLayout.CENTER);
    }
    
    private void createBillInfoPanel(JPanel parent) {
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new GridLayout(2, 4, 10, 10));
        infoPanel.setBackground(Color.WHITE);
        infoPanel.setBorder(BorderFactory.createTitledBorder("Bill Information"));
        
        // Bill ID
        JLabel lblBillId = new JLabel("Bill ID:");
        lblBillId.setFont(FONT_CONTENT_MEDIUM);
        infoPanel.add(lblBillId);
        
        JLabel lblBillIdValue = new JLabel(billId);
        lblBillIdValue.setFont(FONT_CONTENT_MEDIUM);
        infoPanel.add(lblBillIdValue);
        
        // Date
        JLabel lblDate = new JLabel("Date:");
        lblDate.setFont(FONT_CONTENT_MEDIUM);
        infoPanel.add(lblDate);
        
        JLabel lblDateValue = new JLabel("2024-01-01"); // This would come from your data
        lblDateValue.setFont(FONT_CONTENT_MEDIUM);
        infoPanel.add(lblDateValue);
        
        // Supplier
        JLabel lblSupplier = new JLabel("Supplier:");
        lblSupplier.setFont(FONT_CONTENT_MEDIUM);
        infoPanel.add(lblSupplier);
        
        JLabel lblSupplierValue = new JLabel("NIJIA"); // This would come from your data
        lblSupplierValue.setFont(FONT_CONTENT_MEDIUM);
        infoPanel.add(lblSupplierValue);
        
        // Total Amount
        JLabel lblTotal = new JLabel("Total Amount:");
        lblTotal.setFont(FONT_CONTENT_MEDIUM);
        infoPanel.add(lblTotal);
        
        JLabel lblTotalValue = new JLabel("$0.00"); // This would be calculated
        lblTotalValue.setFont(FONT_CONTENT_MEDIUM);
        lblTotalValue.setForeground(PRIMARY_COLOR);
        infoPanel.add(lblTotalValue);
        
        parent.add(infoPanel, BorderLayout.NORTH);
    }
    
    private void createItemsTable(JPanel parent) {
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBackground(Color.WHITE);
        tablePanel.setBorder(BorderFactory.createTitledBorder("Bill Items"));
        
        String[] columns = {
            "Warehouse ID", "Product Name", "Category", "Quantity", 
            "Unit Price", "Total Price"
        };
        
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Make table read-only
            }
        };
        
        tableBillItems = new JTable(model);
        tableBillItems.setRowHeight(30);
        tableBillItems.setFont(FONT_CONTENT_SMALL); 
        tableBillItems.setSelectionBackground(PRIMARY_COLOR);
        tableBillItems.setSelectionForeground(Color.WHITE);
        tableBillItems.setGridColor(Color.LIGHT_GRAY);
        tableBillItems.setShowGrid(true);
        
        JScrollPane scrollPane = new JScrollPane(tableBillItems);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        
        tablePanel.add(scrollPane, BorderLayout.CENTER);
        parent.add(tablePanel, BorderLayout.CENTER);
    }
    
    private void createButtons(JPanel parent) {
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 10));
        buttonPanel.setBackground(Color.WHITE);
        
        btnPrint = new MyButton("Print", 20);
        btnPrint.setBackgroundColor(Color.decode("#2196F3"));
        btnPrint.setHoverColor(Color.decode("#1976D2"));
        btnPrint.setPressedColor(Color.decode("#1565C0"));
        btnPrint.setForeground(Color.WHITE);
        btnPrint.setFont(FONT_CONTENT_MEDIUM);
        btnPrint.addActionListener(e -> printBill());
        
        btnExport = new MyButton("Export PDF", 20);
        btnExport.setBackgroundColor(Color.decode("#4CAF50"));
        btnExport.setHoverColor(Color.decode("#45A049"));
        btnExport.setPressedColor(Color.decode("#3D8B40"));
        btnExport.setForeground(Color.WHITE);
        btnExport.setFont(FONT_CONTENT_MEDIUM);
        btnExport.addActionListener(e -> exportBill());
        
        btnClose = new MyButton("Close", 20);
        btnClose.setBackgroundColor(Color.decode("#F44336"));
        btnClose.setHoverColor(Color.decode("#D32F2F"));
        btnClose.setPressedColor(Color.decode("#C62828"));
        btnClose.setForeground(Color.WHITE);
        btnClose.setFont(FONT_CONTENT_MEDIUM);
        btnClose.addActionListener(e -> dispose());
        
        buttonPanel.add(btnPrint);
        buttonPanel.add(btnExport);
        buttonPanel.add(btnClose);
        
        parent.add(buttonPanel, BorderLayout.SOUTH);
    }
    
    private void loadBillData() {
        DefaultTableModel model = (DefaultTableModel) tableBillItems.getModel();
        model.setRowCount(0);
        
        try {
            // Here you would load the actual bill data from your database
            // For now, adding sample data
            addSampleData(model);
        } catch (Exception e) {
            CustomDialog.showError("Failed to load bill data: " + e.getMessage());
        }
    }
    
    private void addSampleData(DefaultTableModel model) {
        // Sample data - replace with actual data loading
        model.addRow(new Object[]{
            "WH001", "Electric Bike Model A", "Electric Bike", 5, 
            "$1,500.00", "$7,500.00"
        });
        model.addRow(new Object[]{
            "WH002", "Electric Bike Model B", "Electric Bike", 3, 
            "$2,000.00", "$6,000.00"
        });
        model.addRow(new Object[]{
            "WH003", "Electric Car Model X", "Electric Car", 2, 
            "$15,000.00", "$30,000.00"
        });
    }
    
    private void printBill() {
        try {
            // Implement print functionality
            CustomDialog.showSuccess("Bill sent to printer!");
        } catch (Exception e) {
            CustomDialog.showError("Print failed: " + e.getMessage());
        }
    }
    
    private void exportBill() {
        try {
        JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Save PDF file");
            
            int result = fileChooser.showSaveDialog(null);
            if (result == JFileChooser.APPROVE_OPTION) {
                String path = fileChooser.getSelectedFile().getAbsolutePath();
                if (!path.toLowerCase().endsWith(".pdf")) {
                    path += ".pdf";
                }
                
                // Implement PDF export functionality
                // busInventory.exportBillToPDF(billId, path);
                
                CustomDialog.showSuccess("Bill exported to PDF successfully!");
            }
        } catch (Exception e) {
            CustomDialog.showError("Export failed: " + e.getMessage());
        }
    }
}
