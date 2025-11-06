package com.Admin.export.DAO;

import com.User.dashboard_user.DTO.DTOProfile_cus;
import com.Admin.export.DTO.DTO_BillExported;
import com.Admin.export.DTO.DTO_BillExportedDetail;
// import com.Admin.export.DTO.DTO_BillExport; // not used
// import com.Admin.product.DTO.DTOProduct; // not used
import com.Admin.promotion.BUS.BUSPromotion;
import com.Admin.promotion.DTO.DTOPromotion;
import com.ComponentandDatabase.Database_Connection.DatabaseConnection;

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

    // Insert Export Bill (header) with optional promotion code and VAT
    public boolean insertBillExported(DTO_BillExported bill, String promotionCode) {
        String sql = "INSERT INTO Bill_Exported (Invoice_No, Admin_ID, Customer_ID, Order_No, Total_Product, Promotion_Code, VAT_Percent, VAT_Amount, Total_Amount) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, bill.getInvoiceNo());
            pstmt.setString(2, bill.getAdminId());
            pstmt.setString(3, bill.getCustomerId());
            pstmt.setString(4, bill.getOrderNo()); // Thêm Order_No
            pstmt.setInt(5, bill.getTotalProduct());
            
            // Ưu tiên promotion code từ parameter, nếu null thì lấy từ DTO
            String promoCodeToInsert = promotionCode;
            if (promoCodeToInsert == null || promoCodeToInsert.trim().isEmpty()) {
                promoCodeToInsert = bill.getPromotionCode();
            }
            
            // Debug log
            System.out.println("=== DEBUG: DAO insertBillExported ===");
            System.out.println("Promotion Code from parameter: " + promotionCode);
            System.out.println("Promotion Code from DTO: " + bill.getPromotionCode());
            System.out.println("Promotion Code to insert: " + promoCodeToInsert);
            
            if (promoCodeToInsert == null || promoCodeToInsert.trim().isEmpty()) {
                pstmt.setNull(6, Types.VARCHAR);
                System.out.println("Setting Promotion_Code to NULL");
            } else {
                pstmt.setString(6, promoCodeToInsert.trim());
                System.out.println("Setting Promotion_Code to: " + promoCodeToInsert.trim());
            }
            
            // VAT fields
            if (bill.getVatPercent() != null) {
                pstmt.setBigDecimal(7, bill.getVatPercent());
            } else {
                pstmt.setBigDecimal(7, BigDecimal.valueOf(8.00)); // Default 8%
            }
            pstmt.setBigDecimal(8, bill.getVatAmount());
            pstmt.setBigDecimal(9, bill.getTotalAmount());
            
            int rowsAffected = pstmt.executeUpdate();
            System.out.println("Rows affected: " + rowsAffected);
            System.out.println("=== End DEBUG ===");
            
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error inserting bill exported: " + e.getMessage());
            System.err.println("SQL State: " + e.getSQLState());
            System.err.println("Error Code: " + e.getErrorCode());
            e.printStackTrace();
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

            // 4) Insert detail - Ưu tiên promotion code từ parameter, nếu null thì lấy từ detail
            String promoCodeForDetail = promotionCode;
            if (promoCodeForDetail == null || promoCodeForDetail.trim().isEmpty()) {
                promoCodeForDetail = detail.getPromotionCode();
            }
            
            String insertDetail = "INSERT INTO Bill_Exported_Details (Invoice_No, Admin_ID, Product_ID, "
                                + "Unit_Price_Sell_Before, Unit_Price_Sell_After, Sold_Quantity, Discount_Percent, Total_Price_Before, Total_Price_After, "
                                + "Date_Exported, Time_Exported, Start_Date, End_Date, Promotion_Code) "
                                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(insertDetail)) {
                ps.setString(1, detail.getInvoiceNo());
                ps.setString(2, detail.getAdminId());
                ps.setString(3, detail.getProductId());
                ps.setBigDecimal(4, detail.getUnitPriceBefore()); // Unit_Price_Sell_Before
                ps.setBigDecimal(5, detail.getUnitPrice()); // Unit_Price_Sell_After
                ps.setInt(6, detail.getQuantity());
                ps.setBigDecimal(7, percent);
                ps.setBigDecimal(8, totalBefore);
                ps.setBigDecimal(9, totalAfter);
                ps.setDate(10, detail.getDateExported());
                ps.setTime(11, detail.getTimeExported());
                ps.setDate(12, detail.getStartDate());
                ps.setDate(13, detail.getEndDate());
                // Set Promotion_Code
                if (promoCodeForDetail == null || promoCodeForDetail.trim().isEmpty()) {
                    ps.setNull(14, Types.VARCHAR);
                } else {
                    ps.setString(14, promoCodeForDetail.trim());
                }
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
        String sql =
            "SELECT d.Invoice_No, d.Admin_ID, b.Customer_ID, d.Product_ID, " +
            "       d.Unit_Price_Sell_Before, d.Unit_Price_Sell_After, d.Sold_Quantity, d.Discount_Percent, " +
            "       d.Total_Price_Before, d.Total_Price_After, d.Date_Exported, d.Time_Exported, " +
            "       d.Start_Date, d.End_Date, d.Promotion_Code " +
            "FROM Bill_Exported_Details d " +
            "JOIN Bill_Exported b ON b.Invoice_No = d.Invoice_No AND b.Admin_ID = d.Admin_ID " +
            "WHERE d.Status = 'Available'";
        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                DTO_BillExportedDetail d = new DTO_BillExportedDetail(
                    rs.getString("Invoice_No"),
                    rs.getString("Admin_ID"),
                    rs.getString("Customer_ID"),
                    rs.getString("Product_ID"),
                    rs.getBigDecimal("Unit_Price_Sell_Before"),
                    rs.getBigDecimal("Unit_Price_Sell_After"),
                    rs.getInt("Sold_Quantity"),
                    rs.getBigDecimal("Discount_Percent"),
                    rs.getBigDecimal("Total_Price_Before"),
                    rs.getBigDecimal("Total_Price_After"),
                    rs.getDate("Date_Exported"),
                    rs.getTime("Time_Exported"),
                    rs.getString("Promotion_Code"), // Promotion_Code
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
        String sql = "SELECT Invoice_No, Admin_ID, Customer_ID, Order_No, Total_Product, Description, VAT_Percent, VAT_Amount, Total_Amount FROM Bill_Exported WHERE Status = 'Available'";
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
                // Set Order_No from database
                dto.setOrderNo(rs.getString("Order_No"));
                // Set VAT fields
                dto.setVatPercent(rs.getBigDecimal("VAT_Percent"));
                dto.setVatAmount(rs.getBigDecimal("VAT_Amount"));
                dto.setTotalAmount(rs.getBigDecimal("Total_Amount"));
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
        String sql = "SELECT Invoice_No, Admin_ID, Customer_ID, Order_No, Total_Product, Description, Promotion_Code, VAT_Percent, VAT_Amount, Total_Amount FROM Bill_Exported WHERE Invoice_No = ? AND Admin_ID = ? AND Status = 'Available'";
        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, invoiceNo);
            ps.setString(2, adminID);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    DTO_BillExported bill = new DTO_BillExported(
                        rs.getString("Invoice_No"),
                        rs.getString("Admin_ID"),
                        rs.getString("Customer_ID"),
                        rs.getInt("Total_Product"),
                        rs.getString("Description"),
                        rs.getString("Promotion_Code")
                    );
                    bill.setOrderNo(rs.getString("Order_No"));
                    bill.setVatPercent(rs.getBigDecimal("VAT_Percent"));
                    bill.setVatAmount(rs.getBigDecimal("VAT_Amount"));
                    bill.setTotalAmount(rs.getBigDecimal("Total_Amount"));
                    return bill;
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
    
    /**
     * Search bill exported by criteria
     */
    public java.util.List<com.Admin.export.DTO.DTO_BillExport> searchBillExported(String searchType, String keyword) throws SQLException {
        java.util.List<com.Admin.export.DTO.DTO_BillExport> list = new java.util.ArrayList<>();
        String sql = "SELECT Invoice_No, Admin_ID, Customer_ID, Order_No, Total_Product, Description, VAT_Percent, VAT_Amount, Total_Amount FROM Bill_Exported WHERE Status = 'Available'";
        
        // Add search condition based on search type
        switch (searchType.toLowerCase()) {
            case "invoice no":
                sql += " AND Invoice_No LIKE ?";
                break;
            case "customer id":
                sql += " AND Customer_ID LIKE ?";
                break;
            case "admin id":
                sql += " AND Admin_ID LIKE ?";
                break;
            default:
                sql += " AND (Invoice_No LIKE ? OR Customer_ID LIKE ? OR Admin_ID LIKE ?)";
                break;
        }
        
        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            if (searchType.toLowerCase().equals("invoice no") || searchType.toLowerCase().equals("customer id") || searchType.toLowerCase().equals("admin id")) {
                ps.setString(1, "%" + keyword + "%");
            } else {
                ps.setString(1, "%" + keyword + "%");
                ps.setString(2, "%" + keyword + "%");
                ps.setString(3, "%" + keyword + "%");
            }
            
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    com.Admin.export.DTO.DTO_BillExport dto = new com.Admin.export.DTO.DTO_BillExport(
                        rs.getString("Invoice_No"),
                        rs.getString("Admin_ID"),
                        rs.getString("Customer_ID"),
                        rs.getInt("Total_Product"),
                        rs.getString("Description")
                    );
                    dto.setOrderNo(rs.getString("Order_No"));
                    dto.setVatPercent(rs.getBigDecimal("VAT_Percent"));
                    dto.setVatAmount(rs.getBigDecimal("VAT_Amount"));
                    dto.setTotalAmount(rs.getBigDecimal("Total_Amount"));
                    list.add(dto);
                }
            }
        }
        return list;
    }
    
    /**
     * Get bill details by invoice number
     */
    public java.util.List<DTO_BillExportedDetail> getBillDetailsByInvoice(String invoiceNo) throws SQLException {
        java.util.List<DTO_BillExportedDetail> ls = new java.util.ArrayList<>();
        String sql = "SELECT d.Invoice_No, d.Admin_ID, b.Customer_ID, d.Product_ID, " +
                    "       d.Unit_Price_Sell_Before, d.Unit_Price_Sell_After, d.Sold_Quantity, d.Discount_Percent, " +
                    "       d.Total_Price_Before, d.Total_Price_After, d.Date_Exported, d.Time_Exported, " +
                    "       d.Start_Date, d.End_Date, d.Promotion_Code " +
                    "FROM Bill_Exported_Details d " +
                    "JOIN Bill_Exported b ON b.Invoice_No = d.Invoice_No AND b.Admin_ID = d.Admin_ID " +
                    "WHERE d.Invoice_No = ? AND d.Status = 'Available'";
        
        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, invoiceNo);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    DTO_BillExportedDetail d = new DTO_BillExportedDetail(
                        rs.getString("Invoice_No"),
                        rs.getString("Admin_ID"),
                        rs.getString("Customer_ID"),
                        rs.getString("Product_ID"),
                        rs.getBigDecimal("Unit_Price_Sell_Before"),
                        rs.getBigDecimal("Unit_Price_Sell_After"),
                        rs.getInt("Sold_Quantity"),
                        rs.getBigDecimal("Discount_Percent"),
                        rs.getBigDecimal("Total_Price_Before"),
                        rs.getBigDecimal("Total_Price_After"),
                        rs.getDate("Date_Exported"),
                        rs.getTime("Time_Exported"),
                        rs.getString("Promotion_Code"), // Promotion_Code
                        rs.getDate("Start_Date"),
                        rs.getDate("End_Date")
                    );
                    ls.add(d);
                }
            }
        }
        return ls;
    }
}