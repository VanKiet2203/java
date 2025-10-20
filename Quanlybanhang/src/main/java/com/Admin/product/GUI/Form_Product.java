package com.Admin.product.GUI;

import com.Admin.product.BUS.BusProduct;
import com.Admin.product.DTO.DTOProduct;
import com.ComponentandDatabase.Components.MyButton;
import com.ComponentandDatabase.Components.CustomDialog;
import com.ComponentandDatabase.Components.MyCombobox;
import com.ComponentandDatabase.Components.MyPanel;
import com.ComponentandDatabase.Components.MyTable;
import com.ComponentandDatabase.Components.MyTextField;
import javax.swing.JPanel;
import javax.swing.table.DefaultTableModel;
import javax.swing.JLabel;
import java.awt.Color;
import java.awt.BorderLayout;
import javax.swing.SwingConstants;
import java.awt.Dimension;
import javax.swing.*;
import javax.swing.BorderFactory;
import static com.ComponentandDatabase.Components.UIConstants.*;

public class Form_Product extends JPanel  implements ProductUpdateObserver {
    private JPanel panel, panelSearch;
    private MyButton bntSearch, bntNew, bntEdit, bntDelete, bntRefresh, bntExportFile, bntImportFile;
    private MyCombobox<String> cmbSearchProduct;
    public MyTable tableProduct;
    private MyTextField txtSearch;
    private BusProduct busProduct;

    public Form_Product() {
        initComponents();
        init();
        ProductUpdateNotifier.getInstance().registerObserver(this);
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
            JLabel lblTitle = new JLabel("MANAGE PRODUCT");
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
            panelSearch.setBounds(20, 60, 1160, 120);
            
  
            // ComboBox search
            String[] items = {"Product.ID", "Product Name", "Brand.ID", "Available", "Unavailable"};
            cmbSearchProduct = new MyCombobox<>(items);
            cmbSearchProduct.setBounds(20, 30, 150, 35);
            cmbSearchProduct.setCustomFont(FONT_CONTENT_MEDIUM);
            cmbSearchProduct.setCustomColors(Color.WHITE, Color.GRAY, Color.BLACK);
            
            // TextField search
            txtSearch = new MyTextField();
            txtSearch.setHint("Search something...");
            txtSearch.setBounds(180, 30, 300, 35);
            txtSearch.setTextFont(FONT_CONTENT_MEDIUM);
            panelSearch.add(txtSearch);
           cmbSearchProduct.repaint();
           cmbSearchProduct.revalidate();

                    // 👉 Thêm đoạn invokeLater để đảm bảo cmbSearch được refresh UI
           SwingUtilities.invokeLater(() -> {
               
             cmbSearchProduct.repaint();
             cmbSearchProduct.revalidate();
              //cmbSearch.updateUI(); // 👈 Bắt buộc để refresh lại giao diện
           });

            panelSearch.add(cmbSearchProduct);
            
            bntSearch = new MyButton("Search", 20);
            stylePrimaryButton(bntSearch);
            bntSearch.setBounds(490, 30, 120, 35);
            bntSearch.setButtonIcon("src\\main\\resources\\Icons\\Admin_icon\\search.png", 25, 25, 5, SwingConstants.RIGHT, SwingConstants.CENTER);
           bntSearch.addActionListener((e) -> {
            String selectedColumn = cmbSearchProduct.getSelectedItem().toString();
            String keyword = txtSearch.getText().trim();

            DefaultTableModel model = (DefaultTableModel) tableProduct.getModel();
            BusProduct busProduct = new BusProduct();
            busProduct.searchProduct(keyword, selectedColumn, model);  // Gọi hàm mới dùng void
      });


            
            panelSearch.add(bntSearch);
            panel.add(panelSearch);
            
           bntNew = new MyButton("Add new", 20);
           stylePrimaryButton(bntNew);
           bntNew.setButtonIcon("src\\main\\resources\\Icons\\Admin_icon\\new.png", 25, 25, 5, SwingConstants.RIGHT, SwingConstants.CENTER);    
           bntNew.setBounds(410, 70, 130, 35); // Hàng 2
          bntNew.addActionListener(e -> {
            NewProduct newProductFrame = new NewProduct();
             newProductFrame.setVisible(true);
           
      });


           panelSearch.add(bntNew);
           
          bntRefresh = new MyButton("Refresh", 20);
          styleInfoButton(bntRefresh);
          bntRefresh.setBounds(620, 30, 120, 35); // Hàng 1
          bntRefresh.setButtonIcon("src\\main\\resources\\Icons\\Admin_icon\\refresh.png", 25, 25, 10, SwingConstants.RIGHT, SwingConstants.CENTER);
          bntRefresh.addActionListener((e) -> {
              initRefreshButton();
          });
          panelSearch.add(bntRefresh);
          
           bntEdit = new MyButton("Edit", 20);
           styleWarningButton(bntEdit);
           bntEdit.setButtonIcon("src\\main\\resources\\Icons\\Admin_icon\\edit.png", 25, 25, 5, SwingConstants.RIGHT, SwingConstants.CENTER);    
           bntEdit.setBounds(20, 70, 120, 35); // Hàng 2

            bntEdit.addActionListener(e -> {
          // 1. Kiểm tra dòng được chọn
          int selectedRow = tableProduct.getSelectedRow();
          if (selectedRow == -1) {
              CustomDialog.showError("Please choose the product to edit");
              return;
          }

          busProduct= new BusProduct();
          // 2. Lấy Product_ID từ dòng được chọn
          String productID = tableProduct.getValueAt(selectedRow, 0).toString();

          // 3. Gọi phương thức getProductById() từ DAO/Service
          DTOProduct product = busProduct.getProductById(productID);
          if (product == null) {
            CustomDialog.showError("Product information is not found !");
              return;
          }

          // 4. Tạo và thiết lập form Edit
          EditProduct editFrame = new EditProduct();

         editFrame.showDetail(product);

          // 5. Hiển thị form
          editFrame.setVisible(true);
      });
            panelSearch.add(bntEdit);
            
            bntDelete = new MyButton("Delete", 20);
            styleDangerButton(bntDelete);
            bntDelete.setBounds(150, 70, 120, 35); // Hàng 2
            bntDelete.setButtonIcon("src\\main\\resources\\Icons\\Admin_icon\\delete.png", 25, 25, 10, SwingConstants.RIGHT, SwingConstants.CENTER);
            // Add click event for Delete button
            bntDelete.addActionListener(e -> {
                // 1. Check selected row
                int selectedRow = tableProduct.getSelectedRow();
                if (selectedRow == -1) {
                    CustomDialog.showError("Please select a product to delete!");
                    return;
                }

                // 2. Get Product_ID from selected row
                String productId = tableProduct.getValueAt(selectedRow, 0).toString();
                String productName = tableProduct.getValueAt(selectedRow, 1).toString();

               boolean confirm= CustomDialog.showOptionPane(
                    "Confirm Deletion",
                    "Are you sure you want to delete: "+productName+" ?",
                    UIManager.getIcon("OptionPane.questionIcon"),
                    Color.decode("#FF6666")
            );

                // 4. If user confirms deletion
                if (confirm) {
                    try {
                        // 5. Call deleteProduct method from BUS layer
                        boolean isDeleted = busProduct.deleteProduct(productId);

                        if (isDeleted) {                  
                          // 6. Refresh table data
                            DefaultTableModel model = (DefaultTableModel) tableProduct.getModel();
                            model.setRowCount(0); // Clear old data
                            busProduct.uploadProduct(model); // Reload new data
                            tableProduct.adjustColumnWidths(); // Adjust columns
                        } 
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        CustomDialog.showError("System error while deleting product: " + ex.getMessage());
                    }
                }
            });

            panelSearch.add(bntDelete);
            
           // IMEI button removed
           
           bntExportFile = new MyButton("Export",20);
           bntExportFile.setBackgroundColor(Color.WHITE); // Màu nền
           bntExportFile.setPressedColor(Color.decode("#D3D3D3")); // Màu khi nhấn
           bntExportFile.setHoverColor(Color.decode("#EEEEEE")); // Màu khi rê chuột vào
           bntExportFile.setBounds(280, 70, 120, 35); // Hàng 2
        //    bntExportFile.setFont(new Font("sansserif", Font.BOLD, 18));
           bntExportFile.setForeground(Color.BLACK);
           bntExportFile.setButtonIcon("src\\main\\resources\\Icons\\Admin_icon\\Excel.png", 30, 30, 10, SwingConstants.RIGHT, SwingConstants.CENTER);
           bntExportFile.addActionListener((e) -> {
              JFileChooser fileChooser = new JFileChooser();
                fileChooser.setDialogTitle("Save Excel file");

                int userSelection = fileChooser.showSaveDialog(null);
                if (userSelection == JFileChooser.APPROVE_OPTION) {
                    String path = fileChooser.getSelectedFile().getAbsolutePath();

                    // Thêm phần mở rộng nếu chưa có
                    if (!path.toLowerCase().endsWith(".xlsx")) {
                        path += ".xlsx";
        }
               busProduct= new BusProduct();
               busProduct.exportFile(path);
            }
         });
           panelSearch.add(bntExportFile);
           
           // Import File button
           bntImportFile = new MyButton("Import", 20);
           bntImportFile.setBackgroundColor(Color.WHITE);
           bntImportFile.setPressedColor(Color.decode("#D3D3D3"));
           bntImportFile.setHoverColor(Color.decode("#EEEEEE"));
           bntImportFile.setBounds(410, 70, 120, 35); // Next to Export button
           bntImportFile.setForeground(Color.BLACK);
           bntImportFile.setButtonIcon("src\\main\\resources\\Icons\\Admin_icon\\import.png", 30, 30, 10, SwingConstants.RIGHT, SwingConstants.CENTER);
           bntImportFile.addActionListener((e) -> {
               JFileChooser fileChooser = new JFileChooser();
               fileChooser.setDialogTitle("Choose Excel file to import");
               fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Excel Files (*.xlsx)", "xlsx"));
               
               int result = fileChooser.showOpenDialog(null);
               if (result == JFileChooser.APPROVE_OPTION) {
                   java.io.File selectedFile = fileChooser.getSelectedFile();
                   busProduct = new BusProduct();
                   busProduct.importFile(selectedFile);
                   
                   // Refresh table after import
                   DefaultTableModel model = (DefaultTableModel) tableProduct.getModel();
                   model.setRowCount(0);
                   busProduct.uploadProduct(model);
                   tableProduct.adjustColumnWidths();
               }
           });
           panelSearch.add(bntImportFile);

                    // 1️⃣ Tên cột
         String[] columnNames = {
             "Product ID", 
             "Product Name", 
             "Color",
             "Speed",
             "Battery Capacity",
             "Quantity",
             "Price",
             "Category ID",
             "Category Name"
         };

         // 2️⃣ Tạo model
         DefaultTableModel model = new DefaultTableModel(columnNames, 0);

         

         // 5️⃣ Tạo bảng với style chuẩn
         tableProduct = createStyledTable(model);
         tableProduct.setRowHeight(30);

         // 6️⃣ ScrollPane chứa bảng - tối ưu cho màn hình nhỏ
         JScrollPane tableScrollPane = MyTable.createScrollPane(tableProduct, 20, 200, 1160, 350);

         // 7️⃣ Tùy chỉnh thanh cuộn
         tableScrollPane.getVerticalScrollBar().setPreferredSize(new Dimension(15, Integer.MAX_VALUE));
         tableScrollPane.getHorizontalScrollBar().setPreferredSize(new Dimension(Integer.MAX_VALUE, 15));

         // 8️⃣ Thêm tableScrollPane vào panel
         panel.add(tableScrollPane);

         SwingUtilities.invokeLater(() -> {
               busProduct = new BusProduct(); // Có thể khai báo sẵn ở đầu lớp GUI
               busProduct.uploadProduct(model);
               expandTableColumns(); // Mở rộng cột như bảng Order
          });
             
   }
    
    // Thêm phương thức refresh
    public void refreshTable() {
        DefaultTableModel model = (DefaultTableModel) tableProduct.getModel();
        model.setRowCount(0); // Xóa dữ liệu cũ
        busProduct.uploadProduct(model);
        expandTableColumns(); // Sử dụng expandTableColumns thay vì adjustColumnWidths
    }
    
    @Override
    public void onProductUpdated() {
        SwingUtilities.invokeLater(() -> {
            refreshTable();
        });
    }
    
    // Thêm nút Refresh (nếu chưa có)
    private void initRefreshButton() {
        bntRefresh.addActionListener(e -> refreshTable());
    }
    
    // Khi form đóng (nếu là JFrame)
    @Override
    public void removeNotify() {
        super.removeNotify();
        ProductUpdateNotifier.getInstance().unregisterObserver(this);
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

    private void styleDangerButton(MyButton btn) {
        btn.setBackgroundColor(DANGER_COLOR);
        btn.setHoverColor(DANGER_HOVER);
        btn.setPressedColor(DANGER_HOVER.darker());
        btn.setFont(FONT_BUTTON_MEDIUM);
        btn.setForeground(Color.WHITE);
    }

    private void styleWarningButton(MyButton btn) {
        btn.setBackgroundColor(WARNING_COLOR);
        btn.setHoverColor(WARNING_HOVER);
        btn.setPressedColor(WARNING_HOVER.darker());
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
    
    /**
     * Mở rộng các cột của table để sử dụng hết không gian có sẵn
     */
    private void expandTableColumns() {
        if (tableProduct == null) return;
        
        javax.swing.table.TableColumnModel columnModel = tableProduct.getColumnModel();
        int totalWidth = 1160; // Chiều rộng tổng của table (tối ưu cho màn hình nhỏ)
        int columnCount = tableProduct.getColumnCount();
        
        // Định nghĩa tỷ lệ chiều rộng cho từng cột (tối ưu cho màn hình nhỏ)
        double[] columnRatios = {
            0.12,  // Product ID - 12%
            0.22,  // Product Name - 22%
            0.08,  // Color - 8%
            0.08,  // Speed - 8%
            0.12,  // Battery Capacity - 12%
            0.08,  // Quantity - 8%
            0.10,  // Price - 10%
            0.10,  // Category ID - 10%
            0.10   // Category Name - 10%
        };
        
        // Áp dụng tỷ lệ cho từng cột
        for (int i = 0; i < columnCount && i < columnRatios.length; i++) {
            javax.swing.table.TableColumn column = columnModel.getColumn(i);
            int columnWidth = (int) (totalWidth * columnRatios[i]);
            column.setPreferredWidth(columnWidth);
            column.setWidth(columnWidth);
        }
        
        // Đảm bảo table sử dụng hết không gian
        tableProduct.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        tableProduct.revalidate();
        tableProduct.repaint();
    }
}
