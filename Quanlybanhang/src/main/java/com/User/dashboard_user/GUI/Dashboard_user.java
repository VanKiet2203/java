package com.User.dashboard_user.GUI;

import java.awt.*;
import javax.swing.*;
import java.awt.event.*;
import javax.swing.Box;
import com.ComponentandDatabase.Components.MyButton;
import com.ComponentandDatabase.Components.MyPanel;
import com.User.home.GUI.Home_user;
import com.User.Cart.GUI.Form_Cart;
import com.User.order.GUI.Order_Form;
import com.User.dashboard_user.BUS.BUSProfile_cus;
import com.User.login_user.GUI.PanelLoginandRegister_User;
import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import javax.imageio.ImageIO;

public class Dashboard_user extends JFrame {
    private JPanel bg, contentPanel;
    private JLabel title, lblDateTime;
    private MyButton logout;
    private MyPanel panelTitle;
    private Menu_user menu;
    private CardLayout cardLayout;
    private Set<JLabel> hoveredLabels = new HashSet<>();
    private BUSProfile_cus busProfile;
    public static String email, customerID;
    private String userName;

    public Dashboard_user() {
        this(null); // Gọi constructor với email = null
    }
    
    public Dashboard_user(String userEmail) {
        initComponents(userEmail);
        loadUserInfo(); // Load thông tin user
        setSize(1570, 800); // KHÔNG full screen nữa
        setLocationRelativeTo(null); // Hiển thị giữa màn hình
        setResizable(true);
        setVisible(true);
    }

    private void initComponents() {
        initComponents(null);
    }
    
    private void initComponents(String userEmail) {
        // PANEL NỀN CHÍNH
        bg = new JPanel(new BorderLayout());

        // PANEL TIÊU ĐỀ
        panelTitle = new MyPanel(new BorderLayout());
        panelTitle.setPreferredSize(new Dimension(0, 50));
        panelTitle.setGradientColors(Color.decode("#1CB5E0"), Color.decode("#4682B4"), MyPanel.VERTICAL_GRADIENT);

        // Tiêu đề chính giữa
        ImageIcon titleIcon = new ImageIcon(getClass().getResource("/Icons/Admin_icon/Title_icon.png"));
        Image img = titleIcon.getImage().getScaledInstance(40, 40, Image.SCALE_SMOOTH);
        titleIcon = new ImageIcon(img);
        title = new JLabel("NPK Store - Electric Bicycle Sales", titleIcon, JLabel.CENTER);
        title.setFont(new Font("Times New Roman", Font.BOLD, 20));
        title.setForeground(Color.WHITE);

        // Logout và thời gian bên phải
        logout = new MyButton("Logout", 20);
        logout.setBackgroundColor(Color.decode("#E55454"));
        logout.setPressedColor(Color.decode("#C04444"));
        logout.setHoverColor(Color.decode("#FF7F7F"));
        logout.setFont(new Font("Times New Roman", Font.BOLD, 18));
        logout.setForeground(Color.WHITE);
        logout.setButtonIcon("/Icons/Admin_icon/logout.png", 25, 25, 10, SwingConstants.RIGHT, SwingConstants.CENTER);
        logout.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to logout?",
                "Logout Confirmation",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
            );

            if (confirm == JOptionPane.YES_OPTION) {
                // Đóng frame hiện tại và quay về login
                this.dispose();
                // Mở lại form login
                com.User.login_user.GUI.Login_User loginForm = new com.User.login_user.GUI.Login_User();
                loginForm.setVisible(true);
            }
        });

        lblDateTime = new JLabel();
        lblDateTime.setFont(new Font("Times New Roman", Font.BOLD, 18));
        lblDateTime.setForeground(Color.WHITE);
        updateDateTime();

        JPanel rightPanel = new JPanel();
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.X_AXIS));
        rightPanel.setOpaque(false);
        rightPanel.add(Box.createHorizontalGlue());
        rightPanel.add(lblDateTime);
        rightPanel.add(Box.createHorizontalStrut(50));
        rightPanel.add(logout);

        JPanel containerPanel = new JPanel(new BorderLayout());
        containerPanel.setOpaque(false);
        containerPanel.add(rightPanel, BorderLayout.EAST);

        panelTitle.add(title, BorderLayout.CENTER);
        panelTitle.add(containerPanel, BorderLayout.EAST);
        bg.add(panelTitle, BorderLayout.NORTH);

        // MENU + CONTENT
        menu = new Menu_user(this);
        menu.setLayout(new BoxLayout(menu, BoxLayout.Y_AXIS));
        menu.setPreferredSize(new Dimension(260, 0));

        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.add(menu, BorderLayout.WEST);
        wrapper.add(contentPanel, BorderLayout.CENTER);

        bg.add(wrapper, BorderLayout.CENTER);
     
        // Sử dụng email được truyền vào hoặc lấy từ static method
        if (userEmail != null && !userEmail.isEmpty()) {
            email = userEmail;
            System.out.println("🔍 DEBUG - Dashboard received email: " + email);
        } else {
            // Lấy email từ static method
            String savedEmail = PanelLoginandRegister_User.getCurrentUserEmail();
            if (savedEmail != null && !savedEmail.isEmpty()) {
                email = savedEmail;
                System.out.println("🔍 DEBUG - Dashboard got email from static method: " + email);
            } else {
                // Fallback nếu không có email
                email = "default@email.com";
                System.out.println("🔍 DEBUG - Dashboard using fallback email: " + email);
            }
        }
        busProfile = new BUSProfile_cus();
        customerID = busProfile.getCustomerID(email);
        System.out.println("🔍 DEBUG - Customer ID: " + customerID);
        // THÊM FORM
        
        Form_Cart cartForm = new Form_Cart(customerID);
        Order_Form orderForm = new Order_Form(customerID);

        // Đăng ký listener trước khi thêm vào contentPanel
        cartForm.setOrderUpdateListener(orderForm);

        // Thêm các form vào contentPanel
        contentPanel.add(new Home_user(), "Home_user");
        contentPanel.add(cartForm, "Form_Cart");
        contentPanel.add(orderForm, "Order_Form");

        // FRAME SETUP
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setTitle("Dashboard Customer");
        setContentPane(bg);

        // UPDATE GIỜ THỰC TẾ
        Timer timer = new Timer(1000, e -> updateDateTime());
        timer.start();
    }

    private void updateDateTime() {
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("'Date:' dd/MM/yyyy  '-' HH:mm:ss");
        String currentTime = sdf.format(new java.util.Date());
        lblDateTime.setText(currentTime);
    }

    public void showForm(String formName) {
        cardLayout.show(contentPanel, formName);
    }

    // Tạo Label có icon (dành cho future use)
    private JLabel createLabelWithIcon(String text, String iconName, int x, int y, int width, int height, boolean isTitle) throws IOException {
        File file = new File("src\\main\\resources\\Icons\\Admin_icon\\" + iconName);
        if (!file.exists()) {
            System.err.println("⚠️ File not found: " + file.getAbsolutePath());
            return new JLabel(text);
        }

        Image img = ImageIO.read(file).getScaledInstance(30, 30, Image.SCALE_SMOOTH);
        ImageIcon icon = new ImageIcon(img);
        JLabel label = new JLabel("<html>" + text + "</html>", icon, JLabel.LEFT);
        label.setFont(isTitle ? new Font("Arial", Font.BOLD | Font.ITALIC, 20) : new Font("sansserif", Font.BOLD, 18));
        label.setForeground(Color.WHITE);
        label.setBounds(x, y, width, height);
        label.setIconTextGap(15);
        label.setOpaque(false);
        return label;
    }

    // Method để lấy tên user
    public String getUserName() {
        if (userName == null && customerID != null) {
            try {
                busProfile = new BUSProfile_cus();
                userName = busProfile.getCustomerName(customerID);
            } catch (Exception e) {
                userName = "User";
            }
        }
        return userName;
    }
    
    // Method để load thông tin user khi khởi tạo
    private void loadUserInfo() {
        if (customerID != null) {
            try {
                busProfile = new BUSProfile_cus();
                userName = busProfile.getCustomerName(customerID);
            } catch (Exception e) {
                userName = "User";
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Dashboard_user());
    }
}
