package com.Admin.export.BUS;

import com.Admin.export.DAO.DAO_ExportBill;
import com.Admin.export.DTO.DTO_BillExported;
import com.Admin.export.DTO.DTO_BillExportedDetail;
import com.Admin.export.DTO.DTO_BillExport;
import com.Admin.export.DTO.DTO_WarrantyInfo;
import com.ComponentandDatabase.Components.CustomDialog;
import com.User.dashboard_user.DTO.DTOProfile_cus;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.sql.Date;
import java.time.LocalDate;

public class BUS_ExportBill {
    private final DAO_ExportBill daoExportBill;

    public BUS_ExportBill() {
        this.daoExportBill = new DAO_ExportBill();
    }
    
    public DTOProfile_cus getCustomerInfo(String customerID) throws SQLException {
        return daoExportBill.getCustomerInfoByID(customerID);
    }
    
  
    public DTOProfile_cus getCustomerInfoSafe(String customerID) {
        try {
            return daoExportBill.getCustomerInfoByID(customerID);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }
    public boolean insertBillExported(DTO_BillExported bill) {
        return daoExportBill.insertBillExported(bill, null);
    }
    
    public boolean insertBillExported(DTO_BillExported bill, String promotionCode) {
        return daoExportBill.insertBillExported(bill, promotionCode);
    }
    
    public boolean insertBillDetail(DTO_BillExportedDetail detail, List<String> imeiList) {
        // Tính toán Start_Date và End_Date từ Warranty_Months
        calculateWarrantyDates(detail);
        return daoExportBill.insertBillExportedDetail(detail, detail.getPromotionCode());
    }


     public boolean updateProductQuantity(DTO_BillExportedDetail detail) {
        try {
            // Gọi phương thức từ DAO và truyền toàn bộ đối tượng detail
            return daoExportBill.updateProductQuantity(detail);
        } catch (SQLException e) {
            System.err.println("Error updating product quantity in BUS layer: " + e.getMessage());
            return false;
        }
    }
     
    public List<DTO_BillExportedDetail> getAllBillDetails() {
        try {
            return daoExportBill.getAllBillDetails();
        } catch (SQLException e) {
            e.printStackTrace();
               CustomDialog.showError("Data upload eror ! ");
            return Collections.emptyList();
        }
    }
    
    public List<DTO_BillExport> getAllBillExported() {
        try {
            return daoExportBill.getAllBillExported();
        } catch (SQLException e) {
            e.printStackTrace();
               CustomDialog.showError("Data upload eror ! ");
            return Collections.emptyList();
        }
    }
    
    public boolean exportToExcel(String filePath) {
        return daoExportBill.exportToExcel(filePath);
    }
    
    
     public List<DTO_BillExportedDetail> searchBillDetails(String searchType, String searchKeyword) {
        // Có thể thêm logic nghiệp vụ ở đây trước khi gọi DAO
        return daoExportBill.searchBillDetails(searchType, searchKeyword);
    }
    
    public String getWarranty(String productID) {
        try {
            return daoExportBill.getWarranty(productID);
        } catch (Exception e) { // broaden catch: underlying call may not throw SQLException directly
            e.printStackTrace();
            CustomDialog.showError("Data upload error ! ");
            return "12 tháng"; // Default warranty
        }
    }
    
    /**
     * Tính toán Start_Date và End_Date từ Warranty_Months của sản phẩm
     */
    private void calculateWarrantyDates(DTO_BillExportedDetail detail) {
        try {
            // Lấy Warranty_Months từ Product
            int warrantyMonths = daoExportBill.getWarrantyMonths(detail.getProductId());
            
            // Start_Date = Date_Exported
            Date startDate = detail.getDateExported();
            detail.setStartDate(startDate);
            
            // End_Date = Date_Exported + Warranty_Months
            LocalDate startLocalDate = startDate.toLocalDate();
            LocalDate endLocalDate = startLocalDate.plusMonths(warrantyMonths);
            Date endDate = Date.valueOf(endLocalDate);
            detail.setEndDate(endDate);
            
        } catch (Exception e) {
            e.printStackTrace();
            // Nếu có lỗi, sử dụng giá trị mặc định
            detail.setStartDate(detail.getDateExported());
            detail.setEndDate(detail.getDateExported());
        }
    }
    
    /**
     * Get all exported bills that are available for insurance creation
     * @return List of exported bills with insurance status
     */
    public List<DTO_BillExport> getAllAvailableExportBillsForInsurance() {
        try {
            return daoExportBill.getAllBillExported(); // Using existing method instead
        } catch (SQLException e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }
    
    /**
     * Get export bill details for insurance creation
     * @param invoiceNo Invoice number
     * @param adminID Admin ID
     * @return Export bill details
     */
    public DTO_BillExported getExportBillDetailsForInsurance(String invoiceNo, String adminID) {
        try {
            return daoExportBill.getExportBillDetailsByInvoice(invoiceNo, adminID);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Lấy danh sách thông tin bảo hành
     */
    public List<DTO_WarrantyInfo> getWarrantyInformation() {
        try {
            return daoExportBill.getWarrantyInformation();
        } catch (Exception e) {
            e.printStackTrace();
            CustomDialog.showError("Failed to get warranty information: " + e.getMessage());
            return Collections.emptyList();
        }
    }
    
    /**
     * Sửa lỗi số lượng (RESET và đồng bộ lại)
     */
    public boolean fixQuantityIssues() {
        try {
            return daoExportBill.fixQuantityIssues();
        } catch (Exception e) {
            e.printStackTrace();
            CustomDialog.showError("Failed to fix quantity issues: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * RESET và đồng bộ lại tất cả số lượng (sửa lỗi nhân đôi)
     */
    public boolean resetAndSyncAllQuantities() {
        try {
            return daoExportBill.fixQuantityIssues(); // Sử dụng method fix đã có
        } catch (Exception e) {
            e.printStackTrace();
            CustomDialog.showError("Failed to reset and sync quantities: " + e.getMessage());
            return false;
        }
    }
     
}