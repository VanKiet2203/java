package com.User.home.DTO;

import java.math.BigDecimal;

public class productDTO {

    private String productID;
    private String productName;
    private String color;           // Thay thế cpu
    private String batteryCapacity; // Thay thế ram  
    private String speed;           // Thay thế graphicsCard
    private String operatingSystem;
    private BigDecimal price;
    private int quantity;
    private String warrantyPeriod;
    private String status;
    private String categoryID;
    private String brand;
    private String image;

    // Constructor mặc định
    public productDTO() {}

    // Constructor với tất cả các tham số
    public productDTO(String productID, String productName, String color, String batteryCapacity, String speed,
                      String operatingSystem, BigDecimal price, int quantity, String warrantyPeriod, 
                      String status, String categoryID, String image) {
        this.productID = productID;
        this.productName = productName;
        this.color = color;
        this.batteryCapacity = batteryCapacity;
        this.speed = speed;
        this.operatingSystem = operatingSystem;
        this.price = price;
        this.quantity = quantity;
        this.warrantyPeriod = warrantyPeriod;
        this.status = status;
        this.categoryID = categoryID;
        this.image = image;
    }

    // Getter and Setter methods

    public String getProductID() {
        return productID;
    }

    public void setProductID(String productID) {
        this.productID = productID;
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

    public String getBatteryCapacity() {
        return batteryCapacity;
    }

    public void setBatteryCapacity(String batteryCapacity) {
        this.batteryCapacity = batteryCapacity;
    }

    public String getSpeed() {
        return speed;
    }

    public void setSpeed(String speed) {
        this.speed = speed;
    }

    public String getOperatingSystem() {
        return operatingSystem;
    }

    public void setOperatingSystem(String operatingSystem) {
        this.operatingSystem = operatingSystem;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String getWarrantyPeriod() {
        return warrantyPeriod;
    }

    public void setWarrantyPeriod(String warrantyPeriod) {
        this.warrantyPeriod = warrantyPeriod;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCategoryID() {
        return categoryID;
    }

    public void setCategoryID(String categoryID) {
        this.categoryID = categoryID;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    @Override
    public String toString() {
        return "DTOProduct{" +
                "productID='" + productID + '\'' +
                ", productName='" + productName + '\'' +
                ", color='" + color + '\'' +
                ", batteryCapacity='" + batteryCapacity + '\'' +
                ", speed='" + speed + '\'' +
                ", operatingSystem='" + operatingSystem + '\'' +
                ", price=" + price +
                ", quantity=" + quantity +
                ", warrantyPeriod='" + warrantyPeriod + '\'' +
                ", status='" + status + '\'' +
                ", categoryID='" + categoryID + '\'' +
                ", brand='" + brand + '\'' +
                ", image='" + image + '\'' +
                '}';
    }
}
