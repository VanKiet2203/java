
package com.User.login_user.GUI;

import com.ComponentandDatabase.Components.MyTextField;
import com.ComponentandDatabase.Components.CustomDialog;
import com.ComponentandDatabase.Components.MyButton;
import java.awt.CardLayout;
import java.awt.Font;
import java.awt.Color;
import java.util.Date;
import com.User.login_user.BUS.BusAccount_cus;
import com.User.login_user.DTO.DTOAccount_cus;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JScrollPane;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import com.User.dashboard_user.GUI.Dashboard_user;
import java.sql.*;
import com.toedter.calendar.JDateChooser;

public class PanelLoginandRegister_User extends javax.swing.JLayeredPane {
    private CardLayout cardLayout;
    private JPanel container;
    private MyTextField txtIDCard;
    private MyTextField txtFullName;
    private JRadioButton rdoMale;
    private JRadioButton rdoFemale;
    private JDateChooser dateOfBirth;
    private MyTextField txtEmail;
    public static MyTextField txtEmailLogin;
    private MyTextField txtContact;
    private JTextArea txtAddress;
    private MyTextField txtPassword;
    private MyTextField txtPasswordLogin;
    private ButtonGroup genderGroup;

    private BusAccount_cus busAccount;
    private CustomDialog cs;
    private static int mouseX, mouseY; // Biến lưu vị trí chuột
    private static String currentUserEmail = null; // Lưu email của user hiện tại

    public PanelLoginandRegister_User() {
        initComponents();
        initLogin();
        initRegister();
        login.setVisible(true);
        register.setVisible(false);
      
    }
    private void initRegister() {
        register.setLayout(null);
    
        // Offset chỉnh căn lề ngang
        int labelOffsetX = -20; // dịch label sang trái
        int fieldOffsetX = 30;  // dịch field sang phải
    
        JLabel label_title = new JLabel("Register Account");
        label_title.setFont(new Font("Times new roman", Font.BOLD, 30));
        label_title.setForeground(new Color(7, 164, 121));
        label_title.setBounds(160, 30, 280, 40);
        register.add(label_title);
    
        // ID Card
        JLabel lbl_idcard = new JLabel("ID Card");
        lbl_idcard.setFont(new Font("goudy old style", Font.PLAIN, 20));
        lbl_idcard.setForeground(Color.BLACK);
        lbl_idcard.setBounds(60 + labelOffsetX, 100, 200, 30);
        register.add(lbl_idcard);
    
        txtIDCard = new MyTextField();
        txtIDCard.setTextFont(new Font("Times new roman", Font.PLAIN, 16));
        txtIDCard.setHint("Enter your ID Card");
        txtIDCard.setBorder(BorderFactory.createLineBorder(new Color(7, 164, 121), 2));
        txtIDCard.setPreFixIcon("src\\main\\resources\\Icons\\User_icon\\ID_card.jpg");
        txtIDCard.setBounds((519 - 280) / 2 + fieldOffsetX, 100, 280, 35);
        register.add(txtIDCard);
    
        // Full Name
        JLabel lbl_fullname = new JLabel("Full Name");
        lbl_fullname.setFont(new Font("goudy old style", Font.PLAIN, 20));
        lbl_fullname.setForeground(Color.BLACK);
        lbl_fullname.setBounds(60 + labelOffsetX, 170, 200, 35);
        register.add(lbl_fullname);
    
        txtFullName = new MyTextField();
        txtFullName.setTextFont(new Font("Times new roman", Font.PLAIN, 16));
        txtFullName.setHint("Enter your full name");
        txtFullName.setBorder(BorderFactory.createLineBorder(new Color(7, 164, 121), 2));
        txtFullName.setPreFixIcon("src\\main\\resources\\Icons\\User_icon\\mail.png");
        txtFullName.setBounds((519 - 280) / 2 + fieldOffsetX, 170, 280, 35);
        register.add(txtFullName);
    
        // Gender
        JLabel lblGender = new JLabel("Gender");
        lblGender.setFont(new Font("goudy old style", Font.PLAIN, 20));
        lblGender.setForeground(Color.BLACK);
        lblGender.setBounds(60 + labelOffsetX, 240, 100, 30);
        register.add(lblGender);
    
        rdoMale = new JRadioButton("Male");
        rdoMale.setFont(new Font("Arial", Font.PLAIN, 16));
        rdoMale.setBounds(160 + fieldOffsetX, 240, 70, 30);
        rdoMale.setBackground(Color.WHITE);
        register.add(rdoMale);
    
        rdoFemale = new JRadioButton("Female");
        rdoFemale.setFont(new Font("Arial", Font.PLAIN, 16));
        rdoFemale.setBounds(250 + fieldOffsetX, 240, 80, 30);
        rdoFemale.setBackground(Color.WHITE);
        register.add(rdoFemale);
    
        genderGroup = new ButtonGroup();
        genderGroup.add(rdoMale);
        genderGroup.add(rdoFemale);
    
        // Date of Birth
        JLabel lblDOB = new JLabel("Date of Birth");
        lblDOB.setFont(new Font("goudy old style", Font.PLAIN, 20));
        lblDOB.setForeground(Color.BLACK);
        lblDOB.setBounds(60 + labelOffsetX, 300, 120, 30);
        register.add(lblDOB);
    
        dateOfBirth = new JDateChooser();
        dateOfBirth.setFont(new Font("Times New Roman", Font.PLAIN, 18));
        dateOfBirth.setDateFormatString("dd/MM/yyyy");
        dateOfBirth.setBounds((519 - 280) / 2 + fieldOffsetX, 295, 280, 35);
        dateOfBirth.setBackground(Color.WHITE);
        dateOfBirth.setOpaque(true);
        dateOfBirth.setBorder(BorderFactory.createLineBorder(new Color(7, 164, 121), 2));
        register.add(dateOfBirth);
    
        // Email
        JLabel lblEmail = new JLabel("Email");
        lblEmail.setFont(new Font("goudy old style", Font.PLAIN, 20));
        lblEmail.setForeground(Color.BLACK);
        lblEmail.setBounds(60 + labelOffsetX, 363, 120, 30);
        register.add(lblEmail);
    
        txtEmail = new MyTextField();
        txtEmail.setTextFont(new Font("Times new roman", Font.PLAIN, 16));
        txtEmail.setHint("Enter your Email");
        txtEmail.setBorder(BorderFactory.createLineBorder(new Color(7, 164, 121), 2));
        txtEmail.setPreFixIcon("src\\main\\resources\\Icons\\User_icon\\mail.png");
        txtEmail.setBounds((519 - 280) / 2 + fieldOffsetX, 358, 280, 35);
        register.add(txtEmail);
    
        // Contact
        JLabel lblContact = new JLabel("Contact");
        lblContact.setFont(new Font("goudy old style", Font.PLAIN, 20));
        lblContact.setForeground(Color.BLACK);
        lblContact.setBounds(60 + labelOffsetX, 425, 120, 30);
        register.add(lblContact);
    
        txtContact = new MyTextField();
        txtContact.setTextFont(new Font("Times new roman", Font.PLAIN, 16));
        txtContact.setHint("Enter your contact");
        txtContact.setBorder(BorderFactory.createLineBorder(new Color(7, 164, 121), 2));
        txtContact.setPreFixIcon("src\\main\\resources\\Icons\\User_icon\\contact.png");
        txtContact.setBounds((519 - 280) / 2 + fieldOffsetX, 423, 280, 35);
        register.add(txtContact);
    
        // Address
        JLabel lblAddress = new JLabel("Address");
        lblAddress.setFont(new Font("goudy old style", Font.PLAIN, 20));
        lblAddress.setForeground(Color.BLACK);
        lblAddress.setBounds(60 + labelOffsetX, 500, 120, 30);
        register.add(lblAddress);
    
        txtAddress = new JTextArea();
        txtAddress.setFont(new Font("Times new roman", Font.PLAIN, 16));
        txtAddress.setBorder(BorderFactory.createLineBorder(new Color(7, 164, 121), 2));
        txtAddress.setLineWrap(true);
        txtAddress.setWrapStyleWord(true);
    
        JScrollPane scrollAddress = new JScrollPane(txtAddress);
        scrollAddress.setBounds((519 - 280) / 2 + fieldOffsetX, 480, 280, 80);
        register.add(scrollAddress);
    
        // Password
        JLabel lblpass = new JLabel("Password");
        lblpass.setFont(new Font("goudy old style", Font.PLAIN, 20));
        lblpass.setForeground(Color.BLACK);
        lblpass.setBounds(60 + labelOffsetX, 585, 120, 30);
        register.add(lblpass);
    
        txtPassword = new MyTextField();
        txtPassword.setTextFont(new Font("Times New Roman", Font.PLAIN, 16));
        txtPassword.setPreFixIcon("src\\main\\resources\\Icons\\User_icon\\pass.png");
        txtPassword.setHint("Enter password");
        txtPassword.setEnabled(true);
    
        Color borderColor = new Color(7, 164, 121);
        int borderThickness = 2;
    
        JPanel passwordPanel = txtPassword.createPasswordFieldWithEyeButton(
            "Enter password",
            "src\\main\\resources\\Icons\\User_icon\\hidepass.png",
            "src\\main\\resources\\Icons\\User_icon\\showpass.png",
            borderColor,
            borderThickness
        );

     // Cập nhật vị trí
     passwordPanel.setBounds((519 - 280) / 2 + fieldOffsetX, 585, 280, 35);

     // Thêm vào giao diện
     register.add(passwordPanel);

       
        MyButton signup = new MyButton("SIGN UP", 20);
        signup.setBackgroundColor(new Color(0, 150, 136)); // Màu nền
        signup.setPressedColor(new Color(0, 100, 90)); // Màu khi nhấn
        signup.setHoverColor(new Color(0, 180, 150)); // Màu khi rê chuột vào
        // Center SIGN UP button on register
        signup.setBounds((519 - 200) / 2, 650, 200, 35);
        signup.setFont(new Font("Times New Roman", Font.BOLD, 18));
        signup.setForeground(Color.WHITE);

        // Thêm vào panel
        register.add(signup);

        // Nút Back to Sign In
        MyButton backToSignIn = new MyButton("Back to Sign In", 20);
        // Give Back to Sign In a filled blue theme and center it
        backToSignIn.setBackgroundColor(new Color(66, 133, 244));
        backToSignIn.setPressedColor(new Color(52, 103, 189));
        backToSignIn.setHoverColor(new Color(92, 153, 255));
        backToSignIn.setForeground(Color.WHITE);
        backToSignIn.setBounds((519 - 200) / 2, 690, 200, 35);
        backToSignIn.setFont(new Font("Times New Roman", Font.BOLD, 18));
        register.add(backToSignIn);
        backToSignIn.addActionListener(ev -> showRegister(true));

        // Xử lý sự kiện khi nhấn nút
        signup.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                busAccount= new BusAccount_cus();
                String idCard = txtIDCard.getText().trim();
                String fullName = txtFullName.getText().trim();
                String gender = rdoMale.isSelected() ? "Male" : (rdoFemale.isSelected() ? "Female" : "");
                        // Kiểm tra ngày sinh
                java.util.Date utilDob = dateOfBirth.getDate();
                if (utilDob == null) {
                    CustomDialog.showError("Please fill in all required fields! ");
                    return; // Dừng sự kiện nếu ngày sinh không hợp lệ
                }
                java.sql.Date sqlDob = new java.sql.Date(utilDob.getTime());
                String email = txtEmail.getText().trim();
                String contact = txtContact.getText().trim();
                String address = txtAddress.getText().trim();
                String password = txtPassword.getPasswordText().trim();
                String status = "Inactive";
                DTOAccount_cus DTOAccount= new DTOAccount_cus(idCard, fullName, gender, sqlDob, email, contact, address, password, status);
                boolean ok = busAccount.registerCustomer(DTOAccount);
                if (ok) {
                    showRegister(true);
                }

            }
        });
        
    }
    
        private void initLogin(){
      // Đặt layout của panel là null (không sử dụng layout manager)
            login.setLayout(null);

            // Tạo label "Create Account" và căn giữa
            JLabel label_title = new JLabel("Sign In");
            label_title.setFont(new Font("sansserif", Font.BOLD, 30));
            label_title.setForeground(new Color(7, 164, 121));

            // Đặt tọa độ và kích thước cho label
            label_title.setBounds(180, 230, 280, 40); 
            login.add(label_title);
            
             // Tạo đối tượng MyTextField email
            txtEmailLogin = new MyTextField();
            txtEmailLogin.setTextFont(new Font("Times New Roman", Font.PLAIN, 16));
            txtEmailLogin.setHint("Email");
            txtEmailLogin.setPreFixIcon("src\\main\\resources\\Icons\\User_icon\\mail.png");

            txtEmailLogin.setBorder(null);
            txtEmailLogin.setBackgroundColor(Color.decode("#E0F2E9")); // ✅ Gọi hàm mới để đổi màu
            txtEmailLogin.setBounds(100, 300, 280, 35);
            login.add(txtEmailLogin);

             // Tạo đối tượng MyTextField password
            txtPasswordLogin = new MyTextField();
            txtPasswordLogin.setBorder(null);
            Color backgroundColor = Color.decode("#E0F2E9"); // Màu xanh nhạt
            txtPasswordLogin.setBackgroundColor(backgroundColor);
            txtPasswordLogin.setTextFont(new Font("Times New Roman", Font.PLAIN, 16));
            txtPasswordLogin.setPreFixIcon("src\\main\\resources\\Icons\\User_icon\\pass.png");
            txtPasswordLogin.setHint("Enter password");

            // Gọi phương thức tạo password field có nút ẩn/hiện
            JPanel passwordPanel = txtPasswordLogin.createPasswordFieldWithEyeButton(
                "Password", 
                "src\\main\\resources\\Icons\\User_icon\\hidepass.png",
                "src\\main\\resources\\Icons\\User_icon\\showpass.png",
                backgroundColor,   // Màu viền đồng bộ
                0 // Không cần viền
            );

            // Đồng bộ màu nền cho toàn bộ passwordPanel
            passwordPanel.setOpaque(true);
            passwordPanel.setBackground(backgroundColor);
            passwordPanel.setBounds(100, 350, 280, 35);

            // Thêm vào giao diện
            login.add(passwordPanel);

           
        MyButton forget = new MyButton("Forgot password ?", 20);
        forget.setBackgroundColor(Color.WHITE); // Màu nền trắng
        forget.setPressedColor(new Color(200, 200, 200)); // Màu xám nhạt khi nhấn
        forget.setHoverColor(new Color(220, 220, 220)); // Màu xám sáng khi rê chuột vào
        forget.setBounds(150, 420, 200, 35);
        forget.setFont(new Font("Times New Roman", Font.BOLD | Font.ITALIC, 16));

        forget.setForeground(new Color(0, 150, 136)); // Màu chữ xanh đậm để dễ nhìn
        forget.addActionListener((e) -> {
            String email= txtEmailLogin.getText().strip();
            
            if (email.isEmpty()) {
              CustomDialog.showError("Please enter your Email before proceeding!");
                return; // không thực hiện tiếp
            }

            busAccount = new BusAccount_cus();
            String sentOtp = busAccount.sentOTP(email);

            if (sentOtp != null) {
                OTPFrame_cus OTP = new OTPFrame_cus();
                OTP.setVisible(true);
            } else {
                CustomDialog.showError("Failed to send OTP. Please check your Email or try again later.");
            }
        });

        // Thêm vào panel
        login.add(forget);
        
        //           
        MyButton signin = new MyButton("SIGN IN", 20);
        signin.setBackgroundColor(new Color(0, 150, 136)); // Màu nền
        signin.setPressedColor(new Color(0, 100, 90)); // Màu khi nhấn
        signin.setHoverColor(new Color(0, 180, 150)); // Màu khi rê chuột vào
        // Center SIGN IN button horizontally
        signin.setBounds((519 - 230) / 2, 480, 230, 35);
        signin.setFont(new Font("Times New Roman", Font.BOLD, 18));
        signin.setForeground(Color.WHITE);

        // Khi nhấn vào nút
        signin.addActionListener((e) -> {
            // Kiểm tra null trước khi truy cập
            if (txtEmailLogin == null) {
                System.out.println("🔍 ERROR - txtEmailLogin is null!");
                return;
            }
            if (txtPasswordLogin == null) {
                System.out.println("🔍 ERROR - txtPasswordLogin is null!");
                return;
            }
            
            String email = txtEmailLogin.getText().strip();
            String password = txtPasswordLogin.getPasswordText().strip();
            
            // Debug: Kiểm tra email và password
            System.out.println("🔍 DEBUG - Email from login: " + email);
            System.out.println("🔍 DEBUG - Password length: " + password.length());

           busAccount = new BusAccount_cus();  // ✅ Tạo BUS mới

            DTOAccount_cus account = busAccount.login(email, password);  // ✅ Gọi login
            if (account != null) {
                String fullName = busAccount.getName();  // ✅ Lấy tên từ BUS
                CustomDialog.showSuccess("Welcome " + fullName + "!");
                
                // Lưu email vào static variable
                currentUserEmail = email;
                System.out.println("🔍 DEBUG - Login successful, email saved: " + currentUserEmail);
                
                JFrame Login_User = (JFrame) SwingUtilities.getWindowAncestor(signin);
                Login_User.setVisible(false); // hoặc dispose nếu không cần
                
                Dashboard_user dashboard = new Dashboard_user(email);
                dashboard.setVisible(true);
            } else {
                System.out.println("🔍 DEBUG - Login failed, account is null");
            }
            // Nếu account == null thì đã có CustomDialog lỗi bên trong BUS rồi
        });
        
        login.add(signin);

        // Add a SIGN UP button below SIGN IN to switch directly to Register
        MyButton signupBelow = new MyButton("SIGN UP", 20);
        // Style SIGN UP button under login with a distinct colored theme and center it
        signupBelow.setBackgroundColor(new Color(255, 136, 0));
        signupBelow.setPressedColor(new Color(214, 115, 0));
        signupBelow.setHoverColor(new Color(255, 160, 51));
        signupBelow.setForeground(Color.WHITE);
        signupBelow.setBounds((519 - 230) / 2, 520, 230, 35);
        signupBelow.setFont(new Font("Times New Roman", Font.BOLD, 18));
        signupBelow.addActionListener(ev -> showRegister(false));
        login.add(signupBelow);

    }
        
    // Method để lấy email của user hiện tại
    public static String getCurrentUserEmail() {
        return currentUserEmail;
    }
    
    public void showRegister(boolean show){
        if(show){
            login.setVisible(true);
            register.setVisible(false);
            
         }
        else{
            login.setVisible(false);
            register.setVisible(true);
        }      
 }
     
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        login = new javax.swing.JPanel();
        register = new javax.swing.JPanel();

        setLayout(new java.awt.CardLayout());

        login.setBackground(new java.awt.Color(255, 255, 255));

        javax.swing.GroupLayout loginLayout = new javax.swing.GroupLayout(login);
        login.setLayout(loginLayout);
        loginLayout.setHorizontalGroup(
            loginLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 519, Short.MAX_VALUE)
        );
        loginLayout.setVerticalGroup(
            loginLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 419, Short.MAX_VALUE)
        );

        add(login, "card3");

        register.setBackground(new java.awt.Color(255, 255, 255));

        javax.swing.GroupLayout registerLayout = new javax.swing.GroupLayout(register);
        register.setLayout(registerLayout);
        registerLayout.setHorizontalGroup(
            registerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 519, Short.MAX_VALUE)
        );
        registerLayout.setVerticalGroup(
            registerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 419, Short.MAX_VALUE)
        );

        add(register, "card2");
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel login;
    private javax.swing.JPanel register;
    // End of variables declaration//GEN-END:variables
}
