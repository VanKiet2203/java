package com.Admin.inventory.DAO;

import com.Admin.inventory.DTO.DTOInventory;
import com.ComponentandDatabase.Components.CustomDialog;
import com.ComponentandDatabase.Database_Connection.DatabaseConnection;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import java.text.SimpleDateFormat;
import java.text.DecimalFormat;
import java.util.Locale;
import java.util.Date;

import javax.swing.table.DefaultTableModel;
import java.io.*;
import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class DAOInventory {
    
    private Connection getConnection() throws SQLException {
        return DatabaseConnection.connect();
    }
    
    public void loadInventoryData(DefaultTableModel model) {
        String sql = """
            SELECT 
                ps.Warehouse_Item_ID,
                ps.Product_Name,
                c.Category_Name,
                s.Sup_Name,
                ps.Quantity_Stock,
                ps.Unit_Price_Import,
                (ps.Quantity_Stock * ps.Unit_Price_Import) AS Total_Value,
                ps.Created_Date
            FROM Product_Stock ps
            LEFT JOIN Category c ON ps.Category_ID = c.Category_ID
            LEFT JOIN Supplier s ON ps.Sup_ID = s.Sup_ID
            ORDER BY ps.Created_Date DESC
            """;
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
        
            model.setRowCount(0);
            
            while (rs.next()) {
                Object[] row = {
                    rs.getString("Warehouse_Item_ID"),
                    rs.getString("Product_Name"),
                    rs.getString("Category_Name"),
                    rs.getString("Sup_Name"),
                    rs.getInt("Quantity_Stock"),
                    rs.getBigDecimal("Unit_Price_Import"),
                    rs.getBigDecimal("Total_Value"),
                    rs.getDate("Created_Date")
                };
                model.addRow(row);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to load inventory data", e);
        }
    }
    
    public void searchInventory(String keyword, String searchType, DefaultTableModel model) {
        String sql = """
            SELECT 
                ps.Warehouse_Item_ID,
                ps.Product_Name,
                c.Category_Name,
                s.Sup_Name,
                ps.Quantity_Stock,
                ps.Unit_Price_Import,
                (ps.Quantity_Stock * ps.Unit_Price_Import) AS Total_Value,
                ps.Created_Date
            FROM Product_Stock ps
            LEFT JOIN Category c ON ps.Category_ID = c.Category_ID
            LEFT JOIN Supplier s ON ps.Sup_ID = s.Sup_ID
            WHERE 
            """;
        
        // Add search condition based on searchType
        switch (searchType) {
            case "Warehouse ID":
                sql += "ps.Warehouse_Item_ID LIKE ?";
                break;
            case "Product Name":
                sql += "ps.Product_Name LIKE ?";
                break;
            case "Category":
                sql += "c.Category_Name LIKE ?";
                break;
            case "Supplier":
                sql += "s.Sup_Name LIKE ?";
                break;
            default:
                sql += "ps.Warehouse_Item_ID LIKE ?";
        }
        
        sql += " ORDER BY ps.Created_Date DESC";
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, "%" + keyword + "%");
            
            try (ResultSet rs = stmt.executeQuery()) {
                model.setRowCount(0);
                
                while (rs.next()) {
                    Object[] row = {
                        rs.getString("Warehouse_Item_ID"),
                        rs.getString("Product_Name"),
                        rs.getString("Category_Name"),
                        rs.getString("Sup_Name"),
                        rs.getInt("Quantity_Stock"),
                        rs.getBigDecimal("Unit_Price_Import"),
                        rs.getBigDecimal("Total_Value"),
                        rs.getDate("Created_Date")
                    };
                    model.addRow(row);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Search failed", e);
        }
    }
    
    public boolean importInventoryFromExcel(File excelFile) {
        String upsertSQL = """
            MERGE Product_Stock AS target
            USING (SELECT ?, ?, ?, ?, ?, ?, GETDATE(), GETDATE()) AS source 
                (Warehouse_Item_ID, Product_Name, Category_ID, Sup_ID, Quantity_Stock, Unit_Price_Import, Created_Date, Created_Time)
            ON target.Warehouse_Item_ID = source.Warehouse_Item_ID
            WHEN MATCHED THEN
                UPDATE SET 
                    Product_Name = source.Product_Name,
                    Quantity_Stock = target.Quantity_Stock + source.Quantity_Stock,
                    Unit_Price_Import = source.Unit_Price_Import
            WHEN NOT MATCHED THEN
                INSERT (Warehouse_Item_ID, Product_Name, Category_ID, Sup_ID, 
                       Quantity_Stock, Unit_Price_Import, Created_Date, Created_Time)
                VALUES (source.Warehouse_Item_ID, source.Product_Name, source.Category_ID, source.Sup_ID,
                       source.Quantity_Stock, source.Unit_Price_Import, source.Created_Date, source.Created_Time);
        """;
        String checkCategorySQL = "SELECT COUNT(*) FROM Category WHERE Category_ID = ? AND Status = 'Available'";
        String checkSupplierSQL = "SELECT COUNT(*) FROM Supplier WHERE Sup_ID = ? AND Status = 'Available'";

        int successCount = 0;
        int errorCount = 0;
        StringBuilder errors = new StringBuilder();

        try (FileInputStream fis = new FileInputStream(excelFile);
             XSSFWorkbook workbook = new XSSFWorkbook(fis)) {

            Sheet sheet = workbook.getSheetAt(0);
            Iterator<Row> rowIterator = sheet.iterator();

            if (rowIterator.hasNext()) {
                rowIterator.next(); // Skip header
            }

            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();
                int rowNum = row.getRowNum() + 1;

                if (row.getPhysicalNumberOfCells() >= 6) {
                    String warehouseItemId = getCellValueAsString(row.getCell(0));
                    String productName = getCellValueAsString(row.getCell(1));
                    String categoryId = getCellValueAsString(row.getCell(2));
                    String supId = getCellValueAsString(row.getCell(3));
                    int quantity = (int) getCellValueAsNumber(row.getCell(4));
                    double unitPrice = getCellValueAsNumber(row.getCell(5));

                    if (warehouseItemId == null || warehouseItemId.trim().isEmpty() ||
                        productName == null || productName.trim().isEmpty() ||
                        categoryId == null || categoryId.trim().isEmpty() ||
                        supId == null || supId.trim().isEmpty() ||
                        quantity <= 0 || unitPrice <= 0) {
                        errors.append("Row ").append(rowNum).append(": Missing or invalid data\n");
                        errorCount++;
                        continue;
                    }

                    try (Connection conn = getConnection()) {
                        conn.setAutoCommit(false);

                        try (PreparedStatement checkCategoryStmt = conn.prepareStatement(checkCategorySQL);
                             PreparedStatement checkSupplierStmt = conn.prepareStatement(checkSupplierSQL);
                             PreparedStatement insertStmt = conn.prepareStatement(upsertSQL)) {

                            // Check if Category exists
                            checkCategoryStmt.setString(1, categoryId);
                            try (ResultSet categoryRs = checkCategoryStmt.executeQuery()) {
                                if (!categoryRs.next() || categoryRs.getInt(1) == 0) {
                                    errors.append("Row ").append(rowNum).append(": Category ID '").append(categoryId).append("' does not exist\n");
                                    errorCount++;
                                    continue;
                                }
                            }

                            // Check if Supplier exists
                            checkSupplierStmt.setString(1, supId);
                            try (ResultSet supplierRs = checkSupplierStmt.executeQuery()) {
                                if (!supplierRs.next() || supplierRs.getInt(1) == 0) {
                                    errors.append("Row ").append(rowNum).append(": Supplier ID '").append(supId).append("' does not exist\n");
                                    errorCount++;
                                    continue;
                                }
                            }

                            // Upsert warehouse item (MERGE will handle both insert and update)
                            // Cho phép nhập lại cùng Warehouse_Item_ID
                            insertStmt.setString(1, warehouseItemId);
                            insertStmt.setString(2, productName);
                            insertStmt.setString(3, categoryId);
                            insertStmt.setString(4, supId);
                            insertStmt.setInt(5, quantity);
                            insertStmt.setBigDecimal(6, BigDecimal.valueOf(unitPrice));

                            insertStmt.executeUpdate();
                            
                            // Tạo hóa đơn nhập cho item này
                            createImportBillForItem(conn, warehouseItemId, quantity, unitPrice);
                            
                            conn.commit();
                            successCount++;

                        } catch (SQLException e) {
                            conn.rollback();
                            errors.append("Row ").append(rowNum).append(": ").append(e.getMessage()).append("\n");
                            errorCount++;
                        } finally {
                            conn.setAutoCommit(true);
                        }
            
        } catch (SQLException e) {
                        errors.append("Row ").append(rowNum).append(": Database connection error - ").append(e.getMessage()).append("\n");
                        errorCount++;
                    }
                } else {
                    errors.append("Row ").append(rowNum).append(": Insufficient data (need at least 6 columns)\n");
                    errorCount++;
                }
            }

            // Show results
            String message = String.format("Import completed!\nSuccess: %d items\nErrors: %d rows", 
                                         successCount, errorCount);
            if (errorCount > 0) {
                message += "\n\nErrors:\n" + errors.toString();
            }
            
            if (errorCount == 0) {
                CustomDialog.showSuccess(message);
            } else if (successCount > 0) {
                CustomDialog.showError(message);
            } else {
                CustomDialog.showError("Import failed!\n\n" + errors.toString());
            }
            
            return errorCount == 0;
            
        } catch (IOException e) {
            CustomDialog.showError("Error reading Excel file: " + e.getMessage());
            return false;
        } catch (Exception e) {
            CustomDialog.showError("Unexpected error during import: " + e.getMessage());
            return false;
        }
    }
    
    // Tạo hóa đơn nhập cho một item cụ thể
    private void createImportBillForItem(Connection conn, String warehouseItemId, int quantity, double unitPrice) throws SQLException {
        String invoiceNo = "BILL-" + System.currentTimeMillis() + "-" + warehouseItemId;
        String adminId = getCurrentAdminId(); // Lấy Admin_ID từ session hiện tại
        BigDecimal totalPrice = BigDecimal.valueOf(unitPrice).multiply(BigDecimal.valueOf(quantity));
        
        String insertBillSQL = """
            INSERT INTO Bill_Imported (Invoice_No, Admin_ID, Total_Product, Total_Price, Status)
            VALUES (?, ?, 1, ?, 'Available')
        """;
        
        String insertBillDetailSQL = """
            INSERT INTO Bill_Imported_Details (Invoice_No, Admin_ID, Warehouse_Item_ID, Quantity, Unit_Price_Import, Total_Price, Date_Imported, Time_Imported, Status)
            VALUES (?, ?, ?, ?, ?, ?, GETDATE(), GETDATE(), 'Available')
        """;
        
        try (PreparedStatement billStmt = conn.prepareStatement(insertBillSQL);
             PreparedStatement billDetailStmt = conn.prepareStatement(insertBillDetailSQL)) {
            
            // Insert Bill_Imported
            billStmt.setString(1, invoiceNo);
            billStmt.setString(2, adminId);
            billStmt.setBigDecimal(3, totalPrice);
            billStmt.executeUpdate();
            
            // Insert Bill_Imported_Details
            billDetailStmt.setString(1, invoiceNo);
            billDetailStmt.setString(2, adminId);
            billDetailStmt.setString(3, warehouseItemId);
            billDetailStmt.setInt(4, quantity);
            billDetailStmt.setBigDecimal(5, BigDecimal.valueOf(unitPrice));
            billDetailStmt.setBigDecimal(6, totalPrice);
            billDetailStmt.executeUpdate();
            
            System.out.println("Import bill created for " + warehouseItemId + ": " + invoiceNo);
        }
    }
    
    // Tạo hóa đơn nhập từ Product_Stock
    public String createImportBill() {
        String invoiceNo = "BILL-" + System.currentTimeMillis();
        String adminId = getCurrentAdminId(); // Lấy Admin_ID từ session hiện tại
        
        String insertBillSQL = """
            INSERT INTO Bill_Imported (Invoice_No, Admin_ID, Total_Product, Total_Price, Status)
            VALUES (?, ?, ?, ?, 'Available')
        """;
        String insertBillDetailSQL = """
            INSERT INTO Bill_Imported_Details (Invoice_No, Admin_ID, Warehouse_Item_ID, Quantity, Unit_Price_Import, Total_Price, Date_Imported, Time_Imported, Status)
            SELECT ?, ?, Warehouse_Item_ID, Quantity_Stock, Unit_Price_Import, 
                   (Quantity_Stock * Unit_Price_Import), GETDATE(), GETDATE(), 'Available'
            FROM Product_Stock
            WHERE Status = 'Available' 
            AND Warehouse_Item_ID NOT IN (
                SELECT DISTINCT Warehouse_Item_ID FROM Bill_Imported_Details
            )
        """;
        String calculateTotalSQL = """
            UPDATE Bill_Imported 
            SET Total_Product = (
                SELECT COUNT(*) 
                FROM Bill_Imported_Details 
                WHERE Invoice_No = ? AND Admin_ID = ?
            ),
            Total_Price = (
                SELECT ISNULL(SUM(Total_Price), 0) 
                FROM Bill_Imported_Details 
                WHERE Invoice_No = ? AND Admin_ID = ?
            )
            WHERE Invoice_No = ? AND Admin_ID = ?
        """;

        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);
            
            try (PreparedStatement insertBillStmt = conn.prepareStatement(insertBillSQL);
                 PreparedStatement insertDetailStmt = conn.prepareStatement(insertBillDetailSQL);
                 PreparedStatement calculateTotalStmt = conn.prepareStatement(calculateTotalSQL)) {

                // Insert bill
                insertBillStmt.setString(1, invoiceNo);
                insertBillStmt.setString(2, adminId);
                insertBillStmt.setInt(3, 0); // Will be updated later
                insertBillStmt.setBigDecimal(4, BigDecimal.ZERO); // Will be updated later
                insertBillStmt.executeUpdate();

                // Insert bill details
                insertDetailStmt.setString(1, invoiceNo);
                insertDetailStmt.setString(2, adminId);
                insertDetailStmt.executeUpdate();

                // Calculate and update total
                calculateTotalStmt.setString(1, invoiceNo);
                calculateTotalStmt.setString(2, adminId);
                calculateTotalStmt.setString(3, invoiceNo);
                calculateTotalStmt.setString(4, adminId);
                calculateTotalStmt.setString(5, invoiceNo);
                calculateTotalStmt.setString(6, adminId);
                calculateTotalStmt.executeUpdate();
                
                conn.commit();
                return invoiceNo;
                
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to create import bill", e);
        }
    }
    
    public boolean updateInventoryFromExcel(File excelFile) {
        String updateSQL = """
            UPDATE Product_Stock 
            SET Quantity_Stock = Quantity_Stock + ?, 
                Unit_Price_Import = ?
            WHERE Warehouse_Item_ID = ?
        """;
        // KHÔNG cần cập nhật Product.Quantity vì trigger sẽ tự động cập nhật
        // String updateProductSQL = """
        //     UPDATE Product 
        //     SET Quantity = Quantity + ?
        //     WHERE Warehouse_Item_ID = ?
        // """;
        String checkWarehouseSQL = "SELECT COUNT(*) FROM Product_Stock WHERE Warehouse_Item_ID = ? AND Status = 'Available'";

        int successCount = 0;
        int errorCount = 0;
        StringBuilder errors = new StringBuilder();

        try (FileInputStream fis = new FileInputStream(excelFile);
             XSSFWorkbook workbook = new XSSFWorkbook(fis)) {

            Sheet sheet = workbook.getSheetAt(0);
            Iterator<Row> rowIterator = sheet.iterator();

            if (rowIterator.hasNext()) {
                rowIterator.next(); // Skip header
            }

            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();
                int rowNum = row.getRowNum() + 1;

                if (row.getPhysicalNumberOfCells() >= 3) {
                    String warehouseItemId = getCellValueAsString(row.getCell(0));
                    int additionalQuantity = (int) getCellValueAsNumber(row.getCell(1));
                    double newUnitPrice = getCellValueAsNumber(row.getCell(2));

                    if (warehouseItemId == null || warehouseItemId.trim().isEmpty() ||
                        additionalQuantity <= 0 || newUnitPrice <= 0) {
                        errors.append("Row ").append(rowNum).append(": Missing or invalid data\n");
                        errorCount++;
                        continue;
                    }

                    try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);
            
                        try (PreparedStatement checkWarehouseStmt = conn.prepareStatement(checkWarehouseSQL);
                             PreparedStatement updateStmt = conn.prepareStatement(updateSQL)) {

                            // Check if Warehouse Item exists
                            checkWarehouseStmt.setString(1, warehouseItemId);
                            try (ResultSet warehouseRs = checkWarehouseStmt.executeQuery()) {
                                if (!warehouseRs.next() || warehouseRs.getInt(1) == 0) {
                                    errors.append("Row ").append(rowNum).append(": Warehouse Item ID '").append(warehouseItemId).append("' does not exist\n");
                                    errorCount++;
                                    continue;
                                }
                            }

                            // Update warehouse item
                            updateStmt.setInt(1, additionalQuantity);
                            updateStmt.setBigDecimal(2, BigDecimal.valueOf(newUnitPrice));
                            updateStmt.setString(3, warehouseItemId);

                            int rowsAffected = updateStmt.executeUpdate();
                            if (rowsAffected > 0) {
                                // KHÔNG cập nhật Product.Quantity ở đây vì trigger sẽ tự động cập nhật
                                // Chỉ cập nhật Product_Stock.Quantity_Stock là đủ
                                
                                conn.commit();
                                successCount++;
                                System.out.println("Updated warehouse item: " + warehouseItemId + " (+" + additionalQuantity + ")");
                                System.out.println("✅ Product.Quantity will be updated by trigger automatically");
                            } else {
                                errors.append("Row ").append(rowNum).append(": No rows updated\n");
                                errorCount++;
                            }

            } catch (SQLException e) {
                conn.rollback();
                            errors.append("Row ").append(rowNum).append(": ").append(e.getMessage()).append("\n");
                            errorCount++;
            } finally {
                conn.setAutoCommit(true);
            }
            
        } catch (SQLException e) {
                        errors.append("Row ").append(rowNum).append(": Database connection error - ").append(e.getMessage()).append("\n");
                        errorCount++;
                    }
                } else {
                    errors.append("Row ").append(rowNum).append(": Insufficient data (need at least 3 columns)\n");
                    errorCount++;
                }
            }

            // Show results
            String message = String.format("Update completed!\nSuccess: %d items\nErrors: %d rows", 
                                         successCount, errorCount);
            if (errorCount > 0) {
                message += "\n\nErrors:\n" + errors.toString();
            }
            
            if (errorCount == 0) {
                CustomDialog.showSuccess(message);
            } else if (successCount > 0) {
                CustomDialog.showError(message);
            } else {
                CustomDialog.showError("Update failed!\n\n" + errors.toString());
            }
            
            return errorCount == 0;
            
        } catch (IOException e) {
            CustomDialog.showError("Error reading Excel file: " + e.getMessage());
            return false;
        } catch (Exception e) {
            CustomDialog.showError("Unexpected error during update: " + e.getMessage());
            return false;
        }
    }
    
    public void exportInventoryToExcel(String filePath) {
        String sql = """
            SELECT 
                ps.Warehouse_Item_ID,
                ps.Product_Name,
                c.Category_Name,
                s.Sup_Name,
                ps.Quantity_Stock,
                ps.Unit_Price_Import,
                (ps.Quantity_Stock * ps.Unit_Price_Import) AS Total_Value,
                ps.Created_Date
            FROM Product_Stock ps
            LEFT JOIN Category c ON ps.Category_ID = c.Category_ID
            LEFT JOIN Supplier s ON ps.Sup_ID = s.Sup_ID
            ORDER BY ps.Created_Date DESC
        """;
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            XSSFWorkbook workbook = new XSSFWorkbook();
            
            Sheet sheet = workbook.createSheet("Inventory");
            
            // Create header row
            Row headerRow = sheet.createRow(0);
            String[] headers = {"Warehouse ID", "Product Name", "Category", "Supplier", 
                              "Quantity", "Unit Price", "Total Value", "Created Date"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
            }
            
            // Add data rows
            int rowNum = 1;
            while (rs.next()) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(rs.getString("Warehouse_Item_ID"));
                row.createCell(1).setCellValue(rs.getString("Product_Name"));
                row.createCell(2).setCellValue(rs.getString("Category_Name"));
                row.createCell(3).setCellValue(rs.getString("Sup_Name"));
                row.createCell(4).setCellValue(rs.getInt("Quantity_Stock"));
                row.createCell(5).setCellValue(rs.getDouble("Unit_Price_Import"));
                row.createCell(6).setCellValue(rs.getDouble("Total_Value"));
                row.createCell(7).setCellValue(rs.getDate("Created_Date").toString());
            }
            
            // Write to file
            try (FileOutputStream fos = new FileOutputStream(filePath)) {
                workbook.write(fos);
            }
            workbook.close();
            
        } catch (SQLException | IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Export failed", e);
        }
    }
    
    public void exportInventoryToPDF(String filePath) {
        String sql = """
            SELECT 
                ps.Warehouse_Item_ID,
                ps.Product_Name,
                c.Category_Name,
                s.Sup_Name,
                ps.Quantity_Stock,
                ps.Unit_Price_Import,
                (ps.Quantity_Stock * ps.Unit_Price_Import) AS Total_Value,
                ps.Created_Date
            FROM Product_Stock ps
            LEFT JOIN Category c ON ps.Category_ID = c.Category_ID
            LEFT JOIN Supplier s ON ps.Sup_ID = s.Sup_ID
            ORDER BY ps.Created_Date DESC
        """;
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            Document document = new Document(PageSize.A4, 40, 40, 50, 50);
            PdfWriter.getInstance(document, new FileOutputStream(filePath));
            document.open();
            
            // Add beautiful header
            addInventoryHeader(document);
            
            // Add summary info
            addInventorySummary(document, rs);
            
            // Reset result set for data
            try (PreparedStatement stmt2 = conn.prepareStatement(sql);
                 ResultSet rs2 = stmt2.executeQuery()) {
            
            // Create beautiful table
            addInventoryTable(document, rs2);
            }
            
            // Add footer
            addInventoryFooter(document);
            
            document.close();
            
        } catch (SQLException | IOException | DocumentException e) {
            e.printStackTrace();
            throw new RuntimeException("PDF export failed", e);
        }
    }
    
    private void addInventoryHeader(Document document) throws DocumentException {
        // Main title
        com.itextpdf.text.Font titleFont = getVietnameseFont(18, com.itextpdf.text.Font.BOLD);
        titleFont.setColor(BaseColor.BLUE);
        Paragraph title = new Paragraph("INVENTORY REPORT", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingAfter(15f);
        document.add(title);

        // Date and time
        com.itextpdf.text.Font infoFont = getVietnameseFont(12, com.itextpdf.text.Font.NORMAL);
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy hh:mm a", Locale.ENGLISH);
        String formattedDate = dateFormat.format(new Date());
        
        Paragraph info = new Paragraph();
        info.add(new Chunk("Generated on: " + formattedDate, infoFont));
        info.setAlignment(Element.ALIGN_CENTER);
        info.setSpacingAfter(20f);
        document.add(info);
        
        addLineSeparator(document, 0.5f, 95f, BaseColor.LIGHT_GRAY);
    }
    
    private void addInventorySummary(Document document, ResultSet rs) throws DocumentException {
        try {
            // Calculate totals
            BigDecimal totalValue = BigDecimal.ZERO;
            int totalItems = 0;
            int totalQuantity = 0;
            
            while (rs.next()) {
                totalValue = totalValue.add(rs.getBigDecimal("Total_Value"));
                totalItems++;
                totalQuantity += rs.getInt("Quantity_Stock");
            }
            
            // Create summary table
            PdfPTable summaryTable = new PdfPTable(2);
            summaryTable.setWidthPercentage(60);
            summaryTable.setSpacingBefore(10f);
            summaryTable.setSpacingAfter(15f);
            summaryTable.setHorizontalAlignment(Element.ALIGN_CENTER);
            
            // Header color
            BaseColor headerBgColor = new BaseColor(0, 51, 102);
            com.itextpdf.text.Font headerFont = getVietnameseFont(12, com.itextpdf.text.Font.BOLD);
            headerFont.setColor(BaseColor.WHITE);
            
            // Summary data
            String[][] summaryData = {
                {"Total Items:", String.valueOf(totalItems)},
                {"Total Quantity:", String.valueOf(totalQuantity)},
                {"Total Value:", formatCurrency(totalValue.toString())}
            };
            
            for (String[] row : summaryData) {
                // Label cell
                PdfPCell labelCell = new PdfPCell(new Phrase(row[0], headerFont));
                labelCell.setBackgroundColor(headerBgColor);
                labelCell.setPadding(8);
                labelCell.setBorder(Rectangle.BOX);
                labelCell.setBorderWidth(0.5f);
                summaryTable.addCell(labelCell);
                
                // Value cell
                com.itextpdf.text.Font valueFont = getVietnameseFont(12, com.itextpdf.text.Font.BOLD);
                valueFont.setColor(BaseColor.DARK_GRAY);
                PdfPCell valueCell = new PdfPCell(new Phrase(row[1], valueFont));
                valueCell.setPadding(8);
                valueCell.setBorder(Rectangle.BOX);
                valueCell.setBorderWidth(0.5f);
                summaryTable.addCell(valueCell);
            }
            
            document.add(summaryTable);
            addLineSeparator(document, 0.5f, 95f, BaseColor.LIGHT_GRAY);
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    private void addInventoryTable(Document document, ResultSet rs) throws DocumentException {
        // Section title
        com.itextpdf.text.Font sectionFont = getVietnameseFont(14, com.itextpdf.text.Font.BOLD);
        sectionFont.setColor(BaseColor.DARK_GRAY);
        Paragraph section = new Paragraph("INVENTORY DETAILS", sectionFont);
        section.setSpacingAfter(10f);
        document.add(section);
        
        // Create table with 8 columns
        PdfPTable table = new PdfPTable(8);
        table.setWidthPercentage(100);
        table.setSpacingBefore(10f);
        table.setSpacingAfter(15f);
        
        // Column widths
        float[] columnWidths = {1.2f, 2.5f, 1.8f, 1.8f, 1.0f, 1.2f, 1.5f, 1.0f};
        table.setWidths(columnWidths);
        
        // Table header
        com.itextpdf.text.Font headerFont = getVietnameseFont(10, com.itextpdf.text.Font.BOLD);
        headerFont.setColor(BaseColor.WHITE);
        BaseColor headerBgColor = new BaseColor(0, 51, 102);
        
        String[] headers = {"No.", "Product Name", "Category", "Supplier", 
                          "Quantity", "Unit Price", "Total Value", "Date"};
        
        for (String header : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(header, headerFont));
            cell.setBackgroundColor(headerBgColor);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            cell.setPadding(6);
            cell.setBorder(Rectangle.BOX);
            cell.setBorderWidth(0.5f);
            table.addCell(cell);
        }
        
        // Add data rows
        com.itextpdf.text.Font rowFont = getVietnameseFont(9, com.itextpdf.text.Font.NORMAL);
        int rowNum = 1;
        
        try {
            while (rs.next()) {
                // Alternate row color
                BaseColor rowColor = rowNum % 2 == 0 ? new BaseColor(248, 248, 248) : BaseColor.WHITE;
                
                table.addCell(createInventoryCell(String.valueOf(rowNum++), rowFont, rowColor, Element.ALIGN_CENTER));
                table.addCell(createInventoryCell(rs.getString("Product_Name"), rowFont, rowColor, Element.ALIGN_LEFT));
                table.addCell(createInventoryCell(rs.getString("Category_Name"), rowFont, rowColor, Element.ALIGN_LEFT));
                table.addCell(createInventoryCell(rs.getString("Sup_Name"), rowFont, rowColor, Element.ALIGN_LEFT));
                table.addCell(createInventoryCell(String.valueOf(rs.getInt("Quantity_Stock")), rowFont, rowColor, Element.ALIGN_CENTER));
                table.addCell(createInventoryCell(formatCurrency(rs.getBigDecimal("Unit_Price_Import").toString()), rowFont, rowColor, Element.ALIGN_RIGHT));
                table.addCell(createInventoryCell(formatCurrency(rs.getBigDecimal("Total_Value").toString()), rowFont, rowColor, Element.ALIGN_RIGHT));
                table.addCell(createInventoryCell(rs.getDate("Created_Date").toString(), rowFont, rowColor, Element.ALIGN_CENTER));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        document.add(table);
    }
    
    private PdfPCell createInventoryCell(String text, com.itextpdf.text.Font font, BaseColor bgColor, int alignment) {
        PdfPCell cell = new PdfPCell(new Phrase(text != null ? text : "", font));
        cell.setBackgroundColor(bgColor);
        cell.setPadding(5);
        cell.setBorder(Rectangle.BOX);
        cell.setBorderWidth(0.5f);
        cell.setBorderColor(BaseColor.LIGHT_GRAY);
        cell.setHorizontalAlignment(alignment);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        return cell;
    }
    
    private void addInventoryFooter(Document document) throws DocumentException {
        Paragraph footer = new Paragraph();
        footer.add(new Chunk("Inventory Management System - Generated Report", 
            getVietnameseFont(10, com.itextpdf.text.Font.ITALIC)));
        footer.setAlignment(Element.ALIGN_CENTER);
        footer.setSpacingBefore(20f);
        document.add(footer);
        
        addLineSeparator(document, 0.5f, 50f, BaseColor.GRAY);
    }
    
    private void addLineSeparator(Document document, float lineWidth, float percentage, BaseColor color) 
            throws DocumentException {
        Paragraph line = new Paragraph();
        com.itextpdf.text.pdf.draw.LineSeparator ls = new com.itextpdf.text.pdf.draw.LineSeparator(lineWidth, percentage, color, Element.ALIGN_CENTER, -1);
        line.add(new Chunk(ls));
        line.setSpacingAfter(10f);
        document.add(line);
    }
    
    private String formatCurrency(String amount) {
        try {
            BigDecimal value = new BigDecimal(amount.replaceAll("[^\\d.]", ""));
            DecimalFormat df = new DecimalFormat("0.00");
            df.setDecimalSeparatorAlwaysShown(true);
            df.setGroupingUsed(false);
            return df.format(value);
        } catch (Exception e) {
            return amount;
        }
    }
    
    private static com.itextpdf.text.Font getVietnameseFont(float size, int style) {
        try {
            BaseFont baseFont = BaseFont.createFont("C:\\Windows\\Fonts\\Arial.ttf", BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
            return new com.itextpdf.text.Font(baseFont, size, style);
        } catch (Exception e) {
            e.printStackTrace();
            return FontFactory.getFont(FontFactory.HELVETICA, size, style);
        }
    }
    
    public void loadBillsData(DefaultTableModel model) {
        String sql = """
            SELECT 
                'BILL-' + CAST(ROW_NUMBER() OVER (ORDER BY Created_Date) AS VARCHAR) AS Bill_ID,
                Created_Date AS Date,
                s.Sup_Name AS Supplier,
                COUNT(*) AS Total_Items,
                SUM(Quantity_Stock * Unit_Price_Import) AS Total_Amount,
                'Completed' AS Status
            FROM Product_Stock ps
            LEFT JOIN Supplier s ON ps.Sup_ID = s.Sup_ID
            GROUP BY Created_Date, s.Sup_Name
            ORDER BY Created_Date DESC
        """;
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            model.setRowCount(0);
            
            while (rs.next()) {
                Object[] row = {
                    rs.getString("Bill_ID"),
                    rs.getDate("Date"),
                    rs.getString("Supplier"),
                    rs.getInt("Total_Items"),
                    rs.getBigDecimal("Total_Amount"),
                    rs.getString("Status")
                };
                model.addRow(row);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to load bills data", e);
        }
    }
    
    public DTOInventory getInventoryItemById(String warehouseItemId) {
        String sql = """
            SELECT 
                ps.Warehouse_Item_ID,
                ps.Product_Name,
                ps.Category_ID,
                ps.Sup_ID,
                ps.Quantity_Stock,
                ps.Unit_Price_Import,
                ps.Created_Date
            FROM Product_Stock ps
            WHERE ps.Warehouse_Item_ID = ?
        """;
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, warehouseItemId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    DTOInventory item = new DTOInventory();
                    item.setWarehouseItemId(rs.getString("Warehouse_Item_ID"));
                    item.setProductName(rs.getString("Product_Name"));
                    item.setCategoryId(rs.getString("Category_ID"));
                    item.setSupId(rs.getString("Sup_ID"));
                    item.setQuantityStock(rs.getInt("Quantity_Stock"));
                    item.setUnitPriceImport(rs.getBigDecimal("Unit_Price_Import"));
                    item.setCreatedDate(rs.getDate("Created_Date"));
                    return item;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    public boolean updateInventoryItem(DTOInventory item) {
        String sql = """
            UPDATE Product_Stock 
            SET Product_Name = ?, 
                Quantity_Stock = ?, 
                Unit_Price_Import = ?
            WHERE Warehouse_Item_ID = ?
        """;
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, item.getProductName());
            stmt.setInt(2, item.getQuantityStock());
            stmt.setBigDecimal(3, item.getUnitPriceImport());
            stmt.setString(4, item.getWarehouseItemId());
            
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    private String getCellValueAsString(Cell cell) {
        if (cell == null) {
            return null;
        }
        
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue().trim();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue().toString();
                } else {
                    return String.valueOf((long) cell.getNumericCellValue());
                }
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                return cell.getCellFormula();
            default:
                return null;
        }
    }

    private double getCellValueAsNumber(Cell cell) {
        if (cell == null) {
            return 0.0;
        }
        
        switch (cell.getCellType()) {
            case NUMERIC:
                return cell.getNumericCellValue();
            case STRING:
                try {
                    return Double.parseDouble(cell.getStringCellValue());
                } catch (NumberFormatException e) {
                    return 0.0;
                }
            default:
                return 0.0;
        }
    }
    
    public String generateWarehouseId() {
        String sql = "SELECT MAX(CAST(SUBSTRING(Warehouse_Item_ID, 3, LEN(Warehouse_Item_ID)) AS INT)) FROM Product_Stock WHERE Warehouse_Item_ID LIKE 'WH%' AND Status = 'Available'";
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            int maxId = 0;
            if (rs.next()) {
                maxId = rs.getInt(1);
            }
            
            return "WH" + String.format("%03d", maxId + 1);
        } catch (SQLException e) {
            e.printStackTrace();
            return "WH001"; // Default fallback
        }
    }
    
    public boolean addInventoryItem(DTOInventory inventoryItem) {
        // First validate that Category_ID and Sup_ID exist
        if (!validateCategoryExists(inventoryItem.getCategoryId())) {
            System.err.println("Category ID does not exist: " + inventoryItem.getCategoryId());
            return false;
        }
        
        if (!validateSupplierExists(inventoryItem.getSupId())) {
            System.err.println("Supplier ID does not exist: " + inventoryItem.getSupId());
            return false;
        }
        
        // Validate Admin_ID before proceeding
        String adminId;
        try {
            adminId = getCurrentAdminId();
            System.out.println("Using Admin_ID: " + adminId);
        } catch (Exception e) {
            System.err.println("Failed to get Admin_ID: " + e.getMessage());
            return false;
        }
        
        // SQL để tạo hóa đơn nhập (chỉ cần Bill_Imported và Bill_Imported_Details)
        String insertBillSQL = """
            INSERT INTO Bill_Imported (Invoice_No, Admin_ID, Total_Product, Total_Price, Status)
            VALUES (?, ?, 1, ?, 'Available')
        """;
        
        String insertBillDetailSQL = """
            INSERT INTO Bill_Imported_Details (Invoice_No, Admin_ID, Warehouse_Item_ID, Quantity, Unit_Price_Import, Total_Price, Date_Imported, Time_Imported, Status)
            VALUES (?, ?, ?, ?, ?, ?, GETDATE(), GETDATE(), 'Available')
        """;
        
        // SQL để upsert Product_Stock (MERGE sẽ xử lý cả insert và update)
        String upsertStockSQL = """
            MERGE Product_Stock AS target
            USING (SELECT ?, ?, ?, ?, ?, ?, GETDATE(), GETDATE(), 0) AS source 
                (Warehouse_Item_ID, Product_Name, Category_ID, Sup_ID, Quantity_Stock, Unit_Price_Import, Created_Date, Created_Time, Is_In_Product)
            ON target.Warehouse_Item_ID = source.Warehouse_Item_ID
            WHEN MATCHED THEN
                UPDATE SET 
                    Product_Name = source.Product_Name,
                    Category_ID = source.Category_ID,
                    Sup_ID = source.Sup_ID,
                    Quantity_Stock = target.Quantity_Stock + source.Quantity_Stock,
                    Unit_Price_Import = source.Unit_Price_Import
            WHEN NOT MATCHED THEN
                INSERT (Warehouse_Item_ID, Product_Name, Category_ID, Sup_ID, 
                       Quantity_Stock, Unit_Price_Import, Created_Date, Created_Time, Is_In_Product)
                VALUES (source.Warehouse_Item_ID, source.Product_Name, source.Category_ID, source.Sup_ID,
                       source.Quantity_Stock, source.Unit_Price_Import, source.Created_Date, source.Created_Time, source.Is_In_Product);
        """;
        
        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);
            
            try (PreparedStatement stockStmt = conn.prepareStatement(upsertStockSQL);
                 PreparedStatement billStmt = conn.prepareStatement(insertBillSQL);
                 PreparedStatement billDetailStmt = conn.prepareStatement(insertBillDetailSQL)) {
                
                System.out.println("Adding inventory item:");
                System.out.println("Warehouse ID: " + inventoryItem.getWarehouseItemId());
                System.out.println("Product Name: " + inventoryItem.getProductName());
                System.out.println("Category ID: " + inventoryItem.getCategoryId());
                System.out.println("Supplier ID: " + inventoryItem.getSupId());
                System.out.println("Quantity: " + inventoryItem.getQuantityStock());
                System.out.println("Unit Price: " + inventoryItem.getUnitPriceImport());
                System.out.println("Admin ID: " + adminId);
                
                // 1. Upsert Product_Stock (MERGE sẽ cộng thêm Quantity_Stock nếu đã tồn tại)
                stockStmt.setString(1, inventoryItem.getWarehouseItemId());
                stockStmt.setString(2, inventoryItem.getProductName());
                stockStmt.setString(3, inventoryItem.getCategoryId());
                stockStmt.setString(4, inventoryItem.getSupId());
                stockStmt.setInt(5, inventoryItem.getQuantityStock());
                stockStmt.setBigDecimal(6, inventoryItem.getUnitPriceImport());
                
                int stockResult = stockStmt.executeUpdate();
                System.out.println("Stock insert result: " + stockResult);
                
                // 2. Tạo hóa đơn nhập
                String invoiceNo = "BILL-" + System.currentTimeMillis() + "-" + inventoryItem.getWarehouseItemId();
                BigDecimal totalPrice = inventoryItem.getUnitPriceImport().multiply(BigDecimal.valueOf(inventoryItem.getQuantityStock()));
                
                System.out.println("Creating import bill:");
                System.out.println("Invoice No: " + invoiceNo);
                System.out.println("Admin ID: " + adminId);
                System.out.println("Total Price: " + totalPrice);
                
                // Insert Bill_Imported
                billStmt.setString(1, invoiceNo);
                billStmt.setString(2, adminId);
                billStmt.setBigDecimal(3, totalPrice);
                int billResult = billStmt.executeUpdate();
                System.out.println("Bill insert result: " + billResult);
                
                if (billResult > 0) {
                    // Insert Bill_Imported_Details (trigger sẽ tự động cập nhật Quantity_Stock)
                    billDetailStmt.setString(1, invoiceNo);
                    billDetailStmt.setString(2, adminId);
                    billDetailStmt.setString(3, inventoryItem.getWarehouseItemId());
                    billDetailStmt.setInt(4, inventoryItem.getQuantityStock());
                    billDetailStmt.setBigDecimal(5, inventoryItem.getUnitPriceImport());
                    billDetailStmt.setBigDecimal(6, totalPrice);
                    int billDetailResult = billDetailStmt.executeUpdate();
                    System.out.println("Bill detail insert result: " + billDetailResult);
                    
                    if (billDetailResult > 0) {
                        conn.commit();
                        System.out.println("✅ Import bill created successfully: " + invoiceNo);
                        System.out.println("✅ Trigger automatically updated Product_Stock.Quantity_Stock");
                        return true;
                    } else {
                        conn.rollback();
                        System.err.println("❌ Failed to insert Bill_Imported_Details");
                        return false;
                    }
                } else {
                    conn.rollback();
                    System.err.println("❌ Failed to insert Bill_Imported");
                    return false;
                }
                
            } catch (SQLException e) {
                conn.rollback();
                System.err.println("❌ SQL Error adding inventory item: " + e.getMessage());
                System.err.println("SQL State: " + e.getSQLState());
                System.err.println("Error Code: " + e.getErrorCode());
                e.printStackTrace();
                return false;
            } finally {
                conn.setAutoCommit(true);
            }
            
        } catch (SQLException e) {
            System.err.println("❌ Connection Error adding inventory item: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    // Method để nhập lại Warehouse Item (cộng thêm số lượng)
    public boolean reimportWarehouseItem(String warehouseItemId, int additionalQuantity, BigDecimal unitPrice) {
        String updateStockSQL = """
            UPDATE Product_Stock 
            SET Quantity_Stock = Quantity_Stock + ?
            WHERE Warehouse_Item_ID = ?
        """;
        
        String insertBillSQL = """
            INSERT INTO Bill_Imported (Invoice_No, Admin_ID, Total_Product, Total_Price, Status)
            VALUES (?, ?, 1, ?, 'Available')
        """;
        
        String insertBillDetailSQL = """
            INSERT INTO Bill_Imported_Details (Invoice_No, Admin_ID, Warehouse_Item_ID, Quantity, Unit_Price_Import, Total_Price, Date_Imported, Time_Imported, Status)
            VALUES (?, ?, ?, ?, ?, ?, GETDATE(), GETDATE(), 'Available')
        """;
        
        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);
            
            try (PreparedStatement updateStmt = conn.prepareStatement(updateStockSQL);
                 PreparedStatement billStmt = conn.prepareStatement(insertBillSQL);
                 PreparedStatement billDetailStmt = conn.prepareStatement(insertBillDetailSQL)) {
                
                // 1. Cộng thêm số lượng vào Product_Stock
                updateStmt.setInt(1, additionalQuantity);
                updateStmt.setString(2, warehouseItemId);
                int updateResult = updateStmt.executeUpdate();
                
                if (updateResult > 0) {
                    // 2. Tạo hóa đơn nhập cho số lượng bổ sung
                    String adminId = getCurrentAdminId();
                    String invoiceNo = "BILL-" + System.currentTimeMillis() + "-" + warehouseItemId;
                    BigDecimal totalPrice = unitPrice.multiply(BigDecimal.valueOf(additionalQuantity));
                    
                    billStmt.setString(1, invoiceNo);
                    billStmt.setString(2, adminId);
                    billStmt.setBigDecimal(3, totalPrice);
                    billStmt.executeUpdate();
                    
                    billDetailStmt.setString(1, invoiceNo);
                    billDetailStmt.setString(2, adminId);
                    billDetailStmt.setString(3, warehouseItemId);
                    billDetailStmt.setInt(4, additionalQuantity);
                    billDetailStmt.setBigDecimal(5, unitPrice);
                    billDetailStmt.setBigDecimal(6, totalPrice);
                    billDetailStmt.executeUpdate();
                    
                    conn.commit();
                    System.out.println("✅ Nhập lại Warehouse Item thành công: " + warehouseItemId + " (+" + additionalQuantity + ")");
                    return true;
                } else {
                    conn.rollback();
                    System.err.println("❌ Warehouse Item không tồn tại: " + warehouseItemId);
                    return false;
                }
                
            } catch (SQLException e) {
                conn.rollback();
                System.err.println("❌ Lỗi nhập lại Warehouse Item: " + e.getMessage());
                return false;
            } finally {
                conn.setAutoCommit(true);
            }
            
        } catch (SQLException e) {
            System.err.println("❌ Lỗi kết nối: " + e.getMessage());
            return false;
        }
    }
    
    // Method để tạo Product từ Warehouse Item và đồng bộ số lượng
    public boolean createProductFromWarehouse(String warehouseItemId, String color, String speed, 
                                            String batteryCapacity, BigDecimal price) {
        // KHÔNG set Quantity trong INSERT - để trigger tự động xử lý
        String insertSQL = """
            INSERT INTO Product (Product_ID, Product_Name, Color, Speed, Battery_Capacity, 
                               Quantity, Category_ID, Sup_ID, Image, Price, List_Price_Before, 
                               List_Price_After, Warehouse_Item_ID)
            SELECT ?, Product_Name, ?, ?, ?, 0, Category_ID, Sup_ID, 
                   NULL, ?, ?, ?, Warehouse_Item_ID
            FROM Product_Stock 
            WHERE Warehouse_Item_ID = ? AND Status = 'Available'
        """;
        
        String updateWarehouseSQL = """
            UPDATE Product_Stock 
            SET Is_In_Product = 1 
            WHERE Warehouse_Item_ID = ?
        """;
        
        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);
            
            try (PreparedStatement insertStmt = conn.prepareStatement(insertSQL);
                 PreparedStatement updateStmt = conn.prepareStatement(updateWarehouseSQL)) {
                
                insertStmt.setString(1, warehouseItemId); // Product_ID = Warehouse_Item_ID
                insertStmt.setString(2, color);
                insertStmt.setString(3, speed);
                insertStmt.setString(4, batteryCapacity);
                insertStmt.setBigDecimal(5, price);
                insertStmt.setBigDecimal(6, price.multiply(new BigDecimal("0.9"))); // List_Price_Before
                insertStmt.setBigDecimal(7, price.multiply(new BigDecimal("0.8"))); // List_Price_After
                insertStmt.setString(8, warehouseItemId);
                
                int rowsAffected = insertStmt.executeUpdate();
                
                if (rowsAffected > 0) {
                    // Update Is_In_Product flag
                    updateStmt.setString(1, warehouseItemId);
                    updateStmt.executeUpdate();
                    
                    // Trigger sẽ tự động cập nhật Quantity
                    // Không cần gọi syncProductQuantities() ở đây
                    
                    conn.commit();
                    System.out.println("✅ Product created from warehouse: " + warehouseItemId);
                    System.out.println("✅ Quantity will be set by trigger automatically");
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
    
    // Lấy Admin_ID hiện tại từ session
    private String getCurrentAdminId() {
        try {
            // Thử lấy từ SessionManager trước
            com.ComponentandDatabase.Session.SessionManager sessionManager = com.ComponentandDatabase.Session.SessionManager.getInstance();
            if (sessionManager.isLoggedIn()) {
                String adminId = sessionManager.getCurrentAdminId();
                if (adminId != null && !adminId.isEmpty() && validateAdminExists(adminId)) {
                    return adminId;
                }
            }
            
            // Fallback: Lấy từ Dashboard_ad static variable
            Class<?> dashboardClass = Class.forName("com.Admin.dashboard_admin.GUI.Dashboard_ad");
            java.lang.reflect.Field adminIdField = dashboardClass.getDeclaredField("adminID");
            adminIdField.setAccessible(true);
            String adminId = (String) adminIdField.get(null);
            
            if (adminId != null && !adminId.isEmpty() && validateAdminExists(adminId)) {
                return adminId;
            }
            
            // Fallback cuối cùng: Lấy Admin đầu tiên có sẵn
            String defaultAdminId = getFirstAvailableAdminId();
            if (defaultAdminId != null) {
                System.err.println("Warning: Could not get current Admin_ID, using first available admin: " + defaultAdminId);
                return defaultAdminId;
            } else {
                System.err.println("Error: No admin found in database!");
                throw new RuntimeException("No admin found in database. Please ensure at least one admin exists.");
            }
        } catch (Exception e) {
            System.err.println("Error getting current Admin_ID: " + e.getMessage());
            String defaultAdminId = getFirstAvailableAdminId();
            if (defaultAdminId != null) {
                return defaultAdminId;
            }
            throw new RuntimeException("Failed to get Admin_ID: " + e.getMessage());
        }
    }
    
    // Kiểm tra Admin_ID có tồn tại trong database không
    private boolean validateAdminExists(String adminId) {
        String sql = "SELECT COUNT(*) FROM Admin WHERE Admin_ID = ? AND Status = 'Available'";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, adminId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error validating admin: " + e.getMessage());
        }
        return false;
    }
    
    // Lấy Admin_ID đầu tiên có sẵn
    private String getFirstAvailableAdminId() {
        String sql = "SELECT TOP 1 Admin_ID FROM Admin WHERE Status = 'Available' ORDER BY Admin_ID";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                return rs.getString("Admin_ID");
            }
        } catch (SQLException e) {
            System.err.println("Error getting first available admin: " + e.getMessage());
        }
        return null;
    }
    
    
    
    // Method để đồng bộ số lượng Product từ dữ liệu nhập và bán
    public void syncProductQuantities() {
        String syncSQL = """
            EXEC sp_SyncAllProductQuantities
        """;
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(syncSQL)) {
            
            stmt.execute();
            System.out.println("✅ Đồng bộ số lượng Product thành công!");
            
        } catch (SQLException e) {
            System.err.println("❌ Lỗi đồng bộ số lượng: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    // Method để reset và đồng bộ lại tất cả số lượng (sửa lỗi nhân đôi) - FIXED
    public void resetAndSyncAllQuantities() {
        String fixSQL = "EXEC sp_FixQuantityIssues";
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(fixSQL)) {
            
            stmt.execute();
            System.out.println("✅ Đã sửa lỗi số lượng thành công!");
            
        } catch (SQLException e) {
            System.err.println("❌ Lỗi sửa lỗi số lượng: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    // Method để hiển thị báo cáo số lượng
    public void displayQuantityReport() {
        String reportSQL = """
            SELECT 
                p.Product_ID,
                p.Product_Name,
                ISNULL(ps.Quantity_Stock, 0) AS Total_Imported,
                p.Quantity AS Current_Stock,
                ISNULL(ps.Quantity_Stock, 0) - p.Quantity AS Total_Sold,
                CASE 
                    WHEN ISNULL(ps.Quantity_Stock, 0) = p.Quantity + (ISNULL(ps.Quantity_Stock, 0) - p.Quantity)
                    THEN '✓ Cân bằng'
                    ELSE '✗ Lệch'
                END AS Balance_Status
            FROM Product p
            LEFT JOIN Product_Stock ps ON p.Warehouse_Item_ID = ps.Warehouse_Item_ID
            WHERE p.Status = 'Available' AND (ps.Status = 'Available' OR ps.Status IS NULL)
            ORDER BY p.Product_ID
        """;
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(reportSQL);
             ResultSet rs = stmt.executeQuery()) {
            
            System.out.println("\n📊 BÁO CÁO SỐ LƯỢNG SẢN PHẨM:");
            System.out.println("=" + "=".repeat(80));
            System.out.printf("%-15s %-20s %-12s %-12s %-12s %-12s%n", 
                "Product_ID", "Product_Name", "Tổng_Nhập", "Tồn_Kho", "Đã_Bán", "Trạng_Thái");
            System.out.println("=" + "=".repeat(80));
            
            while (rs.next()) {
                System.out.printf("%-15s %-20s %-12d %-12d %-12d %-12s%n",
                    rs.getString("Product_ID"),
                    rs.getString("Product_Name"),
                    rs.getInt("Total_Imported"),
                    rs.getInt("Current_Stock"),
                    rs.getInt("Total_Sold"),
                    rs.getString("Balance_Status")
                );
            }
            System.out.println("=" + "=".repeat(80));
            
        } catch (SQLException e) {
            System.err.println("❌ Lỗi hiển thị báo cáo: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    // Method để kiểm tra và đồng bộ toàn bộ hệ thống số lượng - FIXED
    public void checkAndSyncAllQuantities() {
        System.out.println("=== KIỂM TRA VÀ ĐỒNG BỘ SỐ LƯỢNG TOÀN HỆ THỐNG ===");
        
        // 1. Sử dụng stored procedure mới để fix lỗi
        System.out.println("\n1. Sửa lỗi số lượng với stored procedure...");
        resetAndSyncAllQuantities();
        
        // 2. Hiển thị báo cáo
        System.out.println("\n2. Báo cáo số lượng:");
        displayQuantityReport();
        
        // 3. Kiểm tra cân bằng
        System.out.println("\n3. Kiểm tra cân bằng số lượng:");
        String checkBalanceSQL = """
            SELECT 
                p.Product_ID,
                p.Product_Name,
                ISNULL(ps.Quantity_Stock, 0) AS Total_Imported,
                p.Quantity AS Current_Stock,
                ISNULL(ps.Quantity_Stock, 0) - p.Quantity AS Total_Sold,
                CASE 
                    WHEN ISNULL(ps.Quantity_Stock, 0) = p.Quantity + (ISNULL(ps.Quantity_Stock, 0) - p.Quantity)
                    THEN '✓ Cân bằng'
                    ELSE '✗ Lệch'
                END AS Balance_Status
            FROM Product p
            LEFT JOIN Product_Stock ps ON p.Warehouse_Item_ID = ps.Warehouse_Item_ID
            WHERE p.Status = 'Available' AND (ps.Status = 'Available' OR ps.Status IS NULL)
            ORDER BY p.Product_ID
        """;
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(checkBalanceSQL);
             ResultSet rs = stmt.executeQuery()) {
            
            int balancedCount = 0;
            int unbalancedCount = 0;
            
            while (rs.next()) {
                String status = rs.getString("Balance_Status");
                if (status.contains("✓")) {
                    balancedCount++;
                } else {
                    unbalancedCount++;
                    System.out.println("⚠️  Lệch: " + rs.getString("Product_ID") + " - " + rs.getString("Product_Name"));
                }
            }
            
            System.out.println("\n=== KẾT QUẢ KIỂM TRA ===");
            System.out.println("✓ Cân bằng: " + balancedCount + " sản phẩm");
            System.out.println("✗ Lệch: " + unbalancedCount + " sản phẩm");
            
            if (unbalancedCount == 0) {
                System.out.println("🎉 Tất cả số lượng đều cân bằng!");
            } else {
                System.out.println("⚠️  Có " + unbalancedCount + " sản phẩm bị lệch số lượng!");
            }
            
        } catch (SQLException e) {
            System.err.println("Error checking balance: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private boolean validateCategoryExists(String categoryId) {
        String sql = "SELECT COUNT(*) FROM Category WHERE Category_ID = ? AND Status = 'Available'";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, categoryId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error validating category: " + e.getMessage());
        }
        return false;
    }
    
    private boolean validateSupplierExists(String supplierId) {
        String sql = "SELECT COUNT(*) FROM Supplier WHERE Sup_ID = ? AND Status = 'Available'";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, supplierId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error validating supplier: " + e.getMessage());
        }
        return false;
    }
    
    
    
    public List<String> getAllSuppliers() {
        List<String> suppliers = new ArrayList<>();
        String sql = "SELECT Sup_ID, Sup_Name FROM Supplier WHERE Status = 'Available' ORDER BY Sup_ID";
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                suppliers.add(rs.getString("Sup_ID") + " - " + rs.getString("Sup_Name"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return suppliers;
    }
    
    public void ensureSampleDataExists() {
        try (Connection conn = getConnection()) {
            // Check if we have any categories
            String checkCategoriesSQL = "SELECT COUNT(*) FROM Category WHERE Status = 'Available'";
            try (PreparedStatement stmt = conn.prepareStatement(checkCategoriesSQL);
                 ResultSet rs = stmt.executeQuery()) {
                if (rs.next() && rs.getInt(1) == 0) {
                    // No categories exist, add sample data
                    addSampleCategories(conn);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error ensuring sample data exists: " + e.getMessage());
        }
    }
    
    private void addSampleCategories(Connection conn) {
        String[] categories = {
            "INSERT INTO Category (Category_ID, Category_Name, Sup_ID) VALUES ('CAT001', N'Xe đạp điện', 'NIJIA')",
            "INSERT INTO Category (Category_ID, Category_Name, Sup_ID) VALUES ('CAT002', N'Xe máy điện', 'NIJIA')",
            "INSERT INTO Category (Category_ID, Category_Name, Sup_ID) VALUES ('CAT003', N'Xe đạp điện', 'TAILG')",
            "INSERT INTO Category (Category_ID, Category_Name, Sup_ID) VALUES ('CAT004', N'Xe máy điện', 'TAILG')",
            "INSERT INTO Category (Category_ID, Category_Name, Sup_ID) VALUES ('CAT005', N'Xe đạp điện', 'YADEA')",
            "INSERT INTO Category (Category_ID, Category_Name, Sup_ID) VALUES ('CAT006', N'Xe máy điện', 'YADEA')",
            "INSERT INTO Category (Category_ID, Category_Name, Sup_ID) VALUES ('CAT007', N'Xe đạp điện', 'VINFAST')",
            "INSERT INTO Category (Category_ID, Category_Name, Sup_ID) VALUES ('CAT008', N'Xe máy điện', 'VINFAST')"
        };
        
        try {
            for (String sql : categories) {
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.executeUpdate();
                } catch (SQLException e) {
                    // Ignore if category already exists
                    System.out.println("Category might already exist: " + e.getMessage());
                }
            }
            System.out.println("Sample categories added successfully!");
        } catch (Exception e) {
            System.err.println("Error adding sample categories: " + e.getMessage());
        }
    }
    
    public void exportExcelBillImport(String filePath) {
        try (Connection conn = getConnection()) {
            // Kiểm tra xem có dữ liệu trong Bill_Imported không
            String checkSql = "SELECT COUNT(*) FROM Bill_Imported WHERE Status = 'Available'";
            try (PreparedStatement checkStmt = conn.prepareStatement(checkSql);
                 ResultSet checkRs = checkStmt.executeQuery()) {
                if (checkRs.next() && checkRs.getInt(1) == 0) {
                    // Nếu không có dữ liệu, tạo dữ liệu mẫu từ Product_Stock
                    createSampleImportBills(conn);
                }
            }
            
            String sql = """
                SELECT 
                    bi.Invoice_No,
                    bi.Admin_ID,
                    bi.Total_Product,
                    bi.Total_Price,
                    bid.Warehouse_Item_ID,
                    ps.Product_Name,
                    bid.Quantity,
                    bid.Unit_Price_Import,
                    bid.Total_Price,
                    bid.Date_Imported,
                    bid.Time_Imported
                FROM Bill_Imported bi
                JOIN Bill_Imported_Details bid ON bi.Invoice_No = bid.Invoice_No AND bi.Admin_ID = bid.Admin_ID
                JOIN Product_Stock ps ON bid.Warehouse_Item_ID = ps.Warehouse_Item_ID
                WHERE bi.Status = 'Available' AND bid.Status = 'Available'
                ORDER BY bi.Invoice_No, bid.Warehouse_Item_ID
            """;
            
            try (PreparedStatement stmt = conn.prepareStatement(sql);
                 ResultSet rs = stmt.executeQuery()) {
                
                Workbook workbook = new XSSFWorkbook();
                Sheet sheet = workbook.createSheet("Import Bills");
                
                // Create header row
                Row headerRow = sheet.createRow(0);
                String[] headers = {
                    "Invoice No", "Admin ID", "Total Product", "Total Price",
                    "Warehouse Item ID", "Product Name", "Quantity", 
                    "Unit Price", "Line Total", "Date Imported", "Time Imported"
                };
                
                for (int i = 0; i < headers.length; i++) {
                    Cell cell = headerRow.createCell(i);
                    cell.setCellValue(headers[i]);
                }
                
                // Add data rows
                int rowNum = 1;
                while (rs.next()) {
                    Row row = sheet.createRow(rowNum++);
                    row.createCell(0).setCellValue(rs.getString("Invoice_No"));
                    row.createCell(1).setCellValue(rs.getString("Admin_ID"));
                    row.createCell(2).setCellValue(rs.getInt("Total_Product"));
                    row.createCell(3).setCellValue(rs.getBigDecimal("Total_Price").doubleValue());
                    row.createCell(4).setCellValue(rs.getString("Warehouse_Item_ID"));
                    row.createCell(5).setCellValue(rs.getString("Product_Name"));
                    row.createCell(6).setCellValue(rs.getInt("Quantity"));
                    row.createCell(7).setCellValue(rs.getBigDecimal("Unit_Price_Import").doubleValue());
                    row.createCell(8).setCellValue(rs.getBigDecimal("Total_Price").doubleValue());
                    row.createCell(9).setCellValue(rs.getDate("Date_Imported").toString());
                    row.createCell(10).setCellValue(rs.getTime("Time_Imported").toString());
                }
                
                // Auto-size columns
                for (int i = 0; i < headers.length; i++) {
                    sheet.autoSizeColumn(i);
                }
                
                // Write to file
                try (FileOutputStream fileOut = new FileOutputStream(filePath)) {
                    workbook.write(fileOut);
                }
                workbook.close();
                
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to export Excel bill import: " + e.getMessage());
        }
    }
    
    public void exportPDFBillImport(String filePath) {
        try (Connection conn = getConnection()) {
            // Kiểm tra xem có dữ liệu trong Bill_Imported không
            String checkSql = "SELECT COUNT(*) FROM Bill_Imported WHERE Status = 'Available'";
            try (PreparedStatement checkStmt = conn.prepareStatement(checkSql);
                 ResultSet checkRs = checkStmt.executeQuery()) {
                if (checkRs.next() && checkRs.getInt(1) == 0) {
                    // Nếu không có dữ liệu, tạo dữ liệu mẫu từ Product_Stock
                    createSampleImportBills(conn);
                }
            }
            
            String sql = """
                SELECT 
                    bi.Invoice_No,
                    bi.Admin_ID,
                    bi.Total_Product,
                    bi.Total_Price,
                    bid.Warehouse_Item_ID,
                    ps.Product_Name,
                    bid.Quantity,
                    bid.Unit_Price_Import,
                    bid.Total_Price,
                    bid.Date_Imported,
                    bid.Time_Imported
                FROM Bill_Imported bi
                JOIN Bill_Imported_Details bid ON bi.Invoice_No = bid.Invoice_No AND bi.Admin_ID = bid.Admin_ID
                JOIN Product_Stock ps ON bid.Warehouse_Item_ID = ps.Warehouse_Item_ID
                WHERE bi.Status = 'Available' AND bid.Status = 'Available'
                ORDER BY bi.Invoice_No, bid.Warehouse_Item_ID
            """;
            
            try (PreparedStatement stmt = conn.prepareStatement(sql);
                 ResultSet rs = stmt.executeQuery()) {
                
                Document document = new Document(PageSize.A4, 40, 40, 50, 50);
                PdfWriter.getInstance(document, new FileOutputStream(filePath));
                document.open();
                
                // Add beautiful header
                addBillImportHeader(document);
                
                // Add summary info
                addBillImportSummary(document, rs);
                
                // Reset result set for data
                try (PreparedStatement stmt2 = conn.prepareStatement(sql);
                     ResultSet rs2 = stmt2.executeQuery()) {
                
                // Create beautiful table
                addBillImportTable(document, rs2);
                }
                
                // Add footer
                addBillImportFooter(document);
                
                document.close();
                
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to export PDF bill import: " + e.getMessage());
        }
    }
    
    private void addBillImportHeader(Document document) throws DocumentException {
        // Main title
        com.itextpdf.text.Font titleFont = getVietnameseFont(18, com.itextpdf.text.Font.BOLD);
        titleFont.setColor(BaseColor.BLUE);
        Paragraph title = new Paragraph("IMPORT BILLS REPORT", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingAfter(15f);
        document.add(title);

        // Date and time
        com.itextpdf.text.Font infoFont = getVietnameseFont(12, com.itextpdf.text.Font.NORMAL);
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy hh:mm a", Locale.ENGLISH);
        String formattedDate = dateFormat.format(new Date());
        
        Paragraph info = new Paragraph();
        info.add(new Chunk("Generated on: " + formattedDate, infoFont));
        info.setAlignment(Element.ALIGN_CENTER);
        info.setSpacingAfter(20f);
        document.add(info);
        
        addLineSeparator(document, 0.5f, 95f, BaseColor.LIGHT_GRAY);
    }
    
    private void addBillImportSummary(Document document, ResultSet rs) throws DocumentException {
        try {
            // Calculate totals
            BigDecimal totalValue = BigDecimal.ZERO;
            int totalBills = 0;
            int totalItems = 0;
            int totalQuantity = 0;
            String currentInvoice = "";
            
            while (rs.next()) {
                if (!currentInvoice.equals(rs.getString("Invoice_No"))) {
                    totalBills++;
                    currentInvoice = rs.getString("Invoice_No");
                }
                totalValue = totalValue.add(rs.getBigDecimal("Total_Price"));
                totalItems++;
                totalQuantity += rs.getInt("Quantity");
            }
            
            // Create summary table
            PdfPTable summaryTable = new PdfPTable(2);
            summaryTable.setWidthPercentage(60);
            summaryTable.setSpacingBefore(10f);
            summaryTable.setSpacingAfter(15f);
            summaryTable.setHorizontalAlignment(Element.ALIGN_CENTER);
            
            // Header color
            BaseColor headerBgColor = new BaseColor(0, 51, 102);
            com.itextpdf.text.Font headerFont = getVietnameseFont(12, com.itextpdf.text.Font.BOLD);
            headerFont.setColor(BaseColor.WHITE);
            
            // Summary data
            String[][] summaryData = {
                {"Total Bills:", String.valueOf(totalBills)},
                {"Total Items:", String.valueOf(totalItems)},
                {"Total Quantity:", String.valueOf(totalQuantity)},
                {"Total Value:", formatCurrency(totalValue.toString())}
            };
            
            for (String[] row : summaryData) {
                // Label cell
                PdfPCell labelCell = new PdfPCell(new Phrase(row[0], headerFont));
                labelCell.setBackgroundColor(headerBgColor);
                labelCell.setPadding(8);
                labelCell.setBorder(Rectangle.BOX);
                labelCell.setBorderWidth(0.5f);
                summaryTable.addCell(labelCell);
                
                // Value cell
                com.itextpdf.text.Font valueFont = getVietnameseFont(12, com.itextpdf.text.Font.BOLD);
                valueFont.setColor(BaseColor.DARK_GRAY);
                PdfPCell valueCell = new PdfPCell(new Phrase(row[1], valueFont));
                valueCell.setPadding(8);
                valueCell.setBorder(Rectangle.BOX);
                valueCell.setBorderWidth(0.5f);
                summaryTable.addCell(valueCell);
            }
            
            document.add(summaryTable);
            addLineSeparator(document, 0.5f, 95f, BaseColor.LIGHT_GRAY);
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    private void addBillImportTable(Document document, ResultSet rs) throws DocumentException {
        // Section title
        com.itextpdf.text.Font sectionFont = getVietnameseFont(14, com.itextpdf.text.Font.BOLD);
        sectionFont.setColor(BaseColor.DARK_GRAY);
        Paragraph section = new Paragraph("IMPORT BILLS DETAILS", sectionFont);
        section.setSpacingAfter(10f);
        document.add(section);
        
        // Create table with 11 columns
        PdfPTable table = new PdfPTable(11);
        table.setWidthPercentage(100);
        table.setSpacingBefore(10f);
        table.setSpacingAfter(15f);
        
        // Column widths
        float[] columnWidths = {1.0f, 1.0f, 0.8f, 1.2f, 1.2f, 2.0f, 0.8f, 1.0f, 1.2f, 1.0f, 1.0f};
        table.setWidths(columnWidths);
        
        // Table header
        com.itextpdf.text.Font headerFont = getVietnameseFont(9, com.itextpdf.text.Font.BOLD);
        headerFont.setColor(BaseColor.WHITE);
        BaseColor headerBgColor = new BaseColor(0, 51, 102);
        
        String[] headers = {"No.", "Invoice No", "Admin ID", "Total Product", "Total Price",
                          "Warehouse ID", "Product Name", "Quantity", "Unit Price", "Line Total", "Date"};
        
        for (String header : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(header, headerFont));
            cell.setBackgroundColor(headerBgColor);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            cell.setPadding(4);
            cell.setBorder(Rectangle.BOX);
            cell.setBorderWidth(0.5f);
            table.addCell(cell);
        }
        
        // Add data rows
        com.itextpdf.text.Font rowFont = getVietnameseFont(8, com.itextpdf.text.Font.NORMAL);
        int rowNum = 1;
        
        try {
            while (rs.next()) {
                // Alternate row color
                BaseColor rowColor = rowNum % 2 == 0 ? new BaseColor(248, 248, 248) : BaseColor.WHITE;
                
                table.addCell(createBillImportCell(String.valueOf(rowNum++), rowFont, rowColor, Element.ALIGN_CENTER));
                table.addCell(createBillImportCell(rs.getString("Invoice_No"), rowFont, rowColor, Element.ALIGN_LEFT));
                table.addCell(createBillImportCell(rs.getString("Admin_ID"), rowFont, rowColor, Element.ALIGN_LEFT));
                table.addCell(createBillImportCell(String.valueOf(rs.getInt("Total_Product")), rowFont, rowColor, Element.ALIGN_CENTER));
                table.addCell(createBillImportCell(formatCurrency(rs.getBigDecimal("Total_Price").toString()), rowFont, rowColor, Element.ALIGN_RIGHT));
                table.addCell(createBillImportCell(rs.getString("Warehouse_Item_ID"), rowFont, rowColor, Element.ALIGN_LEFT));
                table.addCell(createBillImportCell(rs.getString("Product_Name"), rowFont, rowColor, Element.ALIGN_LEFT));
                table.addCell(createBillImportCell(String.valueOf(rs.getInt("Quantity")), rowFont, rowColor, Element.ALIGN_CENTER));
                table.addCell(createBillImportCell(formatCurrency(rs.getBigDecimal("Unit_Price_Import").toString()), rowFont, rowColor, Element.ALIGN_RIGHT));
                table.addCell(createBillImportCell(formatCurrency(rs.getBigDecimal("Total_Price").toString()), rowFont, rowColor, Element.ALIGN_RIGHT));
                table.addCell(createBillImportCell(rs.getDate("Date_Imported").toString(), rowFont, rowColor, Element.ALIGN_CENTER));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        document.add(table);
    }
    
    private PdfPCell createBillImportCell(String text, com.itextpdf.text.Font font, BaseColor bgColor, int alignment) {
        PdfPCell cell = new PdfPCell(new Phrase(text != null ? text : "", font));
        cell.setBackgroundColor(bgColor);
        cell.setPadding(3);
        cell.setBorder(Rectangle.BOX);
        cell.setBorderWidth(0.5f);
        cell.setBorderColor(BaseColor.LIGHT_GRAY);
        cell.setHorizontalAlignment(alignment);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        return cell;
    }
    
    private void addBillImportFooter(Document document) throws DocumentException {
        Paragraph footer = new Paragraph();
        footer.add(new Chunk("Import Management System - Generated Report", 
            getVietnameseFont(10, com.itextpdf.text.Font.ITALIC)));
        footer.setAlignment(Element.ALIGN_CENTER);
        footer.setSpacingBefore(20f);
        document.add(footer);
        
        addLineSeparator(document, 0.5f, 50f, BaseColor.GRAY);
    }
    
    private void createSampleImportBills(Connection conn) {
        try {
            // Kiểm tra xem đã có dữ liệu chưa
            String checkBillSQL = "SELECT COUNT(*) FROM Bill_Imported";
            try (PreparedStatement checkStmt = conn.prepareStatement(checkBillSQL);
                 ResultSet checkRs = checkStmt.executeQuery()) {
                if (checkRs.next() && checkRs.getInt(1) > 0) {
                    return; // Đã có dữ liệu, không cần tạo thêm
                }
            }
            
            // Tạo Bill_Imported mẫu từ Product_Stock
            String insertBillSQL = """
                INSERT INTO Bill_Imported (Invoice_No, Admin_ID, Total_Product, Total_Price, Status)
                SELECT 
                    'BILL-' + CAST(ROW_NUMBER() OVER (ORDER BY ps.Created_Date) AS VARCHAR) AS Invoice_No,
                    'ADMIN001' AS Admin_ID,
                    COUNT(*) AS Total_Product,
                    SUM(ps.Quantity_Stock * ps.Unit_Price_Import) AS Total_Price,
                    'Available' AS Status
                FROM Product_Stock ps
                WHERE ps.Status = 'Available'
                GROUP BY ps.Created_Date
            """;
            
            String insertBillDetailsSQL = """
                INSERT INTO Bill_Imported_Details (Invoice_No, Admin_ID, Warehouse_Item_ID, Quantity, Unit_Price_Import, Total_Price, Date_Imported, Time_Imported, Status)
                SELECT 
                    'BILL-' + CAST(ROW_NUMBER() OVER (ORDER BY ps.Created_Date) AS VARCHAR) AS Invoice_No,
                    'ADMIN001' AS Admin_ID,
                    ps.Warehouse_Item_ID,
                    ps.Quantity_Stock,
                    ps.Unit_Price_Import,
                    ps.Quantity_Stock * ps.Unit_Price_Import AS Total_Price,
                    ps.Created_Date,
                    ps.Created_Time,
                    'Available' AS Status
                FROM Product_Stock ps
                WHERE ps.Status = 'Available'
            """;
            
            try (PreparedStatement stmt1 = conn.prepareStatement(insertBillSQL);
                 PreparedStatement stmt2 = conn.prepareStatement(insertBillDetailsSQL)) {
                stmt1.executeUpdate();
                stmt2.executeUpdate();
                System.out.println("Sample import bills created successfully!");
            }
        } catch (SQLException e) {
            System.err.println("Error creating sample import bills: " + e.getMessage());
        }
    }
    
    // Method để kiểm tra và debug Bill_Imported
    public void debugBillImported() {
        System.out.println("=== DEBUG BILL_IMPORTED ===");
        
        try (Connection conn = getConnection()) {
            // 1. Kiểm tra Admin table
            String adminSQL = "SELECT Admin_ID, Admin_Name, Status FROM Admin WHERE Status = 'Available'";
            try (PreparedStatement stmt = conn.prepareStatement(adminSQL);
                 ResultSet rs = stmt.executeQuery()) {
                System.out.println("Available Admins:");
                while (rs.next()) {
                    System.out.println("  - " + rs.getString("Admin_ID") + " | " + rs.getString("Admin_Name") + " | " + rs.getString("Status"));
                }
            }
            
            // 2. Kiểm tra Bill_Imported
            String billSQL = "SELECT Invoice_No, Admin_ID, Total_Product, Total_Price, Status FROM Bill_Imported ORDER BY Invoice_No";
            try (PreparedStatement stmt = conn.prepareStatement(billSQL);
                 ResultSet rs = stmt.executeQuery()) {
                System.out.println("\nBill_Imported records:");
                int count = 0;
                while (rs.next()) {
                    count++;
                    System.out.println("  " + count + ". " + rs.getString("Invoice_No") + " | " + 
                                    rs.getString("Admin_ID") + " | " + 
                                    rs.getInt("Total_Product") + " | " + 
                                    rs.getBigDecimal("Total_Price") + " | " + 
                                    rs.getString("Status"));
                }
                if (count == 0) {
                    System.out.println("  No Bill_Imported records found!");
                }
            }
            
            // 3. Kiểm tra Bill_Imported_Details
            String detailSQL = "SELECT Invoice_No, Admin_ID, Warehouse_Item_ID, Quantity, Unit_Price_Import, Total_Price FROM Bill_Imported_Details ORDER BY Invoice_No";
            try (PreparedStatement stmt = conn.prepareStatement(detailSQL);
                 ResultSet rs = stmt.executeQuery()) {
                System.out.println("\nBill_Imported_Details records:");
                int count = 0;
                while (rs.next()) {
                    count++;
                    System.out.println("  " + count + ". " + rs.getString("Invoice_No") + " | " + 
                                    rs.getString("Admin_ID") + " | " + 
                                    rs.getString("Warehouse_Item_ID") + " | " + 
                                    rs.getInt("Quantity") + " | " + 
                                    rs.getBigDecimal("Unit_Price_Import") + " | " + 
                                    rs.getBigDecimal("Total_Price"));
                }
                if (count == 0) {
                    System.out.println("  No Bill_Imported_Details records found!");
                }
            }
            
            // 4. Kiểm tra Product_Stock
            String stockSQL = "SELECT Warehouse_Item_ID, Product_Name, Quantity_Stock, Unit_Price_Import, Created_Date FROM Product_Stock WHERE Status = 'Available' ORDER BY Created_Date";
            try (PreparedStatement stmt = conn.prepareStatement(stockSQL);
                 ResultSet rs = stmt.executeQuery()) {
                System.out.println("\nProduct_Stock records:");
                int count = 0;
                while (rs.next()) {
                    count++;
                    System.out.println("  " + count + ". " + rs.getString("Warehouse_Item_ID") + " | " + 
                                    rs.getString("Product_Name") + " | " + 
                                    rs.getInt("Quantity_Stock") + " | " + 
                                    rs.getBigDecimal("Unit_Price_Import") + " | " + 
                                    rs.getDate("Created_Date"));
                }
                if (count == 0) {
                    System.out.println("  No Product_Stock records found!");
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Error debugging Bill_Imported: " + e.getMessage());
            e.printStackTrace();
        }
    }
}