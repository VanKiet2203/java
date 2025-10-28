package com.User.dashboard_user.GUI;

import java.awt.*;
import javax.swing.*;
import javax.swing.Box;
import com.ComponentandDatabase.Components.MyPanel;
import com.User.home.GUI.Home_user;
import com.User.Cart.GUI.Form_Cart;
import com.User.order.GUI.Order_Form;
import com.User.dashboard_user.BUS.BUSProfile_cus;
import com.User.login_user.GUI.PanelLoginandRegister_User;

public class Dashboard_user extends JFrame {
    private JPanel bg, contentPanel;
    
    private MyPanel panelTitle;
    private Menu_user menu;
    private CardLayout cardLayout;
    private BUSProfile_cus busProfile;
    public static String email, customerID;
    private String userName;

    public Dashboard_user() {
        this(null); // Gọi constructor với email = null
    }
    
    public Dashboard_user(String userEmail) {
        initComponents(userEmail);
        loadUserInfo(); // Load thông tin user
        setSize(1400, 900); // Kích thước tối ưu cho giao diện hiện đại
        setLocationRelativeTo(null); // Hiển thị giữa màn hình
        setResizable(true);
        setVisible(true);
    }

    
    private void initComponents(String userEmail) {
        // PANEL NỀN CHÍNH
        bg = new JPanel(new BorderLayout());

        // ===== HEADER (panelTitle) =====
        panelTitle = new MyPanel(new BorderLayout());
        panelTitle.setPreferredSize(new Dimension(0, 140));
        panelTitle.setGradientColors(Color.decode("#667eea"),
                                     Color.decode("#764ba2"),
                                     MyPanel.VERTICAL_GRADIENT);
        panelTitle.setBorder(BorderFactory.createMatteBorder(0, 0, 4, 0, Color.decode("#5a67d8")));

        // Icon cho tiêu đề (tùy bạn giữ/đổi đường dẫn)
        ImageIcon titleIcon = new ImageIcon(getClass().getResource("/Icons/Admin_icon/Title_icon.png"));
        titleIcon = new ImageIcon(titleIcon.getImage().getScaledInstance(50, 50, Image.SCALE_SMOOTH));

        // Tạo 2 label: Title & Subtitle
        JLabel lblTitle = new JLabel("NPK STORE", titleIcon, JLabel.CENTER);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 36));
        lblTitle.setForeground(Color.WHITE);
        lblTitle.setHorizontalAlignment(SwingConstants.CENTER);
        lblTitle.setVerticalAlignment(SwingConstants.CENTER);
        lblTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblTitle.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

        // Subtitle nằm NGAY DƯỚI title
        JLabel lblSubtitle = new JLabel("Electric Bicycle Sales Management System");
        lblSubtitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblSubtitle.setForeground(Color.WHITE);
        lblSubtitle.setHorizontalAlignment(SwingConstants.CENTER);
        lblSubtitle.setVerticalAlignment(SwingConstants.CENTER);
        lblSubtitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblSubtitle.setBorder(BorderFactory.createEmptyBorder(4, 0, 12, 0));

        // Container dọc để xếp 2 dòng
        JPanel textContainer = new JPanel();
        textContainer.setOpaque(false);
        textContainer.setLayout(new BoxLayout(textContainer, BoxLayout.Y_AXIS));
        // Thêm padding để tránh bị cắt ở cạnh dưới
        textContainer.setBorder(BorderFactory.createEmptyBorder(10, 20, 18, 20));
        textContainer.add(Box.createVerticalGlue());
        textContainer.add(lblTitle);
        textContainer.add(Box.createVerticalStrut(5)); // khoảng cách giữa 2 dòng
        textContainer.add(lblSubtitle);
        textContainer.add(Box.createVerticalGlue());

        // Tạo lớp phủ để đặt ảnh nền trang trí mờ và text chồng lên
        JPanel centerOverlay = new JPanel();
        centerOverlay.setOpaque(false);
        centerOverlay.setLayout(new OverlayLayout(centerOverlay));

        // Ảnh trang trí mờ để header sinh động hơn
        JLabel decorLabel = createTranslucentImage("/Icons/Admin_icon/title.png", 700, 220, 0.12f);
        decorLabel.setAlignmentX(0.5f);
        decorLabel.setAlignmentY(0.5f);

        // Căn giữa khối text
        textContainer.setAlignmentX(0.5f);
        textContainer.setAlignmentY(0.5f);

        centerOverlay.add(textContainer);
        centerOverlay.add(decorLabel);

        // Thêm lớp phủ vào header
        panelTitle.add(centerOverlay, BorderLayout.CENTER);

        // Hiệu ứng gloss nhẹ phía trên để tạo cảm giác chiều sâu
        JComponent gloss = new JComponent() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, new Color(255, 255, 255, 90), 0, getHeight(), new Color(255, 255, 255, 0));
                g2.setPaint(gp);
                g2.fillRoundRect(8, 4, getWidth() - 16, getHeight() - 6, 20, 20);
                g2.dispose();
            }
        };
        gloss.setOpaque(false);
        gloss.setPreferredSize(new Dimension(0, 28));
        panelTitle.add(gloss, BorderLayout.NORTH);

        panelTitle.add(textContainer, BorderLayout.CENTER);
        bg.add(panelTitle, BorderLayout.NORTH);

        // MENU + CONTENT với layout hiện đại
        menu = new Menu_user(this);
        menu.setLayout(new BoxLayout(menu, BoxLayout.Y_AXIS));
        menu.setPreferredSize(new Dimension(300, 0));
        menu.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 0, 3, Color.decode("#A0AEC0")),
            BorderFactory.createEmptyBorder(0, 0, 0, 0)
        ));

        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        contentPanel.setBackground(Color.WHITE); // Nền trắng sáng cho content
        contentPanel.setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(Color.WHITE);
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

    }

    private JLabel createTranslucentImage(String resourcePath, int width, int height, float alpha) {
        try {
            java.net.URL url = getClass().getResource(resourcePath);
            if (url == null) return new JLabel();
            ImageIcon icon = new ImageIcon(url);
            Image scaled = icon.getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH);
            ImageIcon scaledIcon = new ImageIcon(scaled);

            JLabel label = new JLabel(scaledIcon);
            label.setOpaque(false);
            label = new JLabel(scaledIcon) {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
                    g2.drawImage(scaledIcon.getImage(), 0, 0, null);
                    g2.dispose();
                }
            };
            label.setPreferredSize(new Dimension(width, height));
            return label;
        } catch (Exception e) {
            return new JLabel();
        }
    }


    public void showForm(String formName) {
        cardLayout.show(contentPanel, formName);
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
