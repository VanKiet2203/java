
package com.Admin.export.GUI;

import com.ComponentandDatabase.Components.MyButton;
import com.ComponentandDatabase.Components.MyCombobox;
import com.ComponentandDatabase.Components.MyPanel;
import com.ComponentandDatabase.Components.MyTable;
import com.ComponentandDatabase.Components.MyTextField;
import com.Admin.export.BUS.BUS_ExportBill;
import com.Admin.export.DTO.DTO_BillExportedDetail;
import com.ComponentandDatabase.Components.CustomDialog;
import java.text.SimpleDateFormat;

import java.util.Locale;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.util.List;
import java.util.Date;
import java.text.ParseException;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
import net.miginfocom.swing.MigLayout;
import static com.ComponentandDatabase.Components.UIConstants.*;


public class Bill_ExportDetails extends javax.swing.JFrame {
     private JLabel lblTitle;
     private MyPanel panelTitle;
     private MyCombobox<String> cmbSearch;
     private MyButton bntSearch, bntRefresh;
     private MyTextField txtSearch;
     private MyTable tableBillDetail;
     private BUS_ExportBill busExportBill;
     private javax.swing.JLayeredPane bg;
 
     private DefaultTableModel model;
    public Bill_ExportDetails() {
        initComponents();
        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE); 
        setAlwaysOnTop(true); // Luôn hiển thị trên cùng
        init();
    }
    
    // Method để thiết lập thông tin hóa đơn
    public void setOrderInfo(String orderNo, String customerID, String orderDate, String totalAmount) {
        // Có thể sử dụng thông tin này để hiển thị chi tiết hóa đơn
        // Ví dụ: cập nhật title hoặc load dữ liệu chi tiết
        if (lblTitle != null) {
            lblTitle.setText("CHI TIẾT HÓA ĐƠN - " + orderNo);
        }
        
        // Load chi tiết hóa đơn dựa trên orderNo
        loadOrderDetails(orderNo);
    }
    
    // Method để load chi tiết hóa đơn
    private void loadOrderDetails(String orderNo) {
        try {
            busExportBill = new BUS_ExportBill();
            List<DTO_BillExportedDetail> allDetails = busExportBill.getAllBillDetails();
            
            // Clear table
            model.setRowCount(0);
            
            // Filter và add data to table
            for (DTO_BillExportedDetail detail : allDetails) {
                if (detail.getInvoiceNo().equals(orderNo)) {
                    model.addRow(new Object[]{
                        detail.getProductId(),
                        "Product Name", // Có thể cần lấy từ bảng product
                        detail.getQuantity(),
                        detail.getUnitPrice(),
                        detail.getTotalPriceAfter()
                    });
                }
            }
        } catch (Exception e) {
            CustomDialog.showError("Lỗi khi tải chi tiết hóa đơn: " + e.getMessage());
        }
    }

   public void init() {
     // Thiết lập layout chính
     bg.setLayout(new MigLayout("fillx, insets 0", "[grow]", "[][][grow]"));

     // 1. Panel tiêu đề
     panelTitle = new MyPanel(new MigLayout("fill, insets 0"));
     panelTitle.setGradientColors(Color.decode("#1CB5E0"), Color.decode("#4682B4"), MyPanel.VERTICAL_GRADIENT);

     lblTitle = new JLabel("Sales Invoice Details", JLabel.CENTER);
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


        // 5️⃣ Tạo bảng với style chuẩn
        tableBillDetail = createStyledTable(model);
        tableBillDetail.setRowHeight(30);

        JScrollPane scrollPane = MyTable.createScrollPane(tableBillDetail, 10, 150, 950, 630);

        // 7️⃣ Tùy chỉnh thanh cuộn
        scrollPane.getVerticalScrollBar().setPreferredSize(new Dimension(15, Integer.MAX_VALUE));
        scrollPane.getHorizontalScrollBar().setPreferredSize(new Dimension(Integer.MAX_VALUE, 15));
         SwingUtilities.invokeLater(() -> {
             loadBillDetailsData();
              tableBillDetail.adjustColumnWidths();        // Căn chỉnh cột
          });          
        
        
        bg.add(scrollPane, "pos 10 140, w 1430!, h 630!");
        
       
            // TextField search
            txtSearch = new MyTextField();
            txtSearch.setHint("Search something...");
            txtSearch.setTextFont(FONT_CONTENT_MEDIUM);
            bg.add(txtSearch, "pos 510 75, w 300!, h 35!");
            
            // ComboBox search
            String[] items = {"Invoice.No", "Admin.ID", "Customer.ID", "Date"};
            cmbSearch = new MyCombobox<>(items);
            cmbSearch.setBounds(30, 10, 165,35);
            cmbSearch.setCustomFont(FONT_CONTENT_MEDIUM);
            cmbSearch.setCustomColors(Color.WHITE, Color.GRAY, Color.BLACK);
            //cmbSearch.setMaximumRowCount(5); // Giới hạn dòng dropdown nếu dài
            cmbSearch.repaint();
            cmbSearch.revalidate();

           bg.add(cmbSearch, "pos 350 75, w 140!, h 35!");
            
            bntSearch = new MyButton("Tìm kiếm", 20);
            stylePrimaryButton(bntSearch);
            bntSearch.setButtonIcon("src\\main\\resources\\Icons\\Admin_icon\\search.png", 25, 25, 5, SwingConstants.RIGHT, SwingConstants.CENTER);    
            bntSearch.addActionListener((e) -> {
                String searchType = cmbSearch.getSelectedItem().toString();
                String keyword = txtSearch.getText().trim();
                busExportBill= new BUS_ExportBill();
                busExportBill.searchBillDetails(searchType, keyword);
                // Xử lý đặc biệt nếu tìm kiếm theo ngày
                    if (searchType.equals("Date")) {
                        try {
                            // Chuẩn hóa chuỗi ngày tháng (cho phép nhập 1 hoặc 2 chữ số cho ngày/tháng)
                            keyword = normalizeDateString(keyword);

                            // Chuyển từ dd/MM/yyyy sang yyyy-MM-dd để tìm kiếm trong SQL
                            SimpleDateFormat inputFormat = new SimpleDateFormat("dd/MM/yyyy");
                            inputFormat.setLenient(false); // Không chấp nhận ngày không hợp lệ

                            SimpleDateFormat sqlFormat = new SimpleDateFormat("yyyy-MM-dd");

                            // Parse và format lại ngày
                            Date parsedDate = inputFormat.parse(keyword);
                            keyword = sqlFormat.format(parsedDate);
                        } catch (ParseException ex) {
                            CustomDialog.showError("Invalid date format. Please enter date in dd/MM/yyyy format (e.g. 01/01/2023)");
                            return;
                        }
                }
                 // Lấy kết quả từ BUS
                List<DTO_BillExportedDetail> results = busExportBill.searchBillDetails(searchType, keyword);

                // Cập nhật dữ liệu lên bảng
                updateTableData(results);
        });
        bg.add(bntSearch, "pos 820 75, w 120!, h 35");
        
          bntRefresh = new MyButton("Refresh", 20);
          styleInfoButton(bntRefresh);
          bntRefresh.setButtonIcon("src\\main\\resources\\Icons\\Admin_icon\\refresh.png", 25, 25, 10, SwingConstants.RIGHT, SwingConstants.CENTER);
          bntRefresh.addActionListener((e) -> {
              Refresh();
          });
          
          bg.add(bntRefresh, "pos 100 75, w 140!, h 35");
        
     
   }

    public void loadBillDetailsData() {
       // Xóa dữ liệu cũ trong model
       model.setRowCount(0);

       // Lấy dữ liệu từ BUS
       busExportBill = new BUS_ExportBill();
       List<DTO_BillExportedDetail> billDetails = busExportBill.getAllBillDetails();

       // Định dạng cho ngày và giờ
       SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
       SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a", Locale.ENGLISH);

       // Định dạng tiền tệ
      

       // Thêm dữ liệu mới từ danh sách DTO
       for (DTO_BillExportedDetail detail : billDetails) {
           // Get promotion name
           String promotionName = "N/A";
           if (detail.getPromotionCode() != null && !detail.getPromotionCode().isEmpty()) {
               try {
                   com.Admin.promotion.BUS.BUSPromotion busPromotion = new com.Admin.promotion.BUS.BUSPromotion();
                   com.Admin.promotion.DTO.DTOPromotion promotion = busPromotion.getPromotionByCode(detail.getPromotionCode());
                   if (promotion != null) {
                       promotionName = promotion.getPromotionName();
                   }
               } catch (Exception e) {
                   // Use default value if error
               }
           }
           
           Object[] rowData = new Object[]{
               detail.getInvoiceNo(),
               detail.getAdminId(),
               detail.getCustomerId(),
               detail.getProductId(),
               detail.getUnitPrice(),
               detail.getQuantity(),
               detail.getPromotionCode() != null ? detail.getPromotionCode() : "N/A",
               promotionName,
               detail.getDiscountPercent() + "%",
               detail.getTotalPriceBefore(),
               detail.getTotalPriceAfter(),
               dateFormat.format(detail.getDateExported()),
               timeFormat.format(detail.getTimeExported())
           };
           model.addRow(rowData);
       }
    
    // Căn chỉnh cột sau khi load dữ liệu
    SwingUtilities.invokeLater(() -> {
        tableBillDetail.adjustColumnWidths();
    });
} 
    private void updateTableData(List<DTO_BillExportedDetail> data) {
        
           // 1️⃣ Tên cột
        String[] columnNames = {
            "Invoice.No", "Admin.ID", "Customer.ID", "Product.ID", 
            "Unit Price", "Quantity" , "Promotion Code", "Promotion Name", "Discount %", "Total Price Before",
            "Total Price After", "Date Exported", "Time Exported"
        };
        model.setRowCount(0);
        model.setColumnIdentifiers(columnNames); // columnNames là mảng String chứa tên cột

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");

        for (DTO_BillExportedDetail detail : data) {
            // Get promotion name
            String promotionName = "N/A";
            if (detail.getPromotionCode() != null && !detail.getPromotionCode().isEmpty()) {
                try {
                    com.Admin.promotion.BUS.BUSPromotion busPromotion = new com.Admin.promotion.BUS.BUSPromotion();
                    com.Admin.promotion.DTO.DTOPromotion promotion = busPromotion.getPromotionByCode(detail.getPromotionCode());
                    if (promotion != null) {
                        promotionName = promotion.getPromotionName();
                    }
                } catch (Exception e) {
                    // Use default value if error
                }
            }
            
            Object[] row = new Object[]{
                detail.getInvoiceNo(),
                detail.getAdminId(),
                detail.getCustomerId(),
                detail.getProductId(),
                detail.getUnitPrice(),
                detail.getQuantity(),
                detail.getPromotionCode() != null ? detail.getPromotionCode() : "N/A",
                promotionName,
                detail.getDiscountPercent()+"%",
                detail.getTotalPriceBefore(),
                detail.getTotalPriceAfter(),
                detail.getDateExported() != null ? dateFormat.format(detail.getDateExported()) : "",
                detail.getTimeExported() != null ? timeFormat.format(detail.getTimeExported()) : ""
            };

            // Kiểm tra số lượng cột trước khi thêm
            if (row.length == model.getColumnCount()) {
                model.addRow(row);
            } else {
                System.err.println("The number of columns does not match: " + row.length + " vs " + model.getColumnCount());
            }
        }

      
        if (tableBillDetail.getCellEditor() != null) {
            tableBillDetail.getCellEditor().stopCellEditing();
        }

        tableBillDetail.adjustColumnWidths();
    }
    private void Refresh(){
        loadBillDetailsData();
        tableBillDetail.adjustColumnWidths();
        cmbSearch.setSelectedIndex(0);
        txtSearch.setText(null);
    }
    
    private String normalizeDateString(String dateString) {
        // Xóa tất cả ký tự không phải số
        String numbersOnly = dateString.replaceAll("[^0-9]", "");

        // Đảm bảo đủ 8 chữ số (thêm số 0 nếu cần)
        if (numbersOnly.length() == 6) {
            numbersOnly = "0" + numbersOnly; // Thêm 0 vào ngày nếu cần
            numbersOnly = numbersOnly.substring(0, 2) + "0" + numbersOnly.substring(2); // Thêm 0 vào tháng nếu cần
        } else if (numbersOnly.length() == 7) {
            // Xác định xem cần thêm 0 vào ngày hay tháng
            if (dateString.indexOf('/') == 1) { // Ngày có 1 chữ số
                numbersOnly = "0" + numbersOnly;
            } else { // Tháng có 1 chữ số
                numbersOnly = numbersOnly.substring(0, 2) + "0" + numbersOnly.substring(2);
            }
        }

        // Định dạng lại thành dd/MM/yyyy
        if (numbersOnly.length() >= 8) {
            return numbersOnly.substring(0, 2) + "/" + numbersOnly.substring(2, 4) + "/" + numbersOnly.substring(4, 8);
        }

        return dateString; 
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
            .addGap(0, 1447, Short.MAX_VALUE)
        );
        bgLayout.setVerticalGroup(
            bgLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 800, Short.MAX_VALUE)
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
