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
                p.List_Price_Before,
                p.List_Price_After,
                ISNULL(p.Quantity, 0) AS Quantity,
                p.Warranty_Months,
                CASE 
                    WHEN ISNULL(p.Quantity, 0) = 0 THEN 'Unavailable'
                    ELSE 'Available'
                END AS Status,
                p.Category_ID,
                p.Sup_ID,
                p.Warehouse_Item_ID,
                p.Image
            FROM 
                Product p
            WHERE p.Status = 'Available'
            """ + (condition != null && !condition.trim().isEmpty() ? " AND " + condition : "");

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
                    prd.setListPriceBefore(rs.getBigDecimal("List_Price_Before"));
                    prd.setListPriceAfter(rs.getBigDecimal("List_Price_After"));
                    prd.setQuantity(rs.getInt("Quantity"));
                    prd.setWarrantyMonths(rs.getInt("Warranty_Months"));
                    prd.setStatus(rs.getString("Status"));
                    prd.setCategoryID(rs.getString("Category_ID"));
                    prd.setSupID(rs.getString("Sup_ID"));
                    prd.setWarehouseItemID(rs.getString("Warehouse_Item_ID"));
                    prd.setImage(rs.getString("Image"));
                    list.add(prd);
                }
            }
            return list;

        } catch (SQLException e) {
            e.printStackTrace();
            CustomDialog.showError("Lỗi khi tải danh sách sản phẩm!");
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
                p.List_Price_Before,
                p.List_Price_After,
                ISNULL(p.Quantity, 0) AS Quantity,
                p.Warranty_Months,
                CASE 
                    WHEN ISNULL(p.Quantity, 0) = 0 THEN 'Unavailable'
                    ELSE 'Available'
                END AS Status,
                p.Category_ID,
                p.Sup_ID,
                p.Warehouse_Item_ID,
                p.Image
            FROM Product p
            WHERE p.Product_ID = ? AND p.Status = 'Available'
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
                    prd.setListPriceBefore(rs.getBigDecimal("List_Price_Before"));
                    prd.setListPriceAfter(rs.getBigDecimal("List_Price_After"));
                    prd.setQuantity(rs.getInt("Quantity"));
                    prd.setWarrantyMonths(rs.getInt("Warranty_Months"));
                    prd.setStatus(rs.getString("Status"));
                    prd.setCategoryID(rs.getString("Category_ID"));
                    prd.setSupID(rs.getString("Sup_ID"));
                    prd.setWarehouseItemID(rs.getString("Warehouse_Item_ID"));
                    prd.setImage(rs.getString("Image"));
                    return prd;
                }
            }
            return null;
        } catch (SQLException e) {
            e.printStackTrace();
            CustomDialog.showError("Lỗi khi truy vấn chi tiết sản phẩm!");
        }

        return null;
    }

    public String getBrandByProductId(String productId) {
        String sql = """
            SELECT s.Sup_Name
            FROM Product p
            JOIN Supplier s ON p.Sup_ID = s.Sup_ID
            WHERE p.Product_ID = ?
        """;

        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, productId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return rs.getString("Sup_Name");
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
                p.List_Price_Before,
                p.List_Price_After,
                ISNULL(p.Quantity, 0) AS Quantity,
                p.Warranty_Months,
                CASE 
                    WHEN ISNULL(p.Quantity, 0) = 0 THEN 'Unavailable'
                    ELSE 'Available'
                END AS Status,
                p.Category_ID,
                p.Sup_ID,
                p.Warehouse_Item_ID,
                p.Image
            FROM 
                Product p
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
                    product.setListPriceBefore(rs.getBigDecimal("List_Price_Before"));
                    product.setListPriceAfter(rs.getBigDecimal("List_Price_After"));
                    product.setQuantity(rs.getInt("Quantity"));
                    product.setWarrantyMonths(rs.getInt("Warranty_Months"));
                    product.setStatus(rs.getString("Status"));
                    product.setCategoryID(rs.getString("Category_ID"));
                    product.setSupID(rs.getString("Sup_ID"));
                    product.setWarehouseItemID(rs.getString("Warehouse_Item_ID"));
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
