package com.Admin.category.BUS;

import com.Admin.category.DAO.ControlCategory;
import com.Admin.category.DTO.DTOCategory;
import java.io.File;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import java.util.List;

public class BusCategory {
    private ControlCategory controlCategory;

    public BusCategory() {
        controlCategory = new ControlCategory();
    }

    // Thêm danh mục
    public boolean addCategory(DTOCategory category) {
        // Validate
        if (category.getCategoryID() == null || category.getCategoryID().trim().isEmpty()) {
            throw new IllegalArgumentException("Category ID cannot be empty");
        }
        if (category.getCategoryName() == null || category.getCategoryName().trim().isEmpty()) {
            throw new IllegalArgumentException("Category Name cannot be empty");
        }
        if (category.getSupID() == null || category.getSupID().trim().isEmpty()) {
            throw new IllegalArgumentException("Supplier ID cannot be empty");
        }
        // call insertCategory from DAO (ControlCategory.java defines insertCategory)
        return controlCategory.insertCategory(category);
    }

    // Cập nhật danh mục
    public boolean updateCategory(DTOCategory category) {
        // Validate
        if (category.getCategoryID() == null || category.getCategoryID().trim().isEmpty()) {
            throw new IllegalArgumentException("Category ID cannot be empty");
        }
        if (category.getCategoryName() == null || category.getCategoryName().trim().isEmpty()) {
            throw new IllegalArgumentException("Category Name cannot be empty");
        }
        if (category.getSupID() == null || category.getSupID().trim().isEmpty()) {
            throw new IllegalArgumentException("Supplier ID cannot be empty");
        }

        return controlCategory.updateCategory(category);
    }

    // Xóa danh mục
    public boolean deleteCategory(String categoryID) {
        return controlCategory.deleteCategory(categoryID);
    }

    // Lấy danh sách tất cả danh mục
    public List<DTOCategory> getAllCategories() {
        return controlCategory.getAllCategories();
    }

    public List<DTOCategory> searchCategory(String keyword, String selectedItem) {
        // Delegate search to DAO. ControlCategory.searchCategories handles mapping/SQL.
        return controlCategory.searchCategories(keyword, selectedItem);
    }

    // Helper method to map selected item to column name
    private String mapSearchColumn(String selectedItem) {
        switch (selectedItem) {
            case "Category ID":
                return "c.Category_ID";
            case "Category Name":
                return "c.Category_Name";
            case "Supplier ID":
                return "c.Sup_ID";
            default:
                throw new IllegalArgumentException("Invalid search column: " + selectedItem);
        }
    }

    // Kiểm tra trùng mã
    public boolean isDuplicateID(String categoryID) {
        return controlCategory.isDuplicateID(categoryID);
    }

   // Trong lớp BUS (Business logic layer)
    public void loadCategoryToTable(DefaultTableModel model) {
        controlCategory.loadCategoryToTable(model);
    }
    
    public List<String> getAllSupplierIDs() {
       return controlCategory.getAllSupplierIDs();
}
    public DTOCategory getCategoryID(String categoryID) {
      return controlCategory.getCategoryByID(categoryID);
}
    public void refreshTable(DefaultTableModel model){
       controlCategory.loadCategoryToTable(model);
}
    public void importFile(File file){
     controlCategory.importFile(file);
 }
    public void exportFile(JTable table){
        controlCategory.exportFile(table);
    }
}
