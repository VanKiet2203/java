package com.Admin.inventory.DTO;

import java.math.BigDecimal;
import java.sql.Date;

public class DTOInventory {
    private String warehouseItemId;
    private String productName;
    private String categoryId;
    private String supId;
    private int quantityStock;
    private BigDecimal unitPriceImport;
    private Date createdDate;
    private String createdTime;
    
    public DTOInventory() {
    }
    
    public DTOInventory(String warehouseItemId, String productName, String categoryId, 
                        String supId, int quantityStock, BigDecimal unitPriceImport, 
                        Date createdDate, String createdTime) {
        this.warehouseItemId = warehouseItemId;
        this.productName = productName;
        this.categoryId = categoryId;
        this.supId = supId;
        this.quantityStock = quantityStock;
        this.unitPriceImport = unitPriceImport;
        this.createdDate = createdDate;
        this.createdTime = createdTime;
    }
    
    // Getters and Setters
    public String getWarehouseItemId() {
        return warehouseItemId;
    }
    
    public void setWarehouseItemId(String warehouseItemId) {
        this.warehouseItemId = warehouseItemId;
    }
    
    public String getProductName() {
        return productName;
    }
    
    public void setProductName(String productName) {
        this.productName = productName;
    }
    
    public String getCategoryId() {
        return categoryId;
    }
    
    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }
    
    public String getSupId() {
        return supId;
    }
    
    public void setSupId(String supId) {
        this.supId = supId;
    }
    
    public int getQuantityStock() {
        return quantityStock;
    }
    
    public void setQuantityStock(int quantityStock) {
        this.quantityStock = quantityStock;
    }
    
    public BigDecimal getUnitPriceImport() {
        return unitPriceImport;
    }
    
    public void setUnitPriceImport(BigDecimal unitPriceImport) {
        this.unitPriceImport = unitPriceImport;
    }
    
    public Date getCreatedDate() {
        return createdDate;
    }
    
    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }
    
    public String getCreatedTime() {
        return createdTime;
    }
    
    public void setCreatedTime(String createdTime) {
        this.createdTime = createdTime;
    }
    
    @Override
    public String toString() {
        return "DTOInventory{" +
                "warehouseItemId='" + warehouseItemId + '\'' +
                ", productName='" + productName + '\'' +
                ", categoryId='" + categoryId + '\'' +
                ", supId='" + supId + '\'' +
                ", quantityStock=" + quantityStock +
                ", unitPriceImport=" + unitPriceImport +
                ", createdDate=" + createdDate +
                ", createdTime='" + createdTime + '\'' +
                '}';
    }
}