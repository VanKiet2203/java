package com.Admin.login.DAO;

import java.sql.*;
import javax.swing.*;
import org.mindrot.jbcrypt.BCrypt;
import com.formdev.flatlaf.FlatLightLaf;
import com.Admin.login.DTO.DTOAccount_ad;
import com.ComponentandDatabase.Components.CustomDialog;
import com.ComponentandDatabase.Database_Connection.DatabaseConnection;

public class ControlRegister {
    private Connection conn;
    private DatabaseConnection db = new DatabaseConnection();
    private DTOAccount_ad registeredAccount;

    // 🟢 Getter để lấy đối tượng DTO sau khi đăng ký thành công
    public DTOAccount_ad getRegisteredAccount() {
        return registeredAccount;
    }

    // 🟢 Hàm đăng ký khách hàng
    public boolean registerAd(String idCard, String fullName, String gender,
                                 String contact, String email, String password) {
        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
        } catch (Exception e) {
            e.printStackTrace();
        }

        conn = db.connect();

        if (idCard.isEmpty() || fullName.isEmpty() || gender.isEmpty() ||
            email.isEmpty() || contact.isEmpty() || password.isEmpty()) {
            CustomDialog.showError("Please fill in all required fields!");
            return false;
        }

        if (!email.matches("^[\\w.-]+@[\\w-]+\\.[a-z]{2,4}$")) {
            CustomDialog.showError("Invalid email format!");
            return false;
        }

        if (!contact.matches("^0\\d{9}$")) {
            CustomDialog.showError("Phone number must be 10 digits and start with 0!");
            return false;
        }

        if (conn == null) {
            CustomDialog.showError("Database connection failed!");
            return false;
        }

        try {
            String checkSql = "SELECT COUNT(*) FROM Admin WHERE Admin_ID = ? AND Status = 'Available'";
            try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
                checkStmt.setString(1, idCard);
                ResultSet rs = checkStmt.executeQuery();
                if (rs.next() && rs.getInt(1) > 0) {
                    CustomDialog.showError("ID Card already exists! Please enter a different ID.");
                    return false;
                }
            }

            String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt(12));

            String sql = "INSERT INTO Admin (Admin_ID, Admin_Name, Gender, Email, Contact, Password) VALUES (?, ?, ?, ?, ?, ?)";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, idCard);
                pstmt.setString(2, fullName);
                pstmt.setString(3, gender);
                pstmt.setString(4, email);
                pstmt.setString(5, contact);
                pstmt.setString(6, hashedPassword);

                int rowsInserted = pstmt.executeUpdate();
                if (rowsInserted > 0) {
                    // ✅ Gán vào DTO sau khi đăng ký thành công
                    registeredAccount = new DTOAccount_ad(
                        idCard, fullName, gender, email, contact, hashedPassword
                    );
                    CustomDialog.showSuccess("Registration successful!");
                    return true;
                }
            }

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(null, "Lỗi khi đăng ký: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
            return false;
        }
        return false;
    }
}
