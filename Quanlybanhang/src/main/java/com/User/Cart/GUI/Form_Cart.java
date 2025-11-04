package com.User.Cart.GUI;

import com.ComponentandDatabase.Components.CustomDialog;
import com.ComponentandDatabase.Components.MyButton;
import com.ComponentandDatabase.Components.MyRadioButton;
import com.User.order.DTO.DTO_OrderDetails;
import com.User.order.BUS.BUS_OrderDetails;
import com.User.order.DTO.DTO_Order;
import com.User.order.BUS.BUS_Order;
import com.User.home.DTO.productDTO;
import com.User.home.GUI.productDeteails;
import com.User.home.GUI.CartUpdateListener;
import com.User.order.GUI.OrderUpdateListener;
import com.User.order.GUI.Order_Form;
import com.User.home.BUS.productBUS;
import com.User.Cart.BUS.BUSCart;
import com.User.Cart.DTO.DTOCart;
import javax.swing.JPanel;
import javax.swing.JLabel;
import java.awt.Color;
import java.awt.Font;
import java.math.BigDecimal;
import javax.swing.BorderFactory;
import javax.swing.SwingConstants;
import java.awt.Dimension;
import javax.swing.*;
import javax.swing.Box;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.JFormattedTextField;
import java.awt.*;
import java.util.ArrayList;

public class Form_Cart extends JPanel implements CartUpdateListener {
    private JPanel panelShow;
    private JLabel lblPayment;
    private JScrollPane scrollShow;
    private productBUS proBUS;
    private MyButton bntOrder, bntSelectAll, bntClearSelection;
    private BUSCart cartBUS;
    private String currentCustomerID; // Thêm biến để lưu ID khách hàng hiện tại 
    private MyRadioButton momo, cash;
    private BUS_Order busOrder;
    private BUS_OrderDetails busOrderDetails;
    public  ArrayList<DTOCart> cartItems;
    ArrayList<productDTO> productsInCart;
    private OrderUpdateListener orderUpdateListener;
    private ArrayList<JCheckBox> productCheckboxes; // Danh sách checkbox cho từng sản phẩm

    public Form_Cart(String customerID) {
        this.currentCustomerID = customerID;
        this.cartBUS = new BUSCart();
        this.productCheckboxes = new ArrayList<>();
        initComponents();
        initProductDisplayArea();
        updateProductList();
        productDeteails.addCartUpdateListener(this);
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(1200, 800));
        setBackground(Color.decode("#F8F9FA"));
        
        // Tạo header section
        createHeaderSection();
        
        // Tạo main content area
        createMainContentArea();
        
        // Tạo footer section
        createFooterSection();
    }
      public void setOrderUpdateListener(OrderUpdateListener listener) {
        this.orderUpdateListener = listener;
    }
    
    // Tạo header section với title và action buttons
    private void createHeaderSection() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setPreferredSize(new Dimension(0, 80));
        headerPanel.setBackground(Color.WHITE);
        headerPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, Color.decode("#E0E0E0")),
            BorderFactory.createEmptyBorder(15, 20, 15, 20)
        ));
        
        // Title section
        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        titlePanel.setOpaque(false);
        
        JLabel titleLabel = new JLabel("Shopping Cart");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(Color.decode("#2C3E50"));
        
        JLabel subtitleLabel = new JLabel("Manage your selected items");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitleLabel.setForeground(Color.decode("#7F8C8D"));
        subtitleLabel.setBorder(BorderFactory.createEmptyBorder(5, 10, 0, 0));
        
        titlePanel.add(titleLabel);
        titlePanel.add(subtitleLabel);
        
        // Action buttons section
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonPanel.setOpaque(false);
        
        bntSelectAll = new MyButton("Select All", 15);
        bntSelectAll.setBackgroundColor(Color.decode("#27AE60"));
        bntSelectAll.setHoverColor(Color.decode("#2ECC71"));
        bntSelectAll.setPressedColor(Color.decode("#229954"));
        bntSelectAll.setFont(new Font("Segoe UI", Font.BOLD, 14));
        bntSelectAll.setForeground(Color.WHITE);
        bntSelectAll.setPreferredSize(new Dimension(160, 40));
        bntSelectAll.setBorder(BorderFactory.createEmptyBorder(8, 20, 8, 20));
        bntSelectAll.setButtonIcon("src\\main\\resources\\Icons\\User_icon\\select.jpg", 
                                    18, 18, 5, SwingConstants.LEFT, SwingConstants.CENTER);
        bntSelectAll.addActionListener(e -> selectAllProducts());
        
        bntClearSelection = new MyButton("Clear", 15);
        bntClearSelection.setBackgroundColor(Color.decode("#E67E22"));
        bntClearSelection.setHoverColor(Color.decode("#F39C12"));
        bntClearSelection.setPressedColor(Color.decode("#D68910"));
        bntClearSelection.setFont(new Font("Segoe UI", Font.BOLD, 14));
        bntClearSelection.setForeground(Color.WHITE);
        bntClearSelection.setPreferredSize(new Dimension(100, 40));
        bntClearSelection.setBorder(BorderFactory.createEmptyBorder(8, 20, 8, 20));
        bntClearSelection.setButtonIcon("src\\main\\resources\\Icons\\User_icon\\clear.png", 
                                         18, 18, 5, SwingConstants.LEFT, SwingConstants.CENTER);
        bntClearSelection.addActionListener(e -> clearSelection());
        
        buttonPanel.add(bntSelectAll);
        buttonPanel.add(bntClearSelection);
        
        headerPanel.add(titlePanel, BorderLayout.WEST);
        headerPanel.add(buttonPanel, BorderLayout.EAST);
        
        add(headerPanel, BorderLayout.NORTH);
    }
    
    // Tạo main content area
    private void createMainContentArea() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 20));
        
        // Tạo scroll pane cho products
        panelShow = new JPanel();
        panelShow.setLayout(new GridLayout(0, 3, 15, 15));
        panelShow.setBackground(Color.WHITE);
        
        scrollShow = new JScrollPane(panelShow);
        scrollShow.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollShow.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollShow.setBorder(BorderFactory.createLineBorder(Color.decode("#E0E0E0"), 1));
        scrollShow.setBackground(Color.WHITE);
        
        mainPanel.add(scrollShow, BorderLayout.CENTER);
        add(mainPanel, BorderLayout.CENTER);
    }
    
    // Tạo footer section với payment và order
    private void createFooterSection() {
        JPanel footerPanel = new JPanel(new BorderLayout());
        footerPanel.setPreferredSize(new Dimension(0, 120));
        footerPanel.setBackground(Color.WHITE);
        footerPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, Color.decode("#E0E0E0")),
            BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));
        
        // Payment section
        JPanel paymentSection = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 0));
        paymentSection.setOpaque(false);
        
        lblPayment = new JLabel("Payment Method:");
        lblPayment.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblPayment.setForeground(Color.decode("#2C3E50"));
        
        // Momo payment option
        JPanel momoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        momoPanel.setBackground(Color.WHITE);
        momoPanel.setPreferredSize(new Dimension(180, 50));
        
        JLabel momoIcon = new JLabel(loadScaledIcon("/Icons/User_icon/momo.png", 25, 25));
        momo = new MyRadioButton("Momo", null, 0, "Select Momo");
        momo.setFont(new Font("Segoe UI", Font.BOLD, 14));
        momo.setForeground(Color.decode("#2C3E50"));
        
        momoPanel.add(momoIcon);
        momoPanel.add(momo);
        
        // Cash payment option
        JPanel cashPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        cashPanel.setBackground(Color.WHITE);
        cashPanel.setPreferredSize(new Dimension(180, 50));
        
        JLabel cashIcon = new JLabel(loadScaledIcon("/Icons/User_icon/cash.png", 25, 25));
        cash = new MyRadioButton("Cash", null, 0, "Select Cash");
        cash.setFont(new Font("Segoe UI", Font.BOLD, 14));
        cash.setForeground(Color.decode("#2C3E50"));
        
        cashPanel.add(cashIcon);
        cashPanel.add(cash);
        
        // Button group
        ButtonGroup group = new ButtonGroup();
        group.add(momo);
        group.add(cash);
        
        paymentSection.add(lblPayment);
        paymentSection.add(momoPanel);
        paymentSection.add(cashPanel);
        
        // Order button
        bntOrder = new MyButton("Order", 20);
        bntOrder.setBackgroundColor(Color.decode("#E74C3C"));
        bntOrder.setPressedColor(Color.decode("#C0392B"));
        bntOrder.setHoverColor(Color.decode("#EC7063"));
        bntOrder.setFont(new Font("Segoe UI", Font.BOLD, 16));
        bntOrder.setForeground(Color.WHITE);
        bntOrder.setPreferredSize(new Dimension(180, 50));
        bntOrder.setBorder(BorderFactory.createEmptyBorder(10, 25, 10, 25));
        bntOrder.setButtonIcon("src\\main\\resources\\Icons\\User_icon\\success.png", 
                                20, 20, 5, SwingConstants.LEFT, SwingConstants.CENTER);
        bntOrder.addActionListener((e) -> {
            Order();
        });
        
        footerPanel.add(paymentSection, BorderLayout.WEST);
        footerPanel.add(bntOrder, BorderLayout.EAST);
        
        add(footerPanel, BorderLayout.SOUTH);
    }
    
    
    public void updateProductList() {
        panelShow.removeAll();
        productCheckboxes.clear(); // Clear danh sách checkbox
        proBUS = new productBUS();
        
        // Lấy danh sách sản phẩm trong giỏ hàng
        cartItems = cartBUS.getCartItemsByCustomer(currentCustomerID);
        productsInCart = new ArrayList<>();
        
        // Lấy thông tin chi tiết của từng sản phẩm trong giỏ hàng
        for (DTOCart cartItem : cartItems) {
            productDTO product = proBUS.getProductById(cartItem.getProductID());
            if (product != null) {
                // Cập nhật số lượng theo giỏ hàng
                product.setQuantity(cartItem.getQuantity());
                productsInCart.add(product);
            }
        }
        
        displayProducts(productsInCart);
    }

    private void initProductDisplayArea() {
        // Method này không cần thiết nữa vì layout đã được tạo trong initComponents
        // Giữ lại để tương thích với code cũ
    }

    
    public void displayProducts(ArrayList<productDTO> products) {
        panelShow.removeAll();

        if (products.isEmpty()) {
            // Khi empty, đổi layout để emptyPanel chiếm toàn bộ không gian
            panelShow.setLayout(new BorderLayout());
            panelShow.setBackground(Color.WHITE);
            
            // Thiết kế empty cart với layout mới - căn giữa hoàn toàn
            JPanel emptyPanel = new JPanel(new BorderLayout());
            emptyPanel.setBackground(Color.WHITE);
            emptyPanel.setBorder(BorderFactory.createEmptyBorder(60, 60, 60, 60));

            // Wrapper panel để căn giữa cả chiều ngang và dọc
            JPanel centerWrapper = new JPanel(new GridBagLayout());
            centerWrapper.setBackground(Color.WHITE);
            centerWrapper.setOpaque(false);
            
            // Icon và text container
            JPanel contentPanel = new JPanel();
            contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
            contentPanel.setOpaque(false);
            contentPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

            // Icon lớn - sử dụng ảnh từ resources
            JLabel emptyIcon = new JLabel(loadScaledIcon("/Icons/User_icon/cart.png", 120, 120), SwingConstants.CENTER);
            emptyIcon.setAlignmentX(Component.CENTER_ALIGNMENT);
            emptyIcon.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
            
            // Text chính
            JLabel noProducts = new JLabel("Your Shopping Cart is Empty", SwingConstants.CENTER);
            noProducts.setFont(new Font("Segoe UI", Font.BOLD, 32));
            noProducts.setForeground(Color.decode("#2C3E50"));
            noProducts.setAlignmentX(Component.CENTER_ALIGNMENT);
            
            // Text phụ
            JLabel subText = new JLabel("Browse our products and add items to your cart to get started", SwingConstants.CENTER);
            subText.setFont(new Font("Segoe UI", Font.PLAIN, 18));
            subText.setForeground(Color.decode("#7F8C8D"));
            subText.setAlignmentX(Component.CENTER_ALIGNMENT);

            contentPanel.add(emptyIcon);
            contentPanel.add(Box.createVerticalStrut(20));
            contentPanel.add(noProducts);
            contentPanel.add(Box.createVerticalStrut(15));
            contentPanel.add(subText);

            // Thêm contentPanel vào centerWrapper để căn giữa hoàn toàn
            centerWrapper.add(contentPanel);
            emptyPanel.add(centerWrapper, BorderLayout.CENTER);
            panelShow.add(emptyPanel, BorderLayout.CENTER);

        } else {
            // Khi có sản phẩm, dùng GridLayout để hiển thị
            panelShow.setLayout(new GridLayout(0, 3, 15, 15));
            panelShow.setBackground(Color.WHITE);
            
            // Hiển thị sản phẩm với grid layout
            for (productDTO product : products) {
                JPanel productPanel = createProductPanel(product);
                panelShow.add(productPanel);
            }
        }

        panelShow.revalidate();
        panelShow.repaint();
    }

     
   private JPanel createProductPanel(productDTO product) {
    JPanel panelcreate = new JPanel(new BorderLayout(8, 8));
        panelcreate.setPreferredSize(new Dimension(300, 380));
        panelcreate.setBackground(Color.WHITE);
        panelcreate.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.decode("#E0E0E0"), 1),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));

        // Top panel: Checkbox bên phải, Image ở giữa
        JPanel topPanel = new JPanel(new BorderLayout(5, 5));
        topPanel.setBackground(Color.WHITE);
        
        // Checkbox để chọn sản phẩm - đặt bên phải
        JCheckBox selectCheckbox = new JCheckBox("Select");
        selectCheckbox.setFont(new Font("Segoe UI", Font.BOLD, 11));
        selectCheckbox.setForeground(Color.decode("#27AE60"));
        selectCheckbox.setBackground(Color.WHITE);
        selectCheckbox.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        productCheckboxes.add(selectCheckbox);
        
        // Panel chứa checkbox ở góc phải trên
        JPanel checkboxPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        checkboxPanel.setBackground(Color.WHITE);
        checkboxPanel.setOpaque(false);
        checkboxPanel.add(selectCheckbox);
        topPanel.add(checkboxPanel, BorderLayout.NORTH);

        // Product Image ở giữa với border màu
        ImageIcon icon = new ImageIcon(product.getImage());
        Image img = icon.getImage().getScaledInstance(200, 140, Image.SCALE_SMOOTH);
        JLabel imageLabel = new JLabel(new ImageIcon(img), SwingConstants.CENTER);
        imageLabel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.decode("#1E88E5"), 2), // Border màu xanh đậm
            BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.decode("#E0E0E0"), 1),
                BorderFactory.createEmptyBorder(8, 8, 8, 8)
            )
        ));
        imageLabel.setBackground(Color.decode("#FAFAFA"));
        imageLabel.setOpaque(true);
        topPanel.add(imageLabel, BorderLayout.CENTER);
        panelcreate.add(topPanel, BorderLayout.NORTH);

        // Product Details - căn trái
        JPanel detailsPanel = new JPanel();
        detailsPanel.setLayout(new BoxLayout(detailsPanel, BoxLayout.Y_AXIS));
        detailsPanel.setBackground(Color.WHITE);
        detailsPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        detailsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Product Name - màu đặc biệt (xanh đậm)
        JLabel nameLabel = new JLabel(product.getProductName());
        nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        nameLabel.setForeground(Color.decode("#1E88E5")); // Màu xanh đậm cho tên
        nameLabel.setHorizontalAlignment(SwingConstants.LEFT);
        nameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        nameLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 12, 0));
        detailsPanel.add(nameLabel);
        detailsPanel.add(Box.createVerticalStrut(5)); // Thêm khoảng cách

        // Product ID
        JLabel idLabel = new JLabel("ID: " + product.getProductID());
        idLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        idLabel.setForeground(Color.decode("#7F8C8D"));
        idLabel.setHorizontalAlignment(SwingConstants.LEFT);
        idLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        idLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        detailsPanel.add(idLabel);
        detailsPanel.add(Box.createVerticalStrut(5)); // Thêm khoảng cách

        // Price - màu đặc biệt (đỏ cam)
        JLabel priceLabel = new JLabel("Price: " + product.getPrice() + " VNĐ");
        priceLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        priceLabel.setForeground(Color.decode("#F57C00")); // Màu cam đậm cho giá
        priceLabel.setHorizontalAlignment(SwingConstants.LEFT);
        priceLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        priceLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        detailsPanel.add(priceLabel);
        detailsPanel.add(Box.createVerticalStrut(5)); // Thêm khoảng cách

        // Quantity - với JSpinner để có thể chỉnh sửa
        JPanel quantityPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        quantityPanel.setBackground(Color.WHITE);
        quantityPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JLabel quantityLabel = new JLabel("Quantity:");
        quantityLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        quantityLabel.setForeground(Color.decode("#7F8C8D"));
        quantityPanel.add(quantityLabel);
        
        // Lấy số lượng hiện tại trong cart (đã được set trong updateProductList)
        int currentCartQuantity = product.getQuantity(); // Số lượng trong cart
        
        // Lấy stock thực tế từ database (không phải từ product vì đã bị thay đổi)
        int currentStock = cartBUS != null ? cartBUS.getCurrentStock(product.getProductID()) : 0;
        
        // Tạo JSpinner với giá trị hiện tại, min=1, max=stock hiện tại
        SpinnerNumberModel quantityModel = new SpinnerNumberModel(
            currentCartQuantity,  // Giá trị hiện tại
            1,                    // Min
            Math.max(currentStock, currentCartQuantity),  // Max (ít nhất bằng số lượng hiện tại)
            1                    // Step
        );
        JSpinner quantitySpinner = new JSpinner(quantityModel);
        quantitySpinner.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        quantitySpinner.setPreferredSize(new Dimension(60, 25));
        
        // Customize spinner editor
        JComponent editor = quantitySpinner.getEditor();
        if (editor instanceof JSpinner.DefaultEditor) {
            JFormattedTextField textField = ((JSpinner.DefaultEditor) editor).getTextField();
            textField.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            textField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.decode("#E0E0E0"), 1),
                BorderFactory.createEmptyBorder(2, 5, 2, 5)
            ));
        }
        quantityPanel.add(quantitySpinner);
        
        // Nút Update Quantity
        MyButton btnUpdateQty = new MyButton("Update", 8);
        btnUpdateQty.setPreferredSize(new Dimension(70, 25));
        btnUpdateQty.setBackgroundColor(Color.decode("#3498DB"));
        btnUpdateQty.setHoverColor(Color.decode("#2980B9"));
        btnUpdateQty.setPressedColor(Color.decode("#21618C"));
        btnUpdateQty.setForeground(Color.WHITE);
        btnUpdateQty.setFont(new Font("Segoe UI", Font.BOLD, 10));
        btnUpdateQty.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
        btnUpdateQty.addActionListener(e -> {
            int newQuantity = (int) quantitySpinner.getValue();
            updateCartQuantity(product.getProductID(), newQuantity);
        });
        quantityPanel.add(btnUpdateQty);
        
        quantityPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        detailsPanel.add(quantityPanel);

        panelcreate.add(detailsPanel, BorderLayout.CENTER);

        // Action Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 8));
        buttonPanel.setBackground(Color.WHITE);

        MyButton detailBtn = new MyButton("Details", 8);
        detailBtn.setPreferredSize(new Dimension(100, 35));
        detailBtn.setBackgroundColor(Color.decode("#27AE60")); // Màu xanh lá
        detailBtn.setHoverColor(Color.decode("#2ECC71"));
        detailBtn.setPressedColor(Color.decode("#229954"));
        detailBtn.setForeground(Color.WHITE);
        detailBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        detailBtn.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));
        detailBtn.setButtonIcon("src\\main\\resources\\Icons\\Admin_icon\\details.png", 
                                 16, 16, 5, SwingConstants.LEFT, SwingConstants.CENTER);
        detailBtn.addActionListener((e) -> {
            CartDetails details = new CartDetails();
            details.setVisible(true);
            details.displayProductDetails(product);
        });
        buttonPanel.add(detailBtn);
        
        MyButton bntDelete = new MyButton("Delete", 8);
        bntDelete.setPreferredSize(new Dimension(100, 35));
        bntDelete.setBackgroundColor(Color.decode("#E74C3C")); 
        bntDelete.setHoverColor(Color.decode("#C0392B"));      
        bntDelete.setPressedColor(Color.decode("#A93226")); 
        bntDelete.setForeground(Color.WHITE);
        bntDelete.setFont(new Font("Segoe UI", Font.BOLD, 12));
        bntDelete.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));
        bntDelete.setButtonIcon("src\\main\\resources\\Icons\\Admin_icon\\delete.png", 
                                 16, 16, 5, SwingConstants.LEFT, SwingConstants.CENTER);
        bntDelete.addActionListener(e -> {
            boolean confirm = CustomDialog.showOptionPane(
                "Confirm Deletion",
                "Are you sure you want to delete this Product?",
                UIManager.getIcon("OptionPane.questionIcon"),
                Color.decode("#FF6666")
            );
            if (confirm) {
                deleteProductFromCart(product.getProductID());
            }
        });
        buttonPanel.add(bntDelete);
        
        panelcreate.add(buttonPanel, BorderLayout.SOUTH);

        return panelcreate;
 }
   
        // Hàm load ảnh và resize từ resources
    private ImageIcon loadScaledIcon(String resourcePath, int width, int height) {
         try {
             // Load icon từ resources
             java.net.URL iconURL = getClass().getResource(resourcePath);
             if (iconURL != null) {
                 ImageIcon originalIcon = new ImageIcon(iconURL);
                 Image scaledImage = originalIcon.getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH);
                 return new ImageIcon(scaledImage);
             } else {
                 System.err.println("Icon not found: " + resourcePath);
                 // Trả về icon mặc định nếu không tìm thấy
                 return new ImageIcon();
             }
         } catch (Exception e) {
             System.err.println("Error loading icon: " + resourcePath + " - " + e.getMessage());
             return new ImageIcon();
         }
     }

  

    private void addCompactDetail(JPanel panel, String text, int fontStyle, int fontSize) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", fontStyle, fontSize));
        label.setForeground(Color.decode("#7F8C8D"));
        label.setHorizontalAlignment(SwingConstants.CENTER);
        label.setBorder(BorderFactory.createEmptyBorder(2, 0, 2, 0)); // Padding tối ưu
        panel.add(label);
    }
    private String getStatusText(productDTO product) {
        return product.getQuantity() == 0 ? "Out of Stock" : product.getStatus();
    }
    
    // Method để chọn tất cả sản phẩm
    private void selectAllProducts() {
        for (JCheckBox checkbox : productCheckboxes) {
            checkbox.setSelected(true);
        }
    }
    
    // Method để bỏ chọn tất cả sản phẩm
    private void clearSelection() {
        for (JCheckBox checkbox : productCheckboxes) {
            checkbox.setSelected(false);
        }
    }
    
    // Method để lấy danh sách sản phẩm được chọn
    public ArrayList<productDTO> getSelectedProducts() {
        ArrayList<productDTO> selectedProducts = new ArrayList<>();
        for (int i = 0; i < productCheckboxes.size(); i++) {
            if (productCheckboxes.get(i).isSelected() && i < productsInCart.size()) {
                selectedProducts.add(productsInCart.get(i));
            }
        }
        return selectedProducts;
    }
    
    
    private void deleteProductFromCart(String productId) {
        // 1. Xóa từ cơ sở dữ liệu
        boolean success = cartBUS.removeFromCart(currentCustomerID, productId);

        if (success) {
            // 2. Cập nhật giao diện
            updateProductList();
            CustomDialog.showSuccess("This product deleted from cart successfully !");
            
        } else {
            CustomDialog.showError("The product from cart delete failure !");
        }
    }
    
    // Method để update quantity trong cart
    private void updateCartQuantity(String productID, int newQuantity) {
        DTOCart cartItem = new DTOCart(currentCustomerID, productID, newQuantity);
        boolean success = cartBUS.updateCartQuantity(cartItem);
        
        if (success) {
            // Cập nhật lại danh sách sản phẩm
            updateProductList();
            CustomDialog.showSuccess("Quantity updated successfully!");
        } else {
            // Nếu update thất bại, refresh lại để hiển thị giá trị cũ
            updateProductList();
        }
    }
    
     public void Order() {
          // Lấy danh sách sản phẩm được chọn
          ArrayList<productDTO> selectedProducts = getSelectedProducts();
          
          // Kiểm tra nếu không có sản phẩm nào được chọn
          if (selectedProducts == null || selectedProducts.isEmpty()) {
              CustomDialog.showError("Please select at least one product to order!");
              return;
          }

          // Kiểm tra phương thức thanh toán đã được chọn
          String paymentMethod = getSelectedPaymentMethod();
          if (paymentMethod == null) {
              CustomDialog.showError("Please select a payment method!");
              return;
          }

          // Xử lý thanh toán MoMo
          if ("Momo".equals(paymentMethod)) {
              if (!processMoMoPayment(selectedProducts)) {
                  return; // Người dùng hủy thanh toán
              }
          }

          // Tạo Order_No ngẫu nhiên 8 chữ số
          String orderNo = String.format("%08d", new java.util.Random().nextInt(100000000));

          // Lấy thời gian hiện tại
          java.time.LocalDate currentDate = java.time.LocalDate.now();
          java.time.LocalTime currentTime = java.time.LocalTime.now().withNano(0);

          // Tính tổng quantity và tổng price từ các sản phẩm được chọn
          int totalQuantity = 0;
          BigDecimal totalPrice = BigDecimal.ZERO;

          for (productDTO product : selectedProducts) {
              totalQuantity += product.getQuantity();
              totalPrice = totalPrice.add(product.getPrice().multiply(new BigDecimal(product.getQuantity())));
          }

          try {
              // Tìm Cart_ID từ cartItems cho sản phẩm đầu tiên được chọn
              String cartID = null;
              if (cartItems != null && !cartItems.isEmpty() && !selectedProducts.isEmpty()) {
                  String firstProductID = selectedProducts.get(0).getProductID();
                  for (DTOCart item : cartItems) {
                      if (item.getProductID().equals(firstProductID)) {
                          cartID = item.getCartID();
                          break;
                      }
                  }
              }
              
              // Tạo và thêm Order trước
              DTO_Order order = new DTO_Order();
              order.setOrderNo(orderNo);
              order.setCustomerID(currentCustomerID);
              order.setCartID(cartID);
              order.setTotalQuantityProduct(totalQuantity);
              order.setTotalPrice(totalPrice);
              order.setPayment(paymentMethod);
              order.setDateOrder(currentDate);
              order.setTimeOrder(currentTime);

              busOrder = new BUS_Order();
              boolean orderSuccess = busOrder.addOrderDetail(order);

              if (!orderSuccess) {
                  CustomDialog.showError("Failed to create order!");
                  return;
              }

              // Thêm các Order Details chỉ cho sản phẩm được chọn
              boolean allDetailsSuccess = true;
              busOrderDetails = new BUS_OrderDetails();
              ArrayList<String> orderedProductIDs = new ArrayList<>(); // Lưu danh sách sản phẩm đã đặt hàng

              for (productDTO product : selectedProducts) {
                  DTO_OrderDetails orderDetail = new DTO_OrderDetails();
                  orderDetail.setOrderNo(orderNo);
                  orderDetail.setCustomerID(currentCustomerID);
                  orderDetail.setProductID(product.getProductID());
                  orderDetail.setPrice(product.getPrice());
                  orderDetail.setQuantity(product.getQuantity());
                  orderDetail.setDateOrder(currentDate);
                  orderDetail.setTimeOrder(currentTime);
                  orderDetail.setStatus("Waiting");

                  boolean detailSuccess = busOrderDetails.addOrderDetail(orderDetail);
                  if (detailSuccess) {
                      orderedProductIDs.add(product.getProductID());
                  } else {
                      allDetailsSuccess = false;
                      System.err.println("Failed to insert product: " + product.getProductID());
                  }
              }

              if (allDetailsSuccess) {
                  // Chỉ xóa các sản phẩm đã được đặt hàng khỏi cart
                  for (String productID : orderedProductIDs) {
                      cartBUS.removeFromCart(currentCustomerID, productID);
                  }
                  
                  updateProductList();
                  CustomDialog.showSuccess("Products ordered successfully! Order No: " + orderNo);
                  panelShow.setBorder(null);
                  
                  if (orderUpdateListener != null) {
                      orderUpdateListener.onOrderPlaced(currentCustomerID, orderNo);
                  }
                  
                  // Tự động chuyển sang tab Order_Form
                  switchToOrderForm();
              } else {
                  // Nếu có lỗi khi thêm details, xóa order đã tạo
                  CustomDialog.showError("Some items could not be ordered. Please try again!");
              }
          } catch (Exception e) {
              e.printStackTrace();
              CustomDialog.showError("An error occurred while processing your order!");
          }
      }
      
      // Xử lý thanh toán MoMo với dialog đơn giản
      private boolean processMoMoPayment(ArrayList<productDTO> selectedProducts) {
          // Tính tổng tiền
          BigDecimal totalAmount = BigDecimal.ZERO;
          for (productDTO product : selectedProducts) {
              totalAmount = totalAmount.add(product.getPrice().multiply(new BigDecimal(product.getQuantity())));
          }
          
          // Tạo dialog MoMo payment
          JDialog momoDialog = new JDialog((JFrame) SwingUtilities.getWindowAncestor(this), "MoMo Payment", true);
          momoDialog.setSize(450, 700);
          momoDialog.setLocationRelativeTo(this);
          momoDialog.setLayout(new BorderLayout());
          momoDialog.getContentPane().setBackground(Color.WHITE);
          
          // Header panel
          JPanel headerPanel = new JPanel(new BorderLayout());
          headerPanel.setBackground(Color.decode("#AF005F"));
          headerPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
          
          JLabel momoLabel = new JLabel("MoMo Payment", SwingConstants.CENTER);
          momoLabel.setIcon(loadScaledIcon("/Icons/User_icon/momo.png", 24, 24));
          momoLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
          momoLabel.setForeground(Color.WHITE);
          headerPanel.add(momoLabel, BorderLayout.CENTER);
          
          // Content panel
          JPanel contentPanel = new JPanel();
          contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
          contentPanel.setBackground(Color.WHITE);
          contentPanel.setBorder(BorderFactory.createEmptyBorder(30, 30, 20, 30));
          
          JLabel amountLabel = new JLabel("Total Amount:", SwingConstants.LEFT);
          amountLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
          amountLabel.setForeground(Color.decode("#2C3E50"));
          amountLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
          
          JLabel amountValue = new JLabel(totalAmount.toString() + " VNĐ", SwingConstants.LEFT);
          amountValue.setFont(new Font("Segoe UI", Font.BOLD, 20));
          amountValue.setForeground(Color.decode("#E74C3C"));
          amountValue.setAlignmentX(Component.LEFT_ALIGNMENT);
          amountValue.setBorder(BorderFactory.createEmptyBorder(5, 0, 20, 0));
          
          JLabel qrLabel = new JLabel("Scan QR Code to Pay:", SwingConstants.LEFT);
          qrLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
          qrLabel.setForeground(Color.decode("#2C3E50"));
          qrLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
          qrLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
          
          // QR Code placeholder (có thể thay bằng ảnh QR thật)
          JPanel qrPanel = new JPanel();
          qrPanel.setPreferredSize(new Dimension(200, 200));
          qrPanel.setBackground(Color.WHITE);
          qrPanel.setBorder(BorderFactory.createCompoundBorder(
              BorderFactory.createLineBorder(Color.decode("#E0E0E0"), 2),
              BorderFactory.createEmptyBorder(10, 10, 10, 10)
          ));
          
          JLabel qrPlaceholder = new JLabel("QR CODE", SwingConstants.CENTER);
          qrPlaceholder.setFont(new Font("Segoe UI", Font.BOLD, 16));
          qrPlaceholder.setForeground(Color.decode("#7F8C8D"));
          qrPanel.add(qrPlaceholder);
          
          contentPanel.add(amountLabel);
          contentPanel.add(amountValue);
          contentPanel.add(qrLabel);
          contentPanel.add(qrPanel);
          
          // Button panel
          JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
          buttonPanel.setBackground(Color.WHITE);
          buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
          
          MyButton confirmBtn = new MyButton("Confirm Payment", 14);
          confirmBtn.setBackgroundColor(Color.decode("#27AE60"));
          confirmBtn.setHoverColor(Color.decode("#2ECC71"));
          confirmBtn.setPressedColor(Color.decode("#229954"));
          confirmBtn.setForeground(Color.WHITE);
          confirmBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
          confirmBtn.setPreferredSize(new Dimension(150, 40));
          
          MyButton cancelBtn = new MyButton("Cancel", 14);
          cancelBtn.setBackgroundColor(Color.decode("#95A5A6"));
          cancelBtn.setHoverColor(Color.decode("#7F8C8D"));
          cancelBtn.setPressedColor(Color.decode("#5D6D7E"));
          cancelBtn.setForeground(Color.WHITE);
          cancelBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
          cancelBtn.setPreferredSize(new Dimension(100, 40));
          
          final boolean[] paymentConfirmed = {false};
          
          confirmBtn.addActionListener(e -> {
              paymentConfirmed[0] = true;
              momoDialog.dispose();
          });
          
          cancelBtn.addActionListener(e -> {
              paymentConfirmed[0] = false;
              momoDialog.dispose();
          });
          
          buttonPanel.add(confirmBtn);
          buttonPanel.add(cancelBtn);
          
          momoDialog.add(headerPanel, BorderLayout.NORTH);
          momoDialog.add(contentPanel, BorderLayout.CENTER);
          momoDialog.add(buttonPanel, BorderLayout.SOUTH);
          
          momoDialog.setVisible(true);
          
          return paymentConfirmed[0];
      }

      // Hàm lấy phương thức thanh toán được chọn
      private String getSelectedPaymentMethod() {
          if (momo.isSelected()) {
              return "Momo";
          } else if (cash.isSelected()) {
              return "Cash";
          }
          return null;
      }

    
    @Override
    public void onCartUpdated(String customerID) {
        if (this.currentCustomerID.equals(customerID)) {
            // Cập nhật UI trong EDT (Event Dispatch Thread)
            SwingUtilities.invokeLater(() -> {
                updateProductList();
            });
        }
    }

    // Hủy đăng ký khi không cần thiết
    @Override
    public void removeNotify() {
        productDeteails.removeCartUpdateListener(this);
        super.removeNotify();
    }
    
    private void switchToOrderForm() {
        Container parent = getParent();
        while (parent != null) {
            if (parent instanceof JTabbedPane) {
                JTabbedPane tabbedPane = (JTabbedPane) parent;
                for (int i = 0; i < tabbedPane.getTabCount(); i++) {
                    if (tabbedPane.getComponentAt(i) instanceof Order_Form) {
                        tabbedPane.setSelectedIndex(i);
                        break;
                    }
                }
                break;
            }
            parent = parent.getParent();
        }
    }
}