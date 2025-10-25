package com.Admin.export;

import com.Admin.export.BUS.BUS_ExportBill;
import com.Admin.export.DAO.DAO_ExportBill;
import com.ComponentandDatabase.Database_Connection.DatabaseConnection;
import java.sql.*;

/**
 * Test class để kiểm tra và sửa lỗi số lượng
 */
public class TestQuantityFix {
    
    public static void main(String[] args) {
        System.out.println("=== KIỂM TRA VÀ SỬA LỖI SỐ LƯỢNG ===");
        
        try {
            // 1. Kiểm tra tình trạng hiện tại
            System.out.println("\n1. Kiểm tra tình trạng hiện tại:");
            checkCurrentStatus();
            
            // 2. Sửa lỗi số lượng
            System.out.println("\n2. Sửa lỗi số lượng:");
            BUS_ExportBill busExport = new BUS_ExportBill();
            boolean success = busExport.resetAndSyncAllQuantities();
            
            if (success) {
                System.out.println("✅ Sửa lỗi thành công!");
                
                // 3. Kiểm tra lại sau khi sửa
                System.out.println("\n3. Kiểm tra lại sau khi sửa:");
                checkCurrentStatus();
            } else {
                System.out.println("❌ Sửa lỗi thất bại!");
            }
            
        } catch (Exception e) {
            System.err.println("❌ Lỗi: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static void checkCurrentStatus() {
        String sql = """
            SELECT 
                p.Product_ID,
                p.Product_Name,
                ISNULL(ps.Quantity_Stock, 0) AS Total_Imported,
                p.Quantity AS Current_Stock,
                ISNULL(sold.Sold_Quantity, 0) AS Total_Sold,
                CASE 
                    WHEN ISNULL(ps.Quantity_Stock, 0) = p.Quantity + ISNULL(sold.Sold_Quantity, 0)
                    THEN '✓ Cân bằng'
                    ELSE '✗ Lệch'
                END AS Balance_Status
            FROM Product p
            LEFT JOIN Product_Stock ps ON p.Warehouse_Item_ID = ps.Warehouse_Item_ID
            LEFT JOIN (
                SELECT p.Warehouse_Item_ID, 
                       ISNULL(SUM(bed.Sold_Quantity), 0) AS Sold_Quantity
                FROM Product p
                LEFT JOIN Bill_Exported_Details bed ON p.Product_ID = bed.Product_ID AND bed.Status = 'Available'
                WHERE p.Warehouse_Item_ID IS NOT NULL
                GROUP BY p.Warehouse_Item_ID
            ) sold ON ps.Warehouse_Item_ID = sold.Warehouse_Item_ID
            WHERE p.Status = 'Available' AND (ps.Status = 'Available' OR ps.Status IS NULL)
            ORDER BY Balance_Status DESC, p.Product_ID
        """;
        
        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            
            System.out.println("Product_ID | Product_Name | Imported | Stock | Sold | Status");
            System.out.println("-----------|--------------|----------|-------|------|--------");
            
            while (rs.next()) {
                System.out.printf("%-10s | %-12s | %-8d | %-5d | %-4d | %s%n",
                    rs.getString("Product_ID"),
                    rs.getString("Product_Name"),
                    rs.getInt("Total_Imported"),
                    rs.getInt("Current_Stock"),
                    rs.getInt("Total_Sold"),
                    rs.getString("Balance_Status")
                );
            }
            
        } catch (SQLException e) {
            System.err.println("❌ Lỗi kiểm tra: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
