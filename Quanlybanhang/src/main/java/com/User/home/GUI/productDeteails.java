
package com.User.home.GUI;

import com.ComponentandDatabase.Components.MyButton;
import com.ComponentandDatabase.Components.MyCombobox;
import com.ComponentandDatabase.Components.MyPanel;
import com.ComponentandDatabase.Components.MyTextField;
import com.User.dashboard_user.BUS.BUSProfile_cus;
import com.User.home.DTO.productDTO;
import com.User.Cart.DTO.DTOCart;
import com.User.dashboard_user.GUI.MyProfile_cus;
import com.User.dashboard_user.GUI.Dashboard_user;
import com.User.Cart.GUI.Form_Cart;
import com.ComponentandDatabase.Components.CustomDialog;
import com.User.home.BUS.productBUS;
import com.User.Cart.BUS.BUSCart;
import com.toedter.calendar.JDateChooser;
import java.awt.Color;
import java.awt.Toolkit;
import javax.swing.SwingWorker;
import javax.swing.SwingUtilities;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.awt.Image;
import net.coobird.thumbnailator.Thumbnails;
import net.coobird.thumbnailator.geometry.Positions;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Component;
import javax.swing.JFileChooser;
import javax.swing.ImageIcon;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.File;
import java.io.IOException;
import javax.swing.JButton;
import javax.swing.JComponent;
import java.nio.file.Files;
import javax.swing.BorderFactory;
import javax.swing.JFormattedTextField;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import net.miginfocom.swing.MigLayout;

public class productDeteails extends javax.swing.JFrame {
     public JLabel lblTitle, lblID, lblProductName, lblColor, lblBatteryCapacity, lblSpeed, lblWarranty, lblCateID
             , lblBrand, lblCountry, lblQuantity;
     public MyPanel panelTitle;
     public MyCombobox cmbGender;
     public static MyTextField txtID, txtProductName, txtColor, txtBatteryCapacity, txtSpeed, txtWarranty, txtCateID, txtBrand, txtCountry, txtPrice, txtQuantity;
     private JDateChooser dateOfBirth;
     private JTextArea txtAddress;
     public JPanel panelUpload;
     public MyButton bntUpload, bntAddcart;
     public JSpinner spinnerQuantity;
     private BUSProfile_cus busProfile;
     private productBUS busProduct;
     private BUSCart busCart;
     private MyProfile_cus profile;
    
    public productDeteails() {
        initComponents();
        setSize(800, 750); // Tăng kích thước để hiển thị đầy đủ các trường mới
        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE); 

        // Tính toán vị trí để căn giữa và trên cùng
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int x = (screenSize.width - getWidth()) / 2;
        setLocation(x, 0); // y = 0 để nằm trên cùng

        setAlwaysOnTop(true); // Luôn hiển thị trên cùng
        init();
    }
    private static List<CartUpdateListener> listeners = new ArrayList<>();

    public static void addCartUpdateListener(CartUpdateListener listener) {
        listeners.add(listener);
    }

    public static void removeCartUpdateListener(CartUpdateListener listener) {
        listeners.remove(listener);
    }

    private void fireCartUpdatedEvent(String customerID) {
        for (CartUpdateListener listener : listeners) {
            listener.onCartUpdated(customerID);
        }
    }
    
    /**
     * Static method để fire cart update event từ bên ngoài class
     * Được sử dụng bởi Home_user và các class khác
     */
    public static void fireCartUpdatedEventStatic(String customerID) {
        for (CartUpdateListener listener : listeners) {
            listener.onCartUpdated(customerID);
        }
    }
    
    public void init() {
     // Thiết lập layout chính
     bg.setLayout(new MigLayout("fillx, insets 0", "[grow]", "[][][grow]"));

     // 1. Panel tiêu đề với design đẹp
     panelTitle = new MyPanel(new MigLayout("fill, insets 10"));
     panelTitle.setGradientColors(Color.decode("#2196F3"), Color.decode("#1976D2"), MyPanel.VERTICAL_GRADIENT);
     panelTitle.setBorder(BorderFactory.createCompoundBorder(
         BorderFactory.createLineBorder(Color.decode("#1976D2"), 2),
         BorderFactory.createEmptyBorder(10, 15, 10, 15)
     ));

     lblTitle = new JLabel("Product Details", JLabel.CENTER);
     lblTitle.setFont(new Font("Arial", Font.BOLD, 24));
     lblTitle.setForeground(Color.WHITE);

     panelTitle.add(lblTitle, "grow, push, align center");
     bg.add(panelTitle, "growx, h 60!, wrap"); // Tăng chiều cao

        // Panel upload ảnh với design đẹp
    panelUpload = new JPanel();
    panelUpload.setLayout(new MigLayout("fill, insets 10"));
    panelUpload.setBackground(Color.WHITE);
    panelUpload.setPreferredSize(new Dimension(250, 250));
    panelUpload.setBorder(BorderFactory.createCompoundBorder(
        BorderFactory.createLineBorder(Color.decode("#E0E0E0"), 2),
        BorderFactory.createEmptyBorder(10, 10, 10, 10)
    ));

    // Thêm vào bg
    bg.add(panelUpload, "w 250!, h 250!, gap 10, align center, wrap");

     // Labels với style đẹp - sắp xếp theo 2 cột, di chuyển xuống dưới hình
     // Cột trái
     lblID= new JLabel("Product ID:");
     lblID.setFont(new Font("Arial", Font.BOLD, 14));
     lblID.setForeground(Color.decode("#1976D2"));
     bg.add(lblID, "pos 30 350, w 140!, h 30!");
  
    lblProductName= new JLabel("Product Name:");
    lblProductName.setFont(new Font("Arial", Font.BOLD, 14));
    lblProductName.setForeground(Color.decode("#1976D2"));
    bg.add(lblProductName, "pos 30 390, w 140!, h 30!");
     
     lblColor= new JLabel("Color:");
     lblColor.setFont(new Font("Arial", Font.BOLD, 14));
     lblColor.setForeground(Color.decode("#1976D2"));
     bg.add(lblColor, "pos 30 430, w 140!, h 30!");
     
     lblBatteryCapacity= new JLabel("Battery Capacity:");
     lblBatteryCapacity.setFont(new Font("Arial", Font.BOLD, 14));
     lblBatteryCapacity.setForeground(Color.decode("#1976D2"));
     bg.add(lblBatteryCapacity, "pos 30 470, w 140!, h 30!");
     
     lblSpeed= new JLabel("Max Speed:");
     lblSpeed.setFont(new Font("Arial", Font.BOLD, 14));
     lblSpeed.setForeground(Color.decode("#1976D2"));
     bg.add(lblSpeed, "pos 30 510, w 140!, h 30!");
     
     lblWarranty= new JLabel("Warranty (Months):");
     lblWarranty.setFont(new Font("Arial", Font.BOLD, 14));
     lblWarranty.setForeground(Color.decode("#1976D2"));
     bg.add(lblWarranty, "pos 30 550, w 140!, h 30!");
     
     // Cột phải
     lblCateID= new JLabel("Category ID:");
     lblCateID.setFont(new Font("Arial", Font.BOLD, 14));
     lblCateID.setForeground(Color.decode("#1976D2"));
     bg.add(lblCateID, "pos 400 350, w 140!, h 30!");
     
     lblBrand= new JLabel("Supplier:");
     lblBrand.setFont(new Font("Arial", Font.BOLD, 14));
     lblBrand.setForeground(Color.decode("#1976D2"));
     bg.add(lblBrand, "pos 400 390, w 140!, h 30!");
     
     lblQuantity= new JLabel("Quantity:");
     lblQuantity.setFont(new Font("Arial", Font.BOLD, 14));
     lblQuantity.setForeground(Color.decode("#1976D2"));
     bg.add(lblQuantity, "pos 400 430, w 140!, h 30!");
     
     // Only show Price (not Original Price, Promo Price)
     JLabel lblPrice = new JLabel("Price:");
     lblPrice.setFont(new Font("Arial", Font.BOLD, 14));
     lblPrice.setForeground(Color.decode("#1976D2"));
     bg.add(lblPrice, "pos 400 470, w 170!, h 30!");
     
     
     
     txtID = new MyTextField();
     txtID.setBorder(BorderFactory.createCompoundBorder(
         BorderFactory.createLineBorder(Color.decode("#E0E0E0"), 1),
         BorderFactory.createEmptyBorder(5, 10, 5, 10)
     ));
     txtID.setTextColor(Color.decode("#333333"));
     txtID.setLocked(true);
     txtID.setTextFont(new Font("Arial", Font.PLAIN, 14));
     txtID.setBackgroundColor(Color.WHITE);

     bg.add(txtID, "pos 180 350, w 150!, h 35!");
     
     txtProductName = new MyTextField();
     txtProductName.setBorder(BorderFactory.createCompoundBorder(
         BorderFactory.createLineBorder(Color.decode("#E0E0E0"), 1),
         BorderFactory.createEmptyBorder(5, 10, 5, 10)
     ));
     txtProductName.setTextColor(Color.decode("#333333"));
     txtProductName.setLocked(true);
     txtProductName.setTextFont(new Font("Arial", Font.PLAIN, 14));
     txtProductName.setBackgroundColor(Color.WHITE);

     bg.add(txtProductName, "pos 180 390, w 150!, h 35!");
     
     
     // Cột trái - Text fields
     txtColor = new MyTextField();
     txtColor.setBorder(BorderFactory.createCompoundBorder(
         BorderFactory.createLineBorder(Color.decode("#E0E0E0"), 1),
         BorderFactory.createEmptyBorder(5, 10, 5, 10)
     ));
     txtColor.setTextColor(Color.decode("#333333"));
     txtColor.setLocked(true);
     txtColor.setTextFont(new Font("Arial", Font.PLAIN, 14));
     txtColor.setBackgroundColor(Color.WHITE);
     bg.add(txtColor, "pos 180 430, w 150!, h 35!");
     
     txtBatteryCapacity = new MyTextField();
     txtBatteryCapacity.setBorder(BorderFactory.createCompoundBorder(
         BorderFactory.createLineBorder(Color.decode("#E0E0E0"), 1),
         BorderFactory.createEmptyBorder(5, 10, 5, 10)
     ));
     txtBatteryCapacity.setTextColor(Color.decode("#333333"));
     txtBatteryCapacity.setLocked(true);
     txtBatteryCapacity.setTextFont(new Font("Arial", Font.PLAIN, 14));
     txtBatteryCapacity.setBackgroundColor(Color.WHITE);
     bg.add(txtBatteryCapacity, "pos 180 470, w 150!, h 35!");
     
     txtSpeed = new MyTextField();
     txtSpeed.setBorder(BorderFactory.createCompoundBorder(
         BorderFactory.createLineBorder(Color.decode("#E0E0E0"), 1),
         BorderFactory.createEmptyBorder(5, 10, 5, 10)
     ));
     txtSpeed.setTextColor(Color.decode("#333333"));
     txtSpeed.setLocked(true);
     txtSpeed.setTextFont(new Font("Arial", Font.PLAIN, 14));
     txtSpeed.setBackgroundColor(Color.WHITE);
     bg.add(txtSpeed, "pos 180 510, w 150!, h 35!");
     
     txtWarranty = new MyTextField();
     txtWarranty.setBorder(BorderFactory.createCompoundBorder(
         BorderFactory.createLineBorder(Color.decode("#E0E0E0"), 1),
         BorderFactory.createEmptyBorder(5, 10, 5, 10)
     ));
     txtWarranty.setTextColor(Color.decode("#333333"));
     txtWarranty.setLocked(true);
     txtWarranty.setTextFont(new Font("Arial", Font.PLAIN, 14));
     txtWarranty.setBackgroundColor(Color.WHITE);
     bg.add(txtWarranty, "pos 180 550, w 150!, h 35!");
     
     // Cột phải - Text fields
     txtCateID = new MyTextField();
     txtCateID.setBorder(BorderFactory.createCompoundBorder(
         BorderFactory.createLineBorder(Color.decode("#E0E0E0"), 1),
         BorderFactory.createEmptyBorder(5, 10, 5, 10)
     ));
     txtCateID.setTextColor(Color.decode("#333333"));
     txtCateID.setLocked(true);
     txtCateID.setTextFont(new Font("Arial", Font.PLAIN, 14));
     txtCateID.setBackgroundColor(Color.WHITE);
     bg.add(txtCateID, "pos 550 350, w 150!, h 35!");
     
     txtBrand = new MyTextField();
     txtBrand.setBorder(BorderFactory.createCompoundBorder(
         BorderFactory.createLineBorder(Color.decode("#E0E0E0"), 1),
         BorderFactory.createEmptyBorder(5, 10, 5, 10)
     ));
     txtBrand.setTextColor(Color.decode("#333333"));
     txtBrand.setLocked(true);
     txtBrand.setTextFont(new Font("Arial", Font.PLAIN, 14));
     txtBrand.setBackgroundColor(Color.WHITE);
     bg.add(txtBrand, "pos 550 390, w 150!, h 35!");
     
     // Country (Origin) - Fixed position to avoid overlap with Quantity
     lblCountry = new JLabel("Origin:");
     lblCountry.setFont(new Font("Arial", Font.BOLD, 14));
     lblCountry.setForeground(Color.decode("#1976D2"));
     bg.add(lblCountry, "pos 400 510, w 140!, h 30!");
     
     txtCountry = new MyTextField();
     txtCountry.setBorder(BorderFactory.createCompoundBorder(
         BorderFactory.createLineBorder(Color.decode("#E0E0E0"), 1),
         BorderFactory.createEmptyBorder(5, 10, 5, 10)
     ));
     txtCountry.setTextColor(Color.decode("#333333"));
     txtCountry.setLocked(true);
     txtCountry.setTextFont(new Font("Arial", Font.PLAIN, 14));
     txtCountry.setBackgroundColor(Color.WHITE);
     bg.add(txtCountry, "pos 550 510, w 150!, h 35!");
     
     // Text field cho Quantity - Fixed position
     txtQuantity = new MyTextField();
     txtQuantity.setBorder(BorderFactory.createCompoundBorder(
         BorderFactory.createLineBorder(Color.decode("#E0E0E0"), 1),
         BorderFactory.createEmptyBorder(5, 10, 5, 10)
     ));
     txtQuantity.setTextColor(Color.decode("#333333"));
     txtQuantity.setLocked(true);
     txtQuantity.setTextFont(new Font("Arial", Font.PLAIN, 14));
     txtQuantity.setBackgroundColor(Color.WHITE);
     bg.add(txtQuantity, "pos 550 430, w 150!, h 35!");
     
     // Text field cho Price (only Price, not Original or Promo)
     txtPrice = new MyTextField();
     txtPrice.setBorder(BorderFactory.createCompoundBorder(
         BorderFactory.createLineBorder(Color.decode("#E0E0E0"), 1),
         BorderFactory.createEmptyBorder(5, 10, 5, 10)
     ));
     txtPrice.setTextColor(Color.decode("#D32F2F"));
     txtPrice.setLocked(true);
     txtPrice.setTextFont(new Font("Arial", Font.BOLD, 14));
     txtPrice.setBackgroundColor(Color.WHITE);
     bg.add(txtPrice, "pos 550 470, w 220!, h 35!"); 
     
     // Spinner cho Add to Cart - di chuyển xuống dưới
     SpinnerNumberModel quantityModel = new SpinnerNumberModel(1, 0, 1000, 1);
     spinnerQuantity = new JSpinner(quantityModel);
     JComponent editor = spinnerQuantity.getEditor();
     JFormattedTextField textField = ((JSpinner.DefaultEditor) editor).getTextField();
     textField.setFont(new Font("Arial", Font.PLAIN, 14));
     textField.setBackground(Color.WHITE);
     textField.setBorder(BorderFactory.createCompoundBorder(
         BorderFactory.createLineBorder(Color.decode("#E0E0E0"), 1),
         BorderFactory.createEmptyBorder(5, 10, 5, 10)
     ));
     bg.add(spinnerQuantity, "pos 30 640, w 80!, h 35!");
     
     
     bntAddcart = new MyButton("Add to Cart", 20);
     bntAddcart.setBackgroundColor(Color.decode("#4CAF50"));
     bntAddcart.setPressedColor(Color.decode("#45A049"));
     bntAddcart.setHoverColor(Color.decode("#66BB6A"));
     bntAddcart.setFont(new Font("Arial", Font.BOLD, 14));
     bntAddcart.setForeground(Color.WHITE);
     bntAddcart.setButtonIcon("/Icons/User_icon/cart.png", 20, 20, 5, SwingConstants.RIGHT, SwingConstants.CENTER);   
     // Không thêm ActionListener ở đây - sẽ được thêm trong displayProductDetails()
     bg.add(bntAddcart, "w 160!, h 35!, pos 200 640, align center, gapbottom 15, gaptop 20");
 }
    
    public void displayProductDetails(productDTO product) {
        busProduct = new productBUS();
        panelUpload.removeAll();
        panelUpload.setLayout(new MigLayout("fill, insets 0"));

        if (product.getImage() != null && !product.getImage().isEmpty()) {
            try {
                BufferedImage originalImage = Thumbnails.of(new File(product.getImage()))
                    .scale(1)
                    .asBufferedImage();

                int targetWidth = panelUpload.getWidth() - 20;
                int targetHeight = panelUpload.getHeight() - 20;

                double widthRatio = (double)targetWidth / originalImage.getWidth();
                double heightRatio = (double)targetHeight / originalImage.getHeight();
                double ratio = Math.min(widthRatio, heightRatio);

                int newWidth = (int)(originalImage.getWidth() * ratio);
                int newHeight = (int)(originalImage.getHeight() * ratio);

                BufferedImage resizedImage = Thumbnails.of(originalImage)
                    .size(newWidth, newHeight)
                    .keepAspectRatio(true)
                    .outputQuality(1.0)
                    .asBufferedImage();

                JLabel imageLabel = new JLabel(new ImageIcon(resizedImage));
                imageLabel.setHorizontalAlignment(SwingConstants.CENTER);
                imageLabel.setVerticalAlignment(SwingConstants.CENTER);

                panelUpload.add(imageLabel, "w 100%, h 100%, gap 0, align center");

            } catch (IOException e) {
                JLabel errorLabel = new JLabel("Không thể tải ảnh", SwingConstants.CENTER);
                panelUpload.add(errorLabel, "w 100%, h 100%, align center");
            }
        } else {
            JLabel noImageLabel = new JLabel("No image", SwingConstants.CENTER);
            panelUpload.add(noImageLabel, "w 100%, h 100%, align center");
        }

        // Hiển thị thông tin sản phẩm
        txtID.setText(product.getProductID() != null ? product.getProductID() : "N/A");
        txtProductName.setText(product.getProductName() != null ? product.getProductName() : "N/A");
        txtColor.setText(product.getColor() != null ? product.getColor() : "N/A");
        txtBatteryCapacity.setText(product.getBatteryCapacity() != null ? product.getBatteryCapacity() : "N/A");
        txtSpeed.setText(product.getSpeed() != null ? product.getSpeed() : "N/A");
        txtWarranty.setText(product.getWarrantyMonths() + " tháng");
        txtCateID.setText(product.getCategoryID() != null ? product.getCategoryID() : "N/A");
        
        // Only display Price (from database)
        if (product.getPrice() != null) {
            txtPrice.setText(String.format("%,.0f VNĐ", product.getPrice().doubleValue()));
        } else {
            txtPrice.setText("N/A");
        }
        
        // Quantity and Country
        txtQuantity.setText(String.valueOf(product.getQuantity()));
        txtCountry.setText(product.getCountry() != null ? product.getCountry() : "N/A");
        
        // Xử lý spinner quantity và nút Add/Update Cart
        if (product.getQuantity() == 0) {
            setupUIForOutOfStock();
        } else {
            setupUIForAvailableProduct(product);
        }

        String brand = busProduct.getBrandByProductId(product.getProductID());
        txtBrand.setText(brand != null ? brand : "N/A");

             // Cập nhật giao diện
      panelUpload.revalidate();
      panelUpload.repaint();
  }
    
    
    
    
    private void setupUIForOutOfStock() {
        // Thiết lập spinner = 0 và khóa lại
        SpinnerNumberModel quantityModel = new SpinnerNumberModel(
            0,      // Giá trị
            0,      // Min
            0,      // Max
            1       // Step
        );
        spinnerQuantity.setModel(quantityModel);
        spinnerQuantity.setEnabled(false);

        // Khóa cả editor text field
        JComponent editor = spinnerQuantity.getEditor();
        if (editor instanceof JSpinner.DefaultEditor) {
            ((JSpinner.DefaultEditor)editor).getTextField().setEditable(false);
        }

        // Thiết lập nút Add/Update Cart
        bntAddcart.setEnabled(false);
        bntAddcart.setText("Out of Stock");
        bntAddcart.setForeground(Color.red);
        bntAddcart.setBackgroundColor(Color.LIGHT_GRAY);
        bntAddcart.setHoverColor(Color.LIGHT_GRAY);
        bntAddcart.setPressedColor(Color.LIGHT_GRAY);
    }

    private void setupUIForAvailableProduct(productDTO product) {
        // Thiết lập spinner bình thường
        SpinnerNumberModel quantityModel = new SpinnerNumberModel(
            1,                      // Giá trị ban đầu
            1,                      // Min
            product.getQuantity(),   // Max = tồn kho
            1                       // Step
        );
        spinnerQuantity.setModel(quantityModel);
        spinnerQuantity.setEnabled(true);

        // Cho phép nhập tay nhưng có validate khi thay đổi
        JComponent editor = spinnerQuantity.getEditor();
        if (editor instanceof JSpinner.DefaultEditor) {
            JSpinner.DefaultEditor defaultEditor = (JSpinner.DefaultEditor) editor;
            defaultEditor.getTextField().setEditable(true);
            
            // Thêm ChangeListener để validate khi user thay đổi giá trị
            spinnerQuantity.addChangeListener(e -> {
                int value = (int) spinnerQuantity.getValue();
                int maxStock = product.getQuantity();
                
                // Tự động điều chỉnh nếu vượt quá max
                if (value > maxStock) {
                    spinnerQuantity.setValue(maxStock);
                    CustomDialog.showError(
                        "Quantity cannot exceed available stock!\n" +
                        "Maximum: " + maxStock
                    );
                } else if (value <= 0) {
                    spinnerQuantity.setValue(1);
                    CustomDialog.showError("Quantity must be greater than 0!");
                }
            });
        }

        // Thiết lập lại nút Add/Update Cart
        bntAddcart.setEnabled(true);
        bntAddcart.setText("Add|Update Cart");
        bntAddcart.setBackgroundColor(Color.decode("#FFA500"));
        bntAddcart.setHoverColor(Color.decode("#FFCC66"));
        bntAddcart.setPressedColor(Color.decode("#FF7F50"));
        
        // XÓA TẤT CẢ ActionListener cũ trước khi thêm mới để tránh double execution
        java.awt.event.ActionListener[] listeners = bntAddcart.getActionListeners();
        for (java.awt.event.ActionListener listener : listeners) {
            bntAddcart.removeActionListener(listener);
        }
        
        bntAddcart.addActionListener(e -> {
            // Validate quantity trước khi submit
            int quantity = (int) spinnerQuantity.getValue();
            
            // Kiểm tra số lượng hợp lệ
            if (quantity <= 0) {
                CustomDialog.showError("Quantity must be greater than 0!");
                spinnerQuantity.setValue(1);
                spinnerQuantity.requestFocus();
                return;
            }
            
            // Kiểm tra lại trạng thái tồn kho
            if (product != null && product.getQuantity() == 0) {
                CustomDialog.showError("This product is out of stock and cannot be added to cart!");
                return;
            }
            
            // Kiểm tra số lượng không vượt quá tồn kho
            if (product != null && quantity > product.getQuantity()) {
                CustomDialog.showError(
                    "Quantity exceeds available stock!\n\n" +
                    "Requested: " + quantity + "\n" +
                    "Available: " + product.getQuantity() + "\n\n" +
                    "Please reduce the quantity."
                );
                // Tự động điều chỉnh về giá trị max
                spinnerQuantity.setValue(product.getQuantity());
                spinnerQuantity.requestFocus();
                return;
            }

            // Lấy thông tin từ giao diện
            String productID = txtID.getText();
            String customerID = Dashboard_user.customerID;

            if (customerID == null || customerID.isEmpty()) {
                CustomDialog.showError("Please login to add products to cart!");
                return;
            }

            // Tạo DTO và xử lý
            DTOCart cartItem = new DTOCart(customerID, productID, quantity);
            busCart = new BUSCart();
            
            // Kiểm tra xem là ADD mới hay ADD THÊM (trước khi gọi addToCart)
            boolean isAddingMore = busCart.getCartItemsByCustomer(customerID).stream()
                .anyMatch(item -> item.getProductID().equals(productID));
            
            // Lấy số lượng hiện tại trong cart nếu đã có
            int existingQuantity = 0;
            if (isAddingMore) {
                existingQuantity = busCart.getCartItemsByCustomer(customerID).stream()
                    .filter(item -> item.getProductID().equals(productID))
                    .findFirst()
                    .map(item -> item.getQuantity())
                    .orElse(0);
            }
            
            boolean result = busCart.addToCart(cartItem);

            if (result) {
                // Hiển thị thông báo phù hợp
                if (isAddingMore) {
                    int newTotal = existingQuantity + quantity;
                    CustomDialog.showSuccess(
                        "Added " + quantity + " more item(s) to cart!\n\n" +
                        "Previous: " + existingQuantity + "\n" +
                        "Added: " + quantity + "\n" +
                        "Total in cart: " + newTotal
                    );
                } else {
                    CustomDialog.showSuccess("Product added to cart successfully!");
                }
                
                fireCartUpdatedEvent(customerID);
                this.dispose();
            } else {
                // Thông báo lỗi chi tiết hơn
                CustomDialog.showError(
                    "Failed to add product to cart!\n\n" +
                    "Possible reasons:\n" +
                    "• Quantity exceeds available stock\n" +
                    "• Invalid product or customer\n" +
                    "• Database connection error"
                );
            }
        });
        
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">                          
    private void initComponents() {

        bg = new javax.swing.JLayeredPane();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        bg.setBackground(new java.awt.Color(255, 255, 255));
        bg.setOpaque(true);

        javax.swing.GroupLayout bgLayout = new javax.swing.GroupLayout(bg);
        bg.setLayout(bgLayout);
        bgLayout.setHorizontalGroup(
            bgLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 407, Short.MAX_VALUE)
        );
        bgLayout.setVerticalGroup(
            bgLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 454, Short.MAX_VALUE)
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
    }// </editor-fold>                        

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger( productDeteails.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger( productDeteails.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger( productDeteails.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger( productDeteails.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new  productDeteails().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify                     
    private javax.swing.JLayeredPane bg;
    // End of variables declaration                   
}
