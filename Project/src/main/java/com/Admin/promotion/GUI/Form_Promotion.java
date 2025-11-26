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
import javax.swing.SwingConstants;

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
        setPreferredSize(new Dimension(1200, 700)); // Thu nhỏ từ 1530x860 xuống 1200x700
        setBackground(Color.WHITE);
    }
    
    private void init() {
        mainPanel = new JPanel();
        mainPanel.setLayout(null);
        mainPanel.setBounds(0, 0, 1200, 700); // Thu nhỏ mainPanel
        mainPanel.setBackground(Color.WHITE);
        add(mainPanel);
        
        // Title
        JLabel lblTitle = new JLabel("MANAGE PROMOTION");
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
            "Search",
            0, 0,
            FONT_TITLE_SMALL,
            PRIMARY_COLOR
        ));
        searchPanel.setBounds(20, 60, 1160, 100); // Thu nhỏ từ 1490 xuống 1160
        mainPanel.add(searchPanel);
        
        // Search type combo
        String[] searchTypes = {"All", "Code", "Name", "Active", "Expired", "Upcoming"};
        cmbSearchType = new MyCombobox<>(searchTypes);
        cmbSearchType.setBounds(20, 30, 150, 35);
        cmbSearchType.setCustomFont(new Font("Arial", Font.PLAIN, 13));
        searchPanel.add(cmbSearchType);
        
        // Search text field - THÊM VIỀN
        txtSearch = new MyTextField();
        txtSearch.setHint("Search something...");
        txtSearch.setBounds(180, 30, 250, 35); // Thu nhỏ từ 300 xuống 250
        txtSearch.setTextFont(new Font("Arial", Font.PLAIN, 13));
        txtSearch.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(PRIMARY_COLOR, 1),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        searchPanel.add(txtSearch);
        
        // Search button
        btnSearch = createButton("Search", 440, 30, 120, 35, PRIMARY_COLOR, PRIMARY_HOVER); // Điều chỉnh vị trí
        btnSearch.setButtonIcon("src\\main\\resources\\Icons\\Admin_icon\\search.png", 20, 20, 5, SwingConstants.RIGHT, SwingConstants.CENTER);
        btnSearch.addActionListener(e -> searchPromotion());
        searchPanel.add(btnSearch);
        
        // Refresh button
        btnRefresh = createButton("Refresh", 570, 30, 120, 35, INFO_COLOR, INFO_HOVER); // Điều chỉnh vị trí
        btnRefresh.setButtonIcon("src\\main\\resources\\Icons\\Admin_icon\\refresh.png", 20, 20, 5, SwingConstants.RIGHT, SwingConstants.CENTER);
        btnRefresh.addActionListener(e -> {
            txtSearch.setText("");
            cmbSearchType.setSelectedIndex(0);
            loadPromotionData();
        });
        searchPanel.add(btnRefresh);
        
        // Add button
        btnAdd = createButton("Add new", 700, 30, 120, 35, PRIMARY_COLOR, PRIMARY_HOVER); // Điều chỉnh vị trí
        btnAdd.setButtonIcon("src\\main\\resources\\Icons\\Admin_icon\\new.png", 20, 20, 5, SwingConstants.RIGHT, SwingConstants.CENTER);
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
            "Promotion information",
            0, 0,
            new Font("Arial", Font.BOLD, 14),
            PRIMARY_COLOR
        ));
        formPanel.setBounds(20, 180, 1160, 200); // Thu nhỏ từ 1490 xuống 1160
        formPanel.setVisible(false); // Hidden by default
        mainPanel.add(formPanel);
        
        int labelX = 30;
        int fieldX = 200;
        int labelWidth = 120; // Thu nhỏ từ 150 xuống 120
        int fieldWidth = 200; // Thu nhỏ từ 250 xuống 200
        
        // Row 1: Promotion Code
        addLabel("Promotion Code:", labelX, 30, labelWidth, 30);
        txtPromotionCode = new MyTextField();
        txtPromotionCode.setBounds(fieldX, 30, fieldWidth, 35);
        txtPromotionCode.setTextFont(new Font("Arial", Font.PLAIN, 13));
        txtPromotionCode.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(PRIMARY_COLOR, 1),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        formPanel.add(txtPromotionCode);
        
        // Row 1: Promotion Name
        addLabel("Promotion Name:", labelX + 450, 30, labelWidth, 30); // Điều chỉnh vị trí
        txtPromotionName = new MyTextField();
        txtPromotionName.setBounds(fieldX + 450, 30, fieldWidth + 50, 35); // Điều chỉnh vị trí và kích thước
        txtPromotionName.setTextFont(new Font("Arial", Font.PLAIN, 13));
        txtPromotionName.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(PRIMARY_COLOR, 1),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        formPanel.add(txtPromotionName);
        
        // Row 2: Start Date
        addLabel("Start Date:", labelX, 80, labelWidth, 30);
        dateStart = new JDateChooser();
        dateStart.setBounds(fieldX, 80, fieldWidth, 35);
        dateStart.setDateFormatString("dd/MM/yyyy");
        dateStart.setFont(new Font("Arial", Font.PLAIN, 13));
        dateStart.setBorder(BorderFactory.createLineBorder(PRIMARY_COLOR, 1));
        formPanel.add(dateStart);
        
        // Row 2: End Date
        addLabel("End Date:", labelX + 450, 80, labelWidth, 30); // Điều chỉnh vị trí
        dateEnd = new JDateChooser();
        dateEnd.setBounds(fieldX + 450, 80, fieldWidth, 35); // Điều chỉnh vị trí
        dateEnd.setDateFormatString("dd/MM/yyyy");
        dateEnd.setFont(new Font("Arial", Font.PLAIN, 13));
        dateEnd.setBorder(BorderFactory.createLineBorder(PRIMARY_COLOR, 1));
        formPanel.add(dateEnd);
        
        // Row 3: Discount Percent
        addLabel("Discount Percent:", labelX, 130, labelWidth, 30);
        txtDiscountPercent = new MyTextField();
        txtDiscountPercent.setBounds(fieldX, 130, 120, 35); // Thu nhỏ từ 150 xuống 120
        txtDiscountPercent.setTextFont(new Font("Arial", Font.PLAIN, 13));
        txtDiscountPercent.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(PRIMARY_COLOR, 1),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        formPanel.add(txtDiscountPercent);
        
        // Buttons
        btnSave = createButton("Save", 950, 30, 100, 35, PRIMARY_COLOR, PRIMARY_HOVER); // Điều chỉnh vị trí
        btnSave.setButtonIcon("src\\main\\resources\\Icons\\Admin_icon\\update.png", 20, 20, 5, SwingConstants.RIGHT, SwingConstants.CENTER);
        btnSave.addActionListener(e -> savePromotion());
        formPanel.add(btnSave);
        
        btnClear = createButton("Cancel", 950, 80, 120, 35, DANGER_COLOR, DANGER_HOVER); // Điều chỉnh vị trí
        btnClear.setButtonIcon("src\\main\\resources\\Icons\\Admin_icon\\delete.png", 20, 20, 5, SwingConstants.RIGHT, SwingConstants.CENTER);
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
            "Promotion List",
            0, 0,
            new Font("Arial", Font.BOLD, 14),
            PRIMARY_COLOR
        ));
        tablePanel.setBounds(20, 400, 1160, 280); // Thu nhỏ từ 1490x460 xuống 1160x280
        mainPanel.add(tablePanel);
        
        // Create table
        String[] columns = {
            "STT", "Promotion Code", "Promotion Name", 
            "Start Date", "End Date", "Discount (%)", "Status"
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
        
        // Set column widths - Điều chỉnh để vừa với chiều rộng mới
        tablePromotion.getColumnModel().getColumn(0).setPreferredWidth(40);   // STT
        tablePromotion.getColumnModel().getColumn(1).setPreferredWidth(100);  // Code
        tablePromotion.getColumnModel().getColumn(2).setPreferredWidth(200);  // Name
        tablePromotion.getColumnModel().getColumn(3).setPreferredWidth(100);  // Start
        tablePromotion.getColumnModel().getColumn(4).setPreferredWidth(100);  // End
        tablePromotion.getColumnModel().getColumn(5).setPreferredWidth(80);   // Discount
        tablePromotion.getColumnModel().getColumn(6).setPreferredWidth(100);  // Status
        
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
        scrollPane.setBounds(20, 30, 1120, 190); // Thu nhỏ từ 1450x370 xuống 1120x190
        tablePanel.add(scrollPane);
        
        // Action buttons
        btnEdit = createButton("Edit", 20, 230, 100, 35, WARNING_COLOR, WARNING_HOVER); // Điều chỉnh vị trí
        btnEdit.setButtonIcon("src\\main\\resources\\Icons\\Admin_icon\\edit.png", 20, 20, 5, SwingConstants.RIGHT, SwingConstants.CENTER);
        btnEdit.addActionListener(e -> editSelectedPromotion());
        btnEdit.setEnabled(false);
        tablePanel.add(btnEdit);
        
        btnDelete = createButton("Delete", 130, 230, 100, 35, DANGER_COLOR, DANGER_HOVER); // Điều chỉnh vị trí
        btnDelete.setButtonIcon("src\\main\\resources\\Icons\\Admin_icon\\delete.png", 20, 20, 5, SwingConstants.RIGHT, SwingConstants.CENTER);
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
            showMessage("Error loading data: " + e.getMessage(), "error");
        }
    }
    
    private void searchPromotion() {
        try {
            String searchType = (String) cmbSearchType.getSelectedItem();
            String keyword = txtSearch.getText().trim();
            
            List<DTOPromotion> promotions = busPromotion.searchPromotions(searchType, keyword);
            displayPromotions(promotions);
        } catch (Exception e) {
            showMessage("Error searching: " + e.getMessage(), "error");
        }
    }
    
    private void displayPromotions(List<DTOPromotion> promotions) {
        tableModel.setRowCount(0);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        
        int stt = 1;
        // Chỉ hiển thị các promotion có Status = 'Available' (đã được filter trong getAllPromotions)
        // Và translate status để hiển thị đúng
        for (DTOPromotion p : promotions) {
            String status = busPromotion.getPromotionStatus(p);
            // Translate status
            String displayStatus;
            switch (status) {
                case "Đang hoạt động":
                    displayStatus = "Active";
                    break;
                case "Đã hết hạn":
                    displayStatus = "Expired";
                    break;
                case "Sắp diễn ra":
                    displayStatus = "Upcoming";
                    break;
                default:
                    displayStatus = status;
            }
            
            tableModel.addRow(new Object[]{
                stt++,
                p.getPromotionCode(),
                p.getPromotionName(),
                p.getStartDate().format(formatter),
                p.getEndDate().format(formatter),
                p.getDiscountPercent() + "%",
                displayStatus
            });
        }
        
        updateStatusLabel();
    }
    
    private void updateStatusLabel() {
        try {
            int activeCount = busPromotion.countActivePromotions();
            tablePanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(PRIMARY_COLOR, 2),
                "Promotion List (Total: " + tableModel.getRowCount() + 
                " | Active: " + activeCount + ")",
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
        txtPromotionCode.setEditable(true);
        txtPromotionCode.requestFocus();
    }
    
    private void editSelectedPromotion() {
        if (selectedPromotionCode == null) {
            showMessage("Please select the promotion to edit!", "error");
            return;
        }
        
        try {
            DTOPromotion promotion = busPromotion.getPromotionByCode(selectedPromotionCode);
            if (promotion == null) {
                showMessage("Promotion not found!", "error");
                return;
            }
            
            isEditing = true;
            formPanel.setVisible(true);
            
            // Fill form
            txtPromotionCode.setText(promotion.getPromotionCode());
            txtPromotionCode.setEditable(false);
            txtPromotionName.setText(promotion.getPromotionName());
            dateStart.setDate(java.sql.Date.valueOf(promotion.getStartDate()));
            dateEnd.setDate(java.sql.Date.valueOf(promotion.getEndDate()));
            txtDiscountPercent.setText(promotion.getDiscountPercent().toString());
            
        } catch (Exception e) {
            showMessage("Error loading information: " + e.getMessage(), "error");
        }
    }
    
    private void deleteSelectedPromotion() {
        if (selectedPromotionCode == null) {
            showMessage("Please select the promotion to delete!", "error");
            return;
        }
        
        int confirm = JOptionPane.showConfirmDialog(
            this,
            "Are you sure you want to delete the promotion '" + selectedPromotionCode + "'?",
            "Confirm delete",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE
        );
        
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                if (busPromotion.deletePromotion(selectedPromotionCode)) {
                    showMessage("Delete promotion successfully!", "success");
                    loadPromotionData();
                    clearForm();
                    // Refresh promotion list in Form_Export if it exists
                    com.Admin.export.GUI.Form_Export.refreshPromotionsIfExists();
                } else {
                    showMessage("Delete promotion failed!", "error");
                }
            } catch (Exception e) {
                showMessage("Error: " + e.getMessage(), "error");
            }
        }
    }
    
    private void savePromotion() {
        try {
            // Validate inputs
            if (txtPromotionCode.getText().trim().isEmpty()) {
                showMessage("Promotion code cannot be empty!", "error");
                txtPromotionCode.requestFocus();
                return;
            }
            
            if (txtPromotionName.getText().trim().isEmpty()) {
                showMessage("Promotion name cannot be empty!", "error");
                txtPromotionName.requestFocus();
                return;
            }
            
            if (dateStart.getDate() == null) {
                showMessage("Start date cannot be empty!", "error");
                return;
            }
            
            if (dateEnd.getDate() == null) {
                showMessage("End date cannot be empty!", "error");
                return;
            }
            
            if (txtDiscountPercent.getText().trim().isEmpty()) {
                showMessage("Discount percent cannot be empty!", "error");
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
                    isEditing ? "Update promotion successfully!" : "Add promotion successfully!",
                    "success"
                );
                loadPromotionData();
                clearForm();
                // Refresh promotion list in Form_Export if it exists
                com.Admin.export.GUI.Form_Export.refreshPromotionsIfExists();
            } else {
                showMessage(
                    isEditing ? "Update promotion failed!" : "Add promotion failed!",
                    "error"
                );
            }
            
        } catch (NumberFormatException e) {
            showMessage("Discount percent must be a number!", "error");
        } catch (Exception e) {
            showMessage("Error: " + e.getMessage(), "error");
        }
    }
    
    private void clearForm() {
        clearFormFields();
        formPanel.setVisible(false);
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
            JOptionPane.showMessageDialog(this, message, "Success", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
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
                    if (status.equals("Active")) {
                        c.setBackground(new Color(200, 255, 200));
                        c.setForeground(new Color(0, 100, 0));
                    } else if (status.equals("Expired")) {
                        c.setBackground(new Color(255, 200, 200));
                        c.setForeground(new Color(139, 0, 0));
                    } else if (status.equals("Upcoming")) {
                        c.setBackground(new Color(255, 255, 200));
                        c.setForeground(new Color(139, 69, 0));
                    }
                }
            }
            
            return c;
        }
    }
}

