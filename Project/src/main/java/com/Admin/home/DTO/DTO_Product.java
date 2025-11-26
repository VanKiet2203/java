package com.Admin.home.DTO;

import java.math.BigDecimal;

public class DTO_Product {
    private String productID;
    private String productName;
    private String color;
    private String speed;
    private String batteryCapacity;
    private BigDecimal price;
    private int quantity;
    private String categoryID;
    private String image;

    public DTO_Product() {}

    public DTO_Product(String productID, String productName, String color, String speed,
                       String batteryCapacity, BigDecimal price, int quantity,
                       String categoryID, String image) {
        this.productID = productID;
        this.productName = productName;
        this.color = color;
        this.speed = speed;
        this.batteryCapacity = batteryCapacity;
        this.price = price;
        this.quantity = quantity;
        this.categoryID = categoryID;
        this.image = image;
    }

    public String getProductID() { return productID; }
    public void setProductID(String productID) { this.productID = productID; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }

    public String getSpeed() { return speed; }
    public void setSpeed(String speed) { this.speed = speed; }

    public String getBatteryCapacity() { return batteryCapacity; }
    public void setBatteryCapacity(String batteryCapacity) { this.batteryCapacity = batteryCapacity; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public String getCategoryID() { return categoryID; }
    public void setCategoryID(String categoryID) { this.categoryID = categoryID; }

    public String getImage() { return image; }
    public void setImage(String image) { this.image = image; }

    @Override
    public String toString() {
        return "DTO_Product{" +
                "productID='" + productID + '\'' +
                ", productName='" + productName + '\'' +
                ", color='" + color + '\'' +
                ", speed='" + speed + '\'' +
                ", batteryCapacity='" + batteryCapacity + '\'' +
                ", price=" + price +
                ", quantity=" + quantity +
                ", categoryID='" + categoryID + '\'' +
                ", image='" + image + '\'' +
                '}';
    }
}
