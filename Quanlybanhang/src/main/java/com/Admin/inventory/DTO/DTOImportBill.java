package com.Admin.inventory.DTO;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

public class DTOImportBill {
    private String invoiceNo;
    private String adminId;
    private String adminName;
    private int totalProduct;
    private BigDecimal totalPrice;
    private LocalDate dateImported;
    private LocalTime timeImported;
    
    // Constructors
    public DTOImportBill() {}
    
    public DTOImportBill(String invoiceNo, String adminId, String adminName, 
                        int totalProduct, BigDecimal totalPrice, 
                        LocalDate dateImported, LocalTime timeImported) {
        this.invoiceNo = invoiceNo;
        this.adminId = adminId;
        this.adminName = adminName;
        this.totalProduct = totalProduct;
        this.totalPrice = totalPrice;
        this.dateImported = dateImported;
        this.timeImported = timeImported;
    }
    
    // Getters and Setters
    public String getInvoiceNo() { return invoiceNo; }
    public void setInvoiceNo(String invoiceNo) { this.invoiceNo = invoiceNo; }
    
    public String getAdminId() { return adminId; }
    public void setAdminId(String adminId) { this.adminId = adminId; }
    
    public String getAdminName() { return adminName; }
    public void setAdminName(String adminName) { this.adminName = adminName; }
    
    public int getTotalProduct() { return totalProduct; }
    public void setTotalProduct(int totalProduct) { this.totalProduct = totalProduct; }
    
    public BigDecimal getTotalPrice() { return totalPrice; }
    public void setTotalPrice(BigDecimal totalPrice) { this.totalPrice = totalPrice; }
    
    public LocalDate getDateImported() { return dateImported; }
    public void setDateImported(LocalDate dateImported) { this.dateImported = dateImported; }
    
    public LocalTime getTimeImported() { return timeImported; }
    public void setTimeImported(LocalTime timeImported) { this.timeImported = timeImported; }
}
