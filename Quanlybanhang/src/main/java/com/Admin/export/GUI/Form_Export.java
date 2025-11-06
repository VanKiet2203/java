
package com.Admin.export.GUI;

import com.Admin.dashboard_admin.GUI.Dashboard_ad;
import com.ComponentandDatabase.Components.MyButton;
import com.ComponentandDatabase.Components.MyCombobox;
import com.ComponentandDatabase.Components.MyPanel;
import com.ComponentandDatabase.Components.MyTable;
import com.ComponentandDatabase.Components.MyTextField;
import com.Admin.export.BUS.BUS_OrderDetail;
import com.Admin.export.BUS.BUS_ExportBill;
import com.Admin.export.DTO.DTO_Oderdetails;
import com.Admin.export.DTO.DTO_BillExport;
import com.Admin.export.DTO.DTO_BillExported;
import com.Admin.export.DTO.DTO_BillExportedDetail;
import com.Admin.promotion.BUS.BUSPromotion;
import com.Admin.promotion.DTO.DTOPromotion;
import com.User.order.GUI.OrderUpdateNotifier;
import com.User.dashboard_user.DTO.DTOProfile_cus;
import com.ComponentandDatabase.Components.CustomDialog;
import static com.ComponentandDatabase.Components.UIConstants.*;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.File;
import java.math.RoundingMode;
import java.util.Collections;
import java.math.BigDecimal;  // Cho BigDecimal
import java.util.ArrayList;
import javax.swing.JPanel;
import java.util.Random;
import javax.swing.JScrollPane;
import javax.swing.table.TableColumnModel;
import javax.swing.JLabel;
import java.awt.Color;
import java.awt.Font;
import javax.swing.*;
import java.text.SimpleDateFormat;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import javax.swing.SwingConstants;
import java.awt.Dimension;
import java.util.List;
import javax.swing.table.DefaultTableModel;

public class Form_Export extends JPanel {

    private JPanel panel, panelSearch,billBody;
    private MyPanel panelBill, panelTitle;
    private JLabel lblAdminID, lblAdminName, lblInvoice;
    private MyButton bntSearchOrder, bntExportFile, bntDetails, bntRefresh, bntAddBill, bntExport, bntWarranty;
    private MyTextField txtSearchOrder, txtAdminID, txtAdminName;
    private MyCombobox<String> cmbSearchOrder;
    private MyTable tableOrderDetails;
    private MyTable tableExportBills;
    public static String invoiceNo;
    public static String orderNo;
    private DefaultTableModel model;
    private DefaultTableModel modelExportBills;
    private JLabel lblPromotionInfo;
    private BUS_OrderDetail busOrderDetail;
    private BUS_ExportBill busExportBill;
    private BUSPromotion busPromotion;
    private DTOProfile_cus customer;
    private DTOPromotion currentPromotion;
    private JTabbedPane tablesTabbedPane;
    
    // Static reference để Form_Promotion có thể refresh promotion list
    private static Form_Export currentInstance = null;

    public Form_Export() {
        initComponents();
        init();
        currentInstance = this; // Đăng ký instance hiện tại
    }
    
    /**
     * Constructor với tham số để nhận OrderNo và CustomerID từ Order form
     */
    public Form_Export(String orderNo, String customerID) {
        Form_Export.orderNo = orderNo;
        initComponents();
        init();
        currentInstance = this; // Đăng ký instance hiện tại
        // Tự động load dữ liệu cho Order đã chọn
        loadSpecificOrderData(orderNo, customerID);
    }
    
    /**
     * Static method để refresh promotion list từ Form_Promotion
     * (Không cần thiết nữa vì promotion được lấy từ order)
     */
    public static void refreshPromotionsIfExists() {
        // Promotion code được lấy từ order, không cần refresh
    }

    private void initComponents() {
        setLayout(null);
        setPreferredSize(new Dimension(1400, 750)); // Reduced size for smaller screens
        setBackground(BG_WHITE); // Kiểm tra hiển thị
    }

    private void init() {
        panel = new JPanel();
        panel.setLayout(null);
        panel.setBounds(0, 0, 1400, 750); // Reduced bounds for smaller screens
        panel.setBackground(Color.WHITE);
        add(panel);
        
        // Title
        JLabel lblTitle = new JLabel("MANAGE INVOICE");
        lblTitle.setFont(FONT_TITLE_LARGE);
        lblTitle.setForeground(PRIMARY_COLOR);
        lblTitle.setBounds(20, 10, 400, 40);
        panel.add(lblTitle);
        
        // Tạo panelSearch với màu nền trắng
        panelSearch = new MyPanel(Color.WHITE);
        panelSearch.setLayout(null);
        panelSearch.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(PRIMARY_COLOR, 2),
            "Invoice Management",
            0, 0,
            FONT_TITLE_SMALL,
            PRIMARY_COLOR
        ));
        panelSearch.setBounds(20, 60, 1360, 90);
          
          bntRefresh = new MyButton("Refresh", 20);
          styleInfoButton(bntRefresh);
          bntRefresh.setBounds(620, 30, 120, 35);
          bntRefresh.setButtonIcon("src\\main\\resources\\Icons\\Admin_icon\\refresh.png", 25, 25, 10, SwingConstants.RIGHT, SwingConstants.CENTER);
          bntRefresh.addActionListener((e) -> {
              Refresh();
          });
          
          panelSearch.add(bntRefresh); 
          
          bntDetails = new MyButton("Details", 20);
          styleInfoButton(bntDetails);
          bntDetails.setButtonIcon("src\\main\\resources\\Icons\\Admin_icon\\bill_export.png", 25, 25, 5, SwingConstants.RIGHT, SwingConstants.CENTER);    
          bntDetails.setBounds(750, 30, 120, 35);
          bntDetails.addActionListener((e) -> {
              // Chỉ áp dụng cho tab Export Bills
              if (tablesTabbedPane == null || tablesTabbedPane.getSelectedIndex() != 1) {
                  CustomDialog.showError("Nút Details chỉ áp dụng cho bảng Export Bills.");
                  return;
              }
              int selectedRow = tableExportBills.getSelectedRow();
              if (selectedRow == -1) {
                  CustomDialog.showError("Vui lòng chọn một export bill để xem chi tiết!");
                  return;
              }
              String invoiceNoSel = tableExportBills.getValueAt(selectedRow, 0).toString();
              String adminIdSel = tableExportBills.getValueAt(selectedRow, 1).toString();
              Bill_ExportDetails wnd = new Bill_ExportDetails();
              wnd.setInvoiceInfo(invoiceNoSel, adminIdSel);
              wnd.setLocationRelativeTo(null);
              wnd.setVisible(true);
          });
          panelSearch.add(bntDetails);

          
          
          bntExportFile = new MyButton("Export", 20);
          bntExportFile.setBackgroundColor(Color.WHITE);
          bntExportFile.setPressedColor(Color.decode("#D3D3D3"));
          bntExportFile.setHoverColor(Color.decode("#EEEEEE"));
          bntExportFile.setFont(FONT_BUTTON_MEDIUM);
          bntExportFile.setForeground(Color.BLACK);
          bntExportFile.setBounds(880, 30, 120, 35);
          bntExportFile.setButtonIcon("src\\main\\resources\\Icons\\Admin_icon\\Excel.png", 30, 30, 10, SwingConstants.RIGHT, SwingConstants.CENTER);
          bntExportFile.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Save Excel File");
            fileChooser.setFileFilter(new FileNameExtensionFilter("Excel Files", "xlsx"));
            fileChooser.setSelectedFile(new File("Bill_Export_Report.xlsx"));

            int userSelection = fileChooser.showSaveDialog(null);
            if (userSelection == JFileChooser.APPROVE_OPTION) {
                File fileToSave = fileChooser.getSelectedFile();
                String filePath = fileToSave.getAbsolutePath();

                // Đảm bảo có đuôi .xlsx
                if (!filePath.toLowerCase().endsWith(".xlsx")) {
                    filePath += ".xlsx";
                }

                busExportBill = new BUS_ExportBill();
                boolean success = busExportBill.exportToExcel(filePath);

                if (success) {
                    CustomDialog.showSuccess("Export successful! File saved at: " + filePath);
                } else {
                    CustomDialog.showError("Export failed. Please try again.");
                }
            }
        });
          
          panelSearch.add(bntExportFile);
          
          // Search section - Moved inside panelSearch
          String[] itemsOrder = {"Order.No", "Customer.ID", "Date Order"};
          cmbSearchOrder= new MyCombobox<>(itemsOrder);
          cmbSearchOrder.setBounds(20, 30, 150, 35);
          cmbSearchOrder.setCustomFont(FONT_CONTENT_MEDIUM);
          cmbSearchOrder.setCustomColors(Color.WHITE, Color.GRAY, Color.BLACK);
          cmbSearchOrder.repaint();
          cmbSearchOrder.revalidate();

          SwingUtilities.invokeLater(() -> {
              cmbSearchOrder.repaint();
              cmbSearchOrder.revalidate();
          });

          panelSearch.add(cmbSearchOrder);
          
          txtSearchOrder = new MyTextField();
          txtSearchOrder.setHint("Search something...");
          txtSearchOrder.setBounds(180, 30, 300, 35);
          txtSearchOrder.setTextFont(FONT_CONTENT_MEDIUM);
          txtSearchOrder.setBorder(BorderFactory.createCompoundBorder(
              BorderFactory.createLineBorder(PRIMARY_COLOR, 1),
              BorderFactory.createEmptyBorder(5, 10, 5, 10)
          ));
          panelSearch.add(txtSearchOrder);
         
          bntSearchOrder= new MyButton("Search", 20);
          stylePrimaryButton(bntSearchOrder);
          bntSearchOrder.setButtonIcon("src\\main\\resources\\Icons\\Admin_icon\\search.png", 25, 25, 5, SwingConstants.RIGHT, SwingConstants.CENTER);     
          bntSearchOrder.setBounds(490, 30, 120, 35);
          bntSearchOrder.addActionListener(e -> {
             String searchType = (String) cmbSearchOrder.getSelectedItem();
             String keyword = txtSearchOrder.getText().trim();

              busOrderDetail = new BUS_OrderDetail();
             List<DTO_Oderdetails> searchResults = busOrderDetail.searchOrderDetails(searchType, keyword);

             // Gọi phương thức hiển thị kết quả lên table
             displaySearchResults(searchResults);
         });
          panelSearch.add(bntSearchOrder);
          
          panel.add(panelSearch);
          
          // Admin Info section - Outside Export Management panel, moved to left
          lblAdminID= new JLabel("Admin.ID");
          lblAdminID.setFont(FONT_CONTENT_MEDIUM);
          lblAdminID.setForeground(TEXT_PRIMARY);
          lblAdminID.setBounds(400, 160, 100, 25);
          panel.add(lblAdminID);
       
          txtAdminID = new MyTextField();
          txtAdminID.setBorder(BorderFactory.createLineBorder(Color.GRAY));
          txtAdminID.setTextColor(Color.RED);
          txtAdminID.setLocked(true);
          txtAdminID.setTextFont(FONT_CONTENT_MEDIUM);
          txtAdminID.setBackgroundColor(Color.WHITE);
          txtAdminID.setBounds(400, 185, 120, 35);
          txtAdminID.setText(Dashboard_ad.adminID);
          panel.add(txtAdminID);
          
         lblAdminName= new JLabel("Admin Name");
         lblAdminName.setFont(FONT_CONTENT_MEDIUM);
         lblAdminName.setForeground(TEXT_PRIMARY);
         lblAdminName.setBounds(530, 160, 100, 25);
         panel.add(lblAdminName);
         
          txtAdminName = new MyTextField();
          txtAdminName.setBorder(BorderFactory.createLineBorder(Color.GRAY));
          txtAdminName.setTextColor(Color.BLUE);
          txtAdminName.setLocked(true);
          txtAdminName.setTextFont(FONT_CONTENT_MEDIUM);
          txtAdminName.setBackgroundColor(Color.WHITE);
          txtAdminName.setBounds(530, 185, 120, 35);
          txtAdminName.setText(Dashboard_ad.getAdminName(txtAdminID.getText().strip()));
          panel.add(txtAdminName);
          
          // Promotion Code info - Hiển thị promotion code từ order (do user chọn)
          JLabel lblPromo = new JLabel("Promotion Code (from Order):");
          lblPromo.setFont(FONT_CONTENT_MEDIUM);
          lblPromo.setForeground(TEXT_PRIMARY);
          lblPromo.setBounds(660, 160, 250, 25);
          panel.add(lblPromo);

          // Label hiển thị promotion code từ order
          lblPromotionInfo = new JLabel("No promotion code selected");
          lblPromotionInfo.setFont(new Font("Arial", Font.ITALIC, 12));
          lblPromotionInfo.setForeground(Color.decode("#666666"));
          lblPromotionInfo.setBounds(660, 185, 300, 25);
          panel.add(lblPromotionInfo);
           



// Tạo panelBill với layout và style đơn giản      
            
       panelBill = new MyPanel();
       panelBill.setLayout(new BorderLayout());
       panelBill.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1)); // Chỉ 1 viền chính
       panelBill.setBackground(Color.WHITE);
       panelBill.setBounds(820, 200, 450, 450);
       panel.add(panelBill);

       // Tạo panel title "Bill For Order" (không thêm border riêng)
       JPanel paneltitle = new JPanel();
       paneltitle.setBackground(PRIMARY_COLOR);
       paneltitle.setPreferredSize(new Dimension(450, 30)); // Fixed height
       JLabel lblBillTitle = new JLabel("BILL FOR ORDER", JLabel.CENTER);
       lblBillTitle.setFont(new Font("Arial", Font.BOLD, 16));
       lblBillTitle.setForeground(Color.WHITE);
       paneltitle.add(lblBillTitle);
       panelBill.add(paneltitle, BorderLayout.NORTH);

       // Tạo panel content chính với scroll (không border)
       JPanel billContent = new JPanel();
       billContent.setLayout(new BorderLayout());
       billContent.setBackground(Color.WHITE);

       // Các panel con (không border)
       billBody = new JPanel();
       billBody.setLayout(new BoxLayout(billBody, BoxLayout.Y_AXIS));
       billBody.setBackground(Color.WHITE);

       // Scroll pane chính (không border)
       JScrollPane mainScrollPane = new JScrollPane(billBody);
       mainScrollPane.setBorder(null); // Loại bỏ border mặc định của scroll pane
       mainScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
       mainScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

       // Thêm vào billContent
       billContent.add(mainScrollPane, BorderLayout.CENTER);
       panelBill.add(billContent, BorderLayout.CENTER);
      
            // 1️⃣ Tên cột
        String[] columnNames = {
            "Order.No", "Customer.ID", "Product.ID", "Price", 
            "Quantity", "Date Order", "Time Order" , "Status"
        };

        // 2️⃣ Tạo model
        model = new DefaultTableModel(columnNames, 0);


        // 5️⃣ Tạo bảng với style chuẩn giống Product
        tableOrderDetails = createStyledTable(model);

        JScrollPane scrollPaneOrders = MyTable.createScrollPane(tableOrderDetails, 0, 0, 0, 0);

        // 7️⃣ Tùy chỉnh thanh cuộn (orders tab)
        scrollPaneOrders.getVerticalScrollBar().setPreferredSize(new Dimension(15, Integer.MAX_VALUE));
        scrollPaneOrders.getHorizontalScrollBar().setPreferredSize(new Dimension(Integer.MAX_VALUE, 15));

           SwingUtilities.invokeLater(() -> {
              loadConfirmedOrderDetailsToTable();
               tableOrderDetails.adjustColumnWidths();         // Căn chỉnh cột
          });          
        
          tableOrderDetails.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                try {
                    int[] selectedRows = tableOrderDetails.getSelectedRows();
                    if (selectedRows.length > 0) {
                        // Kiểm tra cùng Order.No
                        String firstOrderNo = tableOrderDetails.getValueAt(selectedRows[0], 0).toString();

                        for (int row : selectedRows) {
                            String currentOrderNo = tableOrderDetails.getValueAt(row, 0).toString();
                            if (!currentOrderNo.equals(firstOrderNo)) {
                                throw new Exception("All selected items must have the same Order.No!");
                            }

                    // IMEI logic removed
                        }
                    }
                } catch (Exception ex) {
                    CustomDialog.showError(ex.getMessage());
                    // IMEI input removed
                }
            }
        });
           
              
           
        // ========== Tạo TabbedPane chứa 2 bảng: Orders và Export Bills ==========
        tablesTabbedPane = new JTabbedPane();
        tablesTabbedPane.setFont(FONT_TITLE_MEDIUM);
        tablesTabbedPane.setBounds(20, 200, 790, 450);

        // Tab Orders
        JPanel ordersTabPanel = new JPanel(new BorderLayout());
        ordersTabPanel.setBackground(Color.WHITE);
        ordersTabPanel.add(scrollPaneOrders, BorderLayout.CENTER);
        tablesTabbedPane.addTab("Orders", ordersTabPanel);

        // Tab Export Bills
        JPanel exportBillsTabPanel = new JPanel(new BorderLayout());
        exportBillsTabPanel.setBackground(Color.WHITE);

        String[] exportCols = {"Invoice No", "Admin ID", "Customer ID", "Order No", "Total Products", "Description"};
        modelExportBills = new DefaultTableModel(exportCols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        tableExportBills = createStyledTable(modelExportBills);
        tableExportBills.setRowHeight(30);
        JScrollPane scrollPaneExport = new JScrollPane(tableExportBills);
        scrollPaneExport.setBorder(null);
        exportBillsTabPanel.add(scrollPaneExport, BorderLayout.CENTER);
        tablesTabbedPane.addTab("Export Bills", exportBillsTabPanel);

        panel.add(tablesTabbedPane);

        // Load dữ liệu cho tab Export Bills
        SwingUtilities.invokeLater(() -> {
            loadExportBillsData();
        });
        
        // Add tab change listener để refresh data khi chuyển tab
        tablesTabbedPane.addChangeListener(e -> {
            if (tablesTabbedPane.getSelectedIndex() == 1) {
                // Tab Export Bills được chọn, refresh data
                loadExportBillsData();
            }
        });
        
        
        

        bntAddBill = new MyButton("Add Bill", 20);
        stylePrimaryButton(bntAddBill);
        bntAddBill.setBounds(20, 670, 110, 35);

        // Hàm xử lý khi nhấn nút Add Bill
        bntAddBill.addActionListener(e -> {
            try {
                busExportBill = new BUS_ExportBill();

                // 1. Lấy các dòng được chọn (kiểm tra cùng Order.No)
                List<Object[]> orderItems = getMultipleOrderInfo();
                if (orderItems.isEmpty()) {
                    throw new Exception("No order items selected!");
                }

        // 2. Lấy thông tin chung
        String customerID = orderItems.get(0)[1].toString();
        String orderNo = orderItems.get(0)[0].toString();
        Form_Export.orderNo = orderNo; // Lưu orderNo vào biến static để sử dụng sau
        
        // Lấy promotion code từ order (do user chọn khi đặt hàng)
        currentPromotion = null;
        double discount = 0.0; // Default discount
        try {
            if (busPromotion == null) {
                busPromotion = new BUSPromotion();
            }
            com.User.order.BUS.BUS_Order busOrder = new com.User.order.BUS.BUS_Order();
            String promotionCode = busOrder.getPromotionCodeByOrderNo(orderNo);
            if (promotionCode != null && !promotionCode.trim().isEmpty()) {
                currentPromotion = busPromotion.findActivePromotion(promotionCode);
                if (currentPromotion != null) {
                    discount = currentPromotion.getDiscountPercent().doubleValue();
                }
            }
            // Cập nhật hiển thị promotion info
            updatePromotionInfoFromOrder(orderNo);
        } catch (Exception ex) {
            System.err.println("Error loading promotion from order: " + ex.getMessage());
        }
        
        String imeiNumbers = ""; // IMEI removed
        customer = busExportBill.getCustomerInfoSafe(customerID);

                // 3. Xóa nội dung cũ trước khi tạo hóa đơn mới
                billBody = getBillBody();
                if (billBody != null) {
                    billBody.removeAll();
                }

                // 4. Tạo hóa đơn mới
                createBillContent(orderItems, discount, imeiNumbers, customer);

                // 5. Cập nhật lại giao diện
                billBody.revalidate();
                billBody.repaint();

            } catch (Exception ex) {
                CustomDialog.showError(ex.getMessage());
                ex.printStackTrace(); // In lỗi ra console để debug
            }
        });


        panel.add(bntAddBill);
        
        bntExport = new MyButton("Generate/Save Bill", 20);
        stylePrimaryButton(bntExport);
        bntExport.setFont(FONT_BUTTON_LARGE);
        bntExport.setBounds(950, 670, 200, 45);
        bntExport.addActionListener(e -> {
            boolean confirm = CustomDialog.showOptionPane(
                "Confirm Exportation",
                "Are you sure want to export bill ?",
                UIManager.getIcon("OptionPane.questionIcon"),
                Color.decode("#FF6666")
            );

            if (confirm) {
                try {
                    confirmExport();
                     OrderUpdateNotifier.notifyOrderDeleted(customer.getCustomerID(), orderNo);
                    Refresh();
                } catch (Exception ex) {
                    CustomDialog.showError("Export error: " + ex.getMessage());
                    ex.printStackTrace();
                }
            }
    });
            
        panel.add(bntExport);  
  }
    
    private void loadConfirmedOrderDetailsToTable() {
        busOrderDetail = new BUS_OrderDetail();
        List<DTO_Oderdetails> orderDetailsList = busOrderDetail.getConfirmedOrderDetailsOldestFirst();
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        model.setRowCount(0); // Xóa dữ liệu cũ

        for (DTO_Oderdetails detail : orderDetailsList) {
            Object[] row = {
                detail.getOrderNo(),
                detail.getCustomerID(),
                detail.getProductID(),
                detail.getPrice(),
                detail.getQuantity(),
                detail.getDateOrder().format(dateFormatter),
                detail.getTimeOrder().toString(),
                detail.getStatus()
            };
            model.addRow(row);
        }
   }
   
   /**
    * Load dữ liệu cho Order cụ thể được chọn từ Order form
    */
   private void loadSpecificOrderData(String orderNo, String customerID) {
       try {
           busOrderDetail = new BUS_OrderDetail();
           List<DTO_Oderdetails> specificOrderDetails = busOrderDetail.getOrderDetailsByOrderNo(orderNo);
           
           if (specificOrderDetails.isEmpty()) {
               CustomDialog.showError("No confirmed order details found for Order: " + orderNo);
               return;
           }
           
           DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
           model.setRowCount(0); // Xóa dữ liệu cũ
           
           for (DTO_Oderdetails detail : specificOrderDetails) {
               Object[] row = {
                   detail.getOrderNo(),
                   detail.getCustomerID(),
                   detail.getProductID(),
                   detail.getPrice(),
                   detail.getQuantity(),
                   detail.getDateOrder().format(dateFormatter),
                   detail.getTimeOrder().toString(),
                   detail.getStatus()
               };
               model.addRow(row);
           }
           
           // Tự động chọn tất cả các dòng của Order này
           tableOrderDetails.selectAll();
           
           CustomDialog.showSuccess("Loaded order details for Order: " + orderNo);
           
       } catch (Exception e) {
           e.printStackTrace();
           CustomDialog.showError("Error loading specific order data: " + e.getMessage());
       }
   }

    private void displaySearchResults(List<DTO_Oderdetails> results) {
        model = (DefaultTableModel) tableOrderDetails.getModel();
        model.setRowCount(0); // Xóa dữ liệu cũ

        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        for (DTO_Oderdetails detail : results) {
            Object[] rowData = {
                detail.getOrderNo(),
                detail.getCustomerID(),
                detail.getProductID(),
                detail.getPrice(),
                detail.getQuantity(),
                detail.getDateOrder().format(dateFormatter),
                detail.getTimeOrder().toString(),
                detail.getStatus()
            };
            model.addRow(rowData);
        }

        if (model.getRowCount() == 0) {
            return;
        }
     }
    
    public void Refresh(){
       SwingUtilities.invokeLater(() -> {
              loadConfirmedOrderDetailsToTable();
               tableOrderDetails.adjustColumnWidths();         // Căn chỉnh cột
          });     
        cmbSearchOrder.setSelectedIndex(0);
        txtSearchOrder.setText(""); // Clear search text
        // Clear promotion info
        currentPromotion = null;
        lblPromotionInfo.setText("No promotion code selected");
        // IMEI cleared removed
        billBody.removeAll();
        billBody.revalidate();
        billBody.repaint();
        // Refresh Export Bills table
        loadExportBillsData();

    }
    
    /**
     * Update promotion info display from order
     */
    private void updatePromotionInfoFromOrder(String orderNo) {
        try {
            if (busPromotion == null) {
                busPromotion = new BUSPromotion();
            }
            
            com.User.order.BUS.BUS_Order busOrder = new com.User.order.BUS.BUS_Order();
            String promotionCode = busOrder.getPromotionCodeByOrderNo(orderNo);
            
            if (promotionCode != null && !promotionCode.trim().isEmpty()) {
                currentPromotion = busPromotion.findActivePromotion(promotionCode);
                if (currentPromotion != null) {
                    String infoText = String.format("✅ %s (%s) - %.1f%% off", 
                        currentPromotion.getPromotionCode(),
                        currentPromotion.getPromotionName(),
                        currentPromotion.getDiscountPercent().doubleValue());
                    lblPromotionInfo.setText(infoText);
                    lblPromotionInfo.setForeground(Color.decode("#28a745"));
                } else {
                    lblPromotionInfo.setText("❌ Promotion code: " + promotionCode + " (not found or expired)");
                    lblPromotionInfo.setForeground(Color.decode("#dc3545"));
                    currentPromotion = null;
                }
            } else {
                lblPromotionInfo.setText("No promotion code selected");
                lblPromotionInfo.setForeground(Color.decode("#666666"));
                currentPromotion = null;
            }
        } catch (Exception e) {
            lblPromotionInfo.setText("Error loading promotion: " + e.getMessage());
            lblPromotionInfo.setForeground(Color.decode("#dc3545"));
            currentPromotion = null;
        }
    }
    
    // Hàm tạo separator
    private JLabel createSeparator() {
        JLabel separator = new JLabel("===================================================");
        separator.setForeground(Color.GRAY);
        separator.setAlignmentX(Component.CENTER_ALIGNMENT);
        return separator;
    }

    // Hàm thêm dòng thông tin
    private void addInfoRow(JPanel panel, String label, String value) {
        JPanel rowPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        rowPanel.setBackground(Color.WHITE);
        
        JLabel lblLabel = new JLabel(label);
        lblLabel.setFont(new Font("Arial", Font.BOLD, 12));
        
        JLabel lblValue = new JLabel(value);
        lblValue.setFont(new Font("Arial", Font.PLAIN, 12));
        
        rowPanel.add(lblLabel);
        rowPanel.add(lblValue);
        panel.add(rowPanel);
    }


    // Hàm lấy billBody an toàn (phiên bản mới phù hợp với cấu trúc của bạn)
        private JPanel getBillBody() {
            // Kiểm tra cấu trúc panel theo đúng cách bạn đã thiết kế
            if (panelBill.getComponentCount() > 0) {
                // Lấy component CENTER (index 1 nếu có cả NORTH và CENTER)
                if (panelBill.getComponentCount() > 1) {
                    Component centerComp = panelBill.getComponent(1);
                    if (centerComp instanceof JPanel) {
                        JPanel billContent = (JPanel) centerComp;
                        if (billContent.getComponentCount() > 0) {
                            Component scrollComp = billContent.getComponent(0);
                            if (scrollComp instanceof JScrollPane) {
                                JScrollPane scrollPane = (JScrollPane) scrollComp;
                                return (JPanel) scrollPane.getViewport().getView();
                            }
                        }
                    }
                }
            }

            // Fallback: tạo mới nếu không tìm thấy (đảm bảo không bao giờ null)
            JPanel newBody = new JPanel();
            newBody.setLayout(new BoxLayout(newBody, BoxLayout.Y_AXIS));
            newBody.setBackground(Color.WHITE);

            // Tạo lại cấu trúc scroll pane nếu cần
            JScrollPane scrollPane = new JScrollPane(newBody);
            scrollPane.setBorder(null);

            // Tạo lại cấu trúc billContent
            JPanel billContent = new JPanel(new BorderLayout());
            billContent.add(scrollPane, BorderLayout.CENTER);

            // Cập nhật lại panelBill
            panelBill.removeAll();
            panelBill.add(panelTitle, BorderLayout.NORTH);
            panelBill.add(billContent, BorderLayout.CENTER);
            panelBill.revalidate();

            return newBody;
         }
    private void createBillContent(List<Object[]> orderItems, double discount, String imeiNumbers, DTOProfile_cus customer) {
        billBody = getBillBody();
        if (billBody == null) {
            CustomDialog.showError("Bill display area not found!");
            return;
        }

        billBody.removeAll();
        billBody.setLayout(new BoxLayout(billBody, BoxLayout.Y_AXIS));
        billBody.setBackground(Color.WHITE);

        // ===== 0. Invoice No =====
        invoiceNo = String.format("%010d", new Random().nextInt(1_000_000_000)) + "-" + customer.getCustomerID();
        lblInvoice = new JLabel("Invoice No: " + invoiceNo, SwingConstants.CENTER);
        lblInvoice.setFont(new Font("Arial", Font.BOLD, 16));
        lblInvoice.setAlignmentX(Component.CENTER_ALIGNMENT);
        billBody.add(lblInvoice);
        billBody.add(Box.createVerticalStrut(15));

        // ===== 1. Admin Information =====
        JPanel adminPanel = createSectionPanel("ADMIN INFORMATION");
        addInfoRow(adminPanel, "Admin ID:", txtAdminID.getText());
        addInfoRow(adminPanel, "Admin Name:", txtAdminName.getText());
        billBody.add(adminPanel);
        billBody.add(createSeparator());
        billBody.add(Box.createVerticalStrut(10));

        // ===== 2. Customer Information =====
        JPanel customerPanel = createSectionPanel("CUSTOMER INFORMATION");
        addInfoRow(customerPanel, "Customer ID:", customer.getCustomerID());
        addInfoRow(customerPanel, "Customer Name:", customer.getFullName());
        addInfoRow(customerPanel, "Address:", customer.getAddress());
        addInfoRow(customerPanel, "Contact:", customer.getContact());
        billBody.add(customerPanel);
        billBody.add(createSeparator());
        billBody.add(Box.createVerticalStrut(10));

        // ===== 3. Order Details =====
        JPanel orderPanel = createSectionPanel("ORDER DETAILS");
        // Always get Order_No from orderItems (from Orders table)
        String orderNoToUse = orderItems.get(0)[0].toString();
        addInfoRow(orderPanel, "Order No:", orderNoToUse);

        String[] columns = {"No.", "Product ID", "Product Name", "Quantity", "Unit Price", "Discount", "Total Price", "Waranty Period"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        // Tính tổng tiền TRƯỚC discount
        BigDecimal totalBeforeDiscount = BigDecimal.ZERO;
        int totalProducts = 0;
        int stt = 1;

        for (Object[] item : orderItems) {
            String productID = item[2].toString();
            BigDecimal unitPrice = (BigDecimal) item[3];
            int quantity = (int) item[4];

            // Tính tổng tiền trước discount (chỉ để hiển thị trong bảng)
            BigDecimal itemTotalBeforeDiscount = unitPrice.multiply(BigDecimal.valueOf(quantity));
            totalBeforeDiscount = totalBeforeDiscount.add(itemTotalBeforeDiscount);
            totalProducts += quantity;
            busExportBill= new BUS_ExportBill();
            String warranty= busExportBill.getWarranty(productID);
            
            // Hiển thị giá gốc trong bảng (không discount ở đây)
            model.addRow(new Object[]{
                stt++,
                productID,
                busOrderDetail.getProductName(productID),
                quantity,
                String.format("%,d VND", unitPrice.intValue()),
                discount + "%",
                String.format("%,d VND", itemTotalBeforeDiscount.intValue()),
                warranty
                     
            });
        }
        
        // Tính discount trên TỔNG tiền (không tính trên từng sản phẩm)
        BigDecimal totalAfterDiscount = totalBeforeDiscount;
        BigDecimal discountAmount = BigDecimal.ZERO;
        if (currentPromotion != null && discount > 0) {
            discountAmount = totalBeforeDiscount.multiply(BigDecimal.valueOf(discount))
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            totalAfterDiscount = totalBeforeDiscount.subtract(discountAmount);
        }

        JTable productTable = new JTable(model);
        productTable.setFont(new Font("Arial", Font.PLAIN, 12));
        productTable.setRowHeight(35);
        productTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        productTable.setFillsViewportHeight(true);

        // Adjust column widths
        TableColumnModel columnModel = productTable.getColumnModel();
        columnModel.getColumn(0).setPreferredWidth(50);   // No.
        columnModel.getColumn(1).setPreferredWidth(100);  // Product ID
        columnModel.getColumn(2).setPreferredWidth(320);  // Product Name
        columnModel.getColumn(3).setPreferredWidth(80);   // Quantity
        columnModel.getColumn(4).setPreferredWidth(160);  // Unit Price
        columnModel.getColumn(5).setPreferredWidth(80);   // Discount
        columnModel.getColumn(6).setPreferredWidth(160);  // Total Price
        columnModel.getColumn(7).setPreferredWidth(200);

        JScrollPane tableScroll = new JScrollPane(productTable);
        tableScroll.setPreferredSize(new Dimension(710, Math.min(orderItems.size() * 30 + 50, 300)));
        orderPanel.add(tableScroll);

        billBody.add(orderPanel);
        billBody.add(createSeparator());
        billBody.add(Box.createVerticalStrut(15));

        // ===== 4. Promotion Information =====
        if (currentPromotion != null) {
            JPanel promoPanel = createSectionPanel("PROMOTION INFORMATION");
            addInfoRow(promoPanel, "Promotion Code:", currentPromotion.getPromotionCode());
            addInfoRow(promoPanel, "Promotion Name:", currentPromotion.getPromotionName());
            addInfoRow(promoPanel, "Discount:", String.format("%.1f%%", currentPromotion.getDiscountPercent().doubleValue()));
            billBody.add(promoPanel);
            billBody.add(createSeparator());
            billBody.add(Box.createVerticalStrut(10));
        }

        // ===== 5. Order Summary =====
        JPanel summaryPanel = createSectionPanel("ORDER SUMMARY");
        addInfoRow(summaryPanel, "Total Products:", String.valueOf(totalProducts));
        addInfoRow(summaryPanel, "Subtotal (before discount):", String.format("%,d VND", totalBeforeDiscount.intValue()));
        
        if (currentPromotion != null && discount > 0) {
            addInfoRow(summaryPanel, "Discount (" + String.format("%.1f%%", discount) + "):", "-" + String.format("%,d VND", discountAmount.intValue()));
        }
        addInfoRow(summaryPanel, "Subtotal (after discount):", String.format("%,d VND", totalAfterDiscount.intValue()));
        
        // ===== 6. VAT Calculation =====
        // VAT tính trên giá sau chiết khấu
        BigDecimal vatPercent = BigDecimal.valueOf(8.00); // VAT 8%
        BigDecimal vatAmount = totalAfterDiscount.multiply(vatPercent).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        BigDecimal totalWithVAT = totalAfterDiscount.add(vatAmount);
        
        addInfoRow(summaryPanel, "VAT (" + vatPercent + "% after discount):", String.format("%,d VND", vatAmount.intValue()));
        addInfoRow(summaryPanel, "Total Amount (incl. VAT):", String.format("%,d VND", totalWithVAT.intValue()));
        
        billBody.add(summaryPanel);
        billBody.add(createSeparator());
        billBody.add(Box.createVerticalStrut(15));

        // IMEI section removed

        billBody.revalidate();
        billBody.repaint();
    }

    public JPanel createSectionPanel(String title) {
        JPanel panelcreate = new JPanel();
        panelcreate.setLayout(new BoxLayout(panelcreate, BoxLayout.Y_AXIS));
        panelcreate.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panelcreate.setBackground(Color.WHITE);

        JLabel titleLabel = new JLabel(title, SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 14));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JSeparator separator = new JSeparator();
        separator.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));

        panelcreate.add(titleLabel);
        panelcreate.add(Box.createVerticalStrut(5));
        panelcreate.add(separator);
        panelcreate.add(Box.createVerticalStrut(5));

        return panelcreate;
    }

    private List<Object[]> getMultipleOrderInfo() throws Exception {
        int[] selectedRows = tableOrderDetails.getSelectedRows();
        if (selectedRows.length == 0) {
            throw new Exception("Please select at least one order!");
        }

        // Kiểm tra cùng Order.No
        String firstOrderNo = tableOrderDetails.getValueAt(selectedRows[0], 0).toString();
        List<Object[]> orderInfos = new ArrayList<>();

        for (int row : selectedRows) {
            String currentOrderNo = tableOrderDetails.getValueAt(row, 0).toString();
            if (!currentOrderNo.equals(firstOrderNo)) {
                throw new Exception("All selected orders must have the same Order.No!");
            }

            orderInfos.add(new Object[]{
                currentOrderNo,
                tableOrderDetails.getValueAt(row, 1).toString(),
                tableOrderDetails.getValueAt(row, 2).toString(),
                new BigDecimal(tableOrderDetails.getValueAt(row, 3).toString()),
                Integer.parseInt(tableOrderDetails.getValueAt(row, 4).toString())
            });
        }

        return orderInfos;
    }

        // Hàm thêm phần IMEI vào hóa đơn
    // IMEI helper removed
  
    private void confirmExport() throws Exception {
        // Lấy danh sách sản phẩm và IMEI
        List<Object[]> orderItems = getMultipleOrderInfo();
        List<String> imeis = Collections.emptyList();

        if (orderItems.isEmpty()) {
            throw new Exception("No products to export!");
        }
        
        // Đảm bảo promotion code được lấy lại từ order trước khi export
        String orderNoToUse = orderItems.get(0)[0].toString();
        if (currentPromotion == null || orderNo == null || !orderNo.equals(orderNoToUse)) {
            // Lấy lại promotion code từ order
            try {
                if (busPromotion == null) {
                    busPromotion = new BUSPromotion();
                }
                com.User.order.BUS.BUS_Order busOrder = new com.User.order.BUS.BUS_Order();
                String promotionCode = busOrder.getPromotionCodeByOrderNo(orderNoToUse);
                if (promotionCode != null && !promotionCode.trim().isEmpty()) {
                    currentPromotion = busPromotion.findActivePromotion(promotionCode);
                } else {
                    currentPromotion = null;
                }
            } catch (Exception ex) {
                System.err.println("Error loading promotion from order in confirmExport: " + ex.getMessage());
                currentPromotion = null;
            }
        }

        // 🔹 Tạo hộp thoại chờ nhưng KHÔNG hiển thị ngay lập tức
        JDialog loadingDialog = new JDialog((Frame) null, "Processing", true);
        loadingDialog.setUndecorated(true);
        loadingDialog.setSize(300, 160);
        loadingDialog.setLayout(null);
        loadingDialog.getContentPane().setBackground(Color.WHITE);

        JLabel messageLabel = new JLabel("Exporting... Please wait for a moment!", SwingConstants.CENTER);
        messageLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        messageLabel.setBounds(20, 60, 260, 40);

        ImageIcon loadingIcon = new ImageIcon("src\\main\\resources\\Icons\\Admin_icon\\loading.gif");
        JLabel iconLabel = new JLabel(loadingIcon);
        iconLabel.setBounds(125, 100, 50, 50);

        loadingDialog.add(messageLabel);
        loadingDialog.add(iconLabel);
        loadingDialog.setLocationRelativeTo(null);

        // 🔹 Hiển thị hộp thoại trên luồng UI
        SwingUtilities.invokeLater(() -> loadingDialog.setVisible(true));

        // 🔹 Bắt đầu tiến trình nền
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() {
                try {
                    // Kiểm tra email trước khi gửi
                    final boolean emailSent;
                    if (customer != null && customer.getEmail() != null && !customer.getEmail().trim().isEmpty()) {
                        // Thử gửi email
                        emailSent = SendEmail.sendInvoiceEmail(
                            customer, 
                            orderItems, 
                            0.0, // No discount spinner, use promotion code instead
                            invoiceNo
                        );
                    } else {
                        emailSent = false;
                    }

                    // Xuất PDF (luôn thực hiện)
                    String promotionCode = currentPromotion != null ? currentPromotion.getPromotionCode() : "";
                    double pdfDiscount = currentPromotion != null ? currentPromotion.getDiscountPercent().doubleValue() : 0.0;
                    PDFExporter exporter = new PDFExporter(
                        panelBill, txtAdminID.getText(), txtAdminName.getText(),
                        customer, busOrderDetail, orderItems,
                        pdfDiscount, // Use calculated discount from promotion
                        promotionCode,
                        invoiceNo // Pass the invoice number
                    );
                    exporter.exportToPDF();

                    // Xử lý dữ liệu (luôn thực hiện)
                    processExportData(orderItems, imeis);
                    Refresh();
                    
                    // Hiển thị thông báo kết quả
                    SwingUtilities.invokeLater(() -> {
                        if (emailSent) {
                            CustomDialog.showSuccess("Export completed successfully!\nInvoice sent to customer email: " + customer.getEmail());
                        } else {
                            CustomDialog.showSuccess("Export completed successfully!\nNote: Invoice was not sent via email (customer email not available)");
                        }
                    });
                    
                } catch (Exception e) {
                    e.printStackTrace();
                    SwingUtilities.invokeLater(() -> {
                        CustomDialog.showError("Export failed: " + e.getMessage());
                    });
                }
                return null;
            }

            @Override
            protected void done() {
                // 🔹 Đóng hộp thoại loading trên luồng UI
                SwingUtilities.invokeLater(() -> loadingDialog.dispose());
            }
        };

        worker.execute();
    }



    private void processExportData(List<Object[]> orderItems, List<String> imeis) throws Exception {
        // Tính tổng tiền TRƯỚC discount
        BigDecimal totalBeforeDiscount = BigDecimal.ZERO;
        
        // Always get Order_No from orderItems (from Orders table)
        String orderNoToUse = orderItems.get(0)[0].toString();
        
        // Lấy promotion code TRỰC TIẾP từ order để đảm bảo không bị mất
        String promotionCode = null;
        BigDecimal discountPercent = BigDecimal.valueOf(0.0);
        
        try {
            com.User.order.BUS.BUS_Order busOrder = new com.User.order.BUS.BUS_Order();
            promotionCode = busOrder.getPromotionCodeByOrderNo(orderNoToUse);
            
            // Nếu có promotion code, lấy thông tin promotion
            if (promotionCode != null && !promotionCode.trim().isEmpty()) {
                if (busPromotion == null) {
                    busPromotion = new BUSPromotion();
                }
                currentPromotion = busPromotion.findActivePromotion(promotionCode);
                if (currentPromotion != null) {
                    discountPercent = currentPromotion.getDiscountPercent();
                } else {
                    // Nếu promotion không active, vẫn giữ promotion code nhưng discount = 0
                    System.err.println("Warning: Promotion code " + promotionCode + " not found or expired, but keeping code in bill.");
                }
            }
        } catch (Exception ex) {
            System.err.println("Error loading promotion from order in processExportData: " + ex.getMessage());
            // Nếu có lỗi, vẫn tiếp tục với promotion code từ order (nếu có)
        }
        
        // Calculate subtotal BEFORE discount
        for (Object[] item : orderItems) {
            BigDecimal unitPrice = (BigDecimal) item[3];
            int quantity = ((Number) item[4]).intValue();
            BigDecimal itemTotal = unitPrice.multiply(BigDecimal.valueOf(quantity));
            totalBeforeDiscount = totalBeforeDiscount.add(itemTotal);
        }
        
        // Tính discount trên TỔNG tiền
        BigDecimal totalAfterDiscount = totalBeforeDiscount;
        if (discountPercent.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal discountAmount = totalBeforeDiscount.multiply(discountPercent)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            totalAfterDiscount = totalBeforeDiscount.subtract(discountAmount);
        }
        
        // VAT tính trên tổng sau giảm giá
        BigDecimal vatPercent = BigDecimal.valueOf(8.00); // VAT 8%
        BigDecimal vatAmount = totalAfterDiscount.multiply(vatPercent).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        BigDecimal totalWithVAT = totalAfterDiscount.add(vatAmount);
        
        // 1. Insert Bill Exported with promotion code and VAT
        DTO_BillExported bill = new DTO_BillExported();
        bill.setInvoiceNo(invoiceNo);
        bill.setAdminId(txtAdminID.getText());
        bill.setCustomerId(customer.getCustomerID());
        bill.setOrderNo(orderNoToUse);
        // Calculate total quantity of all products, not just number of rows
        int totalQuantity = 0;
        for (Object[] item : orderItems) {
            totalQuantity += ((Number) item[4]).intValue(); // item[4] is quantity
        }
        bill.setTotalProduct(totalQuantity);
        bill.setVatPercent(vatPercent);
        bill.setVatAmount(vatAmount);
        bill.setTotalAmount(totalWithVAT);
        // Set promotion code vào DTO để đảm bảo
        bill.setPromotionCode(promotionCode);

        // Debug: In ra để kiểm tra
        System.out.println("=== DEBUG: Inserting Bill Exported ===");
        System.out.println("Order No: " + orderNoToUse);
        System.out.println("Promotion Code: " + promotionCode);
        System.out.println("Discount Percent: " + discountPercent);
        
        if (!busExportBill.insertBillExported(bill, promotionCode)) {
            throw new Exception("Failed to insert exported bill!");
        }
        
        System.out.println("=== Bill Exported inserted successfully ===");

        // Get the discount value from promotion (already calculated above)

        for (Object[] item : orderItems) {
            String productID = item[2].toString();
            int quantity = ((Number) item[4]).intValue();
            BigDecimal unitPrice = (BigDecimal) item[3];

            // Xử lý theo tổng số lượng của sản phẩm, không chia nhỏ
            processProductItem(
                item,
                discountPercent,
                unitPrice,
                productID,
                quantity,
                promotionCode
            );
        }

        cleanupAfterExport(imeis, orderNoToUse);
    }

    private void processProductItem(Object[] item, BigDecimal discountPercent, 
                                   BigDecimal unitPrice, String productID, int quantity, String promotionCode) throws Exception {
        // Calculate total before discount (không áp dụng discount ở đây vì đã tính trên tổng)
        BigDecimal totalBefore = unitPrice.multiply(BigDecimal.valueOf(quantity));
        // Tính discount cho item này theo tỷ lệ (để hiển thị trong detail)
        BigDecimal discountAmount = totalBefore.multiply(discountPercent)
                                               .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        BigDecimal totalAfter = totalBefore.subtract(discountAmount);

        // Create export detail với tổng số lượng
        DTO_BillExportedDetail detail = createExportDetail(
            productID, 
            null, // No IMEI
            unitPrice, 
            quantity, // Tổng số lượng thực tế
            discountPercent,
            totalBefore,
            totalAfter,
            promotionCode
        );

        if (!busExportBill.insertBillDetail(detail, Collections.emptyList())) {
            throw new Exception("Không thể xử lý sản phẩm: " + productID + " với số lượng: " + quantity);
        }
    }

    private void loadExportBillsData() {
        try {
            if (busExportBill == null) busExportBill = new BUS_ExportBill();
            List<DTO_BillExport> billExports = busExportBill.getAllBillExported();
            
            if (billExports == null) {
                System.err.println("Warning: getAllBillExported() returned null");
                modelExportBills.setRowCount(0); // Clear table
                return;
            }
            
            // Clear existing rows
            modelExportBills.setRowCount(0);
            
            if (billExports.isEmpty()) {
                System.out.println("Info: No export bills found in database");
                // Vẫn cần refresh table để hiển thị trạng thái rỗng
                if (tableExportBills != null) {
                    tableExportBills.revalidate();
                    tableExportBills.repaint();
                }
                return;
            }
            
            // Add rows to model
            for (DTO_BillExport bill : billExports) {
                Object[] row = {
                    bill.getInvoiceNo(),
                    bill.getAdminId(),
                    bill.getCustomerId(),
                    bill.getOrderNo() != null ? bill.getOrderNo() : "N/A",
                    bill.getTotalProduct(),
                    bill.getDescription() != null ? bill.getDescription() : "N/A"
                };
                modelExportBills.addRow(row);
            }
            
            // Refresh table display
            if (tableExportBills != null) {
                tableExportBills.adjustColumnWidths();
                tableExportBills.revalidate();
                tableExportBills.repaint();
                System.out.println("Successfully loaded " + billExports.size() + " export bills");
            } else {
                System.err.println("Warning: tableExportBills is null when loading data");
            }
        } catch (Exception ex) {
            ex.printStackTrace(); // In stack trace để debug
            CustomDialog.showError("Error loading export bills: " + ex.getMessage());
            System.err.println("Exception details: " + ex.getClass().getName() + ": " + ex.getMessage());
            // Clear table on error
            if (modelExportBills != null) {
                modelExportBills.setRowCount(0);
            }
        }
    }

    private void showExportBillDetailsDialog(String invoiceNoDialog, String adminIdDialog) {
        try {
            if (busExportBill == null) busExportBill = new BUS_ExportBill();
            List<com.Admin.export.DTO.DTO_BillExportedDetail> details = busExportBill.getBillDetailsByInvoice(invoiceNoDialog);

            JDialog dialog = new JDialog((Frame) null, "Bill Details - " + invoiceNoDialog, true);
            dialog.setSize(900, 540);
            dialog.setLayout(new BorderLayout());
            dialog.setLocationRelativeTo(null);

            String[] colNames = {
                "Invoice No", "Admin ID", "Customer ID", "Product ID",
                "Unit Price", "Quantity", "Discount %", "Total Before",
                "Total After", "Date Exported", "Time Exported"
            };
            DefaultTableModel mdl = new DefaultTableModel(colNames, 0) {
                @Override
                public boolean isCellEditable(int r, int c) { return false; }
            };

            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
            SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
            for (com.Admin.export.DTO.DTO_BillExportedDetail d : details) {
                Object[] row = new Object[] {
                    d.getInvoiceNo(),
                    d.getAdminId(),
                    d.getCustomerId(),
                    d.getProductId(),
                    d.getUnitPrice(),
                    d.getQuantity(),
                    d.getDiscountPercent() + "%",
                    d.getTotalPriceBefore(),
                    d.getTotalPriceAfter(),
                    dateFormat.format(d.getDateExported()),
                    timeFormat.format(d.getTimeExported())
                };
                mdl.addRow(row);
            }

            MyTable detailsTable = createStyledTable(mdl);
            detailsTable.setRowHeight(28);
            JScrollPane sp = new JScrollPane(detailsTable);
            dialog.add(sp, BorderLayout.CENTER);

            JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            MyButton closeBtn = new MyButton("Close", 18);
            styleInfoButton(closeBtn);
            closeBtn.addActionListener(ev -> dialog.dispose());
            footer.add(closeBtn);
            dialog.add(footer, BorderLayout.SOUTH);

            dialog.setVisible(true);
        } catch (Exception ex) {
            CustomDialog.showError("Error loading bill details: " + ex.getMessage());
        }
    }




        private DTO_BillExportedDetail createExportDetail(
            String productID, String imei, BigDecimal unitPriceBefore, int quantity,
            BigDecimal discountPercent, BigDecimal totalBefore, BigDecimal totalAfter, String promotionCode
        ) {
            // Tính đơn giá sau khuyến mãi trên mỗi đơn vị
            BigDecimal unitPriceAfter = unitPriceBefore.subtract(
                unitPriceBefore.multiply(discountPercent).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP)
            );

            return new DTO_BillExportedDetail(
                invoiceNo,
                txtAdminID.getText(),
                customer.getCustomerID(),
                productID,
                unitPriceBefore,
                unitPriceAfter,
                quantity,
                discountPercent,
                totalBefore,
                totalAfter,
                new java.sql.Date(System.currentTimeMillis()),
                new java.sql.Time(System.currentTimeMillis()),
                promotionCode,
                new java.sql.Date(System.currentTimeMillis()), // Start_Date
                new java.sql.Date(System.currentTimeMillis())  // End_Date (will be calculated by BUS layer)
            );
        }

    private void cleanupAfterExport(List<String> imeis, String orderNo) {
        // Xóa order
        busOrderDetail.deleteOrder(orderNo);
        
        // REMOVED: Không gọi fixQuantityIssues ở đây để tránh trùng lặp với trigger
        // Trigger trg_BED_AI_Stock đã tự động cập nhật số lượng
        
        CustomDialog.showSuccess("Export bill and update database successfully!");
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
    
    private void styleWarningButton(MyButton btn) {
        btn.setBackgroundColor(Color.decode("#FF9800"));
        btn.setHoverColor(Color.decode("#F57C00"));
        btn.setPressedColor(Color.decode("#EF6C00"));
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

}
