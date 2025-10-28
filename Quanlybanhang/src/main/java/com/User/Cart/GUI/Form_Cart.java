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
import java.awt.Desktop;
import java.net.URI;
import java.awt.Font;
import java.math.BigDecimal;
import javax.swing.BorderFactory;
import javax.swing.SwingConstants;
import java.awt.Dimension;
import javax.swing.*;
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
        bntSelectAll.setPreferredSize(new Dimension(120, 40));
        bntSelectAll.setBorder(BorderFactory.createEmptyBorder(8, 20, 8, 20));
        bntSelectAll.addActionListener(e -> selectAllProducts());
        
        bntClearSelection = new MyButton("Clear", 15);
        bntClearSelection.setBackgroundColor(Color.decode("#E67E22"));
        bntClearSelection.setHoverColor(Color.decode("#F39C12"));
        bntClearSelection.setPressedColor(Color.decode("#D68910"));
        bntClearSelection.setFont(new Font("Segoe UI", Font.BOLD, 14));
        bntClearSelection.setForeground(Color.WHITE);
        bntClearSelection.setPreferredSize(new Dimension(100, 40));
        bntClearSelection.setBorder(BorderFactory.createEmptyBorder(8, 20, 8, 20));
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
        momoPanel.setBorder(BorderFactory.createLineBorder(Color.decode("#4A90E2"), 2));
        momoPanel.setPreferredSize(new Dimension(180, 50));
        
        JLabel momoIcon = new JLabel(loadScaledIcon("/Icons/User_icon/momo.png", 25, 25));
        momo = new MyRadioButton("Momo", null, 0, "Select Momo");
        momo.setFont(new Font("Segoe UI", Font.BOLD, 14));
        momo.setForeground(Color.decode("#2C3E50"));
        momo.addActionListener(e -> {
            if (momo.isSelected()) {
                try {
                    Desktop.getDesktop().browse(new URI("https://momo.vn"));
                } catch (Exception ex) {
                    ex.printStackTrace();
                    CustomDialog.showError("Can't open the Momo website !");
                }
            }
        });
        
        momoPanel.add(momoIcon);
        momoPanel.add(momo);
        
        // Cash payment option
        JPanel cashPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        cashPanel.setBackground(Color.WHITE);
        cashPanel.setBorder(BorderFactory.createLineBorder(Color.decode("#27AE60"), 2));
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
        bntOrder = new MyButton("Place Order Now", 20);
        bntOrder.setBackgroundColor(Color.decode("#E74C3C"));
        bntOrder.setPressedColor(Color.decode("#C0392B"));
        bntOrder.setHoverColor(Color.decode("#EC7063"));
        bntOrder.setFont(new Font("Segoe UI", Font.BOLD, 16));
        bntOrder.setForeground(Color.WHITE);
        bntOrder.setPreferredSize(new Dimension(180, 50));
        bntOrder.setBorder(BorderFactory.createEmptyBorder(10, 25, 10, 25));
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
        panelShow.setLayout(new GridLayout(0, 3, 15, 15));

        if (products.isEmpty()) {
            // Thiết kế empty cart với layout mới
            JPanel emptyPanel = new JPanel(new BorderLayout());
            emptyPanel.setBackground(Color.WHITE);
            emptyPanel.setBorder(BorderFactory.createEmptyBorder(60, 60, 60, 60));

            // Icon và text container
            JPanel contentPanel = new JPanel();
            contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
            contentPanel.setOpaque(false);
            contentPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

            // Icon lớn
            JLabel emptyIcon = new JLabel("🛒", SwingConstants.CENTER);
            emptyIcon.setFont(new Font("Segoe UI", Font.PLAIN, 80));
            emptyIcon.setForeground(Color.decode("#4A90E2"));
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

            emptyPanel.add(contentPanel, BorderLayout.CENTER);
            panelShow.add(emptyPanel);

        } else {
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

        // Checkbox để chọn sản phẩm với thiết kế hiện đại
        JCheckBox selectCheckbox = new JCheckBox("✓ Select for Order");
        selectCheckbox.setFont(new Font("Segoe UI", Font.BOLD, 12));
        selectCheckbox.setForeground(Color.decode("#27AE60"));
        selectCheckbox.setBackground(Color.WHITE);
        selectCheckbox.setBorder(BorderFactory.createEmptyBorder(2, 0, 2, 0));
        productCheckboxes.add(selectCheckbox);
        
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(Color.WHITE);
        topPanel.add(selectCheckbox, BorderLayout.WEST);

        // Product Image với thiết kế hiện đại
        ImageIcon icon = new ImageIcon(product.getImage());
        Image img = icon.getImage().getScaledInstance(200, 140, Image.SCALE_SMOOTH);
        JLabel imageLabel = new JLabel(new ImageIcon(img), SwingConstants.CENTER);
        imageLabel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.decode("#E0E0E0"), 1),
            BorderFactory.createEmptyBorder(8, 8, 8, 8)
        ));
        imageLabel.setBackground(Color.decode("#FAFAFA"));
        imageLabel.setOpaque(true);
        topPanel.add(imageLabel, BorderLayout.CENTER);
        panelcreate.add(topPanel, BorderLayout.NORTH);

        // Product Details
        JPanel detailsPanel = new JPanel(new GridLayout(0, 1, 5, 5));
        detailsPanel.setBackground(Color.WHITE);
        detailsPanel.setBorder(BorderFactory.createEmptyBorder(10, 5, 10, 5));

        // Product Name (highlighted) với typography hiện đại
        JLabel nameLabel = new JLabel(product.getProductName());
        nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        nameLabel.setForeground(Color.decode("#2C3E50"));
        nameLabel.setHorizontalAlignment(SwingConstants.CENTER);
        detailsPanel.add(nameLabel);

        addCompactDetail(detailsPanel, "ID: " + product.getProductID(), Font.PLAIN, 11);
        addCompactDetail(detailsPanel, "Price: " + product.getPrice() + " VNĐ", Font.BOLD, 13);
        addCompactDetail(detailsPanel, "Quantity: " + product.getQuantity(), Font.PLAIN, 12);
        
        // Status với màu sắc hiện đại
        JLabel statusLabel = new JLabel(getStatusText(product));
        statusLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        if (product.getQuantity() == 0) {
            statusLabel.setForeground(Color.decode("#E74C3C"));
        } else {
            statusLabel.setForeground(Color.decode("#27AE60"));
        }
        statusLabel.setHorizontalAlignment(SwingConstants.CENTER);
        detailsPanel.add(statusLabel);

        panelcreate.add(detailsPanel, BorderLayout.CENTER);

        // Action Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 8));
        buttonPanel.setBackground(Color.WHITE);

        MyButton detailBtn = new MyButton("👁 Details", 8);
        detailBtn.setPreferredSize(new Dimension(100, 35));
        detailBtn.setBackgroundColor(Color.decode("#3498DB"));
        detailBtn.setHoverColor(Color.decode("#2980B9"));
        detailBtn.setPressedColor(Color.decode("#21618C"));
        detailBtn.setForeground(Color.WHITE);
        detailBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        detailBtn.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));
        detailBtn.addActionListener((e) -> {
            CartDetails details = new CartDetails();
            details.setVisible(true);
            details.displayProductDetails(product);
        });
        buttonPanel.add(detailBtn);
        
        MyButton bntDelete = new MyButton("🗑 Delete", 8);
        bntDelete.setPreferredSize(new Dimension(100, 35));
        bntDelete.setBackgroundColor(Color.decode("#E74C3C")); 
        bntDelete.setHoverColor(Color.decode("#C0392B"));      
        bntDelete.setPressedColor(Color.decode("#A93226")); 
        bntDelete.setForeground(Color.WHITE);
        bntDelete.setFont(new Font("Segoe UI", Font.BOLD, 12));
        bntDelete.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));
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
    
     public void Order() {
          // Kiểm tra nếu giỏ hàng trống
          if (productsInCart == null || productsInCart.isEmpty()) {
              CustomDialog.showError("Your cart is empty!");
              return;
          }

          // Kiểm tra phương thức thanh toán đã được chọn
          String paymentMethod = getSelectedPaymentMethod();
          if (paymentMethod == null) {
              CustomDialog.showError("Please select a payment method!");
              return;
          }

          // Tạo Order_No ngẫu nhiên 8 chữ số
          String orderNo = String.format("%08d", new java.util.Random().nextInt(100000000));

          // Lấy thời gian hiện tại
          java.time.LocalDate currentDate = java.time.LocalDate.now();
          java.time.LocalTime currentTime = java.time.LocalTime.now().withNano(0);

          // Tính tổng quantity và tổng price
          int totalQuantity = 0;
          BigDecimal totalPrice = BigDecimal.ZERO;

          for (productDTO product : productsInCart) {
              totalQuantity += product.getQuantity();
              totalPrice = totalPrice.add(product.getPrice().multiply(new BigDecimal(product.getQuantity())));
          }

          try {
              // Lấy Cart_ID từ cartItems (lấy Cart_ID đầu tiên)
              String cartID = null;
              if (cartItems != null && !cartItems.isEmpty()) {
                  cartID = cartItems.get(0).getCartID();
              }
              
              // Tạo và thêm Order trước
              DTO_Order order = new DTO_Order();
              order.setOrderNo(orderNo);
              order.setCustomerID(currentCustomerID);
              order.setCartID(cartID);  // Set Cart_ID
              order.setTotalQuantityProduct(totalQuantity);
              order.setTotalPrice(totalPrice);
              order.setPayment(paymentMethod);  // Sử dụng paymentMethod đã lấy
              order.setDateOrder(currentDate);
              order.setTimeOrder(currentTime);

               busOrder = new BUS_Order();
              boolean orderSuccess = busOrder.addOrderDetail(order);

              if (!orderSuccess) {
                  CustomDialog.showError("Failed to create order!");
                  return;
              }

              // Thêm các Order Details
              boolean allDetailsSuccess = true;
               busOrderDetails = new BUS_OrderDetails();

              for (productDTO product : productsInCart) {
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
                  if (!detailSuccess) {
                      allDetailsSuccess = false;
                      System.err.println("Failed to insert product: " + product.getProductID());
                  }
              }

              if (allDetailsSuccess) {
                  // Xóa giỏ hàng sau khi đặt hàng thành công
                  cartBUS.clearCart(currentCustomerID);
                  updateProductList();
                  CustomDialog.showSuccess("Products ordered successfully! Order No: " + orderNo);
                  panelShow.setBorder(null);
                  
               if (orderUpdateListener != null) {
                orderUpdateListener.onOrderPlaced(currentCustomerID, orderNo);
            }
            
            // Tự động chuyển sang tab Order_Form
              switchToOrderForm();
        }
               else {
                  // Nếu có lỗi khi thêm details, xóa order đã tạo
                  CustomDialog.showError("Some items could not be ordered. Please try again!");
              }
          } catch (Exception e) {
              e.printStackTrace();
              CustomDialog.showError("An error occurred while processing your order!");
          }
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