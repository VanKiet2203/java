package com.Admin.inventory.GUI;

import com.Admin.inventory.BUS.BUSInventory;
import com.Admin.category.BUS.BusCategory;
import com.Admin.category.DTO.DTOCategory;
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
import java.util.List;

public class Form_Inventory extends JPanel {
    private JPanel panel, panelSearch;
    private MyButton bntRefresh, bntSearch, bntImportNew, bntImportExisting, bntCreateBill, bntViewBills, bntExportExcel, bntExportPDF;
    private MyTextField txtSearch;
    private MyCombobox<String> cmbSearch, cmbCategory, cmbSupplier;
    private JTable tableInventory, tableBills;
    private BUSInventory busInventory;
    private BusCategory busCategory;
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
            "Search & Filter",
            0, 0,
            FONT_TITLE_SMALL,
            PRIMARY_COLOR
        ));
        panelSearch.setBounds(20, 60, 1160, 120);
        
        // Search components
        String[] searchItems = {"Warehouse ID", "Product Name", "Category", "Supplier"};
        cmbSearch = new MyCombobox<>(searchItems);
        cmbSearch.setBounds(20, 30, 150, 35);
        cmbSearch.setCustomFont(FONT_CONTENT_MEDIUM);
        
        txtSearch = new MyTextField();
        txtSearch.setHint("Search...");
        txtSearch.setBounds(180, 30, 300, 35);
        txtSearch.setTextFont(FONT_CONTENT_MEDIUM);
        
        bntSearch = new MyButton("Search", 20);
        stylePrimaryButton(bntSearch);
        bntSearch.setBounds(490, 30, 120, 35);
        bntSearch.addActionListener(e -> performSearch());
        
        bntRefresh = new MyButton("Refresh", 20);
        styleInfoButton(bntRefresh);
        bntRefresh.setBounds(620, 30, 120, 35);
        bntRefresh.addActionListener(e -> refreshData());
        
        // Filter components
        cmbCategory = new MyCombobox<>();
        cmbCategory.setBounds(20, 75, 150, 35);
        cmbCategory.setCustomFont(FONT_CONTENT_MEDIUM);
        
        cmbSupplier = new MyCombobox<>();
        cmbSupplier.setBounds(180, 75, 150, 35);
        cmbSupplier.setCustomFont(FONT_CONTENT_MEDIUM);
        
        panelSearch.add(cmbSearch);
        panelSearch.add(txtSearch);
        panelSearch.add(bntSearch);
        panelSearch.add(bntRefresh);
        panelSearch.add(cmbCategory);
        panelSearch.add(cmbSupplier);
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
        actionPanel.setBounds(20, 190, 1160, 80);
        
        // Add New Item button
        MyButton bntAddNew = new MyButton("Add New Item", 20);
        styleSuccessButton(bntAddNew);
        bntAddNew.setBounds(20, 30, 150, 35);
        bntAddNew.addActionListener(e -> addNewInventoryItem());
        actionPanel.add(bntAddNew);
        
        // Import buttons
        bntImportNew = new MyButton("Import New Items", 20);
        styleSuccessButton(bntImportNew);
        bntImportNew.setBounds(180, 30, 150, 35);
        bntImportNew.addActionListener(e -> importNewItems());
        
        bntImportExisting = new MyButton("Import Existing", 20);
        styleWarningButton(bntImportExisting);
        bntImportExisting.setBounds(340, 30, 150, 35);
        bntImportExisting.addActionListener(e -> importExistingItems());
        
        
        // Export buttons
        bntExportExcel = new MyButton("Export Excel", 20);
        styleInfoButton(bntExportExcel);
        bntExportExcel.setBounds(500, 30, 150, 35);
        bntExportExcel.addActionListener(e -> exportToExcel());
        
        bntExportPDF = new MyButton("Export PDF", 20);
        styleDangerButton(bntExportPDF);
        bntExportPDF.setBounds(660, 30, 150, 35);
        bntExportPDF.addActionListener(e -> exportToPDF());
        // Create Bill button
        bntCreateBill = new MyButton("Create Import Bill", 20);
        styleSuccessButton(bntCreateBill);
        bntCreateBill.setBounds(820, 30, 150, 35);
        bntCreateBill.addActionListener(e -> createImportBill());
        // View bills button
        bntViewBills = new MyButton("View Bills", 20);
        stylePrimaryButton(bntViewBills);
        bntViewBills.setBounds(980, 30, 150, 35);
        bntViewBills.addActionListener(e -> viewBills());
        
        actionPanel.add(bntImportNew);
        actionPanel.add(bntImportExisting);
        actionPanel.add(bntCreateBill);
        actionPanel.add(bntExportExcel);
        actionPanel.add(bntExportPDF);
        actionPanel.add(bntViewBills);
        panel.add(actionPanel);
    }
    
    private void createTabbedPane() {
        tabbedPane = new JTabbedPane();
        tabbedPane.setBounds(20, 280, 1160, 400);
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
        busCategory = new BusCategory();
        
        // Load categories and suppliers
        loadCategories();
        loadSuppliers();
        
        // Load inventory data
        refreshData();
    }
    
    private void loadCategories() {
        try {
            List<DTOCategory> categories = busCategory.getAllCategories();
            cmbCategory.removeAllItems();
            cmbCategory.addItem("All Categories");
            for (DTOCategory category : categories) {
                cmbCategory.addItem(category.getCategoryName());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void loadSuppliers() {
        try {
            // Load suppliers - you may need to implement this in your BUS layer
            cmbSupplier.removeAllItems();
            cmbSupplier.addItem("All Suppliers");
            // Add supplier loading logic here
        } catch (Exception e) {
            e.printStackTrace();
        }
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
    
    private void importNewItems() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Choose Excel file to import new items");
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Excel Files (*.xlsx)", "xlsx"));

        int result = fileChooser.showOpenDialog(null);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            try {
                boolean success = busInventory.importNewItems(selectedFile);
                if (success) {
                    CustomDialog.showSuccess("New items imported successfully!");
                    refreshData();
                }
            } catch (Exception e) {
                CustomDialog.showError("Import failed: " + e.getMessage());
            }
        }
    }
    
    private void importExistingItems() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Choose Excel file to import existing items");
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Excel Files (*.xlsx)", "xlsx"));

        int result = fileChooser.showOpenDialog(null);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            try {
                boolean success = busInventory.importExistingItems(selectedFile);
            if (success) {
                    CustomDialog.showSuccess("Existing items updated successfully!");
                    refreshData();
                }
            } catch (Exception e) {
                CustomDialog.showError("Import failed: " + e.getMessage());
            }
        }
    }
    
    private void exportToExcel() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save Excel file");
        
        int result = fileChooser.showSaveDialog(null);
        if (result == JFileChooser.APPROVE_OPTION) {
            String path = fileChooser.getSelectedFile().getAbsolutePath();
            if (!path.toLowerCase().endsWith(".xlsx")) {
                path += ".xlsx";
            }
            try {
                busInventory.exportToExcel(path);
                CustomDialog.showSuccess("Data exported successfully!");
            } catch (Exception e) {
                CustomDialog.showError("Export failed: " + e.getMessage());
            }
        }
    }
    
    private void exportToPDF() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save PDF file");
        
        int result = fileChooser.showSaveDialog(null);
        if (result == JFileChooser.APPROVE_OPTION) {
            String path = fileChooser.getSelectedFile().getAbsolutePath();
            if (!path.toLowerCase().endsWith(".pdf")) {
                path += ".pdf";
            }
            try {
                busInventory.exportToPDF(path);
                CustomDialog.showSuccess("PDF exported successfully!");
            } catch (Exception e) {
                CustomDialog.showError("PDF export failed: " + e.getMessage());
            }
        }
    }
    
    private void createImportBill() {
        try {
            String billId = busInventory.createImportBill();
            if (billId != null) {
                // Refresh inventory data after creating bill
                refreshData();
            }
        } catch (Exception e) {
            CustomDialog.showError("Failed to create import bill: " + e.getMessage());
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
    
    private void styleWarningButton(MyButton button) {
        button.setBackgroundColor(Color.decode("#FF9800"));
        button.setHoverColor(Color.decode("#F57C00"));
        button.setPressedColor(Color.decode("#E65100"));
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
}
