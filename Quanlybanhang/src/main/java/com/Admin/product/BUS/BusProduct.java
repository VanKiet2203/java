package com.Admin.product.BUS;
import java.util.List;
import javax.swing.JSpinner;
import java.math.BigDecimal;
import com.ComponentandDatabase.Components.CustomDialog;
import com.Admin.product.DAO.DAOProduct;
import com.ComponentandDatabase.Components.MyButton;
import com.ComponentandDatabase.Components.MyTextField;
import com.ComponentandDatabase.Components.MyCombobox;
import com.Admin.category.DTO.DTOCategory;
import com.Admin.product.DTO.DTOProduct;
import javax.swing.JPanel;
import javax.swing.table.DefaultTableModel;

public class BusProduct {
    private DAOProduct productDAO;

    public BusProduct() {
        productDAO = new DAOProduct();
        daoProduct = productDAO; // Ensure both fields reference the same DAOProduct instance
    }

    public boolean addProduct(String productId, String productName, String color,
                            String speed, String batteryCapacity, int quantity,
                            String categoryId, String image, double price) {
        
        DTOProduct product = new DTOProduct(productId, productName, color,
                                          speed, batteryCapacity, quantity,
                                          categoryId, image, price);
        return productDAO.addProduct(product);
    }
   
    private String imagePath;

    private DAOProduct daoProduct;
    // Removed duplicate constructor
    

    // Phương thức xử lý tải ảnh khi nhấn nút Upload
   public void handleUpload(MyButton bntupload, JPanel panel) {
     imagePath = daoProduct.handleUploadButton(bntupload, panel); // Lưu lại đường dẫn ảnh
}
    
    public String getImagePath() {
        return imagePath;
    }

    // ⭐ Thêm phương thức này để gọi từ DAOProduct
    public List<DTOCategory> getAllCategoriesWithSupplier() {
        return daoProduct.getAllCategoriesWithSupplier();
    }
    
    // FIXED: Method để sửa lỗi số lượng
    public boolean fixQuantityIssues() {
        try {
            // Gọi method từ inventory để fix lỗi số lượng
            com.Admin.inventory.BUS.BUSInventory busInventory = new com.Admin.inventory.BUS.BUSInventory();
            busInventory.resetAndSyncAllQuantities();
            return true;
        } catch (Exception e) {
            CustomDialog.showError("Failed to fix quantity issues: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    // ✅ Hàm xử lý lưu sản phẩm
    public void saveProduct(DTOProduct product) {
        daoProduct.saveProduct(product);  // Gọi phương thức saveProduct của DAO để lưu vào DB
    }
    
   public void uploadProduct(DefaultTableModel model){
       daoProduct.uploadProductToTable(model);
   }
    public DTOProduct getProductById(String productId) {
        return daoProduct.getProductById(productId);
    }
    
    public boolean updateProduct(DTOProduct product) {
        try {
            // Gọi phương thức update từ DAO
            boolean updateResult = daoProduct.updateProduct(product);
            
            if (updateResult) {
                CustomDialog.showSuccess("Product updated successfully!");
                return true;
            } else {
               CustomDialog.showError("Product update failed: ID not found.");
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            CustomDialog.showError("An error occurred while updating the product.");
            return false;
        }
    }
    
    public boolean deleteProduct(String productId) {
        try {
            // 1. Kiểm tra sản phẩm có tồn tại không
            DTOProduct product = daoProduct.getProductById(productId);
            if (product == null) {
                CustomDialog.showError("Can't fine the product to delete !");
                return false;
            }

            // 2. Gọi DAO để thực hiện xóa
            boolean deleteResult = daoProduct.deleteProduct(productId);
            
            if (deleteResult) {
                CustomDialog.showSuccess("Product deleted successfully !");
                return true;
            } else {
                CustomDialog.showError("Product delete failed: ID not found.");
                return false;
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            CustomDialog.showError("An error occurred while deleting the product." + e.getMessage());
            return false;
        }
    }
    
    public void searchProduct(String keyword, String selected, DefaultTableModel model){
        daoProduct.searchProduct(keyword, selected, model);
    }
    public void exportFile(String path){
        daoProduct.exportProductToExcel(path);
    }
    
    public void importFile(java.io.File file) {
        daoProduct.importProductFromExcel(file);
    }
    
    // Lấy thông tin sản phẩm từ kho (Product_Stock) để tạo Product
    public DTOProduct getProductFromWarehouse(String warehouseItemId) {
        return daoProduct.getProductFromWarehouse(warehouseItemId);
    }
    
    // Lấy danh sách Warehouse Items chưa được tạo Product
    public void loadAvailableWarehouseItems(DefaultTableModel model) {
        daoProduct.loadAvailableWarehouseItems(model);
    }
    
    // Tạo Product từ Warehouse Item
    public boolean createProductFromWarehouse(String warehouseItemId, String color, String speed, 
                                            String batteryCapacity, java.math.BigDecimal price) {
        try {
            boolean success = daoProduct.createProductFromWarehouse(warehouseItemId, color, speed, batteryCapacity, price);
            if (success) {
                // KHÔNG cần gọi syncAllProductQuantities() vì trigger đã tự động cập nhật
                // Trigger trg_Set_Product_Quantity_On_Create sẽ set Quantity = Quantity_Stock
                // Trigger trg_Update_Product_Stock_On_Import sẽ tính lại khi có dữ liệu nhập
                CustomDialog.showSuccess("Product created from warehouse item successfully!");
            } else {
                CustomDialog.showError("Failed to create product from warehouse item!");
            }
            return success;
        } catch (Exception e) {
            CustomDialog.showError("Error creating product: " + e.getMessage());
            return false;
        }
    }
    
    // Lấy danh sách Warehouse Items có thể tạo Product
    public List<String> getAvailableWarehouseItems() {
        try {
            return daoProduct.getAvailableWarehouseItems();
        } catch (Exception e) {
            CustomDialog.showError("Failed to load warehouse items: " + e.getMessage());
            return new java.util.ArrayList<>();
        }
    }
    
    
    
    // Method để đồng bộ tất cả số lượng Product
    public void syncAllProductQuantities() {
        try {
            daoProduct.syncAllProductQuantities();
            CustomDialog.showSuccess("All product quantities synchronized successfully!");
        } catch (Exception e) {
            CustomDialog.showError("Failed to sync product quantities: " + e.getMessage());
        }
    }
    
    // Method để kiểm tra tính đúng đắn của số lượng
    public void checkQuantityBalance() {
        try {
            daoProduct.checkQuantityBalance();
        } catch (Exception e) {
            CustomDialog.showError("Failed to check quantity balance: " + e.getMessage());
        }
    }
    
    
}



