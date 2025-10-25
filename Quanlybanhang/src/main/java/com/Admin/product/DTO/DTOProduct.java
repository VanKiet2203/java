package com.Admin.product.DTO;

import java.math.BigDecimal;

public class DTOProduct {
    private String productId;
    private String productName;
    private String color;
    private String speed;
    private String batteryCapacity;
    private int quantity;
    private String categoryId;
    private String supId;
    private String image;
    private BigDecimal price;
    private BigDecimal listPriceBefore;
    private BigDecimal listPriceAfter;
    private int warrantyMonths;

    // Constructor
    public DTOProduct() {}

    public DTOProduct(String productId, String productName, String color,
                     String speed, String batteryCapacity, int quantity,
                     String categoryId, String supId, String image,
                     BigDecimal price, BigDecimal listPriceBefore, BigDecimal listPriceAfter, int warrantyMonths) {
        this.productId = productId;
        this.productName = productName;
        this.color = color;
        this.speed = speed;
        this.batteryCapacity = batteryCapacity;
        this.quantity = quantity;
        this.categoryId = categoryId;
        this.supId = supId;
        this.image = image;
        this.price = price;
        this.listPriceBefore = listPriceBefore;
        this.listPriceAfter = listPriceAfter;
        this.warrantyMonths = warrantyMonths;
    }

    // Convenience constructor with Sup_ID and no list prices (defaults list prices to price)
    public DTOProduct(String productId, String productName, String color,
                      String speed, String batteryCapacity, int quantity,
                      String categoryId, String supId, String image, BigDecimal price, int warrantyMonths) {
        this.productId = productId;
        this.productName = productName;
        this.color = color;
        this.speed = speed;
        this.batteryCapacity = batteryCapacity;
        this.quantity = quantity;
        this.categoryId = categoryId;
        this.supId = supId;
        this.image = image;
        this.price = price;
        this.listPriceBefore = price;
        this.listPriceAfter = price;
        this.warrantyMonths = warrantyMonths;
    }

    // Backward-compatible constructor (old usage)
    public DTOProduct(String productId, String productName, String color,
                      String speed, String batteryCapacity, int quantity,
                      String categoryId, String image, double price) {
        this.productId = productId;
        this.productName = productName;
        this.color = color;
        this.speed = speed;
        this.batteryCapacity = batteryCapacity;
        this.quantity = quantity;
        this.categoryId = categoryId;
        this.image = image;
        this.price = BigDecimal.valueOf(price);
        // Default list prices to current price when not provided
        this.listPriceBefore = this.price;
        this.listPriceAfter = this.price;
        this.warrantyMonths = 12; // Default warranty months
    }

    // Getters and setters
    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getSpeed() {
        return speed;
    }

    public void setSpeed(String speed) {
        this.speed = speed;
    }

    public String getBatteryCapacity() {
        return batteryCapacity;
    }

    public void setBatteryCapacity(String batteryCapacity) {
        this.batteryCapacity = batteryCapacity;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
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

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public BigDecimal getListPriceBefore() {
        return listPriceBefore;
    }

    public void setListPriceBefore(BigDecimal listPriceBefore) {
        this.listPriceBefore = listPriceBefore;
    }

    public BigDecimal getListPriceAfter() {
        return listPriceAfter;
    }

    public void setListPriceAfter(BigDecimal listPriceAfter) {
        this.listPriceAfter = listPriceAfter;
    }

    public int getWarrantyMonths() {
        return warrantyMonths;
    }

    public void setWarrantyMonths(int warrantyMonths) {
        this.warrantyMonths = warrantyMonths;
    }

    @Override
    public String toString() {
        return "DTOProduct{" +
                "productId='" + productId + '\'' +
                ", productName='" + productName + '\'' +
                ", color='" + color + '\'' +
                ", speed='" + speed + '\'' +
                ", batteryCapacity='" + batteryCapacity + '\'' +
                ", quantity=" + quantity +
                ", categoryId='" + categoryId + '\'' +
                ", supId='" + supId + '\'' +
                ", image='" + image + '\'' +
                ", price=" + price +
                ", listPriceBefore=" + listPriceBefore +
                ", listPriceAfter=" + listPriceAfter +
                ", warrantyMonths=" + warrantyMonths +
                '}';
    }
}
