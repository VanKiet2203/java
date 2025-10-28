package com.User.dashboard_user.GUI;


import java.awt.Color;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.*;
import java.awt.*;
import java.io.File;
import javax.imageio.ImageIO;
import java.io.IOException;
import java.awt.event.*; // Import đầy đủ các sự kiện
import com.ComponentandDatabase.Components.CustomDialog;


public class Menu_user extends JPanel {
    
    private JLabel lblHome, lblCart, lblOrder, lblProfile, lblExit, lblMenu;
    private JPanel menuPanel; // Panel chứa tất cả label
     private boolean isMenuExpanded = false;
    public Dashboard_user parentFrame;
        private Color hoverColor = Color.decode("#7C3AED"); // Màu khi hover hiện đại
        private Color selectedColor = Color.decode("#8B5CF6"); // Màu khi được chọn hiện đại

    private Color transparentColor = new Color(0, 0, 0, 0); // Màu trong suốt
    private JLabel selectedLabel = null; // Label đang được chọn
    public CustomDialog cs;
      // Biến lưu label đang được hover


    public Menu_user(Dashboard_user parentFrame) {
        this.parentFrame=parentFrame;
        initComponents();
        init(); 
        setOpaque(false);
    }

    private void initComponents() {
        setLayout(null); // Dùng null layout để tự thiết lập vị trí các thành phần
    }

  
    private void init() {
     try {
        // Tạo panel chứa menu với design hiện đại
        menuPanel = new JPanel();
        menuPanel.setLayout(null);
        menuPanel.setOpaque(false);
        menuPanel.setBounds(0, 0, 300, 1000);
        
        // Tạo header section với user info hiện đại
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        headerPanel.setBounds(0, 0, 300, 90);
        
        // User profile với design hiện đại
        String userName = parentFrame.getUserName();
        lblProfile = createLabelWithIcon(userName != null ? userName : "User Profile", "profile.png", 20, 20, 260, 60, true);
        lblProfile.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblProfile.setForeground(Color.WHITE);
        lblProfile.setHorizontalAlignment(SwingConstants.CENTER);
        lblProfile.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.decode("#A0AEC0"), 2),
            BorderFactory.createEmptyBorder(15, 20, 15, 20)
        ));
        
        // Menu toggle button với design hiện đại
        lblMenu = createLabelWithIcon("Navigation Menu", "menu.png", 20, 100, 260, 55, false);
        lblMenu.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblMenu.setForeground(Color.WHITE);
        lblMenu.setHorizontalAlignment(SwingConstants.CENTER);
        lblMenu.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 3, 0, Color.decode("#A0AEC0")),
            BorderFactory.createEmptyBorder(15, 20, 15, 20)
        ));
 
        lblMenu.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    toggleMenu();
                }
            });

        // Đọc ảnh từ đường dẫn tuyệt đối
//        lblTitle = createLabelWithIcon("Sales Management<br><p style='margin-left: 20px;'>Application</p>",
//                "Title_icon.png", 20, 20, 260, 55, true);

        // Menu items với design hiện đại và typography cải tiến
        lblHome = createLabelWithIcon("Home Dashboard", "home.png", 20, 170, 260, 50, false);
        lblHome.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblHome.setForeground(Color.WHITE);
        lblHome.setHorizontalAlignment(SwingConstants.LEFT);
        lblHome.setBorder(BorderFactory.createEmptyBorder(12, 25, 12, 25));
        
        lblCart = createLabelWithIcon("Shopping Cart", "cart.png", 20, 235, 260, 50, false);
        lblCart.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblCart.setForeground(Color.WHITE);
        lblCart.setHorizontalAlignment(SwingConstants.LEFT);
        lblCart.setBorder(BorderFactory.createEmptyBorder(12, 25, 12, 25));
        
        lblOrder = createLabelWithIcon("My Orders", "order.png", 20, 300, 260, 50, false);
        lblOrder.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblOrder.setForeground(Color.WHITE);
        lblOrder.setHorizontalAlignment(SwingConstants.LEFT);
        lblOrder.setBorder(BorderFactory.createEmptyBorder(12, 25, 12, 25));
        
        lblExit = createLabelWithIcon("Logout", "exit.png", 20, 365, 260, 50, false);
        lblExit.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblExit.setForeground(Color.WHITE);
        lblExit.setHorizontalAlignment(SwingConstants.LEFT);
        lblExit.setBorder(BorderFactory.createEmptyBorder(12, 25, 12, 25));
//        
        
        // Thêm hiệu ứng hover
        addHoverEffectForExit(lblMenu);
        addHoverEffectForExit(lblHome);
        addHoverEffectForExit(lblCart);
        addHoverEffectForExit(lblOrder);
        addHoverEffectForExit(lblProfile);
        addHoverEffectForExit(lblExit);

        // Thêm các label vào menuPanel với thứ tự khoa học
        menuPanel.add(lblProfile);
        menuPanel.add(lblMenu);
        menuPanel.add(lblHome);
        menuPanel.add(lblCart);
        menuPanel.add(lblOrder);
        menuPanel.add(lblExit);
//        
       
        lblHome.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                parentFrame.showForm("Home_user");
            }
        });
        
         lblProfile.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                MyProfile_cus profile= new MyProfile_cus();
                profile.setVisible(true);
            }
        });
        
          lblCart.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                parentFrame.showForm("Form_Cart");
            
            }
        });
          
          lblOrder.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                parentFrame.showForm("Order_Form");
            }
        });
         
         
        

      lblExit.addMouseListener(new MouseAdapter() {
        @Override
        public void mouseClicked(MouseEvent e) {
            int confirm = JOptionPane.showConfirmDialog(
                Menu_user.this,
                "Are you sure you want to logout?",
                "Logout Confirmation",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
            );

            if (confirm == JOptionPane.YES_OPTION) {
                // Đóng frame hiện tại và quay về login
                JFrame currentFrame = (JFrame) SwingUtilities.getWindowAncestor(Menu_user.this);
                if (currentFrame != null) {
                    currentFrame.dispose();
                }
                // Mở lại form login
                com.User.login_user.GUI.Login_User loginForm = new com.User.login_user.GUI.Login_User();
                loginForm.setVisible(true);
            }
        }
});

        // Thêm menuPanel vào frame
        setLayout(null);
        add(menuPanel);
   } catch (IOException e) {
        e.printStackTrace();
    }
}

    
    private void addHoverEffectForExit(JLabel label) {
     label.addMouseListener(new MouseAdapter() {
        @Override
        public void mouseEntered(MouseEvent e) {
            // Áp dụng hover nếu label không được chọn
            if (label != selectedLabel) {
                setLabelHoverStyle(label);
            }
        }

        @Override
        public void mouseExited(MouseEvent e) {
            // Chỉ reset nếu label không được chọn
            if (label != selectedLabel) {
                resetLabelStyle(label);
            }
        }

        @Override
        public void mouseClicked(MouseEvent e) {
            // Nếu click vào label đang được chọn thì bỏ chọn
            if (label == selectedLabel) {
                resetLabelStyle(label);
                selectedLabel = null;
            } 
            // Nếu click vào label khác
            else {
                // Bỏ chọn label cũ (nếu có)
                if (selectedLabel != null) {
                    resetLabelStyle(selectedLabel);
                }
                
                // Chọn label mới
                selectedLabel = label;
                setLabelSelectedStyle(label);
            }
            
        }
    });
}

    // Style khi hover
    private void setLabelHoverStyle(JLabel label) {
        label.setOpaque(true);
        label.setBackground(hoverColor);
        label.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        label.revalidate();
        label.repaint();
    }

    // Style khi được chọn (dính cứng)
    private void setLabelSelectedStyle(JLabel label) {
        label.setOpaque(true);
        label.setBackground(selectedColor); // Màu khác với hover để phân biệt
        label.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        label.revalidate();
        label.repaint();
    }

    // Reset style về mặc định
    private void resetLabelStyle(JLabel label) {
        label.setOpaque(false);
        label.setBackground(transparentColor);
        label.setCursor(Cursor.getDefaultCursor());
        label.revalidate();
        label.repaint();
    }


    // Tạo label có icon với typography khoa học
    private JLabel createLabelWithIcon(String text, String iconName, int x, int y, int width, int height, boolean isTitle) throws IOException {
        // Kiểm tra file ảnh
        File file = new File("src\\main\\resources\\Icons\\User_icon\\" + iconName);
        if (!file.exists()) {
            System.err.println("⚠️ File not found: " + file.getAbsolutePath());
            return new JLabel(text);
        }

        // Đọc và resize ảnh với chất lượng cao
        Image img = ImageIO.read(file).getScaledInstance(32, 32, Image.SCALE_SMOOTH);
        ImageIcon icon = new ImageIcon(img);

        // Tạo JLabel với typography khoa học
        JLabel label = new JLabel(text, icon, JLabel.LEFT);
        
        // Typography hierarchy
        if (isTitle) {
            label.setFont(new Font("Segoe UI", Font.BOLD, 20));
        } else {
            label.setFont(new Font("Segoe UI", Font.BOLD, 15));
        }
        
        label.setForeground(Color.WHITE);
        label.setBounds(x, y, width, height);
        label.setIconTextGap(15); // Tăng khoảng cách giữa icon và text
        label.setOpaque(false);
        label.setBorder(BorderFactory.createEmptyBorder(12, 20, 12, 20)); // Padding khoa học
        return label;
    }


     // Hiển thị hoặc ẩn các label con khi bấm vào lblMenu
    private void toggleMenu() {
        isMenuExpanded = !isMenuExpanded;
        setMenuVisibility(isMenuExpanded);
    }

    private void setMenuVisibility(boolean isVisible) {
        lblHome.setVisible(isVisible);
        lblCart.setVisible(isVisible);
        lblOrder.setVisible(isVisible);
//        lblCategory.setVisible(isVisible);
//        lblProduct.setVisible(isVisible);
//        lblImport.setVisible(isVisible);
//        lblExport.setVisible(isVisible);
//        lblStock.setVisible(isVisible);
//        lblInsurance.setVisible(isVisible);
//        lblStatistics.setVisible(isVisible);
//        lblMessage.setVisible(isVisible);
        lblExit.setVisible(isVisible);
        menuPanel.revalidate();
        menuPanel.repaint();
    }

    
      @Override
    protected void paintChildren(Graphics grphcs) {
    Graphics2D g2 = (Graphics2D) grphcs;
    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // Tạo gradient background hiện đại và chuyên nghiệp
        GradientPaint g = new GradientPaint(0, 0, Color.decode("#667eea"), 0, getHeight(), Color.decode("#764ba2"));
        g2.setPaint(g);

        // Vẽ nền với border radius hiện đại
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);

        // Thêm border hiện đại với shadow effect
        g2.setColor(Color.decode("#5a67d8"));
        g2.setStroke(new java.awt.BasicStroke(3));
        g2.drawRoundRect(2, 2, getWidth()-4, getHeight()-4, 15, 15);

    super.paintChildren(grphcs);
}

}
