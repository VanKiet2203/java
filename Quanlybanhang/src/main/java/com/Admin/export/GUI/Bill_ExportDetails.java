
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
import java.util.List;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
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
        setSize(1400, 1000); // Tăng kích thước cửa sổ mặc định
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
            page.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.decode("#E0E0E0"), 1),
                BorderFactory.createEmptyBorder(24, 32, 24, 32)
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

                JPanel card = new JPanel(new GridLayout(2, 4, 12, 8));
                card.setBackground(Color.decode("#F8F9FA"));
                card.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(Color.decode("#DEE2E6"), 1),
                    BorderFactory.createEmptyBorder(16, 16, 16, 16)
                ));

                card.add(field("Product ID", d.getProductId()));
                card.add(field("Unit Price", String.valueOf(d.getUnitPrice())));
                card.add(field("Quantity", String.valueOf(d.getQuantity())));
                card.add(field("Warranty", warrantyText));

                String promoCode = (d.getPromotionCode() != null && !d.getPromotionCode().isEmpty()) ? d.getPromotionCode() : "N/A";
                card.add(field("Promotion Code", promoCode));
                card.add(field("Discount %", d.getDiscountPercent() + "%"));
                card.add(field("Total Before", String.valueOf(d.getTotalPriceBefore())));
                card.add(field("Total After", String.valueOf(d.getTotalPriceAfter())));

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
            
            // Tạo panel tổng tiền với style đặc biệt
            JPanel totalPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
            totalPanel.setBackground(Color.WHITE);
            totalPanel.setBorder(BorderFactory.createEmptyBorder(8, 0, 8, 0));
            
            JLabel subtotalLabel = new JLabel("Subtotal: ");
            subtotalLabel.setFont(new Font("Arial", Font.BOLD, 14));
            subtotalLabel.setForeground(Color.decode("#7F8C8D"));
            JLabel subtotalValue = new JLabel(String.valueOf(grandAfter));
            subtotalValue.setFont(new Font("Arial", Font.PLAIN, 14));
            subtotalValue.setForeground(Color.decode("#2C3E50"));
            
            totalPanel.add(subtotalLabel);
            totalPanel.add(subtotalValue);
            
            // Add VAT information if available
            if (billHeader != null && billHeader.getVatPercent() != null && billHeader.getVatAmount() != null) {
                totalPanel.add(Box.createHorizontalStrut(20));
                
                JLabel vatLabel = new JLabel("VAT (" + billHeader.getVatPercent() + "%): ");
                vatLabel.setFont(new Font("Arial", Font.BOLD, 14));
                vatLabel.setForeground(Color.decode("#7F8C8D"));
                JLabel vatValue = new JLabel(String.valueOf(billHeader.getVatAmount()));
                vatValue.setFont(new Font("Arial", Font.PLAIN, 14));
                vatValue.setForeground(Color.decode("#2C3E50"));
                
                totalPanel.add(vatLabel);
                totalPanel.add(vatValue);
            }
            
            totalPanel.add(Box.createHorizontalStrut(20));
            
            // Total after VAT
            java.math.BigDecimal finalTotal = grandAfter;
            if (billHeader != null && billHeader.getTotalAmount() != null) {
                finalTotal = billHeader.getTotalAmount();
            } else if (billHeader != null && billHeader.getVatAmount() != null) {
                finalTotal = grandAfter.add(billHeader.getVatAmount());
            }
            
            JLabel totalLabel = new JLabel("Total Amount (incl. VAT): ");
            totalLabel.setFont(new Font("Arial", Font.BOLD, 16));
            totalLabel.setForeground(Color.decode("#E74C3C"));
            JLabel totalValue = new JLabel(String.valueOf(finalTotal));
            totalValue.setFont(new Font("Arial", Font.BOLD, 16));
            totalValue.setForeground(Color.decode("#E74C3C"));
            
            totalPanel.add(totalLabel);
            totalPanel.add(totalValue);
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
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(Color.WHITE);
        p.setBorder(BorderFactory.createEmptyBorder(2, 4, 2, 4));
        JLabel l = new JLabel(label);
        l.setFont(new Font("Arial", Font.PLAIN, 11));
        l.setForeground(Color.decode("#95A5A6"));
        JLabel v = new JLabel(value);
        v.setFont(new Font("Arial", Font.BOLD, 13));
        v.setForeground(Color.decode("#2C3E50"));
        p.add(l, BorderLayout.NORTH);
        p.add(v, BorderLayout.CENTER);
        return p;
    }

   public void init() {
     // Thiết lập layout chính
     bg.setLayout(new MigLayout("fillx, insets 0", "[grow]", "[][][grow]"));

     // 1. Panel tiêu đề
     panelTitle = new MyPanel(new MigLayout("fill, insets 0"));
     panelTitle.setGradientColors(Color.decode("#1CB5E0"), Color.decode("#4682B4"), MyPanel.VERTICAL_GRADIENT);

     lblTitle = new JLabel("Bill Export Details", JLabel.CENTER);
     lblTitle.setFont(new Font("Arial", Font.BOLD, 20));
     lblTitle.setForeground(Color.WHITE);

     panelTitle.add(lblTitle, "grow, push, align center");
     bg.add(panelTitle, "growx, h 40!, wrap"); // wrap để component sau xuống dòng
     
           // 1️⃣ Tên cột
        String[] columnNames = {
            "Invoice.No", "Admin.ID", "Customer.ID", "Product.ID", "Unit Price", "Quantity" , "Promotion Code", "Promotion Name", "Discount %", "Total Price Before",
            "Total Price After", "Date Exported", "Time Exported"
        };

        // 2️⃣ Tạo model
        model = new DefaultTableModel(columnNames, 0);


        // 5️⃣ Thay thế bảng bằng container scroll cho nội dung dạng trang
        contentScroll = new JScrollPane();
        contentScroll.setBorder(null);
        contentScroll.getVerticalScrollBar().setPreferredSize(new Dimension(15, Integer.MAX_VALUE));
        contentScroll.getHorizontalScrollBar().setPreferredSize(new Dimension(Integer.MAX_VALUE, 15));
        contentScroll.setViewportBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        bg.add(contentScroll, "pos 10 140, w 1360!, h 820!");
     
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

        javax.swing.GroupLayout bgLayout = new javax.swing.GroupLayout(bg);
        bg.setLayout(bgLayout);
        bgLayout.setHorizontalGroup(
            bgLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 1400, Short.MAX_VALUE)
        );
        bgLayout.setVerticalGroup(
            bgLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 1000, Short.MAX_VALUE)
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
