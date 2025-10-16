package com.Admin.product.GUI;

import com.ComponentandDatabase.Components.MyCombobox;
import com.ComponentandDatabase.Components.MyPanel;
import com.ComponentandDatabase.Components.MyTextField;
import com.ComponentandDatabase.Components.MyButton;
import com.ComponentandDatabase.Components.CustomDialog;
import com.Admin.product.BUS.BusProduct;
import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;
import com.Admin.category.DTO.DTOCategory;
import com.Admin.product.DTO.DTOProduct;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import javax.swing.*;
import net.miginfocom.swing.MigLayout;
import java.awt.Dimension;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import javax.swing.table.DefaultTableModel;
import javax.swing.BorderFactory;

public class NewProduct extends javax.swing.JFrame {
    private int mouseX, mouseY;
    private JLabel lblTitle, lblProductID, lblProductName, lblColor, lblBattery,
            lblSpeed, lblPrice, lblQuantity, lblCate;
    private MyPanel panelTitle;
    private MyTextField txtProductID, txtProductName, txtColor, txtBattery, txtSpeed, txtPrice;
    private MyCombobox<String> cmbOperate;
    private MyButton bntupload, bntSave, bntReset;
    private JPanel panelUpload;
    private JSpinner spinnerQuantity;
    private JMenu menu;
    private String image;
    private BusProduct busProduct;

    public NewProduct() {
        initComponents();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        // cho phép kéo di chuyển cửa sổ
        bg.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                mouseX = evt.getX();
                mouseY = evt.getY();
            }
        });
        bg.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            public void mouseDragged(java.awt.event.MouseEvent evt) {
                int x = evt.getXOnScreen();
                int y = evt.getYOnScreen();
                setLocation(x - mouseX, y - mouseY);
            }
        });

        init();
    }

    public void init() {
        // Layout chính: tiêu đề + nội dung
        bg.setLayout(new net.miginfocom.swing.MigLayout("fill, insets 10, wrap", "[grow]", "[][][grow]"));
    
        // Panel tiêu đề
        panelTitle = new MyPanel(new net.miginfocom.swing.MigLayout("fill, insets 0"));
        panelTitle.setGradientColors(Color.decode("#1CB5E0"), Color.decode("#4682B4"), MyPanel.VERTICAL_GRADIENT);
    
        lblTitle = new JLabel("Add New Product from Warehouse", JLabel.CENTER);
        lblTitle.setFont(new Font("Arial", Font.BOLD, 22));
        lblTitle.setForeground(Color.WHITE);
        panelTitle.add(lblTitle, "growx, align center");
    
        bg.add(panelTitle, "growx, h 45!, wrap");
    
        // Panel hướng dẫn
        JPanel instructionPanel = new JPanel(new MigLayout("insets 10, fill"));
        instructionPanel.setBackground(Color.decode("#E3F2FD"));
        instructionPanel.setBorder(BorderFactory.createLineBorder(Color.decode("#2196F3"), 1));
        
        JLabel lblInstruction = new JLabel("<html><b>📋 Workflow:</b><br>" +
                "1. <b>Select from Warehouse:</b> Click 'Browse Warehouse' to choose an item from inventory<br>" +
                "2. <b>Load Basic Info:</b> System will auto-fill Product Name, Category, and Stock quantity<br>" +
                "3. <b>Add Product Details:</b> Fill in Color, Speed, Battery, and Selling Price<br>" +
                "4. <b>Upload Image:</b> Add product photo and click 'Save' to create product</html>");
        lblInstruction.setFont(new Font("Arial", Font.PLAIN, 12));
        lblInstruction.setForeground(Color.decode("#1976D2"));
        instructionPanel.add(lblInstruction, "growx");
        
        bg.add(instructionPanel, "growx, h 90!, wrap");
    
        // Panel chính với 2 cột: Warehouse Selection + Product Details
        JPanel mainPanel = new JPanel(new MigLayout("fill, insets 0", "[300!][grow]", "[grow]"));
        mainPanel.setBackground(Color.WHITE);
        
        // Panel bên trái: Warehouse Selection
        JPanel warehousePanel = createWarehouseSelectionPanel();
        mainPanel.add(warehousePanel, "growy");
        
        // Panel bên phải: Product Details
        JPanel productPanel = createProductDetailsPanel();
        mainPanel.add(productPanel, "grow");
        
        bg.add(mainPanel, "grow, pushy");
    }
    
    /**
     * Tạo panel chọn sản phẩm từ kho
     */
    private JPanel createWarehouseSelectionPanel() {
        JPanel panel = new JPanel(new MigLayout("fill, insets 15, wrap 1", "[grow]", "[][][grow][]"));
        panel.setBackground(Color.decode("#F5F5F5"));
        panel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.GRAY, 2), 
                "Step 1: Select from Warehouse", 0, 0, new Font("Arial", Font.BOLD, 14), Color.decode("#1976D2")));
        
        // Label hướng dẫn
        JLabel lblStep1 = new JLabel("<html><b>Choose a warehouse item to create product:</b></html>");
        lblStep1.setFont(new Font("Arial", Font.PLAIN, 12));
        lblStep1.setForeground(Color.decode("#424242"));
        panel.add(lblStep1, "growx, wrap");
        
        // Nút Browse Warehouse
        MyButton bntBrowseWarehouse = new MyButton("Browse Warehouse Items", 20);
        bntBrowseWarehouse.setBackgroundColor(Color.decode("#4CAF50"));
        bntBrowseWarehouse.setPressedColor(Color.decode("#45a049"));
        bntBrowseWarehouse.setHoverColor(Color.decode("#66bb6a"));
        bntBrowseWarehouse.setFont(new Font("Arial", Font.BOLD, 14));
        bntBrowseWarehouse.setForeground(Color.WHITE);
        bntBrowseWarehouse.addActionListener(e -> browseWarehouseItems());
        panel.add(bntBrowseWarehouse, "growx, h 40!, wrap");
        
        // Panel hiển thị thông tin warehouse đã chọn
        JPanel selectedInfoPanel = new JPanel(new MigLayout("fill, insets 10, wrap 1", "[grow]", "[][][][][]"));
        selectedInfoPanel.setBackground(Color.WHITE);
        selectedInfoPanel.setBorder(BorderFactory.createTitledBorder("Selected Warehouse Item"));
        
        // Các label hiển thị thông tin
        JLabel lblWarehouseId = new JLabel("Warehouse ID: <i>Not selected</i>");
        JLabel lblWarehouseName = new JLabel("Product Name: <i>Not selected</i>");
        JLabel lblWarehouseCategory = new JLabel("Category: <i>Not selected</i>");
        JLabel lblWarehouseStock = new JLabel("Current Stock: <i>Not selected</i>");
        JLabel lblWarehousePrice = new JLabel("Import Price: <i>Not selected</i>");
        
        for (JLabel lbl : new JLabel[]{lblWarehouseId, lblWarehouseName, lblWarehouseCategory, lblWarehouseStock, lblWarehousePrice}) {
            lbl.setFont(new Font("Arial", Font.PLAIN, 11));
            lbl.setForeground(Color.decode("#666666"));
            selectedInfoPanel.add(lbl, "growx, wrap");
        }
        
        panel.add(selectedInfoPanel, "grow, wrap");
        
        // Nút Load Data
        MyButton bntLoadData = new MyButton("Load to Product Form", 20);
        bntLoadData.setBackgroundColor(Color.decode("#2196F3"));
        bntLoadData.setPressedColor(Color.decode("#1976D2"));
        bntLoadData.setHoverColor(Color.decode("#42A5F5"));
        bntLoadData.setFont(new Font("Arial", Font.BOLD, 14));
        bntLoadData.setForeground(Color.WHITE);
        bntLoadData.addActionListener(e -> loadProductDataFromInventory());
        panel.add(bntLoadData, "growx, h 40!");
        
        // Lưu reference để cập nhật thông tin
        this.selectedWarehouseInfo = new JLabel[]{lblWarehouseId, lblWarehouseName, lblWarehouseCategory, lblWarehouseStock, lblWarehousePrice};
        
        return panel;
    }
    
    /**
     * Tạo panel chi tiết sản phẩm
     */
    private JPanel createProductDetailsPanel() {
        JPanel panel = new JPanel(new MigLayout("fill, insets 15, wrap 2", "[right][grow]", "[][][][][][][][][][]"));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.GRAY, 2), 
                "Step 2: Product Details", 0, 0, new Font("Arial", Font.BOLD, 14), Color.decode("#1976D2")));
        
        // Product ID (read-only, từ warehouse)
        JLabel lblProductID = new JLabel("Product ID:");
        lblProductID.setFont(new Font("Arial", Font.BOLD, 12));
        panel.add(lblProductID);
        
        txtProductID = makeTextField();
        txtProductID.setEditable(false);
        txtProductID.setBackgroundColor(Color.decode("#F0F0F0"));
        panel.add(txtProductID, "growx, wrap");
        
        // Product Name (read-only, từ warehouse)
        JLabel lblProductName = new JLabel("Product Name:");
        lblProductName.setFont(new Font("Arial", Font.BOLD, 12));
        panel.add(lblProductName);
        
        txtProductName = makeTextField();
        txtProductName.setEditable(false);
        txtProductName.setBackgroundColor(Color.decode("#F0F0F0"));
        panel.add(txtProductName, "growx, wrap");
        
        // Category (read-only, từ warehouse)
        JLabel lblCategory = new JLabel("Category:");
        lblCategory.setFont(new Font("Arial", Font.BOLD, 12));
        panel.add(lblCategory);
        
        JPanel categoryPanel = new JPanel(new MigLayout("fill, insets 0"));
        categoryPanel.setBackground(Color.WHITE);
        
        // Menu chọn category (giữ nguyên cách bạn đang làm)
        JMenuBar menuBar = new JMenuBar();
        menu = new JMenu("Choose Category");
        menu.setFont(new Font("Arial", Font.PLAIN, 12));
        try {
            busProduct = new BusProduct();
            java.util.List<com.Admin.category.DTO.DTOCategory> listCategory = busProduct.getAllCategoriesWithSupplier();
            Map<String, JMenu> supplierMenuMap = new LinkedHashMap<>();
            for (com.Admin.category.DTO.DTOCategory dto : listCategory) {
                String supName = dto.getSupName() == null ? "Unknown Supplier" : dto.getSupName();
                JMenu supMenu = supplierMenuMap.computeIfAbsent(supName, k -> {
                    JMenu m = new JMenu(k);
                    m.setFont(new Font("Arial", Font.PLAIN, 11));
                    menuBar.add(m);
                    return m;
                });
                String categoryID = String.valueOf(dto.getCategoryID());
                String categoryName = dto.getCategoryName();
                JMenuItem categoryItem = new JMenuItem(categoryName + " (ID: " + categoryID + ")");
                categoryItem.setFont(new Font("Arial", Font.PLAIN, 11));
                categoryItem.addActionListener(e -> menu.setText(categoryID));
                supMenu.add(categoryItem);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            CustomDialog.showError("Không tải được danh mục: " + ex.getMessage());
        }
        menuBar.add(menu);
        categoryPanel.add(menuBar, "growx");
        panel.add(categoryPanel, "growx, wrap");
        
        // Color (cần nhập)
        JLabel lblColor = new JLabel("Color *:");
        lblColor.setFont(new Font("Arial", Font.BOLD, 12));
        panel.add(lblColor);
        
        txtColor = makeTextField();
        panel.add(txtColor, "growx, wrap");
        
        // Speed (cần nhập)
        JLabel lblSpeed = new JLabel("Speed *:");
        lblSpeed.setFont(new Font("Arial", Font.BOLD, 12));
        panel.add(lblSpeed);
        
        txtSpeed = makeTextField();
        panel.add(txtSpeed, "growx, wrap");
        
        // Battery Capacity (cần nhập)
        JLabel lblBattery = new JLabel("Battery Capacity *:");
        lblBattery.setFont(new Font("Arial", Font.BOLD, 12));
        panel.add(lblBattery);
        
        txtBattery = makeTextField();
        panel.add(txtBattery, "growx, wrap");
        
        // Price (cần nhập)
        JLabel lblPrice = new JLabel("Selling Price *:");
        lblPrice.setFont(new Font("Arial", Font.BOLD, 12));
        panel.add(lblPrice);
        
        txtPrice = makeTextField();
        panel.add(txtPrice, "growx, wrap");
        
        // Quantity (read-only, từ warehouse)
        JLabel lblQuantity = new JLabel("Stock Quantity:");
        lblQuantity.setFont(new Font("Arial", Font.BOLD, 12));
        panel.add(lblQuantity);
        
        spinnerQuantity = new JSpinner(new SpinnerNumberModel(1, 1, 1000000, 1));
        spinnerQuantity.setEnabled(false);
        panel.add(spinnerQuantity, "growx, wrap");
        
        // Upload Image
        JLabel lblImage = new JLabel("Product Image *:");
        lblImage.setFont(new Font("Arial", Font.BOLD, 12));
        panel.add(lblImage);
        
        bntupload = new MyButton("Upload Image", 20);
        bntupload.setBackgroundColor(Color.WHITE);
        bntupload.setPressedColor(Color.decode("#D3D3D3"));
        bntupload.setHoverColor(Color.decode("#EEEEEE"));
        bntupload.setFont(new Font("Arial", Font.BOLD, 12));
        bntupload.setButtonIcon("src\\main\\resources\\Icons\\Admin_icon\\upload_image.png",
                30, 30, 10, SwingConstants.RIGHT, SwingConstants.CENTER);
        panel.add(bntupload, "growx, wrap");
        
        // Buttons
        JPanel buttonPanel = new JPanel(new MigLayout("insets 0, gap 10", "[grow][grow]", "[]"));
        buttonPanel.setBackground(Color.WHITE);
        
        bntSave = new MyButton("Save Product", 20);
        bntSave.setBackgroundColor(Color.decode("#E55454"));
        bntSave.setPressedColor(Color.decode("#C04444"));
        bntSave.setHoverColor(Color.decode("#FF7F7F"));
        bntSave.setFont(new Font("Arial", Font.BOLD, 14));
        bntSave.setForeground(Color.WHITE);
        buttonPanel.add(bntSave, "growx");
        
        bntReset = new MyButton("Reset Form", 20);
        bntReset.setBackgroundColor(Color.WHITE);
        bntReset.setPressedColor(Color.decode("#D3D3D3"));
        bntReset.setHoverColor(Color.decode("#EEEEEE"));
        bntReset.setFont(new Font("Arial", Font.BOLD, 14));
        bntReset.addActionListener(e -> resetForm());
        buttonPanel.add(bntReset, "growx");
        
        panel.add(buttonPanel, "span 2, growx, wrap");
        
        return panel;
    }
    
    // Biến để lưu reference đến các label hiển thị thông tin warehouse
    private JLabel[] selectedWarehouseInfo;
    

    private MyTextField makeTextField() {
        MyTextField field = new MyTextField();
        field.setBorder(BorderFactory.createLineBorder(Color.GRAY, 2));
        field.setTextFont(new Font("Times New Roman", Font.PLAIN, 15));
        field.setBackgroundColor(Color.decode("#F0FFFF"));
        return field;
    }

    public void saveProductFromGUI() {
        String productId = txtProductID.getText().trim();
        String productName = txtProductName.getText().trim();
        String color = txtColor.getText().trim();
        String speed = txtSpeed.getText().trim();
        String batteryCapacity = txtBattery.getText().trim();
        String categoryId = menu.getText().trim();
        String priceStr = txtPrice.getText().trim();
        int quantity = (int) spinnerQuantity.getValue();
        // Use GUI image field first, fall back to busProduct if set
        String imagePath = (image != null && !image.isEmpty()) ? image : busProduct.getImagePath();

        if (productId.isEmpty() || productName.isEmpty() || color.isEmpty()
                || speed.isEmpty() || batteryCapacity.isEmpty()
                || categoryId.isEmpty() || priceStr.isEmpty()) {
            CustomDialog.showError("Please fill in all required fields!");
            return;
        }

        double price;
        try {
            price = Double.parseDouble(priceStr);
            if (price < 0) {
                CustomDialog.showError("Price must be >= 0!");
                return;
            }
        } catch (NumberFormatException e) {
            CustomDialog.showError("Invalid price format!");
            return;
        }

        if (imagePath == null || imagePath.isEmpty()) {
            CustomDialog.showError("Please upload an image!");
            return;
        }

        DTOProduct product = new DTOProduct(
                productId, productName, color, speed, batteryCapacity,
                quantity, categoryId, imagePath, price
        );

        busProduct.saveProduct(product);
    }

    private void loadProductDataFromInventory() {
        String warehouseItemId = txtProductID.getText().trim();
        if (warehouseItemId.isEmpty()) {
            CustomDialog.showError("Please enter Warehouse Item ID!");
            return;
        }
        
        // Kiểm tra busProduct đã được khởi tạo chưa
        if (busProduct == null) {
            busProduct = new BusProduct();
        }
        
        // Kiểm tra sản phẩm có tồn tại trong bảng Product_Stock (kho) không
        System.out.println("🔍 Looking for warehouse item: " + warehouseItemId);
        DTOProduct warehouseProduct = busProduct.getProductFromWarehouse(warehouseItemId);
        if (warehouseProduct != null) {
            System.out.println("✅ Found warehouse item: " + warehouseProduct.getProductName());
            // Nếu có trong kho, điền thông tin từ kho
            txtProductName.setText(warehouseProduct.getProductName());
            // Color, Speed, Battery_Capacity không có trong kho mới - để trống để admin nhập
            txtColor.setText("");
            txtSpeed.setText("");
            txtBattery.setText("");
            // Giá bán để trống để admin nhập
            txtPrice.setText("");
            // Số lượng từ kho
            spinnerQuantity.setValue(warehouseProduct.getQuantity());
            menu.setText(warehouseProduct.getCategoryId());
            
            CustomDialog.showSuccess("Warehouse data loaded successfully!\nPlease fill in Color, Speed, Battery, and Selling price.");
        } else {
            System.out.println("❌ Warehouse item not found: " + warehouseItemId);
            CustomDialog.showError("Warehouse Item ID not found!\nPlease import it in the Inventory module first.");
        }
    }
    
    /**
     * Browse warehouse items to select from
     */
    private void browseWarehouseItems() {
        // Kiểm tra busProduct đã được khởi tạo chưa
        if (busProduct == null) {
            busProduct = new BusProduct();
        }
        
        // Tạo dialog để hiển thị danh sách warehouse items
        JDialog browseDialog = new JDialog(this, "Select Warehouse Item", true);
        browseDialog.setSize(800, 600);
        browseDialog.setLocationRelativeTo(this);
        
        // Tạo bảng hiển thị warehouse items
        String[] columnNames = {"Warehouse ID", "Product Name", "Category", "Supplier", "Stock", "Import Price"};
        DefaultTableModel model = new DefaultTableModel(columnNames, 0);
        JTable table = new JTable(model);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        // Load dữ liệu warehouse
        try {
            // Sử dụng BUSInventory để lấy dữ liệu warehouse
            com.Admin.inventory.BUS.BUSInventory busInventory = new com.Admin.inventory.BUS.BUSInventory();
            busInventory.loadInventoryToTable(model);
        } catch (Exception e) {
            e.printStackTrace();
            CustomDialog.showError("Error loading warehouse data: " + e.getMessage());
            return;
        }
        
        JScrollPane scrollPane = new JScrollPane(table);
        browseDialog.add(scrollPane, BorderLayout.CENTER);
        
        // Panel nút
        JPanel buttonPanel = new JPanel(new FlowLayout());
        MyButton btnSelect = new MyButton("Select", 20);
        btnSelect.setBackgroundColor(Color.decode("#4CAF50"));
        btnSelect.setForeground(Color.WHITE);
        btnSelect.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow >= 0) {
                String warehouseId = table.getValueAt(selectedRow, 0).toString();
                String productName = table.getValueAt(selectedRow, 1).toString();
                String category = table.getValueAt(selectedRow, 2).toString();
                String supplier = table.getValueAt(selectedRow, 3).toString();
                String stock = table.getValueAt(selectedRow, 4).toString();
                String importPrice = table.getValueAt(selectedRow, 5).toString();
                
                // Cập nhật thông tin warehouse đã chọn
                updateSelectedWarehouseInfo(warehouseId, productName, category, supplier, stock, importPrice);
                
                browseDialog.dispose();
            } else {
                CustomDialog.showError("Please select a warehouse item!");
            }
        });
        
        MyButton btnCancel = new MyButton("Cancel", 20);
        btnCancel.setBackgroundColor(Color.decode("#f44336"));
        btnCancel.setForeground(Color.WHITE);
        btnCancel.addActionListener(e -> browseDialog.dispose());
        
        buttonPanel.add(btnSelect);
        buttonPanel.add(btnCancel);
        browseDialog.add(buttonPanel, BorderLayout.SOUTH);
        
        browseDialog.setVisible(true);
    }
    
    /**
     * Cập nhật thông tin warehouse đã chọn
     */
    private void updateSelectedWarehouseInfo(String warehouseId, String productName, String category, String supplier, String stock, String importPrice) {
        if (selectedWarehouseInfo != null && selectedWarehouseInfo.length >= 5) {
            selectedWarehouseInfo[0].setText("<html>Warehouse ID: <b>" + warehouseId + "</b></html>");
            selectedWarehouseInfo[1].setText("<html>Product Name: <b>" + productName + "</b></html>");
            selectedWarehouseInfo[2].setText("<html>Category: <b>" + category + "</b></html>");
            selectedWarehouseInfo[3].setText("<html>Current Stock: <b>" + stock + "</b></html>");
            selectedWarehouseInfo[4].setText("<html>Import Price: <b>" + importPrice + "</b></html>");
        }
        
        // Cập nhật Product ID field
        txtProductID.setText(warehouseId);
    }
    
    private void resetForm() {
        txtProductID.setText("");
        txtProductName.setText("");
        txtColor.setText("");
        txtBattery.setText("");
        txtSpeed.setText("");
        txtPrice.setText("");
        spinnerQuantity.setValue(1);
        image = null;
        if (menu != null) menu.setText("Choose Category");
        
        // Reset warehouse info
        if (selectedWarehouseInfo != null && selectedWarehouseInfo.length >= 5) {
            selectedWarehouseInfo[0].setText("Warehouse ID: <i>Not selected</i>");
            selectedWarehouseInfo[1].setText("Product Name: <i>Not selected</i>");
            selectedWarehouseInfo[2].setText("Category: <i>Not selected</i>");
            selectedWarehouseInfo[3].setText("Current Stock: <i>Not selected</i>");
            selectedWarehouseInfo[4].setText("Import Price: <i>Not selected</i>");
        }
    }

    @SuppressWarnings("unchecked")
    private void initComponents() {
        jMenuItem1 = new javax.swing.JMenuItem();
        bg = new javax.swing.JLayeredPane();

        jMenuItem1.setText("jMenuItem1");
        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        bg.setBackground(new java.awt.Color(255, 255, 255));
        bg.setOpaque(true);

        javax.swing.GroupLayout bgLayout = new javax.swing.GroupLayout(bg);
        bg.setLayout(bgLayout);
        bgLayout.setHorizontalGroup(
                bgLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGap(0, 1414, Short.MAX_VALUE)
        );
        bgLayout.setVerticalGroup(
                bgLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGap(0, 483, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(bg)
        );
        layout.setVerticalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(bg)
        );

        pack();
        setLocationRelativeTo(null);
    }

    public static void main(String args[]) {
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception ignored) {}

        java.awt.EventQueue.invokeLater(() -> new NewProduct().setVisible(true));
    }

    private javax.swing.JLayeredPane bg;
    private javax.swing.JMenuItem jMenuItem1;
}
