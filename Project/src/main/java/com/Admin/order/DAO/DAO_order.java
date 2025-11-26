package com.Admin.order.DAO;

import com.Admin.order.DTO.DTO_order;
import com.Admin.order.DTO.DTO_orderDetails;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import java.io.FileOutputStream;
import java.math.BigDecimal;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import com.ComponentandDatabase.Database_Connection.DatabaseConnection;

public class DAO_order {
    
  
    public List<DTO_order> getAllOrdersSorted() {
        List<DTO_order> orders = new ArrayList<>();
        String sql = "SELECT o.Order_No, o.Customer_ID, o.Cart_ID, o.Total_Quantity_Product, " +
                     "o.Total_Price, o.Payment, o.Date_Order, o.Time_Order, " +
                     "o.Status, c.Full_Name, c.Address, c.Contact " +
                     "FROM Orders o " +
                     "JOIN Customer c ON o.Customer_ID = c.Customer_ID " +
                     "WHERE o.Status = 'Waiting' AND o.Record_Status = 'Available' AND c.Record_Status = 'Available' " +
                     "ORDER BY o.Date_Order DESC, o.Time_Order DESC";
        
        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                DTO_order order = new DTO_order();
                order.setOrderNo(rs.getString("Order_No"));
                order.setCustomerID(rs.getString("Customer_ID"));
                order.setCartID(rs.getString("Cart_ID")); // Thêm Cart_ID
                order.setTotalQuantityProduct(rs.getInt("Total_Quantity_Product"));
                order.setTotalPrice(rs.getBigDecimal("Total_Price"));
                order.setPayment(rs.getString("Payment"));
                order.setDateOrder(rs.getDate("Date_Order").toLocalDate());
                order.setTimeOrder(rs.getTime("Time_Order").toLocalTime());
                
                // Thêm thông tin khách hàng và trạng thái
                order.setCustomerName(rs.getString("Full_Name"));
                order.setAddress(rs.getString("Address"));
                order.setContact(rs.getString("Contact"));
                order.setStatus(rs.getString("Status"));
                
                orders.add(order);
            }
            
            // Sắp xếp theo yêu cầu: Status='Waiting' -> Date Order -> Time Order
            orders = orders.stream()
                .sorted(Comparator.comparing(DTO_order::getStatus, (s1, s2) -> {
                    if (s1.equals("Waiting") && !s2.equals("Waiting")) return -1;
                    if (!s1.equals("Waiting") && s2.equals("Waiting")) return 1;
                    return 0;
                })
                .thenComparing(DTO_order::getDateOrder)
                .thenComparing(DTO_order::getTimeOrder))
                .collect(Collectors.toList());
            
        } catch (SQLException e) {
            e.printStackTrace();
        }    
        return orders;
    }
    
    public boolean updateOrderStatus(String orderNo, String newStatus) {
        // Cập nhật Status trong bảng Orders (chính)
        String updateOrdersSQL = "UPDATE Orders SET Status = ? WHERE Order_No = ?";
        // Cập nhật Status trong bảng Orders_Details (chi tiết)
        String updateOrderDetailsSQL = "UPDATE Orders_Details SET Status = ? WHERE Order_No = ?";
        // KHÔNG cập nhật Product.Quantity thủ công vì trigger sẽ tự động xử lý
        // String updateStockSQL = """
        //     UPDATE Product 
        //     SET Quantity = Quantity + od.Sold_Quantity
        //     FROM Product p
        //     JOIN Orders_Details od ON p.Product_ID = od.Product_ID
        //     WHERE od.Order_No = ? AND od.Status = 'Cancelled'
        // """;

        try (Connection conn = DatabaseConnection.connect()) {
            conn.setAutoCommit(false);
            
            try (PreparedStatement updateOrdersStmt = conn.prepareStatement(updateOrdersSQL);
                 PreparedStatement updateOrderDetailsStmt = conn.prepareStatement(updateOrderDetailsSQL)) {
                
                // Cập nhật trạng thái trong bảng Orders
                updateOrdersStmt.setString(1, newStatus);
                updateOrdersStmt.setString(2, orderNo);
                int ordersRows = updateOrdersStmt.executeUpdate();
                
                // Cập nhật trạng thái trong bảng Orders_Details
                updateOrderDetailsStmt.setString(1, newStatus);
                updateOrderDetailsStmt.setString(2, orderNo);
                int orderDetailsRows = updateOrderDetailsStmt.executeUpdate();
                
                // KHÔNG cập nhật Product.Quantity thủ công vì trigger sẽ tự động xử lý
                // Nếu cần trả lại số lượng khi cancel order, trigger sẽ xử lý
                
                conn.commit();
                return ordersRows > 0 && orderDetailsRows > 0;
                
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
    
    public List<DTO_orderDetails> getAllOrderDetails() throws SQLException {
        List<DTO_orderDetails> orderDetails = new ArrayList<>();
        String sql = """
            SELECT od.Order_No, od.Customer_ID, od.Product_ID, od.Price, od.Sold_Quantity, 
                   od.Date_Order, od.Time_Order, od.Status,
                   p.Product_Name, p.Color, p.Speed, p.Battery_Capacity,
                   ISNULL(ps.Quantity_Stock, 0) AS Total_Imported,
                   p.Quantity AS Current_Stock,
                   ISNULL(ps.Quantity_Stock, 0) - p.Quantity AS Total_Sold
            FROM Orders_Details od
            JOIN Product p ON od.Product_ID = p.Product_ID
            LEFT JOIN Product_Stock ps ON p.Warehouse_Item_ID = ps.Warehouse_Item_ID
            WHERE od.Record_Status = 'Available' AND od.Status = 'Waiting'
        """;

        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                DTO_orderDetails detail = new DTO_orderDetails();
                detail.setOrderNo(rs.getString("Order_No"));
                detail.setCustomerID(rs.getString("Customer_ID"));
                detail.setProductID(rs.getString("Product_ID"));
                detail.setPrice(rs.getBigDecimal("Price"));
                detail.setQuantity(rs.getInt("Sold_Quantity"));
                detail.setDateOrder(rs.getDate("Date_Order").toLocalDate());
                detail.setTimeOrder(rs.getTime("Time_Order").toLocalTime());
                detail.setStatus(rs.getString("Status"));

                orderDetails.add(detail);
            }
        }
        return orderDetails;
    }
    
    public boolean exportOrderDetailsToExcel(String filePath) {
        try {
            List<DTO_orderDetails> orderDetails = getAllOrderDetails();

            try (Workbook workbook = new XSSFWorkbook()) {
                Sheet sheet = workbook.createSheet("Order Details");

                // Tạo header
                String[] headers = {
                    "Order.No", "Customer.ID", "Product.ID", 
                    "Price", "Quantity", "Subtotal",
                    "Date Order", "Time Order", "Status"
                };

                // Style cho header
                CellStyle headerStyle = workbook.createCellStyle();
                Font headerFont = workbook.createFont();
                headerFont.setBold(true);
                headerStyle.setFont(headerFont);
                headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
                headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

                // Tạo header row
                Row headerRow = sheet.createRow(0);
                for (int i = 0; i < headers.length; i++) {
                    Cell cell = headerRow.createCell(i);
                    cell.setCellValue(headers[i]);
                    cell.setCellStyle(headerStyle);
                }

                // Đổ dữ liệu
                int rowNum = 1;
                for (DTO_orderDetails detail : orderDetails) {
                    Row row = sheet.createRow(rowNum++);

                    row.createCell(0).setCellValue(detail.getOrderNo());
                    row.createCell(1).setCellValue(detail.getCustomerID());
                    row.createCell(2).setCellValue(detail.getProductID());
                    row.createCell(3).setCellValue(detail.getPrice().doubleValue());
                    row.createCell(4).setCellValue(detail.getQuantity());

                    // Tính subtotal
                    BigDecimal subtotal = detail.getPrice().multiply(new BigDecimal(detail.getQuantity()));
                    row.createCell(5).setCellValue(subtotal.doubleValue());

                    row.createCell(6).setCellValue(detail.getDateOrder().toString());
                    row.createCell(7).setCellValue(detail.getTimeOrder().toString());
                    row.createCell(8).setCellValue(detail.getStatus());
                }

                // Tự động điều chỉnh cột
                for (int i = 0; i < headers.length; i++) {
                    sheet.autoSizeColumn(i);
                }

                // Ghi file
                try (FileOutputStream outputStream = new FileOutputStream(filePath)) {
                    workbook.write(outputStream);
                    return true;
                }
            }
        } catch (SQLException | IOException e) {
            e.printStackTrace();
            return false;
        }
}
    
    public List<DTO_order> searchOrders(String searchType, String keyword, String status) throws SQLException {
       List<DTO_order> orders = new ArrayList<>();

       StringBuilder sql = new StringBuilder(
           "SELECT DISTINCT o.Order_No, o.Customer_ID, o.Total_Quantity_Product, " +
           "o.Total_Price, o.Payment, o.Date_Order, o.Time_Order, " +
           "od.Status, c.Full_Name, c.Address, c.Contact " +
           "FROM Orders o " +
           "JOIN Orders_Details od ON o.Order_No = od.Order_No " +
           "JOIN Customer c ON o.Customer_ID = c.Customer_ID " +
           "WHERE od.Status = ?"
       );

       // Add search condition if keyword is not empty
       if (!keyword.isEmpty()) {
           sql.append(" AND ");
           switch (searchType) {
               case "Order.No":
                   sql.append("o.Order_No LIKE ?");
                   break;
               case "Customer.ID":
                   sql.append("o.Customer_ID LIKE ?");
                   break;
               case "Date Order":
                   if (keyword.matches("\\d{1,2}/\\d{1,2}/\\d{4}")) {
                       sql.append("CONVERT(varchar, o.Date_Order, 103) = ?");
                   } else if (keyword.matches("\\d{4}-\\d{1,2}-\\d{1,2}")) {
                       sql.append("o.Date_Order = ?");
                   } else {
                       sql.append("CONVERT(varchar, o.Date_Order, 103) LIKE ?");
                   }
                   break;
               default:
                   sql.append("o.Order_No LIKE ?");
           }
       }

       // Sắp xếp theo thời gian đặt hàng
       sql.append(" ORDER BY o.Date_Order DESC, o.Time_Order DESC");

       try (Connection conn = DatabaseConnection.connect();
            PreparedStatement stmt = conn.prepareStatement(sql.toString())) {

           stmt.setString(1, status);

           if (!keyword.isEmpty()) {
               if (searchType.equals("Date Order")) {
                   if (keyword.matches("\\d{1,2}/\\d{1,2}/\\d{4}")) {
                       stmt.setString(2, keyword);
                   } else if (keyword.matches("\\d{4}-\\d{1,2}-\\d{1,2}")) {
                       stmt.setDate(2, java.sql.Date.valueOf(keyword));
                   } else {
                       stmt.setString(2, "%" + keyword + "%");
                   }
               } else {
                   stmt.setString(2, "%" + keyword + "%");
               }
           }

           try (ResultSet rs = stmt.executeQuery()) {
               while (rs.next()) {
                   DTO_order order = new DTO_order();
                   order.setOrderNo(rs.getString("Order_No"));
                   order.setCustomerID(rs.getString("Customer_ID"));
                   order.setTotalQuantityProduct(rs.getInt("Total_Quantity_Product"));
                   order.setTotalPrice(rs.getBigDecimal("Total_Price"));
                   order.setPayment(rs.getString("Payment"));
                   order.setDateOrder(rs.getDate("Date_Order").toLocalDate());
                   order.setTimeOrder(rs.getTime("Time_Order").toLocalTime());
                   order.setCustomerName(rs.getString("Full_Name"));
                   order.setAddress(rs.getString("Address"));
                   order.setContact(rs.getString("Contact"));
                   order.setStatus(rs.getString("Status"));

                   orders.add(order);
               }
           }
       }
       return orders;
   }
   
    public boolean deleteOrder(String orderNo) {
        String sql = "UPDATE Orders SET Record_Status = 'Unavailable' WHERE Order_No = ?";
        
        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
           
           stmt.setString(1, orderNo);
           int affectedRows = stmt.executeUpdate();
           
           return affectedRows > 0;
           
       } catch (SQLException e) {
           e.printStackTrace();
           return false;
       }
   }
   
   /**
    * Lấy danh sách các Order đã confirmed để có thể chuyển sang Export
    */
   public List<DTO_order> getConfirmedOrders() {
       List<DTO_order> orders = new ArrayList<>();
       String sql = "SELECT o.Order_No, o.Customer_ID, o.Cart_ID, o.Total_Quantity_Product, " +
                    "o.Total_Price, o.Payment, o.Date_Order, o.Time_Order, " +
                    "o.Status, c.Full_Name, c.Address, c.Contact " +
                    "FROM Orders o " +
                    "JOIN Customer c ON o.Customer_ID = c.Customer_ID " +
                    "WHERE o.Status = 'Confirmed' AND o.Record_Status = 'Available' AND c.Record_Status = 'Available' " +
                    "ORDER BY o.Date_Order DESC, o.Time_Order DESC";
       
       try (Connection conn = DatabaseConnection.connect();
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery()) {
           
           while (rs.next()) {
               DTO_order order = new DTO_order();
               order.setOrderNo(rs.getString("Order_No"));
               order.setCustomerID(rs.getString("Customer_ID"));
               order.setCartID(rs.getString("Cart_ID"));
               order.setTotalQuantityProduct(rs.getInt("Total_Quantity_Product"));
               order.setTotalPrice(rs.getBigDecimal("Total_Price"));
               order.setPayment(rs.getString("Payment"));
               order.setDateOrder(rs.getDate("Date_Order").toLocalDate());
               order.setTimeOrder(rs.getTime("Time_Order").toLocalTime());
               order.setStatus(rs.getString("Status"));
               order.setCustomerName(rs.getString("Full_Name"));
               order.setAddress(rs.getString("Address"));
               order.setContact(rs.getString("Contact"));
               
               orders.add(order);
           }
           
       } catch (SQLException e) {
           e.printStackTrace();
       }    
       return orders;
   }
   
   /**
    * Lấy chi tiết Order đã confirmed để chuyển sang Export
    */
   public List<DTO_orderDetails> getConfirmedOrderDetails(String orderNo) throws SQLException {
       List<DTO_orderDetails> orderDetails = new ArrayList<>();
       String sql = """
           SELECT od.Order_No, od.Customer_ID, od.Product_ID, od.Price, od.Sold_Quantity, 
                  od.Date_Order, od.Time_Order, od.Status,
                  p.Product_Name, p.Color, p.Speed, p.Battery_Capacity
           FROM Orders_Details od
           JOIN Product p ON od.Product_ID = p.Product_ID
           WHERE od.Order_No = ? AND od.Record_Status = 'Available' AND od.Status = 'Confirmed'
       """;

       try (Connection conn = DatabaseConnection.connect();
            PreparedStatement stmt = conn.prepareStatement(sql)) {
           
           stmt.setString(1, orderNo);
           try (ResultSet rs = stmt.executeQuery()) {
               while (rs.next()) {
                   DTO_orderDetails detail = new DTO_orderDetails();
                   detail.setOrderNo(rs.getString("Order_No"));
                   detail.setCustomerID(rs.getString("Customer_ID"));
                   detail.setProductID(rs.getString("Product_ID"));
                   detail.setPrice(rs.getBigDecimal("Price"));
                   detail.setQuantity(rs.getInt("Sold_Quantity"));
                   detail.setDateOrder(rs.getDate("Date_Order").toLocalDate());
                   detail.setTimeOrder(rs.getTime("Time_Order").toLocalTime());
                   detail.setStatus(rs.getString("Status"));

                   orderDetails.add(detail);
               }
           }
       }
       return orderDetails;
   }

}