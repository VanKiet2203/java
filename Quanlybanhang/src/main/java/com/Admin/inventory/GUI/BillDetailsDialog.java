package com.Admin.inventory.GUI;

import com.Admin.inventory.BUS.BUSInventory;
import com.Admin.inventory.DTO.DTOImportBillDetails;
import com.ComponentandDatabase.Components.MyButton;
import com.ComponentandDatabase.Components.MyTable;
import com.ComponentandDatabase.Components.CustomDialog;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.io.File;

public class BillDetailsDialog extends JDialog {
    private String invoiceNo;
    private BUSInventory busInventory;
    private MyTable tableDetails;
    private JLabel lblInvoiceNo, lblTotalProducts, lblTotalPrice, lblDate, lblTime;
    
    public BillDetailsDialog(JFrame parent, String invoiceNo) {
        super(parent, "Bill Details - " + invoiceNo, true);
        this.invoiceNo = invoiceNo;
        this.busInventory = new BUSInventory();
        
        initComponents();
        loadBillDetails();
    }
    
    private void initComponents() {
        setLayout(new BorderLayout());
        setSize(800, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        
        // Panel header
        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new GridLayout(2, 3, 10, 10));
        headerPanel.setBorder(BorderFactory.createTitledBorder("Bill Information"));
        headerPanel.setBackground(Color.WHITE);
        
        // Invoice No
        JLabel lblInvoiceNoLabel = new JLabel("Invoice No:");
        lblInvoiceNoLabel.setFont(new Font("Arial", Font.BOLD, 12));
        headerPanel.add(lblInvoiceNoLabel);
        
        lblInvoiceNo = new JLabel(invoiceNo);
        lblInvoiceNo.setFont(new Font("Arial", Font.PLAIN, 12));
        headerPanel.add(lblInvoiceNo);
        
        // Total Products
        JLabel lblTotalProductsLabel = new JLabel("Total Products:");
        lblTotalProductsLabel.setFont(new Font("Arial", Font.BOLD, 12));
        headerPanel.add(lblTotalProductsLabel);
        
        lblTotalProducts = new JLabel("0");
        lblTotalProducts.setFont(new Font("Arial", Font.PLAIN, 12));
        headerPanel.add(lblTotalProducts);
        
        // Total Price
        JLabel lblTotalPriceLabel = new JLabel("Total Price:");
        lblTotalPriceLabel.setFont(new Font("Arial", Font.BOLD, 12));
        headerPanel.add(lblTotalPriceLabel);
        
        lblTotalPrice = new JLabel("0 VND");
        lblTotalPrice.setFont(new Font("Arial", Font.PLAIN, 12));
        headerPanel.add(lblTotalPrice);
        
        // Date
        JLabel lblDateLabel = new JLabel("Date:");
        lblDateLabel.setFont(new Font("Arial", Font.BOLD, 12));
        headerPanel.add(lblDateLabel);
        
        lblDate = new JLabel("");
        lblDate.setFont(new Font("Arial", Font.PLAIN, 12));
        headerPanel.add(lblDate);
        
        // Time
        JLabel lblTimeLabel = new JLabel("Time:");
        lblTimeLabel.setFont(new Font("Arial", Font.BOLD, 12));
        headerPanel.add(lblTimeLabel);
        
        lblTime = new JLabel("");
        lblTime.setFont(new Font("Arial", Font.PLAIN, 12));
        headerPanel.add(lblTime);
        
        add(headerPanel, BorderLayout.NORTH);
        
        // Table for bill details
        String[] columnNames = {"Product ID", "Product Name", "Quantity", "Unit Price", "Total Price", "Date", "Time"};
        DefaultTableModel model = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        tableDetails = new MyTable(
            model,
            Color.WHITE, Color.BLACK, Color.decode("#E3F2FD"), Color.BLACK,
            Color.decode("#1976D2"), Color.WHITE,
            new Font("Arial", Font.PLAIN, 12),
            new Font("Arial", Font.BOLD, 12)
        );
        
        JScrollPane scrollPane = new JScrollPane(tableDetails);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Bill Details"));
        add(scrollPane, BorderLayout.CENTER);
        
        // Panel buttons
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(Color.WHITE);
        
        MyButton btnClose = new MyButton("Close", 20);
        btnClose.setBackgroundColor(Color.decode("#757575"));
        btnClose.setHoverColor(Color.decode("#616161"));
        btnClose.setPressedColor(Color.decode("#424242"));
        btnClose.setFont(new Font("Arial", Font.BOLD, 12));
        btnClose.setForeground(Color.WHITE);
        btnClose.addActionListener(e -> dispose());
        buttonPanel.add(btnClose);
        
        MyButton btnExportPDF = new MyButton("Export PDF", 20);
        btnExportPDF.setBackgroundColor(Color.decode("#F44336"));
        btnExportPDF.setHoverColor(Color.decode("#D32F2F"));
        btnExportPDF.setPressedColor(Color.decode("#C62828"));
        btnExportPDF.setFont(new Font("Arial", Font.BOLD, 12));
        btnExportPDF.setForeground(Color.WHITE);
        btnExportPDF.addActionListener(e -> exportToPDF());
        buttonPanel.add(btnExportPDF);
        
        MyButton btnExportExcel = new MyButton("Export Excel", 20);
        btnExportExcel.setBackgroundColor(Color.decode("#4CAF50"));
        btnExportExcel.setHoverColor(Color.decode("#45a049"));
        btnExportExcel.setPressedColor(Color.decode("#3d8b40"));
        btnExportExcel.setFont(new Font("Arial", Font.BOLD, 12));
        btnExportExcel.setForeground(Color.WHITE);
        btnExportExcel.addActionListener(e -> exportToExcel());
        buttonPanel.add(btnExportExcel);
        
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    private void loadBillDetails() {
        List<DTOImportBillDetails> details = busInventory.getImportBillDetails(invoiceNo);
        DefaultTableModel model = (DefaultTableModel) tableDetails.getModel();
        model.setRowCount(0);
        
        int totalProducts = 0;
        double totalPrice = 0.0;
        
        for (DTOImportBillDetails detail : details) {
            Object[] row = {
                detail.getProductId(),
                detail.getProductName(),
                detail.getQuantity(),
                String.format("%,.0f VND", detail.getUnitPrice().doubleValue()),
                String.format("%,.0f VND", detail.getTotalPrice().doubleValue()),
                detail.getDateImported(),
                detail.getTimeImported()
            };
            model.addRow(row);
            
            totalProducts += detail.getQuantity();
            totalPrice += detail.getTotalPrice().doubleValue();
            
            // Set date and time from first record
            if (lblDate.getText().isEmpty()) {
                lblDate.setText(detail.getDateImported().toString());
                lblTime.setText(detail.getTimeImported().toString());
            }
        }
        
        lblTotalProducts.setText(String.valueOf(totalProducts));
        lblTotalPrice.setText(String.format("%,.0f VND", totalPrice));
        
        tableDetails.adjustColumnWidths();
    }
    
    private void exportToPDF() {
        // TODO: Implement PDF export for bill details
        CustomDialog.showSuccess("PDF export feature will be implemented!");
    }
    
    private void exportToExcel() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save Excel File");
        fileChooser.setSelectedFile(new File("Bill_" + invoiceNo + ".xlsx"));
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Excel Files (*.xlsx)", "xlsx"));

        int userSelection = fileChooser.showSaveDialog(null);
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();
            String filePath = fileToSave.getAbsolutePath();
            if (!filePath.endsWith(".xlsx")) {
                filePath += ".xlsx";
            }

            boolean success = busInventory.exportBillToExcel(invoiceNo, filePath);
            if (success) {
                CustomDialog.showSuccess("Excel file exported successfully!");
            } else {
                CustomDialog.showError("Failed to export Excel file!");
            }
        }
    }
}
