package com.Admin.category.GUI;

import javax.swing.JPanel;
import javax.swing.JLabel;
import java.awt.Color;
import java.awt.Font;
import java.io.File;
import java.awt.event.ActionEvent;
import java.util.List;
import javax.swing.JFileChooser;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.table.DefaultTableModel;
import javax.swing.ImageIcon;
import javax.swing.SwingConstants;
import java.awt.Dimension;
import com.ComponentandDatabase.Components.MyTable;
import com.ComponentandDatabase.Components.MyCombobox;
import com.ComponentandDatabase.Components.MyTextField;
import com.ComponentandDatabase.Components.MyPanel;
import com.ComponentandDatabase.Components.MyButton;
import com.ComponentandDatabase.Components.CustomDialog;
import com.Admin.category.BUS.BusCategory;
import com.Admin.category.DTO.DTOCategory;
import javax.swing.BorderFactory;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.Timer;
import static com.ComponentandDatabase.Components.UIConstants.*;

public class Form_Category extends JPanel {
    private JPanel panel, panelSearch;
    private JLabel lblCateID, lblCateName, lblBrandID; 
    private MyTextField txtSearch, txtCateID, txtCateName;
    private MyButton bntSearch, bntSave, bntUpdate, bntDelete, bntClear, bntRefresh,bntImportFile, bntExportFile;
    private MyCombobox<String> cmbSearchcate, cmbBrandID;
    private MyTable tableCate;
    private BusCategory busCategory;
    
    // Image carousel variables
    private JLabel carouselImageLabel;
    private Timer carouselTimer;
    private int currentImageIndex = 0;
    private final String[] imagePaths = new String[] {
        "src\\main\\resources\\Icons\\Admin_icon\\XDD004.png",
        "src\\main\\resources\\Icons\\Admin_icon\\XDD005.png",
        "src\\main\\resources\\Icons\\Admin_icon\\JS50.png"
    };
    public Form_Category() {
        initComponents();
        init();
    }

    private void initComponents() {
        setLayout(null);
        setPreferredSize(new Dimension(1530, 860)); // Giữ kích thước nhưng không ép buộc vị trí
        setBackground(Color.WHITE); // Kiểm tra hiển thị
    }

    private void init() {
        
        panel = new JPanel();
        panel.setLayout(null);
        panel.setBounds(0, 0, 1530, 860); // Giữ nguyên layout của các thành phần
        panel.setBackground(Color.WHITE); // Màu xanh dương
        add(panel);

        // Tiêu đề
//        lblTitle = new JLabel("Form Category");
//        lblTitle.setFont(new Font("Arial", Font.BOLD, 25));
//        lblTitle.setForeground(Color.WHITE);
//        lblTitle.setHorizontalAlignment(SwingConstants.CENTER);
//        lblTitle.setBounds(280, 130, 250, 50); // Định vị đúng bên trong panel
//        lblTitle.setForeground(Color.BLACK);
//        panel.add(lblTitle);
        
            // Title
            JLabel lblTitle = new JLabel("MANAGE CATEGORY");
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
            panelSearch.setBounds(20, 60, 1490, 100);
            
  
            // ComboBox search type
            String[] items = {"Category.ID", "Category Name", "Brand.ID"};
            cmbSearchcate = new MyCombobox<>(items);
            cmbSearchcate.setBounds(20, 30, 150, 35);
            cmbSearchcate.setCustomFont(FONT_CONTENT_MEDIUM);
            cmbSearchcate.setCustomColors(Color.WHITE, Color.GRAY, Color.BLACK);
            
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
            //cmbSearch.setMaximumRowCount(5); // Giới hạn dòng dropdown nếu dài
            cmbSearchcate.repaint();
            cmbSearchcate.revalidate();

                    // 👉 Thêm đoạn invokeLater để đảm bảo cmbSearch được refresh UI
           SwingUtilities.invokeLater(() -> {
              cmbSearchcate.repaint();
              cmbSearchcate.revalidate();
              //cmbSearch.updateUI(); // 👈 Bắt buộc để refresh lại giao diện
           });

            panelSearch.add(cmbSearchcate);
            
            bntSearch = new MyButton("Search", 20);
            stylePrimaryButton(bntSearch);
            bntSearch.setBounds(490, 30, 120, 35);
            bntSearch.setButtonIcon("src\\main\\resources\\Icons\\Admin_icon\\search.png", 25, 25, 5, SwingConstants.RIGHT, SwingConstants.CENTER);
            bntSearch.addActionListener((ActionEvent e) -> {
                // Clear TextFields
               String keyword= txtSearch.getText().trim();
               String selectedItem = cmbSearchcate.getSelectedItem().toString();
               List<DTOCategory> results = busCategory.searchCategory(keyword, selectedItem);
                            // Clear bảng trước khi đổ dữ liệu mới
                DefaultTableModel model = (DefaultTableModel) tableCate.getModel();
                model.setRowCount(0); // Xóa dữ liệu cũ trong bảng

                // Đổ kết quả vào JTable
                for (DTOCategory cate : results) {
                    model.addRow(new Object[]{
                        cate.getCategoryID(),
                        cate.getCategoryName(),
                        cate.getSupID()
                    });
                }
         });
            panelSearch.add(bntSearch);
            panel.add(panelSearch);
            
            // Form Panel
            JPanel formPanel = new MyPanel(Color.WHITE);
            formPanel.setLayout(null);
            formPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(PRIMARY_COLOR, 2),
                "Category Information", 0, 0, FONT_TITLE_SMALL, PRIMARY_COLOR
            ));
            formPanel.setBounds(20, 180, 450, 250);
            panel.add(formPanel);
            
            lblCateID= new JLabel("Category.ID");
            lblCateID.setFont(FONT_LABEL_BOLD);
            lblCateID.setForeground(Color.BLACK);
            lblCateID.setBounds(20, 30, 120, 30);
            formPanel.add(lblCateID);
            
            txtCateID = new MyTextField();
            txtCateID.setBounds(150, 30, 200, 35);
            txtCateID.setTextFont(FONT_CONTENT_MEDIUM);
            txtCateID.setBorder(BorderFactory.createLineBorder(BORDER_GRAY, 1));
            formPanel.add(txtCateID);
            
            lblCateName= new JLabel("Category Name");
            lblCateName.setFont(FONT_LABEL_BOLD);
            lblCateName.setForeground(Color.BLACK);
            lblCateName.setBounds(20, 80, 120, 30);
            formPanel.add(lblCateName);
            
            txtCateName= new MyTextField();
            txtCateName.setBounds(150, 80, 200, 35);
            txtCateName.setTextFont(FONT_CONTENT_MEDIUM);
            txtCateName.setBorder(BorderFactory.createLineBorder(BORDER_GRAY, 1));
            formPanel.add(txtCateName);
            
            lblBrandID= new JLabel("Brand.ID");
            lblBrandID.setFont(FONT_LABEL_BOLD);
            lblBrandID.setForeground(Color.BLACK);
            lblBrandID.setBounds(20, 130, 120, 30);
            formPanel.add(lblBrandID);
            
            
           // Tạo danh sách item cho JComboBox (ban đầu rỗng)
            cmbBrandID = new MyCombobox<String>();
            cmbBrandID.setBounds(150, 130, 200, 35);
            cmbBrandID.setCustomFont(FONT_CONTENT_MEDIUM);
            cmbBrandID.setCustomColors(Color.WHITE, Color.GRAY, Color.BLACK);

            // 👉 Gọi SwingUtilities để đảm bảo UI cập nhật đúng
            SwingUtilities.invokeLater(() -> {
                cmbBrandID.removeAllItems(); // Xóa item cũ nếu có

                // Lấy danh sách Sup_ID từ cơ sở dữ liệu
                List<String> supIDs = busCategory.getAllSupplierIDs();

                // Thêm từng Sup_ID vào combobox
                for (String id : supIDs) {
                    cmbBrandID.addItem(id);
                }

                // Làm mới giao diện của combobox
                cmbBrandID.repaint();
                cmbBrandID.revalidate();
            });


           formPanel.add(cmbBrandID);
           busCategory= new BusCategory();
           bntSave = new MyButton("Save", 20);
           stylePrimaryButton(bntSave);
           bntSave.setBounds(10, 180, 100, 35);
           bntSave.addActionListener(e -> {
            String categoryID = txtCateID.getText().trim();
            String categoryName = txtCateName.getText().trim();
            String brandID = cmbBrandID.getSelectedItem() != null ? cmbBrandID.getSelectedItem().toString() : null;

            if (categoryID.isEmpty() || categoryName.isEmpty() || brandID == null) {
                CustomDialog.showError("Please fill in all required fields!");
                return;
            }

            // Kiểm tra trùng ID trước
            if (busCategory.isDuplicateID(categoryID)) {
                CustomDialog.showError("This Category.ID already exists!");
                return;
            }

            DTOCategory category = new DTOCategory();
            category.setCategoryID(categoryID);
            category.setCategoryName(categoryName);
            category.setSupID(brandID);

            boolean success = busCategory.addCategory(category);
            if (success) {
                CustomDialog.showSuccess("This category was added successfully!");
                DefaultTableModel model = (DefaultTableModel) tableCate.getModel();
                model.setRowCount(0); // Xóa dữ liệu cũ trong bảng
                busCategory.loadCategoryToTable(model); // Tải lại dữ liệu từ database
            } else {
                CustomDialog.showError("Failed to add category. Please try again later.");
            }
        });

           formPanel.add(bntSave);
           
           bntUpdate = new MyButton("Update", 20);
           styleWarningButton(bntUpdate);
           bntUpdate.setBounds(120, 180, 100, 35);
           bntUpdate.addActionListener((ActionEvent e) -> {
            String categoryID = txtCateID.getText().trim();
            String categoryName = txtCateName.getText().trim();
            String supplierID = (String) cmbBrandID.getSelectedItem();

            if (categoryID.isEmpty() || categoryName.isEmpty() || supplierID == null) {
                CustomDialog.showError("Please select a category to update !");
                return;
            }

            DTOCategory category = new DTOCategory();
            category.setCategoryID(categoryID);
            category.setCategoryName(categoryName);
            category.setSupID(supplierID);

            boolean success = busCategory.updateCategory(category);

            if (success) {
               CustomDialog.showSuccess("Category updated successfully !");
                // Refresh lại bảng sau khi cập nhật
                DefaultTableModel model = (DefaultTableModel) tableCate.getModel();
                model.setRowCount(0); // Xóa dữ liệu cũ
                busCategory.loadCategoryToTable(model);
            } else {
               CustomDialog.showError("Update the list of failures !");
            }
        });

           formPanel.add(bntUpdate);
           
           bntDelete = new MyButton("Delete", 20);
           styleDangerButton(bntDelete);
           bntDelete.setBounds(230, 180, 100, 35);
           bntDelete.addActionListener((ActionEvent e) -> {
            int selectedRow = tableCate.getSelectedRow();

            if (selectedRow != -1) {
                String cateID = tableCate.getValueAt(selectedRow, 0).toString();
                String cateName = tableCate.getValueAt(selectedRow, 1).toString(); // Column 1 is category name

                boolean confirm = CustomDialog.showOptionPane(
                        "Confirm Deletion",
                        "Are you sure you want to delete the category: " + cateName + "?",
                        UIManager.getIcon("OptionPane.questionIcon"),
                        Color.decode("#FF6666")
                );

                if (confirm) {
                    boolean success = busCategory.deleteCategory(cateID);

                    if (success) {
                        CustomDialog.showSuccess("Successfully deleted the category: " + cateName + "!");
                        // Refresh table
                        DefaultTableModel model = (DefaultTableModel) tableCate.getModel();
                        model.setRowCount(0);
                        busCategory.loadCategoryToTable(model);
                    } else {
                        CustomDialog.showError("Failed to delete the category!");
                    }
                }

            } else {
                CustomDialog.showError("Please select a category to delete!");
            }
        });

           formPanel.add(bntDelete);
           
          bntClear = new MyButton("Clear", 20);
          styleInfoButton(bntClear);
          bntClear.setBounds(340, 180, 100, 35);
            bntClear.addActionListener((ActionEvent e) -> {
                // Clear TextFields
                txtCateID.setText("");
                txtCateName.setText("");

                // Reset combobox về item đầu tiên (nếu có)
                if (cmbBrandID.getItemCount() > 0) {
                    cmbBrandID.setSelectedIndex(0);
                }
            });
          formPanel.add(bntClear);
          
          bntRefresh = new MyButton("Refresh", 20);
          styleInfoButton(bntRefresh);
          bntRefresh.setBounds(1100, 30, 120, 35); // Trong cùng ô nền với search
          bntRefresh.setButtonIcon("src\\main\\resources\\Icons\\Admin_icon\\refresh.png", 25, 25, 10, SwingConstants.RIGHT, SwingConstants.CENTER);
            // Gắn sự kiện cho nút Refresh
            bntRefresh.addActionListener((ActionEvent e) -> {
                DefaultTableModel model = (DefaultTableModel) tableCate.getModel();
                model.setRowCount(0); // Xóa dữ liệu cũ trong bảng
                busCategory.refreshTable(model); // Tải lại dữ liệu từ database
            });
          panelSearch.add(bntRefresh);
          
          bntImportFile = new MyButton("Import File", 0);
          bntImportFile.setBackgroundColor(Color.WHITE); // Màu nền
          bntImportFile.setPressedColor(Color.decode("#D3D3D3")); // Màu khi nhấn
          bntImportFile.setHoverColor(Color.decode("#EEEEEE")); // Màu khi rê chuột vào
          bntImportFile.setBounds(780, 30, 150, 35); // Trong cùng ô nền với search
          bntImportFile.setFont(new Font("sansserif", Font.BOLD, 14));
          bntImportFile.setForeground(Color.BLACK);
          bntImportFile.setButtonIcon("src\\main\\resources\\Icons\\Admin_icon\\Excel.png", 30, 30, 10, SwingConstants.RIGHT, SwingConstants.CENTER);
           bntImportFile.addActionListener((ActionEvent e) -> {
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setDialogTitle("Choose a file excel to import");

                int result = fileChooser.showOpenDialog(null);
                if (result == JFileChooser.APPROVE_OPTION) {
                    File selectedFile = fileChooser.getSelectedFile();
                    busCategory.importFile(selectedFile);
                      DefaultTableModel model = (DefaultTableModel) tableCate.getModel();
                      model.setRowCount(0); // Xóa dữ liệu cũ trong bảng
                      busCategory.loadCategoryToTable(model);
                      tableCate.adjustColumnWidths();
                }
            });

          panelSearch.add(bntImportFile);
          
          
           bntExportFile = new MyButton("Export File", 0);
           bntExportFile.setBackgroundColor(Color.WHITE); // Màu nền
           bntExportFile.setPressedColor(Color.decode("#D3D3D3")); // Màu khi nhấn
           bntExportFile.setHoverColor(Color.decode("#EEEEEE")); // Màu khi rê chuột vào
           bntExportFile.setBounds(940, 30, 150, 35); // Trong cùng ô nền với search
           bntExportFile.setFont(new Font("sansserif", Font.BOLD, 14));
           bntExportFile.setForeground(Color.BLACK);
           bntExportFile.setButtonIcon("src\\main\\resources\\Icons\\Admin_icon\\Excel.png", 30, 30, 10, SwingConstants.RIGHT, SwingConstants.CENTER);
            bntExportFile.addActionListener((ActionEvent e) -> {
                busCategory.exportFile(tableCate);
            });
           panelSearch.add(bntExportFile);

                        // Tạo bảng
             String[] columnNames = {"Category.ID", "Category Name", "Brand.ID"};
             DefaultTableModel model = new DefaultTableModel(columnNames, 0);
             tableCate = createStyledTable(model);
             tableCate.setRowHeight(30);

             JScrollPane scrollPane = MyTable.createScrollPane(tableCate, 490, 180, 1020, 250);
             scrollPane.getVerticalScrollBar().setPreferredSize(new Dimension(15, Integer.MAX_VALUE));
             scrollPane.getHorizontalScrollBar().setPreferredSize(new Dimension(Integer.MAX_VALUE, 15));
             panel.add(scrollPane);

             SwingUtilities.invokeLater(() -> {
                 busCategory.loadCategoryToTable(model);
                 tableCate.adjustColumnWidths();
             });

             // 🔁 Sự kiện click để hiển thị dữ liệu lên form
             tableCate.addMouseListener(new MouseAdapter() {
                 @Override
                 public void mouseClicked(MouseEvent e) {
                     int selectedRow = tableCate.getSelectedRow();
                     if (selectedRow != -1) {
                         txtCateID.setText(tableCate.getValueAt(selectedRow, 0).toString());
                         txtCateName.setText(tableCate.getValueAt(selectedRow, 1).toString());
                         cmbBrandID.setSelectedItem(tableCate.getValueAt(selectedRow, 2).toString());
                     }
                 }
             });

            
        // Tạo image carousel tương tự PanelCover
        createImageCarousel();
        
//        // 9️⃣ Repaint panel sau khi thêm
//        panel.revalidate();
//        panel.repaint();
        
        this.repaint();
        this.revalidate();  
               
   }
   
   /**
    * Dừng carousel timer khi form bị đóng
    */
   public void stopCarousel() {
       if (carouselTimer != null && carouselTimer.isRunning()) {
           carouselTimer.stop();
       }
   }
   
   /**
    * Tạo image carousel tương tự PanelCover
    */
   private void createImageCarousel() {
       carouselImageLabel = new JLabel();
       carouselImageLabel.setBounds(20, 430, 450, 300);
       carouselImageLabel.setHorizontalAlignment(SwingConstants.CENTER);
       carouselImageLabel.setVerticalAlignment(SwingConstants.CENTER);
       carouselImageLabel.setBorder(BorderFactory.createLineBorder(PRIMARY_COLOR, 2));
       
       panel.add(carouselImageLabel);
       updateCarouselImage();
       
       carouselTimer = new Timer(3000, e -> {
           currentImageIndex = (currentImageIndex + 1) % imagePaths.length;
           updateCarouselImage();
       });
       carouselTimer.start();
   }
   
   /**
    * Cập nhật ảnh trong carousel
    */
   private void updateCarouselImage() {
       try {
           String path = imagePaths[currentImageIndex];
           ImageIcon icon = new ImageIcon(path);
           java.awt.Image scaled = icon.getImage().getScaledInstance(
               carouselImageLabel.getWidth() - 20, 
               carouselImageLabel.getHeight() - 20, 
               java.awt.Image.SCALE_SMOOTH
           );
           carouselImageLabel.setIcon(new ImageIcon(scaled));
       } catch (Exception e) {
           carouselImageLabel.setText("Image not found");
           carouselImageLabel.setHorizontalAlignment(SwingConstants.CENTER);
       }
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
}
