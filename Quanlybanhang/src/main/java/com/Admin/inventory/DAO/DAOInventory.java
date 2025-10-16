package com.Admin.inventory.DAO;

import com.Admin.inventory.DTO.DTOInventory;
import com.Admin.inventory.DTO.DTOImportBill;
import com.Admin.inventory.DTO.DTOImportBillDetails;
import com.ComponentandDatabase.Database_Connection.DatabaseConnection;
import com.ComponentandDatabase.Components.CustomDialog;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

public class DAOInventory {
    
    // Lấy tất cả sản phẩm trong kho (Product_Stock) với thông tin tồn kho
    public List<DTOInventory> getAllInventory() {
        List<DTOInventory> inventoryList = new ArrayList<>();
        
        String sql = """
            SELECT ps.Warehouse_Item_ID, ps.Product_Name, ps.Unit_Price_Import, 
                   ps.Category_ID, ps.Sup_ID, ps.Quantity_Stock, 
                   ps.Created_Date, ps.Created_Time, ps.Is_In_Product
            FROM Product_Stock ps
            ORDER BY ps.Created_Date DESC, ps.Created_Time DESC
            """;
        
        System.out.println("🔍 Loading inventory data...");
        
        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            
            while (rs.next()) {
                DTOInventory inventory = new DTOInventory();
                inventory.setWarehouseItemId(rs.getString("Warehouse_Item_ID"));
                inventory.setProductName(rs.getString("Product_Name"));
                inventory.setUnitPriceImport(rs.getBigDecimal("Unit_Price_Import"));
                inventory.setCategoryId(rs.getString("Category_ID"));
                inventory.setBrandId(rs.getString("Sup_ID"));
                inventory.setQuantityInStock(rs.getInt("Quantity_Stock"));
                inventory.setColor(null);
                inventory.setSpeed(null);
                inventory.setBatteryCapacity(null);
                inventory.setCreatedDate(rs.getDate("Created_Date") != null ? 
                                       rs.getDate("Created_Date").toLocalDate() : null);
                inventory.setCreatedTime(rs.getTime("Created_Time") != null ? 
                                       rs.getTime("Created_Time").toLocalTime() : null);
                inventory.setInProduct(rs.getBoolean("Is_In_Product"));
                
                inventoryList.add(inventory);
                System.out.println("📦 Found inventory item: " + inventory.getWarehouseItemId() + " - " + inventory.getProductName());
            }
        } catch (SQLException e) {
            e.printStackTrace();
            CustomDialog.showError("Error loading inventory data!");
        }
        
        return inventoryList;
    }
    
    // Tìm kiếm sản phẩm trong kho (Product_Stock)
    public List<DTOInventory> searchInventory(String searchType, String keyword) {
        List<DTOInventory> inventoryList = new ArrayList<>();
        
        String baseSql = """
            SELECT ps.Warehouse_Item_ID, ps.Product_Name, ps.Unit_Price_Import, 
                   ps.Category_ID, ps.Sup_ID, ps.Quantity_Stock, 
                   ps.Created_Date, ps.Created_Time, ps.Is_In_Product
            FROM Product_Stock ps
            """;
        
        String whereClause = "";
        switch (searchType) {
            case "Product.ID":
                whereClause = "WHERE ps.Warehouse_Item_ID LIKE ?";
                break;
            case "Product Name":
                whereClause = "WHERE ps.Product_Name LIKE ?";
                break;
            case "Brand.ID":
                whereClause = "WHERE ps.Sup_ID LIKE ?";
                break;
            default:
                whereClause = "WHERE ps.Warehouse_Item_ID LIKE ? OR ps.Product_Name LIKE ? OR ps.Sup_ID LIKE ?";
        }
        
        String sql = baseSql + whereClause + " ORDER BY ps.Created_Date DESC, ps.Created_Time DESC";
        
        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            if (searchType.equals("Product.ID") || searchType.equals("Product Name") || searchType.equals("Brand.ID")) {
                pstmt.setString(1, "%" + keyword + "%");
            } else {
                pstmt.setString(1, "%" + keyword + "%");
                pstmt.setString(2, "%" + keyword + "%");
                pstmt.setString(3, "%" + keyword + "%");
            }
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    DTOInventory inventory = new DTOInventory();
                    inventory.setWarehouseItemId(rs.getString("Warehouse_Item_ID"));
                    inventory.setProductName(rs.getString("Product_Name"));
                    inventory.setUnitPriceImport(rs.getBigDecimal("Unit_Price_Import"));
                    inventory.setCategoryId(rs.getString("Category_ID"));
                    inventory.setBrandId(rs.getString("Sup_ID"));
                    inventory.setQuantityInStock(rs.getInt("Quantity_Stock"));
                    // Color, Speed, Battery_Capacity không có trong database mới - set null
                    inventory.setColor(null);
                    inventory.setSpeed(null);
                    inventory.setBatteryCapacity(null);
                    inventory.setCreatedDate(rs.getDate("Created_Date") != null ? 
                                           rs.getDate("Created_Date").toLocalDate() : null);
                    inventory.setCreatedTime(rs.getTime("Created_Time") != null ? 
                                           rs.getTime("Created_Time").toLocalTime() : null);
                    inventory.setInProduct(rs.getBoolean("Is_In_Product"));
                    
                    inventoryList.add(inventory);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            CustomDialog.showError("Error searching inventory!");
        }
        
        return inventoryList;
    }
    
    // Tạo hóa đơn nhập mới
    public boolean createImportBill(DTOImportBill bill) {
        String sql = "INSERT INTO Bill_Imported (Invoice_No, Admin_ID, Total_Product, Total_Price) VALUES (?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, bill.getInvoiceNo());
            pstmt.setString(2, bill.getAdminId());
            pstmt.setInt(3, bill.getTotalProduct());
            pstmt.setBigDecimal(4, bill.getTotalPrice());
            
            int rowsInserted = pstmt.executeUpdate();
            return rowsInserted > 0;
            
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    // Thêm chi tiết hóa đơn nhập
    public boolean addImportBillDetails(DTOImportBillDetails detail) {
        String sql = """
            INSERT INTO Bill_Imported_Details 
            (Invoice_No, Admin_ID, Warehouse_Item_ID, Quantity, Unit_Price_Import, Total_Price, Date_Imported, Time_Imported) 
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            """;
        
        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, detail.getInvoiceNo());
            pstmt.setString(2, detail.getAdminId());
            pstmt.setString(3, detail.getWarehouseItemId());  // Changed to Warehouse_Item_ID
            pstmt.setInt(4, detail.getQuantity());
            pstmt.setBigDecimal(5, detail.getUnitPrice());
            pstmt.setBigDecimal(6, detail.getTotalPrice());
            pstmt.setDate(7, Date.valueOf(detail.getDateImported()));
            pstmt.setTime(8, Time.valueOf(detail.getTimeImported()));
            
            int rowsInserted = pstmt.executeUpdate();
            return rowsInserted > 0;
            
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    // Lấy tất cả hóa đơn nhập
    public List<DTOImportBill> getAllImportBills() {
        List<DTOImportBill> bills = new ArrayList<>();
        
        String sql = """
            SELECT bi.Invoice_No, bi.Admin_ID, a.Admin_Name, bi.Total_Product, bi.Total_Price,
                   bid.Date_Imported, bid.Time_Imported
            FROM Bill_Imported bi
            JOIN Admin a ON bi.Admin_ID = a.Admin_ID
            JOIN Bill_Imported_Details bid ON bi.Invoice_No = bid.Invoice_No AND bi.Admin_ID = bid.Admin_ID
            GROUP BY bi.Invoice_No, bi.Admin_ID, a.Admin_Name, bi.Total_Product, bi.Total_Price,
                     bid.Date_Imported, bid.Time_Imported
            ORDER BY bid.Date_Imported DESC, bid.Time_Imported DESC
            """;
        
        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            
            while (rs.next()) {
                DTOImportBill bill = new DTOImportBill();
                bill.setInvoiceNo(rs.getString("Invoice_No"));
                bill.setAdminId(rs.getString("Admin_ID"));
                bill.setAdminName(rs.getString("Admin_Name"));
                bill.setTotalProduct(rs.getInt("Total_Product"));
                bill.setTotalPrice(rs.getBigDecimal("Total_Price"));
                bill.setDateImported(rs.getDate("Date_Imported").toLocalDate());
                bill.setTimeImported(rs.getTime("Time_Imported").toLocalTime());
                
                bills.add(bill);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return bills;
    }
    
    // Lấy chi tiết hóa đơn nhập theo mã hóa đơn
    public List<DTOImportBillDetails> getImportBillDetails(String invoiceNo) {
        List<DTOImportBillDetails> details = new ArrayList<>();
        
        String sql = """
            SELECT bid.Invoice_No, bid.Admin_ID, bid.Warehouse_Item_ID, ps.Product_Name,
                   bid.Quantity, bid.Unit_Price_Import, bid.Total_Price,
                   bid.Date_Imported, bid.Time_Imported
            FROM Bill_Imported_Details bid
            JOIN Product_Stock ps ON bid.Warehouse_Item_ID = ps.Warehouse_Item_ID
            WHERE bid.Invoice_No = ?
            ORDER BY bid.Warehouse_Item_ID
            """;
        
        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, invoiceNo);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    DTOImportBillDetails detail = new DTOImportBillDetails();
                    detail.setInvoiceNo(rs.getString("Invoice_No"));
                    detail.setAdminId(rs.getString("Admin_ID"));
                    detail.setWarehouseItemId(rs.getString("Warehouse_Item_ID"));  // Changed
                    detail.setProductName(rs.getString("Product_Name"));
                    detail.setQuantity(rs.getInt("Quantity"));
                    detail.setUnitPrice(rs.getBigDecimal("Unit_Price_Import"));
                    detail.setTotalPrice(rs.getBigDecimal("Total_Price"));
                    detail.setDateImported(rs.getDate("Date_Imported").toLocalDate());
                    detail.setTimeImported(rs.getTime("Time_Imported").toLocalTime());
                    
                    details.add(detail);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return details;
    }
    
    /**
     * Nhập sản phẩm mới vào kho (Product_Stock) - KHÔNG tạo Product
     * Workflow mới: Import vào kho → Sau đó Admin thêm vào Product catalog với giá bán
     */
    public boolean importNewWarehouseItem(String warehouseItemId, String productName,
                                         String categoryId, String supId, 
                                         String color, String speed, String batteryCapacity,
                                         int quantity, BigDecimal unitPriceImport, 
                                         String adminId) {
        
        String insertStockSql = """
            INSERT INTO Product_Stock 
            (Warehouse_Item_ID, Product_Name, Category_ID, Sup_ID, 
             Quantity_Stock, Unit_Price_Import, Created_Date, Created_Time, Is_In_Product) 
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, 0)
            """;
        
        String insertBillSql = "INSERT INTO Bill_Imported (Invoice_No, Admin_ID, Total_Product, Total_Price) VALUES (?, ?, ?, ?)";
        
        String insertBillDetailsSql = """
            INSERT INTO Bill_Imported_Details 
            (Invoice_No, Admin_ID, Warehouse_Item_ID, Quantity, Unit_Price_Import, Total_Price, Date_Imported, Time_Imported) 
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            """;
        
        String invoiceNo = "IMP" + System.currentTimeMillis();
        BigDecimal totalPrice = unitPriceImport.multiply(BigDecimal.valueOf(quantity));
        LocalDate currentDate = LocalDate.now();
        LocalTime currentTime = LocalTime.now();
        
        try (Connection conn = DatabaseConnection.connect()) {
            conn.setAutoCommit(false);
            
            try {
                // 1. Tạo warehouse item (Product_Stock) - Database mới không có Color, Speed, Battery
                try (PreparedStatement pstmt = conn.prepareStatement(insertStockSql)) {
                    pstmt.setString(1, warehouseItemId);
                    pstmt.setString(2, productName);
                    pstmt.setString(3, categoryId);
                    pstmt.setString(4, supId);
                    pstmt.setInt(5, 0); // Quantity_Stock starts at 0, will be updated by trigger
                    pstmt.setBigDecimal(6, unitPriceImport);
                    pstmt.setDate(7, Date.valueOf(currentDate));
                    pstmt.setTime(8, Time.valueOf(currentTime));
                    pstmt.executeUpdate();
                }
                
                // 2. Tạo hóa đơn nhập
                try (PreparedStatement pstmt = conn.prepareStatement(insertBillSql)) {
                    pstmt.setString(1, invoiceNo);
                    pstmt.setString(2, adminId);
                    pstmt.setInt(3, 1); // 1 warehouse item
                    pstmt.setBigDecimal(4, totalPrice);
                    pstmt.executeUpdate();
                }
                
                // 3. Thêm chi tiết hóa đơn (trigger sẽ tự động cập nhật Quantity_Stock)
                try (PreparedStatement pstmt = conn.prepareStatement(insertBillDetailsSql)) {
                    pstmt.setString(1, invoiceNo);
                    pstmt.setString(2, adminId);
                    pstmt.setString(3, warehouseItemId);
                    pstmt.setInt(4, quantity);
                    pstmt.setBigDecimal(5, unitPriceImport);
                    pstmt.setBigDecimal(6, totalPrice);
                    pstmt.setDate(7, Date.valueOf(currentDate));
                    pstmt.setTime(8, Time.valueOf(currentTime));
                    pstmt.executeUpdate();
                }
                
                conn.commit();
                System.out.println("✅ Warehouse item imported successfully: " + warehouseItemId);
                CustomDialog.showSuccess("Warehouse item imported successfully! Now you can add it to Product catalog.");
                return true;
                
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
            CustomDialog.showError("Error importing warehouse item: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Legacy method for compatibility - redirects to new method
     * @deprecated Use importNewWarehouseItem instead
     */
    @Deprecated
    public boolean importSimpleProduct(String productId, String productName, BigDecimal price, 
                                     String categoryId, String brandId, int quantity, 
                                     BigDecimal unitPrice, String adminId, String color, 
                                     String speed, String batteryCapacity) {
        return importNewWarehouseItem(productId, productName, categoryId, brandId, 
                                     color, speed, batteryCapacity, quantity, unitPrice, adminId);
    }
    
    /**
     * Nhập thêm số lượng cho warehouse item đã có
     * @param warehouseItemId ID của warehouse item
     */
    public boolean importExistingWarehouseItem(String warehouseItemId, int quantity, BigDecimal unitPrice, String adminId) {
        String insertBillSql = "INSERT INTO Bill_Imported (Invoice_No, Admin_ID, Total_Product, Total_Price) VALUES (?, ?, ?, ?)";
        
        String insertBillDetailsSql = """
            INSERT INTO Bill_Imported_Details 
            (Invoice_No, Admin_ID, Warehouse_Item_ID, Quantity, Unit_Price_Import, Total_Price, Date_Imported, Time_Imported) 
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            """;
        
        String invoiceNo = "IMP" + System.currentTimeMillis();
        BigDecimal totalPrice = unitPrice.multiply(BigDecimal.valueOf(quantity));
        LocalDate currentDate = LocalDate.now();
        LocalTime currentTime = LocalTime.now();
        
        try (Connection conn = DatabaseConnection.connect()) {
            conn.setAutoCommit(false);
            
            try {
                // 1. Tạo hóa đơn nhập
                try (PreparedStatement pstmt = conn.prepareStatement(insertBillSql)) {
                    pstmt.setString(1, invoiceNo);
                    pstmt.setString(2, adminId);
                    pstmt.setInt(3, 1); // 1 warehouse item
                    pstmt.setBigDecimal(4, totalPrice);
                    pstmt.executeUpdate();
                }
                
                // 2. Thêm chi tiết hóa đơn (trigger sẽ tự động cập nhật Quantity_Stock)
                try (PreparedStatement pstmt = conn.prepareStatement(insertBillDetailsSql)) {
                    pstmt.setString(1, invoiceNo);
                    pstmt.setString(2, adminId);
                    pstmt.setString(3, warehouseItemId);
                    pstmt.setInt(4, quantity);
                    pstmt.setBigDecimal(5, unitPrice);
                    pstmt.setBigDecimal(6, totalPrice);
                    pstmt.setDate(7, Date.valueOf(currentDate));
                    pstmt.setTime(8, Time.valueOf(currentTime));
                    pstmt.executeUpdate();
                }
                
                conn.commit();
                CustomDialog.showSuccess("Stock updated successfully!");
                return true;
                
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
            CustomDialog.showError("Error updating stock: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Legacy method for compatibility
     * @deprecated Use importExistingWarehouseItem instead
     */
    @Deprecated
    public boolean importExistingProduct(String productId, int quantity, BigDecimal unitPrice, String adminId) {
        return importExistingWarehouseItem(productId, quantity, unitPrice, adminId);
    }
    
    // Xuất dữ liệu kho hàng ra Excel
    public boolean exportInventoryToExcel(String filePath) {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Warehouse Stock Report");
            
            // Tạo header
            String[] headers = {"Warehouse ID", "Product Name", "Import Price", "Category", "Brand", 
                              "Stock Qty", "Color", "Speed", "Battery", "In Product", "Created Date"};
            Row headerRow = sheet.createRow(0);
            
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setBorderBottom(BorderStyle.THIN);
            headerStyle.setBorderTop(BorderStyle.THIN);
            headerStyle.setBorderLeft(BorderStyle.THIN);
            headerStyle.setBorderRight(BorderStyle.THIN);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);
            
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }
            
            // Lấy dữ liệu và thêm vào sheet
            List<DTOInventory> inventoryList = getAllInventory();
            int rowNum = 1;
            
            for (DTOInventory inventory : inventoryList) {
                Row row = sheet.createRow(rowNum++);
                
                row.createCell(0).setCellValue(inventory.getWarehouseItemId());
                row.createCell(1).setCellValue(inventory.getProductName());
                row.createCell(2).setCellValue(inventory.getUnitPriceImport() != null ? 
                                              inventory.getUnitPriceImport().doubleValue() : 0.0);
                row.createCell(3).setCellValue(inventory.getCategoryId());
                row.createCell(4).setCellValue(inventory.getBrandId());
                row.createCell(5).setCellValue(inventory.getQuantityInStock());
                row.createCell(6).setCellValue(inventory.getColor() != null ? inventory.getColor() : "N/A");
                row.createCell(7).setCellValue(inventory.getSpeed() != null ? inventory.getSpeed() : "N/A");
                row.createCell(8).setCellValue(inventory.getBatteryCapacity() != null ? inventory.getBatteryCapacity() : "N/A");
                row.createCell(9).setCellValue(inventory.isInProduct() ? "Yes" : "No");
                row.createCell(10).setCellValue(inventory.getCreatedDate() != null ? 
                                               inventory.getCreatedDate().toString() : "");
            }
            
            // Tự động điều chỉnh độ rộng cột
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }
            
            // Ghi file
            try (FileOutputStream outputStream = new FileOutputStream(filePath)) {
                workbook.write(outputStream);
                CustomDialog.showSuccess("Warehouse stock exported to Excel successfully!");
                return true;
            }
            
        } catch (IOException e) {
            e.printStackTrace();
            CustomDialog.showError("Error exporting warehouse stock to Excel!");
            return false;
        }
    }
    
    // Xuất hóa đơn nhập ra Excel
    public boolean exportBillToExcel(String invoiceNo, String filePath) {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Bill Details - " + invoiceNo);
            
            // Tạo header
            String[] headers = {"Warehouse Item ID", "Product Name", "Quantity", "Unit Price", "Total Price", "Date", "Time"};
            Row headerRow = sheet.createRow(0);
            
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setBorderBottom(BorderStyle.THIN);
            headerStyle.setBorderTop(BorderStyle.THIN);
            headerStyle.setBorderLeft(BorderStyle.THIN);
            headerStyle.setBorderRight(BorderStyle.THIN);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);
            
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }
            
            // Lấy chi tiết hóa đơn
            List<DTOImportBillDetails> details = getImportBillDetails(invoiceNo);
            int rowNum = 1;
            
            for (DTOImportBillDetails detail : details) {
                Row row = sheet.createRow(rowNum++);
                
                row.createCell(0).setCellValue(detail.getWarehouseItemId());
                row.createCell(1).setCellValue(detail.getProductName());
                row.createCell(2).setCellValue(detail.getQuantity());
                row.createCell(3).setCellValue(detail.getUnitPrice().doubleValue());
                row.createCell(4).setCellValue(detail.getTotalPrice().doubleValue());
                row.createCell(5).setCellValue(detail.getDateImported().toString());
                row.createCell(6).setCellValue(detail.getTimeImported().toString());
            }
            
            // Tự động điều chỉnh độ rộng cột
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }
            
            // Ghi file
            try (FileOutputStream outputStream = new FileOutputStream(filePath)) {
                workbook.write(outputStream);
                CustomDialog.showSuccess("Bill exported to Excel successfully!");
                return true;
            }
            
        } catch (IOException e) {
            e.printStackTrace();
            CustomDialog.showError("Error exporting bill to Excel!");
            return false;
        }
    }
    
    /**
     * Nhập dữ liệu từ Excel - DEPRECATED
     * This method uses MySQL syntax which doesn't work with SQL Server
     * Consider implementing a proper batch import for warehouse items
     * @deprecated This functionality needs to be rewritten for the new schema
     */
    @Deprecated
    public boolean importInventoryFromExcel(File excelFile) {
        CustomDialog.showError("Excel import feature is being updated for the new warehouse system.");
        return false;
        
        /* TODO: Implement proper Excel import for Warehouse Stock
         * Expected format:
         * - Warehouse_Item_ID
         * - Product_Name
         * - Category_ID
         * - Sup_ID
         * - Quantity
         * - Unit_Price_Import
         * - Color, Speed, Battery_Capacity (optional)
         */
    }
    
    // Dọn dẹp tất cả dữ liệu kho hàng
    public boolean cleanAllInventory() {
        String sql = "DELETE FROM Product_Stock";
        
        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.executeUpdate();
            CustomDialog.showSuccess("All inventory data cleaned successfully!");
            return true;
            
        } catch (SQLException e) {
            e.printStackTrace();
            CustomDialog.showError("Error cleaning inventory data!");
            return false;
        }
    }
    
    // Xuất dữ liệu kho hàng ra PDF
    public boolean exportInventoryToPDF(String filePath) {
        try {
            // Tạo file PDF đơn giản bằng cách tạo HTML và convert
            List<DTOInventory> inventoryList = getAllInventory();
            
            StringBuilder htmlContent = new StringBuilder();
            htmlContent.append("<!DOCTYPE html>");
            htmlContent.append("<html><head><title>Inventory Report</title>");
            htmlContent.append("<style>");
            htmlContent.append("body { font-family: Arial, sans-serif; margin: 20px; }");
            htmlContent.append("table { border-collapse: collapse; width: 100%; }");
            htmlContent.append("th, td { border: 1px solid #ddd; padding: 8px; text-align: left; }");
            htmlContent.append("th { background-color: #f2f2f2; }");
            htmlContent.append("h1 { color: #333; }");
            htmlContent.append("</style></head><body>");
            htmlContent.append("<h1>Inventory Report</h1>");
            htmlContent.append("<p>Generated on: ").append(java.time.LocalDateTime.now()).append("</p>");
            htmlContent.append("<table>");
            htmlContent.append("<tr><th>Warehouse ID</th><th>Product Name</th><th>Category</th><th>Supplier</th><th>Import Price</th><th>Current Stock</th></tr>");
            
            for (DTOInventory inventory : inventoryList) {
                htmlContent.append("<tr>");
                htmlContent.append("<td>").append(inventory.getWarehouseItemId()).append("</td>");
                htmlContent.append("<td>").append(inventory.getProductName()).append("</td>");
                htmlContent.append("<td>").append(inventory.getCategoryId()).append("</td>");
                htmlContent.append("<td>").append(inventory.getBrandId()).append("</td>");
                htmlContent.append("<td>").append(inventory.getUnitPriceImport()).append("</td>");
                htmlContent.append("<td>").append(inventory.getQuantityInStock()).append("</td>");
                htmlContent.append("</tr>");
            }
            
            htmlContent.append("</table></body></html>");
            
            // Ghi file HTML
            String htmlFilePath = filePath.replace(".pdf", ".html");
            try (FileOutputStream fos = new FileOutputStream(htmlFilePath)) {
                fos.write(htmlContent.toString().getBytes("UTF-8"));
            }
            
            CustomDialog.showSuccess("Inventory report exported to HTML file:\n" + htmlFilePath + "\n\nNote: PDF export requires additional libraries. HTML file created instead.");
            return true;
            
        } catch (Exception e) {
            e.printStackTrace();
            CustomDialog.showError("Error exporting inventory to PDF: " + e.getMessage());
            return false;
        }
    }
}
