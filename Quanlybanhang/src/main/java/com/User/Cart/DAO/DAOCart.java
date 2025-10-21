package com.User.Cart.DAO;

import com.User.Cart.DTO.DTOCart;
import com.ComponentandDatabase.Database_Connection.DatabaseConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class DAOCart {
    public int getCurrentStock(String productId) {
        String sql = """
            SELECT 
                CASE 
                    WHEN EXISTS (SELECT 1 FROM Bill_Imported_Details bid WHERE bid.Warehouse_Item_ID = p.Warehouse_Item_ID)
                    THEN ISNULL(p.Quantity, 0)  -- Sử dụng Product.Quantity nếu đã có dữ liệu nhập
                    ELSE ISNULL(ps.Quantity_Stock, 0)  -- Sử dụng Product_Stock.Quantity_Stock nếu chưa có dữ liệu nhập
                END AS Current_Stock
            FROM Product p
            LEFT JOIN Product_Stock ps ON p.Warehouse_Item_ID = ps.Warehouse_Item_ID
            WHERE p.Product_ID = ? AND p.Status = 'Available'
        """;
        
        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, productId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    int stock = rs.getInt("Current_Stock");
                    System.out.println("Stock check for Product " + productId + ": " + stock);
                    return stock;
                } else {
                    System.out.println("No product found with ID: " + productId);
                }
            }
        } catch (SQLException e) {
            System.out.println("Error getting stock for product " + productId + ": " + e.getMessage());
            e.printStackTrace();
        }
        return 0;
    }

    // Them san pham vao gio hang
    public boolean addToCart(DTOCart cartItem) {
        String sql = "INSERT INTO Cart (Cart_ID, Customer_ID, Product_ID, Quantity, Status) VALUES (?, ?, ?, ?, ?)";
        
        Connection conn = null;
        PreparedStatement stmt = null;
        
        try {
            conn = DatabaseConnection.connect();
            if (conn == null || conn.isClosed()) {
                System.out.println("Connection is null or closed, creating new connection...");
                conn = DatabaseConnection.connect();
            }
            
            stmt = conn.prepareStatement(sql);
            
            System.out.println("=== DEBUG CART ADD ===");
            System.out.println("Customer ID: " + cartItem.getCustomerID());
            System.out.println("Product ID: " + cartItem.getProductID());
            System.out.println("Quantity: " + cartItem.getQuantity());
            
            // Validate stock to prevent over-adding
            int currentStock = getCurrentStock(cartItem.getProductID());
            System.out.println("Current Stock: " + currentStock);
            
            if (cartItem.getQuantity() <= 0 || cartItem.getQuantity() > currentStock) {
                System.out.println("Stock validation failed: Quantity=" + cartItem.getQuantity() + ", Stock=" + currentStock);
                return false;
            }

            // Tạo Cart_ID duy nhất
            String cartId = "CART-" + System.currentTimeMillis() + "-" + cartItem.getCustomerID();
            System.out.println("Generated Cart ID: " + cartId);
            
            stmt.setString(1, cartId);
            stmt.setString(2, cartItem.getCustomerID());
            stmt.setString(3, cartItem.getProductID());
            stmt.setInt(4, cartItem.getQuantity());
            stmt.setString(5, "Available");
            
            int rowsAffected = stmt.executeUpdate();
            System.out.println("Rows affected: " + rowsAffected);
            
            if (rowsAffected > 0) {
                System.out.println("Product added to cart successfully!");
                return true;
            } else {
                System.out.println("No rows affected - insert failed");
                return false;
            }
            
        } catch (SQLException e) {
            System.out.println("SQL Error: " + e.getMessage());
            System.out.println("SQL State: " + e.getSQLState());
            System.out.println("Error Code: " + e.getErrorCode());
            e.printStackTrace();
            return false;
        } finally {
            // Dong resources
            try {
                if (stmt != null) stmt.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                System.out.println("Error closing resources: " + e.getMessage());
            }
        }
    }
    
    // Kiem tra san pham da co trong gio hang chua
    public boolean isProductInCart(String customerID, String productID) {
        String sql = "SELECT COUNT(*) FROM Cart WHERE Customer_ID = ? AND Product_ID = ? AND Status = 'Available'";
        
        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, customerID);
            stmt.setString(2, productID);
            
            try (var rs = stmt.executeQuery()) {
                if (rs.next()) {
                    int count = rs.getInt(1);
                    System.out.println("Product in cart check: " + count + " items found");
                    return count > 0;
                }
            }
        } catch (SQLException e) {
            System.out.println("Error checking if product in cart: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }
    
    public boolean updateCartItem(DTOCart cartItem) {
        String sql = "UPDATE Cart SET Quantity = ? WHERE Customer_ID = ? AND Product_ID = ? AND Status = 'Available'";

        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            // Validate stock to prevent exceeding available
            int currentStock = getCurrentStock(cartItem.getProductID());
            if (cartItem.getQuantity() <= 0 || cartItem.getQuantity() > currentStock) {
                return false;
            }

            stmt.setInt(1, cartItem.getQuantity());
            stmt.setString(2, cartItem.getCustomerID());
            stmt.setString(3, cartItem.getProductID());

            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

     public ArrayList<DTOCart> getCartItemsByCustomer(String customerID) {
        ArrayList<DTOCart> cartItems = new ArrayList<>();
        String sql = "SELECT * FROM Cart WHERE Customer_ID = ? AND Status = 'Available'";

        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, customerID);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                DTOCart item = new DTOCart(
                    rs.getString("Cart_ID"),
                    rs.getString("Customer_ID"),
                    rs.getString("Product_ID"),
                    rs.getInt("Quantity")
                );
                cartItems.add(item);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return cartItems;
    }
    
    public boolean deleteCartItem(String customerId, String productId) {
        String sql = "UPDATE Cart SET Status = 'Unavailable' WHERE Customer_ID = ? AND Product_ID = ?";

        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, customerId);
            stmt.setString(2, productId);

            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
     
   public boolean clearCart(String customerID) {
        String sql = "UPDATE Cart SET Status = 'Unavailable' WHERE Customer_ID = ?";
        
        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, customerID);
            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
            
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
     
}