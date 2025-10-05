
package com.Admin.login.GUI;

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
import javax.swing.JButton;
import javax.swing.JPanel;
import net.miginfocom.swing.MigLayout;
import com.ComponentandDatabase.Components.MyButton;


public class PanelCover extends javax.swing.JPanel {
    private ActionListener event;
    private final DecimalFormat df= new DecimalFormat("##0.###");
    private MigLayout layout;
    private JLabel imageLabel;
    private JLabel captionLabel;
    private JLabel shadowLabel;
    private JLabel sloganLabel;
    private MyButton signin;
    private boolean isSignUp = true; // Mặc định là Sign up
    private Timer carouselTimer;
    private int currentIndex = 0;
    private final String[] imagePaths = new String[] {
        "src\\main\\resources\\Icons\\Admin_icon\\XDD004.png",
        "src\\main\\resources\\Icons\\Admin_icon\\XDD005.png",
        "src\\main\\resources\\Icons\\Admin_icon\\JS50.png"
    };

    public PanelCover() {
        setOpaque(false);
        setLayout(null);
        initComponents();
        setOpaque(false);
        init();
    }

  
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
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
    }// </editor-fold>//GEN-END:initComponents

    
    @Override
    protected void paintComponent(Graphics grphcs) {
        Graphics2D g2= (Graphics2D)grphcs;
        GradientPaint gra= new GradientPaint(0,0, new Color(30,166,97), 0, getHeight(), new Color(22,116,66));
        g2.setPaint(gra);
        g2.fillRect(0, 0, getWidth(), getHeight());
        super.paintComponent(grphcs);
        
    }
    
     public void addEvent(ActionListener event){
        this.event = event;
    }
     
    private void init() {
        imageLabel = new JLabel();
        captionLabel = new JLabel("Admin Portal", JLabel.CENTER);
        captionLabel.setFont(new Font("SansSerif", Font.BOLD, 22));
        captionLabel.setForeground(Color.WHITE);

        shadowLabel = new JLabel("Admin Portal", JLabel.CENTER);
        shadowLabel.setFont(new Font("SansSerif", Font.BOLD, 22));
        shadowLabel.setForeground(new Color(0, 0, 0, 150));

        sloganLabel = new JLabel("Manage • Monitor • Maintain", JLabel.CENTER);
        sloganLabel.setFont(new Font("SansSerif", Font.ITALIC, 14));
        sloganLabel.setForeground(new Color(230, 230, 230));
        // Center the image within the cover panel, and re-center on resize
        int imgW = 420;
        int imgH = 340;
        imageLabel.setSize(imgW, imgH);
        addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent e) {
                int x = (getWidth() - imageLabel.getWidth()) / 2;
                int y = (getHeight() - imageLabel.getHeight()) / 2;
                imageLabel.setLocation(x, y);
            }
        });
        // initial center
        int x = (getWidth() - imgW) / 2;
        int y = (getHeight() - imgH) / 2;
        imageLabel.setLocation(x, y);
        add(imageLabel);
        add(shadowLabel);
        add(captionLabel);
        add(sloganLabel);

        updateImage();
        carouselTimer = new Timer(2500, e -> {
            currentIndex = (currentIndex + 1) % imagePaths.length;
            updateImage();
        });
        carouselTimer.start();

        // Vị trí chữ dưới ảnh khi resize
        addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent e) {
                int textY = imageLabel.getY() + imageLabel.getHeight() + 10;
                int maxY = getHeight() - 60;
                textY = Math.min(textY, maxY);
                shadowLabel.setBounds(3, textY + 3, getWidth(), 30);
                captionLabel.setBounds(0, textY, getWidth(), 30);
                sloganLabel.setBounds(0, textY + 25, getWidth(), 25);
            }
        });

        // Thiết lập vị trí ban đầu cho chữ
        int textY0 = imageLabel.getY() + imageLabel.getHeight() + 10;
        int maxY0 = getHeight() - 60;
        textY0 = Math.min(textY0, maxY0);
        shadowLabel.setBounds(3, textY0 + 3, getWidth(), 30);
        captionLabel.setBounds(0, textY0, getWidth(), 30);
        sloganLabel.setBounds(0, textY0 + 25, getWidth(), 25);

        // Ảnh dưới, chữ trên (z-order)
        setComponentZOrder(imageLabel, getComponentCount() - 1);
    }

    private void updateImage() {
        String path = imagePaths[currentIndex];
        ImageIcon icon = new ImageIcon(path);
        java.awt.Image scaled = icon.getImage().getScaledInstance(imageLabel.getWidth(), imageLabel.getHeight(), java.awt.Image.SCALE_SMOOTH);
        imageLabel.setIcon(new ImageIcon(scaled));
    }
  
}
