package com.Admin.promotion.DAO;

import com.Admin.promotion.DTO.DTOPromotion;
import com.ComponentandDatabase.Database_Connection.DatabaseConnection;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class DAOPromotion {

    // ============================================
    // READ Operations
    // ============================================
    
    /**
     * Lấy tất cả mã giảm giá
     */
    public List<DTOPromotion> getAllPromotions() throws SQLException {
        List<DTOPromotion> list = new ArrayList<>();
        String sql = "SELECT Promotion_Code, Promotion_Name, Start_Date, End_Date, Discount_Percent "
                   + "FROM Promotion ORDER BY Start_Date DESC";
        
        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            
            while (rs.next()) {
                DTOPromotion p = new DTOPromotion();
                p.setPromotionCode(rs.getString("Promotion_Code"));
                p.setPromotionName(rs.getString("Promotion_Name"));
                p.setStartDate(rs.getDate("Start_Date").toLocalDate());
                p.setEndDate(rs.getDate("End_Date").toLocalDate());
                p.setDiscountPercent(rs.getBigDecimal("Discount_Percent"));
                list.add(p);
            }
        }
        return list;
    }
    
    /**
     * Lấy mã giảm giá theo code
     */
    public DTOPromotion getPromotionByCode(String code) throws SQLException {
        String sql = "SELECT Promotion_Code, Promotion_Name, Start_Date, End_Date, Discount_Percent "
                   + "FROM Promotion WHERE Promotion_Code = ?";
        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, code);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                DTOPromotion p = new DTOPromotion();
                p.setPromotionCode(rs.getString("Promotion_Code"));
                p.setPromotionName(rs.getString("Promotion_Name"));
                p.setStartDate(rs.getDate("Start_Date").toLocalDate());
                p.setEndDate(rs.getDate("End_Date").toLocalDate());
                p.setDiscountPercent(rs.getBigDecimal("Discount_Percent"));
                return p;
            }
            return null;
        }
    }
    
    /**
     * Tìm kiếm mã giảm giá
     */
    public List<DTOPromotion> searchPromotions(String searchBy, String keyword) throws SQLException {
        List<DTOPromotion> list = new ArrayList<>();
        String sql = "SELECT Promotion_Code, Promotion_Name, Start_Date, End_Date, Discount_Percent FROM Promotion WHERE Status = 'Available' AND ";
        
        switch (searchBy) {
            case "Code":
                sql += "Promotion_Code LIKE ?";
                break;
            case "Name":
                sql += "Promotion_Name LIKE ?";
                break;
            case "Active":
                sql += "GETDATE() BETWEEN Start_Date AND End_Date";
                break;
            case "Expired":
                sql += "End_Date < GETDATE()";
                break;
            case "Upcoming":
                sql += "Start_Date > GETDATE()";
                break;
            default:
                sql += "Promotion_Code LIKE ? OR Promotion_Name LIKE ?";
        }
        
        sql += " ORDER BY Start_Date DESC";
        
        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            if (!searchBy.equals("Active") && !searchBy.equals("Expired") && !searchBy.equals("Upcoming")) {
                ps.setString(1, "%" + keyword + "%");
                if (searchBy.equals("All")) {
                    ps.setString(2, "%" + keyword + "%");
                }
            }
            
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                DTOPromotion p = new DTOPromotion();
                p.setPromotionCode(rs.getString("Promotion_Code"));
                p.setPromotionName(rs.getString("Promotion_Name"));
                p.setStartDate(rs.getDate("Start_Date").toLocalDate());
                p.setEndDate(rs.getDate("End_Date").toLocalDate());
                p.setDiscountPercent(rs.getBigDecimal("Discount_Percent"));
                list.add(p);
            }
        }
        return list;
    }

    /**
     * Kiểm tra mã giảm giá có hiệu lực
     */
    public boolean isPromotionActive(String code, LocalDate onDate) throws SQLException {
        String sql = "SELECT 1 FROM Promotion WHERE Promotion_Code=? AND Start_Date<=? AND End_Date>=? AND Status = 'Available'";
        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, code);
            ps.setDate(2, Date.valueOf(onDate));
            ps.setDate(3, Date.valueOf(onDate));
            ResultSet rs = ps.executeQuery();
            return rs.next();
        }
    }
    
    // ============================================
    // CREATE Operation
    // ============================================
    
    /**
     * Thêm mã giảm giá mới
     */
    public boolean addPromotion(DTOPromotion promotion) throws SQLException {
        String sql = "INSERT INTO Promotion (Promotion_Code, Promotion_Name, Start_Date, End_Date, Discount_Percent, Status) "
                   + "VALUES (?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, promotion.getPromotionCode());
            ps.setString(2, promotion.getPromotionName());
            ps.setDate(3, Date.valueOf(promotion.getStartDate()));
            ps.setDate(4, Date.valueOf(promotion.getEndDate()));
            ps.setBigDecimal(5, promotion.getDiscountPercent());
            ps.setString(6, "Available");
            
            return ps.executeUpdate() > 0;
        }
    }
    
    // ============================================
    // UPDATE Operation
    // ============================================
    
    /**
     * Cập nhật mã giảm giá
     */
    public boolean updatePromotion(DTOPromotion promotion) throws SQLException {
        String sql = "UPDATE Promotion SET Promotion_Name=?, Start_Date=?, End_Date=?, Discount_Percent=? "
                   + "WHERE Promotion_Code=?";
        
        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, promotion.getPromotionName());
            ps.setDate(2, Date.valueOf(promotion.getStartDate()));
            ps.setDate(3, Date.valueOf(promotion.getEndDate()));
            ps.setBigDecimal(4, promotion.getDiscountPercent());
            ps.setString(5, promotion.getPromotionCode());
            
            return ps.executeUpdate() > 0;
        }
    }
    
    // ============================================
    // DELETE Operation
    // ============================================
    
    /**
     * Xóa mã giảm giá (Soft delete)
     */
    public boolean deletePromotion(String code) throws SQLException {
        String sql = "UPDATE Promotion SET Status = 'Unavailable' WHERE Promotion_Code=?";
        
        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, code);
            return ps.executeUpdate() > 0;
        }
    }
    
    // ============================================
    // Utility Methods
    // ============================================
    
    /**
     * Kiểm tra mã giảm giá đã tồn tại chưa
     */
    public boolean isPromotionCodeExists(String code) throws SQLException {
        String sql = "SELECT 1 FROM Promotion WHERE Promotion_Code=? AND Status = 'Available'";
        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, code);
            ResultSet rs = ps.executeQuery();
            return rs.next();
        }
    }
    
    /**
     * Đếm số lượng mã giảm giá đang hoạt động
     */
    public int countActivePromotions() throws SQLException {
        String sql = "SELECT COUNT(*) FROM Promotion WHERE GETDATE() BETWEEN Start_Date AND End_Date AND Status = 'Available'";
        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
            return 0;
        }
    }
}