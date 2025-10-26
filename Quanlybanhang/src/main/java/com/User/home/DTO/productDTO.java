package com.User.home.DTO;

import java.math.BigDecimal;

public class productDTO {

    private String productID;
    private String productName;
    private String color;
    private String batteryCapacity;
    private String speed;
    private BigDecimal price;
    private BigDecimal listPriceBefore;
    private BigDecimal listPriceAfter;
    private int quantity;
    private int warrantyMonths;
    private String status;
    private String categoryID;
    private String supID;
    private String warehouseItemID;
    private String image;

    // Constructor mặc định
    public productDTO() {}

    // Constructor với tất cả các tham số
    public productDTO(String productID, String productName, String color, String batteryCapacity, String speed,
                      BigDecimal price, BigDecimal listPriceBefore, BigDecimal listPriceAfter, int quantity, 
                      int warrantyMonths, String status, String categoryID, String supID, 
                      String warehouseItemID, String image) {
        this.productID = productID;
        this.productName = productName;
        this.color = color;
        this.batteryCapacity = batteryCapacity;
        this.speed = speed;
        this.price = price;
        this.listPriceBefore = listPriceBefore;
        this.listPriceAfter = listPriceAfter;
        this.quantity = quantity;
        this.warrantyMonths = warrantyMonths;
        this.status = status;
        this.categoryID = categoryID;
        this.supID = supID;
        this.warehouseItemID = warehouseItemID;
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

    public int getWarrantyMonths() {
        return warrantyMonths;
    }

    public void setWarrantyMonths(int warrantyMonths) {
        this.warrantyMonths = warrantyMonths;
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

    public String getSupID() {
        return supID;
    }

    public void setSupID(String supID) {
        this.supID = supID;
    }

    public String getWarehouseItemID() {
        return warehouseItemID;
    }

    public void setWarehouseItemID(String warehouseItemID) {
        this.warehouseItemID = warehouseItemID;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    @Override
    public String toString() {
        return "productDTO{" +
                "productID='" + productID + '\'' +
                ", productName='" + productName + '\'' +
                ", color='" + color + '\'' +
                ", batteryCapacity='" + batteryCapacity + '\'' +
                ", speed='" + speed + '\'' +
                ", price=" + price +
                ", listPriceBefore=" + listPriceBefore +
                ", listPriceAfter=" + listPriceAfter +
                ", quantity=" + quantity +
                ", warrantyMonths=" + warrantyMonths +
                ", status='" + status + '\'' +
                ", categoryID='" + categoryID + '\'' +
                ", supID='" + supID + '\'' +
                ", warehouseItemID='" + warehouseItemID + '\'' +
                ", image='" + image + '\'' +
                '}';
    }
}
