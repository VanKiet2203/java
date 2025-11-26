package com.Admin.inventory.DTO;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * DTO for Bill_Imported_Details table
 * Represents details of imported goods linked to warehouse items
 */
public class DTOImportBillDetails {
    private String invoiceNo;
    private String adminId;
    private String warehouseItemId;  // Changed from productId - references Product_Stock
    private String productName;      // From Product_Stock.Product_Name
    private int quantity;
    private BigDecimal unitPrice;
    private BigDecimal totalPrice;
    private LocalDate dateImported;
    private LocalTime timeImported;
    
    // Constructors
    public DTOImportBillDetails() {}
    
    public DTOImportBillDetails(String invoiceNo, String adminId, String warehouseItemId, 
                              String productName, int quantity, BigDecimal unitPrice, 
                              BigDecimal totalPrice, LocalDate dateImported, LocalTime timeImported) {
        this.invoiceNo = invoiceNo;
        this.adminId = adminId;
        this.warehouseItemId = warehouseItemId;
        this.productName = productName;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.totalPrice = totalPrice;
        this.dateImported = dateImported;
        this.timeImported = timeImported;
    }
    
    // Getters and Setters
    public String getInvoiceNo() { return invoiceNo; }
    public void setInvoiceNo(String invoiceNo) { this.invoiceNo = invoiceNo; }
    
    public String getAdminId() { return adminId; }
    public void setAdminId(String adminId) { this.adminId = adminId; }
    
    public String getWarehouseItemId() { return warehouseItemId; }
    public void setWarehouseItemId(String warehouseItemId) { this.warehouseItemId = warehouseItemId; }
    
    // Legacy compatibility
    public String getProductId() { return warehouseItemId; }
    public void setProductId(String productId) { this.warehouseItemId = productId; }
    
    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }
    
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    
    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }
    
    public BigDecimal getTotalPrice() { return totalPrice; }
    public void setTotalPrice(BigDecimal totalPrice) { this.totalPrice = totalPrice; }
    
    public LocalDate getDateImported() { return dateImported; }
    public void setDateImported(LocalDate dateImported) { this.dateImported = dateImported; }
    
    public LocalTime getTimeImported() { return timeImported; }
    public void setTimeImported(LocalTime timeImported) { this.timeImported = timeImported; }
}
