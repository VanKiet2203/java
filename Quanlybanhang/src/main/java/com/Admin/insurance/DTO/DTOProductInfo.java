package com.Admin.insurance.DTO;

public class DTOProductInfo {
    private String productId;
    private String productName;
    private String categoryId;
    private String supplierId;
    private String brand;
    private String warrantyPeriod;

    // Constructor
    public DTOProductInfo(String productId, String productName, 
                          String categoryId, String supplierId) {
        this.productId = productId;
        this.productName = productName;
        this.categoryId = categoryId;
        this.supplierId = supplierId;
    }
    
    // Constructor with all fields
    public DTOProductInfo(String productId, String productName, 
                          String categoryId, String supplierId, String brand, String warrantyPeriod) {
        this.productId = productId;
        this.productName = productName;
        this.categoryId = categoryId;
        this.supplierId = supplierId;
        this.brand = brand;
        this.warrantyPeriod = warrantyPeriod;
    }

    // Getters and Setters
    public String getProductId() { return productId; }
    public void setProductId(String productId) { this.productId = productId; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public String getCategoryId() { return categoryId; }
    public void setCategoryId(String categoryId) { this.categoryId = categoryId; }

    public String getSupplierId() { return supplierId; }
    public void setSupplierId(String supplierId) { this.supplierId = supplierId; }
    
    public String getBrand() { return brand; }
    public void setBrand(String brand) { this.brand = brand; }
    
    public String getWarrantyPeriod() { return warrantyPeriod; }
    public void setWarrantyPeriod(String warrantyPeriod) { this.warrantyPeriod = warrantyPeriod; }

    // Optional: toString() for easier debugging
    @Override
    public String toString() {
        return "DTOProductInfo{" +
                "productId='" + productId + '\'' +
                ", productName='" + productName + '\'' +
                ", categoryId='" + categoryId + '\'' +
                ", supplierId='" + supplierId + '\'' +
                ", brand='" + brand + '\'' +
                ", warrantyPeriod='" + warrantyPeriod + '\'' +
                '}';
    }
}
