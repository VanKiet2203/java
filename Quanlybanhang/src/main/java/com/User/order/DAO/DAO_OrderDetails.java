
package com.User.order.DAO;

import com.User.order.DTO.DTO_OrderDetails;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.util.ArrayList;
import com.ComponentandDatabase.Database_Connection.DatabaseConnection;

public class DAO_OrderDetails {

    public boolean insertOrderDetail(DTO_OrderDetails detail) {
        String insertSQL = "INSERT INTO Orders_Details (Order_No, Customer_ID, Product_ID, Price, Sold_Quantity, Date_Order, Time_Order, Status) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConnection.connect()) {
            conn.setAutoCommit(false);
            
            try (PreparedStatement insertStmt = conn.prepareStatement(insertSQL)) {
                
                // Kiểm tra tồn kho trước khi insert (chỉ kiểm tra, không trừ)
                String checkStockSQL = "SELECT Quantity FROM Product WHERE Product_ID = ?";
                try (PreparedStatement checkStmt = conn.prepareStatement(checkStockSQL)) {
                    checkStmt.setString(1, detail.getProductID());
                    try (ResultSet rs = checkStmt.executeQuery()) {
                        if (!rs.next() || rs.getInt("Quantity") < detail.getQuantity()) {
                            conn.rollback();
                            return false; // Không đủ hàng
                        }
                    }
                }
                
                // Insert order detail (KHÔNG trừ số lượng ở đây)
                insertStmt.setString(1, detail.getOrderNo());
                insertStmt.setString(2, detail.getCustomerID());
                insertStmt.setString(3, detail.getProductID());
                insertStmt.setBigDecimal(4, detail.getPrice());
                insertStmt.setInt(5, detail.getQuantity());
                insertStmt.setDate(6, java.sql.Date.valueOf(detail.getDateOrder()));
                insertStmt.setTime(7, java.sql.Time.valueOf(detail.getTimeOrder()));
                insertStmt.setString(8, detail.getStatus());
                
                int insertRows = insertStmt.executeUpdate();
                
                if (insertRows > 0) {
                    conn.commit();
                    return true;
                } else {
                    conn.rollback();
                    return false;
                }
                
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    // Method để duyệt đơn hàng và trừ số lượng tồn kho
    public boolean approveOrder(String orderNo) {
        String updateStatusSQL = "UPDATE Orders_Details SET Status = 'Approved' WHERE Order_No = ?";
        String updateStockSQL = """
            UPDATE Product 
            SET Quantity = Quantity - od.Sold_Quantity
            FROM Product p
            JOIN Orders_Details od ON p.Product_ID = od.Product_ID
            WHERE od.Order_No = ? AND od.Status = 'Approved'
        """;
        
        try (Connection conn = DatabaseConnection.connect()) {
            conn.setAutoCommit(false);
            
            try (PreparedStatement updateStatusStmt = conn.prepareStatement(updateStatusSQL);
                 PreparedStatement updateStockStmt = conn.prepareStatement(updateStockSQL)) {
                
                // Cập nhật trạng thái đơn hàng
                updateStatusStmt.setString(1, orderNo);
                int statusRows = updateStatusStmt.executeUpdate();
                
                if (statusRows > 0) {
                    // Trừ số lượng tồn kho
                    updateStockStmt.setString(1, orderNo);
                    int stockRows = updateStockStmt.executeUpdate();
                    
                    if (stockRows > 0) {
                        conn.commit();
                        return true;
                    } else {
                        conn.rollback();
                        return false;
                    }
                } else {
                    conn.rollback();
                    return false;
                }
                
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
     public ArrayList<DTO_OrderDetails> getOrderDetails(String customerID, String orderNo) {
         ArrayList<DTO_OrderDetails> detailsList = new ArrayList<>();
        String sql = """
            SELECT od.*, p.Product_Name, p.Color, p.Speed, p.Battery_Capacity,
                   ISNULL(ps.Quantity_Stock, 0) AS Total_Imported,
                   p.Quantity AS Current_Stock,
                   ISNULL(ps.Quantity_Stock, 0) - p.Quantity AS Total_Sold
            FROM Orders_Details od
            JOIN Product p ON od.Product_ID = p.Product_ID
            LEFT JOIN Product_Stock ps ON p.Warehouse_Item_ID = ps.Warehouse_Item_ID
            WHERE od.Customer_ID = ? AND od.Order_No = ? AND od.Record_Status = 'Available'
        """;
        
        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, customerID);
            stmt.setString(2, orderNo);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    DTO_OrderDetails detail = new DTO_OrderDetails();
                    detail.setOrderNo(rs.getString("Order_No"));
                    detail.setCustomerID(rs.getString("Customer_ID"));
                    detail.setProductID(rs.getString("Product_ID"));
                    detail.setPrice(rs.getBigDecimal("Price"));
                    detail.setQuantity(rs.getInt("Sold_Quantity"));
                    detail.setDateOrder(rs.getDate("Date_Order").toLocalDate());
                    detail.setTimeOrder(rs.getTime("Time_Order").toLocalTime());
                    detail.setStatus(rs.getString("Status"));
                    
                    detailsList.add(detail);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return detailsList;
    }
    
    
}
