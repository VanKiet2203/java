package com.Admin.inventory.GUI;

import com.Admin.inventory.BUS.BUSInventory;
import com.ComponentandDatabase.Components.MyButton;
import com.ComponentandDatabase.Components.MyCombobox;
import com.ComponentandDatabase.Components.MyPanel;
import com.ComponentandDatabase.Components.MyTextField;
import com.ComponentandDatabase.Components.CustomDialog;
import static com.ComponentandDatabase.Components.UIConstants.*;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.BorderLayout;
import java.io.File;
import javax.swing.SwingConstants;

public class Form_Inventory extends JPanel {
    private JPanel panel, panelSearch;
    private MyButton bntRefresh, bntSearch, bntAddNew, bntImportInventory, bntExportInventory, bntExportExcelBill, bntExportPDFBill, bntViewBills, bntReimportItem;
    private MyTextField txtSearch;
    private MyCombobox<String> cmbSearch;
    private JTable tableInventory, tableBills;
    private BUSInventory busInventory;
    private JTabbedPane tabbedPane;
    
    public Form_Inventory() {
        initComponents();
        init();
    }
    
    private void initComponents() {
        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(1200, 700));
        setBackground(BG_WHITE);
    }
    
    private void init() {
        // Tạo main panel với scroll
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(null);
        mainPanel.setPreferredSize(new Dimension(1200, 900));
        mainPanel.setBackground(Color.WHITE);
        
        // Tạo scroll pane
        JScrollPane scrollPane = new JScrollPane(mainPanel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setBorder(null);
        
        add(scrollPane, BorderLayout.CENTER);
        panel = mainPanel;
        
        // Title
        JLabel lblTitle = new JLabel("INVENTORY MANAGEMENT");
        lblTitle.setFont(FONT_TITLE_LARGE);
        lblTitle.setForeground(PRIMARY_COLOR);
        lblTitle.setBounds(20, 10, 400, 40);
        panel.add(lblTitle);
        
        // Search Panel
        createSearchPanel();
        
        // Action Buttons Panel
        createActionButtonsPanel();
        
        // Tabbed Pane for different views
        createTabbedPane();
        
        // Initialize data
        initializeData();
    }
    
    private void createSearchPanel() {
        panelSearch = new MyPanel(Color.WHITE);
        panelSearch.setLayout(null);
        panelSearch.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(PRIMARY_COLOR, 2),
            "Search",
            0, 0,
            FONT_TITLE_SMALL,
            PRIMARY_COLOR
        ));
        panelSearch.setBounds(20, 60, 1160, 100);
        
        // Search components
        String[] searchItems = {"Warehouse ID", "Product Name", "Category", "Supplier"};
        cmbSearch = new MyCombobox<>(searchItems);
        cmbSearch.setBounds(20, 30, 150, 35);
        cmbSearch.setCustomFont(FONT_CONTENT_MEDIUM);
        
        txtSearch = new MyTextField();
        txtSearch.setHint("Search something...");
        txtSearch.setBounds(180, 30, 300, 35);
        txtSearch.setTextFont(FONT_CONTENT_MEDIUM);
        txtSearch.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(PRIMARY_COLOR, 1),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        
        bntSearch = new MyButton("Search", 20);
        stylePrimaryButton(bntSearch);
        bntSearch.setBounds(490, 30, 120, 35);
        bntSearch.setButtonIcon("src\\main\\resources\\Icons\\Admin_icon\\search.png", 25, 25, 5, SwingConstants.RIGHT, SwingConstants.CENTER);
        bntSearch.addActionListener(e -> performSearch());
        
        
        panelSearch.add(cmbSearch);
        panelSearch.add(txtSearch);
        panelSearch.add(bntSearch);
        panel.add(panelSearch);
    }
    
    private void createActionButtonsPanel() {
        JPanel actionPanel = new MyPanel(Color.WHITE);
        actionPanel.setLayout(null);
        actionPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(PRIMARY_COLOR, 2),
            "Actions",
            0, 0,
            FONT_TITLE_SMALL,
            PRIMARY_COLOR
        ));
        actionPanel.setBounds(20, 190, 1160, 120);
        
        // Hàng 1: Các chức năng cơ bản và thường dùng
        // 1. Add New Item button - Thêm sản phẩm mới
        bntAddNew = new MyButton("New Item", 20);
        styleSuccessButton(bntAddNew);
        bntAddNew.setBounds(20, 30, 150, 35);
        bntAddNew.setButtonIcon("src\\main\\resources\\Icons\\Admin_icon\\new.png", 25, 25, 5, SwingConstants.RIGHT, SwingConstants.CENTER);
        bntAddNew.addActionListener(e -> addNewInventoryItem());
        actionPanel.add(bntAddNew);
        
        // 2. Import Existing Item button - Nhập thêm sản phẩm đã có
        bntReimportItem = new MyButton("Reimport Item", 20);
        styleWarningButton(bntReimportItem);
        bntReimportItem.setBounds(180, 30, 150, 35);
        bntReimportItem.setButtonIcon("src\\main\\resources\\Icons\\Admin_icon\\import.png", 25, 25, 5, SwingConstants.RIGHT, SwingConstants.CENTER);
        bntReimportItem.addActionListener(e -> reimportExistingItem());
        actionPanel.add(bntReimportItem);
        
        // 3. Import Inventory button - Import từ file Excel
        bntImportInventory = new MyButton("Import File", 20);
        styleSuccessButton(bntImportInventory);
        bntImportInventory.setBounds(340, 30, 150, 35);
        bntImportInventory.setButtonIcon("src\\main\\resources\\Icons\\Admin_icon\\import.png", 25, 25, 5, SwingConstants.RIGHT, SwingConstants.CENTER);
        bntImportInventory.addActionListener(e -> importInventory());
        actionPanel.add(bntImportInventory);
        
        // 4. Refresh button - Làm mới dữ liệu
        bntRefresh = new MyButton("Refresh", 20);
        styleInfoButton(bntRefresh);
        bntRefresh.setBounds(500, 30, 150, 35);
        bntRefresh.setButtonIcon("src\\main\\resources\\Icons\\Admin_icon\\refresh.png", 25, 25, 5, SwingConstants.RIGHT, SwingConstants.CENTER);
        bntRefresh.addActionListener(e -> refreshData());
        actionPanel.add(bntRefresh);
        
        // Hàng 2: Các chức năng xuất báo cáo và xem dữ liệu
        // 5. Export Inventory button - Xuất danh sách tồn kho
        bntExportInventory = new MyButton("Export File", 20);
        styleInfoButton(bntExportInventory);
        bntExportInventory.setBounds(20, 70, 150, 35);
        bntExportInventory.setButtonIcon("src\\main\\resources\\Icons\\Admin_icon\\Excel.png", 25, 25, 5, SwingConstants.RIGHT, SwingConstants.CENTER);
        bntExportInventory.addActionListener(e -> exportInventory());
        actionPanel.add(bntExportInventory);
        
        // 6. View Bills button - Xem hóa đơn nhập
        bntViewBills = new MyButton("View Bills", 20);
        stylePrimaryButton(bntViewBills);
        bntViewBills.setBounds(180, 70, 150, 35);
        bntViewBills.setButtonIcon("src\\main\\resources\\Icons\\Admin_icon\\bill_export.png", 25, 25, 5, SwingConstants.RIGHT, SwingConstants.CENTER);
        bntViewBills.addActionListener(e -> viewBills());
        actionPanel.add(bntViewBills);
        
        // 7. Export Excel Bill button - Xuất hóa đơn Excel
        bntExportExcelBill = new MyButton("Export Bill", 20);
        styleInfoButton(bntExportExcelBill);
        bntExportExcelBill.setBounds(340, 70, 150, 35);
        bntExportExcelBill.setButtonIcon("src\\main\\resources\\Icons\\Admin_icon\\Excel.png", 25, 25, 5, SwingConstants.RIGHT, SwingConstants.CENTER);
        bntExportExcelBill.addActionListener(e -> exportExcelBillImport());
        actionPanel.add(bntExportExcelBill);
        
        // 8. Export PDF Bill button - Xuất hóa đơn PDF
        bntExportPDFBill = new MyButton("Export PDF", 20);
        styleDangerButton(bntExportPDFBill);
        bntExportPDFBill.setBounds(500, 70, 150, 35);
        bntExportPDFBill.setButtonIcon("src\\main\\resources\\Icons\\Admin_icon\\bill_export.png", 25, 25, 5, SwingConstants.RIGHT, SwingConstants.CENTER);
        bntExportPDFBill.addActionListener(e -> exportPDFBillImport());
        actionPanel.add(bntExportPDFBill);
           
        
        panel.add(actionPanel);
    }
    
    private void createTabbedPane() {
        tabbedPane = new JTabbedPane();
        tabbedPane.setBounds(20, 340, 1160, 400);
        tabbedPane.setFont(FONT_CONTENT_MEDIUM);
        
        // Inventory Tab
        JPanel inventoryPanel = new JPanel(new BorderLayout());
        String[] inventoryColumns = {
            "Warehouse ID", "Product Name", "Category", "Supplier", 
            "Quantity", "Unit Price", "Total Value", "Last Updated"
        };
        DefaultTableModel inventoryModel = new DefaultTableModel(inventoryColumns, 0);
        tableInventory = createStyledTable(inventoryModel);
        JScrollPane inventoryScroll = new JScrollPane(tableInventory);
        inventoryPanel.add(inventoryScroll, BorderLayout.CENTER);
        tabbedPane.addTab("Inventory", inventoryPanel);
        
        // Bills Tab
        JPanel billsPanel = new JPanel(new BorderLayout());
        String[] billsColumns = {
            "Bill ID", "Date", "Supplier", "Total Items", "Total Amount", "Status"
        };
        DefaultTableModel billsModel = new DefaultTableModel(billsColumns, 0);
        tableBills = createStyledTable(billsModel);
        JScrollPane billsScroll = new JScrollPane(tableBills);
        billsPanel.add(billsScroll, BorderLayout.CENTER);
        tabbedPane.addTab("Import Bills", billsPanel);
        
        panel.add(tabbedPane);
    }
    
    private void initializeData() {
        busInventory = new BUSInventory();
        
        
        // Load inventory data
        refreshData();
    }
    
    
    private void performSearch() {
        String searchType = cmbSearch.getSelectedItem().toString();
        String keyword = txtSearch.getText().trim();
        
        DefaultTableModel model = (DefaultTableModel) tableInventory.getModel();
        model.setRowCount(0);
        
        // Implement search logic based on searchType and keyword
        // This would call your BUS layer search method
        try {
            busInventory.searchInventory(keyword, searchType, model);
        } catch (Exception e) {
            CustomDialog.showError("Search failed: " + e.getMessage());
        }
    }
    
    private void refreshData() {
        DefaultTableModel model = (DefaultTableModel) tableInventory.getModel();
        model.setRowCount(0);
        
        try {
            busInventory.loadInventoryData(model);
        } catch (Exception e) {
            CustomDialog.showError("Failed to load data: " + e.getMessage());
        }
    }
    
    private void importInventory() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Choose Excel file to import inventory");
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Excel Files (*.xlsx)", "xlsx"));

        int result = fileChooser.showOpenDialog(null);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            try {
                boolean success = busInventory.importInventory(selectedFile);
                if (success) {
                    CustomDialog.showSuccess("Inventory imported successfully!");
                    refreshData();
                }
            } catch (Exception e) {
                CustomDialog.showError("Import failed: " + e.getMessage());
            }
        }
    }
    
    private void exportInventory() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save inventory Excel file");
        
        int result = fileChooser.showSaveDialog(null);
        if (result == JFileChooser.APPROVE_OPTION) {
            String path = fileChooser.getSelectedFile().getAbsolutePath();
            if (!path.toLowerCase().endsWith(".xlsx")) {
                path += ".xlsx";
            }
            try {
                busInventory.exportInventory(path);
                CustomDialog.showSuccess("Inventory exported successfully!");
            } catch (Exception e) {
                CustomDialog.showError("Export failed: " + e.getMessage());
            }
        }
    }
    
    private void exportExcelBillImport() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save Excel bill import file");
        
        int result = fileChooser.showSaveDialog(null);
        if (result == JFileChooser.APPROVE_OPTION) {
            String path = fileChooser.getSelectedFile().getAbsolutePath();
            if (!path.toLowerCase().endsWith(".xlsx")) {
                path += ".xlsx";
            }
            try {
                busInventory.exportExcelBillImport(path);
                CustomDialog.showSuccess("Excel bill export successfully!");
            } catch (Exception e) {
                CustomDialog.showError("Excel export failed: " + e.getMessage());
            }
        }
    }
    
    private void exportPDFBillImport() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save PDF bill import file");
        
        int result = fileChooser.showSaveDialog(null);
        if (result == JFileChooser.APPROVE_OPTION) {
            String path = fileChooser.getSelectedFile().getAbsolutePath();
            if (!path.toLowerCase().endsWith(".pdf")) {
                path += ".pdf";
            }
            try {
                busInventory.exportPDFBillImport(path);
                CustomDialog.showSuccess("PDF bill export successfully!");
            } catch (Exception e) {
                CustomDialog.showError("PDF export failed: " + e.getMessage());
            }
        }
    }
    
    private void viewBills() {
        // Switch to bills tab
        tabbedPane.setSelectedIndex(1);
        
        // Load bills data
        DefaultTableModel model = (DefaultTableModel) tableBills.getModel();
        model.setRowCount(0);
        
        try {
            busInventory.loadBillsData(model);
        } catch (Exception e) {
            CustomDialog.showError("Failed to load bills: " + e.getMessage());
        }
    }
    
    
    private JTable createStyledTable(DefaultTableModel model) {
        JTable table = new JTable(model);
        table.setRowHeight(30);
        table.setFont(FONT_CONTENT_SMALL);
        table.setSelectionBackground(PRIMARY_COLOR);
        table.setSelectionForeground(Color.WHITE);
        table.setGridColor(Color.LIGHT_GRAY);
        table.setShowGrid(true);
        return table;
    }
    
    // Button styling methods
    private void stylePrimaryButton(MyButton button) {
        button.setBackgroundColor(PRIMARY_COLOR);
        button.setHoverColor(Color.decode("#1976D2"));
        button.setPressedColor(Color.decode("#1565C0"));
        button.setForeground(Color.WHITE);
        button.setFont(FONT_CONTENT_MEDIUM);
    }
    
    private void styleSuccessButton(MyButton button) {
        button.setBackgroundColor(Color.decode("#4CAF50"));
        button.setHoverColor(Color.decode("#45A049"));
        button.setPressedColor(Color.decode("#3D8B40"));
        button.setForeground(Color.WHITE);
        button.setFont(FONT_CONTENT_MEDIUM);
    }
    
    
    private void styleInfoButton(MyButton button) {
        button.setBackgroundColor(Color.decode("#2196F3"));
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
    
    private void styleWarningButton(MyButton button) {
        button.setBackgroundColor(Color.decode("#FF9800"));
        button.setHoverColor(Color.decode("#F57C00"));
        button.setPressedColor(Color.decode("#EF6C00"));
        button.setForeground(Color.WHITE);
        button.setFont(FONT_CONTENT_MEDIUM);
    }
    
    
    private void addNewInventoryItem() {
        try {
            AddInventoryItem addDialog = new AddInventoryItem((JFrame) SwingUtilities.getWindowAncestor(this));
            addDialog.setVisible(true);
            
            // Refresh data after adding new item
            refreshData();
        } catch (Exception e) {
            e.printStackTrace();
            CustomDialog.showError("Failed to open Add Inventory Item dialog: " + e.getMessage());
        }
    }
    
    private void reimportExistingItem() {
        try {
            // Kiểm tra xem có sản phẩm nào được chọn không
            int selectedRow = tableInventory.getSelectedRow();
            if (selectedRow == -1) {
                CustomDialog.showError("Vui lòng chọn sản phẩm cần nhập thêm!");
                return;
            }
            
            // Lấy thông tin sản phẩm được chọn
            String warehouseId = tableInventory.getValueAt(selectedRow, 0).toString();
            String productName = tableInventory.getValueAt(selectedRow, 1).toString();
            int currentQuantity = Integer.parseInt(tableInventory.getValueAt(selectedRow, 4).toString());
            
            // Mở dialog nhập thêm sản phẩm
            ReimportItemDialog reimportDialog = new ReimportItemDialog(
                (JFrame) SwingUtilities.getWindowAncestor(this),
                warehouseId,
                productName,
                currentQuantity
            );
            reimportDialog.setVisible(true);
            
            // Refresh data after reimporting
            refreshData();
        } catch (Exception e) {
            e.printStackTrace();
            CustomDialog.showError("Failed to open Reimport Item dialog: " + e.getMessage());
        }
    }
    
 
}
