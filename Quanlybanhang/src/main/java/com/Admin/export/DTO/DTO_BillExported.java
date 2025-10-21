package com.Admin.export.DTO;

public class DTO_BillExported {
    private String invoiceNo;
    private String adminId;
    private String customerId;
    private String orderNo; // Thêm Order_No
    private int totalProduct;
    private String description;
    private String promotionCode;

    public DTO_BillExported() {
    }

    public DTO_BillExported(String invoiceNo, String adminId, String customerId, int totalProduct) {
        this.invoiceNo = invoiceNo;
        this.adminId = adminId;
        this.customerId = customerId;
        this.totalProduct = totalProduct;
    }

    public DTO_BillExported(String invoiceNo, String adminId, String customerId, int totalProduct, String description, String promotionCode) {
        this.invoiceNo = invoiceNo;
        this.adminId = adminId;
        this.customerId = customerId;
        this.totalProduct = totalProduct;
        this.description = description;
        this.promotionCode = promotionCode;
    }

    public String getInvoiceNo() {
        return invoiceNo;
    }

    public void setInvoiceNo(String invoiceNo) {
        this.invoiceNo = invoiceNo;
    }

    public String getAdminId() {
        return adminId;
    }

    public void setAdminId(String adminId) {
        this.adminId = adminId;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public String getOrderNo() {
        return orderNo;
    }

    public void setOrderNo(String orderNo) {
        this.orderNo = orderNo;
    }

    public int getTotalProduct() {
        return totalProduct;
    }

    public void setTotalProduct(int totalProduct) {
        this.totalProduct = totalProduct;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPromotionCode() {
        return promotionCode;
    }

    public void setPromotionCode(String promotionCode) {
        this.promotionCode = promotionCode;
    }

    @Override
    public String toString() {
        return "DTO_BillExported{" +
                "invoiceNo='" + invoiceNo + '\'' +
                ", adminId='" + adminId + '\'' +
                ", customerId='" + customerId + '\'' +
                ", totalProduct=" + totalProduct +
                ", description='" + description + '\'' +
                ", promotionCode='" + promotionCode + '\'' +
                '}';
    }
}
