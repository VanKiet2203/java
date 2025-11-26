
package com.User.dashboard_user.GUI;


import com.ComponentandDatabase.Components.MyButton;
import com.ComponentandDatabase.Components.MyCombobox;
import com.ComponentandDatabase.Components.MyPanel;
import com.ComponentandDatabase.Components.MyTextField;
import com.User.dashboard_user.BUS.BUSProfile_cus;
import com.ComponentandDatabase.Components.CustomDialog;
import com.toedter.calendar.JDateChooser;
import java.awt.Color;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import net.coobird.thumbnailator.Thumbnails;
import java.awt.Dimension;
import java.awt.Font;
import javax.swing.JFileChooser;
import javax.swing.ImageIcon;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import javax.swing.BorderFactory;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import net.miginfocom.swing.MigLayout;

public class MyProfile_cus extends javax.swing.JFrame {
     public JLabel lblTitle, lblID, lblFullName, lblGender, lblDateofBirth, lblEmail, lblContact, lblAddress;
     public JLabel lblOldPassword, lblNewPassword, lblConfirmPassword;
     public MyPanel panelTitle;
     public MyCombobox<String> cmbGender;
     public static MyTextField txtID, txtFullName, txtEmail, txtContact;
     private MyTextField txtOldPassword, txtNewPassword, txtConfirmPassword;
     private JDateChooser dateOfBirth;
     private JTextArea txtAddress;
     public JPanel panelUpload;
     public MyButton bntUpload, bntUpdate, btnChangePassword;
     private BUSProfile_cus busProfile;
     private Menu_user menuRef; // Reference đến Menu_user để refresh profile label
     public static String selectedImagePath = null; // Lưu đường dẫn ảnh đã xử lý
    
    public MyProfile_cus() {
        this(null);
    }
    
    public MyProfile_cus(Menu_user menuRef) {
        this.menuRef = menuRef;
        initComponents();
        setSize(600, 720); // Tăng kích thước để hiển thị đủ thông tin
        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE); 
        
        // Add WindowListener để refresh menu khi đóng
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosed(java.awt.event.WindowEvent e) {
                if (menuRef != null) {
                    menuRef.refreshProfileLabel();
                }
            }
        });
        
        init();
        showProfile();
    }
    public void init(){
        // Thiết lập layout chính
        bg.setLayout(new MigLayout("fill, insets 0", "[grow]", "[grow]"));
        
        // Tạo JTabbedPane
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Arial", Font.BOLD, 14));
        tabbedPane.setBackground(Color.WHITE);
        
        // ===== TAB 1: PERSONAL INFORMATION =====
        JPanel panelPersonalInfo = new JPanel(new MigLayout("fill, insets 20, gap 10", "[grow]", "[]15[]15[]15[]15[]15[]15[]15[]15[]"));
        panelPersonalInfo.setBackground(Color.WHITE);
        
        // Panel tiêu đề
        panelTitle = new MyPanel(new MigLayout("fill, insets 0"));
        panelTitle.setGradientColors(Color.decode("#1CB5E0"), Color.decode("#4682B4"), MyPanel.VERTICAL_GRADIENT);
        lblTitle = new JLabel("Your Information", JLabel.CENTER);
        lblTitle.setFont(new Font("Arial", Font.BOLD, 20));
        lblTitle.setForeground(Color.WHITE);
        panelTitle.add(lblTitle, "grow, push, align center");
        panelPersonalInfo.add(panelTitle, "growx, h 50!, wrap");
        
        // Panel chứa avatar và thông tin
        JPanel panelMainContent = new JPanel(new MigLayout("fill, insets 0, gap 15", "[grow][]", ""));
        panelMainContent.setBackground(Color.WHITE);
        
        // Panel bên trái - thông tin
        JPanel panelInfo = new JPanel(new MigLayout("fill, insets 0, gap 5", "[140!][grow]", "[]15[]15[]15[]15[]15[]15[]"));
        panelInfo.setBackground(Color.WHITE);
        
        // ID Card
        lblID = new JLabel("ID Card:");
        lblID.setFont(new Font("Arial", Font.BOLD, 14));
        lblID.setForeground(Color.BLACK);
        panelInfo.add(lblID, "growy, aligny center");
        
        txtID = new MyTextField();
        txtID.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        txtID.setTextColor(Color.BLUE);
        txtID.setLocked(true);
        txtID.setTextFont(new Font("Times New Roman", Font.BOLD, 14));
        txtID.setBackgroundColor(Color.WHITE);
        panelInfo.add(txtID, "growx, h 35!, wrap");
        
        // Full Name
        lblFullName = new JLabel("Full Name:");
        lblFullName.setFont(new Font("Arial", Font.BOLD, 14));
        lblFullName.setForeground(Color.BLACK);
        panelInfo.add(lblFullName, "growy, aligny center");
        
        txtFullName = new MyTextField();
        txtFullName.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        txtFullName.setTextFont(new Font("Times new roman", Font.BOLD, 14));
        txtFullName.setTextColor(Color.red);
        txtFullName.setBackgroundColor(Color.WHITE);
        txtFullName.setLocked(true);
        panelInfo.add(txtFullName, "growx, h 35!, wrap");
        
        // Gender
        lblGender = new JLabel("Gender:");
        lblGender.setFont(new Font("Arial", Font.BOLD, 14));
        lblGender.setForeground(Color.BLACK);
        panelInfo.add(lblGender, "growy, aligny center");
        
        String[] items = {"Male", "Female"};
        cmbGender = new MyCombobox<>(items);
        cmbGender.setCustomFont(new Font("Times New Roman", Font.PLAIN, 14));
        cmbGender.setCustomColors(Color.WHITE, Color.GRAY, Color.BLACK);
        cmbGender.setEnabled(false);
        panelInfo.add(cmbGender, "growx, h 35!, wrap");
        
        // Date of Birth
        lblDateofBirth = new JLabel("Date of Birth:");
        lblDateofBirth.setFont(new Font("Arial", Font.BOLD, 14));
        lblDateofBirth.setForeground(Color.BLACK);
        panelInfo.add(lblDateofBirth, "growy, aligny center");
        
        dateOfBirth = new JDateChooser();
        dateOfBirth.setFont(new Font("Times New Roman", Font.PLAIN, 14));
        dateOfBirth.setDateFormatString("dd/MM/yyyy");
        dateOfBirth.setBackground(Color.WHITE);
        dateOfBirth.setEnabled(false);
        panelInfo.add(dateOfBirth, "growx, h 35!, wrap");
        
        // Email
        lblEmail = new JLabel("Email:");
        lblEmail.setFont(new Font("Arial", Font.BOLD, 14));
        lblEmail.setForeground(Color.BLACK);
        panelInfo.add(lblEmail, "growy, aligny center");
        
        txtEmail = new MyTextField();
        txtEmail.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        txtEmail.setTextFont(new Font("Times new roman", Font.PLAIN, 14));
        txtEmail.setBackgroundColor(Color.WHITE);
        txtEmail.setLocked(true);
        panelInfo.add(txtEmail, "growx, h 35!, w 250:, wrap");
        
        // Contact
        lblContact = new JLabel("Contact:");
        lblContact.setFont(new Font("Arial", Font.BOLD, 14));
        lblContact.setForeground(Color.BLACK);
        panelInfo.add(lblContact, "growy, aligny center");
        
        txtContact = new MyTextField();
        txtContact.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        txtContact.setTextFont(new Font("Times new roman", Font.PLAIN, 14));
        txtContact.setBackgroundColor(Color.WHITE);
        panelInfo.add(txtContact, "growx, h 35!, wrap");
        
        // Address
        lblAddress = new JLabel("Address:");
        lblAddress.setFont(new Font("Arial", Font.BOLD, 14));
        lblAddress.setForeground(Color.BLACK);
        panelInfo.add(lblAddress, "growy, aligny top");
        
        txtAddress = new JTextArea();
        txtAddress.setFont(new Font("Times new roman", Font.PLAIN, 14));
        txtAddress.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        txtAddress.setLineWrap(true);
        txtAddress.setWrapStyleWord(true);
        JScrollPane scrollAddress = new JScrollPane(txtAddress);
        scrollAddress.setBorder(null);
        panelInfo.add(scrollAddress, "growx, h 80!, aligny top, wrap");
        
        panelMainContent.add(panelInfo, "grow, push");
        
        // Panel bên phải - Avatar
        JPanel panelAvatar = new JPanel(new MigLayout("fill, insets 10, gap 10", "[grow]", "[]15[]"));
        panelAvatar.setBackground(Color.WHITE);
        
        panelUpload = new JPanel(new MigLayout("insets 0, gap 0, fill"));
        panelUpload.setBackground(Color.WHITE);
        panelUpload.setVisible(false);
        panelUpload.setPreferredSize(new Dimension(120, 120));
        panelUpload.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1));
        
        JLabel lblUploadImage = new JLabel("Upload Image");
        lblUploadImage.setFont(new Font("Arial", Font.PLAIN, 10));
        panelUpload.add(lblUploadImage, "pos 0.5al 0.5al");
        
        panelAvatar.add(panelUpload, "growx, h 120!, w 120!, wrap");
        
        bntUpload = new MyButton("Upload", 14);
        bntUpload.setBackgroundColor(Color.decode("#1CB5E0"));
        bntUpload.setPressedColor(Color.decode("#4682B4"));
        bntUpload.setHoverColor(Color.decode("#1CB5E0"));
        bntUpload.setFont(new Font("Arial", Font.BOLD, 12));
        bntUpload.setForeground(Color.WHITE);
        bntUpload.setButtonIcon("src\\main\\resources\\Icons\\Admin_icon\\upload_profile.png", 
                                   20, 20, 5, SwingConstants.LEFT, SwingConstants.CENTER);
        bntUpload.addActionListener((e) -> {
            uploadImage();
        });
        panelAvatar.add(bntUpload, "growx, h 35!, w 120!");
        
        panelMainContent.add(panelAvatar, "aligny top");
        
        panelPersonalInfo.add(panelMainContent, "grow, push, wrap");
        
        // Nút Update
        bntUpdate = new MyButton("Update Profile", 16);
        bntUpdate.setBackgroundColor(Color.decode("#00CC33"));
        bntUpdate.setPressedColor(Color.decode("#33CC33"));
        bntUpdate.setHoverColor(Color.decode("#00EE00"));
        bntUpdate.setFont(new Font("Times New Roman", Font.BOLD, 14));
        bntUpdate.setForeground(Color.WHITE);
        bntUpdate.addActionListener((e) -> {
            busProfile = new BUSProfile_cus();
            busProfile.updateProfile(txtID, txtFullName, cmbGender, dateOfBirth, txtEmail, txtContact, txtAddress, selectedImagePath);
            if (menuRef != null) {
                menuRef.refreshProfileLabel();
            }
        });
        panelPersonalInfo.add(bntUpdate, "growx, h 40!, align center");
        
        tabbedPane.addTab("Personal Information", panelPersonalInfo);
        
        // ===== TAB 2: CHANGE PASSWORD =====
        JPanel panelChangePassword = new JPanel(new MigLayout("fill, insets 20, gap 10", "[grow]", "[]15[]15[]15[]15[]15[]"));
        panelChangePassword.setBackground(Color.WHITE);
        
        // Panel tiêu đề - giống với Your Information
        MyPanel passwordTitlePanel = new MyPanel(new MigLayout("fill, insets 0"));
        passwordTitlePanel.setGradientColors(Color.decode("#1CB5E0"), Color.decode("#4682B4"), MyPanel.VERTICAL_GRADIENT);
        JLabel lblPasswordTitle = new JLabel("Change Password", JLabel.CENTER);
        lblPasswordTitle.setFont(new Font("Arial", Font.BOLD, 20));
        lblPasswordTitle.setForeground(Color.WHITE);
        passwordTitlePanel.add(lblPasswordTitle, "grow, push, align center");
        panelChangePassword.add(passwordTitlePanel, "growx, h 50!, wrap");
        
        lblOldPassword = new JLabel("Old Password:");
        lblOldPassword.setFont(new Font("Arial", Font.BOLD, 14));
        lblOldPassword.setForeground(Color.BLACK);
        panelChangePassword.add(lblOldPassword, "growx, wrap");
        
        Color backgroundColor = Color.decode("#E0F2E9");
        txtOldPassword = new MyTextField();
        txtOldPassword.setBorder(null);
        txtOldPassword.setBackgroundColor(backgroundColor);
        txtOldPassword.setTextFont(new Font("Times New Roman", Font.PLAIN, 14));
        txtOldPassword.setPreFixIcon("src\\main\\resources\\Icons\\User_icon\\pass.png");
        
        JPanel passwordPanelOld = txtOldPassword.createPasswordFieldWithEyeButton(
            "Enter old password", 
            "src\\main\\resources\\Icons\\User_icon\\hidepass.png",
            "src\\main\\resources\\Icons\\User_icon\\showpass.png",
            backgroundColor,
            0
        );
        passwordPanelOld.setOpaque(true);
        passwordPanelOld.setBackground(backgroundColor);
        panelChangePassword.add(passwordPanelOld, "growx, h 40!, wrap");
        
        lblNewPassword = new JLabel("New Password:");
        lblNewPassword.setFont(new Font("Arial", Font.BOLD, 14));
        lblNewPassword.setForeground(Color.BLACK);
        panelChangePassword.add(lblNewPassword, "growx, wrap");
        
        txtNewPassword = new MyTextField();
        txtNewPassword.setBorder(null);
        txtNewPassword.setBackgroundColor(backgroundColor);
        txtNewPassword.setTextFont(new Font("Times New Roman", Font.PLAIN, 14));
        txtNewPassword.setPreFixIcon("src\\main\\resources\\Icons\\User_icon\\pass.png");
        
        JPanel passwordPanelNew = txtNewPassword.createPasswordFieldWithEyeButton(
            "Enter new password", 
            "src\\main\\resources\\Icons\\User_icon\\hidepass.png",
            "src\\main\\resources\\Icons\\User_icon\\showpass.png",
            backgroundColor,
            0
        );
        passwordPanelNew.setOpaque(true);
        passwordPanelNew.setBackground(backgroundColor);
        panelChangePassword.add(passwordPanelNew, "growx, h 40!, wrap");
        
        lblConfirmPassword = new JLabel("Confirm Password:");
        lblConfirmPassword.setFont(new Font("Arial", Font.BOLD, 14));
        lblConfirmPassword.setForeground(Color.BLACK);
        panelChangePassword.add(lblConfirmPassword, "growx, wrap");
        
        txtConfirmPassword = new MyTextField();
        txtConfirmPassword.setBorder(null);
        txtConfirmPassword.setBackgroundColor(backgroundColor);
        txtConfirmPassword.setTextFont(new Font("Times New Roman", Font.PLAIN, 14));
        txtConfirmPassword.setPreFixIcon("src\\main\\resources\\Icons\\User_icon\\pass.png");
        
        JPanel passwordPanelConfirm = txtConfirmPassword.createPasswordFieldWithEyeButton(
            "Confirm new password", 
            "src\\main\\resources\\Icons\\User_icon\\hidepass.png",
            "src\\main\\resources\\Icons\\User_icon\\showpass.png",
            backgroundColor,
            0
        );
        passwordPanelConfirm.setOpaque(true);
        passwordPanelConfirm.setBackground(backgroundColor);
        panelChangePassword.add(passwordPanelConfirm, "growx, h 40!, wrap");
        
        btnChangePassword = new MyButton("Change Password", 16);
        btnChangePassword.setBackgroundColor(new Color(255, 136, 0));
        btnChangePassword.setPressedColor(new Color(214, 115, 0));
        btnChangePassword.setHoverColor(new Color(255, 160, 51));
        btnChangePassword.setFont(new Font("Arial", Font.BOLD, 14));
        btnChangePassword.setForeground(Color.WHITE);
        btnChangePassword.addActionListener((e) -> {
            String oldPass = txtOldPassword.getPasswordText().strip();
            String newPass = txtNewPassword.getPasswordText().strip();
            String confirmPass = txtConfirmPassword.getPasswordText().strip();
            String email = Dashboard_user.email;
            
            busProfile = new BUSProfile_cus();
            boolean success = busProfile.changePassword(email, oldPass, newPass, confirmPass);
            
            if (success) {
                txtOldPassword.setText("");
                txtNewPassword.setText("");
                txtConfirmPassword.setText("");
            }
        });
        panelChangePassword.add(btnChangePassword, "growx, h 45!, align center");
        
        tabbedPane.addTab("Change Password", panelChangePassword);
        
        bg.add(tabbedPane, "grow");
    }
    
    private void uploadImage() {
      JFileChooser fileChooser = new JFileChooser();
      fileChooser.setDialogTitle("Select Profile Picture");
      FileNameExtensionFilter filter = new FileNameExtensionFilter("Image Files", "jpg", "jpeg", "png", "gif");
      fileChooser.setFileFilter(filter);

      int result = fileChooser.showOpenDialog(null);
      if (result == JFileChooser.APPROVE_OPTION) {
          File selectedFile = fileChooser.getSelectedFile();
          String fileName = selectedFile.getName();

          // Tạo thư mục đích nếu chưa tồn tại
          File destFolder = new File("src\\main\\resources\\Profile_Image");
          if (!destFolder.exists()) {
              destFolder.mkdirs();
          }

          // Đường dẫn file đích
          File destFile = new File(destFolder, fileName);

          // Copy file nếu chưa tồn tại
          if (!destFile.exists()) {
              try {
                  Files.copy(selectedFile.toPath(), destFile.toPath());
              } catch (IOException e) {
                  e.printStackTrace();
                  CustomDialog.showError("Error copying the image!");
                  return;
              }
          }

          selectedImagePath = destFile.getAbsolutePath();

          try {
              // Đọc ảnh và tính toán kích thước mới
              BufferedImage originalImage = ImageIO.read(destFile);
              int panelWidth = panelUpload.getWidth();
              int panelHeight = panelUpload.getHeight();

              // Scale ảnh bằng Thumbnailator (giữ tỷ lệ + chất lượng cao)
              BufferedImage scaledImage = Thumbnails.of(originalImage)
                  .size(panelWidth > 0 ? panelWidth : 100, panelHeight > 0 ? panelHeight : 100)
                  .keepAspectRatio(true)  // Giữ nguyên tỷ lệ
                  .outputQuality(1.0)     // Chất lượng 100%
                  .asBufferedImage();

              // Hiển thị ảnh
              JLabel imageLabel = new JLabel(new ImageIcon(scaledImage));
              imageLabel.setHorizontalAlignment(JLabel.CENTER);
              imageLabel.setVerticalAlignment(JLabel.CENTER);

              panelUpload.removeAll();
              panelUpload.add(imageLabel, "pos 0.5al 0.5al");
              panelUpload.setVisible(true);
              panelUpload.revalidate();
              panelUpload.repaint();

              CustomDialog.showSuccess("Image uploaded successfully!");
          } catch (Exception ex) {
              ex.printStackTrace();
              CustomDialog.showError("Failed to display image!");
          }
      }
  }
    
  public void showProfile(){
      busProfile= new BUSProfile_cus();
      String email= Dashboard_user.email;
      busProfile.showProfile(email, txtID, txtFullName, cmbGender, dateOfBirth, txtEmail, txtContact, txtAddress, panelUpload);
  }
    
    
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
            java.util.logging.Logger.getLogger(MyProfile_cus.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(MyProfile_cus.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(MyProfile_cus.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(MyProfile_cus.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new MyProfile_cus().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify                     
    private javax.swing.JLayeredPane bg;
    // End of variables declaration                   
}
