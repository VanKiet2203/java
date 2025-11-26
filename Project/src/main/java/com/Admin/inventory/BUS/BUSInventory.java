package com.Admin.inventory.BUS;

import com.Admin.inventory.DAO.DAOInventory;
import com.Admin.inventory.DTO.DTOInventory;
import com.ComponentandDatabase.Components.CustomDialog;
import javax.swing.table.DefaultTableModel;
import java.io.File;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class BUSInventory {
    private DAOInventory daoInventory;
    
    public BUSInventory() {
        daoInventory = new DAOInventory();
    }
    
    public void loadInventoryData(DefaultTableModel model) {
        try {
            daoInventory.loadInventoryData(model);
        } catch (Exception e) {
            CustomDialog.showError("Failed to load inventory data: " + e.getMessage());
        }
    }
    
    public void searchInventory(String keyword, String searchType, DefaultTableModel model) {
        try {
            daoInventory.searchInventory(keyword, searchType, model);
        } catch (Exception e) {
            CustomDialog.showError("Search failed: " + e.getMessage());
        }
    }
    
    public boolean importNewItems(File excelFile) {
        try {
            return daoInventory.importInventoryFromExcel(excelFile);
        } catch (Exception e) {
            CustomDialog.showError("Import failed: " + e.getMessage());
            return false;
        }
    }
    
    public boolean importExistingItems(File excelFile) {
        try {
            return daoInventory.updateInventoryFromExcel(excelFile);
        } catch (Exception e) {
            CustomDialog.showError("Import failed: " + e.getMessage());
            return false;
        }
    }
    
    public boolean importInventory(File excelFile) {
        try {
            return daoInventory.importInventoryFromExcel(excelFile);
        } catch (Exception e) {
            CustomDialog.showError("Import failed: " + e.getMessage());
            return false;
        }
    }
    
    public void exportToExcel(String filePath) {
        try {
            daoInventory.exportInventoryToExcel(filePath);
        } catch (Exception e) {
            CustomDialog.showError("Export failed: " + e.getMessage());
        }
    }
    
    // Method để đồng bộ số lượng toàn hệ thống
    public void syncAllQuantities() {
        try {
            daoInventory.checkAndSyncAllQuantities();
            CustomDialog.showSuccess("All quantities synchronized successfully!");
        } catch (Exception e) {
            CustomDialog.showError("Failed to sync quantities: " + e.getMessage());
        }
    }
    
    // Method để đồng bộ số lượng Product
    public void syncProductQuantities() {
        try {
            daoInventory.syncProductQuantities();
            CustomDialog.showSuccess("Product quantities synchronized successfully!");
        } catch (Exception e) {
            CustomDialog.showError("Failed to sync product quantities: " + e.getMessage());
        }
    }
    
    // Method để reset và đồng bộ lại tất cả số lượng (sửa lỗi nhân đôi)
    public void resetAndSyncAllQuantities() {
        try {
            daoInventory.resetAndSyncAllQuantities();
            CustomDialog.showSuccess("All quantities reset and synchronized successfully!");
        } catch (Exception e) {
            CustomDialog.showError("Failed to reset and sync quantities: " + e.getMessage());
        }
    }
    
    public void exportToPDF(String filePath) {
        try {
            daoInventory.exportInventoryToPDF(filePath);
            CustomDialog.showSuccess("Inventory exported to PDF successfully!");
        } catch (Exception e) {
            CustomDialog.showError("PDF export failed: " + e.getMessage());
        }
    }
    
    public void loadBillsData(DefaultTableModel model) {
        try {
            daoInventory.loadBillsData(model);
        } catch (Exception e) {
            CustomDialog.showError("Failed to load bills data: " + e.getMessage());
        }
    }
    
    public DTOInventory getInventoryItemById(String warehouseItemId) {
        try {
            return daoInventory.getInventoryItemById(warehouseItemId);
        } catch (Exception e) {
            CustomDialog.showError("Failed to get inventory item: " + e.getMessage());
            return null;
        }
    }
    
    public DAOInventory.InventoryItemInfo getInventoryItemFullInfo(String warehouseItemId) {
        try {
            return daoInventory.getInventoryItemFullInfo(warehouseItemId);
        } catch (Exception e) {
            CustomDialog.showError("Failed to get inventory item info: " + e.getMessage());
            return null;
        }
    }
    
    public boolean updateInventoryItem(DTOInventory item) {
        try {
            return daoInventory.updateInventoryItem(item);
        } catch (Exception e) {
            CustomDialog.showError("Failed to update inventory item: " + e.getMessage());
            return false;
        }
    }
    
    public String createImportBill() {
        try {
            String billId = daoInventory.createImportBill();
            CustomDialog.showSuccess("Import bill created successfully!\nBill ID: " + billId);
            return billId;
        } catch (Exception e) {
            CustomDialog.showError("Failed to create import bill: " + e.getMessage());
            return null;
        }
    }
    
    public String generateWarehouseId() {
        try {
            return daoInventory.generateWarehouseId();
        } catch (Exception e) {
            CustomDialog.showError("Failed to generate warehouse ID: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Generate nhiều Warehouse IDs cùng lúc
     * @param count Số lượng IDs cần generate
     * @return List các Warehouse IDs unique
     */
    public List<String> generateMultipleWarehouseIds(int count) {
        try {
            return daoInventory.generateMultipleWarehouseIds(count);
        } catch (Exception e) {
            CustomDialog.showError("Failed to generate warehouse IDs: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    
    public boolean addInventoryItem(DTOInventory inventoryItem) {
        try {
            boolean result = daoInventory.addInventoryItem(inventoryItem);
            if (!result) {
                CustomDialog.showError("Failed to add inventory item! Please check:\n" +
                    "1. Category ID exists in database\n" +
                    "2. Supplier ID exists in database\n" +
                    "3. Warehouse ID is unique\n" +
                    "4. All required fields are filled");
            }
            return result;
        } catch (Exception e) {
            CustomDialog.showError("Failed to add inventory item: " + e.getMessage());
            return false;
        }
    }
    
    public List<String> getAllSuppliers() {
        try {
            return daoInventory.getAllSuppliers();
        } catch (Exception e) {
            CustomDialog.showError("Failed to load suppliers: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    
    public void ensureSampleDataExists() {
        try {
            daoInventory.ensureSampleDataExists();
        } catch (Exception e) {
            System.err.println("Failed to ensure sample data exists: " + e.getMessage());
        }
    }
    
    public boolean createProductFromWarehouse(String warehouseItemId, String color, String speed, 
                                            String batteryCapacity, BigDecimal price) {
        try {
            return daoInventory.createProductFromWarehouse(warehouseItemId, color, speed, batteryCapacity, price);
        } catch (Exception e) {
            CustomDialog.showError("Failed to create product from warehouse: " + e.getMessage());
            return false;
        }
    }
    
    
    public void exportExcelBillImport(String filePath) {
        try {
            daoInventory.exportExcelBillImport(filePath);
            CustomDialog.showSuccess("Excel bill import exported successfully!");
        } catch (Exception e) {
            CustomDialog.showError("Excel bill export failed: " + e.getMessage());
        }
    }
    
    public void exportInventory(String filePath) {
        try {
            daoInventory.exportInventoryToExcel(filePath);
            CustomDialog.showSuccess("Inventory exported successfully!");
        } catch (Exception e) {
            CustomDialog.showError("Export failed: " + e.getMessage());
        }
    }
    
    public void exportPDFBillImport(String filePath) {
        try {
            daoInventory.exportPDFBillImport(filePath);
            CustomDialog.showSuccess("PDF bill export successfully!");
        } catch (Exception e) {
            CustomDialog.showError("PDF bill export failed: " + e.getMessage());
        }
    }
    
    public void exportPDFBillImport(String filePath, String billId) {
        try {
            daoInventory.exportPDFBillImport(filePath, billId);
            CustomDialog.showSuccess("PDF bill export successfully!");
        } catch (Exception e) {
            CustomDialog.showError("PDF bill export failed: " + e.getMessage());
        }
    }
    
    // Method để nhập lại Warehouse Item (cộng thêm số lượng)
    public boolean reimportWarehouseItem(String warehouseItemId, int additionalQuantity, java.math.BigDecimal unitPrice) {
        try {
            // Use stored procedure based reimport for correct quantity sync
            boolean result = daoInventory.reimportWarehouseItemProc(warehouseItemId, additionalQuantity, unitPrice);
            if (result) {
                CustomDialog.showSuccess("Warehouse item reimported successfully!\n" +
                    "Warehouse ID: " + warehouseItemId + "\n" +
                    "Additional Quantity: " + additionalQuantity);
            } else {
                CustomDialog.showError("Failed to reimport warehouse item!\n" +
                    "Please check if Warehouse ID exists: " + warehouseItemId);
            }
            return result;
        } catch (Exception e) {
            CustomDialog.showError("Failed to reimport warehouse item: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Batch import nhiều items vào cùng 1 hóa đơn (cùng supplier)
     * @param items Danh sách các inventory items
     * @param supplierId Supplier ID chung cho tất cả items
     * @return true nếu thành công, false nếu thất bại
     */
    public boolean addInventoryItemsBatch(List<DTOInventory> items, String supplierId) {
        try {
            if (items == null || items.isEmpty()) {
                CustomDialog.showError("Danh sách sản phẩm trống!");
                return false;
            }
            
            if (supplierId == null || supplierId.trim().isEmpty()) {
                CustomDialog.showError("Vui lòng chọn nhà cung cấp!");
                return false;
            }
            
            boolean result = daoInventory.addInventoryItemsBatch(items, supplierId);
            if (result) {
                CustomDialog.showSuccess("Nhập hàng thành công!\n" +
                    "Số lượng sản phẩm: " + items.size() + "\n" +
                    "Nhà cung cấp: " + supplierId);
            } else {
                CustomDialog.showError("Nhập hàng thất bại!\n" +
                    "Vui lòng kiểm tra lại thông tin sản phẩm và nhà cung cấp.");
            }
            return result;
        } catch (Exception e) {
            CustomDialog.showError("Lỗi nhập hàng: " + e.getMessage());
            return false;
        }
    }
}