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
        searchPanel.setBounds(20, 60, 1490, 100);
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
        txtSearch.setBounds(180, 30, 300, 35);
        txtSearch.setTextFont(new Font("Arial", Font.PLAIN, 13));
        txtSearch.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(PRIMARY_COLOR, 1),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        searchPanel.add(txtSearch);
        
        // Search button
        btnSearch = createButton("Search", 490, 30, 120, 35, PRIMARY_COLOR, PRIMARY_HOVER);
        btnSearch.setButtonIcon("src\\main\\resources\\Icons\\Admin_icon\\search.png", 25, 25, 5, SwingConstants.RIGHT, SwingConstants.CENTER);
        btnSearch.addActionListener(e -> searchPromotion());
        searchPanel.add(btnSearch);
        
        // Refresh button
        btnRefresh = createButton("Refresh", 620, 30, 120, 35, INFO_COLOR, INFO_HOVER);
        btnRefresh.setButtonIcon("src\\main\\resources\\Icons\\Admin_icon\\refresh.png", 25, 25, 5, SwingConstants.RIGHT, SwingConstants.CENTER);
        btnRefresh.addActionListener(e -> {
            txtSearch.setText("");
            cmbSearchType.setSelectedIndex(0);
            loadPromotionData();
        });
        searchPanel.add(btnRefresh);
        
        // Add button
        btnAdd = createButton("Add new", 750, 30, 130, 35, PRIMARY_COLOR, PRIMARY_HOVER);
        btnAdd.setButtonIcon("src\\main\\resources\\Icons\\Admin_icon\\new.png", 25, 25, 5, SwingConstants.RIGHT, SwingConstants.CENTER);
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
        formPanel.setBounds(20, 180, 1490, 200);
        formPanel.setVisible(false); // Hidden by default
        mainPanel.add(formPanel);
        
        int labelX = 30;
        int fieldX = 200;
        int labelWidth = 150;
        int fieldWidth = 250;
        
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
        addLabel("Promotion Name:", labelX + 550, 30, labelWidth, 30);
        txtPromotionName = new MyTextField();
        txtPromotionName.setBounds(fieldX + 550, 30, fieldWidth + 100, 35);
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
        addLabel("End Date:", labelX + 550, 80, labelWidth, 30);
        dateEnd = new JDateChooser();
        dateEnd.setBounds(fieldX + 550, 80, fieldWidth, 35);
        dateEnd.setDateFormatString("dd/MM/yyyy");
        dateEnd.setFont(new Font("Arial", Font.PLAIN, 13));
        dateEnd.setBorder(BorderFactory.createLineBorder(PRIMARY_COLOR, 1));
        formPanel.add(dateEnd);
        
        // Row 3: Discount Percent
        addLabel("Discount Percent:", labelX, 130, labelWidth, 30);
        txtDiscountPercent = new MyTextField();
        txtDiscountPercent.setBounds(fieldX, 130, 150, 35);
        txtDiscountPercent.setTextFont(new Font("Arial", Font.PLAIN, 13));
        txtDiscountPercent.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(PRIMARY_COLOR, 1),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        formPanel.add(txtDiscountPercent);
        
        // Buttons
        btnSave = createButton("Save", 1100, 30, 120, 35, PRIMARY_COLOR, PRIMARY_HOVER);
        btnSave.setButtonIcon("src\\main\\resources\\Icons\\Admin_icon\\update.png", 25, 25, 5, SwingConstants.RIGHT, SwingConstants.CENTER);
        btnSave.addActionListener(e -> savePromotion());
        formPanel.add(btnSave);
        
        btnClear = createButton("Cancel", 1100, 80, 120, 35, DANGER_COLOR, DANGER_HOVER);
        btnClear.setButtonIcon("src\\main\\resources\\Icons\\Admin_icon\\delete.png", 25, 25, 5, SwingConstants.RIGHT, SwingConstants.CENTER);
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
        tablePanel.setBounds(20, 400, 1490, 460);
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
        btnEdit = createButton("Edit", 20, 410, 120, 35, WARNING_COLOR, WARNING_HOVER);
        btnEdit.setButtonIcon("src\\main\\resources\\Icons\\Admin_icon\\edit.png", 25, 25, 5, SwingConstants.RIGHT, SwingConstants.CENTER);
        btnEdit.addActionListener(e -> editSelectedPromotion());
        btnEdit.setEnabled(false);
        tablePanel.add(btnEdit);
        
        btnDelete = createButton("Delete", 150, 410, 120, 35, DANGER_COLOR, DANGER_HOVER);
        btnDelete.setButtonIcon("src\\main\\resources\\Icons\\Admin_icon\\delete.png", 25, 25, 5, SwingConstants.RIGHT, SwingConstants.CENTER);
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

