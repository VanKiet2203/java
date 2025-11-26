
package com.User.order.DAO;

import com.User.order.DTO.DTO_Order;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import com.ComponentandDatabase.Database_Connection.DatabaseConnection;
import java.sql.ResultSet;
import java.util.ArrayList;
import com.User.order.DTO.DTO_Order;
import java.sql.Date;
import java.sql.Time;
import java.math.BigDecimal;
public class DAO_Order {

    /**
     * Insert order và trả về error message nếu có lỗi, null nếu thành công
     */
    public String insertOrder(DTO_Order order) {
        String sql = "INSERT INTO [Orders] (Order_No, Customer_ID, Cart_ID, Total_Quantity_Product, Total_Price, Payment, Promotion_Code, Date_Order, Time_Order) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            // Kiểm tra Order_No đã tồn tại với Customer_ID này chưa
            String checkSql = "SELECT COUNT(*) FROM [Orders] WHERE Order_No = ? AND Customer_ID = ?";
            try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
                checkStmt.setString(1, order.getOrderNo());
                checkStmt.setString(2, order.getCustomerID());
                ResultSet rs = checkStmt.executeQuery();
                if (rs.next() && rs.getInt(1) > 0) {
                    String errorMsg = "Order number already exists for this customer. Please try again.";
                    System.err.println("Error: Order_No " + order.getOrderNo() + " already exists for Customer " + order.getCustomerID());
                    return errorMsg;
                }
            }

            stmt.setString(1, order.getOrderNo());
            stmt.setString(2, order.getCustomerID());
            
            // Kiểm tra Cart_ID có tồn tại không (nếu không null)
            if (order.getCartID() != null && !order.getCartID().trim().isEmpty()) {
                String checkCartSql = "SELECT COUNT(*) FROM [Cart] WHERE Cart_ID = ?";
                try (PreparedStatement checkCartStmt = conn.prepareStatement(checkCartSql)) {
                    checkCartStmt.setString(1, order.getCartID());
                    ResultSet rs = checkCartStmt.executeQuery();
                    if (rs.next() && rs.getInt(1) == 0) {
                        System.err.println("Warning: Cart_ID " + order.getCartID() + " does not exist. Setting to NULL.");
                        stmt.setNull(3, java.sql.Types.VARCHAR);
                    } else {
                        stmt.setString(3, order.getCartID());
                    }
                }
            } else {
                stmt.setNull(3, java.sql.Types.VARCHAR);
            }
            
            stmt.setInt(4, order.getTotalQuantityProduct());
            stmt.setBigDecimal(5, order.getTotalPrice());
            stmt.setString(6, order.getPayment());
            
            // Set Promotion_Code (có thể null) - kiểm tra foreign key
            if (order.getPromotionCode() == null || order.getPromotionCode().trim().isEmpty()) {
                stmt.setNull(7, java.sql.Types.VARCHAR);
            } else {
                // Kiểm tra Promotion_Code có tồn tại không
                String checkPromoSql = "SELECT COUNT(*) FROM [Promotion] WHERE Promotion_Code = ? AND Status = 'Available'";
                try (PreparedStatement checkPromoStmt = conn.prepareStatement(checkPromoSql)) {
                    checkPromoStmt.setString(1, order.getPromotionCode());
                    ResultSet rs = checkPromoStmt.executeQuery();
                    if (rs.next() && rs.getInt(1) == 0) {
                        String errorMsg = "Invalid promotion code: " + order.getPromotionCode() + ". The code does not exist or has expired.";
                        System.err.println("Warning: Promotion_Code " + order.getPromotionCode() + " does not exist or is not available.");
                        return errorMsg;
                    } else {
                        stmt.setString(7, order.getPromotionCode());
                    }
                }
            }
            
            stmt.setDate(8, java.sql.Date.valueOf(order.getDateOrder()));
            stmt.setTime(9, java.sql.Time.valueOf(order.getTimeOrder()));

            int rows = stmt.executeUpdate();
            if (rows <= 0) {
                return "Failed to insert order. No rows affected.";
            }
            return null; // Success

        } catch (SQLException e) {
            String errorMsg = "Database connection error: " + e.getMessage();
            System.err.println("SQL Error inserting order: " + e.getMessage());
            System.err.println("Error Code: " + e.getErrorCode());
            System.err.println("SQL State: " + e.getSQLState());
            e.printStackTrace();
            
            // Kiểm tra loại lỗi cụ thể
            if (e.getErrorCode() == 547) { // Foreign key constraint violation
                if (e.getMessage().contains("Promotion")) {
                    errorMsg = "Invalid promotion code. The code does not exist or has expired.";
                } else if (e.getMessage().contains("Customer")) {
                    errorMsg = "Invalid customer ID.";
                } else if (e.getMessage().contains("Cart")) {
                    errorMsg = "Invalid cart ID.";
                }
            } else if (e.getErrorCode() == 2627 || e.getErrorCode() == 2601) { // Primary key violation
                errorMsg = "Order number already exists. Please try again.";
            }
            
            return errorMsg;
        }
    }
    public ArrayList<DTO_Order> getOrdersByCustomer(String customerID) {
        ArrayList<DTO_Order> orderList = new ArrayList<>();
        // Chỉ SELECT các cột cần thiết, đảm bảo lấy đúng cột Status (không phải Record_Status)
        // Filter theo Record_Status = 'Available' để chỉ lấy record chưa bị xóa
        String sql = "SELECT Order_No, Customer_ID, Cart_ID, Total_Quantity_Product, Total_Price, " +
                     "Payment, Promotion_Code, Date_Order, Time_Order, Status " +
                     "FROM Orders " +
                     "WHERE Customer_ID = ? AND Record_Status = 'Available'";

        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, customerID);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String orderNo = rs.getString("Order_No");
                String customerId = rs.getString("Customer_ID");
                String cartId = rs.getString("Cart_ID"); // Lấy Cart_ID
                int totalQuantity = rs.getInt("Total_Quantity_Product");
                BigDecimal totalPrice = rs.getBigDecimal("Total_Price");
                String payment = rs.getString("Payment");
                String promotionCode = rs.getString("Promotion_Code"); // Có thể null
                Date dateOrder = rs.getDate("Date_Order");
                Time timeOrder = rs.getTime("Time_Order");
                // Lấy đúng cột Status (không phải Record_Status)
                String status = rs.getString("Status");
                
                // Debug: Kiểm tra status được lấy ra
                if (status != null && (status.equalsIgnoreCase("unavailable") || status.equalsIgnoreCase("available"))) {
                    System.err.println("WARNING: Status có giá trị '" + status + "' - có thể đang lấy nhầm Record_Status!");
                    System.err.println("Order_No: " + orderNo + ", Customer_ID: " + customerId);
                }

                DTO_Order order = new DTO_Order(orderNo, customerId, cartId, totalQuantity, totalPrice, payment, 
                                                promotionCode, dateOrder.toLocalDate(), timeOrder.toLocalTime());
                order.setStatus(status); // Set status từ cột Status
                orderList.add(order);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return orderList;
    }
    
    public ArrayList<DTO_Order> getSortedOrdersByCustomer(String customerID) {
        ArrayList<DTO_Order> orderList = new ArrayList<>();
        // Chỉ SELECT các cột cần thiết, đảm bảo lấy đúng cột Status (không phải Record_Status)
        // Filter theo Record_Status = 'Available' để chỉ lấy record chưa bị xóa
        String sql = "SELECT Order_No, Customer_ID, Cart_ID, Total_Quantity_Product, Total_Price, " +
                     "Payment, Promotion_Code, Date_Order, Time_Order, Status " +
                     "FROM Orders " +
                     "WHERE Customer_ID = ? AND Record_Status = 'Available' " +
                     "ORDER BY Date_Order DESC, Time_Order DESC"; // Sap xep ngay va gio giam dan

        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, customerID);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String orderNo = rs.getString("Order_No");
                String customerId = rs.getString("Customer_ID");
                String cartId = rs.getString("Cart_ID"); // Lấy Cart_ID
                int totalQuantity = rs.getInt("Total_Quantity_Product");
                BigDecimal totalPrice = rs.getBigDecimal("Total_Price");
                String payment = rs.getString("Payment");
                String promotionCode = rs.getString("Promotion_Code"); // Có thể null
                Date dateOrder = rs.getDate("Date_Order");
                Time timeOrder = rs.getTime("Time_Order");
                // Lấy đúng cột Status (không phải Record_Status)
                String status = rs.getString("Status");

                DTO_Order order = new DTO_Order(orderNo, customerId, cartId, totalQuantity, totalPrice, payment, 
                                              promotionCode, dateOrder.toLocalDate(), timeOrder.toLocalTime());
                order.setStatus(status); // Set status từ cột Status
                orderList.add(order);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return orderList;
    }
    
    /**
     * Lấy promotion code từ order theo OrderNo
     */
    public String getPromotionCodeByOrderNo(String orderNo) {
        String sql = "SELECT Promotion_Code FROM Orders WHERE Order_No = ?";
        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, orderNo);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return rs.getString("Promotion_Code"); // Có thể null
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Cancel order if not confirmed yet
     */
    public boolean cancelOrder(String orderNo, String customerID) {
        String sqlOrder = "UPDATE Orders SET Status = 'Cancelled' WHERE Order_No = ? AND Customer_ID = ? AND Status = 'Waiting'";
        String sqlDetails = "UPDATE Orders_Details SET Status = 'Cancelled' WHERE Order_No = ? AND Customer_ID = ? AND Status = 'Waiting'";
        try (Connection conn = DatabaseConnection.connect()) {
            conn.setAutoCommit(false);
            try (PreparedStatement stmt1 = conn.prepareStatement(sqlOrder);
                 PreparedStatement stmt2 = conn.prepareStatement(sqlDetails)) {
                stmt1.setString(1, orderNo);
                stmt1.setString(2, customerID);
                int affected = stmt1.executeUpdate();

                stmt2.setString(1, orderNo);
                stmt2.setString(2, customerID);
                stmt2.executeUpdate();

                conn.commit();
                return affected > 0;
            } catch (SQLException ex) {
                conn.rollback();
                throw ex;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
}