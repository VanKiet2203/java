package com.User.home.GUI;

import com.ComponentandDatabase.Components.CustomDialog;
import com.ComponentandDatabase.Components.MyButton;
import com.ComponentandDatabase.Components.MyCombobox;
import com.ComponentandDatabase.Components.MyTextField;
import com.User.home.BUS.productBUS;
import com.User.home.DTO.productDTO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import javax.swing.SwingConstants;

public class Home_user extends JPanel {

    private JPanel panelShow;
    private JScrollPane scrollShow;
    private productBUS proBUS;
    private MyCombobox comboBoxSearch;
    private MyTextField inputText, inputMin, inputMax;
    private String selectedComboBoxItem;
    private JLabel minlbl, maxlbl;

    public Home_user() {
        proBUS = new productBUS();
        initComponents();
        updateProductList(); // Load products on initialization
    }

    private void initComponents() {
        setLayout(null);
        setBounds(0, 0, 1300, 860);
        setBackground(Color.WHITE);

        // Search components
        initSearchComponents();
        
        // Products display area
        initProductDisplayArea();
    }

    private void initSearchComponents() {
        // Tạo panel search với border đẹp
        JPanel searchPanel = new JPanel();
        searchPanel.setLayout(null);
        searchPanel.setBounds(20, 20, 1260, 100);
        searchPanel.setBackground(Color.WHITE);
        searchPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.decode("#E0E0E0"), 2),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        add(searchPanel);

        // Filter combo box với icon
        String[] items = {"All Products", "Product ID", "Product Name", "Price Range"};
        comboBoxSearch = new MyCombobox(items);
        comboBoxSearch.setBounds(20, 35, 200, 35);
        comboBoxSearch.setCustomFont(new Font("Arial", Font.PLAIN, 14));
        selectedComboBoxItem = (String) comboBoxSearch.getSelectedItem();
        searchPanel.add(comboBoxSearch);

        // Search input với border đẹp
        inputText = new MyTextField();
        inputText.setBounds(240, 35, 400, 35);
        inputText.setHint("Enter search keyword...");
        inputText.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.decode("#4CAF50"), 2),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        inputText.setTextFont(new Font("Arial", Font.PLAIN, 14));
        searchPanel.add(inputText);

        // Price range labels
        minlbl = new JLabel("Min Price:");
        minlbl.setBounds(240, 20, 80, 20);
        minlbl.setFont(new Font("Arial", Font.BOLD, 12));
        minlbl.setForeground(Color.decode("#666666"));
        minlbl.setVisible(false);
        searchPanel.add(minlbl);

        inputMin = new MyTextField();
        inputMin.setBounds(240, 35, 190, 35);
        inputMin.setHint("Min price...");
        inputMin.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.decode("#4CAF50"), 2),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        inputMin.setVisible(false);
        searchPanel.add(inputMin);

        maxlbl = new JLabel("Max Price:");
        maxlbl.setBounds(450, 20, 80, 20);
        maxlbl.setFont(new Font("Arial", Font.BOLD, 12));
        maxlbl.setForeground(Color.decode("#666666"));
        maxlbl.setVisible(false);
        searchPanel.add(maxlbl);

        inputMax = new MyTextField();
        inputMax.setBounds(450, 35, 190, 35);
        inputMax.setHint("Max price...");
        inputMax.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.decode("#4CAF50"), 2),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        inputMax.setVisible(false);
        searchPanel.add(inputMax);

        comboBoxSearch.addActionListener(e -> {
            selectedComboBoxItem = (String) comboBoxSearch.getSelectedItem();
            boolean isPriceSearch = selectedComboBoxItem.contains("Price");
            
            inputText.setVisible(!isPriceSearch);
            inputMin.setVisible(isPriceSearch);
            inputMax.setVisible(isPriceSearch);
            minlbl.setVisible(isPriceSearch);
            maxlbl.setVisible(isPriceSearch);
            
            revalidate();
            repaint();
        });

        // Search button với icon
        MyButton btnSearch = new MyButton("Search", 10);
        btnSearch.setBounds(660, 35, 120, 35);
        btnSearch.setBackgroundColor(Color.decode("#4CAF50"));
        btnSearch.setHoverColor(Color.decode("#45A049"));
        btnSearch.setForeground(Color.WHITE);
        btnSearch.setFont(new Font("Arial", Font.BOLD, 14));
        btnSearch.setButtonIcon("/Icons/Admin_icon/search.png", 20, 20, 5, SwingConstants.RIGHT, SwingConstants.CENTER);
        btnSearch.addActionListener(e -> searchProducts());
        searchPanel.add(btnSearch);

        // All Products button với icon
        MyButton reShowAllProducts = new MyButton("All Products", 10);
        reShowAllProducts.setBounds(800, 35, 160, 35);
        reShowAllProducts.setBackgroundColor(Color.decode("#2196F3"));
        reShowAllProducts.setHoverColor(Color.decode("#1976D2"));
        reShowAllProducts.setForeground(Color.WHITE);
        reShowAllProducts.setFont(new Font("Arial", Font.BOLD, 14));
        reShowAllProducts.setButtonIcon("/Icons/Admin_icon/refresh.png", 20, 20, 5, SwingConstants.RIGHT, SwingConstants.CENTER);
        reShowAllProducts.addActionListener(e -> updateProductList());
        searchPanel.add(reShowAllProducts);
    }

    private void initProductDisplayArea() {
        panelShow = new JPanel(new GridBagLayout());
        panelShow.setBackground(Color.WHITE);
        panelShow.setBorder(null); // Loại bỏ hoàn toàn border

        scrollShow = new JScrollPane(panelShow);
        scrollShow.setBounds(0, 130, 1250, 700);
        scrollShow.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollShow.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollShow.setBorder(null); // Loại bỏ border của scroll pane nếu cần
        add(scrollShow);
    }
    
    private void updateProductList() {
        panelShow.removeAll();
        panelShow.setLayout(new GridLayout(0, 4, 5, 10)); // Quan trọng: thiết lập lại layout
        ArrayList<productDTO> productList = proBUS.showProduct(null);
        displayProducts(productList);
    }

    private void searchProducts() {
        String condition = null;
        if ("Price".equals(selectedComboBoxItem)) {
            try {
                double min = Double.parseDouble(inputMin.getText());
                double max = Double.parseDouble(inputMax.getText());
                condition = "price BETWEEN " + min + " AND " + max;
            } catch (NumberFormatException e) {
                CustomDialog.showError("Please enter valid price values!");
                return;
            }
        } else {
            String searchText = inputText.getText().trim();
            if (searchText.isEmpty()) {
                CustomDialog.showError("Please enter a search term before searching!");
                return;
            }
            condition = selectedComboBoxItem.toLowerCase() + " LIKE '%" + searchText + "%'";
        }

        ArrayList<productDTO> filteredList = proBUS.showProduct(condition);
        displaySearchResults(filteredList);
    }
    private void displaySearchResults(ArrayList<productDTO> products) {
        panelShow.removeAll();

        // 1. Sử dụng FlowLayout với căn lề trái và trên cùng
        panelShow.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 5));
        panelShow.setBackground(Color.WHITE);
        
        // 2. Thêm một panel container để kiểm soát vị trí
        JPanel container = new JPanel(new GridBagLayout());
        container.setBackground(Color.WHITE);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.NORTHWEST; // Căn góc trên bên trái
        gbc.weightx = 1; // Chiếm toàn bộ không gian ngang
        gbc.weighty = 1; // Chiếm toàn bộ không gian dọc
        gbc.gridx = 0;
        gbc.gridy = 0;

        if (products.isEmpty()) {
            JLabel noProducts = new JLabel("No products found", SwingConstants.LEFT);
            noProducts.setFont(new Font("Arial", Font.BOLD, 16));
            container.add(noProducts, gbc);
        } else {
            // 3. Tạo panel chứa các sản phẩm với FlowLayout căn trái
            JPanel productsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
            productsPanel.setBackground(Color.WHITE);

            for (productDTO product : products) {
                productsPanel.add(createProductPanel(product));
            }

            container.add(productsPanel, gbc);
        }

        panelShow.add(container);
        panelShow.revalidate();
        panelShow.repaint();

        // 4. Cuộn lên đầu trang
        JScrollBar vertical = scrollShow.getVerticalScrollBar();
        vertical.setValue(vertical.getMinimum());
    }

    public void displayProducts(ArrayList<productDTO> products) {
        panelShow.removeAll();
        panelShow.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 5));
        panelShow.setBackground(Color.WHITE);

        // Tạo container chính để đẩy sản phẩm lên trên
        JPanel container = new JPanel(new GridBagLayout());
        container.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.weightx = 1;
        gbc.weighty = 1;
        gbc.gridx = 0;
        gbc.gridy = 0;

        if (products.isEmpty()) {
            JLabel noProducts = new JLabel("Your cart is empty", SwingConstants.LEFT);
            noProducts.setFont(new Font("Arial", Font.BOLD, 16));
            container.add(noProducts, gbc);
        } else {
            // Dùng GridBagLayout để chia hàng cột như trước
            JPanel productsPanel = new JPanel(new GridBagLayout());
            productsPanel.setBackground(Color.WHITE);

            GridBagConstraints prodGbc = new GridBagConstraints();
            prodGbc.insets = new Insets(10, 10, 10, 10);
            prodGbc.anchor = GridBagConstraints.NORTHWEST;

            int col = 0, row = 0;
            final int maxCol = 4;

            for (productDTO product : products) {
                JPanel productPanel = createProductPanel(product);
                prodGbc.gridx = col;
                prodGbc.gridy = row;
                productsPanel.add(productPanel, prodGbc);

                col++;
                if (col == maxCol) {
                    col = 0;
                    row++;
                }
            }

            container.add(productsPanel, gbc);
        }

        panelShow.add(container);
        panelShow.revalidate();
        panelShow.repaint();
    }


    
    private JPanel createProductPanel(productDTO product) {
        // Main product panel với shadow effect
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setPreferredSize(new Dimension(320, 450)); // Tăng kích thước để có không gian rộng hơn
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.decode("#E0E0E0"), 1),
            BorderFactory.createEmptyBorder(15, 15, 15, 15) // Tăng padding
        ));

        // Product Image với border đẹp
        ImageIcon icon = new ImageIcon(product.getImage());
        Image img = icon.getImage().getScaledInstance(200, 150, Image.SCALE_SMOOTH);
        JLabel imageLabel = new JLabel(new ImageIcon(img), SwingConstants.CENTER);
        imageLabel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.decode("#4CAF50"), 2),
            BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
        imageLabel.setBackground(Color.decode("#F8F9FA"));
        imageLabel.setOpaque(true);
        panel.add(imageLabel, BorderLayout.NORTH);

        // Product Details với layout đẹp
        JPanel detailsPanel = new JPanel(new GridLayout(0, 1, 8, 8)); // Tăng khoảng cách giữa các dòng
        detailsPanel.setBackground(Color.WHITE);
        detailsPanel.setBorder(BorderFactory.createEmptyBorder(15, 10, 15, 10)); // Tăng padding

        // Product Name (highlighted)
        JLabel nameLabel = new JLabel(product.getProductName());
        nameLabel.setFont(new Font("Arial", Font.BOLD, 14));
        nameLabel.setForeground(Color.decode("#2E7D32"));
        nameLabel.setHorizontalAlignment(SwingConstants.CENTER);
        detailsPanel.add(nameLabel);

        // Product ID
        JLabel idLabel = new JLabel("ID: " + product.getProductID());
        idLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        idLabel.setForeground(Color.decode("#666666"));
        detailsPanel.add(idLabel);

        // Color
        JLabel colorLabel = new JLabel("Color: " + (product.getColor() != null ? product.getColor() : "N/A"));
        colorLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        colorLabel.setForeground(Color.decode("#666666"));
        detailsPanel.add(colorLabel);

        // Speed
        JLabel speedLabel = new JLabel("Speed: " + (product.getSpeed() != null ? product.getSpeed() : "N/A"));
        speedLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        speedLabel.setForeground(Color.decode("#666666"));
        detailsPanel.add(speedLabel);

        // Battery Capacity
        JLabel batteryLabel = new JLabel("Battery: " + (product.getBatteryCapacity() != null ? product.getBatteryCapacity() : "N/A"));
        batteryLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        batteryLabel.setForeground(Color.decode("#666666"));
        detailsPanel.add(batteryLabel);

        // Price (highlighted)
        JLabel priceLabel = new JLabel("Price: " + product.getPrice() + " VNĐ");
        priceLabel.setFont(new Font("Arial", Font.BOLD, 13));
        priceLabel.setForeground(Color.decode("#D32F2F"));
        detailsPanel.add(priceLabel);

        // Quantity
        JLabel qtyLabel = new JLabel("Stock: " + product.getQuantity());
        qtyLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        qtyLabel.setForeground(Color.decode("#666666"));
        detailsPanel.add(qtyLabel);

        // Warranty
        JLabel warrantyLabel = new JLabel("Warranty: " + product.getWarrantyMonths() + " months");
        warrantyLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        warrantyLabel.setForeground(Color.decode("#666666"));
        detailsPanel.add(warrantyLabel);

        // Status với màu sắc
        JLabel statusLabel = new JLabel(getStatusText(product));
        statusLabel.setFont(new Font("Arial", Font.BOLD, 12));
        if (product.getQuantity() == 0) {
            statusLabel.setForeground(Color.decode("#D32F2F"));
        } else {
            statusLabel.setForeground(Color.decode("#388E3C"));
        }
        detailsPanel.add(statusLabel);

        panel.add(detailsPanel, BorderLayout.CENTER);

        // Action Buttons với style đẹp
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        buttonPanel.setBackground(Color.WHITE);

        MyButton detailBtn = new MyButton("Details", 8);
        detailBtn.setPreferredSize(new Dimension(120, 35));
        detailBtn.setBackgroundColor(Color.decode("#2196F3"));
        detailBtn.setHoverColor(Color.decode("#1976D2"));
        detailBtn.setForeground(Color.WHITE);
        detailBtn.setFont(new Font("Arial", Font.BOLD, 12));
        detailBtn.setButtonIcon("/Icons/Admin_icon/details.png", 20, 20, 5, SwingConstants.RIGHT, SwingConstants.CENTER);
        detailBtn.addActionListener((e) -> {
            productDeteails details = new productDeteails();
            details.setVisible(true);
            details.displayProductDetails(product);
        });
        buttonPanel.add(detailBtn);

        // Add to Cart button
        MyButton addToCartBtn = new MyButton("Add to Cart", 8);
        addToCartBtn.setPreferredSize(new Dimension(120, 35));
        addToCartBtn.setBackgroundColor(Color.decode("#4CAF50"));
        addToCartBtn.setHoverColor(Color.decode("#45A049"));
        addToCartBtn.setForeground(Color.WHITE);
        addToCartBtn.setFont(new Font("Arial", Font.BOLD, 12));
        addToCartBtn.setButtonIcon("/Icons/User_icon/cart.png", 20, 20, 5, SwingConstants.RIGHT, SwingConstants.CENTER);
        addToCartBtn.addActionListener((e) -> {
            // TODO: Implement add to cart functionality
            CustomDialog.showSuccess("Product added to cart!");
        });
        buttonPanel.add(addToCartBtn);

        panel.add(buttonPanel, BorderLayout.SOUTH);

        return panel;
    }

   private void addDetailLabel(JPanel panel, String text, int fontSize) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Arial", Font.PLAIN, fontSize));
        label.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(label);
    }

    private String getStatusText(productDTO product) {
        return product.getQuantity() == 0 ? "Out of Stock" : product.getStatus();
    }

}