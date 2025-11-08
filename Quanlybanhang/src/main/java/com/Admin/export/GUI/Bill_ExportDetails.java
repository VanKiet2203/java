
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
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import net.miginfocom.swing.MigLayout;
import static com.ComponentandDatabase.Components.UIConstants.*;
import java.awt.GridLayout;
import java.awt.FlowLayout;
import java.awt.BorderLayout;
import java.util.Locale;


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
            
            // Responsive padding
            int scrollWidth = contentScroll.getWidth() > 0 ? contentScroll.getWidth() : 800;
            int padding = Math.max(20, Math.min(40, scrollWidth / 30));
            page.setBorder(BorderFactory.createEmptyBorder(padding, padding, padding, padding));

            DTO_BillExportedDetail first = details.get(0);
            
            // ===== 1. SALES INVOICE Title (giống PDF) =====
            JLabel title = new JLabel("SALES INVOICE", JLabel.CENTER);
            title.setFont(new Font("Arial", Font.BOLD, 24));
            title.setForeground(Color.BLUE);
            title.setAlignmentX(0.5f);
            page.add(title);
            page.add(Box.createVerticalStrut(15));

            // ===== 2. Invoice No và Date (giống PDF) =====
            SimpleDateFormat dateTimeFormat = new SimpleDateFormat("dd/MM/yyyy hh:mm a", Locale.ENGLISH);
            String formattedDateTime = dateTimeFormat.format(first.getDateExported());
            
            JLabel invoiceLabel = new JLabel("Invoice No: " + first.getInvoiceNo(), JLabel.CENTER);
            invoiceLabel.setFont(new Font("Arial", Font.BOLD, 14));
            invoiceLabel.setAlignmentX(0.5f);
            page.add(invoiceLabel);
            
            JLabel dateLabel = new JLabel("Date: " + formattedDateTime, JLabel.CENTER);
            dateLabel.setFont(new Font("Arial", Font.PLAIN, 12));
            dateLabel.setAlignmentX(0.5f);
            page.add(dateLabel);
            page.add(Box.createVerticalStrut(20));

            // ===== 3. Two-column layout: ADMIN INFO | CUSTOMER INFO (giống PDF) =====
            JPanel infoPanel = new JPanel(new GridLayout(1, 2, 20, 0));
            infoPanel.setBackground(Color.WHITE);
            infoPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 150));
            
            // Admin Information Panel
            JPanel adminPanel = createInfoSection("ADMIN INFORMATION");
            String adminName = getAdminName(first.getAdminId());
            addInfoRowToSection(adminPanel, "Admin ID:", first.getAdminId());
            addInfoRowToSection(adminPanel, "Admin Name:", adminName);
            
            // Customer Information Panel
            JPanel customerPanel = createInfoSection("CUSTOMER INFORMATION");
            com.User.dashboard_user.DTO.DTOProfile_cus customer = busExportBill.getCustomerInfoSafe(first.getCustomerId());
            if (customer != null) {
                addInfoRowToSection(customerPanel, "Customer ID:", customer.getCustomerID());
                addInfoRowToSection(customerPanel, "Customer Name:", customer.getFullName());
                addInfoRowToSection(customerPanel, "Address:", customer.getAddress());
                addInfoRowToSection(customerPanel, "Contact:", customer.getContact());
            }
            
            infoPanel.add(adminPanel);
            infoPanel.add(customerPanel);
            page.add(infoPanel);
            page.add(Box.createVerticalStrut(20));
            page.add(separator());

            // ===== 4. ORDER DETAILS Table (giống PDF) =====
            JLabel orderDetailsLabel = new JLabel("ORDER DETAILS", JLabel.LEFT);
            orderDetailsLabel.setFont(new Font("Arial", Font.BOLD, 14));
            orderDetailsLabel.setForeground(Color.DARK_GRAY);
            page.add(orderDetailsLabel);
            page.add(Box.createVerticalStrut(10));

            // Tạo table model
            String[] columns = {"No.", "Product ID", "Product Name", "Quantity", "Unit Price", "Total Price", "Warranty Start", "Warranty End"};
            DefaultTableModel tableModel = new DefaultTableModel(columns, 0) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            };

            java.math.BigDecimal grandBefore = java.math.BigDecimal.ZERO;
            java.math.BigDecimal grandAfter = java.math.BigDecimal.ZERO;
            int totalProducts = 0;
            int rowNum = 1;
            
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
            com.Admin.export.BUS.BUS_OrderDetail busOrderDetail = new com.Admin.export.BUS.BUS_OrderDetail();

            for (DTO_BillExportedDetail d : details) {
                totalProducts += d.getQuantity();
                grandBefore = grandBefore.add(d.getTotalPriceBefore());
                grandAfter = grandAfter.add(d.getTotalPriceAfter());
                
                String productName = busOrderDetail.getProductName(d.getProductId());
                String warrantyStart = d.getStartDate() != null ? dateFormat.format(d.getStartDate()) : "N/A";
                String warrantyEnd = d.getEndDate() != null ? dateFormat.format(d.getEndDate()) : "N/A";
                
                tableModel.addRow(new Object[]{
                    rowNum++,
                    d.getProductId(),
                    productName,
                    d.getQuantity(),
                    String.format("%,d VND", d.getUnitPrice().longValue()),
                    String.format("%,d VND", d.getTotalPriceBefore().longValue()),
                    warrantyStart,
                    warrantyEnd
                });
            }

            // Tạo JTable
            JTable table = new JTable(tableModel);
            table.setFont(new Font("Arial", Font.PLAIN, 11));
            table.setRowHeight(30);
            table.setGridColor(Color.LIGHT_GRAY);
            table.setShowGrid(true);
            table.setBackground(Color.WHITE);
            table.setFillsViewportHeight(true);
            
            // Custom header renderer với màu xanh đậm và chữ trắng
            DefaultTableCellRenderer headerRenderer = new DefaultTableCellRenderer() {
                @Override
                public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                    super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                    setBackground(new Color(0, 51, 102));
                    setForeground(Color.WHITE);
                    setFont(new Font("Arial", Font.BOLD, 11));
                    setHorizontalAlignment(JLabel.CENTER);
                    setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(0, 51, 102), 1),
                        BorderFactory.createEmptyBorder(8, 5, 8, 5)
                    ));
                    return this;
                }
            };
            
            // Áp dụng header renderer cho tất cả các cột header
            for (int i = 0; i < table.getColumnCount(); i++) {
                table.getColumnModel().getColumn(i).setHeaderRenderer(headerRenderer);
            }
            
            // Đảm bảo header hiển thị
            table.getTableHeader().setReorderingAllowed(false);
            table.getTableHeader().setResizingAllowed(true);
            table.getTableHeader().setFont(new Font("Arial", Font.BOLD, 11));
            table.getTableHeader().setBackground(new Color(0, 51, 102));
            table.getTableHeader().setForeground(Color.WHITE);
            table.getTableHeader().setPreferredSize(new Dimension(table.getTableHeader().getWidth(), 35));
            
            // Custom cell renderer cho data rows với alternate colors
            DefaultTableCellRenderer cellRenderer = new DefaultTableCellRenderer() {
                @Override
                public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                    super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                    
                    // Alternate row colors
                    if (row % 2 == 0) {
                        setBackground(Color.WHITE);
                    } else {
                        setBackground(new Color(248, 248, 248));
                    }
                    
                    setForeground(Color.BLACK);
                    setFont(new Font("Arial", Font.PLAIN, 11));
                    
                    // Alignment based on column
                    if (column == 0 || column == 3) { // No. and Quantity - center
                        setHorizontalAlignment(JLabel.CENTER);
                    } else if (column == 4 || column == 5) { // Unit Price and Total Price - right
                        setHorizontalAlignment(JLabel.RIGHT);
                    } else { // Text columns - left
                        setHorizontalAlignment(JLabel.LEFT);
                    }
                    
                    setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1),
                        BorderFactory.createEmptyBorder(5, 8, 5, 8)
                    ));
                    
                    return this;
                }
            };
            
            // Áp dụng cell renderer cho tất cả các cột
            for (int i = 0; i < table.getColumnCount(); i++) {
                table.getColumnModel().getColumn(i).setCellRenderer(cellRenderer);
            }
            
            // Set column widths
            table.getColumnModel().getColumn(0).setPreferredWidth(50);  // No.
            table.getColumnModel().getColumn(1).setPreferredWidth(100); // Product ID
            table.getColumnModel().getColumn(2).setPreferredWidth(200); // Product Name
            table.getColumnModel().getColumn(3).setPreferredWidth(80);  // Quantity
            table.getColumnModel().getColumn(4).setPreferredWidth(140); // Unit Price
            table.getColumnModel().getColumn(5).setPreferredWidth(140); // Total Price
            table.getColumnModel().getColumn(6).setPreferredWidth(110); // Warranty Start
            table.getColumnModel().getColumn(7).setPreferredWidth(110); // Warranty End
            
            // Wrap table in scroll pane và căn giữa
            JScrollPane tableScrollPane = new JScrollPane(table);
            tableScrollPane.setPreferredSize(new Dimension(scrollWidth - padding * 2, Math.min(400, tableModel.getRowCount() * 30 + 35)));
            tableScrollPane.setMaximumSize(new Dimension(Integer.MAX_VALUE, 500));
            tableScrollPane.setAlignmentX(Component.CENTER_ALIGNMENT);
            tableScrollPane.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1));
            
            // Panel wrapper để căn giữa bảng
            JPanel tableWrapper = new JPanel();
            tableWrapper.setLayout(new BoxLayout(tableWrapper, BoxLayout.Y_AXIS));
            tableWrapper.setBackground(Color.WHITE);
            tableWrapper.setAlignmentX(Component.CENTER_ALIGNMENT);
            tableWrapper.add(tableScrollPane);
            
            page.add(tableWrapper);
            page.add(Box.createVerticalStrut(15));

            // ===== 5. Payment Method + Order Summary (giống PDF - 2 cột) =====
            // Get bill header information
            com.Admin.export.DTO.DTO_BillExported billHeader = null;
            try {
                billHeader = busExportBill.getExportBillDetailsForInsurance(first.getInvoiceNo(), first.getAdminId());
            } catch (Exception e) {
                // Continue without header info
            }
            
            // Tính toán các giá trị
            java.math.BigDecimal subtotalBefore = grandBefore;
            java.math.BigDecimal discountAmount = java.math.BigDecimal.ZERO;
            java.math.BigDecimal subtotalAfter = grandAfter;
            java.math.BigDecimal vatAmount = java.math.BigDecimal.ZERO;
            java.math.BigDecimal finalTotal = grandAfter;
            
            // Lấy promotion code
            String promoCode = billHeader != null ? billHeader.getPromotionCode() : null;
            if ((promoCode == null || promoCode.trim().isEmpty()) && !details.isEmpty()) {
                promoCode = details.get(0).getPromotionCode();
            }
            
            // Tính discount
            java.math.BigDecimal discountPercent = java.math.BigDecimal.ZERO;
            if (promoCode != null && !promoCode.trim().isEmpty()) {
                try {
                    com.Admin.promotion.BUS.BUSPromotion pBus = new com.Admin.promotion.BUS.BUSPromotion();
                    com.Admin.promotion.DTO.DTOPromotion p = pBus.findActivePromotion(promoCode);
                    if (p != null && p.getDiscountPercent() != null) {
                        discountPercent = p.getDiscountPercent();
                        discountAmount = subtotalBefore.multiply(discountPercent)
                            .divide(new java.math.BigDecimal(100), 2, java.math.RoundingMode.HALF_UP);
                        subtotalAfter = subtotalBefore.subtract(discountAmount);
                    }
                } catch (Exception e) {
                    System.err.println("Error calculating discount: " + e.getMessage());
                }
            }
            
            // Tính VAT
            if (billHeader != null && billHeader.getVatAmount() != null) {
                vatAmount = billHeader.getVatAmount();
                finalTotal = billHeader.getTotalAmount() != null ? billHeader.getTotalAmount() : subtotalAfter.add(vatAmount);
            } else {
                vatAmount = subtotalAfter.multiply(new java.math.BigDecimal("8.00"))
                    .divide(new java.math.BigDecimal(100), 2, java.math.RoundingMode.HALF_UP);
                finalTotal = subtotalAfter.add(vatAmount);
            }
            
            // Get payment method
            String paymentMethod = "Cash";
            if (billHeader != null && billHeader.getOrderNo() != null) {
                try {
                    paymentMethod = busOrderDetail.getPayment(billHeader.getOrderNo());
                    if (paymentMethod == null || paymentMethod.isEmpty() || paymentMethod.equals("Unknown")) {
                        paymentMethod = "Cash";
                    }
                } catch (Exception e) {
                    paymentMethod = "Cash";
                }
            }
            
            // Two-column layout: Payment Method | Order Summary
            JPanel bottomPanel = new JPanel(new GridLayout(1, 2, 20, 0));
            bottomPanel.setBackground(Color.WHITE);
            bottomPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 200));
            
            // Left: Payment Method
            JPanel paymentPanel = new JPanel();
            paymentPanel.setLayout(new BoxLayout(paymentPanel, BoxLayout.Y_AXIS));
            paymentPanel.setBackground(Color.WHITE);
            paymentPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
            
            JLabel paymentLabel = new JLabel("Payment Method: " + paymentMethod);
            paymentLabel.setFont(new Font("Arial", Font.BOLD, 13));
            paymentLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
            paymentPanel.add(paymentLabel);
            
            // Right: Order Summary
            JPanel summaryPanel = new JPanel();
            summaryPanel.setLayout(new BoxLayout(summaryPanel, BoxLayout.Y_AXIS));
            summaryPanel.setBackground(Color.WHITE);
            summaryPanel.setAlignmentX(Component.RIGHT_ALIGNMENT);
            
            addRightAlignedLabel(summaryPanel, "Total Products: " + totalProducts, new Font("Arial", Font.BOLD, 12), Color.RED);
            addRightAlignedLabel(summaryPanel, "Subtotal (before discount): " + String.format("%,d VND", subtotalBefore.longValue()), new Font("Arial", Font.BOLD, 12), Color.RED);
            
            if (discountPercent.compareTo(java.math.BigDecimal.ZERO) > 0) {
                addRightAlignedLabel(summaryPanel, String.format("Discount (%.1f%%): -%,d VND", discountPercent.doubleValue(), discountAmount.longValue()), new Font("Arial", Font.BOLD, 12), Color.RED);
            }
            
            addRightAlignedLabel(summaryPanel, "Subtotal (after discount): " + String.format("%,d VND", subtotalAfter.longValue()), new Font("Arial", Font.BOLD, 12), Color.RED);
            addRightAlignedLabel(summaryPanel, "VAT (8% after discount): " + String.format("%,d VND", vatAmount.longValue()), new Font("Arial", Font.BOLD, 12), Color.RED);
            addRightAlignedLabel(summaryPanel, "Total Amount (incl. VAT): " + String.format("%,d VND", finalTotal.longValue()), new Font("Arial", Font.BOLD, 12), Color.RED);
            
            bottomPanel.add(paymentPanel);
            bottomPanel.add(summaryPanel);
            page.add(bottomPanel);
            page.add(Box.createVerticalStrut(20));
            
            // Thank you message (giống PDF)
            JLabel thankYouLabel = new JLabel("Thank you for your purchase!", JLabel.CENTER);
            thankYouLabel.setFont(new Font("Arial", Font.ITALIC, 12));
            thankYouLabel.setForeground(Color.GRAY);
            thankYouLabel.setAlignmentX(0.5f);
            page.add(thankYouLabel);

            contentScroll.setViewportView(page);
        } catch (Exception e) {
            CustomDialog.showError("Error loading bill details: " + e.getMessage());
        }
    }

    // Helper methods
    private JSeparator separator() {
        JSeparator s = new JSeparator();
        s.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        s.setForeground(Color.LIGHT_GRAY);
        return s;
    }
    
    private JPanel createInfoSection(String title) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        
        // Header with blue background (giống PDF)
        JLabel headerLabel = new JLabel(title);
        headerLabel.setFont(new Font("Arial", Font.BOLD, 12));
        headerLabel.setForeground(Color.WHITE);
        headerLabel.setOpaque(true);
        headerLabel.setBackground(new Color(0, 51, 102));
        headerLabel.setBorder(BorderFactory.createEmptyBorder(5, 8, 5, 8));
        headerLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        headerLabel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        
        panel.add(headerLabel);
        panel.add(Box.createVerticalStrut(8));
        
        return panel;
    }
    
    private void addInfoRowToSection(JPanel panel, String label, String value) {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 2));
        row.setBackground(Color.WHITE);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JLabel lblLabel = new JLabel(label);
        lblLabel.setFont(new Font("Arial", Font.BOLD, 11));
        
        JLabel lblValue = new JLabel(value != null ? value : "N/A");
        lblValue.setFont(new Font("Arial", Font.PLAIN, 11));
        
        row.add(lblLabel);
        row.add(lblValue);
        panel.add(row);
    }
    
    private void addRightAlignedLabel(JPanel panel, String text, Font font, Color color) {
        JLabel label = new JLabel(text);
        label.setFont(font);
        label.setForeground(color);
        label.setAlignmentX(Component.RIGHT_ALIGNMENT);
        panel.add(label);
    }
    
    private String getAdminName(String adminId) {
        try {
            com.Admin.dashboard_admin.DAO.DAOProfile_ad dao = new com.Admin.dashboard_admin.DAO.DAOProfile_ad();
            return dao.getAdminName(adminId);
        } catch (Exception e) {
            return "N/A";
        }
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
