package com.Admin.inventory.DAO;

import com.Admin.inventory.DTO.DTOInventory;
import com.ComponentandDatabase.Components.CustomDialog;
import com.ComponentandDatabase.Database_Connection.DatabaseConnection;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

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
        String insertSQL = """
            INSERT INTO Product_Stock (Warehouse_Item_ID, Product_Name, Category_ID, Sup_ID, 
                                    Quantity_Stock, Unit_Price_Import, Created_Date, Created_Time)
            VALUES (?, ?, ?, ?, ?, ?, GETDATE(), GETDATE())
        """;
        String checkCategorySQL = "SELECT COUNT(*) FROM Category WHERE Category_ID = ?";
        String checkSupplierSQL = "SELECT COUNT(*) FROM Supplier WHERE Sup_ID = ?";
        String checkWarehouseSQL = "SELECT COUNT(*) FROM Product_Stock WHERE Warehouse_Item_ID = ?";

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
                             PreparedStatement checkWarehouseStmt = conn.prepareStatement(checkWarehouseSQL);
                             PreparedStatement insertStmt = conn.prepareStatement(insertSQL)) {

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

                            // Check if Warehouse Item already exists
                            checkWarehouseStmt.setString(1, warehouseItemId);
                            try (ResultSet warehouseRs = checkWarehouseStmt.executeQuery()) {
                                if (warehouseRs.next() && warehouseRs.getInt(1) > 0) {
                                    errors.append("Row ").append(rowNum).append(": Warehouse Item ID '").append(warehouseItemId).append("' already exists\n");
                                    errorCount++;
                                    continue;
                                }
                            }

                            // Insert warehouse item
                            insertStmt.setString(1, warehouseItemId);
                            insertStmt.setString(2, productName);
                            insertStmt.setString(3, categoryId);
                            insertStmt.setString(4, supId);
                            insertStmt.setInt(5, quantity);
                            insertStmt.setBigDecimal(6, BigDecimal.valueOf(unitPrice));

                            insertStmt.executeUpdate();
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
    
    // Tạo hóa đơn nhập từ Product_Stock
    public String createImportBill() {
        String billId = "BILL-" + System.currentTimeMillis();
        String insertBillSQL = """
            INSERT INTO Import_Bill (Bill_ID, Created_Date, Created_Time, Total_Amount, Status)
            VALUES (?, GETDATE(), GETDATE(), ?, 'Completed')
        """;
        String insertBillDetailSQL = """
            INSERT INTO Import_Bill_Details (Bill_ID, Warehouse_Item_ID, Quantity, Unit_Price, Total_Price)
            SELECT ?, Warehouse_Item_ID, Quantity_Stock, Unit_Price_Import, 
                   (Quantity_Stock * Unit_Price_Import)
            FROM Product_Stock
            WHERE Warehouse_Item_ID NOT IN (
                SELECT DISTINCT Warehouse_Item_ID FROM Import_Bill_Details
            )
        """;
        String calculateTotalSQL = """
            UPDATE Import_Bill 
            SET Total_Amount = (
                SELECT SUM(Total_Price) 
                FROM Import_Bill_Details 
                WHERE Bill_ID = ?
            )
            WHERE Bill_ID = ?
        """;

        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);
            
            try (PreparedStatement insertBillStmt = conn.prepareStatement(insertBillSQL);
                 PreparedStatement insertDetailStmt = conn.prepareStatement(insertBillDetailSQL);
                 PreparedStatement calculateTotalStmt = conn.prepareStatement(calculateTotalSQL)) {

                // Insert bill
                insertBillStmt.setString(1, billId);
                insertBillStmt.setBigDecimal(2, BigDecimal.ZERO); // Will be updated later
                insertBillStmt.executeUpdate();

                // Insert bill details
                insertDetailStmt.setString(1, billId);
                insertDetailStmt.executeUpdate();

                // Calculate and update total
                calculateTotalStmt.setString(1, billId);
                calculateTotalStmt.setString(2, billId);
                calculateTotalStmt.executeUpdate();
                
                conn.commit();
                return billId;
                
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
        String checkWarehouseSQL = "SELECT COUNT(*) FROM Product_Stock WHERE Warehouse_Item_ID = ?";

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
                                conn.commit();
                                successCount++;
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
             ResultSet rs = stmt.executeQuery();
             FileOutputStream fos = new FileOutputStream(filePath);
             XSSFWorkbook workbook = new XSSFWorkbook()) {
            
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
            
            workbook.write(fos);
            
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
            
            Document document = new Document();
            PdfWriter.getInstance(document, new FileOutputStream(filePath));
            document.open();
            
            // Add title
            com.itextpdf.text.Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16);
            Paragraph title = new Paragraph("INVENTORY REPORT", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);
            document.add(new Paragraph(" "));
            
            // Create table
            PdfPTable table = new PdfPTable(8);
            table.setWidthPercentage(100);
            
            // Add headers
            String[] headers = {"Warehouse ID", "Product Name", "Category", "Supplier", 
                              "Quantity", "Unit Price", "Total Value", "Created Date"};
            for (String header : headers) {
                table.addCell(header);
            }
            
            // Add data
            while (rs.next()) {
                table.addCell(rs.getString("Warehouse_Item_ID"));
                table.addCell(rs.getString("Product_Name"));
                table.addCell(rs.getString("Category_Name"));
                table.addCell(rs.getString("Sup_Name"));
                table.addCell(String.valueOf(rs.getInt("Quantity_Stock")));
                table.addCell(String.valueOf(rs.getDouble("Unit_Price_Import")));
                table.addCell(String.valueOf(rs.getDouble("Total_Value")));
                table.addCell(rs.getDate("Created_Date").toString());
            }
            
            document.add(table);
            document.close();
            
        } catch (SQLException | IOException | DocumentException e) {
            e.printStackTrace();
            throw new RuntimeException("PDF export failed", e);
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
        String sql = "SELECT MAX(CAST(SUBSTRING(Warehouse_Item_ID, 3, LEN(Warehouse_Item_ID)) AS INT)) FROM Product_Stock WHERE Warehouse_Item_ID LIKE 'WH%'";
        
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
        
        String sql = "INSERT INTO Product_Stock (Warehouse_Item_ID, Product_Name, Category_ID, Sup_ID, Quantity_Stock, Unit_Price_Import, Created_Date, Created_Time) VALUES (?, ?, ?, ?, ?, ?, GETDATE(), GETDATE())";
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            System.out.println("Adding inventory item:");
            System.out.println("Warehouse ID: " + inventoryItem.getWarehouseItemId());
            System.out.println("Product Name: " + inventoryItem.getProductName());
            System.out.println("Category ID: " + inventoryItem.getCategoryId());
            System.out.println("Supplier ID: " + inventoryItem.getSupId());
            System.out.println("Quantity: " + inventoryItem.getQuantityStock());
            System.out.println("Unit Price: " + inventoryItem.getUnitPriceImport());
            
            stmt.setString(1, inventoryItem.getWarehouseItemId());
            stmt.setString(2, inventoryItem.getProductName());
            stmt.setString(3, inventoryItem.getCategoryId());
            stmt.setString(4, inventoryItem.getSupId());
            stmt.setInt(5, inventoryItem.getQuantityStock());
            stmt.setBigDecimal(6, inventoryItem.getUnitPriceImport());
            
            int result = stmt.executeUpdate();
            System.out.println("Insert result: " + result);
            return result > 0;
        } catch (SQLException e) {
            System.err.println("SQL Error adding inventory item: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    private boolean validateCategoryExists(String categoryId) {
        String sql = "SELECT COUNT(*) FROM Category WHERE Category_ID = ?";
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
        String sql = "SELECT COUNT(*) FROM Supplier WHERE Sup_ID = ?";
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
        String sql = "SELECT Sup_ID, Sup_Name FROM Supplier ORDER BY Sup_ID";
        
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
            String checkCategoriesSQL = "SELECT COUNT(*) FROM Category";
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
}