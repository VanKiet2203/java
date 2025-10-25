package com.Admin.export.DAO;

import com.User.dashboard_user.DTO.DTOProfile_cus;
import com.Admin.export.DTO.DTO_BillExported;
import com.Admin.export.DTO.DTO_BillExportedDetail;
// import com.Admin.export.DTO.DTO_BillExport; // not used
// import com.Admin.product.DTO.DTOProduct; // not used
import com.Admin.promotion.BUS.BUSPromotion;
import com.Admin.promotion.DTO.DTOPromotion;
import com.ComponentandDatabase.Database_Connection.DatabaseConnection;
import com.Admin.export.DTO.DTO_WarrantyInfo;

import java.math.BigDecimal;
import java.sql.*;
import java.util.List;
import java.util.ArrayList;
// import java.time.LocalDate; // not used

public class DAO_ExportBill {

    // Existing methods omitted for brevity

    public String getWarranty(String productID) {
        String sql = "SELECT Warranty_Months FROM Product WHERE Product_ID = ? AND Status = 'Available'";
        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, productID);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                int warrantyMonths = rs.getInt("Warranty_Months");
                return warrantyMonths + " tháng";
            }
        } catch (SQLException e) {
            System.err.println("Error getting warranty: " + e.getMessage());
        }
        return "12 tháng"; // Default warranty
    }
    
    public int getWarrantyMonths(String productID) {
        String sql = "SELECT Warranty_Months FROM Product WHERE Product_ID = ? AND Status = 'Available'";
        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, productID);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("Warranty_Months");
            }
        } catch (SQLException e) {
            System.err.println("Error getting warranty months: " + e.getMessage());
        }
        return 12; // Default warranty months
    }

    // Insert Export Bill (header) with optional promotion code
    public boolean insertBillExported(DTO_BillExported bill, String promotionCode) {
        String sql = "INSERT INTO Bill_Exported (Invoice_No, Admin_ID, Customer_ID, Order_No, Total_Product, Promotion_Code) "
                   + "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, bill.getInvoiceNo());
            pstmt.setString(2, bill.getAdminId());
            pstmt.setString(3, bill.getCustomerId());
            pstmt.setString(4, bill.getOrderNo()); // Thêm Order_No
            pstmt.setInt(5, bill.getTotalProduct());
            if (promotionCode == null || promotionCode.isBlank()) pstmt.setNull(6, Types.VARCHAR);
            else pstmt.setString(6, promotionCode);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error inserting bill exported: " + e.getMessage());
            return false;
        }
    }

    // Insert Export Bill Details (no IMEI), update stock, compute totals with promotion percent
    public boolean insertBillExportedDetail(DTO_BillExportedDetail detail, String promotionCode) {
        if (detail == null) return false;
        if (detail.getQuantity() <= 0) return false;

        try (Connection conn = DatabaseConnection.connect()) {
            conn.setAutoCommit(false);

            // 1) Validate stock using stored procedure
            String stockValidationSql = "EXEC sp_ValidateStockBeforeExport ?, ?";
            try (PreparedStatement ps = conn.prepareStatement(stockValidationSql)) {
                ps.setString(1, detail.getProductId());
                ps.setInt(2, detail.getQuantity());
                ResultSet rs = ps.executeQuery();
                if (!rs.next()) { conn.rollback(); return false; }
                
                boolean isValid = rs.getBoolean("IsValid");
                String result = rs.getString("Result");
                
                if (!isValid || !"SUCCESS".equals(result)) {
                    System.err.println("Insufficient stock for product: " + detail.getProductId() + 
                                     ", Requested: " + detail.getQuantity() + 
                                     ", Available: " + rs.getInt("ProductStock"));
                    conn.rollback(); 
                    return false; 
                }
            }

            // 2) Resolve promotion percent (if active)
            BigDecimal percent = BigDecimal.ZERO;
            if (promotionCode != null && !promotionCode.isBlank()) {
                try {
                    BUSPromotion bus = new BUSPromotion();
                    DTOPromotion p = bus.findActivePromotion(promotionCode);
                    if (p != null) percent = p.getDiscountPercent() == null ? BigDecimal.ZERO : p.getDiscountPercent();
                } catch (Exception ignore) {}
            }
            if (percent.compareTo(BigDecimal.ZERO) < 0) percent = BigDecimal.ZERO;

            // 3) Sử dụng giá trị đã tính từ GUI layer (không tính lại)
            // totalBefore và totalAfter đã được tính toán chính xác ở Form_Export.java
            BigDecimal totalBefore = detail.getTotalPriceBefore();
            BigDecimal totalAfter = detail.getTotalPriceAfter();

            // 4) Insert detail
            String insertDetail = "INSERT INTO Bill_Exported_Details (Invoice_No, Admin_ID, Product_ID, "
                                + "Unit_Price_Sell_After, Sold_Quantity, Discount_Percent, Total_Price_Before, Total_Price_After, "
                                + "Date_Exported, Time_Exported, Start_Date, End_Date) "
                                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(insertDetail)) {
                ps.setString(1, detail.getInvoiceNo());
                ps.setString(2, detail.getAdminId());
                ps.setString(3, detail.getProductId());
                ps.setBigDecimal(4, detail.getUnitPrice());
                ps.setInt(5, detail.getQuantity());
                ps.setBigDecimal(6, percent);
                ps.setBigDecimal(7, totalBefore);
                ps.setBigDecimal(8, totalAfter);
                ps.setDate(9, detail.getDateExported());
                ps.setTime(10, detail.getTimeExported());
                ps.setDate(11, detail.getStartDate());
                ps.setDate(12, detail.getEndDate());
                ps.executeUpdate();
            }

            // 5) Do NOT manually decrease Product.Quantity here. Warehouse stock will be updated by DB triggers on Bill_Exported_Details.
            // REMOVED: Không gọi sp_FixQuantityIssues ở đây để tránh trùng lặp với trigger

            conn.commit();
            return true;
        } catch (SQLException e) {
            System.err.println("SQL Error inserting bill exported details: " + e.getMessage());
            return false;
        }
    }

    public java.util.List<DTO_BillExportedDetail> getAllBillDetails() throws SQLException {
        java.util.List<DTO_BillExportedDetail> ls = new java.util.ArrayList<>();
        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement pstmt = conn.prepareStatement("SELECT * FROM Bill_Exported_Details WHERE Status = 'Available'");
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                DTO_BillExportedDetail d = new DTO_BillExportedDetail(
                    rs.getString("Invoice_No"),
                    rs.getString("Admin_ID"),
                    rs.getString("Customer_ID"),
                    rs.getString("Product_ID"),
                    rs.getBigDecimal("Unit_Price_Sell_After"),
                    rs.getInt("Sold_Quantity"),
                    rs.getBigDecimal("Discount_Percent"),
                    rs.getBigDecimal("Total_Price_Before"),
                    rs.getBigDecimal("Total_Price_After"),
                    rs.getDate("Date_Exported"),
                    rs.getTime("Time_Exported"),
                    rs.getDate("Start_Date"),
                    rs.getDate("End_Date")
                );
                ls.add(d);
            }
        }
        return ls;
    }

    // Minimal implementations to satisfy BUS usage
    public java.util.List<com.Admin.export.DTO.DTO_BillExport> getAllBillExported() throws SQLException {
        java.util.List<com.Admin.export.DTO.DTO_BillExport> list = new java.util.ArrayList<>();
        String sql = "SELECT Invoice_No, Admin_ID, Customer_ID, Order_No, Total_Product, Description FROM Bill_Exported WHERE Status = 'Available'";
        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                com.Admin.export.DTO.DTO_BillExport dto = new com.Admin.export.DTO.DTO_BillExport(
                    rs.getString("Invoice_No"),
                    rs.getString("Admin_ID"),
                    rs.getString("Customer_ID"),
                    rs.getInt("Total_Product"),
                    rs.getString("Description")
                );
                list.add(dto);
            }
        }
        return list;
    }

    public boolean exportToExcel(String filePath) { return false; }

    public java.util.List<DTO_BillExportedDetail> searchBillDetails(String searchType, String searchKeyword) {
        try {
            return getAllBillDetails();
        } catch (SQLException e) {
            return java.util.Collections.emptyList();
        }
    }

    // Compatibility methods for existing BUS layer signatures
    public DTOProfile_cus getCustomerInfoByID(String customerID) throws SQLException {
        String sql = "SELECT Customer_ID, Full_Name, Address, Contact FROM Customer WHERE Customer_ID = ? AND Record_Status = 'Available'";
        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, customerID);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    DTOProfile_cus dto = new DTOProfile_cus();
                    dto.setCustomerID(rs.getString("Customer_ID"));
                    dto.setFullName(rs.getString("Full_Name"));
                    dto.setAddress(rs.getString("Address"));
                    dto.setContact(rs.getString("Contact"));
                    return dto;
                }
            }
        }
        return null;
    }

    public boolean insertBillExported(DTO_BillExported bill) {
        return insertBillExported(bill, null);
    }

    public boolean insertBillExportedDetail(DTO_BillExportedDetail detail, java.util.List<String> imeiList) {
        return insertBillExportedDetail(detail, (String) null);
    }

    public boolean updateProductQuantity(DTO_BillExportedDetail detail) throws SQLException {
        // No-op: Stock is handled by DB triggers after inserting Bill_Exported_Details
        return true;
    }
    
    // FIXED: Method để sửa lỗi số lượng ngay lập tức
    public boolean fixQuantityIssues() {
        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement stmt = conn.prepareStatement("EXEC sp_FixQuantityIssues")) {
            
            stmt.execute();
            System.out.println("✅ Đã sửa lỗi số lượng thành công!");
            return true;
            
        } catch (SQLException e) {
            System.err.println("❌ Lỗi sửa lỗi số lượng: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public DTO_BillExported getExportBillDetailsByInvoice(String invoiceNo, String adminID) throws SQLException {
        String sql = "SELECT Invoice_No, Admin_ID, Customer_ID, Order_No, Total_Product, Description, Promotion_Code FROM Bill_Exported WHERE Invoice_No = ? AND Admin_ID = ? AND Status = 'Available'";
        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, invoiceNo);
            ps.setString(2, adminID);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new DTO_BillExported(
                        rs.getString("Invoice_No"),
                        rs.getString("Admin_ID"),
                        rs.getString("Customer_ID"),
                        rs.getInt("Total_Product"),
                        rs.getString("Description"),
                        rs.getString("Promotion_Code")
                    );
                }
            }
        }
        return null;
    }
    
    /**
     * Get all exported bills that are available for insurance creation
     * Uses the view v_Available_Export_Bills_For_Insurance
     */
    public java.util.List<com.Admin.export.DTO.DTO_BillExport> getAllAvailableExportBillsForInsurance() throws SQLException {
        java.util.List<com.Admin.export.DTO.DTO_BillExport> list = new java.util.ArrayList<>();
        String sql = "SELECT DISTINCT bed.Invoice_No, bed.Admin_ID, bed.Customer_ID, " +
                    "COUNT(bed.Product_ID) as Total_Product, " +
                    "MIN(bed.Date_Exported) as Date_Exported " +
                    "FROM Bill_Exported_Details bed " +
                    "LEFT JOIN Insurance i ON bed.Invoice_No = i.Invoice_No AND bed.Admin_ID = i.Admin_ID " +
                    "WHERE bed.Status = 'Available' AND i.Insurance_No IS NULL " +
                    "GROUP BY bed.Invoice_No, bed.Admin_ID, bed.Customer_ID " +
                    "ORDER BY bed.Date_Exported DESC";
        
        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                com.Admin.export.DTO.DTO_BillExport dto = new com.Admin.export.DTO.DTO_BillExport(
                    rs.getString("Invoice_No"),
                    rs.getString("Admin_ID"),
                    rs.getString("Customer_ID"),
                    rs.getInt("Total_Product"),
                    "Available for Insurance"
                );
                list.add(dto);
            }
        }
        return list;
    }
    
    /**
     * Lấy danh sách thông tin bảo hành từ view v_Warranty_Information
     */
    public List<DTO_WarrantyInfo> getWarrantyInformation() {
        List<DTO_WarrantyInfo> warrantyList = new ArrayList<>();
        String sql = "SELECT * FROM v_Warranty_Information ORDER BY Date_Exported DESC";
        
        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            
            while (rs.next()) {
                DTO_WarrantyInfo warranty = new DTO_WarrantyInfo(
                    rs.getString("Invoice_No"),
                    rs.getString("Admin_ID"),
                    rs.getString("Customer_ID"),
                    rs.getString("Customer_Name"),
                    rs.getString("Product_ID"),
                    rs.getString("Product_Name"),
                    rs.getInt("Sold_Quantity"),
                    rs.getDate("Date_Exported"),
                    rs.getDate("Start_Date"),
                    rs.getDate("End_Date"),
                    rs.getString("Warranty_Status"),
                    rs.getInt("Warranty_Months")
                );
                warrantyList.add(warranty);
            }
        } catch (SQLException e) {
            System.err.println("Error getting warranty information: " + e.getMessage());
            e.printStackTrace();
        }
        
        return warrantyList;
    }
    
    
    /**
     * RESET và đồng bộ lại tất cả số lượng (sửa lỗi nhân đôi)
     */
    public boolean resetAndSyncAllQuantities() {
        String sql = "EXEC dbo.sp_FixQuantityIssues";
        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            
            System.out.println("=== RESET VÀ ĐỒNG BỘ SỐ LƯỢNG ===");
            
            // In kết quả kiểm tra
            while (rs.next()) {
                System.out.println("Info: " + rs.getString("Info") + 
                                 ", Product: " + rs.getString("Product_ID") + 
                                 ", Status: " + rs.getString("Balance_Status"));
            }
            
            System.out.println("✅ RESET và đồng bộ số lượng thành công!");
            return true;
        } catch (SQLException e) {
            System.err.println("❌ Error resetting and syncing quantities: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}