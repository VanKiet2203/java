package com.Admin.export.DTO;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Time;

public class DTO_BillExportedDetail {
    private String invoiceNo;
    private String adminId;
    private String customerId;
    private String productId;
    private BigDecimal unitPrice;
    private int quantity;
    private BigDecimal discountPercent;
    private BigDecimal totalPriceBefore;
    private BigDecimal totalPriceAfter;
    private Date dateExported;
    private Time timeExported;

    public DTO_BillExportedDetail() {}

    public DTO_BillExportedDetail(String invoiceNo, String adminId, String customerId, String productId,
                                  BigDecimal unitPrice, int quantity, BigDecimal discountPercent,
                                  BigDecimal totalPriceBefore, BigDecimal totalPriceAfter,
                                  Date dateExported, Time timeExported) {
        this.invoiceNo = invoiceNo;
        this.adminId = adminId;
        this.customerId = customerId;
        this.productId = productId;
        this.unitPrice = unitPrice;
        this.quantity = quantity;
        this.discountPercent = discountPercent;
        this.totalPriceBefore = totalPriceBefore;
        this.totalPriceAfter = totalPriceAfter;
        this.dateExported = dateExported;
        this.timeExported = timeExported;
    }

    public String getInvoiceNo() { return invoiceNo; }
    public void setInvoiceNo(String invoiceNo) { this.invoiceNo = invoiceNo; }
    public String getAdminId() { return adminId; }
    public void setAdminId(String adminId) { this.adminId = adminId; }
    public String getCustomerId() { return customerId; }
    public void setCustomerId(String customerId) { this.customerId = customerId; }
    public String getProductId() { return productId; }
    public void setProductId(String productId) { this.productId = productId; }
    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public BigDecimal getDiscountPercent() { return discountPercent; }
    public void setDiscountPercent(BigDecimal discountPercent) { this.discountPercent = discountPercent; }
    public BigDecimal getTotalPriceBefore() { return totalPriceBefore; }
    public void setTotalPriceBefore(BigDecimal totalPriceBefore) { this.totalPriceBefore = totalPriceBefore; }
    public BigDecimal getTotalPriceAfter() { return totalPriceAfter; }
    public void setTotalPriceAfter(BigDecimal totalPriceAfter) { this.totalPriceAfter = totalPriceAfter; }
    public Date getDateExported() { return dateExported; }
    public void setDateExported(Date dateExported) { this.dateExported = dateExported; }
    public Time getTimeExported() { return timeExported; }
    public void setTimeExported(Time timeExported) { this.timeExported = timeExported; }
}