package com.Admin.promotion.GUI;

import com.Admin.promotion.BUS.BUSPromotion;
import com.Admin.promotion.DTO.DTOPromotion;
import com.ComponentandDatabase.Components.*;
import static com.ComponentandDatabase.Components.UIConstants.*;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class Form_Promotion extends JPanel {
    // Components
    private JPanel mainPanel, searchPanel, formPanel, tablePanel;
    private MyButton btnRefresh, btnSearch, btnAdd, btnEdit, btnDelete, btnClear, btnSave;
    private MyTextField txtSearch, txtPromotionCode, txtPromotionName, txtDiscountPercent;
    private MyCombobox<String> cmbSearchType;
    private MyTable tablePromotion;
    private DefaultTableModel tableModel;
    private JDateChooser dateStart, dateEnd;
    
    // Business Logic
    private BUSPromotion busPromotion;
    
    // State
    private boolean isEditing = false;
    private String selectedPromotionCode = null;
    
    public Form_Promotion() {
        initComponents();
        init();
    }
    
    private void initComponents() {
        setLayout(null);
        setPreferredSize(new Dimension(1530, 860));
        setBackground(Color.WHITE);
    }
    
    private void init() {
        mainPanel = new JPanel();
        mainPanel.setLayout(null);
        mainPanel.setBounds(0, 0, 1530, 860);
        mainPanel.setBackground(Color.WHITE);
        add(mainPanel);
        
        // Title
        JLabel lblTitle = new JLabel("QUẢN LÝ MÃ GIẢM GIÁ");
        lblTitle.setFont(new Font("Arial", Font.BOLD, 24));
        lblTitle.setForeground(PRIMARY_COLOR);
        lblTitle.setBounds(20, 10, 400, 40);
        mainPanel.add(lblTitle);
        
        // Create panels
        createSearchPanel();
        createFormPanel();
        createTablePanel();
        
        // Initialize business logic
        busPromotion = new BUSPromotion();
        
        // Load data
        loadPromotionData();
    }
    
    // ============================================
    // SEARCH PANEL
    // ============================================
    private void createSearchPanel() {
        searchPanel = new MyPanel(Color.WHITE);
        searchPanel.setLayout(null);
        searchPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(PRIMARY_COLOR, 2),
            "Tìm kiếm",
            0, 0,
            new Font("Arial", Font.BOLD, 14),
            PRIMARY_COLOR
        ));
        searchPanel.setBounds(20, 60, 1490, 80);
        mainPanel.add(searchPanel);
        
        // Search type combo
        String[] searchTypes = {"All", "Code", "Name", "Active", "Expired", "Upcoming"};
        cmbSearchType = new MyCombobox<>(searchTypes);
        cmbSearchType.setBounds(20, 30, 150, 35);
        cmbSearchType.setCustomFont(new Font("Arial", Font.PLAIN, 13));
        searchPanel.add(cmbSearchType);
        
        // Search text field
        txtSearch = new MyTextField();
        txtSearch.setHint("Nhập từ khóa tìm kiếm...");
        txtSearch.setBounds(180, 30, 300, 35);
        txtSearch.setTextFont(new Font("Arial", Font.PLAIN, 13));
        searchPanel.add(txtSearch);
        
        // Search button
        btnSearch = createButton("Tìm kiếm", 490, 30, 100, 35, PRIMARY_COLOR, PRIMARY_HOVER);
        btnSearch.addActionListener(e -> searchPromotion());
        searchPanel.add(btnSearch);
        
        // Refresh button
        btnRefresh = createButton("Làm mới", 600, 30, 100, 35, INFO_COLOR, INFO_HOVER);
        btnRefresh.addActionListener(e -> {
            txtSearch.setText("");
            cmbSearchType.setSelectedIndex(0);
            loadPromotionData();
        });
        searchPanel.add(btnRefresh);
        
        // Add button
        btnAdd = createButton("+ Thêm mới", 1340, 30, 130, 35, PRIMARY_COLOR, PRIMARY_HOVER);
        btnAdd.addActionListener(e -> prepareAddPromotion());
        searchPanel.add(btnAdd);
    }
    
    // ============================================
    // FORM PANEL
    // ============================================
    private void createFormPanel() {
        formPanel = new MyPanel(Color.WHITE);
        formPanel.setLayout(null);
        formPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(PRIMARY_COLOR, 2),
            "Thông tin mã giảm giá",
            0, 0,
            new Font("Arial", Font.BOLD, 14),
            PRIMARY_COLOR
        ));
        formPanel.setBounds(20, 150, 1490, 200);
        formPanel.setVisible(false); // Hidden by default
        mainPanel.add(formPanel);
        
        int labelX = 30;
        int fieldX = 200;
        int labelWidth = 150;
        int fieldWidth = 250;
        
        // Row 1: Promotion Code
        addLabel("Mã giảm giá:", labelX, 30, labelWidth, 30);
        txtPromotionCode = new MyTextField();
        txtPromotionCode.setBounds(fieldX, 30, fieldWidth, 35);
        txtPromotionCode.setTextFont(new Font("Arial", Font.PLAIN, 13));
        formPanel.add(txtPromotionCode);
        
        // Row 1: Promotion Name
        addLabel("Tên chương trình:", labelX + 550, 30, labelWidth, 30);
        txtPromotionName = new MyTextField();
        txtPromotionName.setBounds(fieldX + 550, 30, fieldWidth + 100, 35);
        txtPromotionName.setTextFont(new Font("Arial", Font.PLAIN, 13));
        formPanel.add(txtPromotionName);
        
        // Row 2: Start Date
        addLabel("Ngày bắt đầu:", labelX, 80, labelWidth, 30);
        dateStart = new JDateChooser();
        dateStart.setBounds(fieldX, 80, fieldWidth, 35);
        dateStart.setDateFormatString("dd/MM/yyyy");
        dateStart.setFont(new Font("Arial", Font.PLAIN, 13));
        formPanel.add(dateStart);
        
        // Row 2: End Date
        addLabel("Ngày kết thúc:", labelX + 550, 80, labelWidth, 30);
        dateEnd = new JDateChooser();
        dateEnd.setBounds(fieldX + 550, 80, fieldWidth, 35);
        dateEnd.setDateFormatString("dd/MM/yyyy");
        dateEnd.setFont(new Font("Arial", Font.PLAIN, 13));
        formPanel.add(dateEnd);
        
        // Row 3: Discount Percent
        addLabel("Giảm giá (%):", labelX, 130, labelWidth, 30);
        txtDiscountPercent = new MyTextField();
        txtDiscountPercent.setBounds(fieldX, 130, 150, 35);
        txtDiscountPercent.setTextFont(new Font("Arial", Font.PLAIN, 13));
        formPanel.add(txtDiscountPercent);
        
        // Buttons
        btnSave = createButton("💾 Lưu", 1100, 30, 120, 35, PRIMARY_COLOR, PRIMARY_HOVER);
        btnSave.addActionListener(e -> savePromotion());
        formPanel.add(btnSave);
        
        btnClear = createButton("🗑️ Hủy", 1100, 80, 120, 35, DANGER_COLOR, DANGER_HOVER);
        btnClear.addActionListener(e -> clearForm());
        formPanel.add(btnClear);
    }
    
    private void addLabel(String text, int x, int y, int width, int height) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Arial", Font.BOLD, 13));
        label.setBounds(x, y, width, height);
        formPanel.add(label);
    }
    
    // ============================================
    // TABLE PANEL
    // ============================================
    private void createTablePanel() {
        tablePanel = new MyPanel(Color.WHITE);
        tablePanel.setLayout(null);
        tablePanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(PRIMARY_COLOR, 2),
            "Danh sách mã giảm giá",
            0, 0,
            new Font("Arial", Font.BOLD, 14),
            PRIMARY_COLOR
        ));
        tablePanel.setBounds(20, 360, 1490, 480);
        mainPanel.add(tablePanel);
        
        // Create table
        String[] columns = {
            "STT", "Mã giảm giá", "Tên chương trình", 
            "Ngày bắt đầu", "Ngày kết thúc", "Giảm giá (%)", "Trạng thái"
        };
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        tablePromotion = new MyTable(
            tableModel,
            Color.WHITE, Color.BLACK,
            Color.decode("#E8F5E9"), Color.BLACK,
            PRIMARY_COLOR, Color.WHITE,
            new Font("Arial", Font.PLAIN, 12),
            new Font("Arial", Font.BOLD, 12)
        );
        tablePromotion.setRowHeight(30);
        
        // Set column widths
        tablePromotion.getColumnModel().getColumn(0).setPreferredWidth(50);   // STT
        tablePromotion.getColumnModel().getColumn(1).setPreferredWidth(120);  // Code
        tablePromotion.getColumnModel().getColumn(2).setPreferredWidth(300);  // Name
        tablePromotion.getColumnModel().getColumn(3).setPreferredWidth(120);  // Start
        tablePromotion.getColumnModel().getColumn(4).setPreferredWidth(120);  // End
        tablePromotion.getColumnModel().getColumn(5).setPreferredWidth(100);  // Discount
        tablePromotion.getColumnModel().getColumn(6).setPreferredWidth(150);  // Status
        
        // Center align for some columns
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        tablePromotion.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        tablePromotion.getColumnModel().getColumn(5).setCellRenderer(centerRenderer);
        
        // Custom renderer for status column
        tablePromotion.getColumnModel().getColumn(6).setCellRenderer(new StatusCellRenderer());
        
        // Add selection listener
        tablePromotion.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                onTableRowSelected();
            }
        });
        
        JScrollPane scrollPane = new JScrollPane(tablePromotion);
        scrollPane.setBounds(20, 30, 1450, 370);
        tablePanel.add(scrollPane);
        
        // Action buttons
        btnEdit = createButton("✏️ Sửa", 20, 410, 100, 35, WARNING_COLOR, WARNING_HOVER);
        btnEdit.addActionListener(e -> editSelectedPromotion());
        btnEdit.setEnabled(false);
        tablePanel.add(btnEdit);
        
        btnDelete = createButton("🗑️ Xóa", 130, 410, 100, 35, DANGER_COLOR, DANGER_HOVER);
        btnDelete.addActionListener(e -> deleteSelectedPromotion());
        btnDelete.setEnabled(false);
        tablePanel.add(btnDelete);
    }
    
    // ============================================
    // HELPER METHODS
    // ============================================
    
    private MyButton createButton(String text, int x, int y, int width, int height, Color bgColor, Color hoverColor) {
        MyButton btn = new MyButton(text, 20);
        btn.setBounds(x, y, width, height);
        btn.setBackgroundColor(bgColor);
        btn.setHoverColor(hoverColor);
        btn.setPressedColor(hoverColor.darker());
        btn.setFont(new Font("Arial", Font.BOLD, 13));
        btn.setForeground(Color.WHITE);
        return btn;
    }
    
    // ============================================
    // DATA OPERATIONS
    // ============================================
    
    private void loadPromotionData() {
        try {
            List<DTOPromotion> promotions = busPromotion.getAllPromotions();
            displayPromotions(promotions);
        } catch (Exception e) {
            showMessage("Lỗi khi tải dữ liệu: " + e.getMessage(), "error");
        }
    }
    
    private void searchPromotion() {
        try {
            String searchType = (String) cmbSearchType.getSelectedItem();
            String keyword = txtSearch.getText().trim();
            
            List<DTOPromotion> promotions = busPromotion.searchPromotions(searchType, keyword);
            displayPromotions(promotions);
        } catch (Exception e) {
            showMessage("Lỗi khi tìm kiếm: " + e.getMessage(), "error");
        }
    }
    
    private void displayPromotions(List<DTOPromotion> promotions) {
        tableModel.setRowCount(0);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        
        int stt = 1;
        for (DTOPromotion p : promotions) {
            String status = busPromotion.getPromotionStatus(p);
            
            tableModel.addRow(new Object[]{
                stt++,
                p.getPromotionCode(),
                p.getPromotionName(),
                p.getStartDate().format(formatter),
                p.getEndDate().format(formatter),
                p.getDiscountPercent() + "%",
                status
            });
        }
        
        updateStatusLabel();
    }
    
    private void updateStatusLabel() {
        try {
            int activeCount = busPromotion.countActivePromotions();
            tablePanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(PRIMARY_COLOR, 2),
                "Danh sách mã giảm giá (Tổng: " + tableModel.getRowCount() + 
                " | Đang hoạt động: " + activeCount + ")",
                0, 0,
                new Font("Arial", Font.BOLD, 14),
                PRIMARY_COLOR
            ));
        } catch (Exception e) {
            // Ignore
        }
    }
    
    // ============================================
    // FORM OPERATIONS
    // ============================================
    
    private void prepareAddPromotion() {
        isEditing = false;
        selectedPromotionCode = null;
        clearFormFields();
        formPanel.setVisible(true);
        tablePanel.setBounds(20, 360, 1490, 480);
        txtPromotionCode.setEditable(true);
        txtPromotionCode.requestFocus();
    }
    
    private void editSelectedPromotion() {
        if (selectedPromotionCode == null) {
            showMessage("Vui lòng chọn mã giảm giá cần sửa!", "error");
            return;
        }
        
        try {
            DTOPromotion promotion = busPromotion.getPromotionByCode(selectedPromotionCode);
            if (promotion == null) {
                showMessage("Không tìm thấy mã giảm giá!", "error");
                return;
            }
            
            isEditing = true;
            formPanel.setVisible(true);
            tablePanel.setBounds(20, 360, 1490, 480);
            
            // Fill form
            txtPromotionCode.setText(promotion.getPromotionCode());
            txtPromotionCode.setEditable(false);
            txtPromotionName.setText(promotion.getPromotionName());
            dateStart.setDate(java.sql.Date.valueOf(promotion.getStartDate()));
            dateEnd.setDate(java.sql.Date.valueOf(promotion.getEndDate()));
            txtDiscountPercent.setText(promotion.getDiscountPercent().toString());
            
        } catch (Exception e) {
            showMessage("Lỗi khi tải thông tin: " + e.getMessage(), "error");
        }
    }
    
    private void deleteSelectedPromotion() {
        if (selectedPromotionCode == null) {
            showMessage("Vui lòng chọn mã giảm giá cần xóa!", "error");
            return;
        }
        
        int confirm = JOptionPane.showConfirmDialog(
            this,
            "Bạn có chắc chắn muốn xóa mã giảm giá '" + selectedPromotionCode + "'?",
            "Xác nhận xóa",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE
        );
        
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                if (busPromotion.deletePromotion(selectedPromotionCode)) {
                    showMessage("Xóa mã giảm giá thành công!", "success");
                    loadPromotionData();
                    clearForm();
                } else {
                    showMessage("Xóa mã giảm giá thất bại!", "error");
                }
            } catch (Exception e) {
                showMessage("Lỗi: " + e.getMessage(), "error");
            }
        }
    }
    
    private void savePromotion() {
        try {
            // Validate inputs
            if (txtPromotionCode.getText().trim().isEmpty()) {
                showMessage("Mã giảm giá không được để trống!", "error");
                txtPromotionCode.requestFocus();
                return;
            }
            
            if (txtPromotionName.getText().trim().isEmpty()) {
                showMessage("Tên chương trình không được để trống!", "error");
                txtPromotionName.requestFocus();
                return;
            }
            
            if (dateStart.getDate() == null) {
                showMessage("Ngày bắt đầu không được để trống!", "error");
                return;
            }
            
            if (dateEnd.getDate() == null) {
                showMessage("Ngày kết thúc không được để trống!", "error");
                return;
            }
            
            if (txtDiscountPercent.getText().trim().isEmpty()) {
                showMessage("Phần trăm giảm giá không được để trống!", "error");
                txtDiscountPercent.requestFocus();
                return;
            }
            
            // Create promotion object
            DTOPromotion promotion = new DTOPromotion();
            promotion.setPromotionCode(txtPromotionCode.getText().trim());
            promotion.setPromotionName(txtPromotionName.getText().trim());
            promotion.setStartDate(dateStart.getDate().toInstant()
                .atZone(java.time.ZoneId.systemDefault()).toLocalDate());
            promotion.setEndDate(dateEnd.getDate().toInstant()
                .atZone(java.time.ZoneId.systemDefault()).toLocalDate());
            promotion.setDiscountPercent(new BigDecimal(txtDiscountPercent.getText().trim()));
            
            // Save
            boolean success;
            if (isEditing) {
                success = busPromotion.updatePromotion(promotion);
            } else {
                success = busPromotion.addPromotion(promotion);
            }
            
            if (success) {
                showMessage(
                    isEditing ? "Cập nhật mã giảm giá thành công!" : "Thêm mã giảm giá thành công!",
                    "success"
                );
                loadPromotionData();
                clearForm();
            } else {
                showMessage(
                    isEditing ? "Cập nhật thất bại!" : "Thêm mới thất bại!",
                    "error"
                );
            }
            
        } catch (NumberFormatException e) {
            showMessage("Phần trăm giảm giá phải là số!", "error");
        } catch (Exception e) {
            showMessage("Lỗi: " + e.getMessage(), "error");
        }
    }
    
    private void clearForm() {
        clearFormFields();
        formPanel.setVisible(false);
        tablePanel.setBounds(20, 150, 1490, 690);
        isEditing = false;
        selectedPromotionCode = null;
    }
    
    private void clearFormFields() {
        txtPromotionCode.setText("");
        txtPromotionName.setText("");
        dateStart.setDate(null);
        dateEnd.setDate(null);
        txtDiscountPercent.setText("");
        txtPromotionCode.setEditable(true);
    }
    
    private void onTableRowSelected() {
        int selectedRow = tablePromotion.getSelectedRow();
        if (selectedRow >= 0) {
            selectedPromotionCode = (String) tableModel.getValueAt(selectedRow, 1);
            btnEdit.setEnabled(true);
            btnDelete.setEnabled(true);
        } else {
            selectedPromotionCode = null;
            btnEdit.setEnabled(false);
            btnDelete.setEnabled(false);
        }
    }
    
    // ============================================
    // MESSAGE HELPER
    // ============================================
    
    private void showMessage(String message, String type) {
        if (type.equals("success")) {
            JOptionPane.showMessageDialog(this, message, "Thành công", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this, message, "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    // ============================================
    // STATUS CELL RENDERER
    // ============================================
    
    private class StatusCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            
            if (value != null) {
                String status = value.toString();
                setHorizontalAlignment(CENTER);
                
                if (!isSelected) {
                    if (status.equals("Đang hoạt động")) {
                        c.setBackground(new Color(200, 255, 200));
                        c.setForeground(new Color(0, 100, 0));
                    } else if (status.equals("Đã hết hạn")) {
                        c.setBackground(new Color(255, 200, 200));
                        c.setForeground(new Color(139, 0, 0));
                    } else if (status.equals("Sắp diễn ra")) {
                        c.setBackground(new Color(255, 255, 200));
                        c.setForeground(new Color(139, 69, 0));
                    }
                }
            }
            
            return c;
        }
    }
}

