package com.User.home.GUI;

import com.ComponentandDatabase.Components.CustomDialog;
import com.ComponentandDatabase.Components.MyButton;
import com.ComponentandDatabase.Components.MyCombobox;
import com.ComponentandDatabase.Components.MyTextField;
import com.User.home.BUS.productBUS;
import com.User.home.DTO.productDTO;
import com.User.Cart.BUS.BUSCart;
import com.User.Cart.DTO.DTOCart;
import com.User.dashboard_user.GUI.Dashboard_user;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import javax.swing.BoxLayout;
import javax.swing.SwingConstants;

public class Home_user extends JPanel {

    private JPanel panelShow;
    private JScrollPane scrollShow;
    private productBUS proBUS;
    private MyCombobox comboBoxSearch, comboBoxSort;
    private MyTextField inputText, inputMin, inputMax;
    private String selectedComboBoxItem;
    private JLabel minlbl, maxlbl;
    private ArrayList<productDTO> currentProductList; // Lưu danh sách sản phẩm hiện tại để sort

    public Home_user() {
        proBUS = new productBUS();
        initComponents();
        
        // Add component listener to recalculate layout on resize
        addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent evt) {
                recalculateLayout();
            }
        });
        
        updateProductList(); // Load products on initialization
    }

    private void initComponents() {
        setLayout(new BorderLayout()); // Use BorderLayout for responsiveness
        setPreferredSize(new Dimension(1300, 860));
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
        searchPanel.setPreferredSize(new Dimension(1260, 100));
        searchPanel.setBackground(Color.WHITE);
        searchPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.decode("#E0E0E0"), 2),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        add(searchPanel, BorderLayout.NORTH);

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
        
        // Sort combobox
        JLabel sortLabel = new JLabel("Sort by:");
        sortLabel.setBounds(980, 20, 80, 20);
        sortLabel.setFont(new Font("Arial", Font.BOLD, 12));
        sortLabel.setForeground(Color.decode("#666666"));
        searchPanel.add(sortLabel);
        
        String[] sortOptions = {"None", "Price: Low to High", "Price: High to Low", 
                                "Name: A to Z", "Name: Z to A", "Color: A to Z"};
        comboBoxSort = new MyCombobox(sortOptions);
        comboBoxSort.setBounds(980, 35, 200, 35);
        comboBoxSort.setCustomFont(new Font("Arial", Font.PLAIN, 14));
        // Tự động sắp xếp khi chọn option
        // Sử dụng ItemListener để đảm bảo được trigger khi item thay đổi
        comboBoxSort.addItemListener(e -> {
            if (e.getStateChange() == java.awt.event.ItemEvent.SELECTED) {
                String selected = (String) comboBoxSort.getSelectedItem();
                if (selected != null && !selected.isEmpty()) {
                    // Tự động apply sort ngay khi chọn
                    SwingUtilities.invokeLater(() -> {
                        applySort();
                    });
                }
            }
        });
        searchPanel.add(comboBoxSort);
    }

    private void initProductDisplayArea() {
        panelShow = new JPanel();
        panelShow.setBackground(Color.WHITE);
        panelShow.setBorder(null);

        scrollShow = new JScrollPane(panelShow);
        scrollShow.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollShow.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollShow.setBorder(null);
        add(scrollShow, BorderLayout.CENTER); // Use BorderLayout.CENTER for responsiveness
    }
    
    /**
     * Tính toán số cột dựa trên chiều rộng panel hiện tại
     */
    private int calculateColumnCount() {
        int panelWidth = panelShow.getWidth();
        if (panelWidth <= 0) {
            // Try to get width from scroll pane viewport
            if (scrollShow != null && scrollShow.getViewport() != null) {
                panelWidth = scrollShow.getViewport().getWidth();
            }
            if (panelWidth <= 0) {
                panelWidth = 1250; // Default width if not yet initialized
            }
        }
        // Mỗi product card có width ~320px + gap 15px = ~335px
        // Tính số cột có thể chứa, trừ đi margin
        int cardWidth = 335;
        int columns = Math.max(1, (panelWidth - 40) / cardWidth); // 40px for margins/padding
        return columns;
    }
    
    private void updateProductList() {
        panelShow.removeAll();
        // Calculate dynamic column count based on available width
        int columns = calculateColumnCount();
        panelShow.setLayout(new GridLayout(0, columns, 15, 15)); // Increased gaps for better spacing
        ArrayList<productDTO> productList = proBUS.showProduct(null);
        currentProductList = new ArrayList<>(productList); // Lưu danh sách để sort
        // Reset sort combobox về "None" - không cần lo trigger vì "None" chỉ hiển thị lại danh sách gốc
        if (comboBoxSort != null) {
            comboBoxSort.setSelectedIndex(0);
        }
        displayProducts(productList);
    }
    
    /**
     * Recalculate layout when component is resized
     */
    public void recalculateLayout() {
        if (panelShow != null && panelShow.getComponentCount() > 0) {
            // Only recalculate if there are products displayed
            int newColumns = calculateColumnCount();
            int currentColumns = 4; // Default
            
            // Try to determine current column count from layout
            if (panelShow.getLayout() instanceof GridLayout) {
                GridLayout gl = (GridLayout) panelShow.getLayout();
                currentColumns = gl.getColumns();
            }
            
            // Only recalculate if column count changed
            if (newColumns != currentColumns) {
                panelShow.removeAll();
                panelShow.setLayout(new GridLayout(0, newColumns, 15, 15));
                // Sử dụng currentProductList nếu có, nếu không thì load lại
                if (currentProductList != null && !currentProductList.isEmpty()) {
                    displayProducts(currentProductList);
                } else {
                    ArrayList<productDTO> productList = proBUS.showProduct(null);
                    currentProductList = new ArrayList<>(productList);
                    displayProducts(productList);
                }
            }
        }
    }

    private void searchProducts() {
        String condition = null;
        
        // Xử lý "All Products" - không có điều kiện
        if ("All Products".equals(selectedComboBoxItem)) {
            condition = null;
        } 
        // Xử lý "Price Range"
        else if ("Price Range".equals(selectedComboBoxItem)) {
            try {
                String minText = inputMin.getText().trim();
                String maxText = inputMax.getText().trim();
                
                if (minText.isEmpty() || maxText.isEmpty()) {
                    CustomDialog.showError("Please enter both min and max price values!");
                    return;
                }
                
                double min = Double.parseDouble(minText);
                double max = Double.parseDouble(maxText);
                
                if (min > max) {
                    CustomDialog.showError("Min price cannot be greater than max price!");
                    return;
                }
                
                condition = "p.Price BETWEEN " + min + " AND " + max;
            } catch (NumberFormatException e) {
                CustomDialog.showError("Please enter valid price values!");
                return;
            }
        } 
        // Xử lý các tùy chọn tìm kiếm khác
        else {
            String searchText = inputText.getText().trim();
            if (searchText.isEmpty()) {
                CustomDialog.showError("Please enter a search term before searching!");
                return;
            }
            
            // Map đúng tên cột SQL
            String columnName;
            if ("Product ID".equals(selectedComboBoxItem)) {
                columnName = "p.Product_ID";
            } else if ("Product Name".equals(selectedComboBoxItem)) {
                columnName = "p.Product_Name";
            } else {
                columnName = "p.Product_Name"; // Default
            }
            
            condition = columnName + " LIKE '%" + searchText + "%'";
        }

        ArrayList<productDTO> filteredList = proBUS.showProduct(condition);
        currentProductList = new ArrayList<>(filteredList); // Lưu danh sách để sort
        displaySearchResults(filteredList);
    }
    private void displaySearchResults(ArrayList<productDTO> products) {
        panelShow.removeAll();
        // Calculate dynamic column count for search results
        int columns = calculateColumnCount();
        panelShow.setLayout(new GridLayout(0, columns, 15, 15));
        panelShow.setBackground(Color.WHITE);
        
        // Display products directly
        if (products.isEmpty()) {
            JLabel noProducts = new JLabel("No products found", SwingConstants.CENTER);
            noProducts.setFont(new Font("Arial", Font.BOLD, 16));
            noProducts.setForeground(Color.decode("#666666"));
            panelShow.add(noProducts);
        } else {
            for (productDTO product : products) {
                panelShow.add(createProductPanel(product));
            }
        }

        panelShow.revalidate();
        panelShow.repaint();

        // Cuộn lên đầu trang
        SwingUtilities.invokeLater(() -> {
            JScrollBar vertical = scrollShow.getVerticalScrollBar();
            vertical.setValue(vertical.getMinimum());
        });
    }

    public void displayProducts(ArrayList<productDTO> products) {
        // Layout is already set in updateProductList/displaySearchResults
        // Just add products directly to panelShow
        if (products.isEmpty()) {
            JLabel noProducts = new JLabel("No products available", SwingConstants.CENTER);
            noProducts.setFont(new Font("Arial", Font.BOLD, 16));
            noProducts.setForeground(Color.decode("#666666"));
            panelShow.add(noProducts);
        } else {
            for (productDTO product : products) {
                JPanel productPanel = createProductPanel(product);
                panelShow.add(productPanel);
            }
        }

        panelShow.revalidate();
        panelShow.repaint();
    }
    
    /**
     * Áp dụng sắp xếp cho danh sách sản phẩm hiện tại
     * Tự động thực hiện khi user chọn option từ combobox
     */
    private void applySort() {
        // Nếu chưa có danh sách sản phẩm, load lại
        if (currentProductList == null || currentProductList.isEmpty()) {
            currentProductList = new ArrayList<>(proBUS.showProduct(null));
        }
        
        if (currentProductList == null || currentProductList.isEmpty()) {
            return;
        }
        
        String selectedSort = (String) comboBoxSort.getSelectedItem();
        if (selectedSort == null || "None".equals(selectedSort)) {
            // Hiển thị lại danh sách gốc (không sort)
            refreshProductDisplay(currentProductList);
            return;
        }
        
        // Tạo bản copy để sort (không thay đổi danh sách gốc)
        ArrayList<productDTO> sortedList = new ArrayList<>(currentProductList);
        
        // Áp dụng sort theo lựa chọn
        switch (selectedSort) {
            case "Price: Low to High":
                sortedList.sort((p1, p2) -> {
                    Double price1 = p1.getPrice() != null ? p1.getPrice().doubleValue() : 0.0;
                    Double price2 = p2.getPrice() != null ? p2.getPrice().doubleValue() : 0.0;
                    return price1.compareTo(price2);
                });
                break;
                
            case "Price: High to Low":
                sortedList.sort((p1, p2) -> {
                    Double price1 = p1.getPrice() != null ? p1.getPrice().doubleValue() : 0.0;
                    Double price2 = p2.getPrice() != null ? p2.getPrice().doubleValue() : 0.0;
                    return price2.compareTo(price1);
                });
                break;
                
            case "Name: A to Z":
                sortedList.sort((p1, p2) -> {
                    String name1 = p1.getProductName() != null ? p1.getProductName() : "";
                    String name2 = p2.getProductName() != null ? p2.getProductName() : "";
                    return name1.compareToIgnoreCase(name2);
                });
                break;
                
            case "Name: Z to A":
                sortedList.sort((p1, p2) -> {
                    String name1 = p1.getProductName() != null ? p1.getProductName() : "";
                    String name2 = p2.getProductName() != null ? p2.getProductName() : "";
                    return name2.compareToIgnoreCase(name1);
                });
                break;
                
            case "Color: A to Z":
                sortedList.sort((p1, p2) -> {
                    String color1 = p1.getColor() != null ? p1.getColor() : "";
                    String color2 = p2.getColor() != null ? p2.getColor() : "";
                    return color1.compareToIgnoreCase(color2);
                });
                break;
        }
        
        // Hiển thị danh sách đã sort
        refreshProductDisplay(sortedList);
    }
    
    /**
     * Refresh hiển thị sản phẩm với layout đúng
     */
    private void refreshProductDisplay(ArrayList<productDTO> products) {
        panelShow.removeAll();
        int columns = calculateColumnCount();
        panelShow.setLayout(new GridLayout(0, columns, 15, 15));
        panelShow.setBackground(Color.WHITE);
        
        if (products.isEmpty()) {
            JLabel noProducts = new JLabel("No products available", SwingConstants.CENTER);
            noProducts.setFont(new Font("Arial", Font.BOLD, 16));
            noProducts.setForeground(Color.decode("#666666"));
            panelShow.add(noProducts);
        } else {
            for (productDTO product : products) {
                JPanel productPanel = createProductPanel(product);
                panelShow.add(productPanel);
            }
        }
        
        panelShow.revalidate();
        panelShow.repaint();
        
        // Cuộn lên đầu trang
        SwingUtilities.invokeLater(() -> {
            JScrollBar vertical = scrollShow.getVerticalScrollBar();
            vertical.setValue(vertical.getMinimum());
        });
    }


    
    private JPanel createProductPanel(productDTO product) {
        // Main product panel với shadow effect
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setPreferredSize(new Dimension(320, 520)); // Tăng chiều cao từ 450 lên 520 để chứa đủ với spacing mới
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

        // Product Details với layout đẹp - sử dụng BoxLayout để có control tốt hơn về spacing
        JPanel detailsPanel = new JPanel();
        detailsPanel.setLayout(new BoxLayout(detailsPanel, BoxLayout.Y_AXIS));
        detailsPanel.setBackground(Color.WHITE);
        detailsPanel.setBorder(BorderFactory.createEmptyBorder(12, 10, 12, 10));

        // ========== NHÓM 1: THÔNG TIN CHÍNH ==========
        // 1. Product Name (nổi bật, căn giữa)
        JLabel nameLabel = new JLabel(product.getProductName());
        nameLabel.setFont(new Font("Arial", Font.BOLD, 15));
        nameLabel.setForeground(Color.decode("#2E7D32"));
        nameLabel.setHorizontalAlignment(SwingConstants.CENTER);
        nameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        nameLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0)); // Tăng padding dưới tên
        detailsPanel.add(nameLabel);

        // 2. Price (quan trọng, đưa lên gần đầu, nổi bật)
        JPanel pricePanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 2));
        pricePanel.setBackground(Color.WHITE);
        pricePanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        pricePanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 12, 0)); // Tăng padding dưới giá
        
        // Current Price (Price)
        JLabel priceLabel = new JLabel(product.getPrice() != null ? String.format("%,.0f VNĐ", product.getPrice().doubleValue()) : "N/A");
        priceLabel.setFont(new Font("Arial", Font.BOLD, 16));
        priceLabel.setForeground(Color.decode("#D32F2F"));
        pricePanel.add(priceLabel);
        
        // Show original and promotional prices if available
        if (product.getListPriceBefore() != null && product.getListPriceAfter() != null) {
            if (product.getListPriceAfter().compareTo(product.getListPriceBefore()) < 0) {
                // Có giảm giá
                JLabel originalPriceLabel = new JLabel("<html><strike>" + String.format("%,.0f", product.getListPriceBefore().doubleValue()) + "</strike></html>");
                originalPriceLabel.setFont(new Font("Arial", Font.PLAIN, 11));
                originalPriceLabel.setForeground(Color.decode("#999999"));
                pricePanel.add(originalPriceLabel);
                
                // Tính phần trăm giảm
                double discountPercent = ((product.getListPriceBefore().doubleValue() - product.getListPriceAfter().doubleValue()) / product.getListPriceBefore().doubleValue()) * 100;
                JLabel discountLabel = new JLabel(String.format("(-%.0f%%)", discountPercent));
                discountLabel.setFont(new Font("Arial", Font.BOLD, 11));
                discountLabel.setForeground(Color.decode("#FF9800"));
                pricePanel.add(discountLabel);
            }
        }
        detailsPanel.add(pricePanel);

        // Separator để tách nhóm thông tin
        JSeparator separator1 = new JSeparator();
        separator1.setAlignmentX(Component.CENTER_ALIGNMENT);
        separator1.setForeground(Color.decode("#E0E0E0"));
        separator1.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
        detailsPanel.add(separator1);

        // ========== NHÓM 2: THÔNG SỐ KỸ THUẬT ==========
        // 3. Color (màu sắc)
        JLabel colorLabel = new JLabel("Color: " + (product.getColor() != null ? product.getColor() : "N/A"));
        colorLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        colorLabel.setForeground(Color.decode("#666666"));
        colorLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        colorLabel.setBorder(BorderFactory.createEmptyBorder(0, 5, 5, 5)); // Giảm padding giữa các dòng trong nhóm
        detailsPanel.add(colorLabel);

        // 4. Speed (tốc độ)
        JLabel speedLabel = new JLabel("Speed: " + (product.getSpeed() != null ? product.getSpeed() : "N/A"));
        speedLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        speedLabel.setForeground(Color.decode("#666666"));
        speedLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        speedLabel.setBorder(BorderFactory.createEmptyBorder(0, 5, 5, 5));
        detailsPanel.add(speedLabel);

        // 5. Battery Capacity (dung lượng pin)
        JLabel batteryLabel = new JLabel("Battery Capacity: " + (product.getBatteryCapacity() != null ? product.getBatteryCapacity() : "N/A"));
        batteryLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        batteryLabel.setForeground(Color.decode("#666666"));
        batteryLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        batteryLabel.setBorder(BorderFactory.createEmptyBorder(0, 5, 10, 5)); // Tăng padding dưới để tách nhóm
        detailsPanel.add(batteryLabel);

        // Separator để tách nhóm thông tin
        JSeparator separator2 = new JSeparator();
        separator2.setAlignmentX(Component.CENTER_ALIGNMENT);
        separator2.setForeground(Color.decode("#E0E0E0"));
        separator2.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
        detailsPanel.add(separator2);

        // ========== NHÓM 3: THÔNG TIN BỔ SUNG ==========
        // 6. Stock (số lượng tồn kho)
        JLabel qtyLabel = new JLabel("Stock: " + product.getQuantity() + " products");
        qtyLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        qtyLabel.setForeground(product.getQuantity() > 0 ? Color.decode("#27AE60") : Color.decode("#E74C3C")); // Xanh nếu còn, đỏ nếu hết
        qtyLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        qtyLabel.setBorder(BorderFactory.createEmptyBorder(0, 5, 5, 5));
        detailsPanel.add(qtyLabel);

        // 7. Warranty (bảo hành)
        JLabel warrantyLabel = new JLabel("Warranty: " + product.getWarrantyMonths() + " months");
        warrantyLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        warrantyLabel.setForeground(Color.decode("#666666"));
        warrantyLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        warrantyLabel.setBorder(BorderFactory.createEmptyBorder(0, 5, 5, 5));
        detailsPanel.add(warrantyLabel);

        // 8. Origin (xuất xứ)
        JLabel countryLabel = new JLabel("Country: " + (product.getCountry() != null ? product.getCountry() : "N/A"));
        countryLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        countryLabel.setForeground(Color.decode("#666666"));
        countryLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        countryLabel.setBorder(BorderFactory.createEmptyBorder(0, 5, 5, 5));
        detailsPanel.add(countryLabel);

        // 9. Product ID (mã sản phẩm - đặt cuối)
        JLabel idLabel = new JLabel("Product ID: " + product.getProductID());
        idLabel.setFont(new Font("Arial", Font.PLAIN, 11));
        idLabel.setForeground(Color.decode("#999999"));
        idLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        idLabel.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5)); // Không có padding dưới vì là dòng cuối
        detailsPanel.add(idLabel);

        // Status với màu sắc
        // JLabel statusLabel = new JLabel(getStatusText(product));
        // statusLabel.setFont(new Font("Arial", Font.BOLD, 12));
        // statusLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        // statusLabel.setBorder(BorderFactory.createEmptyBorder(4, 0, 0, 0)); // Top padding only
        // if (product.getQuantity() == 0) {
        //     statusLabel.setForeground(Color.decode("#D32F2F"));
        // } else {
        //     statusLabel.setForeground(Color.decode("#388E3C"));
        // }
        // detailsPanel.add(statusLabel);

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
        
        // Disable button if product is out of stock
        if (product.getQuantity() == 0) {
            addToCartBtn.setEnabled(false);
            addToCartBtn.setText("Out of Stock");
            addToCartBtn.setBackgroundColor(Color.LIGHT_GRAY);
            addToCartBtn.setHoverColor(Color.LIGHT_GRAY);
        } else {
            addToCartBtn.addActionListener((e) -> {
                // Lấy Customer ID từ Dashboard
                String customerID = Dashboard_user.customerID;
                
                if (customerID == null || customerID.isEmpty()) {
                    CustomDialog.showError("Please login to add products to cart!");
                    return;
                }
                
                // Kiểm tra tồn kho
                if (product.getQuantity() == 0) {
                    CustomDialog.showError("This product is out of stock and cannot be added to cart!");
                    return;
                }
                
                // Tạo DTO với số lượng mặc định là 1
                DTOCart cartItem = new DTOCart(customerID, product.getProductID(), 1);
                BUSCart busCart = new BUSCart();
                
                // Thêm vào cart
                boolean result = busCart.addToCart(cartItem);
                
                if (result) {
                    CustomDialog.showSuccess("Product added to cart successfully!");
                    
                    // Fire cart update event để cập nhật cart form
                    fireCartUpdatedEvent(customerID);
                } else {
                    // Error message đã được hiển thị trong BUSCart
                    // Không cần hiển thị lại ở đây
                }
            });
        }
        
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
    
    /**
     * Fire cart update event để cập nhật cart form
     * Sử dụng static listeners từ productDeteails để đảm bảo consistency
     */
    private void fireCartUpdatedEvent(String customerID) {
        // Sử dụng lại static listeners từ productDeteails
        productDeteails.fireCartUpdatedEventStatic(customerID);
    }

}