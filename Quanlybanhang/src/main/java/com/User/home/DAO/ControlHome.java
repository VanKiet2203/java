package com.User.home.DAO;

import com.ComponentandDatabase.Components.CustomDialog;
import com.ComponentandDatabase.Database_Connection.DatabaseConnection;
import com.User.home.DTO.productDTO;

import java.sql.*;
import java.util.ArrayList;

public class ControlHome {

    public ArrayList<productDTO> showProduct(String condition) {
        ArrayList<productDTO> list = new ArrayList<>();

        String sql = """
            SELECT 
                p.Product_ID,
                p.Product_Name,
                p.Color,
                p.Battery_Capacity,
                p.Speed,
                p.Price,
                ISNULL(p.Quantity, 0) AS Quantity,
                CASE 
                    WHEN ISNULL(p.Quantity, 0) = 0 THEN 'Unavailable'
                    ELSE 'Available'
                END AS Status,
                c.Category_ID,
                p.Image
            FROM 
                Product p
            LEFT JOIN Category c ON p.Category_ID = c.Category_ID
            LEFT JOIN Product_Stock ps ON ps.Warehouse_Item_ID = p.Warehouse_Item_ID
            """ + (condition != null && !condition.trim().isEmpty() ? " WHERE " + condition : "");

        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    productDTO prd = new productDTO();
                    prd.setProductID(rs.getString("Product_ID"));
                    prd.setProductName(rs.getString("Product_Name"));
                    prd.setColor(rs.getString("Color"));
                    prd.setBatteryCapacity(rs.getString("Battery_Capacity"));
                    prd.setSpeed(rs.getString("Speed"));
                    prd.setPrice(rs.getBigDecimal("Price"));
                    prd.setQuantity(rs.getInt("Quantity"));
                    prd.setStatus(rs.getString("Status"));
                    prd.setCategoryID(rs.getString("Category_ID"));
                    prd.setImage(rs.getString("Image"));
                    list.add(prd);
                }
            }
            return list;

        } catch (SQLException e) {
            e.printStackTrace();
            CustomDialog dialog = new CustomDialog();
            dialog.showError("Lỗi khi tải danh sách sản phẩm!");
            return new ArrayList<>();
        }
    }

    public productDTO getProductByID(String productId) {
        String sql = """
            SELECT 
                p.Product_ID,
                p.Product_Name,
                p.Color,
                p.Battery_Capacity,
                p.Speed,
                p.Price,
                ISNULL(p.Quantity, 0) AS Quantity,
                CASE 
                    WHEN ISNULL(p.Quantity, 0) = 0 THEN 'Unavailable'
                    ELSE 'Available'
                END AS Status,
                c.Category_ID,
                p.Image
            FROM Product p
            LEFT JOIN Category c ON p.Category_ID = c.Category_ID
            WHERE p.Product_ID = ?
        """;

        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, productId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    productDTO prd = new productDTO();
                    prd.setProductID(rs.getString("Product_ID"));
                    prd.setProductName(rs.getString("Product_Name"));
                    prd.setColor(rs.getString("Color"));
                    prd.setBatteryCapacity(rs.getString("Battery_Capacity"));
                    prd.setSpeed(rs.getString("Speed"));
                    prd.setPrice(rs.getBigDecimal("Price"));
                    prd.setQuantity(rs.getInt("Quantity"));
                    prd.setStatus(rs.getString("Status"));
                    prd.setCategoryID(rs.getString("Category_ID"));
                    prd.setImage(rs.getString("Image"));
                    return prd;
                }
            }
            return null;
        } catch (SQLException e) {
            e.printStackTrace();
            CustomDialog dialog = new CustomDialog();
            dialog.showError("Lỗi khi truy vấn chi tiết sản phẩm!");
        }

        return null;
    }

    public String getBrandByProductId(String productId) {
        String sql = """
            SELECT s.Sup_ID
            FROM Product p
            JOIN Category c ON p.Category_ID = c.Category_ID
            JOIN Supplier s ON c.Sup_ID = s.Sup_ID
            WHERE p.Product_ID = ?
        """;

        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, productId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return rs.getString("Sup_ID");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    public productDTO getProductById(String productId) {
        String sql = """
            SELECT 
                p.Product_ID,
                p.Product_Name,
                p.Color,
                p.Battery_Capacity,
                p.Speed,
                p.Price,
                p.Quantity AS Quantity,
                CASE 
                    WHEN p.Quantity <= 0 THEN 'Unavailable'
                    ELSE 'Available'
                END AS Status,
                c.Category_ID,
                p.Image
            FROM 
                Product p
            LEFT JOIN Category c ON p.Category_ID = c.Category_ID
            WHERE p.Product_ID = ? AND p.Status = 'Available'
        """;

        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, productId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    productDTO product = new productDTO();
                    product.setProductID(rs.getString("Product_ID"));
                    product.setProductName(rs.getString("Product_Name"));
                    product.setColor(rs.getString("Color"));
                    product.setBatteryCapacity(rs.getString("Battery_Capacity"));
                    product.setSpeed(rs.getString("Speed"));
                    product.setPrice(rs.getBigDecimal("Price"));
                    product.setQuantity(rs.getInt("Quantity"));
                    product.setStatus(rs.getString("Status"));
                    product.setCategoryID(rs.getString("Category_ID"));
                    product.setImage(rs.getString("Image"));
                    return product;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}
