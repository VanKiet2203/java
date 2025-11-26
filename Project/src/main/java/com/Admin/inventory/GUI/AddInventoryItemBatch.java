package com.Admin.inventory.GUI;

import com.Admin.inventory.BUS.BUSInventory;
import com.Admin.inventory.DTO.DTOInventory;
import com.Admin.category.BUS.BusCategory;
import com.Admin.category.DTO.DTOCategory;
import com.ComponentandDatabase.Components.MyButton;
import com.ComponentandDatabase.Components.MyPanel;
import com.ComponentandDatabase.Components.MyTextField;
import com.ComponentandDatabase.Components.MyCombobox;
import com.ComponentandDatabase.Components.CustomDialog;
import static com.ComponentandDatabase.Components.UIConstants.*;

import javax.swing.*;
import java.awt.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Dialog để nhập nhiều sản phẩm cùng lúc từ 1 nhà cung cấp
 * Tất cả sản phẩm sẽ được nhập vào cùng 1 hóa đơn (Bill_Imported)
 */
public class AddInventoryItemBatch extends JDialog {
    private MyCombobox<String> cmbSupplier;
    private JPanel itemsPanel;
    private List<ItemRowPanel> itemRows;
    private MyButton btnAddMore, btnSave, btnCancel;
    private BUSInventory busInventory;
    private BusCategory busCategory;
    private JScrollPane scrollPane;
    private JLabel lblSupplierLocked;
    private List<String> availableWarehouseIds; // Track các IDs đã được generate và chưa sử dụng
    private int currentIdIndex; // Index hiện tại trong danh sách IDs
    
    public AddInventoryItemBatch(JFrame parent) {
        super(parent, "Batch Import - Nhập nhiều sản phẩm", true);
        this.itemRows = new ArrayList<>();
        this.availableWarehouseIds = new ArrayList<>();
        this.currentIdIndex = 0;
        initComponents();
        init();
        loadData();
    }
    
    private void initComponents() {
        setSize(1200, 800);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        setResizable(true);
    }
    
    private void init() {
        // Header
        JPanel headerPanel = new MyPanel(PRIMARY_COLOR);
        headerPanel.setPreferredSize(new Dimension(1200, 60));
        headerPanel.setLayout(new BorderLayout());
        
        JLabel lblTitle = new JLabel("Batch Import");
        lblTitle.setFont(FONT_TITLE_MEDIUM);
        lblTitle.setForeground(Color.WHITE);
        lblTitle.setHorizontalAlignment(SwingConstants.CENTER);
        headerPanel.add(lblTitle, BorderLayout.CENTER);
        
        add(headerPanel, BorderLayout.NORTH);
        
        // Main content panel
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Supplier selection panel (chọn một lần, sau đó khóa)
        JPanel supplierPanel = new MyPanel(Color.WHITE);
        supplierPanel.setLayout(new GridBagLayout());
        supplierPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(PRIMARY_COLOR, 2),
            "Supplier (choose once - apply to all products)",
            0, 0,
            FONT_TITLE_SMALL,
            PRIMARY_COLOR
        ));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.EAST;
        JLabel lblSupplier = new JLabel("Supplier:");
        lblSupplier.setFont(FONT_CONTENT_MEDIUM);
        supplierPanel.add(lblSupplier, gbc);
        
        gbc.gridx = 1; gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        cmbSupplier = new MyCombobox<>();
        cmbSupplier.setCustomFont(FONT_CONTENT_MEDIUM);
        supplierPanel.add(cmbSupplier, gbc);
        
        // Label hiển thị supplier đã chọn (sẽ hiện khi đã chọn)
        gbc.gridx = 2; gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.weightx = 0;
        lblSupplierLocked = new JLabel("");
        lblSupplierLocked.setFont(FONT_CONTENT_MEDIUM);
        lblSupplierLocked.setForeground(PRIMARY_COLOR);
        lblSupplierLocked.setVisible(false);
        supplierPanel.add(lblSupplierLocked, gbc);
        
        // Hiển thị supplier đã chọn (không khóa ngay)
        cmbSupplier.addActionListener(e -> {
            if (cmbSupplier.getSelectedItem() != null) {
                String selected = cmbSupplier.getSelectedItem().toString();
                lblSupplierLocked.setText("Selected: " + selected);
                lblSupplierLocked.setVisible(true);
                // KHÔNG khóa supplier - để người dùng có thể thay đổi nếu cần
            }
        });
        
        mainPanel.add(supplierPanel, BorderLayout.NORTH);
        
        // Items panel với scroll
        itemsPanel = new JPanel();
        itemsPanel.setLayout(new BoxLayout(itemsPanel, BoxLayout.Y_AXIS));
        itemsPanel.setBackground(Color.WHITE);
        
        scrollPane = new JScrollPane(itemsPanel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(PRIMARY_COLOR, 2),
            "Product list",
            0, 0,
            FONT_TITLE_SMALL,
            PRIMARY_COLOR
        ));
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        buttonPanel.setBackground(Color.WHITE);
        
        btnAddMore = new MyButton("Add More", 20);
        btnAddMore.setBackgroundColor(Color.decode("#2196F3"));
        btnAddMore.setHoverColor(Color.decode("#1976D2"));
        btnAddMore.setPressedColor(Color.decode("#1565C0"));
        btnAddMore.setForeground(Color.WHITE);
        btnAddMore.setFont(FONT_CONTENT_MEDIUM);
        btnAddMore.addActionListener(e -> addNewItemRow());
        
        btnSave = new MyButton("Save Batch", 20);
        btnSave.setBackgroundColor(Color.decode("#4CAF50"));
        btnSave.setHoverColor(Color.decode("#45A049"));
        btnSave.setPressedColor(Color.decode("#3D8B40"));
        btnSave.setForeground(Color.WHITE);
        btnSave.setFont(FONT_CONTENT_MEDIUM);
        btnSave.addActionListener(e -> saveBatch());
        
        btnCancel = new MyButton("Cancel", 20);
        btnCancel.setBackgroundColor(Color.decode("#F44336"));
        btnCancel.setHoverColor(Color.decode("#D32F2F"));
        btnCancel.setPressedColor(Color.decode("#C62828"));
        btnCancel.setForeground(Color.WHITE);
        btnCancel.setFont(FONT_CONTENT_MEDIUM);
        btnCancel.addActionListener(e -> dispose());
        
        buttonPanel.add(btnAddMore);
        buttonPanel.add(btnSave);
        buttonPanel.add(btnCancel);
        
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        add(mainPanel, BorderLayout.CENTER);
    }
    
    private void loadData() {
        // Khởi tạo BUS objects trước
        busInventory = new BUSInventory();
        busCategory = new BusCategory();
        
        // Ensure sample data exists
        busInventory.ensureSampleDataExists();
        
        // Load suppliers
        try {
            List<String> suppliers = busInventory.getAllSuppliers();
            cmbSupplier.removeAllItems();
            for (String supplier : suppliers) {
                cmbSupplier.addItem(supplier);
            }
        } catch (Exception e) {
            e.printStackTrace();
            CustomDialog.showError("Failed to load suppliers: " + e.getMessage());
        }
        
        // Thêm dòng đầu tiên SAU KHI đã load data
        addNewItemRow();
    }
    
    /**
     * Thêm một dòng nhập sản phẩm mới
     */
    private void addNewItemRow() {
        ItemRowPanel row = new ItemRowPanel(itemRows.size() + 1, busCategory, this);
        itemRows.add(row);
        itemsPanel.add(row);
        itemsPanel.revalidate();
        itemsPanel.repaint();
                
        // Scroll to bottom
        SwingUtilities.invokeLater(() -> {
            JScrollBar vertical = scrollPane.getVerticalScrollBar();
            vertical.setValue(vertical.getMaximum());
        });
    }
    
    /**
     * Get next available Warehouse ID (tự động generate thêm nếu cần)
     */
    public String getNextWarehouseId() {
        // Nếu đã hết IDs, generate thêm 10 IDs
        if (currentIdIndex >= availableWarehouseIds.size()) {
            try {
                List<String> newIds = busInventory.generateMultipleWarehouseIds(10);
                availableWarehouseIds.addAll(newIds);
                System.out.println("Generated " + newIds.size() + " new Warehouse IDs");
            } catch (Exception e) {
                e.printStackTrace();
                CustomDialog.showError("Failed to generate Warehouse IDs: " + e.getMessage());
                return null;
            }
        }
        
        // Trả về ID tiếp theo
        if (currentIdIndex < availableWarehouseIds.size()) {
            String id = availableWarehouseIds.get(currentIdIndex);
            currentIdIndex++;
            return id;
        }
        
        return null;
    }
    
    /**
     * Lưu tất cả items vào database (cùng 1 hóa đơn)
     */
    private void saveBatch() {
        // Validate supplier
        if (cmbSupplier.getSelectedItem() == null) {
            CustomDialog.showError("Please select a supplier!");
            return;
        }
        
        String supplierId = cmbSupplier.getSelectedItem().toString().split(" - ")[0];
        
        // Validate và thu thập items
        List<DTOInventory> items = new ArrayList<>();
        List<String> errors = new ArrayList<>();
        
        for (int i = 0; i < itemRows.size(); i++) {
            ItemRowPanel row = itemRows.get(i);
            String error = row.validateInput();
            if (error != null) {
                errors.add("Row " + (i + 1) + ": " + error);
                continue;
            }
            
            DTOInventory item = row.toDTOInventory(supplierId);
            items.add(item);
        }
        
        if (!errors.isEmpty()) {
            CustomDialog.showError("Please fix the following errors:\n" + String.join("\n", errors));
            return;
        }
        
        if (items.isEmpty()) {
            CustomDialog.showError("No products to save!");
            return;
        }
        
        // Confirm
        int confirm = JOptionPane.showConfirmDialog(
            this,
            "Are you sure you want to import " + items.size() + " products from supplier " + supplierId + "?",
            "Confirm",
            JOptionPane.YES_NO_OPTION
        );
        
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }
        
        // Save to database
        try {
            boolean success = busInventory.addInventoryItemsBatch(items, supplierId);
            if (success) {
                CustomDialog.showSuccess("Import successful!\n" +
                    "Number of products: " + items.size());
                dispose();
            }
        } catch (Exception e) {
            e.printStackTrace();
            CustomDialog.showError("Import error: " + e.getMessage());
        }
    }
    
    /**
     * Panel cho mỗi dòng nhập sản phẩm
     */
    private class ItemRowPanel extends JPanel {
        private MyTextField txtWarehouseId, txtProductName, txtQuantity, txtUnitPrice, txtProductionYear;
        private MyCombobox<String> cmbCategory;
        private MyButton btnRemove;
        private int rowNumber;
        private BusCategory rowBusCategory;
        private AddInventoryItemBatch parentDialog;
        
        public ItemRowPanel(int rowNumber, BusCategory busCategory, AddInventoryItemBatch parentDialog) {
            this.rowNumber = rowNumber;
            this.rowBusCategory = busCategory;
            this.parentDialog = parentDialog;
            initComponents();
        }
        
        public String getWarehouseId() {
            return txtWarehouseId.getText();
        }
        
        private void initComponents() {
            setLayout(new GridBagLayout());
            setBackground(Color.WHITE);
            setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1),
                "Product #" + rowNumber,
                0, 0,
                FONT_CONTENT_SMALL,
                PRIMARY_COLOR
            ));
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 180));
            
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(5, 5, 5, 5);
            
            // Warehouse ID
            gbc.gridx = 0; gbc.gridy = 0;
            gbc.anchor = GridBagConstraints.EAST;
            add(new JLabel("Warehouse ID:"), gbc);
            
            gbc.gridx = 1; gbc.gridy = 0;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.weightx = 0.15;
            txtWarehouseId = new MyTextField();
            txtWarehouseId.setHint("Warehouse ID");
            txtWarehouseId.setTextFont(FONT_CONTENT_SMALL);
            add(txtWarehouseId, gbc);
            
            // Product Name
            gbc.gridx = 2; gbc.gridy = 0;
            gbc.anchor = GridBagConstraints.EAST;
            add(new JLabel("Product Name:"), gbc);
            
            gbc.gridx = 3; gbc.gridy = 0;
            gbc.weightx = 0.25;
            txtProductName = new MyTextField();
            txtProductName.setHint("Product Name");
            txtProductName.setTextFont(FONT_CONTENT_SMALL);
            add(txtProductName, gbc);
            
            // Category
            gbc.gridx = 4; gbc.gridy = 0;
            gbc.anchor = GridBagConstraints.EAST;
            add(new JLabel("Category:"), gbc);
            
            gbc.gridx = 5; gbc.gridy = 0;
            gbc.weightx = 0.2;
            cmbCategory = new MyCombobox<>();
            cmbCategory.setCustomFont(FONT_CONTENT_SMALL);
            loadCategories();
            add(cmbCategory, gbc);
            
            // Quantity
            gbc.gridx = 0; gbc.gridy = 1;
            gbc.anchor = GridBagConstraints.EAST;
            add(new JLabel("Quantity:"), gbc);
            
            gbc.gridx = 1; gbc.gridy = 1;
            gbc.weightx = 0.15;
            txtQuantity = new MyTextField();
            txtQuantity.setHint("0");
            txtQuantity.setTextFont(FONT_CONTENT_SMALL);
            add(txtQuantity, gbc);
            
            // Unit Price
            gbc.gridx = 2; gbc.gridy = 1;
            gbc.anchor = GridBagConstraints.EAST;
            add(new JLabel("Unit Price:"), gbc);
            
            gbc.gridx = 3; gbc.gridy = 1;
            gbc.weightx = 0.25;
            txtUnitPrice = new MyTextField();
            txtUnitPrice.setHint("0.00");
            txtUnitPrice.setTextFont(FONT_CONTENT_SMALL);
            add(txtUnitPrice, gbc);
            
            // Production Year
            gbc.gridx = 4; gbc.gridy = 1;
            gbc.anchor = GridBagConstraints.EAST;
            add(new JLabel("Year:"), gbc);
            
            gbc.gridx = 5; gbc.gridy = 1;
            gbc.weightx = 0.15;
            txtProductionYear = new MyTextField();
            txtProductionYear.setHint("Example: 2024");
            txtProductionYear.setTextFont(FONT_CONTENT_SMALL);
            add(txtProductionYear, gbc);
            
            // Remove button
            gbc.gridx = 6; gbc.gridy = 0;
            gbc.gridheight = 2;
            gbc.weightx = 0;
            gbc.fill = GridBagConstraints.NONE;
            btnRemove = new MyButton("Remove", 12);
            btnRemove.setBackgroundColor(Color.decode("#F44336"));
            btnRemove.setForeground(Color.WHITE);
            btnRemove.setFont(FONT_CONTENT_SMALL);
            btnRemove.addActionListener(e -> removeRow());
            add(btnRemove, gbc);
        }
        
        private void loadCategories() {
            try {
                // Sử dụng busCategory được truyền vào constructor
                if (rowBusCategory == null) {
                    rowBusCategory = new BusCategory();
                }
                List<DTOCategory> categories = rowBusCategory.getAllCategories();
                cmbCategory.removeAllItems();
                if (categories != null && !categories.isEmpty()) {
                    for (DTOCategory category : categories) {
                        cmbCategory.addItem(category.getCategoryID() + " - " + category.getCategoryName());
                    }
                } else {
                    System.err.println("Warning: No categories found in database");
                }
            } catch (Exception e) {
                e.printStackTrace();
                System.err.println("Error loading categories: " + e.getMessage());
                CustomDialog.showError("Failed to load categories: " + e.getMessage());
            }
        }
        
        private void removeRow() {
            if (itemRows.size() <= 1) {
                CustomDialog.showError("There must be at least 1 product row!");
                return;
            }
            itemRows.remove(this);
            itemsPanel.remove(this);
            itemsPanel.revalidate();
            itemsPanel.repaint();
            
            // Update row numbers
            for (int i = 0; i < itemRows.size(); i++) {
                ItemRowPanel row = itemRows.get(i);
                row.rowNumber = i + 1;
                row.setBorder(BorderFactory.createTitledBorder(
                    BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1),
                    "Product #" + (i + 1),
                    0, 0,
                    FONT_CONTENT_SMALL,
                    PRIMARY_COLOR
                ));
            }
        }
        
        public String validateInput() {
            if (txtWarehouseId.getText().trim().isEmpty()) {
                return "Warehouse ID cannot be empty";
            }
            if (txtProductName.getText().trim().isEmpty()) {
                return "Product Name cannot be empty";
            }
            if (cmbCategory.getSelectedItem() == null) {
                return "Please select a Category";
            }
            if (txtQuantity.getText().trim().isEmpty()) {
                return "Quantity cannot be empty";
            }
            try {
                int qty = Integer.parseInt(txtQuantity.getText().trim());
                if (qty <= 0) {
                    return "Quantity must be > 0";
                }
            } catch (NumberFormatException e) {
                return "Quantity is not valid";
            }
            if (txtUnitPrice.getText().trim().isEmpty()) {
                return "Unit Price cannot be empty";
            }
            try {
                BigDecimal price = new BigDecimal(txtUnitPrice.getText().trim());
                if (price.compareTo(BigDecimal.ZERO) <= 0) {
                    return "Unit Price must be > 0";
                }
            } catch (NumberFormatException e) {
                return "Unit Price is not valid";
            }
            
            // Validate production year (optional)
            String productionYearStr = txtProductionYear.getText().trim();
            if (!productionYearStr.isEmpty()) {
                try {
                    int productionYear = Integer.parseInt(productionYearStr);
                    int currentYear = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR);
                    if (productionYear < 1900 || productionYear > currentYear + 1) {
                        return "Production Year must be between 1900 and " + (currentYear + 1);
                    }
                } catch (NumberFormatException e) {
                    return "Production Year is not valid";
                }
            }
            return null;
        }
        
        public DTOInventory toDTOInventory(String supplierId) {
            DTOInventory item = new DTOInventory();
            item.setWarehouseItemId(txtWarehouseId.getText().trim());
            item.setProductName(txtProductName.getText().trim());
            
            String categoryStr = cmbCategory.getSelectedItem().toString();
            String categoryId = categoryStr.split(" - ")[0];
            item.setCategoryId(categoryId);
            
            item.setSupId(supplierId);
            item.setQuantityStock(Integer.parseInt(txtQuantity.getText().trim()));
            item.setUnitPriceImport(new BigDecimal(txtUnitPrice.getText().trim()));
            
            // Set production year (optional)
            String productionYearStr = txtProductionYear.getText().trim();
            if (!productionYearStr.isEmpty()) {
                item.setProductionYear(Integer.parseInt(productionYearStr));
            } else {
                item.setProductionYear(null);
            }
            
            return item;
        }
    }
}

