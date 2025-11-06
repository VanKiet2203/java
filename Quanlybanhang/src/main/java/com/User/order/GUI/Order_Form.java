package com.User.order.GUI;

import com.User.order.BUS.BUS_Order;
import com.User.order.DTO.DTO_Order;
import com.User.order.BUS.BUS_OrderDetails;
import com.User.order.DTO.DTO_OrderDetails;
import com.ComponentandDatabase.Components.MyButton;
import com.ComponentandDatabase.Components.CustomDialog;
import com.User.dashboard_user.GUI.Dashboard_user;
import java.time.format.DateTimeFormatter;
import java.math.BigDecimal;
import java.math.RoundingMode;

import javax.swing.JPanel;
import javax.swing.JLabel;
import java.awt.Color;
import java.awt.Font;
import javax.swing.SwingConstants;
import java.awt.Dimension;
import javax.swing.*;
import javax.swing.BorderFactory;
import java.util.ArrayList;
import java.awt.*;

public class Order_Form extends JPanel implements OrderUpdateListener {
    private String customerID;
    private JPanel panelShow;
    private JScrollPane scrollShow;
    private BUS_Order busOrder;
    private JLabel lblTitle;
    public static String orderNo;
    private ArrayList<DTO_Order> currentOrderList; // Lưu danh sách đơn hàng hiện tại để recalculate
    
    public Order_Form(String customerID) {
        this.customerID = customerID;
        this.busOrder = new BUS_Order();
        initComponents();
        initOrderDisplayArea();
        updateOrderList();
        OrderUpdateNotifier.addListener(this);
        
        // Add component listener to recalculate layout on resize
        addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent evt) {
                recalculateLayout();
            }
        });
    }

    private void initComponents() {
        setLayout(new BorderLayout()); // Use BorderLayout for responsiveness
        setPreferredSize(new Dimension(1200, 750)); // Reduced size for smaller screens
        setBackground(Color.WHITE);
        
        // Tạo header panel với title và nút refresh
        createHeaderPanel();
    }
    
    private void createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setPreferredSize(new Dimension(0, 60));
        headerPanel.setBackground(Color.WHITE);
        headerPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, Color.decode("#E0E0E0")),
            BorderFactory.createEmptyBorder(10, 20, 10, 20)
        ));
        
        // Title
        JLabel titleLabel = new JLabel("My Orders");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(Color.decode("#2C3E50"));
        headerPanel.add(titleLabel, BorderLayout.WEST);
        
        // Refresh button
        MyButton btnRefresh = new MyButton("Refresh", 14);
        btnRefresh.setPreferredSize(new Dimension(120, 35));
        btnRefresh.setBackgroundColor(Color.decode("#2196F3"));
        btnRefresh.setHoverColor(Color.decode("#1976D2"));
        btnRefresh.setPressedColor(Color.decode("#1565C0"));
        btnRefresh.setForeground(Color.WHITE);
        btnRefresh.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnRefresh.setButtonIcon("src\\main\\resources\\Icons\\Admin_icon\\refresh.png", 18, 18, 5, SwingConstants.LEFT, SwingConstants.CENTER);
        btnRefresh.addActionListener(e -> {
            // Refresh order list
            updateOrderList();
            CustomDialog.showSuccess("Order list refreshed successfully!");
        });
        
        headerPanel.add(btnRefresh, BorderLayout.EAST);
        
        add(headerPanel, BorderLayout.NORTH);
    }

    private void initOrderDisplayArea() {
        panelShow = new JPanel();
        panelShow.setBackground(Color.WHITE);
        panelShow.setBorder(null);

        scrollShow = new JScrollPane(panelShow);
        scrollShow.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollShow.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollShow.setBorder(null);
        add(scrollShow, BorderLayout.CENTER); // Use BorderLayout.CENTER for responsiveness
    }

    /**
     * Tính toán số cột dựa trên chiều rộng panel hiện tại
     */
    private int calculateColumnCount() {
        int panelWidth = panelShow.getWidth();
        if (panelWidth <= 0) {
            // Try to get width from scroll pane viewport
            if (scrollShow != null && scrollShow.getViewport() != null) {
                panelWidth = scrollShow.getViewport().getWidth();
            }
            if (panelWidth <= 0) {
                panelWidth = 1250; // Default width if not yet initialized
            }
        }
        // Mỗi order card có width ~290px + gap 5px = ~295px (giảm từ 315)
        // Tính số cột có thể chứa, trừ đi margin
        int cardWidth = 295;
        int columns = Math.max(1, (panelWidth - 40) / cardWidth); // 40px for margins/padding
        return columns;
    }
    
    /**
     * Recalculate layout when component is resized
     */
    public void recalculateLayout() {
        if (panelShow != null && panelShow.getComponentCount() > 0) {
            // Only recalculate if there are orders displayed
            int newColumns = calculateColumnCount();
            int currentColumns = 4; // Default
            
            // Try to determine current column count from layout
            if (panelShow.getLayout() instanceof GridLayout) {
                GridLayout gl = (GridLayout) panelShow.getLayout();
                currentColumns = gl.getColumns();
            }
            
            // Only recalculate if column count changed
            if (newColumns != currentColumns) {
                panelShow.removeAll();
                panelShow.setLayout(new GridLayout(0, newColumns, 5, 8));
                // Sử dụng currentOrderList nếu có, nếu không thì load lại
                if (currentOrderList != null && !currentOrderList.isEmpty()) {
                    displayOrders(currentOrderList);
                } else {
                    ArrayList<DTO_Order> orders = busOrder.getSortedOrdersByCustomer(customerID);
                    currentOrderList = new ArrayList<>(orders);
                    if (orders == null || orders.isEmpty()) {
                        showEmptyOrderMessage();
                    } else {
                        displayOrders(orders);
                    }
                }
            }
        }
    }
    
    public void updateOrderList() {
        panelShow.removeAll();
        
        // Calculate dynamic column count based on available width
        int columns = calculateColumnCount();
        panelShow.setLayout(new GridLayout(0, columns, 5, 8));
        
        // Lấy danh sách đơn hàng từ BUS
        ArrayList<DTO_Order> orders = busOrder.getSortedOrdersByCustomer(customerID);
        currentOrderList = new ArrayList<>(orders != null ? orders : new ArrayList<>()); // Lưu danh sách để recalculate
        
        if (orders == null || orders.isEmpty()) {
            showEmptyOrderMessage();
        } else {
            displayOrders(orders);
        }
    }
    
    /**
     * Display orders in the panel
     */
    private void displayOrders(ArrayList<DTO_Order> orders) {
        // Layout is already set in updateOrderList
        // Just add orders directly to panelShow
        if (orders == null || orders.isEmpty()) {
            showEmptyOrderMessage();
        } else {
            for (DTO_Order order : orders) {
                JPanel orderPanel = createOrderPanel(order);
                panelShow.add(orderPanel);
            }
        }
        
        panelShow.revalidate();
        panelShow.repaint();
    }

    private void showEmptyOrderMessage() {
        // Tạm thời đổi layout sang BorderLayout để căn giữa
        panelShow.setLayout(new BorderLayout());
        
        JLabel noOrders = new JLabel("You don't have any orders yet", SwingConstants.CENTER);
        noOrders.setFont(new Font("Arial", Font.BOLD, 20));
        noOrders.setForeground(Color.GRAY);
        
        panelShow.add(noOrders, BorderLayout.CENTER);
        
        panelShow.revalidate();
        panelShow.repaint();
    }

   private JPanel createOrderPanel(DTO_Order order) {
        JPanel panelcreate = new JPanel(new BorderLayout(5, 5));
        // Tăng chiều cao để đủ chứa cả trường hợp có Discount (270 -> 310)
        panelcreate.setPreferredSize(new Dimension(280, 310));
        panelcreate.setMinimumSize(new Dimension(240, 290)); // Tăng minimum size
        panelcreate.setBackground(Color.WHITE);
        panelcreate.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.decode("#E0E0E0"), 2),
            BorderFactory.createEmptyBorder(8, 8, 8, 8)
        ));

        // Header với background đẹp và status badge
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Color.decode("#E3F2FD"));
        headerPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.decode("#2196F3"), 1),
            BorderFactory.createEmptyBorder(6, 10, 6, 10)
        ));

        JPanel leftHeader = new JPanel(new BorderLayout());
        leftHeader.setOpaque(false);
        
        JLabel orderNoLabel = new JLabel("Order: " + order.getOrderNo());
        orderNoLabel.setFont(new Font("Arial", Font.BOLD, 13));
        orderNoLabel.setForeground(Color.decode("#1976D2"));
        
        // Status badge - hiển thị nổi bật ở header
        // Lấy status và normalize (trim, uppercase để so sánh)
        String status = (order.getStatus() == null || order.getStatus().trim().isEmpty()) ? "Unknown" : order.getStatus().trim();
        
        // Debug: Kiểm tra status
        if (status.equalsIgnoreCase("unavailable") || status.equalsIgnoreCase("available")) {
            System.err.println("WARNING: Order_Form hiển thị status = '" + status + "' cho Order_No: " + order.getOrderNo());
            System.err.println("Có thể đang lấy nhầm Record_Status thay vì Status!");
            // Nếu là "unavailable" hoặc "available", đây là Record_Status, không phải Status
            // Hiển thị "Unknown" thay vì giá trị sai
            status = "Unknown";
        }
        JLabel statusBadge = new JLabel(status);
        statusBadge.setFont(new Font("Arial", Font.BOLD, 11));
        statusBadge.setHorizontalAlignment(SwingConstants.CENTER);
        statusBadge.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.WHITE, 1),
            BorderFactory.createEmptyBorder(2, 6, 2, 6)
        ));
        
        // Màu sắc theo status
        if ("Waiting".equalsIgnoreCase(status)) {
            statusBadge.setBackground(Color.decode("#FFA726"));
            statusBadge.setForeground(Color.WHITE);
        } else if ("Confirmed".equalsIgnoreCase(status)) {
            statusBadge.setBackground(Color.decode("#66BB6A"));
            statusBadge.setForeground(Color.WHITE);
        } else if ("Cancelled".equalsIgnoreCase(status)) {
            statusBadge.setBackground(Color.decode("#EF5350"));
            statusBadge.setForeground(Color.WHITE);
        } else {
            statusBadge.setBackground(Color.decode("#78909C"));
            statusBadge.setForeground(Color.WHITE);
        }
        statusBadge.setOpaque(true);

        leftHeader.add(orderNoLabel, BorderLayout.NORTH);
        leftHeader.add(statusBadge, BorderLayout.SOUTH);

        JLabel dateLabel = new JLabel("Date: " + 
            order.getDateOrder().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) + " " +
            order.getTimeOrder().format(DateTimeFormatter.ofPattern("HH:mm"))
        );
        dateLabel.setFont(new Font("Arial", Font.BOLD, 11));
        dateLabel.setForeground(Color.decode("#666666"));
        dateLabel.setHorizontalAlignment(SwingConstants.RIGHT);

        headerPanel.add(leftHeader, BorderLayout.WEST);
        headerPanel.add(dateLabel, BorderLayout.EAST);

        // Details panel với BoxLayout để kiểm soát spacing tốt hơn
        JPanel detailsPanel = new JPanel();
        detailsPanel.setLayout(new BoxLayout(detailsPanel, BoxLayout.Y_AXIS));
        detailsPanel.setBackground(Color.WHITE);
        detailsPanel.setBorder(BorderFactory.createEmptyBorder(10, 8, 10, 8));
        detailsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Customer ID
        JLabel customerLabel = new JLabel("Customer: " + order.getCustomerID());
        customerLabel.setFont(new Font("Arial", Font.PLAIN, 11));
        customerLabel.setForeground(Color.decode("#666666"));
        customerLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        customerLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));
        detailsPanel.add(customerLabel);

        // Total Items
        JLabel itemsLabel = new JLabel("Items: " + order.getTotalQuantityProduct());
        itemsLabel.setFont(new Font("Arial", Font.BOLD, 11));
        itemsLabel.setForeground(Color.decode("#2E7D32"));
        itemsLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        itemsLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        detailsPanel.add(itemsLabel);
        
        // Thêm khoảng cách trước phần breakdown
        detailsPanel.add(Box.createVerticalStrut(4));

        // Breakdown: compute from order details (giống Order_Details.java)
        BUS_OrderDetails orderDetailsBUS = new BUS_OrderDetails();
        java.util.ArrayList<DTO_OrderDetails> orderDetailsList = orderDetailsBUS.getOrderDetails(customerID, order.getOrderNo());
        
        // Tính subtotal từ order details
        java.math.BigDecimal subtotal = java.math.BigDecimal.ZERO;
        if (orderDetailsList != null && !orderDetailsList.isEmpty()) {
            for (DTO_OrderDetails detail : orderDetailsList) {
                subtotal = subtotal.add(detail.getPrice().multiply(new BigDecimal(detail.getQuantity())));
            }
        }
        
        String promotionCode = order.getPromotionCode();
        java.math.BigDecimal promoPercent = java.math.BigDecimal.ZERO;
        if (promotionCode != null && !promotionCode.trim().isEmpty()) {
            try {
                com.Admin.promotion.BUS.BUSPromotion pBus = new com.Admin.promotion.BUS.BUSPromotion();
                com.Admin.promotion.DTO.DTOPromotion p = pBus.findActivePromotion(promotionCode);
                if (p != null && p.getDiscountPercent()!=null) promoPercent = p.getDiscountPercent();
            } catch (Exception ignore) {}
        }
        
        // Tính đúng: Subtotal -> Discount -> After Discount -> VAT -> Total
        java.math.BigDecimal discount = subtotal.multiply(promoPercent).divide(new java.math.BigDecimal(100), 2, RoundingMode.HALF_UP);
        java.math.BigDecimal afterDiscount = subtotal.subtract(discount);
        java.math.BigDecimal vat = afterDiscount.multiply(new java.math.BigDecimal("8.00")).divide(new java.math.BigDecimal(100), 2, RoundingMode.HALF_UP);
        java.math.BigDecimal totalToPay = afterDiscount.add(vat);

        JLabel lblSubtotal = new JLabel("Subtotal: " + String.format("%,d VNĐ", subtotal.intValue()));
        lblSubtotal.setFont(new Font("Arial", Font.PLAIN, 10));
        lblSubtotal.setForeground(Color.decode("#555555"));
        lblSubtotal.setAlignmentX(Component.LEFT_ALIGNMENT);
        lblSubtotal.setBorder(BorderFactory.createEmptyBorder(0, 0, 6, 0));
        detailsPanel.add(lblSubtotal);

        // Luôn dành một vị trí cho Discount để đảm bảo spacing nhất quán
        if (discount.compareTo(BigDecimal.ZERO) > 0) {
            JLabel lblDiscount = new JLabel("Discount: -" + String.format("%,d VNĐ", discount.intValue()) + (promotionCode!=null? " ("+promotionCode+")":""));
            lblDiscount.setFont(new Font("Arial", Font.PLAIN, 10));
            lblDiscount.setForeground(Color.decode("#2E7D32"));
            lblDiscount.setAlignmentX(Component.LEFT_ALIGNMENT);
            lblDiscount.setBorder(BorderFactory.createEmptyBorder(0, 0, 6, 0));
            detailsPanel.add(lblDiscount);
        } else {
            // Nếu không có discount, thêm một spacer có cùng chiều cao để giữ spacing nhất quán
            JPanel discountSpacer = new JPanel();
            discountSpacer.setPreferredSize(new Dimension(0, 20)); // Chiều cao tương đương một dòng text + padding
            discountSpacer.setMaximumSize(new Dimension(Integer.MAX_VALUE, 20));
            discountSpacer.setOpaque(false);
            detailsPanel.add(discountSpacer);
        }

        JLabel lblAfter = new JLabel("After discount: " + String.format("%,d VNĐ", afterDiscount.intValue()));
        lblAfter.setFont(new Font("Arial", Font.PLAIN, 10));
        lblAfter.setForeground(Color.decode("#555555"));
        lblAfter.setAlignmentX(Component.LEFT_ALIGNMENT);
        lblAfter.setBorder(BorderFactory.createEmptyBorder(0, 0, 6, 0));
        detailsPanel.add(lblAfter);

        JLabel lblVat = new JLabel("VAT (8%): " + String.format("%,d VNĐ", vat.intValue()));
        lblVat.setFont(new Font("Arial", Font.PLAIN, 10));
        lblVat.setForeground(Color.decode("#555555"));
        lblVat.setAlignmentX(Component.LEFT_ALIGNMENT);
        lblVat.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));
        detailsPanel.add(lblVat);

        JLabel priceLabel = new JLabel("Total: " + String.format("%,d VNĐ", totalToPay.intValue()));
        priceLabel.setFont(new Font("Arial", Font.BOLD, 12));
        priceLabel.setForeground(Color.decode("#D32F2F"));
        priceLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        priceLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        detailsPanel.add(priceLabel);
        
        // Thêm khoảng cách trước payment
        detailsPanel.add(Box.createVerticalStrut(4));

        // Payment Method
        JLabel paymentLabel = new JLabel("Payment: " + order.getPayment());
        paymentLabel.setFont(new Font("Arial", Font.PLAIN, 10));
        paymentLabel.setForeground(Color.decode("#666666"));
        paymentLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        paymentLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 6, 0));
        detailsPanel.add(paymentLabel);

        // Button panel với style đẹp
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 8));
        buttonPanel.setBackground(Color.WHITE);

        MyButton detailBtn = new MyButton("Details", 10);
        detailBtn.setPreferredSize(new Dimension(110, 30));
        detailBtn.setBackgroundColor(Color.decode("#2196F3"));
        detailBtn.setHoverColor(Color.decode("#1976D2"));
        detailBtn.setForeground(Color.WHITE);
        detailBtn.setFont(new Font("Arial", Font.BOLD, 11));
        detailBtn.addActionListener((e) -> {
            String customerID = Dashboard_user.customerID;
            orderNo = order.getOrderNo();
            Order_Details orderDetails = new Order_Details(customerID, orderNo);
            orderDetails.setVisible(true);
        });
        buttonPanel.add(detailBtn);

        // Cancel button if not yet confirmed (chỉ hiển thị khi status = Waiting)
        // Lấy status từ order và normalize để so sánh
        String currentStatus = order.getStatus();
        if (currentStatus == null) {
            currentStatus = "";
        }
        String normalizedStatus = currentStatus.trim();
        
        // Chỉ hiển thị nút cancel khi status là "Waiting" (case-insensitive)
        if (normalizedStatus.equalsIgnoreCase("Waiting")) {
            MyButton cancelBtn = new MyButton("Cancel", 10);
            cancelBtn.setPreferredSize(new Dimension(100, 30));
            cancelBtn.setBackgroundColor(Color.decode("#E74C3C"));
            cancelBtn.setHoverColor(Color.decode("#C0392B"));
            cancelBtn.setForeground(Color.WHITE);
            cancelBtn.setFont(new Font("Arial", Font.BOLD, 11));
            cancelBtn.addActionListener(e -> {
                // Kiểm tra lại status trước khi cancel (để tránh cancel order đã được confirm)
                ArrayList<DTO_Order> latestOrders = busOrder.getSortedOrdersByCustomer(order.getCustomerID());
                DTO_Order latestOrder = null;
                for (DTO_Order o : latestOrders) {
                    if (o.getOrderNo().equals(order.getOrderNo())) {
                        latestOrder = o;
                        break;
                    }
                }
                
                if (latestOrder != null && !latestOrder.getStatus().equalsIgnoreCase("Waiting")) {
                    CustomDialog.showError("Unable to cancel. The order status has been changed to: " + latestOrder.getStatus());
                    updateOrderList(); // Refresh để cập nhật UI
                    return;
                }
                
                boolean confirm = CustomDialog.showOptionPane(
                    "Cancel Order",
                    "Do you want to cancel this order?",
                    UIManager.getIcon("OptionPane.questionIcon"),
                    Color.decode("#FF6666")
                );
                if (confirm) {
                    boolean ok = busOrder.cancelOrder(order.getOrderNo(), order.getCustomerID());
                    if (ok) {
                        CustomDialog.showSuccess("Order cancelled successfully.");
                        updateOrderList();
                    } else {
                        CustomDialog.showError("Unable to cancel. The order may have been confirmed.");
                        updateOrderList(); // Refresh để cập nhật UI
                    }
                }
            });
            buttonPanel.add(cancelBtn);
        }

        panelcreate.add(headerPanel, BorderLayout.NORTH);
        panelcreate.add(detailsPanel, BorderLayout.CENTER);
        panelcreate.add(buttonPanel, BorderLayout.SOUTH);

        return panelcreate;
    }


    private void addCompactDetail(JPanel panel, String text, int fontStyle, int fontSize) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Arial", fontStyle, fontSize));
        label.setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 5));
        panel.add(label);
    }

    
    @Override
    public void onOrderPlaced(String customerID, String orderNo) {
         if (this.customerID.equals(customerID)) {
         SwingUtilities.invokeLater(() -> {
            // Calculate dynamic column count
            int columns = calculateColumnCount();
            panelShow.removeAll();
            panelShow.setLayout(new GridLayout(0, columns, 5, 8));
            
            // Sử dụng hàm mới đã sắp xếp từ DAO
            ArrayList<DTO_Order> orders = busOrder.getSortedOrdersByCustomer(customerID);
            currentOrderList = new ArrayList<>(orders != null ? orders : new ArrayList<>()); // Lưu danh sách để recalculate
            
            if (orders != null && !orders.isEmpty()) {
                for (DTO_Order order : orders) {
                    JPanel orderPanel = createOrderPanel(order);
                    
                    if (order.getOrderNo().equals(orderNo)) {
                        orderPanel.setBorder(BorderFactory.createLineBorder(Color.GREEN, 2));
                        new Timer(3000, e -> {
                            orderPanel.setBorder(BorderFactory.createCompoundBorder(
                                BorderFactory.createLineBorder(Color.decode("#E0E0E0"), 2),
                                BorderFactory.createEmptyBorder(10, 10, 10, 10)
                            ));
                            ((Timer)e.getSource()).stop();
                        }).start();
                    }
                    panelShow.add(orderPanel);
                }
            } else {
                showEmptyOrderMessage();
            }
            
            panelShow.revalidate();
            panelShow.repaint();
               // 3. Auto-switch to this tab
               Container parent = this.getParent();
               while (parent != null) {
                   if (parent instanceof JTabbedPane) {
                       ((JTabbedPane)parent).setSelectedComponent(this);
                       break;
                   }
                   parent = parent.getParent();
               }
           });
       }
   }
    
     public void onOrderDeleted(String customerID, String orderNo) {
        if (this.customerID.equals(customerID)) {
            SwingUtilities.invokeLater(() -> {
                // Remove the order panel if it exists
                for (Component comp : panelShow.getComponents()) {
                    if (comp instanceof JPanel) {
                        JPanel orderPanel = (JPanel) comp;
                        for (Component innerComp : orderPanel.getComponents()) {
                            if (innerComp instanceof JPanel) {
                                JPanel headerPanel = (JPanel) innerComp;
                                for (Component label : headerPanel.getComponents()) {
                                    if (label instanceof JLabel) {
                                        JLabel orderLabel = (JLabel) label;
                                        if (orderLabel.getText().contains(orderNo)) {
                                            panelShow.remove(orderPanel);
                                            break;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                
                // If no orders left, show empty message
                if (panelShow.getComponentCount() == 0) {
                    showEmptyOrderMessage();
                }
                
                panelShow.revalidate();
                panelShow.repaint();
            });
        }
    }
    
    // Optional: respond to order status updates broadcast by admin side
    public void onOrderUpdated(String customerID, String orderNo) {
        if (this.customerID.equals(customerID)) {
            SwingUtilities.invokeLater(() -> {
                // Force refresh order list to get latest status
                updateOrderList();
                // Also update currentOrderList to ensure consistency
                ArrayList<DTO_Order> orders = busOrder.getSortedOrdersByCustomer(customerID);
                currentOrderList = new ArrayList<>(orders != null ? orders : new ArrayList<>());
            });
        }
    }
    
}