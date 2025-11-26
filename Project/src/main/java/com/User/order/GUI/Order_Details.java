package com.User.order.GUI;

import com.ComponentandDatabase.Components.MyPanel;
import java.awt.Color;
import java.awt.Font;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import com.User.order.BUS.BUS_OrderDetails;
import com.User.home.BUS.productBUS;
import com.User.order.DTO.DTO_OrderDetails;
import com.User.home.DTO.productDTO;
import com.User.dashboard_user.GUI.Dashboard_user;
import javax.swing.BorderFactory;
import java.awt.BorderLayout;
import javax.swing.ImageIcon;
import java.awt.Image;
import java.awt.Dimension;
import java.awt.GridLayout;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.SpinnerNumberModel;
import java.util.ArrayList;
import javax.swing.SwingConstants;
import net.miginfocom.swing.MigLayout;
import com.User.order.BUS.BUS_Order;
import java.math.BigDecimal;
import java.math.RoundingMode;
public class Order_Details extends javax.swing.JFrame {
    public JLabel lblTitle;
    public MyPanel panelTitle;
     private String customerID;
    private String orderNo;
    private JPanel productsPanel;
    private JPanel summaryPanel;
    private JScrollPane scrollPane;
    private BUS_OrderDetails orderDetailsBUS;
    private productBUS productBUS;
     
    public Order_Details(String customerID, String orderNo) {
        
        this.customerID = customerID;
        this.orderNo = orderNo;
        this.orderDetailsBUS = new BUS_OrderDetails();
        this.productBUS = new productBUS();
        initComponents();
        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setAlwaysOnTop(true);
        initUI();
        loadOrderProducts();
    }

   private void initUI() {
        bg.setLayout(new MigLayout("fillx, insets 0", "[grow]", "[][grow][]"));
        
        // Panel tiêu đề với design đẹp hơn
        panelTitle = new MyPanel(new MigLayout("fill, insets 15"));
        panelTitle.setGradientColors(Color.decode("#2196F3"), Color.decode("#1976D2"), MyPanel.VERTICAL_GRADIENT);
        panelTitle.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.decode("#1976D2"), 2),
            BorderFactory.createEmptyBorder(15, 20, 15, 20)
        ));

        lblTitle = new JLabel("Order Details - " + orderNo, JLabel.CENTER);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblTitle.setForeground(Color.WHITE);
        panelTitle.add(lblTitle, "grow, push, align center");
        bg.add(panelTitle, "growx, h 70!, wrap");

        // Panel chứa sản phẩm với spacing tốt hơn
        productsPanel = new JPanel(new GridLayout(0, 4, 20, 20));
        productsPanel.setBackground(Color.decode("#F5F5F5"));
        productsPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        scrollPane = new JScrollPane(productsPanel);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setBackground(Color.decode("#F5F5F5"));
        
        bg.add(scrollPane, "grow, push, wrap");

        // Summary panel với design đẹp hơn
        summaryPanel = new JPanel(new MigLayout("fillx, insets 20", "[grow]", "[]5[]5[]5[]5[]5[]"));
        summaryPanel.setBackground(Color.WHITE);
        summaryPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(2, 0, 0, 0, Color.decode("#E0E0E0")),
            BorderFactory.createEmptyBorder(20, 30, 20, 30)
        ));
        bg.add(summaryPanel, "growx");
    }
   
   
   
    private void loadOrderProducts() {
        productsPanel.removeAll();
        summaryPanel.removeAll();
        
        // Lấy danh sách sản phẩm trong đơn hàng từ BUS
        ArrayList<DTO_OrderDetails> orderDetails = orderDetailsBUS.getOrderDetails(customerID, orderNo);
        
        if (orderDetails == null || orderDetails.isEmpty()) {
            showNoProductsMessage();
        } else {
            // Tính toán subtotal, VAT (8% trên subtotal gốc), discount theo promotion của đơn hàng
            BigDecimal subtotal = BigDecimal.ZERO;
            for (DTO_OrderDetails detail : orderDetails) {
                subtotal = subtotal.add(detail.getPrice().multiply(new BigDecimal(detail.getQuantity())));
            }
            BUS_Order busOrder = new BUS_Order();
            String promotionCode = busOrder.getPromotionCodeByOrderNo(orderNo);
            BigDecimal promoPercent = BigDecimal.ZERO;
            if (promotionCode != null && !promotionCode.trim().isEmpty()) {
                try {
                    com.Admin.promotion.BUS.BUSPromotion pBus = new com.Admin.promotion.BUS.BUSPromotion();
                    com.Admin.promotion.DTO.DTOPromotion p = pBus.findActivePromotion(promotionCode);
                    if (p != null && p.getDiscountPercent()!=null) promoPercent = p.getDiscountPercent();
                } catch (Exception ignore) {}
            }

            BigDecimal discount = subtotal.multiply(promoPercent).divide(new BigDecimal(100), 2, RoundingMode.HALF_UP);
            BigDecimal afterDiscount = subtotal.subtract(discount);
            BigDecimal vat = afterDiscount.multiply(new BigDecimal("8.00")).divide(new BigDecimal(100), 2, RoundingMode.HALF_UP);
            BigDecimal total = afterDiscount.add(vat);

            for (DTO_OrderDetails detail : orderDetails) {
                // Lấy thông tin đầy đủ sản phẩm
                productDTO product = productBUS.getProductById(detail.getProductID());
                if (product != null) {
                    productsPanel.add(createProductCard(product, detail));
                }
            }

            // Hiển thị tổng hợp - đúng thứ tự: Subtotal -> Discount -> After Discount -> VAT -> Total
            addSummaryLine("Promotion:", (promotionCode == null || promotionCode.isBlank()) ? "None" : promotionCode +
                    (promoPercent.compareTo(BigDecimal.ZERO)>0? String.format(" (%.1f%%)", promoPercent.doubleValue()): ""));
            addSummaryLine("Subtotal (before discount):", String.format("%,d VND", subtotal.longValue()));
            
            // Chỉ hiển thị discount nếu có
            if (promoPercent.compareTo(BigDecimal.ZERO) > 0) {
                addSummaryLine("Discount:", "-" + String.format("%,d VND", discount.longValue()));
            }
            
            addSummaryLine("Subtotal (after discount):", String.format("%,d VND", afterDiscount.longValue()));
            addSummaryLine("VAT (8% after discount):", String.format("%,d VND", vat.longValue()));
            
            // Thêm separator trước Total
            JPanel separator = new JPanel();
            separator.setBackground(Color.decode("#E0E0E0"));
            separator.setPreferredSize(new Dimension(0, 2));
            summaryPanel.add(separator, "growx, h 2!, wrap");
            
            addSummaryLine("Total:", String.format("%,d VND", total.longValue()));
        }
        
        productsPanel.revalidate();
        productsPanel.repaint();
        summaryPanel.revalidate();
        summaryPanel.repaint();
    }

    private void showNoProductsMessage() {
       productsPanel.setLayout(new BorderLayout());
       productsPanel.setBackground(Color.WHITE);
        
        JLabel noProducts = new JLabel("No products in this order", SwingConstants.CENTER);
        noProducts.setFont(new Font("Segoe UI", Font.BOLD, 20));
        noProducts.setForeground(Color.decode("#757575"));
        
        productsPanel.add(noProducts, BorderLayout.CENTER);
    }

    private JPanel createProductCard(productDTO product, DTO_OrderDetails orderDetail) {
        JPanel card = new JPanel(new BorderLayout(8, 8));
        card.setPreferredSize(new Dimension(300, 320));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.decode("#E0E0E0"), 1),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));

        // Ảnh sản phẩm với border đẹp
        JPanel imagePanel = new JPanel(new BorderLayout());
        imagePanel.setBackground(Color.WHITE);
        imagePanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.decode("#2196F3"), 2),
            BorderFactory.createEmptyBorder(8, 8, 8, 8)
        ));
        
        ImageIcon icon = new ImageIcon(product.getImage());
        Image img = icon.getImage().getScaledInstance(180, 130, Image.SCALE_SMOOTH);
        JLabel imageLabel = new JLabel(new ImageIcon(img), SwingConstants.CENTER);
        imageLabel.setBackground(Color.WHITE);
        imageLabel.setOpaque(true);
        imagePanel.add(imageLabel, BorderLayout.CENTER);
        card.add(imagePanel, BorderLayout.NORTH);

        // Chi tiết sản phẩm với layout đẹp hơn
        JPanel detailsPanel = new JPanel(new MigLayout("fillx, insets 5", "[grow]", "[]8[]8[]8[]8[]"));
        detailsPanel.setBackground(Color.WHITE);
        
        // Product Name - nổi bật
        JLabel nameLabel = new JLabel(product.getProductName());
        nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 15));
        nameLabel.setForeground(Color.decode("#1976D2"));
        nameLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 5, 0));
        detailsPanel.add(nameLabel, "growx, wrap");
        
        // Product ID
        addDetailStyled(detailsPanel, "ID:", product.getProductID(), Color.decode("#757575"));
        
        // Price - màu cam nổi bật
        String priceText = String.format("%,d VND", orderDetail.getPrice().longValue());
        addDetailStyled(detailsPanel, "Price:", priceText, Color.decode("#F57C00"), Font.BOLD);
        
        // Quantity - màu xanh lá
        addDetailStyled(detailsPanel, "Quantity:", String.valueOf(orderDetail.getQuantity()), Color.decode("#388E3C"));
        
        // Status với màu theo trạng thái
        Color statusColor = getStatusColor(orderDetail.getStatus());
        addDetailStyled(detailsPanel, "Status:", orderDetail.getStatus(), statusColor, Font.BOLD);
        
        card.add(detailsPanel, BorderLayout.CENTER);
        
        return card;
    }
    
    private Color getStatusColor(String status) {
        if (status == null) return Color.GRAY;
        switch (status.toLowerCase()) {
            case "waiting":
                return Color.decode("#FF9800");
            case "processing":
                return Color.decode("#2196F3");
            case "completed":
                return Color.decode("#4CAF50");
            case "cancelled":
                return Color.decode("#F44336");
            default:
                return Color.decode("#757575");
        }
    }

    private void addDetail(JPanel panel, String text, int fontStyle, int fontSize) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", fontStyle, fontSize));
        label.setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 5));
        panel.add(label);
    }
    
    private void addDetailStyled(JPanel panel, String label, String value, Color valueColor) {
        addDetailStyled(panel, label, value, valueColor, Font.PLAIN);
    }
    
    private void addDetailStyled(JPanel panel, String label, String value, Color valueColor, int valueStyle) {
        JLabel labelLabel = new JLabel(label + " ");
        labelLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        labelLabel.setForeground(Color.decode("#616161"));
        
        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("Segoe UI", valueStyle, 13));
        valueLabel.setForeground(valueColor);
        
        JPanel row = new JPanel(new MigLayout("insets 0", "[][]", "[]"));
        row.setBackground(Color.WHITE);
        row.add(labelLabel);
        row.add(valueLabel);
        
        panel.add(row, "growx, wrap");
    }

    private void addSummaryLine(String label, String value) {
        JPanel row = new JPanel(new MigLayout("fillx, insets 0", "[grow][]", "[]"));
        row.setBackground(Color.WHITE);
        
        JLabel labelLabel = new JLabel(label);
        labelLabel.setFont(new Font("Segoe UI", Font.BOLD, 15));
        labelLabel.setForeground(Color.decode("#424242"));
        
        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 15));
        
        // Màu sắc đặc biệt cho Total
        if (label.trim().equals("Total:")) {
            valueLabel.setForeground(Color.decode("#D32F2F"));
            valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        } else if (label.contains("Discount")) {
            valueLabel.setForeground(Color.decode("#388E3C"));
        } else if (label.contains("VAT")) {
            valueLabel.setForeground(Color.decode("#1976D2"));
        } else {
            valueLabel.setForeground(Color.decode("#212121"));
        }
        
        row.add(labelLabel, "alignx left");
        row.add(valueLabel, "alignx right");
        
        summaryPanel.add(row, "growx, wrap");
    }
    
     
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        bg = new javax.swing.JLayeredPane();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        bg.setBackground(new java.awt.Color(255, 255, 255));
        bg.setOpaque(true);

        javax.swing.GroupLayout bgLayout = new javax.swing.GroupLayout(bg);
        bg.setLayout(bgLayout);
        bgLayout.setHorizontalGroup(
            bgLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 1369, Short.MAX_VALUE)
        );
        bgLayout.setVerticalGroup(
            bgLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 583, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(bg, javax.swing.GroupLayout.Alignment.TRAILING)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(bg)
        );

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

   
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(Order_Details.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(Order_Details.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(Order_Details.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(Order_Details.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                String customerID= Dashboard_user.customerID;
                String orderNo= Order_Form.orderNo;
                new Order_Details(customerID, orderNo).setVisible(true);

            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLayeredPane bg;
    // End of variables declaration//GEN-END:variables
}
