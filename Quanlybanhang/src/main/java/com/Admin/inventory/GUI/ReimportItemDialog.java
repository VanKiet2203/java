package com.Admin.inventory.GUI;

import com.Admin.inventory.BUS.BUSInventory;
import com.ComponentandDatabase.Components.CustomDialog;
import com.ComponentandDatabase.Components.MyButton;
import com.ComponentandDatabase.Components.MyTextField;
import static com.ComponentandDatabase.Components.UIConstants.*;

import javax.swing.*;
import java.awt.*;
import java.math.BigDecimal;

public class ReimportItemDialog extends JDialog {
    private JTextField txtWarehouseId, txtProductName, txtCurrentQuantity;
    private MyTextField txtAdditionalQuantity, txtUnitPrice;
    private MyButton btnConfirm, btnCancel;
    private BUSInventory busInventory;
    private String warehouseId;
    private String productName;
    private int currentQuantity;
    
    public ReimportItemDialog(JFrame parent, String warehouseId, String productName, int currentQuantity) {
        super(parent, "Nhập thêm sản phẩm đã tồn tại", true);
        this.warehouseId = warehouseId;
        this.productName = productName;
        this.currentQuantity = currentQuantity;
        this.busInventory = new BUSInventory();
        
        initComponents();
        setupLayout();
        setupEventHandlers();
    }
    
    private void initComponents() {
        setSize(500, 400);
        setLocationRelativeTo(getParent());
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setResizable(false);
        
        // Initialize text fields
        txtWarehouseId = new JTextField(warehouseId);
        txtWarehouseId.setEditable(false);
        txtWarehouseId.setBackground(Color.LIGHT_GRAY);
        
        txtProductName = new JTextField(productName);
        txtProductName.setEditable(false);
        txtProductName.setBackground(Color.LIGHT_GRAY);
        
        txtCurrentQuantity = new JTextField(String.valueOf(currentQuantity));
        txtCurrentQuantity.setEditable(false);
        txtCurrentQuantity.setBackground(Color.LIGHT_GRAY);
        
        txtAdditionalQuantity = new MyTextField();
        txtAdditionalQuantity.setHint("Nhập số lượng cần thêm");
        txtAdditionalQuantity.setTextFont(FONT_CONTENT_MEDIUM);
        txtAdditionalQuantity.setHintFont(FONT_CONTENT_SMALL);
        
        txtUnitPrice = new MyTextField();
        txtUnitPrice.setHint("Nhập giá nhập mới");
        txtUnitPrice.setTextFont(FONT_CONTENT_MEDIUM);
        txtUnitPrice.setHintFont(FONT_CONTENT_SMALL);
        
        // Initialize buttons
        btnConfirm = new MyButton("Xác nhận nhập thêm", 20);
        btnConfirm.setBackgroundColor(Color.decode("#4CAF50"));
        btnConfirm.setHoverColor(Color.decode("#45A049"));
        btnConfirm.setPressedColor(Color.decode("#3D8B40"));
        btnConfirm.setForeground(Color.WHITE);
        btnConfirm.setFont(FONT_BUTTON_MEDIUM);
        
        btnCancel = new MyButton("Hủy", 20);
        btnCancel.setBackgroundColor(Color.decode("#F44336"));
        btnCancel.setHoverColor(Color.decode("#D32F2F"));
        btnCancel.setPressedColor(Color.decode("#C62828"));
        btnCancel.setForeground(Color.WHITE);
        btnCancel.setFont(FONT_BUTTON_MEDIUM);
    }
    
    private void setupLayout() {
        setLayout(new BorderLayout());
        
        // Main panel
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new GridBagLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        mainPanel.setBackground(Color.WHITE);
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.WEST;
        
        // Title
        JLabel lblTitle = new JLabel("NHẬP THÊM SẢN PHẨM ĐÃ TỒN TẠI");
        lblTitle.setFont(FONT_TITLE_LARGE);
        lblTitle.setForeground(PRIMARY_COLOR);
        lblTitle.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        mainPanel.add(lblTitle, gbc);
        
        // Warehouse ID
        gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.gridx = 0; gbc.gridy = 1;
        mainPanel.add(new JLabel("Mã kho:"), gbc);
        gbc.gridx = 1;
        txtWarehouseId.setPreferredSize(new Dimension(200, 30));
        mainPanel.add(txtWarehouseId, gbc);
        
        // Product Name
        gbc.gridx = 0; gbc.gridy = 2;
        mainPanel.add(new JLabel("Tên sản phẩm:"), gbc);
        gbc.gridx = 1;
        txtProductName.setPreferredSize(new Dimension(200, 30));
        mainPanel.add(txtProductName, gbc);
        
        // Current Quantity
        gbc.gridx = 0; gbc.gridy = 3;
        mainPanel.add(new JLabel("Số lượng hiện tại:"), gbc);
        gbc.gridx = 1;
        txtCurrentQuantity.setPreferredSize(new Dimension(200, 30));
        mainPanel.add(txtCurrentQuantity, gbc);
        
        // Additional Quantity
        gbc.gridx = 0; gbc.gridy = 4;
        mainPanel.add(new JLabel("Số lượng cần thêm:"), gbc);
        gbc.gridx = 1;
        txtAdditionalQuantity.setPreferredSize(new Dimension(200, 30));
        mainPanel.add(txtAdditionalQuantity, gbc);
        
        // Unit Price
        gbc.gridx = 0; gbc.gridy = 5;
        mainPanel.add(new JLabel("Giá nhập mới:"), gbc);
        gbc.gridx = 1;
        txtUnitPrice.setPreferredSize(new Dimension(200, 30));
        mainPanel.add(txtUnitPrice, gbc);
        
        // Buttons panel
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.add(btnConfirm);
        buttonPanel.add(btnCancel);
        
        add(mainPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    private void setupEventHandlers() {
        btnConfirm.addActionListener(e -> confirmReimport());
        btnCancel.addActionListener(e -> dispose());
    }
    
    private void confirmReimport() {
        try {
            // Validate input
            String additionalQtyStr = txtAdditionalQuantity.getText().trim();
            String unitPriceStr = txtUnitPrice.getText().trim();
            
            if (additionalQtyStr.isEmpty() || unitPriceStr.isEmpty()) {
                CustomDialog.showError("Vui lòng nhập đầy đủ thông tin!");
                return;
            }
            
            int additionalQuantity = Integer.parseInt(additionalQtyStr);
            BigDecimal unitPrice = new BigDecimal(unitPriceStr);
            
            if (additionalQuantity <= 0) {
                CustomDialog.showError("Số lượng cần thêm phải lớn hơn 0!");
                return;
            }
            
            if (unitPrice.compareTo(BigDecimal.ZERO) <= 0) {
                CustomDialog.showError("Giá nhập phải lớn hơn 0!");
                return;
            }
            
            // Confirm dialog
            boolean confirm = CustomDialog.showOptionPane(
                "Xác nhận nhập thêm",
                "Bạn có chắc chắn muốn nhập thêm " + additionalQuantity + " sản phẩm với giá " + unitPrice + " VND?",
                UIManager.getIcon("OptionPane.questionIcon"),
                Color.decode("#FF9800")
            );
            
            if (confirm) {
                // Call business logic
                boolean success = busInventory.reimportWarehouseItem(warehouseId, additionalQuantity, unitPrice);
                
                if (success) {
                    CustomDialog.showSuccess("Nhập thêm sản phẩm thành công!\n" +
                        "Mã kho: " + warehouseId + "\n" +
                        "Số lượng thêm: " + additionalQuantity + "\n" +
                        "Tổng số lượng sau khi nhập: " + (currentQuantity + additionalQuantity));
                    dispose();
                }
            }
            
        } catch (NumberFormatException e) {
            CustomDialog.showError("Vui lòng nhập số hợp lệ cho số lượng và giá!");
        } catch (Exception e) {
            CustomDialog.showError("Lỗi khi nhập thêm sản phẩm: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
