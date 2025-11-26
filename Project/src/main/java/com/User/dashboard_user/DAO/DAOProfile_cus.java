package com.User.dashboard_user.DAO;

import com.ComponentandDatabase.Components.CustomDialog;
import com.User.dashboard_user.DTO.DTOProfile_cus;
import com.ComponentandDatabase.Components.MyTextField;
import com.ComponentandDatabase.Components.MyCombobox;
import com.toedter.calendar.JDateChooser;
import javax.swing.JTextArea;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

// Giả sử bạn đã có class ConnectDB để lấy Connection
import com.ComponentandDatabase.Database_Connection.DatabaseConnection; 

public class DAOProfile_cus {

    public void showProfile(String emailInput, 
                             MyTextField txtID, 
                             MyTextField txtFullName, 
                             MyCombobox<String> cmbGender, 
                             JDateChooser dateOfBirth, 
                             MyTextField txtEmail, 
                             MyTextField txtContact, 
                             JTextArea txtAddress,
                             javax.swing.JPanel panelUpload) 
    {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        
        try {
            conn = DatabaseConnection.connect();
            String sql = "SELECT * FROM Customer WHERE Email = ?";
            ps = conn.prepareStatement(sql);
            ps.setString(1, emailInput);
            rs = ps.executeQuery();
            
            if (rs.next()) {
                DTOProfile_cus profile = new DTOProfile_cus();
                profile.setCustomerID(rs.getString("Customer_ID"));
                profile.setFullName(rs.getString("Full_Name"));
                profile.setGender(rs.getString("Gender"));
                profile.setDateOfBirth(rs.getDate("Date_Of_Birth"));
                profile.setEmail(rs.getString("Email"));
                profile.setContact(rs.getString("Contact"));
                profile.setAddress(rs.getString("Address"));

                // Đổ dữ liệu lên giao diện
                txtID.setText(profile.getCustomerID());
                txtFullName.setText(profile.getFullName());

                if ("Male".equalsIgnoreCase(profile.getGender())) {
                    cmbGender.setSelectedItem("Male");
                } else {
                    cmbGender.setSelectedItem("Female");
                }

                if (profile.getDateOfBirth() != null) {
                    dateOfBirth.setDate(profile.getDateOfBirth());
                } else {
                    dateOfBirth.setDate(null);
                }

                txtEmail.setText(profile.getEmail());
                txtContact.setText(profile.getContact());
                txtAddress.setText(profile.getAddress());
                
                // Load và hiển thị avatar
                String imagePath = rs.getString("Image");
                if (imagePath != null && !imagePath.trim().isEmpty()) {
                    try {
                        java.io.File imageFile = new java.io.File(imagePath);
                        if (imageFile.exists()) {
                            java.awt.Image img = new javax.swing.ImageIcon(imageFile.getAbsolutePath()).getImage()
                                    .getScaledInstance(100, 100, java.awt.Image.SCALE_SMOOTH);
                            javax.swing.JLabel lblImage = new javax.swing.JLabel(new javax.swing.ImageIcon(img));
                            lblImage.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
                            lblImage.setVerticalAlignment(javax.swing.SwingConstants.CENTER);
                            
                            panelUpload.removeAll();
                            panelUpload.add(lblImage, "pos 0.5al 0.5al");
                            panelUpload.setVisible(true);
                            panelUpload.revalidate();
                            panelUpload.repaint();
                        }
                    } catch (Exception e) {
                        System.out.println("Error loading avatar: " + e.getMessage());
                    }
                }
            } else {
                System.out.println("Không tìm thấy khách hàng với email: " + emailInput);
            }
            
        } catch (SQLException ex) {
            ex.printStackTrace();
        } finally {
            // Đóng tài nguyên
            try { if (rs != null) rs.close(); } catch (SQLException e) { e.printStackTrace(); }
            try { if (ps != null) ps.close(); } catch (SQLException e) { e.printStackTrace(); }
            try { if (conn != null) conn.close(); } catch (SQLException e) { e.printStackTrace(); }
        }
    }
    
    
    public void updateProfile(MyTextField txtID, 
                               MyTextField txtFullName, 
                               MyCombobox<String> cmbGender, 
                               JDateChooser dateOfBirth, 
                               MyTextField txtEmail, 
                               MyTextField txtContact, 
                               JTextArea txtAddress,
                               String imagePath) 
    {
        Connection conn = null;
        PreparedStatement ps = null;

        try {
            conn = DatabaseConnection.connect();
            // Chỉ cập nhật Contact và Address (không cập nhật Full_Name, Gender, Date_Of_Birth, Email vì đã khóa)
            // Kiểm tra xem cột Image có tồn tại không
            boolean hasImageColumn = false;
            if (imagePath != null && !imagePath.trim().isEmpty()) {
                try {
                    java.sql.DatabaseMetaData meta = conn.getMetaData();
                    java.sql.ResultSet columns = meta.getColumns(null, null, "Customer", "Image");
                    hasImageColumn = columns.next();
                    columns.close();
                } catch (SQLException e) {
                    // Nếu không kiểm tra được, giả định không có cột Image
                    hasImageColumn = false;
                }
            }
            
            String sql = "UPDATE Customer SET Contact = ?, Address = ?";
            if (hasImageColumn && imagePath != null && !imagePath.trim().isEmpty()) {
                sql += ", Image = ?";
            }
            sql += " WHERE Email = ?";
            ps = conn.prepareStatement(sql);

            ps.setString(1, txtContact.getText());
            ps.setString(2, txtAddress.getText());
            int paramIndex = 3;
            if (hasImageColumn && imagePath != null && !imagePath.trim().isEmpty()) {
                ps.setString(paramIndex, imagePath);
                paramIndex++;
            }
            ps.setString(paramIndex, txtEmail.getText()); // WHERE Email = ?

            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                CustomDialog.showSuccess("Imformation updated successfully !");
            } else {
               CustomDialog.showError("Imformation updated failure! Can't find the customer with this email.");
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
        } finally {
            try { if (ps != null) ps.close(); } catch (SQLException e) { e.printStackTrace(); }
            try { if (conn != null) conn.close(); } catch (SQLException e) { e.printStackTrace(); }
        }
    }
    
    public String getCustomerID(String email) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        String customerID = null;

        System.out.println("🔍 DEBUG - Getting Customer ID for email: " + email);

        try {
            conn = DatabaseConnection.connect();
            String sql = "SELECT Customer_ID FROM Customer WHERE Email = ?";
            ps = conn.prepareStatement(sql);
            ps.setString(1, email);
            rs = ps.executeQuery();

            if (rs.next()) {
                customerID = rs.getString("Customer_ID");
                System.out.println("✅ Found Customer ID: " + customerID);
            } else {
                System.out.println("❌ No customer found with email: " + email);
            }

        } catch (SQLException ex) {
            System.out.println("❌ SQL Error getting Customer ID: " + ex.getMessage());
            ex.printStackTrace();
        } finally {
            // Đóng tài nguyên
            try { if (rs != null) rs.close(); } catch (SQLException e) { e.printStackTrace(); }
            try { if (ps != null) ps.close(); } catch (SQLException e) { e.printStackTrace(); }
            try { if (conn != null) conn.close(); } catch (SQLException e) { e.printStackTrace(); }
        }

        return customerID;
    }
    
    public String getCustomerName(String customerID) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        
        try {
            conn = DatabaseConnection.connect();
            String sql = "SELECT Full_Name FROM Customer WHERE Customer_ID = ?";
            ps = conn.prepareStatement(sql);
            ps.setString(1, customerID);
            rs = ps.executeQuery();
            
            if (rs.next()) {
                return rs.getString("Full_Name");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (rs != null) rs.close();
                if (ps != null) ps.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        
        return "User";
    }
    
    public String getProfileImagePath(String customerID) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        String imagePath = null;

        try {
            conn = DatabaseConnection.connect();
            // Kiểm tra xem cột Image có tồn tại không
            String sql = "SELECT Image FROM Customer WHERE Customer_ID = ?";
            ps = conn.prepareStatement(sql);
            ps.setString(1, customerID);
            rs = ps.executeQuery();

            if (rs.next()) {
                try {
                    imagePath = rs.getString("Image");
                } catch (SQLException e) {
                    // Cột Image không tồn tại, trả về null
                    return null;
                }
            }
        } catch (SQLException e) {
            // Nếu lỗi do cột không tồn tại, trả về null
            return null;
        } finally {
            try {
                if (rs != null) rs.close();
                if (ps != null) ps.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        return imagePath;
    }
    
    public boolean changePassword(String email, String oldPassword, String newPassword, String confirmPassword) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            // Kiểm tra trống
            if (newPassword == null || newPassword.isEmpty() || confirmPassword == null || confirmPassword.isEmpty() || 
                oldPassword == null || oldPassword.isEmpty()) {
                CustomDialog.showError("Password fields cannot be empty.");
                return false;
            }

            // Kiểm tra khớp mật khẩu mới
            if (!newPassword.equals(confirmPassword)) {
                CustomDialog.showError("New passwords do not match.");
                return false;
            }

            conn = DatabaseConnection.connect();
            
            // Kiểm tra mật khẩu cũ
            String sqlCheck = "SELECT Password FROM Customer WHERE Email = ?";
            ps = conn.prepareStatement(sqlCheck);
            ps.setString(1, email);
            rs = ps.executeQuery();
            
            if (!rs.next()) {
                CustomDialog.showError("Customer not found.");
                return false;
            }
            
            String storedPassword = rs.getString("Password");
            boolean isValidOldPassword;
            
            // Kiểm tra mật khẩu cũ (hỗ trợ cả bcrypt và plaintext)
            if (storedPassword != null && storedPassword.matches("^\\$2[aby]\\$.*")) {
                isValidOldPassword = org.mindrot.jbcrypt.BCrypt.checkpw(oldPassword, storedPassword);
            } else {
                isValidOldPassword = oldPassword != null && oldPassword.equals(storedPassword);
            }
            
            if (!isValidOldPassword) {
                CustomDialog.showError("Old password is incorrect.");
                return false;
            }
            
            // Đóng ResultSet và PreparedStatement trước khi tạo mới
            rs.close();
            ps.close();
            
            // Cập nhật mật khẩu mới
            String hashedPassword = org.mindrot.jbcrypt.BCrypt.hashpw(newPassword, org.mindrot.jbcrypt.BCrypt.gensalt());
            
            String sqlUpdate = "UPDATE Customer SET Password = ? WHERE Email = ?";
            ps = conn.prepareStatement(sqlUpdate);
            ps.setString(1, hashedPassword);
            ps.setString(2, email);

            int rows = ps.executeUpdate();
            if (rows > 0) {
                CustomDialog.showSuccess("Password changed successfully!");
                return true;
            } else {
                CustomDialog.showError("Failed to update password.");
            }
        } catch (SQLException ex) {
            CustomDialog.showError("Error changing password: " + ex.getMessage());
            ex.printStackTrace();
        } finally {
            try {
                if (rs != null) rs.close();
                if (ps != null) ps.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        return false;
    }
    
}
