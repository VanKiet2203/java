package com.Admin.product.GUI;

import com.ComponentandDatabase.Components.MyCombobox;
import com.ComponentandDatabase.Components.MyPanel;
import com.ComponentandDatabase.Components.MyTextField;
import com.ComponentandDatabase.Components.MyButton;
import com.ComponentandDatabase.Components.CustomDialog;
import com.Admin.product.BUS.BusProduct;
import com.Admin.product.DTO.DTOProduct;
import java.awt.Color;
import java.awt.Font;
import javax.swing.*;
import net.miginfocom.swing.MigLayout;
import javax.swing.table.DefaultTableModel;
import javax.swing.BorderFactory;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.Image;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.nio.file.StandardCopyOption;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.math.BigDecimal;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

public class EditProduct extends javax.swing.JFrame {
    private int mouseX, mouseY;
    private DTOProduct updatedProduct;
    private BusProduct busProduct;
    private JLabel lblTitle;
    private MyPanel panelTitle;
    private MyTextField txtProductID, txtProductName, txtColor, txtBattery, txtSpeed, txtPrice;
    private MyCombobox<String> cmbOperate;
    private MyButton bntupload, bntUpdate, bntReset;
    private JSpinner spinnerQuantity, spinnerWarranty;
    private String image;
    private JPanel panelUpload; // Lưu reference đến panel upload

    public EditProduct() {
        initComponents();
        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE); 
        
        // Thêm mouse listener để di chuyển cửa sổ
        bg.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                mouseX = evt.getX();
                mouseY = evt.getY();
            }
        });

        bg.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            public void mouseDragged(java.awt.event.MouseEvent evt) {
                int x = evt.getXOnScreen();
                int y = evt.getYOnScreen();
                setLocation(x - mouseX, y - mouseY);
            }
        });
        init();
    }
    public void init() {
        // Layout chính: tiêu đề + nội dung
        bg.setLayout(new net.miginfocom.swing.MigLayout("fill, insets 10, wrap", "[grow]", "[][][grow]"));
    
        // Panel tiêu đề
        panelTitle = new MyPanel(new net.miginfocom.swing.MigLayout("fill, insets 0"));
        panelTitle.setGradientColors(Color.decode("#1CB5E0"), Color.decode("#4682B4"), MyPanel.VERTICAL_GRADIENT);
    
        lblTitle = new JLabel("Edit Product Information", JLabel.CENTER);
        lblTitle.setFont(new Font("Arial", Font.BOLD, 22));
        lblTitle.setForeground(Color.WHITE);
        panelTitle.add(lblTitle, "growx, align center");
    
        bg.add(panelTitle, "growx, h 45!, wrap");
    
        // Panel chính với 2 cột: Image Upload + Product Details
        JPanel mainPanel = new JPanel(new MigLayout("fill, insets 0", "[300!][grow]", "[grow]"));
        mainPanel.setBackground(Color.WHITE);
        
        // Panel bên trái: Image Upload
        JPanel imagePanel = createImageUploadPanel();
        mainPanel.add(imagePanel, "growy");
        
        // Panel bên phải: Product Details
        JPanel productPanel = createProductEditPanel();
        mainPanel.add(productPanel, "grow");
        
        bg.add(mainPanel, "grow, pushy");
    }
    
    /**
     * Tạo panel upload ảnh
     */
    private JPanel createImageUploadPanel() {
        JPanel panel = new JPanel(new MigLayout("fill, insets 15, wrap 1", "[grow]", "[][grow][]"));
        panel.setBackground(Color.decode("#F5F5F5"));
        panel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.GRAY, 2), 
                "Product Image", 0, 0, new Font("Arial", Font.BOLD, 14), Color.decode("#1976D2")));
        
        // Label hướng dẫn
        JLabel lblStep1 = new JLabel("<html><b>Current product image:</b></html>");
        lblStep1.setFont(new Font("Arial", Font.PLAIN, 12));
        lblStep1.setForeground(Color.decode("#424242"));
        panel.add(lblStep1, "growx, wrap");
        
        // Panel hiển thị ảnh
        panelUpload = new JPanel(new MigLayout("insets 0, gap 0, fill"));
        panelUpload.setBackground(Color.WHITE);
        panelUpload.setVisible(true);
        panelUpload.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));
        
        JLabel lblUploadImage = new JLabel("No Image");
        lblUploadImage.setFont(new Font("Arial", Font.PLAIN, 14));
        lblUploadImage.setForeground(Color.GRAY);
        lblUploadImage.setHorizontalAlignment(JLabel.CENTER);
        panelUpload.add(lblUploadImage, "pos 0.5al 0.5al");

        panel.add(panelUpload, "grow, wrap");

        // Nút Upload
        bntupload = new MyButton("Upload New Image", 20);
        bntupload.setBackgroundColor(Color.WHITE);
        bntupload.setPressedColor(Color.decode("#D3D3D3"));
        bntupload.setHoverColor(Color.decode("#EEEEEE"));
        bntupload.setFont(new Font("Arial", Font.BOLD, 12));
        bntupload.setForeground(Color.BLACK);
        bntupload.setButtonIcon("src\\main\\resources\\Icons\\Admin_icon\\upload_image.png", 
                                30, 30, 10, SwingConstants.RIGHT, SwingConstants.CENTER);
              
        // Thêm action listener cho upload
        bntupload.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Choose an image");
            fileChooser.setAcceptAllFileFilterUsed(false);
            fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("Images", "jpg", "jpeg", "png"));

            if (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                File selectedFile = fileChooser.getSelectedFile();
                try {
                    File storageDir = new File("D:/Image_Data");
                    if (!storageDir.exists()) {
                        storageDir.mkdirs(); // Tạo thư mục nếu chưa có
                    }

                    // Tạo tên file mới dựa trên tên file gốc
                    String originalFileName = selectedFile.getName();
                    File destFile = new File(storageDir, originalFileName);

                    // Kiểm tra xem file đã tồn tại hay chưa
                    if (destFile.exists()) {
                        // Nếu file đã tồn tại, không thay đổi tên và sử dụng file hiện có
                        image = destFile.getAbsolutePath();  // Sử dụng đường dẫn của file đã tồn tại
                        displayProductImage(destFile.getAbsolutePath());  // Hiển thị ảnh lên GUI
                    } else {
                        // Nếu file chưa tồn tại, sao chép ảnh vào thư mục
                        Files.copy(selectedFile.toPath(), destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                        image = destFile.getAbsolutePath();  // Cập nhật đường dẫn file mới
                        displayProductImage(destFile.getAbsolutePath());  // Hiển thị ảnh lên GUI
                    }
                } catch (IOException ex) {
                    ex.printStackTrace();
                    CustomDialog.showError("Cannot upload this image!");
                }
            }
        });

        panel.add(bntupload, "growx, h 40!");
        
        return panel;
    }
    
    /**
     * Tạo panel chi tiết sản phẩm để edit
     */
    private JPanel createProductEditPanel() {
        JPanel panel = new JPanel(new MigLayout("fill, insets 15, wrap 2", "[right][grow]", "[][][][][][][][][]"));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.GRAY, 2), 
                "Product Information", 0, 0, new Font("Arial", Font.BOLD, 14), Color.decode("#1976D2")));
        
        // Product ID (read-only)
        JLabel lblProductID = new JLabel("Product ID:");
        lblProductID.setFont(new Font("Arial", Font.BOLD, 12));
        panel.add(lblProductID);
        
        txtProductID = makeTextField();
        txtProductID.setEditable(false);
        txtProductID.setBackgroundColor(Color.decode("#F0F0F0"));
        panel.add(txtProductID, "growx, wrap");
        
        // Product Name (editable)
        JLabel lblProductName = new JLabel("Product Name:");
        lblProductName.setFont(new Font("Arial", Font.BOLD, 12));
        panel.add(lblProductName);
        
        txtProductName = makeTextField();
        panel.add(txtProductName, "growx, wrap");
        
        // Color (editable)
        JLabel lblColor = new JLabel("Color:");
        lblColor.setFont(new Font("Arial", Font.BOLD, 12));
        panel.add(lblColor);
        
        txtColor = makeTextField();
        panel.add(txtColor, "growx, wrap");
        
        // Speed (editable)
        JLabel lblSpeed = new JLabel("Speed:");
        lblSpeed.setFont(new Font("Arial", Font.BOLD, 12));
        panel.add(lblSpeed);
        
        txtSpeed = makeTextField();
        panel.add(txtSpeed, "growx, wrap");
        
        // Battery Capacity (editable)
        JLabel lblBattery = new JLabel("Battery Capacity:");
        lblBattery.setFont(new Font("Arial", Font.BOLD, 12));
        panel.add(lblBattery);
        
        txtBattery = makeTextField();
        panel.add(txtBattery, "growx, wrap");
        
        // Price (editable)
        JLabel lblPrice = new JLabel("Selling Price:");
        lblPrice.setFont(new Font("Arial", Font.BOLD, 12));
        panel.add(lblPrice);
        
        txtPrice = makeTextField();
        panel.add(txtPrice, "growx, wrap");
        
        // Quantity (read-only, from warehouse)
        JLabel lblQuantity = new JLabel("Stock Quantity:");
        lblQuantity.setFont(new Font("Arial", Font.BOLD, 12));
        panel.add(lblQuantity);
        
        spinnerQuantity = new JSpinner(new SpinnerNumberModel(1, 0, 1000000, 1));
        spinnerQuantity.setEnabled(false);
        panel.add(spinnerQuantity, "growx, wrap");
        
        // Warranty Months (editable)
        JLabel lblWarranty = new JLabel("Warranty (Months):");
        lblWarranty.setFont(new Font("Arial", Font.BOLD, 12));
        panel.add(lblWarranty);
        
        spinnerWarranty = new JSpinner(new SpinnerNumberModel(12, 1, 120, 1));
        panel.add(spinnerWarranty, "growx, wrap");
        
        // Category (editable)
        JLabel lblCategory = new JLabel("Category:");
        lblCategory.setFont(new Font("Arial", Font.BOLD, 12));
        panel.add(lblCategory);
        
        JPanel categoryPanel = new JPanel(new MigLayout("fill, insets 0"));
        categoryPanel.setBackground(Color.WHITE);
        
        // ComboBox chọn category
        cmbOperate = new MyCombobox<>();
        cmbOperate.setCustomFont(new Font("Arial", Font.PLAIN, 12));
        cmbOperate.setCustomColors(Color.WHITE, Color.GRAY, Color.BLACK);
        try {
            busProduct = new BusProduct();
            java.util.List<com.Admin.category.DTO.DTOCategory> listCategory = busProduct.getAllCategoriesWithSupplier();
            for (com.Admin.category.DTO.DTOCategory dto : listCategory) {
                String categoryID = String.valueOf(dto.getCategoryID());
                String categoryName = dto.getCategoryName();
                cmbOperate.addItem(categoryID + " - " + categoryName);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            CustomDialog.showError("Không tải được danh mục: " + ex.getMessage());
        }
        categoryPanel.add(cmbOperate, "growx");
        panel.add(categoryPanel, "growx, wrap");
        
        // Buttons
        JPanel buttonPanel = new JPanel(new MigLayout("insets 0, gap 10", "[grow][grow]", "[]"));
        buttonPanel.setBackground(Color.WHITE);
        
        bntUpdate = new MyButton("Update Product", 20);
        bntUpdate.setBackgroundColor(Color.decode("#4CAF50"));
        bntUpdate.setPressedColor(Color.decode("#45a049"));
        bntUpdate.setHoverColor(Color.decode("#66bb6a"));
        bntUpdate.setFont(new Font("Arial", Font.BOLD, 14));
        bntUpdate.setForeground(Color.WHITE); 
        buttonPanel.add(bntUpdate, "growx");
          
        bntReset = new MyButton("Reset Form", 20);
        bntReset.setBackgroundColor(Color.WHITE);
        bntReset.setPressedColor(Color.decode("#D3D3D3"));
        bntReset.setHoverColor(Color.decode("#EEEEEE"));
        bntReset.setFont(new Font("Arial", Font.BOLD, 14));
        bntReset.addActionListener(e -> resetForm());
        buttonPanel.add(bntReset, "growx");
          
        // Thêm action listener cho update
        bntUpdate.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                updateProduct();
            }
        });
        
        panel.add(buttonPanel, "span 2, growx, wrap");
        
        return panel;
    }
    
    private MyTextField makeTextField() {
        MyTextField field = new MyTextField();
        field.setBorder(BorderFactory.createLineBorder(Color.GRAY, 2));
        field.setTextFont(new Font("Times New Roman", Font.PLAIN, 15));
        field.setBackgroundColor(Color.decode("#F0FFFF"));
        return field;
    }
    
    /**
     * Logic update product (giữ nguyên từ code cũ)
     */
    private void updateProduct() {
            updatedProduct = null;
            try {
                // Lấy thông tin sản phẩm hiện tại từ database
                DTOProduct currentProduct = busProduct.getProductById(txtProductID.getText());
                if (currentProduct == null) {
                CustomDialog.showError("Không tìm thấy sản phẩm!");
                    return;
                }

                // Tạo đối tượng cập nhật
                updatedProduct = new DTOProduct();

                // Copy tất cả thông tin từ sản phẩm hiện tại vào đối tượng cập nhật
                updatedProduct = new DTOProduct(
                    currentProduct.getProductId(),
                    currentProduct.getProductName(),
                    currentProduct.getColor(),
                    currentProduct.getSpeed(),
                    currentProduct.getBatteryCapacity(),
                    currentProduct.getQuantity(),
                    currentProduct.getCategoryId(),
                    currentProduct.getSupId(),
                    currentProduct.getImage(),
                    currentProduct.getPrice(),
                    currentProduct.getListPriceBefore(),
                    currentProduct.getListPriceAfter(),
                    currentProduct.getWarrantyMonths()
                );

                // Cập nhật các trường thay đổi (nếu có)
                if (!txtProductName.getText().equals(currentProduct.getProductName())) {
                    updatedProduct.setProductName(txtProductName.getText());
                }
                if (!txtPrice.getText().equals(String.valueOf(currentProduct.getPrice()))) {
                    updatedProduct.setPrice(new BigDecimal(txtPrice.getText()));
                }
                String selectedCategory = cmbOperate.getSelectedItem() != null ? 
                    cmbOperate.getSelectedItem().toString().split(" - ")[0] : "";
                if (!selectedCategory.equals(currentProduct.getCategoryId())) {
                    updatedProduct.setCategoryId(selectedCategory);
                }
                if (!txtColor.getText().equals(currentProduct.getColor())) {
                updatedProduct.setColor(txtColor.getText());
                }
                if (!txtBattery.getText().equals(currentProduct.getBatteryCapacity())) {
                updatedProduct.setBatteryCapacity(txtBattery.getText());
                }
                if (!txtSpeed.getText().equals(currentProduct.getSpeed())) {
                updatedProduct.setSpeed(txtSpeed.getText());
                }
                
                // Cập nhật warranty months nếu có thay đổi
                int newWarrantyMonths = (Integer) spinnerWarranty.getValue();
                if (newWarrantyMonths != currentProduct.getWarrantyMonths()) {
                    updatedProduct.setWarrantyMonths(newWarrantyMonths);
                }

                // Cập nhật ảnh mới nếu có
                if (image != null && !image.isEmpty() && !image.equals(currentProduct.getImage())) {
                    updatedProduct.setImage(image);
                }

              try {
                boolean success = busProduct.updateProduct(updatedProduct);
                
                // Thông báo cho tất cả Form_Product biết để refresh
                ProductUpdateNotifier.getInstance().notifyProductUpdated();
                
                // Đóng form edit
                dispose();
                
                if (!success) {
                    throw new Exception("Cập nhật thất bại từ tầng BUS");
                }

                // Hiển thị kết quả
                if (image != null && !image.isEmpty()) {
                    displayProductImage(image);
                }
                
                refreshProductTable();
                
            } catch (Exception busEx) {
                throw new Exception("Lỗi khi gọi BUS: " + busEx.getMessage());
            }
            
        } catch (Exception ex) {
            ex.printStackTrace();
             CustomDialog.showError("An error occurred while updating the product.");
        }
    }
    
    /**
     * Refresh product table after update
     */
        private void refreshProductTable() {
           SwingUtilities.invokeLater(() -> {
               Form_Product productForm = getProductFormInstance();
               if (productForm != null && productForm.tableProduct != null) {
                   DefaultTableModel model = (DefaultTableModel) productForm.tableProduct.getModel();
                   model.setRowCount(0); // Xóa dữ liệu cũ

                   // Load lại toàn bộ dữ liệu
                   busProduct.uploadProduct(model);

                   // Căn chỉnh cột và làm mới hiển thị
                   productForm.tableProduct.adjustColumnWidths();
                   productForm.tableProduct.revalidate();
                   productForm.tableProduct.repaint();

                   // Tìm và chọn lại dòng vừa cập nhật
                   selectUpdatedRow(productForm, updatedProduct.getProductId());
               }
           });
       }

       private void selectUpdatedRow(Form_Product form, String productId) {
           for (int i = 0; i < form.tableProduct.getRowCount(); i++) {
               if (form.tableProduct.getValueAt(i, 0).equals(productId)) {
                   form.tableProduct.setRowSelectionInterval(i, i);
                   form.tableProduct.scrollRectToVisible(form.tableProduct.getCellRect(i, 0, true));
                   break;
               }
           }
       }

       // Hàm lấy instance Form_Product
       private Form_Product getProductFormInstance() {
           return new Form_Product();
    }
    
    
    public void showDetail(DTOProduct product) {
        txtProductID.setText(product.getProductId());
        txtProductName.setText(product.getProductName());
        txtColor.setText(product.getColor());
        txtBattery.setText(product.getBatteryCapacity());
        txtSpeed.setText(product.getSpeed());
        txtPrice.setText(String.valueOf(product.getPrice()));
        spinnerQuantity.setValue(product.getQuantity());
        spinnerWarranty.setValue(product.getWarrantyMonths());
        
        // Tìm và chọn category trong ComboBox
        String categoryId = product.getCategoryId();
        for (int i = 0; i < cmbOperate.getItemCount(); i++) {
            String item = cmbOperate.getItemAt(i);
            if (item.startsWith(categoryId + " - ")) {
                cmbOperate.setSelectedIndex(i);
                break;
            }
        }

        // Hiển thị ảnh sản phẩm
        if (product.getImage() != null && !product.getImage().isEmpty()) {
            displayProductImage(product.getImage());
        }
    }
    
    private void resetForm() {
        txtProductID.setText("");
        txtProductName.setText("");
        txtColor.setText("");
        txtBattery.setText("");
        txtSpeed.setText("");
        txtPrice.setText("");
        spinnerQuantity.setValue(1);
        spinnerWarranty.setValue(12);
        image = null;
        if (cmbOperate != null) cmbOperate.setSelectedIndex(0);
        
        // Reset nút upload
        bntupload.setText("Upload New Image");
        bntupload.setBackgroundColor(Color.WHITE);
        bntupload.setForeground(Color.BLACK);
    }
    
    public void displayProductImage(String imagePath) {
        SwingUtilities.invokeLater(() -> {
            try {
                // Kiểm tra đường dẫn ảnh rỗng
                if (imagePath == null || imagePath.trim().isEmpty()) {
                    showDefaultPlaceholder();
                    return;
                }

                // Kiểm tra file ảnh tồn tại
                File imageFile = new File(imagePath);
                if (!imageFile.exists()) {
                    System.out.println("Ảnh không tồn tại tại: " + imagePath);
                    showDefaultPlaceholder();
                    return;
                }

                // Đọc ảnh với nhiều định dạng
                BufferedImage originalImage = null;
                try {
                    originalImage = ImageIO.read(imageFile);
                } catch (IOException e) {
                    System.err.println("Không thể đọc file ảnh: " + e.getMessage());
                    showDefaultPlaceholder();
                    return;
                }

                if (originalImage == null) {
                    showDefaultPlaceholder();
                    return;
                }

                // Scale ảnh
                int maxWidth = 230;
                int maxHeight = 230;
                int originalWidth = originalImage.getWidth();
                int originalHeight = originalImage.getHeight();

                double scaleFactor = Math.min(
                    (double) maxWidth / originalWidth,
                    (double) maxHeight / originalHeight
                );

                int newWidth = (int) (originalWidth * scaleFactor);
                int newHeight = (int) (originalHeight * scaleFactor);

                Image scaledImage = originalImage.getScaledInstance(newWidth, newHeight, Image.SCALE_SMOOTH);
                ImageIcon icon = new ImageIcon(scaledImage);

                // Hiển thị ảnh
                updateImagePanel(icon);

            } catch (Exception e) {
                e.printStackTrace();
                showDefaultPlaceholder();
            }
        });
    }

    private void showDefaultPlaceholder() {
        // Tạo ảnh placeholder đơn giản
        BufferedImage placeholder = new BufferedImage(230, 230, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = placeholder.createGraphics();
        g2d.setColor(Color.LIGHT_GRAY);
        g2d.fillRect(0, 0, 230, 230);
        g2d.setColor(Color.DARK_GRAY);
        g2d.drawString("No Image", 80, 120);
        g2d.dispose();

        updateImagePanel(new ImageIcon(placeholder));
    }

    private void updateImagePanel(ImageIcon icon) {
        if (panelUpload != null) {
            panelUpload.removeAll();
            panelUpload.setLayout(new MigLayout("insets 0, gap 0, fill"));

            JLabel imageLabel = new JLabel(icon);
            imageLabel.setHorizontalAlignment(JLabel.CENTER);
            imageLabel.setVerticalAlignment(JLabel.CENTER);

            panelUpload.add(imageLabel, "pos 0.5al 0.5al");
            panelUpload.setVisible(true);
            panelUpload.revalidate();
            panelUpload.repaint();
        }
    }
    
    private void initComponents() {
        jMenuItem1 = new javax.swing.JMenuItem();
        bg = new javax.swing.JLayeredPane();

        jMenuItem1.setText("jMenuItem1");
        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        bg.setBackground(new java.awt.Color(255, 255, 255));
        bg.setOpaque(true);

        javax.swing.GroupLayout bgLayout = new javax.swing.GroupLayout(bg);
        bg.setLayout(bgLayout);
        bgLayout.setHorizontalGroup(
                bgLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGap(0, 1414, Short.MAX_VALUE)
        );
        bgLayout.setVerticalGroup(
                bgLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGap(0, 483, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(bg)
        );
        layout.setVerticalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(bg)
        );

        pack();
        setLocationRelativeTo(null);
    }
    
    // Variables declaration - do not modify                     
    private javax.swing.JLayeredPane bg;
    private javax.swing.JMenuItem jMenuItem1;
    // End of variables declaration                   
}

