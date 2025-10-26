package com.Admin.order.GUI;

import javax.swing.JPanel;
import javax.swing.JLabel;
import java.awt.Color;
import java.awt.Font;
import java.awt.BorderLayout;
import javax.swing.SwingConstants;
import java.awt.Dimension;
import javax.swing.*;
import com.ComponentandDatabase.Components.MyTable;
import com.ComponentandDatabase.Components.MyCombobox;
import com.ComponentandDatabase.Components.MyTextField;
import com.ComponentandDatabase.Components.MyPanel;
import com.ComponentandDatabase.Components.MyButton;
import com.ComponentandDatabase.Components.CustomDialog;
import com.Admin.order.DTO.DTO_order;
import com.Admin.order.BUS.BUS_order;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JFileChooser;
import java.time.format.DateTimeFormatter;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.File;
import java.util.List;
import javax.swing.table.DefaultTableModel;
import java.awt.FlowLayout;
import static com.ComponentandDatabase.Components.UIConstants.*;

public class Form_Order extends JPanel {
    private JPanel panel, panelSearch;;
    private JLabel lblStatus; 
    private MyButton bntSearch, bntExportFile, bntDetails, bntRefresh, bntUpdate, bntViewConfirmed;
    private MyTextField txtSearch;
    private MyCombobox<String> cmbSearch, cmbStatus;
    private MyTable tableOrder;
    private DefaultTableModel model;
    private BUS_order busOrder;
    public static String orderNo, customerID;
    
    public Form_Order() {
        initComponents();
        init();
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(1200, 700)); // Giảm kích thước cho màn hình nhỏ
        setBackground(Color.WHITE);
    }

    private void init() {
        // Tạo main panel với scroll
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(null);
        mainPanel.setPreferredSize(new Dimension(1200, 900)); // Kích thước lớn hơn để scroll
        mainPanel.setBackground(Color.WHITE);
        
        // Tạo scroll pane
        JScrollPane scrollPane = new JScrollPane(mainPanel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setBorder(null);
        
        add(scrollPane, BorderLayout.CENTER);
        panel = mainPanel; // Gán panel để sử dụng trong các method khác
        
        // Title
        JLabel lblTitle = new JLabel("MANAGE ORDER");
        lblTitle.setFont(FONT_TITLE_LARGE);
        lblTitle.setForeground(PRIMARY_COLOR);
        lblTitle.setBounds(20, 10, 400, 40);
        panel.add(lblTitle);
        
        // Tạo panelSearch với màu nền trắng
        panelSearch = new MyPanel(Color.WHITE);
        panelSearch.setLayout(null);
        panelSearch.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(PRIMARY_COLOR, 2),
            "Search",
            0, 0,
            FONT_TITLE_SMALL,
            PRIMARY_COLOR
        ));
        panelSearch.setBounds(20, 60, 1160, 100);
            
  
        // ComboBox search
        String[] items = {"Order.No", "Customer.ID", "Date Order"};
        cmbSearch = new MyCombobox<>(items);
        cmbSearch.setBounds(20, 30, 150, 35);
        cmbSearch.setCustomFont(FONT_CONTENT_MEDIUM);
        cmbSearch.setCustomColors(Color.WHITE, Color.GRAY, Color.BLACK);
        panelSearch.add(cmbSearch);
        
        // TextField search - THÊM VIỀN
        txtSearch = new MyTextField();
        txtSearch.setHint("Search something...");
        txtSearch.setBounds(180, 30, 300, 35);
        txtSearch.setTextFont(FONT_CONTENT_MEDIUM);
        txtSearch.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(PRIMARY_COLOR, 1),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        panelSearch.add(txtSearch);
            
        // Button Search trong panelSearch
        bntSearch = new MyButton("Search", 20);
        bntSearch.setBackgroundColor(PRIMARY_COLOR);
        bntSearch.setHoverColor(PRIMARY_HOVER);
        bntSearch.setPressedColor(PRIMARY_HOVER.darker());
        bntSearch.setFont(FONT_BUTTON_MEDIUM);
        bntSearch.setForeground(Color.WHITE);
        bntSearch.setBounds(500, 30, 120, 35);
        bntSearch.setButtonIcon("src\\main\\resources\\Icons\\Admin_icon\\search.png", 25, 25, 5, SwingConstants.RIGHT, SwingConstants.CENTER);
        bntSearch.addActionListener(e -> {
            String searchType = (String) cmbSearch.getSelectedItem();
            String keyword = txtSearch.getText().trim();
            String status = (String) cmbStatus.getSelectedItem();

            List<DTO_order> searchResults = busOrder.searchOrders(searchType, keyword, status);
            loadOrderDataToTable(searchResults);

            if (searchResults.isEmpty()) {
                CustomDialog.showError("No orders found with the given criteria");
            }
        });
        panelSearch.add(bntSearch);
        
        // Status dropdown - đặt ngang hàng với search box
        lblStatus = new JLabel("Status");
        lblStatus.setFont(new Font("Arial", Font.PLAIN, 16));
        lblStatus.setForeground(Color.BLACK);
        lblStatus.setBounds(650, 5, 100, 35);
        panelSearch.add(lblStatus);

        String[] items_status = {"Waiting", "Confirmed", "Cancelled"};
        cmbStatus = new MyCombobox<>(items_status);
        cmbStatus.setBounds(650, 30, 110, 35);
        cmbStatus.setCustomFont(new Font("Times New Roman", Font.PLAIN, 15));
        cmbStatus.setCustomColors(Color.WHITE, Color.GRAY, Color.BLACK);
        panelSearch.add(cmbStatus);
        
        panel.add(panelSearch);
          
           
           
        // Sắp xếp lại các button theo hàng ngang, tránh chồng chéo
        // Button Refresh
        bntRefresh = new MyButton("Refresh", 20);
        bntRefresh.setBackgroundColor(INFO_COLOR);
        bntRefresh.setHoverColor(INFO_HOVER);
        bntRefresh.setPressedColor(INFO_HOVER.darker());
        bntRefresh.setFont(FONT_BUTTON_MEDIUM);
        bntRefresh.setForeground(Color.WHITE);
         bntRefresh.setBounds(20, 170, 120, 35); // Giữa search panel và bảng
        bntRefresh.setButtonIcon("src\\main\\resources\\Icons\\Admin_icon\\refresh.png", 25, 25, 10, SwingConstants.RIGHT, SwingConstants.CENTER);
        bntRefresh.addActionListener((e) -> {
            Refresh();
        });
        panel.add(bntRefresh);
        
        
        // Button Details
        bntDetails = new MyButton("Details", 20);
        bntDetails.setBackgroundColor(PRIMARY_COLOR);
        bntDetails.setHoverColor(PRIMARY_HOVER);
        bntDetails.setPressedColor(PRIMARY_HOVER.darker());
        bntDetails.setFont(FONT_BUTTON_MEDIUM);
        bntDetails.setForeground(Color.WHITE);
         bntDetails.setBounds(150, 170, 120, 35); // Giữa search panel và bảng
        bntDetails.setButtonIcon("src\\main\\resources\\Icons\\Admin_icon\\details.png", 25, 25, 5, SwingConstants.RIGHT, SwingConstants.CENTER);
        bntDetails.addActionListener((e) -> {
            showOrderDetails();
        });
        panel.add(bntDetails);
        
        // Button Update
        bntUpdate = new MyButton("Update", 20);
        bntUpdate.setBackgroundColor(WARNING_COLOR);
        bntUpdate.setHoverColor(WARNING_HOVER);
        bntUpdate.setPressedColor(WARNING_HOVER.darker());
        bntUpdate.setFont(FONT_BUTTON_MEDIUM);
        bntUpdate.setForeground(Color.WHITE);
         bntUpdate.setBounds(280, 170, 120, 35); // Giữa search panel và bảng
        bntUpdate.setButtonIcon("src\\main\\resources\\Icons\\Admin_icon\\update.png", 25, 25, 10, SwingConstants.RIGHT, SwingConstants.CENTER);
        bntUpdate.addActionListener((e) -> {
            updateOrderStatus();
        });
        panel.add(bntUpdate);
        
        // Button Export File
        bntExportFile = new MyButton("Export", 20);
        bntExportFile.setBackgroundColor(Color.WHITE);
        bntExportFile.setPressedColor(Color.decode("#D3D3D3"));
        bntExportFile.setHoverColor(Color.decode("#EEEEEE"));
         bntExportFile.setBounds( 410, 170, 120, 35); // Giữa search panel và bảng
        bntExportFile.setFont(new Font("sansserif", Font.BOLD, 14));
        bntExportFile.setForeground(Color.BLACK);
        bntExportFile.setButtonIcon("src\\main\\resources\\Icons\\Admin_icon\\Excel.png", 30, 30, 10, SwingConstants.RIGHT, SwingConstants.CENTER);
        bntExportFile.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Save Excel File");
            fileChooser.setFileFilter(new FileNameExtensionFilter("Excel Files", "xlsx"));
            fileChooser.setSelectedFile(new File("Order_Details_Export.xlsx"));

            if (fileChooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
                File file = fileChooser.getSelectedFile();
                String filePath = file.getAbsolutePath();

                if (!filePath.toLowerCase().endsWith(".xlsx")) {
                    filePath += ".xlsx";
                }

                if (busOrder.exportOrderDetailsToExcel(filePath)) {
                    CustomDialog.showSuccess("File exported successfully !");
                } else {
                    CustomDialog.showError("Export failed!");
                }
            }
        });
        panel.add(bntExportFile);
        
        // // Button View Confirmed Orders
        // bntViewConfirmed = new MyButton("View Confirmed", 20);
        // bntViewConfirmed.setBackgroundColor(SUCCESS_COLOR);
        // bntViewConfirmed.setHoverColor(SUCCESS_HOVER);
        // bntViewConfirmed.setPressedColor(SUCCESS_HOVER.darker());
        // bntViewConfirmed.setFont(FONT_BUTTON_MEDIUM);
        // bntViewConfirmed.setForeground(Color.WHITE);
        // bntViewConfirmed.setBounds(540, 150, 150, 35);
        // bntViewConfirmed.setButtonIcon("src\\main\\resources\\Icons\\Admin_icon\\order.png", 25, 25, 5, SwingConstants.RIGHT, SwingConstants.CENTER);
        // bntViewConfirmed.addActionListener(e -> {
        //     viewConfirmedOrders();
        // });
        // panel.add(bntViewConfirmed);

           // 1️⃣ Tên cột
        String[] columnNames = {
            "Order.No", "Customer.ID", "Customer Name", "Address", 
            "Contact", "Total Product", "Total Price", "Payment","Date Order", "Time Order", "Status"
        };

        // 2️⃣ Tạo model
        model = new DefaultTableModel(columnNames, 0);



        // 5️⃣ Tạo bảng với style chuẩn
        tableOrder = new MyTable(
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

        JScrollPane tableScrollPane = MyTable.createScrollPane(tableOrder, 20, 220, 1160, 400);

        // 7️⃣ Tùy chỉnh thanh cuộn
        tableScrollPane.getVerticalScrollBar().setPreferredSize(new Dimension(15, Integer.MAX_VALUE));
        tableScrollPane.getHorizontalScrollBar().setPreferredSize(new Dimension(Integer.MAX_VALUE, 15));
        
        tableOrder.addMouseListener(new MouseAdapter() {
           @Override
           public void mouseClicked(MouseEvent e) {
               int selectedRow = tableOrder.getSelectedRow();
               if (selectedRow != -1) {
                   String stateValue = tableOrder.getValueAt(selectedRow, 10).toString();

                   // Đưa giá trị lên combobox
                   cmbStatus.setSelectedItem(stateValue);
               }
           }
       });
        
        
        
        // 8️⃣ Thêm tableScrollPane vào panel
        panel.add(tableScrollPane);
        SwingUtilities.invokeLater(() -> {
            busOrder = new BUS_order();
            List<DTO_order> allOrders = busOrder.getAllOrdersSorted();
            loadOrderDataToTable(allOrders);
            expandTableColumns(); // 👈 Gọi method mới để mở rộng cột
        });                
       
   }
    
   private void loadOrderDataToTable(List<DTO_order> orders) {
      // Xóa dữ liệu cũ trong bảng
      model.setRowCount(0);

      // Định dạng ngày tháng
      DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

      // Thêm dữ liệu mới
      for (DTO_order order : orders) {
          Object[] rowData = {
              order.getOrderNo(),
              order.getCustomerID(),
              order.getCustomerName(),
              order.getAddress(),
              order.getContact(),
              order.getTotalQuantityProduct(),
              order.getTotalPrice(),
              order.getPayment(),
              order.getDateOrder().format(dateFormatter), // Định dạng ngày
              order.getTimeOrder(),
              order.getStatus()
          };
          model.addRow(rowData);
      }
}
    
    private void showOrderDetails() {
        int selectedRow = tableOrder.getSelectedRow();
        if (selectedRow < 0) {
           CustomDialog.showError("Please select an order to see details ! ");
            return;
        }

        // Lấy thông tin từ dòng được chọn
         orderNo = (String) model.getValueAt(selectedRow, 0);
         customerID = (String) model.getValueAt(selectedRow, 1);

        // Tạo và hiển thị form chi tiết
        order_Details detailsFrame = new order_Details(customerID, orderNo);
        detailsFrame.setSize(900, 600);
        detailsFrame.setLocationRelativeTo(null);
        detailsFrame.setVisible(true);
    }
    
    private void Refresh(){
        busOrder = new BUS_order();
        List<DTO_order> allOrders = busOrder.getAllOrdersSorted();
        loadOrderDataToTable(allOrders);
        expandTableColumns();
        cmbStatus.setSelectedIndex(0);
        cmbSearch.setSelectedIndex(0);   
    }
    
    /**
     * Mở rộng các cột của table để sử dụng hết không gian có sẵn
     */
    private void expandTableColumns() {
        if (tableOrder == null) return;
        
        javax.swing.table.TableColumnModel columnModel = tableOrder.getColumnModel();
        int totalWidth = 1160; // Chiều rộng tổng của table (tối ưu cho màn hình nhỏ)
        int columnCount = tableOrder.getColumnCount();
        
        // Định nghĩa tỷ lệ chiều rộng cho từng cột (tối ưu cho màn hình nhỏ)
        double[] columnRatios = {
            0.08,  // Order.No - 8%
            0.08,  // Customer.ID - 8%
            0.15,  // Customer Name - 15%
            0.15,  // Address - 15%
            0.08,  // Contact - 8%
            0.08,  // Total Product - 8%
            0.10,  // Total Price - 10%
            0.08,  // Payment - 8%
            0.08,  // Date Order - 8%
            0.05,  // Time Order - 5%
            0.08   // Status - 8%
        };
        
        // Áp dụng tỷ lệ cho từng cột
        for (int i = 0; i < columnCount && i < columnRatios.length; i++) {
            javax.swing.table.TableColumn column = columnModel.getColumn(i);
            int columnWidth = (int) (totalWidth * columnRatios[i]);
            column.setPreferredWidth(columnWidth);
            column.setWidth(columnWidth);
        }
        
        // Đảm bảo table sử dụng hết không gian
        tableOrder.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        tableOrder.revalidate();
        tableOrder.repaint();
    }
    
    private void updateOrderStatus() {
        int selectedRow = tableOrder.getSelectedRow();
        if (selectedRow < 0) {
            CustomDialog.showError("Please select an order to update Status!");
            return;
        }

        orderNo = (String) model.getValueAt(selectedRow, 0); // Get Order No from selected row
        String currentStatus = (String) model.getValueAt(selectedRow, 10); // Current status from table
        String newStatus = (String) cmbStatus.getSelectedItem(); // Get selected status from combobox

        if (newStatus.equals(currentStatus)) {
            CustomDialog.showSuccess("Status is already " + newStatus);
            return;
        }

        // Update directly without confirmation
        boolean success = busOrder.updateOrderStatus(orderNo, newStatus);

        if (success) {
            // Update the table
            model.setValueAt(newStatus, selectedRow, 10);
            CustomDialog.showSuccess("Status updated successfully to " + newStatus + "!");
            List<DTO_order> allOrders = busOrder.getAllOrdersSorted();
            loadOrderDataToTable(allOrders);
            expandTableColumns();
        } else {
            CustomDialog.showError("Failed to update status!");
        }
    }
    
    /**
     * Hiển thị danh sách các Order đã confirmed để có thể chuyển sang Export
     */
    private void viewConfirmedOrders() {
        List<DTO_order> confirmedOrders = busOrder.getConfirmedOrders();
        if (confirmedOrders.isEmpty()) {
            CustomDialog.showSuccess("No confirmed orders found!");
            return;
        }
        
        // Tạo dialog để hiển thị danh sách confirmed orders
        JDialog dialog = new JDialog();
        dialog.setTitle("Confirmed Orders - Ready for Export");
        dialog.setSize(1000, 600);
        dialog.setLocationRelativeTo(null);
        dialog.setModal(true);
        
        JPanel mainPanel = new JPanel(new BorderLayout());
        
        // Tạo bảng cho confirmed orders
        String[] columnNames = {
            "Order.No", "Customer.ID", "Customer Name", "Address", 
            "Contact", "Total Product", "Total Price", "Payment", "Date Order", "Time Order", "Status"
        };
        
        DefaultTableModel confirmedModel = new DefaultTableModel(columnNames, 0);
        JTable confirmedTable = new JTable(confirmedModel);
        
        // Đổ dữ liệu vào bảng
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        for (DTO_order order : confirmedOrders) {
            Object[] rowData = {
                order.getOrderNo(),
                order.getCustomerID(),
                order.getCustomerName(),
                order.getAddress(),
                order.getContact(),
                order.getTotalQuantityProduct(),
                order.getTotalPrice(),
                order.getPayment(),
                order.getDateOrder().format(dateFormatter),
                order.getTimeOrder(),
                order.getStatus()
            };
            confirmedModel.addRow(rowData);
        }
        
        JScrollPane tableScrollPane = new JScrollPane(confirmedTable);
        mainPanel.add(tableScrollPane, BorderLayout.CENTER);
        
        // Panel buttons
        JPanel buttonPanel = new JPanel(new FlowLayout());
        
        MyButton btnCreateExport = new MyButton("Create Export Bill", 20);
        btnCreateExport.setBackgroundColor(PRIMARY_COLOR);
        btnCreateExport.setHoverColor(PRIMARY_HOVER);
        btnCreateExport.setPressedColor(PRIMARY_HOVER.darker());
        btnCreateExport.setFont(FONT_BUTTON_MEDIUM);
        btnCreateExport.setForeground(Color.WHITE);
        btnCreateExport.addActionListener(e -> {
            int selectedRow = confirmedTable.getSelectedRow();
            if (selectedRow < 0) {
                CustomDialog.showError("Please select an order to create export bill!");
                return;
            }
            
            String selectedOrderNo = (String) confirmedModel.getValueAt(selectedRow, 0);
            String selectedCustomerID = (String) confirmedModel.getValueAt(selectedRow, 1);
            
            // Mở form Export với thông tin Order đã chọn
            openExportForm(selectedOrderNo, selectedCustomerID);
            dialog.dispose();
        });
        
        MyButton btnClose = new MyButton("Close", 20);
        btnClose.setBackgroundColor(WARNING_COLOR);
        btnClose.setHoverColor(WARNING_HOVER);
        btnClose.setPressedColor(WARNING_HOVER.darker());
        btnClose.setFont(FONT_BUTTON_MEDIUM);
        btnClose.setForeground(Color.WHITE);
        btnClose.addActionListener(e -> dialog.dispose());
        
        buttonPanel.add(btnCreateExport);
        buttonPanel.add(btnClose);
        
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        dialog.add(mainPanel);
        dialog.setVisible(true);
    }
    
    /**
     * Mở form Export với thông tin Order đã chọn
     */
    private void openExportForm(String orderNo, String customerID) {
        try {
            // Import form Export
            Class<?> exportFormClass = Class.forName("com.Admin.export.GUI.Form_Export");
            java.lang.reflect.Constructor<?> constructor = exportFormClass.getConstructor(String.class, String.class);
            JFrame exportFrame = (JFrame) constructor.newInstance(orderNo, customerID);
            exportFrame.setVisible(true);
        } catch (Exception e) {
            e.printStackTrace();
            CustomDialog.showError("Cannot open Export form: " + e.getMessage());
        }
    }
    
}