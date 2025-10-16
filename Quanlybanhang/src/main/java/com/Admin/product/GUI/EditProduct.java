package com.Admin.product.GUI;
import com.ComponentandDatabase.Components.MyPanel;
import com.ComponentandDatabase.Components.MyButton;
import com.ComponentandDatabase.Components.CustomDialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.Graphics2D;
import com.Admin.product.BUS.BusProduct;
import com.Admin.product.DTO.DTOProduct;
import java.util.List;
import java.util.Map;
import javax.imageio.ImageIO;
import java.util.LinkedHashMap;
import java.math.BigDecimal;
import com.Admin.category.DTO.DTOCategory;
import java.awt.Color;
import javax.swing.ImageIcon;
import java.awt.Image;
import java.awt.image.BufferedImage;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.nio.file.StandardCopyOption;

import net.miginfocom.swing.MigLayout;  // Layout manager chính
         // (Tùy chọn) Nếu cần các constraint nâng cao
import javax.swing.JMenuBar;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.JPanel;
import java.awt.Font;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import javax.swing.JDialog;
import javax.swing.JTable;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.JMenuItem;
import javax.swing.JMenu;
import javax.swing.JTextField;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class EditProduct extends javax.swing.JFrame {
     private int mouseX, mouseY;
     private DTOProduct updatedProduct;
     private String currentImagePath = null; 
     private BusProduct busProduct;
     

    public EditProduct() {
        initComponents();
        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE); 
        
        // Thêm mouse listener để di chuyển cửa sổ
        mainPanel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                mouseX = evt.getX();
                mouseY = evt.getY();
            }
        });

        mainPanel.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            public void mouseDragged(java.awt.event.MouseEvent evt) {
                int x = evt.getXOnScreen();
                int y = evt.getYOnScreen();
                setLocation(x - mouseX, y - mouseY);
            }
        });
        init();
    }
        public void init() {
                // Thiết lập MigLayout cho panel chính với khả năng co giãn
          mainPanel.setLayout(new MigLayout("insets 0, fill", "[grow]", "[40!][90!][grow]"));

          // 1. Panel tiêu đề (tự động co giãn theo chiều ngang)
          panelTitle = new MyPanel(new MigLayout("fill, insets 0"));
          panelTitle.setGradientColors(Color.decode("#1CB5E0"), Color.decode("#4682B4"), MyPanel.VERTICAL_GRADIENT);

          lblTitle = new JLabel("Edit Product Information", JLabel.CENTER);
          lblTitle.setFont(new Font("Arial", Font.BOLD, 20));
          lblTitle.setForeground(Color.WHITE);

          panelTitle.add(lblTitle, "grow, push, align center");
          mainPanel.add(panelTitle, "growx, h 40!, wrap");
          
          // Panel hướng dẫn
          JPanel instructionPanel = new JPanel(new MigLayout("insets 10, fill"));
          instructionPanel.setBackground(Color.decode("#FFF3E0"));
          instructionPanel.setBorder(BorderFactory.createLineBorder(Color.decode("#FF9800"), 1));
          
          JLabel lblInstruction = new JLabel("<html><b>📝 Edit Instructions:</b><br>" +
                  "• <b>Product ID:</b> Cannot be changed (locked)<br>" +
                  "• <b>Basic Info:</b> Product Name, Category, Stock (from warehouse)<br>" +
                  "• <b>Product Details:</b> Color, Speed, Battery, Selling Price (editable)<br>" +
                  "• <b>Image:</b> Upload new product image if needed</html>");
          lblInstruction.setFont(new Font("Arial", Font.PLAIN, 12));
          lblInstruction.setForeground(Color.decode("#E65100"));
          instructionPanel.add(lblInstruction, "growx");
          
          mainPanel.add(instructionPanel, "growx, h 90!, wrap");
          
          // Panel chính với 2 cột: Image Upload + Product Details
          JPanel contentPanel = new JPanel(new MigLayout("fill, insets 0", "[250!][grow]", "[grow]"));
          contentPanel.setBackground(Color.WHITE);
          
          // Panel bên trái: Image Upload
          JPanel imagePanel = createImageUploadPanel();
          contentPanel.add(imagePanel, "growy");
          
          // Panel bên phải: Product Details
          JPanel productPanel = createProductEditPanel();
          contentPanel.add(productPanel, "grow");
          
          mainPanel.add(contentPanel, "grow, pushy");
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
              
        // Thêm action listener cho upload (giữ nguyên logic cũ)
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
                        new CustomDialog().showError("Cannot upload this image!");
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
        
        txtProductID = new JTextField();
        txtProductID.setEditable(false);
        txtProductID.setBorder(BorderFactory.createLineBorder(Color.GRAY, 2));
        txtProductID.setFont(new Font("Arial", Font.PLAIN, 12));
        txtProductID.setBackground(Color.decode("#F0F0F0"));
        panel.add(txtProductID, "growx, wrap");
        
        // Product Name (editable)
        JLabel lblProductName = new JLabel("Product Name:");
        lblProductName.setFont(new Font("Arial", Font.BOLD, 12));
        panel.add(lblProductName);
        
        txtProductName = new JTextField();
        txtProductName.setBorder(BorderFactory.createLineBorder(Color.GRAY, 2));
        txtProductName.setFont(new Font("Arial", Font.PLAIN, 12));
        panel.add(txtProductName, "growx, wrap");
        
        // Color (editable)
        JLabel lblColor = new JLabel("Color:");
        lblColor.setFont(new Font("Arial", Font.BOLD, 12));
        panel.add(lblColor);
        
        txtColor = new JTextField();
        txtColor.setBorder(BorderFactory.createLineBorder(Color.GRAY, 2));
        txtColor.setFont(new Font("Arial", Font.PLAIN, 12));
        panel.add(txtColor, "growx, wrap");
        
        // Speed (editable)
        JLabel lblSpeed = new JLabel("Speed:");
        lblSpeed.setFont(new Font("Arial", Font.BOLD, 12));
        panel.add(lblSpeed);
        
        txtSpeed = new JTextField();
        txtSpeed.setBorder(BorderFactory.createLineBorder(Color.GRAY, 2));
        txtSpeed.setFont(new Font("Arial", Font.PLAIN, 12));
        panel.add(txtSpeed, "growx, wrap");
        
        // Battery Capacity (editable)
        JLabel lblBattery = new JLabel("Battery Capacity:");
        lblBattery.setFont(new Font("Arial", Font.BOLD, 12));
        panel.add(lblBattery);
        
        txtBattery = new JTextField();
        txtBattery.setBorder(BorderFactory.createLineBorder(Color.GRAY, 2));
        txtBattery.setFont(new Font("Arial", Font.PLAIN, 12));
        panel.add(txtBattery, "growx, wrap");
        
        // Price (editable)
        JLabel lblPrice = new JLabel("Selling Price:");
        lblPrice.setFont(new Font("Arial", Font.BOLD, 12));
        panel.add(lblPrice);
        
        txtPrice = new JTextField();
        txtPrice.setBorder(BorderFactory.createLineBorder(Color.GRAY, 2));
        txtPrice.setFont(new Font("Arial", Font.PLAIN, 12));
        panel.add(txtPrice, "growx, wrap");
        
        // Quantity (read-only, from warehouse)
        JLabel lblQuantity = new JLabel("Stock Quantity:");
        lblQuantity.setFont(new Font("Arial", Font.BOLD, 12));
        panel.add(lblQuantity);
        
        spinnerQuantity = new JSpinner(new SpinnerNumberModel(1, 0, 1000000, 1));
        spinnerQuantity.setEnabled(false);
        panel.add(spinnerQuantity, "growx, wrap");
        
        // Category (editable)
        JLabel lblCategory = new JLabel("Category:");
        lblCategory.setFont(new Font("Arial", Font.BOLD, 12));
        panel.add(lblCategory);
        
        JPanel categoryPanel = new JPanel(new MigLayout("fill, insets 0"));
        categoryPanel.setBackground(Color.WHITE);
        
        // Menu chọn category
            JMenuBar menuBar = new JMenuBar();
        menu = new JMenu("Choose Category");
        menu.setFont(new Font("Arial", Font.PLAIN, 12));

        try {
            busProduct = new BusProduct();
            List<DTOCategory> listCategory = busProduct.getAllCategoriesWithSupplier();
            Map<String, JMenu> supplierMenuMap = new LinkedHashMap<>();

            for (DTOCategory dto : listCategory) {
                String supplierID = dto.getSupID();
                String supplierName = dto.getSupName();
                String categoryID = dto.getCategoryID();

                if (!supplierMenuMap.containsKey(supplierID)) {
                    JMenu supplierMenu = new JMenu(supplierID);
                    supplierMenu.setFont(new Font("Arial", Font.PLAIN, 11));
                    supplierMenuMap.put(supplierID, supplierMenu);
                    menu.add(supplierMenu);
                }

                JMenuItem categoryItem = new JMenuItem(categoryID);
                supplierMenuMap.get(supplierID).add(categoryItem);

                categoryItem.addActionListener(e -> {
                    menu.setText(categoryID);
                });
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            CustomDialog.showError("Không tải được danh mục: " + ex.getMessage());
            }

            menuBar.add(menu);
        categoryPanel.add(menuBar, "growx");
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
          
        // Thêm action listener cho update (giữ nguyên logic cũ)
       bntUpdate.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
                // Logic update cũ (sẽ được giữ nguyên)
                updateProduct();
            }
        });
        
        panel.add(buttonPanel, "span 2, growx, wrap");
        
        return panel;
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
                    currentProduct.getListPriceAfter()
                );

                // Cập nhật các trường thay đổi (nếu có)
                if (!txtProductName.getText().equals(currentProduct.getProductName())) {
                    updatedProduct.setProductName(txtProductName.getText());
                }
                if (!txtPrice.getText().equals(String.valueOf(currentProduct.getPrice()))) {
                    updatedProduct.setPrice(new BigDecimal(txtPrice.getText()));
                }
                if (!menu.getText().equals(currentProduct.getCategoryId())) {
                    updatedProduct.setCategoryId(menu.getText());
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
           return this.product != null ? this.product : new Form_Product();
    }
    
    /**
     * Browse warehouse items for editing (chỉ hiển thị, không cho chọn)
     */
    private void browseWarehouseItemsForEdit() {
        // Tạo dialog để hiển thị danh sách warehouse items
        JDialog browseDialog = new JDialog(this, "View Warehouse Items", true);
        browseDialog.setSize(800, 600);
        browseDialog.setLocationRelativeTo(this);
        
        // Tạo bảng hiển thị warehouse items
        String[] columnNames = {"Warehouse ID", "Product Name", "Category", "Supplier", "Stock", "Import Price"};
        DefaultTableModel model = new DefaultTableModel(columnNames, 0);
        JTable table = new JTable(model);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        // Load dữ liệu warehouse
        try {
            // Sử dụng BUSInventory để lấy dữ liệu warehouse
            com.Admin.inventory.BUS.BUSInventory busInventory = new com.Admin.inventory.BUS.BUSInventory();
            busInventory.loadInventoryToTable(model);
        } catch (Exception e) {
            e.printStackTrace();
            CustomDialog.showError("Error loading warehouse data: " + e.getMessage());
            return;
        }
        
        JScrollPane scrollPane = new JScrollPane(table);
        browseDialog.add(scrollPane, BorderLayout.CENTER);
        
        // Panel nút
        JPanel buttonPanel = new JPanel(new FlowLayout());
        MyButton btnClose = new MyButton("Close", 20);
        btnClose.setBackgroundColor(Color.decode("#f44336"));
        btnClose.setForeground(Color.WHITE);
        btnClose.addActionListener(e -> browseDialog.dispose());
        
        buttonPanel.add(btnClose);
        browseDialog.add(buttonPanel, BorderLayout.SOUTH);
        
        browseDialog.setVisible(true);
    }
    
    public void showDetail(DTOProduct product) {
        txtProductID.setText(product.getProductId());
        txtProductName.setText(product.getProductName());
        txtColor.setText(product.getColor());
        txtBattery.setText(product.getBatteryCapacity());
        txtSpeed.setText(product.getSpeed());
        txtPrice.setText(String.valueOf(product.getPrice()));
        spinnerQuantity.setValue(product.getQuantity());
        menu.setText(product.getCategoryId());

        // Hiển thị ảnh sản phẩm
        if (product.getImage() != null && !product.getImage().isEmpty()) {
            displayProductImage(product.getImage());
        }
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
    
    private void initComponents() {
        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Edit Product");
        setResizable(false);
        
        // Tạo main panel
        mainPanel = new JPanel();
        mainPanel.setBackground(Color.WHITE);
        
        // Thiết lập layout cho frame
        setLayout(new BorderLayout());
        add(mainPanel, BorderLayout.CENTER);
        
        pack();
        setLocationRelativeTo(null);
    }
    
    // Variables declaration - do not modify                     
    private javax.swing.JPanel mainPanel;
    private javax.swing.JTextField txtProductID;
    private javax.swing.JTextField txtProductName;
    private javax.swing.JTextField txtPrice;
    private javax.swing.JTextField txtColor;
    private javax.swing.JTextField txtBattery;
    private javax.swing.JTextField txtSpeed;
    private javax.swing.JSpinner spinnerQuantity;
    private javax.swing.JMenu menu;
    private javax.swing.JPanel panelUpload;
    private MyPanel panelTitle;
    private javax.swing.JLabel lblTitle;
    private MyButton bntupload;
    private MyButton bntUpdate;
    private String image;
    private Form_Product product;
    // End of variables declaration                   
}

