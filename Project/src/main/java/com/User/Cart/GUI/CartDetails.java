
package com.User.Cart.GUI;

import com.ComponentandDatabase.Components.CustomDialog;
import com.ComponentandDatabase.Components.MyButton;
import com.ComponentandDatabase.Components.MyCombobox;
import com.ComponentandDatabase.Components.MyPanel;
import com.ComponentandDatabase.Components.MyTextField;
import com.User.Cart.BUS.BUSCart;
import com.User.Cart.DTO.DTOCart;
import com.User.dashboard_user.BUS.BUSProfile_cus;
import com.User.dashboard_user.GUI.MyProfile_cus;
import com.User.home.BUS.productBUS;
import com.User.home.DTO.productDTO;
import com.toedter.calendar.JDateChooser;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import net.coobird.thumbnailator.Thumbnails;
import net.miginfocom.swing.MigLayout;

public class CartDetails extends javax.swing.JFrame {
     public JLabel lblTitle, lblID, lblProductName, lblColor, lblBatteryCapacity, lblSpeed, lblWarranty, lblCateID
             , lblBrand, lblQuantity, lblPrice;
     public MyPanel panelTitle;
     public MyCombobox cmbGender;
     public static MyTextField txtID, txtProductName, txtColor, txtBatteryCapacity, txtSpeed, txtWarranty, txtCateID, txtBrand, txtQuantity, txtPrice;
     private JDateChooser dateOfBirth;
     private JTextArea txtAddress;
     public JPanel panelUpload;
     public MyButton bntUpload, bntAddcart;
//     public JSpinner spinnerQuantity;
     private BUSProfile_cus busProfile;
     private productBUS busProduct;
     private BUSCart busCart;
     private MyProfile_cus profile;
    
    public CartDetails() {
        initComponents();
        setSize(1100, 850); // Tăng kích thước để hiển thị đầy đủ các trường
        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE); 

        // Tính toán vị trí để căn giữa và trên cùng
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int x = (screenSize.width - getWidth()) / 2;
        setLocation(x, 0); // y = 0 để nằm trên cùng

        setAlwaysOnTop(true); // Luôn hiển thị trên cùng
        init();
    }
    public void init() {
     // Thiết lập layout chính với padding
     bg.setLayout(new MigLayout("fillx, insets 20", "[grow]", "[][][grow]"));

     // 1. Panel tiêu đề với design đẹp
     panelTitle = new MyPanel(new MigLayout("fill, insets 15"));
     panelTitle.setGradientColors(Color.decode("#2196F3"), Color.decode("#1976D2"), MyPanel.VERTICAL_GRADIENT);
     panelTitle.setBorder(BorderFactory.createCompoundBorder(
         BorderFactory.createLineBorder(Color.decode("#1976D2"), 2),
         BorderFactory.createEmptyBorder(15, 20, 15, 20)
     ));

     lblTitle = new JLabel("Product Details", JLabel.CENTER);
     lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 26));
     lblTitle.setForeground(Color.WHITE);

     panelTitle.add(lblTitle, "grow, push, align center");
     bg.add(panelTitle, "growx, h 70!, wrap");

        // Panel upload ảnh với design đẹp
    panelUpload = new JPanel();
    panelUpload.setLayout(new MigLayout("fill, insets 10"));
    panelUpload.setBackground(Color.WHITE);
    panelUpload.setPreferredSize(new Dimension(300, 300));
    panelUpload.setBorder(BorderFactory.createCompoundBorder(
        BorderFactory.createLineBorder(Color.decode("#2196F3"), 2),
        BorderFactory.createEmptyBorder(15, 15, 15, 15)
    ));

    // Thêm vào bg
    bg.add(panelUpload, "w 300!, h 300!, gap 20, align center, wrap");

     // Tạo panel chứa thông tin sản phẩm với layout 2 cột
     JPanel infoPanel = new JPanel(new MigLayout("fillx, insets 20", "[180!][20][grow][20][180!][20][grow]", "[]15[]15[]15[]15[]15[]15[]15[]"));
     infoPanel.setBackground(Color.WHITE);
     infoPanel.setBorder(BorderFactory.createCompoundBorder(
         BorderFactory.createLineBorder(Color.decode("#E0E0E0"), 1),
         BorderFactory.createEmptyBorder(20, 20, 20, 20)
     ));
     
     // Cột trái - Labels và Text fields
     lblID = new JLabel("Product ID:");
     lblID.setFont(new Font("Segoe UI", Font.BOLD, 15));
     lblID.setForeground(Color.decode("#1976D2"));
     infoPanel.add(lblID, "cell 0 0, alignx right");
  
    lblProductName = new JLabel("Product Name:");
    lblProductName.setFont(new Font("Segoe UI", Font.BOLD, 15));
    lblProductName.setForeground(Color.decode("#1976D2"));
    infoPanel.add(lblProductName, "cell 0 1, alignx right");
     
     lblColor = new JLabel("Color:");
     lblColor.setFont(new Font("Segoe UI", Font.BOLD, 15));
     lblColor.setForeground(Color.decode("#1976D2"));
     infoPanel.add(lblColor, "cell 0 2, alignx right");
     
     lblBatteryCapacity = new JLabel("Battery Capacity:");
     lblBatteryCapacity.setFont(new Font("Segoe UI", Font.BOLD, 15));
     lblBatteryCapacity.setForeground(Color.decode("#1976D2"));
     infoPanel.add(lblBatteryCapacity, "cell 0 3, alignx right");
     
     lblSpeed = new JLabel("Max Speed:");
     lblSpeed.setFont(new Font("Segoe UI", Font.BOLD, 15));
     lblSpeed.setForeground(Color.decode("#1976D2"));
     infoPanel.add(lblSpeed, "cell 0 4, alignx right");
     
     
     // Cột phải - Labels và Text fields
     lblCateID = new JLabel("Category ID:");
     lblCateID.setFont(new Font("Segoe UI", Font.BOLD, 15));
     lblCateID.setForeground(Color.decode("#1976D2"));
     infoPanel.add(lblCateID, "cell 4 0, alignx right");
     
     lblBrand = new JLabel("Supplier:");
     lblBrand.setFont(new Font("Segoe UI", Font.BOLD, 15));
     lblBrand.setForeground(Color.decode("#1976D2"));
     infoPanel.add(lblBrand, "cell 4 1, alignx right");
     
     lblWarranty = new JLabel("Warranty (Months):");
     lblWarranty.setFont(new Font("Segoe UI", Font.BOLD, 15));
     lblWarranty.setForeground(Color.decode("#1976D2"));
     infoPanel.add(lblWarranty, "cell 4 2, alignx right");

     lblQuantity = new JLabel("Quantity:");
     lblQuantity.setFont(new Font("Segoe UI", Font.BOLD, 15));
     lblQuantity.setForeground(Color.decode("#1976D2"));
     infoPanel.add(lblQuantity, "cell 4 3, alignx right");
     
     lblPrice = new JLabel("Price:");
     lblPrice.setFont(new Font("Segoe UI", Font.BOLD, 15));
     lblPrice.setForeground(Color.decode("#1976D2"));
     infoPanel.add(lblPrice, "cell 4 4, alignx right");
     
     // Cột trái - Text fields với kích thước lớn hơn
     txtID = new MyTextField();
     txtID.setBorder(BorderFactory.createCompoundBorder(
         BorderFactory.createLineBorder(Color.decode("#BDBDBD"), 1),
         BorderFactory.createEmptyBorder(8, 12, 8, 12)
     ));
     txtID.setTextColor(Color.decode("#212121"));
     txtID.setLocked(true);
     txtID.setTextFont(new Font("Segoe UI", Font.PLAIN, 14));
     txtID.setBackgroundColor(Color.decode("#FAFAFA"));
     infoPanel.add(txtID, "cell 2 0, w 250!, h 40!, growx");
     
     txtProductName = new MyTextField();
     txtProductName.setBorder(BorderFactory.createCompoundBorder(
         BorderFactory.createLineBorder(Color.decode("#BDBDBD"), 1),
         BorderFactory.createEmptyBorder(8, 12, 8, 12)
     ));
     txtProductName.setTextColor(Color.decode("#212121"));
     txtProductName.setLocked(true);
     txtProductName.setTextFont(new Font("Segoe UI", Font.PLAIN, 14));
     txtProductName.setBackgroundColor(Color.decode("#FAFAFA"));
     infoPanel.add(txtProductName, "cell 2 1, w 250!, h 40!, growx");
     
     txtColor = new MyTextField();
     txtColor.setBorder(BorderFactory.createCompoundBorder(
         BorderFactory.createLineBorder(Color.decode("#BDBDBD"), 1),
         BorderFactory.createEmptyBorder(8, 12, 8, 12)
     ));
     txtColor.setTextColor(Color.decode("#212121"));
     txtColor.setLocked(true);
     txtColor.setTextFont(new Font("Segoe UI", Font.PLAIN, 14));
     txtColor.setBackgroundColor(Color.decode("#FAFAFA"));
     infoPanel.add(txtColor, "cell 2 2, w 250!, h 40!, growx");
     
     txtBatteryCapacity = new MyTextField();
     txtBatteryCapacity.setBorder(BorderFactory.createCompoundBorder(
         BorderFactory.createLineBorder(Color.decode("#BDBDBD"), 1),
         BorderFactory.createEmptyBorder(8, 12, 8, 12)
     ));
     txtBatteryCapacity.setTextColor(Color.decode("#212121"));
     txtBatteryCapacity.setLocked(true);
     txtBatteryCapacity.setTextFont(new Font("Segoe UI", Font.PLAIN, 14));
     txtBatteryCapacity.setBackgroundColor(Color.decode("#FAFAFA"));
     infoPanel.add(txtBatteryCapacity, "cell 2 3, w 250!, h 40!, growx");
     
     txtSpeed = new MyTextField();
     txtSpeed.setBorder(BorderFactory.createCompoundBorder(
         BorderFactory.createLineBorder(Color.decode("#BDBDBD"), 1),
         BorderFactory.createEmptyBorder(8, 12, 8, 12)
     ));
     txtSpeed.setTextColor(Color.decode("#212121"));
     txtSpeed.setLocked(true);
     txtSpeed.setTextFont(new Font("Segoe UI", Font.PLAIN, 14));
     txtSpeed.setBackgroundColor(Color.decode("#FAFAFA"));
     infoPanel.add(txtSpeed, "cell 2 4, w 250!, h 40!, growx");
     
     // Cột phải - Text fields
     txtCateID = new MyTextField();
     txtCateID.setBorder(BorderFactory.createCompoundBorder(
         BorderFactory.createLineBorder(Color.decode("#BDBDBD"), 1),
         BorderFactory.createEmptyBorder(8, 12, 8, 12)
     ));
     txtCateID.setTextColor(Color.decode("#212121"));
     txtCateID.setLocked(true);
     txtCateID.setTextFont(new Font("Segoe UI", Font.PLAIN, 14));
     txtCateID.setBackgroundColor(Color.decode("#FAFAFA"));
     infoPanel.add(txtCateID, "cell 6 0, w 250!, h 40!, growx");
     
     txtBrand = new MyTextField();
     txtBrand.setBorder(BorderFactory.createCompoundBorder(
         BorderFactory.createLineBorder(Color.decode("#BDBDBD"), 1),
         BorderFactory.createEmptyBorder(8, 12, 8, 12)
     ));
     txtBrand.setTextColor(Color.decode("#212121"));
     txtBrand.setLocked(true);
     txtBrand.setTextFont(new Font("Segoe UI", Font.PLAIN, 14));
     txtBrand.setBackgroundColor(Color.decode("#FAFAFA"));
     infoPanel.add(txtBrand, "cell 6 1, w 250!, h 40!, growx");

     txtWarranty = new MyTextField();
     txtWarranty.setBorder(BorderFactory.createCompoundBorder(
         BorderFactory.createLineBorder(Color.decode("#BDBDBD"), 1),
         BorderFactory.createEmptyBorder(8, 12, 8, 12)
     ));
     txtWarranty.setTextColor(Color.decode("#212121"));
     txtWarranty.setLocked(true);
     txtWarranty.setTextFont(new Font("Segoe UI", Font.PLAIN, 14));
     txtWarranty.setBackgroundColor(Color.decode("#FAFAFA"));
     infoPanel.add(txtWarranty, "cell 6 2, w 250!, h 40!, growx");
     
     txtQuantity = new MyTextField();
     txtQuantity.setBorder(BorderFactory.createCompoundBorder(
         BorderFactory.createLineBorder(Color.decode("#D32F2F"), 2),
         BorderFactory.createEmptyBorder(8, 12, 8, 12)
     ));
     txtQuantity.setTextColor(Color.decode("#C62828"));
     txtQuantity.setLocked(true);
     txtQuantity.setTextFont(new Font("Segoe UI", Font.BOLD, 15));
     txtQuantity.setBackgroundColor(Color.decode("#FFEBEE"));
     infoPanel.add(txtQuantity, "cell 6 3, w 250!, h 40!, growx");

     txtPrice = new MyTextField();
     txtPrice.setBorder(BorderFactory.createCompoundBorder(
         BorderFactory.createLineBorder(Color.decode("#F57C00"), 2),
         BorderFactory.createEmptyBorder(8, 12, 8, 12)
     ));
     txtPrice.setTextColor(Color.decode("#E65100"));
     txtPrice.setLocked(true);
     txtPrice.setTextFont(new Font("Segoe UI", Font.BOLD, 15));
     txtPrice.setBackgroundColor(Color.decode("#FFF3E0"));
     infoPanel.add(txtPrice, "cell 6 4, w 250!, h 40!, growx");
     
     
     // Thêm infoPanel vào bg
     bg.add(infoPanel, "growx, wrap");
    }
    
    // Hàm load ảnh và resize từ resources
    private ImageIcon loadScaledIcon(String resourcePath, int width, int height) {
         try {
             // Load icon từ resources
             java.net.URL iconURL = getClass().getResource(resourcePath);
             if (iconURL != null) {
                 ImageIcon originalIcon = new ImageIcon(iconURL);
                 Image scaledImage = originalIcon.getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH);
                 return new ImageIcon(scaledImage);
             } else {
                 System.err.println("Icon not found: " + resourcePath);
                 // Trả về icon mặc định nếu không tìm thấy
                 return new ImageIcon();
             }
         } catch (Exception e) {
             System.err.println("Error loading icon: " + resourcePath + " - " + e.getMessage());
             return new ImageIcon();
         }
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
        txtWarranty.setText(String.valueOf(product.getWarrantyMonths()));
        txtCateID.setText(product.getCategoryID() != null ? product.getCategoryID() : "N/A");
        txtQuantity.setText(String.valueOf(product.getQuantity()));
        
        // Hiển thị giá với format đẹp
        if (product.getPrice() != null) {
            txtPrice.setText(String.format("%,d VND", product.getPrice().longValue()));
        } else {
            txtPrice.setText("N/A");
        }
        
        String brand = busProduct.getBrandByProductId(product.getProductID());
        txtBrand.setText(brand != null ? brand : "N/A");
       

             // Cập nhật giao diện
        panelUpload.revalidate();
        panelUpload.repaint();
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
            java.util.logging.Logger.getLogger( CartDetails.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger( CartDetails.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger( CartDetails.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger( CartDetails.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new CartDetails().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify                     
    private javax.swing.JLayeredPane bg;
    // End of variables declaration                   
}

