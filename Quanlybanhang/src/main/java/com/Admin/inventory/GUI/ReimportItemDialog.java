package com.Admin.inventory.GUI;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.math.BigDecimal;
import java.sql.*;
import java.util.Objects;

/**
 * ReimportDialog - Giao diện nhập lại hàng (Reimport)
 * - Giao diện gọn, có validate.
 * - Gọi stored procedure sp_ReimportWarehouseItem.
 *
 * YÊU CẦU:
 * - Có sẵn JDBC URL/USER/PASS trong AppConfig (hoặc thay trực tiếp ở getConnection()).
 * - Bảng Product_Stock đã có dữ liệu Warehouse_Item_ID cần reimport.
 */
public class ReimportItemDialog extends JDialog {

    private JTextField txtWarehouseId;
    private JSpinner spnQuantity;
    private JTextField txtUnitPrice;
    private JTextField txtAdminId;
    private JTextField txtInvoiceNo; // optional
    private JButton btnSubmit;
    private JButton btnCancel;
    private JLabel lbStatus;
    
    // Additional fields for the new constructor
    private String warehouseId;
    private String productName;
    private int currentQuantity;

    public ReimportItemDialog(Frame owner) {
        super(owner, "Reimport Warehouse Item", true);
        initComponents();
        setSize(520, 360);
        setLocationRelativeTo(owner);
    }
    
    public ReimportItemDialog(JFrame parent, String warehouseId, String productName, int currentQuantity) {
        super(parent, "Reimport Existing Product", true);
        this.warehouseId = warehouseId;
        this.productName = productName;
        this.currentQuantity = currentQuantity;
        initComponents();
        setSize(450, 300);
        setLocationRelativeTo(parent);
    }

    private void initComponents() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBorder(new EmptyBorder(16,16,16,16));
        setContentPane(root);

        // Header
        JLabel header = new JLabel("Nhập lại hàng vào kho (Reimport)");
        header.setFont(header.getFont().deriveFont(Font.BOLD, 18f));
        header.setBorder(new EmptyBorder(0,0,10,0));
        root.add(header, BorderLayout.NORTH);

        // Form
        JPanel form = new JPanel();
        GroupLayout gl = new GroupLayout(form);
        form.setLayout(gl);
        gl.setAutoCreateGaps(true);
        gl.setAutoCreateContainerGaps(true);

        JLabel lbWarehouseId = new JLabel("Warehouse Item ID");
        txtWarehouseId = new JTextField();
        txtWarehouseId.setToolTipText("VD: WH001");

        JLabel lbQuantity = new JLabel("Số lượng nhập thêm");
        spnQuantity = new JSpinner(new SpinnerNumberModel(1, 1, 1_000_000, 1));

        JLabel lbUnitPrice = new JLabel("Giá nhập (tuỳ chọn)");
        txtUnitPrice = new JTextField();
        txtUnitPrice.setToolTipText("Bỏ trống nếu giữ nguyên giá nhập gần nhất");

        JLabel lbAdmin = new JLabel("Admin ID");
        txtAdminId = new JTextField();

        JLabel lbInvoice = new JLabel("Invoice No (tuỳ chọn)");
        txtInvoiceNo = new JTextField();
        txtInvoiceNo.setToolTipText("Bỏ trống để hệ thống tự sinh IMyyyymmdd####");

        lbStatus = new JLabel(" ");
        lbStatus.setForeground(new Color(0x666666));

        btnSubmit = new JButton("Reimport");
        btnSubmit.addActionListener(e -> onSubmit());

        btnCancel = new JButton("Đóng");
        btnCancel.addActionListener(e -> dispose());

        gl.setHorizontalGroup(gl.createParallelGroup()
            .addGroup(gl.createSequentialGroup()
                .addGroup(gl.createParallelGroup(GroupLayout.Alignment.TRAILING)
                    .addComponent(lbWarehouseId)
                    .addComponent(lbQuantity)
                    .addComponent(lbUnitPrice)
                    .addComponent(lbAdmin)
                    .addComponent(lbInvoice))
                .addGroup(gl.createParallelGroup()
                    .addComponent(txtWarehouseId)
                    .addComponent(spnQuantity)
                    .addComponent(txtUnitPrice)
                    .addComponent(txtAdminId)
                    .addComponent(txtInvoiceNo)))
            .addComponent(lbStatus)
            .addGroup(GroupLayout.Alignment.TRAILING, gl.createSequentialGroup()
                .addComponent(btnCancel, 120, 120, 120)
                .addComponent(btnSubmit, 140, 140, 140))
        );

        gl.setVerticalGroup(gl.createSequentialGroup()
            .addGroup(gl.createParallelGroup(GroupLayout.Alignment.BASELINE)
                .addComponent(lbWarehouseId).addComponent(txtWarehouseId))
            .addGroup(gl.createParallelGroup(GroupLayout.Alignment.BASELINE)
                .addComponent(lbQuantity).addComponent(spnQuantity))
            .addGroup(gl.createParallelGroup(GroupLayout.Alignment.BASELINE)
                .addComponent(lbUnitPrice).addComponent(txtUnitPrice))
            .addGroup(gl.createParallelGroup(GroupLayout.Alignment.BASELINE)
                .addComponent(lbAdmin).addComponent(txtAdminId))
            .addGroup(gl.createParallelGroup(GroupLayout.Alignment.BASELINE)
                .addComponent(lbInvoice).addComponent(txtInvoiceNo))
            .addGap(8)
            .addComponent(lbStatus)
            .addGap(12)
            .addGroup(gl.createParallelGroup(GroupLayout.Alignment.BASELINE)
                .addComponent(btnCancel)
                .addComponent(btnSubmit))
        );

        root.add(form, BorderLayout.CENTER);
    }

    private void onSubmit() {
        String whId = txtWarehouseId.getText().trim();
        int qty = (int) spnQuantity.getValue();
        String adminId = txtAdminId.getText().trim();
        String invoiceNo = txtInvoiceNo.getText().trim();
        BigDecimal unitPrice = null;

        if (whId.isEmpty()) {
            showError("Vui lòng nhập Warehouse Item ID");
            return;
        }
        if (adminId.isEmpty()) {
            showError("Vui lòng nhập Admin ID");
            return;
        }
        String priceStr = txtUnitPrice.getText().trim();
        if (!priceStr.isEmpty()) {
            try {
                unitPrice = new BigDecimal(priceStr);
                if (unitPrice.compareTo(BigDecimal.ZERO) < 0) {
                    showError("Giá nhập phải >= 0");
                    return;
                }
            } catch (NumberFormatException ex) {
                showError("Giá nhập không hợp lệ");
                return;
            }
        }

        // Gọi proc
        try (Connection cn = getConnection();
             CallableStatement cs = cn.prepareCall("{call dbo.sp_ReimportWarehouseItem(?,?,?,?,?)}")) {

            cs.setString(1, whId);
            cs.setInt(2, qty);
            if (unitPrice == null) cs.setNull(3, Types.DECIMAL); else cs.setBigDecimal(3, unitPrice);
            cs.setString(4, adminId);
            if (invoiceNo.isEmpty()) cs.setNull(5, Types.VARCHAR); else cs.setString(5, invoiceNo);

            boolean hasResult = cs.execute();
            String result = "SUCCESS";
            String message = "Reimport thành công";
            String returnedInvoice = null;

            // Đọc ResultSet đầu tiên (nếu proc SELECT)
            if (hasResult) {
                try (ResultSet rs = cs.getResultSet()) {
                    if (rs.next()) {
                        result = Objects.toString(rs.getString("Result"), result);
                        message = Objects.toString(rs.getString("Message"), message);
                        returnedInvoice = rs.getString("Invoice_No");
                    }
                }
            }

            if ("SUCCESS".equalsIgnoreCase(result)) {
                showInfo((returnedInvoice == null)
                        ? message
                        : (message + " | Invoice: " + returnedInvoice));
                dispose();
            } else {
                showError(message);
            }
        } catch (SQLException ex) {
            String msg = ex.getMessage();
            if (msg != null && msg.contains("WAREHOUSE_NOT_FOUND")) {
                showError("Không tìm thấy Warehouse ID: " + whId);
            } else if (msg != null && msg.contains("ADMIN_NOT_FOUND")) {
                showError("Admin ID không hợp lệ: " + adminId);
            } else {
                showError("Lỗi reimport: " + msg);
            }
        }
    }

    private void showError(String m) {
        lbStatus.setForeground(new Color(0xCC3333));
        lbStatus.setText(m);
        JOptionPane.showMessageDialog(this, m, "Error", JOptionPane.ERROR_MESSAGE);
    }

    private void showInfo(String m) {
        lbStatus.setForeground(new Color(0x2E7D32));
        lbStatus.setText(m);
        JOptionPane.showMessageDialog(this, m, "Success", JOptionPane.INFORMATION_MESSAGE);
    }

    private Connection getConnection() throws SQLException {
        // TODO: Sửa lại theo config của dự án bạn
        // Ví dụ:
        // String url = "jdbc:sqlserver://localhost:1433;databaseName=QuanLyKho;encrypt=false";
        // return DriverManager.getConnection(url, "sa", "your_password");
        String url = System.getProperty("db.url");
        String user = System.getProperty("db.user");
        String pass = System.getProperty("db.pass");
        return DriverManager.getConnection(url, user, pass);
    }

    // Demo mở dialog
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ReimportItemDialog(null).setVisible(true));
    }
}
