package com.Admin.export.DTO;

import java.sql.Date;

public class DTO_WarrantyInfo {
    private String invoiceNo;
    private String adminId;
    private String customerId;
    private String customerName;
    private String productId;
    private String productName;
    private int soldQuantity;
    private Date dateExported;
    private Date startDate;
    private Date endDate;
    private String warrantyStatus;
    private int warrantyMonths;
    
    public DTO_WarrantyInfo() {}
    
    public DTO_WarrantyInfo(String invoiceNo, String adminId, String customerId, String customerName,
                           String productId, String productName, int soldQuantity, Date dateExported,
                           Date startDate, Date endDate, String warrantyStatus, int warrantyMonths) {
        this.invoiceNo = invoiceNo;
        this.adminId = adminId;
        this.customerId = customerId;
        this.customerName = customerName;
        this.productId = productId;
        this.productName = productName;
        this.soldQuantity = soldQuantity;
        this.dateExported = dateExported;
        this.startDate = startDate;
        this.endDate = endDate;
        this.warrantyStatus = warrantyStatus;
        this.warrantyMonths = warrantyMonths;
    }
    
    // Getters and Setters
    public String getInvoiceNo() { return invoiceNo; }
    public void setInvoiceNo(String invoiceNo) { this.invoiceNo = invoiceNo; }
    
    public String getAdminId() { return adminId; }
    public void setAdminId(String adminId) { this.adminId = adminId; }
    
    public String getCustomerId() { return customerId; }
    public void setCustomerId(String customerId) { this.customerId = customerId; }
    
    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }
    
    public String getProductId() { return productId; }
    public void setProductId(String productId) { this.productId = productId; }
    
    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }
    
    public int getSoldQuantity() { return soldQuantity; }
    public void setSoldQuantity(int soldQuantity) { this.soldQuantity = soldQuantity; }
    
    public Date getDateExported() { return dateExported; }
    public void setDateExported(Date dateExported) { this.dateExported = dateExported; }
    
    public Date getStartDate() { return startDate; }
    public void setStartDate(Date startDate) { this.startDate = startDate; }
    
    public Date getEndDate() { return endDate; }
    public void setEndDate(Date endDate) { this.endDate = endDate; }
    
    public String getWarrantyStatus() { return warrantyStatus; }
    public void setWarrantyStatus(String warrantyStatus) { this.warrantyStatus = warrantyStatus; }
    
    public int getWarrantyMonths() { return warrantyMonths; }
    public void setWarrantyMonths(int warrantyMonths) { this.warrantyMonths = warrantyMonths; }
    
    @Override
    public String toString() {
        return "DTO_WarrantyInfo{" +
                "invoiceNo='" + invoiceNo + '\'' +
                ", customerName='" + customerName + '\'' +
                ", productName='" + productName + '\'' +
                ", warrantyStatus='" + warrantyStatus + '\'' +
                '}';
    }
}
