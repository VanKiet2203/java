package com.Admin.inventory.DTO;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * DTO for Warehouse Stock items (Product_Stock table)
 * Represents items in warehouse that may or may not be added to Product catalog
 */
public class DTOInventory {
    private String warehouseItemId;      // Warehouse_Item_ID - Primary Key
    private String productName;          // Product_Name
    private String categoryId;           // Category_ID
    private String brandId;              // Sup_ID (Brand/Supplier)
    private String color;                // Color
    private String speed;                // Speed
    private String batteryCapacity;      // Battery_Capacity
    private int quantityInStock;         // Quantity_Stock
    private BigDecimal unitPriceImport;  // Unit_Price_Import (not public to customers)
    private LocalDate createdDate;       // Created_Date
    private LocalTime createdTime;       // Created_Time
    private boolean isInProduct;         // Is_In_Product (has been added to Product table?)
    
    // Legacy field for compatibility (can be removed later)
    private String image;
    
    // Constructors
    public DTOInventory() {}
    
    public DTOInventory(String warehouseItemId, String productName, String categoryId, 
                       String brandId, int quantityInStock, BigDecimal unitPriceImport) {
        this.warehouseItemId = warehouseItemId;
        this.productName = productName;
        this.categoryId = categoryId;
        this.brandId = brandId;
        this.quantityInStock = quantityInStock;
        this.unitPriceImport = unitPriceImport;
        this.isInProduct = false;
    }
    
    // Full constructor
    public DTOInventory(String warehouseItemId, String productName, String categoryId, 
                       String brandId, String color, String speed, String batteryCapacity,
                       int quantityInStock, BigDecimal unitPriceImport, 
                       LocalDate createdDate, LocalTime createdTime, boolean isInProduct) {
        this.warehouseItemId = warehouseItemId;
        this.productName = productName;
        this.categoryId = categoryId;
        this.brandId = brandId;
        this.color = color;
        this.speed = speed;
        this.batteryCapacity = batteryCapacity;
        this.quantityInStock = quantityInStock;
        this.unitPriceImport = unitPriceImport;
        this.createdDate = createdDate;
        this.createdTime = createdTime;
        this.isInProduct = isInProduct;
    }
    
    // Getters and Setters
    public String getWarehouseItemId() { return warehouseItemId; }
    public void setWarehouseItemId(String warehouseItemId) { this.warehouseItemId = warehouseItemId; }
    
    // Legacy compatibility
    public String getProductId() { return warehouseItemId; }
    public void setProductId(String productId) { this.warehouseItemId = productId; }
    
    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }
    
    public String getCategoryId() { return categoryId; }
    public void setCategoryId(String categoryId) { this.categoryId = categoryId; }
    
    public String getBrandId() { return brandId; }
    public void setBrandId(String brandId) { this.brandId = brandId; }
    
    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }
    
    public String getSpeed() { return speed; }
    public void setSpeed(String speed) { this.speed = speed; }
    
    public String getBatteryCapacity() { return batteryCapacity; }
    public void setBatteryCapacity(String batteryCapacity) { this.batteryCapacity = batteryCapacity; }
    
    public int getQuantityInStock() { return quantityInStock; }
    public void setQuantityInStock(int quantityInStock) { this.quantityInStock = quantityInStock; }
    
    public BigDecimal getUnitPriceImport() { return unitPriceImport; }
    public void setUnitPriceImport(BigDecimal unitPriceImport) { this.unitPriceImport = unitPriceImport; }
    
    // Legacy compatibility - price refers to import price in warehouse context
    public BigDecimal getPrice() { return unitPriceImport; }
    public void setPrice(BigDecimal price) { this.unitPriceImport = price; }
    
    public LocalDate getCreatedDate() { return createdDate; }
    public void setCreatedDate(LocalDate createdDate) { this.createdDate = createdDate; }
    
    public LocalTime getCreatedTime() { return createdTime; }
    public void setCreatedTime(LocalTime createdTime) { this.createdTime = createdTime; }
    
    public boolean isInProduct() { return isInProduct; }
    public void setInProduct(boolean inProduct) { isInProduct = inProduct; }
    
    public String getImage() { return image; }
    public void setImage(String image) { this.image = image; }
}
