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
        
        // Panel tiêu đề
        panelTitle = new MyPanel(new MigLayout("fill, insets 0"));
        panelTitle.setGradientColors(Color.decode("#1CB5E0"), Color.decode("#4682B4"), MyPanel.VERTICAL_GRADIENT);

        lblTitle = new JLabel("Order " + orderNo, JLabel.CENTER);
        lblTitle.setFont(new Font("Arial", Font.BOLD, 20));
        lblTitle.setForeground(Color.WHITE);
        panelTitle.add(lblTitle, "grow, push, align center");
        bg.add(panelTitle, "growx, h 40!, wrap");

        // Panel chứa sản phẩm
        productsPanel = new JPanel(new GridLayout(0, 4, 10, 15));
        productsPanel.setBackground(Color.WHITE);
        productsPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        scrollPane = new JScrollPane(productsPanel);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        
        bg.add(scrollPane, "grow, push, wrap");

        // Summary panel for totals
        summaryPanel = new JPanel(new GridLayout(0, 1, 3, 3));
        summaryPanel.setBackground(Color.WHITE);
        summaryPanel.setBorder(BorderFactory.createEmptyBorder(10, 15, 15, 15));
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
            addSummaryLine("Promotion: ", (promotionCode == null || promotionCode.isBlank()) ? "None" : promotionCode +
                    (promoPercent.compareTo(BigDecimal.ZERO)>0? String.format(" (%.1f%%)", promoPercent.doubleValue()): ""));
            addSummaryLine("Subtotal: ", String.format("%,d VND", subtotal.longValue()));
            addSummaryLine("Discount: ", "-" + String.format("%,d VND", discount.longValue()));
            addSummaryLine("Subtotal (after discount): ", String.format("%,d VND", afterDiscount.longValue()));
            addSummaryLine("VAT (8% after discount): ", String.format("%,d VND", vat.longValue()));
            addSummaryLine("Total: ", String.format("%,d VND", total.longValue()));
        }
        
        productsPanel.revalidate();
        productsPanel.repaint();
        summaryPanel.revalidate();
        summaryPanel.repaint();
    }

    private void showNoProductsMessage() {
       productsPanel.setLayout(new BorderLayout());
        
        JLabel noProducts = new JLabel("No products in this order", SwingConstants.CENTER);
        noProducts.setFont(new Font("Arial", Font.BOLD, 18));
        noProducts.setForeground(Color.GRAY);
        
        productsPanel.add(noProducts, BorderLayout.CENTER);
    }

    private JPanel createProductCard(productDTO product, DTO_OrderDetails orderDetail) {
        JPanel card = new JPanel(new BorderLayout(5, 5));
        card.setPreferredSize(new Dimension(280, 240));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.LIGHT_GRAY),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));

        // Ảnh sản phẩm
        ImageIcon icon = new ImageIcon(product.getImage());
        Image img = icon.getImage().getScaledInstance(150, 110, Image.SCALE_SMOOTH);
        JLabel imageLabel = new JLabel(new ImageIcon(img), SwingConstants.CENTER);
        imageLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        card.add(imageLabel, BorderLayout.NORTH);

        // Chi tiết sản phẩm
        JPanel detailsPanel = new JPanel(new GridLayout(0, 1, 3, 3));
        detailsPanel.setBackground(Color.WHITE);
        
        addDetail(detailsPanel, "ID: " + product.getProductID(), Font.PLAIN, 13);
        addDetail(detailsPanel, product.getProductName(), Font.BOLD, 14);
        addDetail(detailsPanel, "Price: " + orderDetail.getPrice() + " VNĐ", Font.PLAIN, 13);
        addDetail(detailsPanel, "Quantity: " + orderDetail.getQuantity(), Font.PLAIN, 13);
        addDetail(detailsPanel, "Status: " + orderDetail.getStatus(), Font.PLAIN, 13);
        
        card.add(detailsPanel, BorderLayout.CENTER);
        
        return card;
    }

    private void addDetail(JPanel panel, String text, int fontStyle, int fontSize) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Times new roman", fontStyle, fontSize));
        label.setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 5));
        panel.add(label);
    }

    private void addSummaryLine(String label, String value) {
        JLabel l = new JLabel(label + value);
        l.setFont(new Font("Arial", Font.PLAIN, 14));
        l.setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 5));
        summaryPanel.add(l);
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
