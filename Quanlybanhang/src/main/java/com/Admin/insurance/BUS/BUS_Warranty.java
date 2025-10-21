package com.Admin.insurance.BUS;

import com.Admin.insurance.DAO.DAO_Warranty;
import com.Admin.insurance.DTO.DTO_Insurance;
import com.Admin.insurance.DTO.DTO_InsuranceDetails;
import java.sql.SQLException;
import java.util.List;

public class BUS_Warranty {
    private DAO_Warranty daoWarranty;

    // Constructor để khởi tạo DAO
    public BUS_Warranty() {
        daoWarranty = new DAO_Warranty();
    }

    // Phương thức gọi DAO để thêm hóa đơn bảo hành
    public boolean insertBillWarranty(DTO_Insurance insurance) {
        try {
            return daoWarranty.insertBillWarranty(insurance);
        } catch (SQLException e) {
            System.err.println("Error inserting warranty bill: " + e.getMessage());
            return false; // Trả về `false` nếu có lỗi
        }
    }

    // Phương thức gọi DAO để thêm chi tiết hóa đơn bảo hành
    public boolean insertBillWarrantyDetails(DTO_InsuranceDetails insuranceDetails) {
        try {
            return daoWarranty.insertBillWarrantyDetails(insuranceDetails);
        } catch (SQLException e) {
            System.err.println("Error inserting warranty bill details: " + e.getMessage());
            return false; // Trả về `false` nếu có lỗi
        }
    }
    
     public List<DTO_InsuranceDetails> getAllInsuranceDetails(){
         try {
            return daoWarranty.getAllInsuranceDetails();
        } catch (SQLException e) {
            System.err.println("Error inserting warranty bill details: " + e.getMessage());
            return null;
        }
     }
     
     public List<DTO_InsuranceDetails> searchInsuranceDetails(String searchType, String keyword) {
        try {
            return daoWarranty.searchInsuranceDetails(searchType, keyword);
        } catch (SQLException e) {
            return null; // Hoặc có thể trả về danh sách rỗng: return new ArrayList<>()
        }
    }
     
     public boolean exportToExcel(String filePath) {
         return daoWarranty.exportToExcel(filePath);
     }
     
     /**
      * Get all insurance records with export bill information
      * @return List of insurance records
      */
     public List<DTO_Insurance> getAllInsuranceWithExportInfo() {
         try {
             return daoWarranty.getAllInsuranceWithExportInfo();
         } catch (SQLException e) {
             System.err.println("Error getting insurance records: " + e.getMessage());
             return null;
         }
     }
     
     /**
      * Get all insurance records
      * @return List of insurance records
      */
     public List<DTO_Insurance> getAllInsurance() {
         try {
             return daoWarranty.getAllInsurance();
         } catch (SQLException e) {
             System.err.println("Error getting insurance records: " + e.getMessage());
             return null;
         }
     }
     
     /**
      * Get insurance by insurance number
      * @param insuranceNo Insurance number
      * @return Insurance record
      */
     public DTO_Insurance getInsuranceByNo(String insuranceNo) {
         try {
             return daoWarranty.getInsuranceByNo(insuranceNo);
         } catch (SQLException e) {
             System.err.println("Error getting insurance by number: " + e.getMessage());
             return null;
         }
     }
     
     /**
      * Get insurance details by insurance number
      * @param insuranceNo Insurance number
      * @return List of insurance details
      */
     public List<DTO_InsuranceDetails> getInsuranceDetailsByNo(String insuranceNo) {
         try {
             return daoWarranty.getInsuranceDetailsByNo(insuranceNo);
         } catch (SQLException e) {
             System.err.println("Error getting insurance details: " + e.getMessage());
             return null;
         }
     }
     
     /**
      * Create insurance with multiple products
      * @param insurance Main insurance record
      * @param detailsList List of insurance details for each product
      * @return true if successful
      */
     public boolean createInsuranceWithProducts(DTO_Insurance insurance, List<DTO_InsuranceDetails> detailsList) {
         try {
             // Insert main insurance record
             boolean insuranceInserted = daoWarranty.insertBillWarranty(insurance);
             if (!insuranceInserted) {
                 return false;
             }
             
             // Insert details for each product
             for (DTO_InsuranceDetails detail : detailsList) {
                 boolean detailInserted = daoWarranty.insertBillWarrantyDetails(detail);
                 if (!detailInserted) {
                     // TODO: Rollback insurance record if needed
                     return false;
                 }
             }
             
             return true;
         } catch (SQLException e) {
             System.err.println("Error creating insurance with products: " + e.getMessage());
             return false;
         }
     }
     
     /**
      * Search insurance records
      * @param searchType Type of search
      * @param keyword Search keyword
      * @return List of matching insurance records
      */
     public List<DTO_Insurance> searchInsurance(String searchType, String keyword) {
         try {
             return daoWarranty.searchInsurance(searchType, keyword);
         } catch (SQLException e) {
             System.err.println("Error searching insurance: " + e.getMessage());
             return null;
         }
     }
     
     /**
      * Get insurance details by invoice number
      * @param invoiceNo Invoice number
      * @param adminId Admin ID
      * @return List of insurance details
      */
     public List<DTO_InsuranceDetails> getInsuranceDetailsByInvoice(String invoiceNo, String adminId) {
         try {
             return daoWarranty.getInsuranceDetailsByInvoice(invoiceNo, adminId);
         } catch (SQLException e) {
             System.err.println("Error getting insurance details by invoice: " + e.getMessage());
             return null;
         }
     }
}
    
