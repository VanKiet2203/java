package com.User.login_user.GUI;

import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import javax.swing.JLabel;
import javax.swing.ImageIcon;
import javax.swing.Timer;
import java.awt.Font;
import java.text.DecimalFormat;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import com.ComponentandDatabase.Components.MyButton;

public class PanelCover_User extends javax.swing.JPanel {
    private ActionListener event;
    private final DecimalFormat df = new DecimalFormat("##0.###");
    private JLabel imageLabel;
    private JLabel captionLabel;
    private JLabel shadowLabel;
    private JLabel sloganLabel;
    private MyButton signin;
    private boolean isSignUp = true;
    private Timer carouselTimer;
    private int currentIndex = 0;

    private final String[] imagePaths = new String[]{
        "src\\main\\resources\\Icons\\Admin_icon\\XDD004.png",
        "src\\main\\resources\\Icons\\Admin_icon\\XDD005.png",
        "src\\main\\resources\\Icons\\Admin_icon\\JS50.png"
    };

    public PanelCover_User() {
        setOpaque(false);
        setLayout(null);
        initComponents();
        init();
    }

    @SuppressWarnings("unchecked")
    private void initComponents() {
        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGap(0, 519, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGap(0, 419, Short.MAX_VALUE)
        );
    }

    @Override
    protected void paintComponent(Graphics grphcs) {
        Graphics2D g2 = (Graphics2D) grphcs;
        GradientPaint gra = new GradientPaint(
            0, 0, new Color(30, 166, 97),
            0, getHeight(), new Color(22, 116, 66)
        );
        g2.setPaint(gra);
        g2.fillRect(0, 0, getWidth(), getHeight());
        super.paintComponent(grphcs);
    }

    public void addEvent(ActionListener event) {
        this.event = event;
    }

    private void init() {
        // ====== Label hiển thị hình ======
        imageLabel = new JLabel();
        imageLabel.setBounds(0, 0, 420, 340);
        imageLabel.setHorizontalAlignment(JLabel.CENTER);
        add(imageLabel);

        // ====== Tiêu đề chính ======
        captionLabel = new JLabel("NPK STORE - Electric Bicycle Sales", JLabel.CENTER);
        captionLabel.setFont(new Font("SansSerif", Font.BOLD, 22));
        captionLabel.setForeground(Color.WHITE);

        // ====== Bóng chữ ======
        shadowLabel = new JLabel("NPK STORE - Electric Bicycle Sales", JLabel.CENTER);
        shadowLabel.setFont(new Font("SansSerif", Font.BOLD, 22));
        shadowLabel.setForeground(new Color(0, 0, 0, 150));

        // ====== Slogan phụ ======
        sloganLabel = new JLabel("Fast • Reliable • Eco-Friendly", JLabel.CENTER);
        sloganLabel.setFont(new Font("SansSerif", Font.ITALIC, 14));
        sloganLabel.setForeground(new Color(230, 230, 230));

        // Thêm các label chữ trực tiếp vào Panel (không nằm trong imageLabel)
        add(shadowLabel);
        add(captionLabel);
        add(sloganLabel);

        // Khi panel thay đổi kích thước, cập nhật vị trí ảnh và chữ
        addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent e) {
                int imgW = 420;
                int imgH = 340;
                int x = (getWidth() - imgW) / 2;
                int y = (getHeight() - imgH) / 2;
                imageLabel.setBounds(x, y, imgW, imgH);

                // Đặt chữ nằm dưới hình (bên dưới ảnh)
                int textY = y + imgH + 10;
                int maxY = getHeight() - 60; // tránh tràn cạnh dưới
                textY = Math.min(textY, maxY);
                shadowLabel.setBounds(3, textY + 3, getWidth(), 30);
                captionLabel.setBounds(0, textY, getWidth(), 30);
                sloganLabel.setBounds(0, textY + 25, getWidth(), 25);
            }
        });

        // Thiết lập vị trí ban đầu (trước khi resize xảy ra)
        int imgW0 = 420;
        int imgH0 = 340;
        int x0 = (getWidth() - imgW0) / 2;
        int y0 = (getHeight() - imgH0) / 2;
        imageLabel.setBounds(x0, y0, imgW0, imgH0);
        int textY0 = y0 + imgH0 + 10;
        int maxY0 = getHeight() - 60;
        textY0 = Math.min(textY0, maxY0);
        shadowLabel.setBounds(3, textY0 + 3, getWidth(), 30);
        captionLabel.setBounds(0, textY0, getWidth(), 30);
        sloganLabel.setBounds(0, textY0 + 25, getWidth(), 25);

        // Đảm bảo ảnh ở dưới cùng, chữ ở trên cùng về mặt z-order
        setComponentZOrder(imageLabel, getComponentCount() - 1);

        // Load hình đầu tiên
        updateImage();

        // ====== Tự động đổi hình ======
        carouselTimer = new Timer(2500, e -> {
            currentIndex = (currentIndex + 1) % imagePaths.length;
            updateImage();
        });
        carouselTimer.start();

        // ====== Hiệu ứng đổi màu chữ ======
        Timer colorTimer = new Timer(80, new ActionListener() {
            float hue = 0f;

            public void actionPerformed(ActionEvent e) {
                captionLabel.setForeground(Color.getHSBColor(hue, 0.5f, 1f));
                hue += 0.01f;
                if (hue > 1f) hue = 0f;
            }
        });
        colorTimer.start();
    }

    private void updateImage() {
        String path = imagePaths[currentIndex];
        ImageIcon icon = new ImageIcon(path);
        java.awt.Image scaled = icon.getImage().getScaledInstance(
            imageLabel.getWidth(), imageLabel.getHeight(), java.awt.Image.SCALE_SMOOTH
        );
        imageLabel.setIcon(new ImageIcon(scaled));
    }
}
