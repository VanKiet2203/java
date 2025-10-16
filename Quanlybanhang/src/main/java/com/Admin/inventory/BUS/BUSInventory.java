package com.Admin.inventory.BUS;

import com.Admin.inventory.DAO.DAOInventory;
import com.Admin.inventory.DTO.DTOInventory;
import com.Admin.inventory.DTO.DTOImportBill;
import com.Admin.inventory.DTO.DTOImportBillDetails;

import javax.swing.table.DefaultTableModel;
import java.math.BigDecimal;
import java.util.List;
import java.io.File;

public class BUSInventory {
    private DAOInventory daoInventory;
    
    public BUSInventory() {
        daoInventory = new DAOInventory();
    }
    
    // Lấy tất cả sản phẩm trong kho
    public List<DTOInventory> getAllInventory() {
        return daoInventory.getAllInventory();
    }
    
    // Tìm kiếm sản phẩm trong kho
    public List<DTOInventory> searchInventory(String searchType, String keyword) {
        return daoInventory.searchInventory(searchType, keyword);
    }
    
    // Lấy tất cả hóa đơn nhập
    public List<DTOImportBill> getAllImportBills() {
        return daoInventory.getAllImportBills();
    }
    
    // Lấy chi tiết hóa đơn nhập
    public List<DTOImportBillDetails> getImportBillDetails(String invoiceNo) {
        return daoInventory.getImportBillDetails(invoiceNo);
    }
    
    // Nhập sản phẩm mới vào kho
    public boolean importSimpleProduct(String productId, String productName, BigDecimal price, 
                                     String categoryId, String brandId, int quantity, 
                                     BigDecimal unitPrice, String adminId, String color, 
                                     String speed, String batteryCapacity) {
        return daoInventory.importSimpleProduct(productId, productName, price, categoryId, 
                                              brandId, quantity, unitPrice, adminId, color, speed, batteryCapacity);
    }
    
    // Nhập thêm sản phẩm đã có vào kho
    public boolean importExistingProduct(String productId, int quantity, BigDecimal unitPrice, String adminId) {
        return daoInventory.importExistingProduct(productId, quantity, unitPrice, adminId);
    }
    
    // Load dữ liệu vào bảng inventory - Cập nhật cho database mới
    public void loadInventoryToTable(DefaultTableModel model) {
        model.setRowCount(0);
        List<DTOInventory> inventoryList = getAllInventory();
        
        for (DTOInventory inventory : inventoryList) {
            Object[] row = {
                inventory.getWarehouseItemId(),        // Warehouse ID
                inventory.getProductName(),            // Product Name
                inventory.getCategoryId(),             // Category
                inventory.getBrandId(),                // Supplier
                inventory.getUnitPriceImport(),        // Import Price
                inventory.getQuantityInStock(),        // Current Stock
                "N/A",                                 // Total Imported (sẽ tính từ Bill_Imported_Details)
                "N/A"                                  // Total Sold (sẽ tính từ Bill_Exported_Details)
            };
            model.addRow(row);
        }
    }
    
    // Load dữ liệu vào bảng import bills
    public void loadImportBillsToTable(DefaultTableModel model) {
        model.setRowCount(0);
        List<DTOImportBill> bills = getAllImportBills();
        
        for (DTOImportBill bill : bills) {
            Object[] row = {
                bill.getInvoiceNo(),
                bill.getAdminId(),
                bill.getAdminName(),
                bill.getTotalProduct(),
                bill.getTotalPrice(),
                bill.getDateImported(),
                bill.getTimeImported()
            };
            model.addRow(row);
        }
    }
    
    // Load chi tiết hóa đơn vào bảng
    public void loadBillDetailsToTable(String invoiceNo, DefaultTableModel model) {
        model.setRowCount(0);
        List<DTOImportBillDetails> details = getImportBillDetails(invoiceNo);
        
        for (DTOImportBillDetails detail : details) {
            Object[] row = {
                detail.getProductId(),
                detail.getProductName(),
                detail.getQuantity(),
                detail.getUnitPrice(),
                detail.getTotalPrice(),
                detail.getDateImported(),
                detail.getTimeImported()
            };
            model.addRow(row);
        }
    }
    
    // Tìm kiếm và load vào bảng - Cập nhật cho database mới
    public void searchAndLoadToTable(String searchType, String keyword, DefaultTableModel model) {
        model.setRowCount(0);
        List<DTOInventory> inventoryList = searchInventory(searchType, keyword);
        
        for (DTOInventory inventory : inventoryList) {
            Object[] row = {
                inventory.getWarehouseItemId(),        // Warehouse ID
                inventory.getProductName(),            // Product Name
                inventory.getCategoryId(),             // Category
                inventory.getBrandId(),                // Supplier
                inventory.getUnitPriceImport(),        // Import Price
                inventory.getQuantityInStock(),        // Current Stock
                "N/A",                                 // Total Imported (sẽ tính từ Bill_Imported_Details)
                "N/A"                                  // Total Sold (sẽ tính từ Bill_Exported_Details)
            };
            model.addRow(row);
        }
    }
    
    // Kiểm tra sản phẩm có tồn tại không
    public boolean isProductExists(String productId) {
        List<DTOInventory> inventoryList = getAllInventory();
        return inventoryList.stream()
                .anyMatch(inventory -> inventory.getWarehouseItemId().equals(productId));
    }
    
    // Lấy thông tin sản phẩm theo ID
    public DTOInventory getProductById(String productId) {
        List<DTOInventory> inventoryList = getAllInventory();
        return inventoryList.stream()
                .filter(inventory -> inventory.getWarehouseItemId().equals(productId))
                .findFirst()
                .orElse(null);
    }
    
    // Xuất dữ liệu kho hàng ra Excel
    public boolean exportInventoryToExcel(String filePath) {
        return daoInventory.exportInventoryToExcel(filePath);
    }
    
    // Xuất hóa đơn nhập ra Excel
    public boolean exportBillToExcel(String invoiceNo, String filePath) {
        return daoInventory.exportBillToExcel(invoiceNo, filePath);
    }
    
    // Nhập dữ liệu từ Excel
    public boolean importInventoryFromExcel(File excelFile) {
        return daoInventory.importInventoryFromExcel(excelFile);
    }
    
    // Dọn dẹp tất cả dữ liệu kho hàng
    public boolean cleanAllInventory() {
        return daoInventory.cleanAllInventory();
    }
    
    // Xuất dữ liệu kho hàng ra PDF
    public boolean exportInventoryToPDF(String filePath) {
        return daoInventory.exportInventoryToPDF(filePath);
    }
}
