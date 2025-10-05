package com.Admin.product.GUI;

import com.ComponentandDatabase.Components.MyCombobox;
import com.ComponentandDatabase.Components.MyPanel;
import com.ComponentandDatabase.Components.MyTextField;
import com.ComponentandDatabase.Components.MyButton;
import com.ComponentandDatabase.Components.CustomDialog;
import com.Admin.product.BUS.BusProduct;
import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;
import com.Admin.category.DTO.DTOCategory;
import com.Admin.product.DTO.DTOProduct;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import javax.swing.*;
import net.miginfocom.swing.MigLayout;
import java.awt.Dimension;

public class NewProduct extends javax.swing.JFrame {
    private int mouseX, mouseY;
    private JLabel lblTitle, lblProductID, lblProductName, lblCPU, lblRam,
            lblCard, lblOprerate, lblPrice, lblQuantity, lblwaranty, lblSpoiled, lblCate;
    private MyPanel panelTitle;
    private MyTextField txtProductID, txtProductName, txtCPU, txtRam, txtCard, txtPrice, txtwaranty;
    private MyCombobox<String> cmbOperate;
    private MyButton bntupload, bntSave, bntReset;
    private JPanel panelUpload;
    private JSpinner spinnerQuantity, spinderBrokenQuantity;
    private JMenu menu;
    private String image;
    private BusProduct busProduct;

    public NewProduct() {
        initComponents();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        // cho phép kéo di chuyển cửa sổ
        bg.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                mouseX = evt.getX();
                mouseY = evt.getY();
            }
        });
        bg.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            public void mouseDragged(java.awt.event.MouseEvent evt) {
                int x = evt.getXOnScreen();
                int y = evt.getYOnScreen();
                setLocation(x - mouseX, y - mouseY);
            }
        });

        init();
    }

    public void init() {
        // Layout chính gồm 2 phần: tiêu đề và nội dung
        bg.setLayout(new MigLayout("fill, insets 10, wrap", "[grow]", "[][grow]"));

        // Panel tiêu đề
        panelTitle = new MyPanel(new MigLayout("fill, insets 0"));
        panelTitle.setGradientColors(Color.decode("#1CB5E0"), Color.decode("#4682B4"), MyPanel.VERTICAL_GRADIENT);

        lblTitle = new JLabel("New Product", JLabel.CENTER);
        lblTitle.setFont(new Font("Arial", Font.BOLD, 22));
        lblTitle.setForeground(Color.WHITE);
        panelTitle.add(lblTitle, "growx, align center");

        bg.add(panelTitle, "growx, h 45!, wrap");

        // Panel nội dung chính
        JPanel contentPanel = new JPanel(new MigLayout(
                "insets 15, wrap 4, gapx 20, gapy 15",
                "[right][150!][right][200!]"
        ));
        contentPanel.setBackground(Color.WHITE);

        // Nút Reset và Upload
        bntReset = new MyButton("Reset", 0);
        bntReset.setBackgroundColor(Color.WHITE);
        bntReset.setHoverColor(Color.decode("#EEEEEE"));
        bntReset.setPressedColor(Color.decode("#D3D3D3"));
        bntReset.setFont(new Font("sansserif", Font.PLAIN, 16));
        bntReset.setButtonIcon("src\\main\\resources\\Icons\\Admin_icon\\reset.png",
                25, 25, 10, SwingConstants.RIGHT, SwingConstants.CENTER);
        bntReset.addActionListener(e -> resetForm());
        contentPanel.add(bntReset, "span, align right");

        // Product ID
        lblProductID = new JLabel("Product ID:");
        lblProductID.setFont(new Font("sansserif", Font.PLAIN, 16));
        txtProductID = makeTextField();
        contentPanel.add(lblProductID);
        contentPanel.add(txtProductID, "growx");

        // Product Name
        lblProductName = new JLabel("Product Name:");
        lblProductName.setFont(new Font("sansserif", Font.PLAIN, 16));
        txtProductName = makeTextField();
        contentPanel.add(lblProductName);
        contentPanel.add(txtProductName, "growx, wrap");

        // Color (CPU)
        lblCPU = new JLabel("Động cơ:");
        lblCPU.setFont(new Font("sansserif", Font.PLAIN, 16));
        txtCPU = makeTextField();
        contentPanel.add(lblCPU);
        contentPanel.add(txtCPU, "growx");

        // Battery (RAM)
        lblRam = new JLabel("Dung lượng pin:");
        lblRam.setFont(new Font("sansserif", Font.PLAIN, 16));
        txtRam = makeTextField();
        contentPanel.add(lblRam);
        contentPanel.add(txtRam, "growx, wrap");

        // Speed (Card)
        lblCard = new JLabel("Quãng đường (1 lần sạc):");
        lblCard.setFont(new Font("sansserif", Font.PLAIN, 16));
        txtCard = makeTextField();
        contentPanel.add(lblCard);
        contentPanel.add(txtCard, "growx");

        // Mode (Operate)
        lblOprerate = new JLabel("Chế độ vận hành:");
        lblOprerate.setFont(new Font("sansserif", Font.PLAIN, 16));
        String[] items = {"Eco", "Normal", "Sport"};
        cmbOperate = new MyCombobox<>(items);
        cmbOperate.setCustomFont(new Font("Times New Roman", Font.PLAIN, 15));
        cmbOperate.setCustomColors(Color.WHITE, Color.GRAY, Color.BLACK);
        contentPanel.add(lblOprerate);
        contentPanel.add(cmbOperate, "growx, wrap");

        // Price
        lblPrice = new JLabel("Price:");
        lblPrice.setFont(new Font("sansserif", Font.PLAIN, 16));
        txtPrice = makeTextField();
        contentPanel.add(lblPrice);
        contentPanel.add(txtPrice, "growx");

        // Quantity
        lblQuantity = new JLabel("Quantity:");
        lblQuantity.setFont(new Font("sansserif", Font.PLAIN, 16));
        spinnerQuantity = new JSpinner(new SpinnerNumberModel(1, 1, 1000, 1));
        spinnerQuantity.setFont(new Font("Times New Roman", Font.PLAIN, 15));
        contentPanel.add(lblQuantity);
        contentPanel.add(spinnerQuantity, "growx, wrap");

        // Warranty
        lblwaranty = new JLabel("Warranty Period:");
        lblwaranty.setFont(new Font("sansserif", Font.PLAIN, 16));
        txtwaranty = makeTextField();
        contentPanel.add(lblwaranty);
        contentPanel.add(txtwaranty, "growx");

        // Broken Quantity
        lblSpoiled = new JLabel("Broken Quantity:");
        lblSpoiled.setFont(new Font("sansserif", Font.PLAIN, 16));
        spinderBrokenQuantity = new JSpinner(new SpinnerNumberModel(0, 0, 1000, 1));
        spinderBrokenQuantity.setFont(new Font("Times New Roman", Font.PLAIN, 15));
        contentPanel.add(lblSpoiled);
        contentPanel.add(spinderBrokenQuantity, "growx, wrap");

        // Category
        lblCate = new JLabel("Category:");
        lblCate.setFont(new Font("sansserif", Font.PLAIN, 16));

        JMenuBar menuBar = new JMenuBar();
        menu = new JMenu("Choose");
        menu.setFont(new Font("Times New Roman", Font.PLAIN, 15));
        busProduct = new BusProduct();
        List<DTOCategory> listCategory = busProduct.getAllCategoriesWithSupplier();
        Map<String, JMenu> supplierMenuMap = new LinkedHashMap<>();

        for (DTOCategory dto : listCategory) {
            String supplierID = dto.getSupID();
            String categoryID = dto.getCategoryID();
            if (!supplierMenuMap.containsKey(supplierID)) {
                JMenu supplierMenu = new JMenu(supplierID);
                supplierMenu.setFont(new Font("Times New Roman", Font.PLAIN, 14));
                supplierMenuMap.put(supplierID, supplierMenu);
                menu.add(supplierMenu);
            }
            JMenuItem categoryItem = new JMenuItem(categoryID);
            supplierMenuMap.get(supplierID).add(categoryItem);
            categoryItem.addActionListener(e -> menu.setText(categoryID));
        }
        menuBar.add(menu);

        contentPanel.add(lblCate);
        contentPanel.add(menuBar, "growx");

        // Upload button
        bntupload = new MyButton("Upload", 0);
        bntupload.setBackgroundColor(Color.WHITE);
        bntupload.setPressedColor(Color.decode("#D3D3D3"));
        bntupload.setHoverColor(Color.decode("#EEEEEE"));
        bntupload.setFont(new Font("sansserif", Font.BOLD, 16));
        bntupload.setButtonIcon("src\\main\\resources\\Icons\\Admin_icon\\upload_image.png",
                40, 40, 10, SwingConstants.RIGHT, SwingConstants.CENTER);
        busProduct = new BusProduct();
        bntupload.addActionListener(e -> {
            try {
                // Debug: bypass BusProduct, dùng JFileChooser trực tiếp để kiểm tra
                JFileChooser chooser = new JFileChooser();
                chooser.setDialogTitle("Chọn ảnh");
                chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
                chooser.setAcceptAllFileFilterUsed(false);
                chooser.addChoosableFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Image files", "jpg", "jpeg", "png", "gif", "bmp"));
                int res = chooser.showOpenDialog(this);
                if (res != JFileChooser.APPROVE_OPTION) {
                    System.out.println("User cancelled file chooser");
                    CustomDialog.showError("Không có file nào được chọn.");
                    return;
                }
                java.io.File f = chooser.getSelectedFile();
                String selectedPath = f.getAbsolutePath();
                System.out.println("JFileChooser selected: " + selectedPath);

                if (selectedPath == null || selectedPath.isEmpty()) {
                    CustomDialog.showError("Không nhận được đường dẫn ảnh (file path trống).");
                    return;
                }
                String lower = selectedPath.toLowerCase();
                if (!(lower.endsWith(".jpg") || lower.endsWith(".jpeg") || lower.endsWith(".png") || lower.endsWith(".gif") || lower.endsWith(".bmp"))) {
                    CustomDialog.showError("Định dạng ảnh không được hỗ trợ: " + selectedPath);
                    return;
                }

                // Ghi lại vào biến image để saveProductFromGUI dùng
                image = selectedPath;
                System.out.println("Set image = " + image);
                JOptionPane.showMessageDialog(this, "Ảnh được chọn: " + image, "Thông báo", JOptionPane.INFORMATION_MESSAGE);

                // Nếu BusProduct có method setImagePath, gọi để giữ đồng bộ (không bắt buộc)
                try {
                    java.lang.reflect.Method m = busProduct.getClass().getMethod("setImagePath", String.class);
                    m.invoke(busProduct, selectedPath);
                    System.out.println("busProduct.setImagePath invoked");
                } catch (NoSuchMethodException ignore) {
                    // phương thức không tồn tại -> chỉ dùng biến image ở GUI
                } catch (Exception reflectEx) {
                    reflectEx.printStackTrace();
                }

            } catch (Exception ex) {
                ex.printStackTrace();
                CustomDialog.showError("Lỗi khi upload ảnh: " + ex.getClass().getSimpleName() + " - " + ex.getMessage());
            }
        });
        contentPanel.add(new JLabel(""));
        contentPanel.add(bntupload, "growx, wrap");

        // Save button
        bntSave = new MyButton("Save", 20);
        bntSave.setBackgroundColor(Color.decode("#E55454"));
        bntSave.setPressedColor(Color.decode("#C04444"));
        bntSave.setHoverColor(Color.decode("#FF7F7F"));
        bntSave.setFont(new Font("Times New Roman", Font.BOLD, 16));
        bntSave.setForeground(Color.WHITE);
        bntSave.addActionListener(e -> {
            saveProductFromGUI();
            ProductUpdateNotifier.getInstance().notifyProductUpdated();
        });
        contentPanel.add(new JLabel(""));
        contentPanel.add(bntSave, "span, align center, w 120!, h 40!");

        bg.add(contentPanel, "grow, pushy");

        // Tự động co font tiêu đề theo kích thước
        panelTitle.addComponentListener(new ComponentAdapter() {
            public void componentResized(ComponentEvent e) {
                float fontSize = Math.min(22f, Math.max(14f, panelTitle.getWidth() / 20f));
                lblTitle.setFont(lblTitle.getFont().deriveFont(fontSize));
            }
        });
    }

    private MyTextField makeTextField() {
        MyTextField field = new MyTextField();
        field.setBorder(BorderFactory.createLineBorder(Color.GRAY, 2));
        field.setTextFont(new Font("Times New Roman", Font.PLAIN, 15));
        field.setBackgroundColor(Color.decode("#F0FFFF"));
        return field;
    }

    public void saveProductFromGUI() {
        String productId = txtProductID.getText().trim();
        String productName = txtProductName.getText().trim();
        String color = txtCPU.getText().trim();
        String speed = txtCard.getText().trim();
        String batteryCapacity = txtRam.getText().trim();
        String categoryId = menu.getText().trim();
        String priceStr = txtPrice.getText().trim();
        int quantity = (int) spinnerQuantity.getValue();
        // Use GUI image field first, fall back to busProduct if set
        String imagePath = (image != null && !image.isEmpty()) ? image : busProduct.getImagePath();

        if (productId.isEmpty() || productName.isEmpty() || color.isEmpty()
                || speed.isEmpty() || batteryCapacity.isEmpty()
                || categoryId.isEmpty() || priceStr.isEmpty()) {
            CustomDialog.showError("Please fill in all required fields!");
            return;
        }

        double price;
        try {
            price = Double.parseDouble(priceStr);
            if (price < 0) {
                CustomDialog.showError("Price must be >= 0!");
                return;
            }
        } catch (NumberFormatException e) {
            CustomDialog.showError("Invalid price format!");
            return;
        }

        if (imagePath == null || imagePath.isEmpty()) {
            CustomDialog.showError("Please upload an image!");
            return;
        }

        DTOProduct product = new DTOProduct(
                productId, productName, color, speed, batteryCapacity,
                quantity, categoryId, imagePath, price
        );

        busProduct.saveProduct(product);
    }

    private void resetForm() {
        txtProductID.setText("");
        txtProductName.setText("");
        txtCPU.setText("");
        txtRam.setText("");
        txtCard.setText("");
        txtwaranty.setText("");
        txtPrice.setText("");
        cmbOperate.setSelectedIndex(0);
        spinnerQuantity.setValue(1);
        spinderBrokenQuantity.setValue(0);
        image = null;
    }

    @SuppressWarnings("unchecked")
    private void initComponents() {
        jMenuItem1 = new javax.swing.JMenuItem();
        bg = new javax.swing.JLayeredPane();

        jMenuItem1.setText("jMenuItem1");
        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        bg.setBackground(new java.awt.Color(255, 255, 255));
        bg.setOpaque(true);

        javax.swing.GroupLayout bgLayout = new javax.swing.GroupLayout(bg);
        bg.setLayout(bgLayout);
        bgLayout.setHorizontalGroup(
                bgLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGap(0, 1414, Short.MAX_VALUE)
        );
        bgLayout.setVerticalGroup(
                bgLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGap(0, 483, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(bg)
        );
        layout.setVerticalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(bg)
        );

        pack();
        setLocationRelativeTo(null);
    }

    public static void main(String args[]) {
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception ignored) {}

        java.awt.EventQueue.invokeLater(() -> new NewProduct().setVisible(true));
    }

    private javax.swing.JLayeredPane bg;
    private javax.swing.JMenuItem jMenuItem1;
}
