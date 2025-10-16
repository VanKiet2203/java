package com.Admin.inventory.GUI;

import com.Admin.dashboard_admin.GUI.Dashboard_ad;
import com.Admin.inventory.BUS.BUSInventory;
import com.Admin.inventory.DTO.DTOInventory;
import com.Admin.category.BUS.BusCategory;
import com.Admin.category.DTO.DTOCategory;
import com.ComponentandDatabase.Components.MyButton;
import com.ComponentandDatabase.Components.MyCombobox;
import com.ComponentandDatabase.Components.MyPanel;
import com.ComponentandDatabase.Components.MyTable;
import com.ComponentandDatabase.Components.MyTextField;
import com.ComponentandDatabase.Components.CustomDialog;
import static com.ComponentandDatabase.Components.UIConstants.*;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.io.File;
import java.util.List;

public class Form_Inventory extends JPanel {
    private JPanel panel, panelSearch, panelImport, panelBills;
    private MyButton bntRefresh, bntSearch, bntImportNew, bntImportExisting, bntViewBills, bntExportExcel, bntExportPDF;
    private MyTextField txtSearch, txtWarehouseId, txtProductName, txtImportPrice;
    private MyCombobox<String> cmbSearch, cmbCategory, cmbSupplier;
    private MyTable tableInventory, tableBills;
    private BUSInventory busInventory;
    private BusCategory busCategory;
    private JTabbedPane tabbedPane;
    private JSpinner spinnerQuantity;
    
    public Form_Inventory() {
        initComponents();
        init();
    }
    
    private void initComponents() {
        setLayout(null);
        setPreferredSize(new Dimension(1530, 860));
        setBackground(BG_WHITE);
    }
    
    private void init() {
        panel = new JPanel();
        panel.setLayout(null);
        panel.setBounds(0, 0, 1530, 860);
        panel.setBackground(BG_WHITE);
        add(panel);
        
        // Khởi tạo BUS objects TRƯỚC khi tạo tabs
        busInventory = new BUSInventory();
        busCategory = new BusCategory();
        
        // Tạo tabbed pane
        tabbedPane = new JTabbedPane();
        tabbedPane.setBounds(10, 10, 1510, 840);
        tabbedPane.setFont(new Font("Arial", Font.BOLD, 14));
        panel.add(tabbedPane);
        
        // Tab 1: Quản lý kho
        createInventoryTab();
        
        // Tab 2: Nhập sản phẩm
        createImportTab();
        
        // Tab 3: Hóa đơn nhập
        createBillsTab();
        
        loadInventoryData();
        generateWarehouseId(); // Auto-generate warehouse ID on startup
    }
    
    private void createInventoryTab() {
        JPanel inventoryPanel = new JPanel();
        inventoryPanel.setLayout(null);
        inventoryPanel.setBackground(BG_WHITE);
        
        // Title
        JLabel lblTitle = new JLabel("MANAGE INVENTORY");
        lblTitle.setFont(FONT_TITLE_LARGE);
        lblTitle.setForeground(PRIMARY_COLOR);
        lblTitle.setBounds(20, 10, 400, 40);
        inventoryPanel.add(lblTitle);
        
        // Panel tìm kiếm 
        panelSearch = new MyPanel(BG_WHITE);
        panelSearch.setLayout(null);
        panelSearch.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(PRIMARY_COLOR, 2),
            "Search",
            0, 0,
            FONT_TITLE_SMALL,
            PRIMARY_COLOR
        ));
        panelSearch.setBounds(20, 60, 1490, 80);
        
        // ComboBox tìm kiếm - Cập nhật cho database mới
        String[] searchItems = {"Product.ID", "Product Name", "Brand.ID"};
        cmbSearch = new MyCombobox<>(searchItems);
        cmbSearch.setBounds(20, 30, 150, 35);
        cmbSearch.setCustomFont(FONT_CONTENT_MEDIUM);
        cmbSearch.setCustomColors(Color.WHITE, Color.GRAY, Color.BLACK);
        panelSearch.add(cmbSearch);
        
        // TextField tìm kiếm
        txtSearch = new MyTextField();
        txtSearch.setHint("Search something...");
        txtSearch.setBounds(180, 30, 300, 35);
        txtSearch.setTextFont(FONT_CONTENT_MEDIUM);
        panelSearch.add(txtSearch);
        
        // Nút tìm kiếm
        bntSearch = new MyButton("Search", 20);
        bntSearch.setBackgroundColor(PRIMARY_COLOR);
        bntSearch.setHoverColor(PRIMARY_HOVER);
        bntSearch.setPressedColor(PRIMARY_HOVER.darker());
        bntSearch.setFont(FONT_BUTTON_MEDIUM);
        bntSearch.setForeground(Color.WHITE);
        bntSearch.setBounds(490, 30, 120, 35);
        bntSearch.setButtonIcon("src\\main\\resources\\Icons\\Admin_icon\\search.png", 25, 25, 5, SwingConstants.RIGHT, SwingConstants.CENTER);
        bntSearch.addActionListener(e -> searchInventory());
        panelSearch.add(bntSearch);
        
        // Nút refresh
        bntRefresh = new MyButton("Refresh", 20);
        bntRefresh.setBackgroundColor(INFO_COLOR);
        bntRefresh.setHoverColor(INFO_HOVER);
        bntRefresh.setPressedColor(INFO_HOVER.darker());
        bntRefresh.setFont(FONT_BUTTON_MEDIUM);
        bntRefresh.setForeground(Color.WHITE);
        bntRefresh.setBounds(620, 30, 120, 35);
        bntRefresh.setButtonIcon("src\\main\\resources\\Icons\\Admin_icon\\refresh.png", 25, 25, 10, SwingConstants.RIGHT, SwingConstants.CENTER);
        bntRefresh.addActionListener(e -> loadInventoryData());
        panelSearch.add(bntRefresh);
        
        // Nút xuất Excel
        bntExportExcel = new MyButton("Export", 20);
        bntExportExcel.setBackgroundColor(Color.WHITE);
        bntExportExcel.setPressedColor(Color.decode("#D3D3D3"));
        bntExportExcel.setHoverColor(Color.decode("#EEEEEE"));
        bntExportExcel.setBounds(750, 30, 120, 35);
        bntExportExcel.setForeground(Color.BLACK);
        bntExportExcel.setButtonIcon("src\\main\\resources\\Icons\\Admin_icon\\Excel.png", 30, 30, 10, SwingConstants.RIGHT, SwingConstants.CENTER);
        bntExportExcel.addActionListener(e -> exportToExcel());
        panelSearch.add(bntExportExcel);
        
        // Nút nhập Excel
        MyButton bntImportExcel = new MyButton("Import", 20);
        bntImportExcel.setBackgroundColor(Color.WHITE);
        bntImportExcel.setPressedColor(Color.decode("#D3D3D3"));
        bntImportExcel.setHoverColor(Color.decode("#EEEEEE"));
        bntImportExcel.setBounds(880, 30, 120, 35);
        bntImportExcel.setForeground(Color.BLACK);
        bntImportExcel.setButtonIcon("src\\main\\resources\\Icons\\Admin_icon\\Excel.png", 30, 30, 10, SwingConstants.RIGHT, SwingConstants.CENTER);
        bntImportExcel.addActionListener(e -> importFromExcel());
        panelSearch.add(bntImportExcel);
        
        // Nút dọn dẹp
        MyButton bntClean = new MyButton("Clean Data", 20);
        bntClean.setBackgroundColor(DANGER_COLOR);
        bntClean.setHoverColor(DANGER_HOVER);
        bntClean.setPressedColor(DANGER_HOVER.darker());
        bntClean.setFont(FONT_BUTTON_MEDIUM);
        bntClean.setForeground(Color.WHITE);
        bntClean.setBounds(1010, 30, 120, 35);
        bntClean.addActionListener(e -> cleanData());
        panelSearch.add(bntClean);
        
        // Nút refresh Category/Supplier
        MyButton bntRefreshData = new MyButton("Refresh Data", 20);
        bntRefreshData.setBackgroundColor(INFO_COLOR);
        bntRefreshData.setHoverColor(INFO_HOVER);
        bntRefreshData.setPressedColor(INFO_HOVER.darker());
        bntRefreshData.setFont(FONT_BUTTON_MEDIUM);
        bntRefreshData.setForeground(Color.WHITE);
        bntRefreshData.setBounds(1270, 30, 120, 35);
        bntRefreshData.addActionListener(e -> refreshCategorySupplierData());
        panelSearch.add(bntRefreshData);
        
        // Nút kiểm tra Admin ID
        MyButton bntCheckAdmin = new MyButton("Check Admin", 20);
        bntCheckAdmin.setBackgroundColor(WARNING_COLOR);
        bntCheckAdmin.setHoverColor(WARNING_HOVER);
        bntCheckAdmin.setPressedColor(WARNING_HOVER.darker());
        bntCheckAdmin.setFont(FONT_BUTTON_MEDIUM);
        bntCheckAdmin.setForeground(Color.WHITE);
        bntCheckAdmin.setBounds(1400, 30, 120, 35);
        bntCheckAdmin.addActionListener(e -> checkAdminInfo());
        panelSearch.add(bntCheckAdmin);
        
        // Nút xem chi tiết sản phẩm
        MyButton bntViewDetails = new MyButton("View Details", 20);
        bntViewDetails.setBackgroundColor(WARNING_COLOR);
        bntViewDetails.setHoverColor(WARNING_HOVER);
        bntViewDetails.setPressedColor(WARNING_HOVER.darker());
        bntViewDetails.setFont(FONT_BUTTON_MEDIUM);
        bntViewDetails.setForeground(Color.WHITE);
        bntViewDetails.setBounds(1140, 30, 120, 35);
        bntViewDetails.addActionListener(e -> viewProductDetails());
        panelSearch.add(bntViewDetails);
        
        inventoryPanel.add(panelSearch);
        
        // Bảng hiển thị kho - giống Form_Product
        String[] columnNames = {"Warehouse ID", "Product Name", "Category", "Supplier", "Import Price", "Current Stock", "Total Imported", "Total Sold"};
        DefaultTableModel model = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        tableInventory = createStyledTable(model);
        tableInventory.setRowHeight(30);
        
        JScrollPane scrollPane = MyTable.createScrollPane(tableInventory, 20, 200, 1490, 630);
        scrollPane.getVerticalScrollBar().setPreferredSize(new Dimension(15, Integer.MAX_VALUE));
        scrollPane.getHorizontalScrollBar().setPreferredSize(new Dimension(Integer.MAX_VALUE, 15));
        inventoryPanel.add(scrollPane);
        
        // Load dữ liệu và mở rộng cột
        SwingUtilities.invokeLater(() -> {
            loadInventoryData();
            expandTableColumns();
        });
        
        tabbedPane.addTab("Inventory Management", inventoryPanel);
    }
    
    private void createImportTab() {
        JPanel importPanel = new JPanel();
        importPanel.setLayout(null);
        importPanel.setBackground(Color.WHITE);
        
        // Title
        JLabel lblTitle = new JLabel("IMPORT PRODUCTS");
        lblTitle.setFont(FONT_TITLE_LARGE);
        lblTitle.setForeground(PRIMARY_COLOR);
        lblTitle.setBounds(20, 10, 400, 40);
        importPanel.add(lblTitle);
        
        // Panel nhập sản phẩm mới - giống Form_Product
        panelImport = new MyPanel(Color.WHITE);
        panelImport.setLayout(null);
        panelImport.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(PRIMARY_COLOR, 2),
            "Import New Product",
            0, 0,
            FONT_TITLE_SMALL,
            PRIMARY_COLOR
        ));
        panelImport.setBounds(20, 60, 1490, 120);
        
        // Warehouse Item ID (tự động generate)
        JLabel lblWarehouseId = new JLabel("Warehouse ID:");
        lblWarehouseId.setBounds(20, 30, 100, 25);
        lblWarehouseId.setFont(FONT_LABEL_BOLD);
        lblWarehouseId.setForeground(Color.BLACK);
        panelImport.add(lblWarehouseId);
        
        txtWarehouseId = new MyTextField();
        txtWarehouseId.setBounds(130, 30, 150, 25);
        txtWarehouseId.setBorder(BorderFactory.createLineBorder(BORDER_GRAY, 1));
        txtWarehouseId.setTextFont(FONT_CONTENT_MEDIUM);
        txtWarehouseId.setEditable(false); // Tự động generate
        panelImport.add(txtWarehouseId);
        
        // Product Name
        JLabel lblProductName = new JLabel("Product Name:");
        lblProductName.setBounds(300, 30, 100, 25);
        lblProductName.setFont(FONT_LABEL_BOLD);
        lblProductName.setForeground(Color.BLACK);
        panelImport.add(lblProductName);
        
        txtProductName = new MyTextField();
        txtProductName.setBounds(410, 30, 200, 25);
        txtProductName.setBorder(BorderFactory.createLineBorder(BORDER_GRAY, 1));
        txtProductName.setTextFont(FONT_CONTENT_MEDIUM);
        panelImport.add(txtProductName);
        
        // Import Price
        JLabel lblImportPrice = new JLabel("Import Price:");
        lblImportPrice.setBounds(630, 30, 100, 25);
        lblImportPrice.setFont(FONT_LABEL_BOLD);
        lblImportPrice.setForeground(Color.BLACK);
        panelImport.add(lblImportPrice);
        
        txtImportPrice = new MyTextField();
        txtImportPrice.setBounds(740, 30, 120, 25);
        txtImportPrice.setBorder(BorderFactory.createLineBorder(BORDER_GRAY, 1));
        txtImportPrice.setTextFont(FONT_CONTENT_MEDIUM);
        panelImport.add(txtImportPrice);
        
        // Category
        JLabel lblCategory = new JLabel("Category:");
        lblCategory.setBounds(20, 70, 100, 25);
        lblCategory.setFont(FONT_LABEL_BOLD);
        lblCategory.setForeground(Color.BLACK);
        panelImport.add(lblCategory);
        
        cmbCategory = new MyCombobox<>();
        cmbCategory.setBounds(130, 70, 150, 25);
        cmbCategory.setCustomFont(FONT_CONTENT_MEDIUM);
        cmbCategory.setCustomColors(Color.WHITE, Color.GRAY, Color.BLACK);
        loadCategoriesToComboBox(); // Load categories from database
        panelImport.add(cmbCategory);
        
        // Supplier
        JLabel lblSupplier = new JLabel("Supplier:");
        lblSupplier.setBounds(300, 70, 100, 25);
        lblSupplier.setFont(FONT_LABEL_BOLD);
        lblSupplier.setForeground(Color.BLACK);
        panelImport.add(lblSupplier);
        
        cmbSupplier = new MyCombobox<>();
        cmbSupplier.setBounds(410, 70, 150, 25);
        cmbSupplier.setCustomFont(FONT_CONTENT_MEDIUM);
        cmbSupplier.setCustomColors(Color.WHITE, Color.GRAY, Color.BLACK);
        loadSuppliersToComboBox(); // Load suppliers from database
        panelImport.add(cmbSupplier);
        
        // Quantity
        JLabel lblQuantity = new JLabel("Quantity:");
        lblQuantity.setBounds(630, 70, 80, 25);
        lblQuantity.setFont(FONT_LABEL_BOLD);
        lblQuantity.setForeground(Color.BLACK);
        panelImport.add(lblQuantity);
        
        SpinnerNumberModel quantityModel = new SpinnerNumberModel(1, 1, 10000, 1);
        spinnerQuantity = new JSpinner(quantityModel);
        spinnerQuantity.setBounds(720, 70, 100, 25);
        panelImport.add(spinnerQuantity);
        
        // Nút nhập sản phẩm mới
        bntImportNew = new MyButton("Import New", 20);
        bntImportNew.setBackgroundColor(PRIMARY_COLOR);
        bntImportNew.setHoverColor(PRIMARY_HOVER);
        bntImportNew.setPressedColor(PRIMARY_HOVER.darker());
        bntImportNew.setFont(FONT_BUTTON_MEDIUM);
        bntImportNew.setForeground(Color.WHITE);
        bntImportNew.setBounds(850, 30, 120, 35);
        bntImportNew.addActionListener(e -> importNewProduct());
        panelImport.add(bntImportNew);
        
        // Nút nhập sản phẩm đã có
        bntImportExisting = new MyButton("Import Existing", 20);
        bntImportExisting.setBackgroundColor(WARNING_COLOR);
        bntImportExisting.setHoverColor(WARNING_HOVER);
        bntImportExisting.setPressedColor(WARNING_HOVER.darker());
        bntImportExisting.setFont(FONT_BUTTON_MEDIUM);
        bntImportExisting.setForeground(Color.WHITE);
        bntImportExisting.setBounds(980, 30, 130, 35);
        bntImportExisting.addActionListener(e -> importExistingProduct());
        panelImport.add(bntImportExisting);
        
        importPanel.add(panelImport);
        
        // Bảng hiển thị sản phẩm trong kho để chọn nhập thêm - giống Form_Product
        String[] columnNames = {"Warehouse ID", "Product Name", "Category", "Supplier", "Import Price", "Current Stock"};
        DefaultTableModel model = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        tableInventory = createStyledTable(model);
        tableInventory.setRowHeight(30);
        
        JScrollPane scrollPane = MyTable.createScrollPane(tableInventory, 20, 200, 1490, 630);
        scrollPane.getVerticalScrollBar().setPreferredSize(new Dimension(15, Integer.MAX_VALUE));
        scrollPane.getHorizontalScrollBar().setPreferredSize(new Dimension(Integer.MAX_VALUE, 15));
        importPanel.add(scrollPane);
        
        tabbedPane.addTab("Import Products", importPanel);
    }
    
    private void createBillsTab() {
        JPanel billsPanel = new JPanel();
        billsPanel.setLayout(null);
        billsPanel.setBackground(Color.WHITE);
        
        // Title
        JLabel lblTitle = new JLabel("IMPORT BILLS");
        lblTitle.setFont(FONT_TITLE_LARGE);
        lblTitle.setForeground(PRIMARY_COLOR);
        lblTitle.setBounds(20, 10, 400, 40);
        billsPanel.add(lblTitle);
        
        // Panel hóa đơn - giống Form_Product
        panelBills = new MyPanel(Color.WHITE);
        panelBills.setLayout(null);
        panelBills.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(PRIMARY_COLOR, 2),
            "Import Bills",
            0, 0,
            FONT_TITLE_SMALL,
            PRIMARY_COLOR
        ));
        panelBills.setBounds(20, 60, 1490, 80);
        
        // Nút xem hóa đơn
        bntViewBills = new MyButton("View All Bills", 20);
        bntViewBills.setBackgroundColor(PRIMARY_COLOR);
        bntViewBills.setHoverColor(PRIMARY_HOVER);
        bntViewBills.setPressedColor(PRIMARY_HOVER.darker());
        bntViewBills.setFont(FONT_BUTTON_MEDIUM);
        bntViewBills.setForeground(Color.WHITE);
        bntViewBills.setBounds(20, 30, 120, 35);
        bntViewBills.addActionListener(e -> loadBillsData());
        panelBills.add(bntViewBills);
        
        // Nút xuất PDF
        bntExportPDF = new MyButton("Export PDF", 20);
        bntExportPDF.setBackgroundColor(DANGER_COLOR);
        bntExportPDF.setHoverColor(DANGER_HOVER);
        bntExportPDF.setPressedColor(DANGER_HOVER.darker());
        bntExportPDF.setFont(FONT_BUTTON_MEDIUM);
        bntExportPDF.setForeground(Color.WHITE);
        bntExportPDF.setBounds(150, 30, 120, 35);
        bntExportPDF.addActionListener(e -> exportToPDF());
        panelBills.add(bntExportPDF);
        
        // Nút xem chi tiết hóa đơn
        MyButton bntViewDetails = new MyButton("View Details", 20);
        bntViewDetails.setBackgroundColor(WARNING_COLOR);
        bntViewDetails.setHoverColor(WARNING_HOVER);
        bntViewDetails.setPressedColor(WARNING_HOVER.darker());
        bntViewDetails.setFont(FONT_BUTTON_MEDIUM);
        bntViewDetails.setForeground(Color.WHITE);
        bntViewDetails.setBounds(280, 30, 120, 35);
        bntViewDetails.addActionListener(e -> viewBillDetails());
        panelBills.add(bntViewDetails);
        
        billsPanel.add(panelBills);
        
        // Bảng hiển thị hóa đơn - giống Form_Product
        String[] columnNames = {"Invoice No", "Admin ID", "Admin Name", "Total Products", "Total Price", "Date", "Time"};
        DefaultTableModel model = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        tableBills = createStyledTable(model);
        tableBills.setRowHeight(30);
        
        JScrollPane scrollPane = MyTable.createScrollPane(tableBills, 20, 200, 1490, 630);
        scrollPane.getVerticalScrollBar().setPreferredSize(new Dimension(15, Integer.MAX_VALUE));
        scrollPane.getHorizontalScrollBar().setPreferredSize(new Dimension(Integer.MAX_VALUE, 15));
        billsPanel.add(scrollPane);
        
        tabbedPane.addTab("Import Bills", billsPanel);
    }
    
    private void loadInventoryData() {
        busInventory.loadInventoryToTable((DefaultTableModel) tableInventory.getModel());
        expandTableColumns();
    }
    
    private void searchInventory() {
        String searchType = cmbSearch.getSelectedItem().toString();
        String keyword = txtSearch.getText().trim();
        
        if (keyword.isEmpty()) {
            loadInventoryData();
            return;
        }
        
        busInventory.searchAndLoadToTable(searchType, keyword, (DefaultTableModel) tableInventory.getModel());
        expandTableColumns();
    }
    
    private void loadBillsData() {
        busInventory.loadImportBillsToTable((DefaultTableModel) tableBills.getModel());
        expandBillsTableColumns();
    }
    
    private void importNewProduct() {
        String warehouseId = txtWarehouseId.getText().trim();
        String productName = txtProductName.getText().trim();
        String importPriceStr = txtImportPrice.getText().trim();
        int quantity = (Integer) spinnerQuantity.getValue();
        
        if (warehouseId.isEmpty() || productName.isEmpty() || importPriceStr.isEmpty()) {
            CustomDialog.showError("Please fill in all required fields!");
            return;
        }
        
        try {
            BigDecimal importPrice = new BigDecimal(importPriceStr);
            String categoryId = cmbCategory.getSelectedItem().toString();
            String supplierId = cmbSupplier.getSelectedItem().toString();
            String adminId = Dashboard_ad.adminID;
            
            // Kiểm tra Admin_ID
            if (adminId == null || adminId.trim().isEmpty()) {
                CustomDialog.showError("Admin ID is not available! Please login again.\nCurrent Admin ID: " + adminId);
                return;
            }
            
            // Debug: Hiển thị thông tin admin
            System.out.println("Importing with Admin ID: " + adminId);
            
            boolean success = busInventory.importSimpleProduct(warehouseId, productName, 
                                                              BigDecimal.ZERO, // Price sẽ được set sau trong Product module
                                                              categoryId, supplierId, quantity, 
                                                              importPrice, adminId, 
                                                              null, null, null); // Color, Speed, Battery sẽ được set sau
            
            if (success) {
                CustomDialog.showSuccess("Product imported to warehouse successfully!\nYou can now add it to Product catalog with full details.");
                clearImportFields();
                // Refresh cả 3 bảng
                loadInventoryData();
                loadBillsData();
                // Force refresh table display
                tableInventory.revalidate();
                tableInventory.repaint();
            } else {
                CustomDialog.showError("Failed to import product to warehouse!");
            }
            
        } catch (NumberFormatException e) {
            CustomDialog.showError("Please enter valid number for import price!");
        }
    }
    
    private void importExistingProduct() {
        int selectedRow = tableInventory.getSelectedRow();
        if (selectedRow == -1) {
            CustomDialog.showError("Please select a product first!");
            return;
        }
        
        String warehouseId = tableInventory.getValueAt(selectedRow, 0).toString();
        String unitPriceStr = JOptionPane.showInputDialog(this, "Enter import price for " + warehouseId + ":");
        if (unitPriceStr == null || unitPriceStr.trim().isEmpty()) {
            return;
        }
        
        String quantityStr = JOptionPane.showInputDialog(this, "Enter quantity to import:");
        if (quantityStr == null || quantityStr.trim().isEmpty()) {
            return;
        }
        
        try {
            BigDecimal unitPrice = new BigDecimal(unitPriceStr.trim());
            int quantity = Integer.parseInt(quantityStr.trim());
            String adminId = Dashboard_ad.adminID;
            
            // Kiểm tra Admin_ID
            if (adminId == null || adminId.trim().isEmpty()) {
                CustomDialog.showError("Admin ID is not available! Please login again.\nCurrent Admin ID: " + adminId);
                return;
            }
            
            // Debug: Hiển thị thông tin admin
            System.out.println("Importing existing with Admin ID: " + adminId);
            
            boolean success = busInventory.importExistingProduct(warehouseId, quantity, unitPrice, adminId);
            
            if (success) {
                CustomDialog.showSuccess("Product imported successfully!");
                // Refresh cả 3 bảng
                loadInventoryData();
                loadBillsData();
                // Force refresh table display
                tableInventory.revalidate();
                tableInventory.repaint();
            } else {
                CustomDialog.showError("Failed to import product!");
            }
            
        } catch (NumberFormatException e) {
            CustomDialog.showError("Please enter valid numbers!");
        }
    }
    
    private void clearImportFields() {
        txtWarehouseId.setText("");
        txtProductName.setText("");
        txtImportPrice.setText("");
        spinnerQuantity.setValue(1);
        // Auto-generate new Warehouse ID
        generateWarehouseId();
    }
    
    private void generateWarehouseId() {
        // Generate warehouse ID based on timestamp
        String timestamp = String.valueOf(System.currentTimeMillis());
        String warehouseId = "WH" + timestamp.substring(timestamp.length() - 8);
        txtWarehouseId.setText(warehouseId);
    }
    
    private void exportToExcel() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save Excel File");
        fileChooser.setSelectedFile(new File("Inventory_Report.xlsx"));
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Excel Files (*.xlsx)", "xlsx"));

        int userSelection = fileChooser.showSaveDialog(null);
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();
            String filePath = fileToSave.getAbsolutePath();
            if (!filePath.endsWith(".xlsx")) {
                filePath += ".xlsx";
            }

            boolean success = busInventory.exportInventoryToExcel(filePath);
            if (success) {
                CustomDialog.showSuccess("Excel file exported successfully!");
            } else {
                CustomDialog.showError("Failed to export Excel file!");
            }
        }
    }
    
    private void importFromExcel() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Choose Excel file to import");
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Excel Files (*.xlsx, *.xls)", "xlsx", "xls"));

        int result = fileChooser.showOpenDialog(null);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            boolean success = busInventory.importInventoryFromExcel(selectedFile);
            if (success) {
                loadInventoryData();
            }
        }
    }
    
    private void cleanData() {
        boolean confirm = CustomDialog.showOptionPane(
            "Confirm Cleaning",
            "Are you sure you want to clean all inventory data?",
            UIManager.getIcon("OptionPane.questionIcon"),
            Color.decode("#FF6666")
        );

        if (confirm) {
            boolean success = busInventory.cleanAllInventory();
            if (success) {
                loadInventoryData();
            }
        }
    }
    
    private void viewBillDetails() {
        int selectedRow = tableBills.getSelectedRow();
        if (selectedRow == -1) {
            CustomDialog.showError("Please select a bill first!");
            return;
        }
        
        String invoiceNo = tableBills.getValueAt(selectedRow, 0).toString();
        BillDetailsDialog dialog = new BillDetailsDialog((JFrame) SwingUtilities.getWindowAncestor(this), invoiceNo);
        dialog.setVisible(true);
    }
    
    private void viewProductDetails() {
        int selectedRow = tableInventory.getSelectedRow();
        if (selectedRow == -1) {
            CustomDialog.showError("Please select a product first!");
            return;
        }
        
        String productId = tableInventory.getValueAt(selectedRow, 0).toString();
        DTOInventory product = busInventory.getProductById(productId);
        
        if (product != null) {
            ProductDetailsDialog dialog = new ProductDetailsDialog((JFrame) SwingUtilities.getWindowAncestor(this), product);
            dialog.setVisible(true);
        } else {
            CustomDialog.showError("Product not found!");
        }
    }
    
    private void exportToPDF() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save PDF File");
        fileChooser.setSelectedFile(new File("Inventory_Report.pdf"));
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("PDF Files (*.pdf)", "pdf"));

        int userSelection = fileChooser.showSaveDialog(null);
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();
            String filePath = fileToSave.getAbsolutePath();
            if (!filePath.endsWith(".pdf")) {
                filePath += ".pdf";
            }

            boolean success = busInventory.exportInventoryToPDF(filePath);
            if (success) {
                CustomDialog.showSuccess("PDF file exported successfully to:\n" + filePath);
            } else {
                CustomDialog.showError("Failed to export PDF file!");
            }
        }
    }
    
    /**
     * Tạo bảng với style chuẩn giống Form_Product
     */
    private MyTable createStyledTable(DefaultTableModel model) {
        return new MyTable(
            model,
            Color.WHITE,                    // Nền bảng
            TEXT_PRIMARY,                   // Chữ bảng
            Color.decode("#E8F5E9"),        // Nền dòng chọn
            Color.BLACK,                    // Chữ dòng chọn
            PRIMARY_COLOR,                  // Nền tiêu đề
            Color.WHITE,                    // Chữ tiêu đề
            FONT_TABLE_CONTENT,             // Font nội dung
            FONT_TABLE_HEADER               // Font tiêu đề
        );
    }
    
    /**
     * Mở rộng các cột của table để sử dụng hết không gian có sẵn
     */
    private void expandTableColumns() {
        if (tableInventory == null) return;
        
        javax.swing.table.TableColumnModel columnModel = tableInventory.getColumnModel();
        int totalWidth = 1490; // Chiều rộng tổng của table
        int columnCount = tableInventory.getColumnCount();
        
        // Định nghĩa tỷ lệ chiều rộng cho từng cột
        double[] columnRatios = {
            0.12,  // Warehouse ID - 12%
            0.20,  // Product Name - 20%
            0.12,  // Category - 12%
            0.12,  // Supplier - 12%
            0.12,  // Import Price - 12%
            0.10,  // Current Stock - 10%
            0.11,  // Total Imported - 11%
            0.11   // Total Sold - 11%
        };
        
        // Áp dụng tỷ lệ cho từng cột
        for (int i = 0; i < columnCount && i < columnRatios.length; i++) {
            javax.swing.table.TableColumn column = columnModel.getColumn(i);
            int columnWidth = (int) (totalWidth * columnRatios[i]);
            column.setPreferredWidth(columnWidth);
            column.setWidth(columnWidth);
        }
        
        // Đảm bảo table sử dụng hết không gian
        tableInventory.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        tableInventory.revalidate();
        tableInventory.repaint();
    }
    
    /**
     * Mở rộng các cột của bảng Bills để sử dụng hết không gian có sẵn
     */
    private void expandBillsTableColumns() {
        if (tableBills == null) return;
        
        javax.swing.table.TableColumnModel columnModel = tableBills.getColumnModel();
        int totalWidth = 1490; // Chiều rộng tổng của table
        int columnCount = tableBills.getColumnCount();
        
        // Định nghĩa tỷ lệ chiều rộng cho từng cột
        double[] columnRatios = {
            0.15,  // Invoice No - 15%
            0.12,  // Admin ID - 12%
            0.18,  // Admin Name - 18%
            0.12,  // Total Products - 12%
            0.15,  // Total Price - 15%
            0.14,  // Date - 14%
            0.14   // Time - 14%
        };
        
        // Áp dụng tỷ lệ cho từng cột
        for (int i = 0; i < columnCount && i < columnRatios.length; i++) {
            javax.swing.table.TableColumn column = columnModel.getColumn(i);
            int columnWidth = (int) (totalWidth * columnRatios[i]);
            column.setPreferredWidth(columnWidth);
            column.setWidth(columnWidth);
        }
        
        // Đảm bảo table sử dụng hết không gian
        tableBills.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        tableBills.revalidate();
        tableBills.repaint();
    }
    
    /**
     * Load categories from database into combobox
     */
    private void loadCategoriesToComboBox() {
        try {
            List<DTOCategory> categories = busCategory.getAllCategories();
            cmbCategory.removeAllItems();
            
            for (DTOCategory category : categories) {
                cmbCategory.addItem(category.getCategoryID());
            }
            
            // Set default selection if available
            if (cmbCategory.getItemCount() > 0) {
                cmbCategory.setSelectedIndex(0);
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            CustomDialog.showError("Error loading categories: " + e.getMessage());
            // Fallback to default
            cmbCategory.addItem("UNCAT");
        }
    }
    
    /**
     * Load suppliers from database into combobox
     */
    private void loadSuppliersToComboBox() {
        try {
            List<String> suppliers = busCategory.getAllSupplierIDs();
            cmbSupplier.removeAllItems();
            
            for (String supplier : suppliers) {
                cmbSupplier.addItem(supplier);
            }
            
            // Set default selection if available
            if (cmbSupplier.getItemCount() > 0) {
                cmbSupplier.setSelectedIndex(0);
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            CustomDialog.showError("Error loading suppliers: " + e.getMessage());
            // Fallback to default
            cmbSupplier.addItem("SUP00");
        }
    }
    
    /**
     * Refresh Category and Supplier data from database
     */
    private void refreshCategorySupplierData() {
        loadCategoriesToComboBox();
        loadSuppliersToComboBox();
        CustomDialog.showSuccess("Category and Supplier data refreshed successfully!");
    }
    
    /**
     * Check current admin information
     */
    private void checkAdminInfo() {
        String adminId = Dashboard_ad.adminID;
        String adminName = Dashboard_ad.adminName;
        
        String message = "Current Admin Information:\n";
        message += "Admin ID: " + (adminId != null ? adminId : "NULL") + "\n";
        message += "Admin Name: " + (adminName != null ? adminName : "NULL") + "\n";
        
        if (adminId == null || adminId.trim().isEmpty()) {
            message += "\n⚠️ WARNING: Admin ID is not available!\n";
            message += "This will cause import errors.\n";
            message += "Please login again.";
            CustomDialog.showError(message);
        } else {
            message += "\n✅ Admin information is available.";
            CustomDialog.showSuccess(message);
        }
    }
}
