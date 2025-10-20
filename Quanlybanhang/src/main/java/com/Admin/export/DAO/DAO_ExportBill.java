package com.Admin.export.DAO;

import com.User.dashboard_user.DTO.DTOProfile_cus;
import com.Admin.export.DTO.DTO_BillExported;
import com.Admin.export.DTO.DTO_BillExportedDetail;
// import com.Admin.export.DTO.DTO_BillExport; // not used
// import com.Admin.product.DTO.DTOProduct; // not used
import com.Admin.promotion.BUS.BUSPromotion;
import com.Admin.promotion.DTO.DTOPromotion;
import com.ComponentandDatabase.Database_Connection.DatabaseConnection;

import java.util.ArrayList;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.*;
// import java.time.LocalDate; // not used
// import java.util.List; // not used

public class DAO_ExportBill {

    // Existing methods omitted for brevity

    public String getWarranty(String productID) { return "N/A"; }

    // Insert Export Bill (header) with optional promotion code
    public boolean insertBillExported(DTO_BillExported bill, String promotionCode) {
        String sql = "INSERT INTO Bill_Exported (Invoice_No, Admin_ID, Customer_ID, Total_Product, Promotion_Code) "
                   + "VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, bill.getInvoiceNo());
            pstmt.setString(2, bill.getAdminId());
            pstmt.setString(3, bill.getCustomerId());
            pstmt.setInt(4, bill.getTotalProduct());
            if (promotionCode == null || promotionCode.isBlank()) pstmt.setNull(5, Types.VARCHAR);
            else pstmt.setString(5, promotionCode);
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

            // 1) Validate stock against current warehouse stock (Product_Stock)
            String stockSql = "SELECT ISNULL(ps.Quantity_Stock, 0) AS Current_Stock "
                            + "FROM Product p "
                            + "LEFT JOIN Product_Stock ps ON ps.Warehouse_Item_ID = p.Warehouse_Item_ID "
                            + "WHERE p.Product_ID = ?";
            try (PreparedStatement ps = conn.prepareStatement(stockSql)) {
                ps.setString(1, detail.getProductId());
                ResultSet rs = ps.executeQuery();
                if (!rs.next()) { conn.rollback(); return false; }
                int onHand = rs.getInt("Current_Stock");
                if (onHand < detail.getQuantity()) { conn.rollback(); return false; }
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

            // 3) Compute totals
            BigDecimal qty = new BigDecimal(detail.getQuantity());
            BigDecimal totalBefore = detail.getUnitPrice().multiply(qty);
            BigDecimal totalAfter = totalBefore.multiply(BigDecimal.ONE.subtract(percent.divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP)));
            totalAfter = totalAfter.setScale(2, RoundingMode.HALF_UP);

            // 4) Insert detail
            String insertDetail = "INSERT INTO Bill_Exported_Details (Invoice_No, Admin_ID, Customer_ID, Product_ID, "
                                + "Unit_Price_Sell_After, Quantity, Discount_Percent, Total_Price_Before, Total_Price_After, "
                                + "Date_Exported, Time_Exported) "
                                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(insertDetail)) {
                ps.setString(1, detail.getInvoiceNo());
                ps.setString(2, detail.getAdminId());
                ps.setString(3, detail.getCustomerId());
                ps.setString(4, detail.getProductId());
                ps.setBigDecimal(5, detail.getUnitPrice());
                ps.setInt(6, detail.getQuantity());
                ps.setBigDecimal(7, percent);
                ps.setBigDecimal(8, totalBefore);
                ps.setBigDecimal(9, totalAfter);
                ps.setDate(10, detail.getDateExported());
                ps.setTime(11, detail.getTimeExported());
                ps.executeUpdate();
            }

            // 5) Do NOT manually decrease Product.Quantity here. Warehouse stock will be updated by DB triggers on Bill_Exported_Details.

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
             PreparedStatement pstmt = conn.prepareStatement("SELECT * FROM Bill_Exported_Details");
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                DTO_BillExportedDetail d = new DTO_BillExportedDetail(
                    rs.getString("Invoice_No"),
                    rs.getString("Admin_ID"),
                    rs.getString("Customer_ID"),
                    rs.getString("Product_ID"),
                    rs.getBigDecimal("Unit_Price_Sell_After"),
                    rs.getInt("Quantity"),
                    rs.getBigDecimal("Discount_Percent"),
                    rs.getBigDecimal("Total_Price_Before"),
                    rs.getBigDecimal("Total_Price_After"),
                    rs.getDate("Date_Exported"),
                    rs.getTime("Time_Exported")
                );
                ls.add(d);
            }
        }
        return ls;
    }

    // Minimal implementations to satisfy BUS usage
    public java.util.List<com.Admin.export.DTO.DTO_BillExport> getAllBillExported() throws SQLException {
        java.util.List<com.Admin.export.DTO.DTO_BillExport> list = new java.util.ArrayList<>();
        String sql = "SELECT Invoice_No, Admin_ID, Customer_ID, Total_Product, Description FROM Bill_Exported";
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
        String sql = "SELECT Customer_ID, Full_Name, Address, Contact FROM Customer WHERE Customer_ID = ?";
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

    public DTO_BillExported getExportBillDetailsByInvoice(String invoiceNo, String adminID) throws SQLException {
        String sql = "SELECT Invoice_No, Admin_ID, Customer_ID, Total_Product, Description, Promotion_Code FROM Bill_Exported WHERE Invoice_No = ? AND Admin_ID = ?";
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
}