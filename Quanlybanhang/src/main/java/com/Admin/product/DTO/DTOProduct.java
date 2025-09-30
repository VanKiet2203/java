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
    private String image;
    private double price;

    // Constructor
    public DTOProduct() {}

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
        this.price = price;
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

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
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
                ", image='" + image + '\'' +
                ", price=" + price +
                '}';
    }
}
