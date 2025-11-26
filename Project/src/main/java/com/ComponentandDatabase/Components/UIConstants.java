package com.ComponentandDatabase.Components;

import java.awt.Color;
import java.awt.Font;

/**
 * UI Constants for consistent design across all forms
 * Bộ hằng số UI để đảm bảo thiết kế nhất quán cho tất cả các form
 */
public class UIConstants {
    
    // ============================================
    // COLORS - MÀU SẮC CHUNG
    // ============================================
    
    // Primary Colors
    public static final Color PRIMARY_COLOR = Color.decode("#4CAF50");      // Xanh lá chủ đạo
    public static final Color PRIMARY_HOVER = Color.decode("#45a049");      // Xanh lá hover
    public static final Color PRIMARY_PRESSED = Color.decode("#3d8b40");    // Xanh lá pressed
    public static final Color PRIMARY_LIGHT = Color.decode("#E8F5E9");      // Xanh lá nhạt
    
    // Secondary Colors
    public static final Color DANGER_COLOR = Color.decode("#f44336");       // Đỏ (Xóa, Hủy)
    public static final Color DANGER_HOVER = Color.decode("#d32f2f");       // Đỏ hover
    
    public static final Color WARNING_COLOR = Color.decode("#FF9800");      // Cam (Cảnh báo, Sửa)
    public static final Color WARNING_HOVER = Color.decode("#F57C00");      // Cam hover
    
    public static final Color INFO_COLOR = Color.decode("#2196F3");         // Xanh dương (Thông tin)
    public static final Color INFO_HOVER = Color.decode("#1976D2");         // Xanh dương hover
    
    public static final Color SUCCESS_COLOR = Color.decode("#4CAF50");      // Xanh lá (Thành công)
    public static final Color SUCCESS_HOVER = Color.decode("#45a049");      // Xanh lá hover
    
    // Background Colors
    public static final Color BG_WHITE = Color.WHITE;                       // Nền trắng
    public static final Color BG_LIGHT_GRAY = Color.decode("#F5F5F5");      // Nền xám nhạt
    public static final Color BG_PANEL = Color.WHITE;                       // Nền panel
    
    // Border Colors
    public static final Color BORDER_GRAY = Color.GRAY;                     // Viền xám
    public static final Color BORDER_PRIMARY = PRIMARY_COLOR;               // Viền màu chủ đạo
    public static final Color BORDER_LIGHT = Color.decode("#E0E0E0");       // Viền nhạt
    
    // Table Colors
    public static final Color TABLE_HEADER_BG = PRIMARY_COLOR;              // Nền header table
    public static final Color TABLE_HEADER_FG = Color.WHITE;                // Chữ header table
    public static final Color TABLE_ROW_BG = Color.WHITE;                   // Nền dòng table
    public static final Color TABLE_ROW_FG = Color.BLACK;                   // Chữ dòng table
    public static final Color TABLE_SELECTION_BG = PRIMARY_LIGHT;           // Nền dòng chọn
    public static final Color TABLE_SELECTION_FG = Color.BLACK;             // Chữ dòng chọn
    public static final Color TABLE_ALTERNATE_BG = Color.decode("#F9F9F9"); // Nền dòng xen kẽ
    
    // Text Colors
    public static final Color TEXT_PRIMARY = Color.BLACK;                   // Chữ chính
    public static final Color TEXT_SECONDARY = Color.GRAY;                  // Chữ phụ
    public static final Color TEXT_WHITE = Color.WHITE;                     // Chữ trắng
    public static final Color TEXT_LINK = INFO_COLOR;                       // Chữ link
    
    // Status Colors (for badges, labels)
    public static final Color STATUS_ACTIVE_BG = Color.decode("#C8E6C9");   // Nền trạng thái hoạt động
    public static final Color STATUS_ACTIVE_FG = Color.decode("#1B5E20");   // Chữ trạng thái hoạt động
    
    public static final Color STATUS_INACTIVE_BG = Color.decode("#FFCDD2"); // Nền trạng thái không hoạt động
    public static final Color STATUS_INACTIVE_FG = Color.decode("#B71C1C"); // Chữ trạng thái không hoạt động
    
    public static final Color STATUS_PENDING_BG = Color.decode("#FFF9C4");  // Nền trạng thái chờ
    public static final Color STATUS_PENDING_FG = Color.decode("#F57F17");  // Chữ trạng thái chờ
    
    // ============================================
    // FONTS - FONT CHỮ CHUNG
    // ============================================
    
    // Title Fonts
    public static final Font FONT_TITLE_LARGE = new Font("Arial", Font.BOLD, 24);      // Tiêu đề lớn
    public static final Font FONT_TITLE_MEDIUM = new Font("Arial", Font.BOLD, 18);     // Tiêu đề vừa
    public static final Font FONT_TITLE_SMALL = new Font("Arial", Font.BOLD, 14);      // Tiêu đề nhỏ
    
    // Content Fonts
    public static final Font FONT_CONTENT_LARGE = new Font("Arial", Font.PLAIN, 16);   // Nội dung lớn
    public static final Font FONT_CONTENT_MEDIUM = new Font("Arial", Font.PLAIN, 13);  // Nội dung vừa
    public static final Font FONT_CONTENT_SMALL = new Font("Arial", Font.PLAIN, 11);   // Nội dung nhỏ
    
    // Button Fonts
    public static final Font FONT_BUTTON_LARGE = new Font("Arial", Font.BOLD, 14);     // Nút lớn
    public static final Font FONT_BUTTON_MEDIUM = new Font("Arial", Font.BOLD, 13);    // Nút vừa
    public static final Font FONT_BUTTON_SMALL = new Font("Arial", Font.BOLD, 11);     // Nút nhỏ
    
    // Table Fonts
    public static final Font FONT_TABLE_HEADER = new Font("Arial", Font.BOLD, 12);     // Header table
    public static final Font FONT_TABLE_CONTENT = new Font("Arial", Font.PLAIN, 12);   // Content table
    
    // Label Fonts
    public static final Font FONT_LABEL_BOLD = new Font("Arial", Font.BOLD, 13);       // Label đậm
    public static final Font FONT_LABEL_NORMAL = new Font("Arial", Font.PLAIN, 13);    // Label thường
    
    // ============================================
    // DIMENSIONS - KÍCH THƯỚC CHUNG
    // ============================================
    
    // Panel Dimensions
    public static final int PANEL_WIDTH = 1530;
    public static final int PANEL_HEIGHT = 860;
    
    // Search Panel
    public static final int SEARCH_PANEL_HEIGHT = 80;
    public static final int SEARCH_PANEL_PADDING = 10;
    
    // Form Panel
    public static final int FORM_PANEL_HEIGHT = 200;
    public static final int FORM_FIELD_HEIGHT = 35;
    public static final int FORM_LABEL_WIDTH = 150;
    
    // Button Dimensions
    public static final int BUTTON_HEIGHT_LARGE = 40;
    public static final int BUTTON_HEIGHT_MEDIUM = 35;
    public static final int BUTTON_HEIGHT_SMALL = 30;
    
    public static final int BUTTON_WIDTH_LARGE = 150;
    public static final int BUTTON_WIDTH_MEDIUM = 120;
    public static final int BUTTON_WIDTH_SMALL = 100;
    
    // Border Radius
    public static final int BORDER_RADIUS_LARGE = 20;
    public static final int BORDER_RADIUS_MEDIUM = 15;
    public static final int BORDER_RADIUS_SMALL = 10;
    
    // Border Width
    public static final int BORDER_WIDTH_THICK = 2;
    public static final int BORDER_WIDTH_THIN = 1;
    
    // Spacing
    public static final int SPACING_LARGE = 20;
    public static final int SPACING_MEDIUM = 15;
    public static final int SPACING_SMALL = 10;
    public static final int SPACING_TINY = 5;
    
    // ============================================
    // HELPER METHODS - HÀM HỖ TRỢ
    // ============================================
    
    /**
     * Tạo màu với độ trong suốt (alpha)
     */
    public static Color withAlpha(Color color, int alpha) {
        return new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha);
    }
    
    /**
     * Làm tối màu
     */
    public static Color darken(Color color, float factor) {
        return new Color(
            Math.max((int)(color.getRed() * factor), 0),
            Math.max((int)(color.getGreen() * factor), 0),
            Math.max((int)(color.getBlue() * factor), 0),
            color.getAlpha()
        );
    }
    
    /**
     * Làm sáng màu
     */
    public static Color lighten(Color color, float factor) {
        return new Color(
            Math.min((int)(color.getRed() + (255 - color.getRed()) * factor), 255),
            Math.min((int)(color.getGreen() + (255 - color.getGreen()) * factor), 255),
            Math.min((int)(color.getBlue() + (255 - color.getBlue()) * factor), 255),
            color.getAlpha()
        );
    }
    
    // ============================================
    // COMMON STYLES - STYLE CHUNG
    // ============================================
    
    /**
     * Style cho nút Primary (Xanh lá)
     */
    public static void stylePrimaryButton(MyButton button) {
        button.setBackgroundColor(PRIMARY_COLOR);
        button.setHoverColor(PRIMARY_HOVER);
        button.setPressedColor(PRIMARY_PRESSED);
        button.setForeground(TEXT_WHITE);
        button.setFont(FONT_BUTTON_MEDIUM);
    }
    
    /**
     * Style cho nút Danger (Đỏ)
     */
    public static void styleDangerButton(MyButton button) {
        button.setBackgroundColor(DANGER_COLOR);
        button.setHoverColor(DANGER_HOVER);
        button.setPressedColor(darken(DANGER_COLOR, 0.8f));
        button.setForeground(TEXT_WHITE);
        button.setFont(FONT_BUTTON_MEDIUM);
    }
    
    /**
     * Style cho nút Warning (Cam)
     */
    public static void styleWarningButton(MyButton button) {
        button.setBackgroundColor(WARNING_COLOR);
        button.setHoverColor(WARNING_HOVER);
        button.setPressedColor(darken(WARNING_COLOR, 0.8f));
        button.setForeground(TEXT_WHITE);
        button.setFont(FONT_BUTTON_MEDIUM);
    }
    
    /**
     * Style cho nút Info (Xanh dương)
     */
    public static void styleInfoButton(MyButton button) {
        button.setBackgroundColor(INFO_COLOR);
        button.setHoverColor(INFO_HOVER);
        button.setPressedColor(darken(INFO_COLOR, 0.8f));
        button.setForeground(TEXT_WHITE);
        button.setFont(FONT_BUTTON_MEDIUM);
    }
    
    /**
     * Style cho Table
     */
    public static MyTable createStyledTable(javax.swing.table.DefaultTableModel model) {
        return new MyTable(
            model,
            TABLE_ROW_BG,
            TABLE_ROW_FG,
            TABLE_SELECTION_BG,
            TABLE_SELECTION_FG,
            TABLE_HEADER_BG,
            TABLE_HEADER_FG,
            FONT_TABLE_CONTENT,
            FONT_TABLE_HEADER
        );
    }
}

