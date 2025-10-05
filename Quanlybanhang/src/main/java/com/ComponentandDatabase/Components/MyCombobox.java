package com.ComponentandDatabase.Components;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.plaf.basic.BasicComboBoxUI;
import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

public class MyCombobox<E> extends JComboBox<E> {
    private Color backgroundColor = Color.WHITE;
    private Color borderColor = Color.GRAY;
    private Color hoverColor = Color.decode("#1E90FF");
    private Color fontColor = Color.BLACK;
    private Font customFont = new Font("Arial", Font.PLAIN, 14);
    private int cornerRadius = 20;
    private int hoveredIndex = -1;
    
    // Modern UI properties
    private boolean isFocused = false;
    private Color focusColor = new Color(7, 164, 121);
    private boolean hasShadow = false;

    public MyCombobox(E[] items) {
        super(items);
        initComboBox();
    }

    public MyCombobox() {
        super();
        initComboBox();
    }

    private void initComboBox() {
        setOpaque(false);
        setFont(customFont);
        setForeground(fontColor);
        setBackground(backgroundColor);
        setBorder(new EmptyBorder(5, 10, 5, 10));
        setUI(new RoundedComboBoxUI());

        setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

                // Nếu đang hover thì tô màu
                if (index == hoveredIndex && !isSelected) {
                    setBackground(hoverColor);
                    setForeground(Color.BLACK);
                } else {
                    setBackground(isSelected ? list.getSelectionBackground() : Color.WHITE);
                    setForeground(isSelected ? list.getSelectionForeground() : Color.BLACK);
                }

                setFont(customFont);
                return this;
            }
        });

        addPopupMenuListener(new PopupMenuListener() {
            @Override
            public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
                SwingUtilities.invokeLater(() -> {
                    Object comp = getUI().getAccessibleChild(MyCombobox.this, 0);
                    if (comp instanceof JPopupMenu) {
                        JPopupMenu popup = (JPopupMenu) comp;
                        JScrollPane scrollPane = (JScrollPane) popup.getComponent(0);
                        JList<?> list = (JList<?>) scrollPane.getViewport().getView();

                        list.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
                            @Override
                            public void mouseMoved(java.awt.event.MouseEvent e) {
                                int index = list.locationToIndex(e.getPoint());
                                if (index != hoveredIndex) {
                                    hoveredIndex = index;
                                    list.repaint();
                                }
                            }
                        });

                        list.addMouseListener(new java.awt.event.MouseAdapter() {
                            @Override
                            public void mouseExited(java.awt.event.MouseEvent e) {
                                hoveredIndex = -1;
                                list.repaint();
                            }
                        });
                    }
                });
            }

            @Override
            public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
                hoveredIndex = -1; // Reset màu hover khi đóng dropdown
                repaint();
            }

            @Override public void popupMenuCanceled(PopupMenuEvent e) {}
        });

        addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                isFocused = true;
                borderColor = focusColor;
                repaint();
            }

            @Override
            public void focusLost(FocusEvent e) {
                isFocused = false;
                borderColor = Color.GRAY;
                repaint();
            }
        });
    }

    public void setCustomFont(Font font) {
        this.customFont = font;
        setFont(font);
        repaint();
    }

    public void setCustomColors(Color bg, Color border, Color font) {
        this.backgroundColor = bg;
        this.borderColor = border;
        this.fontColor = font;
        setForeground(fontColor);
        repaint();
    }
    
    // Modern UI methods
    public void setFocusColor(Color color) {
        this.focusColor = color;
        repaint();
    }
    
    public void setShadow(boolean hasShadow) {
        this.hasShadow = hasShadow;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
    }

    @Override
    protected void paintBorder(Graphics g) {
        // Viền đã được vẽ trong UI
    }

    private class RoundedComboBoxUI extends BasicComboBoxUI {
        @Override
        protected JButton createArrowButton() {
            JButton arrowButton = new JButton("▼");
            arrowButton.setFont(new Font("Arial", Font.BOLD, 12));
            arrowButton.setForeground(Color.BLACK);
            arrowButton.setBackground(new Color(220, 220, 220));
            arrowButton.setBorder(BorderFactory.createEmptyBorder());
            arrowButton.setFocusPainted(false);
            arrowButton.setContentAreaFilled(false);
            arrowButton.setOpaque(true);
            return arrowButton;
        }

        @Override
        public void paint(Graphics g, JComponent c) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

            // Vẽ shadow nếu được bật
            if (hasShadow && !isFocused) {
                g2.setColor(new Color(0, 0, 0, 20));
                g2.fillRoundRect(2, 2, c.getWidth(), c.getHeight(), cornerRadius, cornerRadius);
            }

            // Nền
            g2.setColor(backgroundColor);
            g2.fillRoundRect(0, 0, c.getWidth(), c.getHeight(), cornerRadius, cornerRadius);

            // Viền với hiệu ứng focus
            Color currentBorderColor = isFocused ? focusColor : borderColor;
            int borderThickness = isFocused ? 2 : 1;
            
            g2.setColor(currentBorderColor);
            g2.setStroke(new BasicStroke(borderThickness));
            g2.drawRoundRect(0, 0, c.getWidth() - 1, c.getHeight() - 1, cornerRadius, cornerRadius);

            // Gọi phần vẽ mặc định
            super.paint(g, c);
        }
    }

    public void refreshUI() {
        SwingUtilities.invokeLater(() -> {
            repaint();
            revalidate();
            updateUI();
        });
    }
}
