package com.ComponentandDatabase.Components;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class MyButton extends JButton {
    private int radius; // Bo góc
    private Color backgroundColor = new Color(7, 164, 121);
    private Color pressedColor = new Color(5, 130, 95);
    private Color hoverColor = new Color(10, 190, 140);
    private boolean isHovering = false;
    private boolean hasShadow = true;
    private boolean hasGradient = false;
    private Color gradientStart = new Color(7, 164, 121);
    private Color gradientEnd = new Color(5, 130, 95);

    // Constructor
    public MyButton(String text, int radius) {
        super(text);
        this.radius = radius;
        setOpaque(false);
        setFocusPainted(false);
        setBorderPainted(false);
        setContentAreaFilled(false);

        // Thêm sự kiện chuột để đổi màu khi hover
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                isHovering = true;
                repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                isHovering = false;
                repaint();
            }

            @Override
            public void mousePressed(MouseEvent e) {
                repaint();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                repaint();
            }
        });
    }

    // Set màu nền bình thường
    public void setBackgroundColor(Color color) {
        this.backgroundColor = color;
        repaint();
    }

    // Set màu khi nhấn
    public void setPressedColor(Color color) {
        this.pressedColor = color;
    }

    // Set màu khi hover (rê chuột vào)
    public void setHoverColor(Color color) {
        this.hoverColor = color;
    }

    // Set shadow effect
    public void setShadow(boolean hasShadow) {
        this.hasShadow = hasShadow;
        repaint();
    }

    // Set gradient effect
    public void setGradient(boolean hasGradient, Color start, Color end) {
        this.hasGradient = hasGradient;
        this.gradientStart = start;
        this.gradientEnd = end;
        repaint();
    }

 // Set icon cho button với khả năng chỉnh vị trí
    public void setButtonIcon(String iconPath, int width, int height, int iconTextGap, int horizontalPos, int verticalPos) {
        ImageIcon icon = new ImageIcon(new ImageIcon(iconPath).getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH));
        setIcon(icon);
        setIconTextGap(iconTextGap); // Khoảng cách giữa icon và chữ
        setHorizontalTextPosition(horizontalPos); // Vị trí ngang của text so với icon
        setVerticalTextPosition(verticalPos); // Vị trí dọc của text so với icon
    }


    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        // Vẽ shadow nếu được bật
        if (hasShadow && !getModel().isPressed()) {
            g2.setColor(new Color(0, 0, 0, 30));
            g2.fillRoundRect(2, 2, getWidth(), getHeight(), radius, radius);
        }

        // Xác định màu dựa trên trạng thái
        Color currentColor;
        if (getModel().isPressed()) {
            currentColor = pressedColor;
        } else if (isHovering) {
            currentColor = hoverColor;
        } else {
            currentColor = backgroundColor;
        }

        // Vẽ button với gradient hoặc màu đơn
        if (hasGradient) {
            GradientPaint gradient = new GradientPaint(
                0, 0, gradientStart,
                0, getHeight(), gradientEnd
            );
            g2.setPaint(gradient);
        } else {
            g2.setColor(currentColor);
        }

        // Vẽ button với góc bo tròn
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), radius, radius);
        
        // Vẽ border tinh tế
        g2.setColor(new Color(255, 255, 255, 50));
        g2.setStroke(new BasicStroke(1f));
        g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, radius, radius);
        
        g2.dispose();

        super.paintComponent(g);
    }
}
