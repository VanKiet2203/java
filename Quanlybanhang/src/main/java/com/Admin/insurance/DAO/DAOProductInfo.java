package com.Admin.insurance.DAO;

import com.Admin.insurance.DTO.DTOProductInfo;
import com.ComponentandDatabase.Database_Connection.DatabaseConnection;
import java.sql.*;

public class DAOProductInfo {
    
    public DTOProductInfo getProductInfoByProductId(String productId) throws SQLException {
        String sql = "SELECT "
                   + "p.Product_ID, p.Product_Name, p.Category_ID, "
                   + "c.Sup_ID AS Supplier_ID, s.Sup_Name AS Brand, 'N/A' AS Warranty_Period "
                   + "FROM Product p "
                   + "JOIN Category c ON p.Category_ID = c.Category_ID "
                   + "JOIN Supplier s ON c.Sup_ID = s.Sup_ID "
                   + "WHERE p.Product_ID = ?";

        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setString(1, productId);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                return new DTOProductInfo(
                    rs.getString("Product_ID"),
                    rs.getString("Product_Name"),
                    rs.getString("Category_ID"),
                    rs.getString("Supplier_ID"),
                    rs.getString("Brand"),
                    rs.getString("Warranty_Period")
                );
            }
        }
        return null;
    }
}