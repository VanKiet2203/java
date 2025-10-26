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
            
            // Tạo panelSearch với màu nền trắng - CHUẨN HÓA
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
            String[] items = {"Product.ID", "Product Name", "Brand.ID", "Available", "Unavailable"};
            cmbSearchProduct = new MyCombobox<>(items);
            cmbSearchProduct.setBounds(20, 30, 150, 35);
            cmbSearchProduct.setCustomFont(FONT_CONTENT_MEDIUM);
            cmbSearchProduct.setCustomColors(Color.WHITE, Color.GRAY, Color.BLACK);
            
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
            bntNew.setBounds(20, 170, 130, 35); // Giữa search panel và bảng
            bntNew.addActionListener(e -> {
            NewProduct newProductFrame = new NewProduct();
            newProductFrame.setVisible(true);
           
      });


           panel.add(bntNew);

           bntEdit = new MyButton("Edit", 20);
           styleWarningButton(bntEdit);
           bntEdit.setButtonIcon("src\\main\\resources\\Icons\\Admin_icon\\edit.png", 25, 25, 5, SwingConstants.RIGHT, SwingConstants.CENTER);    
           bntEdit.setBounds(160, 170, 120, 35); // Giữa search panel và bảng

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
            panel.add(bntEdit);

          
           
            
            bntDelete = new MyButton("Delete", 20);
            styleDangerButton(bntDelete);
            bntDelete.setBounds(290, 170, 120, 35); // Giữa search panel và bảng
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

            panel.add(bntDelete);
            
            bntRefresh = new MyButton("Refresh", 20);
            styleInfoButton(bntRefresh);
            bntRefresh.setBounds(420, 170, 120, 35); // Giữa search panel và bảng
            bntRefresh.setButtonIcon("src\\main\\resources\\Icons\\Admin_icon\\refresh.png", 25, 25, 10, SwingConstants.RIGHT, SwingConstants.CENTER);
            bntRefresh.addActionListener((e) -> {
                initRefreshButton();
            });
            panel.add(bntRefresh);
           

           bntExportFile = new MyButton("Export",20);
           bntExportFile.setBackgroundColor(Color.WHITE); // Màu nền
           bntExportFile.setPressedColor(Color.decode("#D3D3D3")); // Màu khi nhấn
           bntExportFile.setHoverColor(Color.decode("#EEEEEE")); // Màu khi rê chuột vào
           bntExportFile.setBounds(550, 170, 120, 35); // Giữa search panel và bảng
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
           panel.add(bntExportFile);
           
           // Import File button
           bntImportFile = new MyButton("Import", 20);
           bntImportFile.setBackgroundColor(Color.WHITE);
           bntImportFile.setPressedColor(Color.decode("#D3D3D3"));
           bntImportFile.setHoverColor(Color.decode("#EEEEEE"));
           bntImportFile.setBounds(680, 170, 120, 35); // Giữa search panel và bảng
           bntImportFile.setForeground(Color.BLACK);
           bntImportFile.setButtonIcon("src\\main\\resources\\Icons\\Admin_icon\\Excel.png", 30, 30, 10, SwingConstants.RIGHT, SwingConstants.CENTER);
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
           panel.add(bntImportFile);
           
    
           
        //    // Sync Quantities button
        //    MyButton bntSyncQuantities = new MyButton("Sync Quantities", 20);
        //    bntSyncQuantities.setBackgroundColor(Color.decode("#4CAF50"));
        //    bntSyncQuantities.setPressedColor(Color.decode("#3D8B40"));
        //    bntSyncQuantities.setHoverColor(Color.decode("#45A049"));
        //    bntSyncQuantities.setBounds(670, 70, 120, 35); // Next to Debug button
        //    bntSyncQuantities.setForeground(Color.WHITE);
        //    bntSyncQuantities.addActionListener((e) -> {
        //        busProduct = new BusProduct();
        //        // FIXED: Sử dụng method fix quantity issues thay vì sync
        //        if (busProduct.fixQuantityIssues()) {
        //            CustomDialog.showSuccess("Quantity issues fixed successfully!");
        //        } else {
        //            CustomDialog.showError("Failed to fix quantity issues!");
        //        }
        //        // Refresh table after fix
        //        DefaultTableModel model = (DefaultTableModel) tableProduct.getModel();
        //        model.setRowCount(0);
        //        busProduct.uploadProduct(model);
        //        tableProduct.adjustColumnWidths();
        //    });
        //    panelSearch.add(bntSyncQuantities);

                    // 1️⃣ Tên cột
         String[] columnNames = {
             "Product ID", 
             "Product Name", 
             "Color", 
             "Speed", 
             "Battery Capacity", 
             "Warranty",
             "Total Imported",    // Số lượng nhập
             "Current Stock",      // Số lượng tồn kho
             "Sold Quantity",      // Số lượng đã bán
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
         JScrollPane tableScrollPane = MyTable.createScrollPane(tableProduct, 20, 220, 1160, 350);

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
            0.10,  // Product ID - 10%
            0.18,  // Product Name - 18%
            0.07,  // Color - 7%
            0.07,  // Speed - 7%
            0.10,  // Battery Capacity - 10%
            0.08,  // Warranty (Months) - 8%
            0.08,  // Total Imported - 8%
            0.08,  // Current Stock - 8%
            0.08,  // Sold Quantity - 8%
            0.08,  // Price - 8%
            0.08,  // Category ID - 8%
            0.09   // Category Name - 9%
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
