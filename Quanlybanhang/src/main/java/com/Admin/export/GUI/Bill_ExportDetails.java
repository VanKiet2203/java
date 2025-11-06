
package com.Admin.export.GUI;

import com.ComponentandDatabase.Components.MyButton;
import com.ComponentandDatabase.Components.MyPanel;
import com.ComponentandDatabase.Components.MyTable;
import com.Admin.export.BUS.BUS_ExportBill;
import com.Admin.export.DTO.DTO_BillExportedDetail;
import com.ComponentandDatabase.Components.CustomDialog;
import java.text.SimpleDateFormat;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Component;
import java.util.List;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
import net.miginfocom.swing.MigLayout;
import static com.ComponentandDatabase.Components.UIConstants.*;
import java.awt.GridLayout;
import java.awt.FlowLayout;
import java.awt.BorderLayout;


public class Bill_ExportDetails extends javax.swing.JFrame {
     private JLabel lblTitle;
     private MyPanel panelTitle;
     private MyTable tableBillDetail;
     private BUS_ExportBill busExportBill;
     private javax.swing.JLayeredPane bg;
     private JScrollPane contentScroll;
 
     private DefaultTableModel model;
    public Bill_ExportDetails() {
        initComponents();
        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE); 
        setAlwaysOnTop(true); // Luôn hiển thị trên cùng
        
        // Set size responsive - 80% của màn hình, tối thiểu 800x600
        Dimension screenSize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
        int width = Math.max(800, (int)(screenSize.width * 0.8));
        int height = Math.max(600, (int)(screenSize.height * 0.8));
        setSize(width, height);
        setMinimumSize(new Dimension(800, 600));
        
        setLocationRelativeTo(null); // Căn giữa màn hình
        init();
    }
    
    // Set bằng Invoice_No + Admin_ID
    public void setInvoiceInfo(String invoiceNo, String adminId) {
        if (lblTitle != null) {
            lblTitle.setText("Bill Export Details - " + invoiceNo);
        }
        loadBillDetails(invoiceNo);
    }

    // Load theo Invoice_No - chỉ hiển thị chi tiết của hóa đơn được chọn
    private void loadBillDetails(String invoiceNo) {
        try {
            busExportBill = new BUS_ExportBill();
            List<DTO_BillExportedDetail> details = busExportBill.getBillDetailsByInvoice(invoiceNo);
            if (details == null || details.isEmpty()) {
                CustomDialog.showError("No bill details found for Invoice: " + invoiceNo);
                return;
            }

            JPanel page = new JPanel();
            page.setLayout(new BoxLayout(page, BoxLayout.Y_AXIS));
            page.setBackground(Color.WHITE);
            
            // Responsive padding - sử dụng percentage hoặc minimum
            int scrollWidth = contentScroll.getWidth() > 0 ? contentScroll.getWidth() : 800;
            int padding = Math.max(16, Math.min(32, scrollWidth / 40)); // 16-32px tùy width
            page.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.decode("#E0E0E0"), 1),
                BorderFactory.createEmptyBorder(padding, padding, padding, padding)
            ));

            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
            SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");

            JLabel title = new JLabel("Bill Export Details", JLabel.CENTER);
            title.setFont(new Font("Arial", Font.BOLD, 24));
            title.setForeground(Color.decode("#2C3E50"));
            title.setAlignmentX(0.5f);
            page.add(title);
            page.add(Box.createVerticalStrut(16));

            DTO_BillExportedDetail first = details.get(0);
            page.add(infoRow("Invoice No:", first.getInvoiceNo()));
            page.add(infoRow("Admin ID:", first.getAdminId()));
            page.add(infoRow("Customer ID:", first.getCustomerId()));
            page.add(infoRow("Date Exported:", dateFormat.format(first.getDateExported())));
            page.add(infoRow("Time Exported:", timeFormat.format(first.getTimeExported())));
            page.add(separator());

            JLabel prodTitle = new JLabel("Products", JLabel.LEFT);
            prodTitle.setFont(new Font("Arial", Font.BOLD, 16));
            prodTitle.setForeground(Color.decode("#34495E"));
            page.add(prodTitle);
            page.add(Box.createVerticalStrut(12));

            java.math.BigDecimal grandBefore = java.math.BigDecimal.ZERO;
            java.math.BigDecimal grandAfter = java.math.BigDecimal.ZERO;

            for (DTO_BillExportedDetail d : details) {
                String warrantyText = busExportBill.getWarranty(d.getProductId());

                // Sử dụng responsive layout - tự động điều chỉnh số cột dựa trên width
                JPanel card = new JPanel();
                card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS)); // Vertical layout để responsive
                card.setBackground(Color.decode("#F8F9FA"));
                card.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(Color.decode("#DEE2E6"), 1),
                    BorderFactory.createEmptyBorder(16, 16, 16, 16)
                ));
                card.setAlignmentX(Component.LEFT_ALIGNMENT);
                card.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));

                // Tạo inner panel với GridLayout responsive - sử dụng FlowLayout để tự động wrap
                JPanel fieldsPanel = new JPanel();
                // Sử dụng FlowLayout với wrap để tự động xuống dòng khi không đủ chỗ
                fieldsPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 12, 8));
                fieldsPanel.setBackground(Color.decode("#F8F9FA"));
                fieldsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

                fieldsPanel.add(field("Product ID", d.getProductId()));
                fieldsPanel.add(field("Unit Price", String.valueOf(d.getUnitPrice())));
                fieldsPanel.add(field("Quantity", String.valueOf(d.getQuantity())));
                fieldsPanel.add(field("Warranty", warrantyText));

                String promoCode = (d.getPromotionCode() != null && !d.getPromotionCode().isEmpty()) ? d.getPromotionCode() : "N/A";
                fieldsPanel.add(field("Promotion Code", promoCode));
                fieldsPanel.add(field("Discount %", d.getDiscountPercent() + "%"));
                fieldsPanel.add(field("Total Before", String.valueOf(d.getTotalPriceBefore())));
                fieldsPanel.add(field("Total After", String.valueOf(d.getTotalPriceAfter())));

                card.add(fieldsPanel);
                page.add(card);
                page.add(Box.createVerticalStrut(12));

                grandBefore = grandBefore.add(d.getTotalPriceBefore());
                grandAfter = grandAfter.add(d.getTotalPriceAfter());
            }

            page.add(separator());
            page.add(Box.createVerticalStrut(8));
            
            // Get bill header information for VAT
            com.Admin.export.DTO.DTO_BillExported billHeader = null;
            try {
                billHeader = busExportBill.getExportBillDetailsForInsurance(first.getInvoiceNo(), first.getAdminId());
            } catch (Exception e) {
                // If cannot get header, continue without VAT info
            }
            
            // Tính toán breakdown từ billHeader hoặc từ details
            java.math.BigDecimal subtotalBefore = grandBefore; // Tổng trước discount
            java.math.BigDecimal discountAmount = java.math.BigDecimal.ZERO;
            java.math.BigDecimal subtotalAfter = grandAfter; // Tổng sau discount (từ details)
            java.math.BigDecimal vatAmount = java.math.BigDecimal.ZERO;
            java.math.BigDecimal finalTotal = grandAfter;
            
            // Lấy promotion code từ bill header (ưu tiên)
            String promoCode = billHeader != null ? billHeader.getPromotionCode() : null;
            
            // Nếu không có trong bill header, thử lấy từ details
            if ((promoCode == null || promoCode.trim().isEmpty()) && !details.isEmpty()) {
                promoCode = details.get(0).getPromotionCode();
            }
            
            // Nếu vẫn không có, thử lấy từ Order thông qua Order_No
            if ((promoCode == null || promoCode.trim().isEmpty()) && billHeader != null && billHeader.getOrderNo() != null) {
                try {
                    com.User.order.BUS.BUS_Order busOrder = new com.User.order.BUS.BUS_Order();
                    promoCode = busOrder.getPromotionCodeByOrderNo(billHeader.getOrderNo());
                } catch (Exception e) {
                    System.err.println("Error getting promotion code from order: " + e.getMessage());
                }
            }
            
            // Tính discount dựa trên promotion code nếu có
            if (promoCode != null && !promoCode.trim().isEmpty()) {
                try {
                    com.Admin.promotion.BUS.BUSPromotion pBus = new com.Admin.promotion.BUS.BUSPromotion();
                    com.Admin.promotion.DTO.DTOPromotion p = pBus.findActivePromotion(promoCode);
                    if (p != null && p.getDiscountPercent() != null) {
                        discountAmount = subtotalBefore.multiply(p.getDiscountPercent())
                            .divide(new java.math.BigDecimal(100), 2, java.math.RoundingMode.HALF_UP);
                        subtotalAfter = subtotalBefore.subtract(discountAmount);
                    }
                } catch (Exception e) {
                    System.err.println("Error calculating discount from promotion: " + e.getMessage());
                }
            }
            
            // Lấy VAT từ bill header (đã tính đúng: 8% sau discount)
            if (billHeader != null && billHeader.getVatAmount() != null) {
                vatAmount = billHeader.getVatAmount();
                if (billHeader.getTotalAmount() != null) {
                    finalTotal = billHeader.getTotalAmount();
                } else {
                    finalTotal = subtotalAfter.add(vatAmount);
                }
            } else {
                // Fallback: tính VAT nếu không có trong header
                vatAmount = subtotalAfter.multiply(new java.math.BigDecimal("8.00"))
                    .divide(new java.math.BigDecimal(100), 2, java.math.RoundingMode.HALF_UP);
                finalTotal = subtotalAfter.add(vatAmount);
            }
            
            // Tạo panel tổng tiền với style đặc biệt - hiển thị đúng thứ tự
            JPanel totalPanel = new JPanel();
            totalPanel.setLayout(new BoxLayout(totalPanel, BoxLayout.Y_AXIS));
            totalPanel.setBackground(Color.WHITE);
            totalPanel.setBorder(BorderFactory.createEmptyBorder(8, 0, 8, 0));
            
            // Promotion info
            if (promoCode != null && !promoCode.trim().isEmpty()) {
                JLabel promoLabel = new JLabel("Promotion: " + promoCode);
                promoLabel.setFont(new Font("Arial", Font.PLAIN, 13));
                promoLabel.setForeground(Color.decode("#7F8C8D"));
                totalPanel.add(promoLabel);
            }
            
            // Subtotal (before discount)
            JLabel subtotalLabel = new JLabel("Subtotal: " + String.format("%,d VND", subtotalBefore.intValue()));
            subtotalLabel.setFont(new Font("Arial", Font.BOLD, 14));
            subtotalLabel.setForeground(Color.decode("#2C3E50"));
            totalPanel.add(subtotalLabel);
            
            // Discount
            if (discountAmount.compareTo(java.math.BigDecimal.ZERO) > 0) {
                JLabel discountLabel = new JLabel("Discount: -" + String.format("%,d VND", discountAmount.intValue()));
                discountLabel.setFont(new Font("Arial", Font.BOLD, 14));
                discountLabel.setForeground(Color.decode("#2E7D32"));
                totalPanel.add(discountLabel);
            }
            
            // Subtotal (after discount)
            JLabel afterLabel = new JLabel("Subtotal (after discount): " + String.format("%,d VND", subtotalAfter.intValue()));
            afterLabel.setFont(new Font("Arial", Font.BOLD, 14));
            afterLabel.setForeground(Color.decode("#2C3E50"));
            totalPanel.add(afterLabel);
            
            // VAT
            JLabel vatLabel = new JLabel("VAT (8% after discount): " + String.format("%,d VND", vatAmount.intValue()));
            vatLabel.setFont(new Font("Arial", Font.BOLD, 14));
            vatLabel.setForeground(Color.decode("#2C3E50"));
            totalPanel.add(vatLabel);
            
            // Total
            JLabel totalLabel = new JLabel("Total Amount (incl. VAT): " + String.format("%,d VND", finalTotal.intValue()));
            totalLabel.setFont(new Font("Arial", Font.BOLD, 16));
            totalLabel.setForeground(Color.decode("#E74C3C"));
            totalPanel.add(totalLabel);
            
            page.add(totalPanel);

            contentScroll.setViewportView(page);
        } catch (Exception e) {
            CustomDialog.showError("Error loading bill details: " + e.getMessage());
        }
    }

    private JPanel infoRow(String label, String value) {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 4));
        row.setBackground(Color.WHITE);
        JLabel l = new JLabel(label);
        l.setFont(new Font("Arial", Font.BOLD, 13));
        l.setForeground(Color.decode("#7F8C8D"));
        JLabel v = new JLabel(value);
        v.setFont(new Font("Arial", Font.PLAIN, 13));
        v.setForeground(Color.decode("#2C3E50"));
        row.add(l);
        row.add(v);
        return row;
    }

    private JSeparator separator() {
        JSeparator s = new JSeparator();
        s.setMaximumSize(new Dimension(Integer.MAX_VALUE, 2));
        s.setForeground(Color.decode("#BDC3C7"));
        return s;
    }

    private JPanel field(String label, String value) {
        JPanel p = new JPanel(new BorderLayout(0, 4));
        p.setBackground(Color.WHITE);
        p.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
        p.setPreferredSize(new Dimension(150, 60)); // Minimum size
        p.setMinimumSize(new Dimension(120, 50));
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));
        
        JLabel l = new JLabel(label);
        l.setFont(new Font("Arial", Font.PLAIN, 11));
        l.setForeground(Color.decode("#95A5A6"));
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JLabel v = new JLabel(value);
        v.setFont(new Font("Arial", Font.BOLD, 13));
        v.setForeground(Color.decode("#2C3E50"));
        v.setAlignmentX(Component.LEFT_ALIGNMENT);
        // Wrap text nếu quá dài
        v.setToolTipText(value);
        
        p.add(l, BorderLayout.NORTH);
        p.add(v, BorderLayout.CENTER);
        return p;
    }

   public void init() {
     // Thiết lập layout chính với BorderLayout để responsive
     bg.setLayout(new BorderLayout());

     // 1. Panel tiêu đề
     panelTitle = new MyPanel(new BorderLayout());
     panelTitle.setGradientColors(Color.decode("#1CB5E0"), Color.decode("#4682B4"), MyPanel.VERTICAL_GRADIENT);
     panelTitle.setPreferredSize(new Dimension(0, 50));

     lblTitle = new JLabel("Bill Export Details", JLabel.CENTER);
     lblTitle.setFont(new Font("Arial", Font.BOLD, 20));
     lblTitle.setForeground(Color.WHITE);

     panelTitle.add(lblTitle, BorderLayout.CENTER);
     bg.add(panelTitle, BorderLayout.NORTH);
     
     // 2. Content scroll pane - chiếm toàn bộ không gian còn lại
     contentScroll = new JScrollPane();
     contentScroll.setBorder(null);
     contentScroll.getVerticalScrollBar().setPreferredSize(new Dimension(15, Integer.MAX_VALUE));
     contentScroll.getHorizontalScrollBar().setPreferredSize(new Dimension(Integer.MAX_VALUE, 15));
     contentScroll.setViewportBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
     bg.add(contentScroll, BorderLayout.CENTER);
     
     // 3. Thêm component listener để resize responsive
     addComponentListener(new java.awt.event.ComponentAdapter() {
         @Override
         public void componentResized(java.awt.event.ComponentEvent evt) {
             // Khi window resize, revalidate để layout tự động điều chỉnh
             SwingUtilities.invokeLater(() -> {
                 bg.revalidate();
                 bg.repaint();
                 if (contentScroll != null && contentScroll.getViewport() != null) {
                     Component view = contentScroll.getViewport().getView();
                     if (view != null) {
                         view.revalidate();
                         view.repaint();
                     }
                 }
             });
         }
     });
   }

    
    // ============================================
    // HELPER METHODS FOR UI STYLING
    // ============================================

    private void stylePrimaryButton(MyButton btn) {
        btn.setBackgroundColor(PRIMARY_COLOR);
        btn.setHoverColor(PRIMARY_HOVER);
        btn.setPressedColor(PRIMARY_HOVER.darker());
        btn.setFont(FONT_BUTTON_MEDIUM);
        btn.setForeground(Color.WHITE);
    }

    private void styleInfoButton(MyButton btn) {
        btn.setBackgroundColor(INFO_COLOR);
        btn.setHoverColor(INFO_HOVER);
        btn.setPressedColor(INFO_HOVER.darker());
        btn.setFont(FONT_BUTTON_MEDIUM);
        btn.setForeground(Color.WHITE);
    }

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
  
    private void initComponents() {

        bg = new javax.swing.JLayeredPane();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        bg.setBackground(new java.awt.Color(255, 255, 255));
        bg.setOpaque(true);

        // Sử dụng BorderLayout thay vì GroupLayout để responsive
        bg.setLayout(new BorderLayout());

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(bg, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(bg, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    public static void main(String args[]) {
        
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(Bill_ExportDetails.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(Bill_ExportDetails.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(Bill_ExportDetails.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(Bill_ExportDetails.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new Bill_ExportDetails().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
}
