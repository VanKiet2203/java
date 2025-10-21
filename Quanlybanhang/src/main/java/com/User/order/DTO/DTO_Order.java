package com.User.order.DTO;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

public class DTO_Order {
    private String orderNo;
    private String customerID;
    private String cartID;
    private int totalQuantityProduct;
    private BigDecimal totalPrice;
    private String payment;
    private LocalDate dateOrder;
    private LocalTime timeOrder;

    // Constructor không tham số
    public DTO_Order() {}

    // Constructor đầy đủ
    public DTO_Order(String orderNo, String customerID, String cartID, int totalQuantityProduct,
                     BigDecimal totalPrice, String payment,
                     LocalDate dateOrder, LocalTime timeOrder) {
        this.orderNo = orderNo;
        this.customerID = customerID;
        this.cartID = cartID;
        this.totalQuantityProduct = totalQuantityProduct;
        this.totalPrice = totalPrice;
        this.payment = payment;
        this.dateOrder = dateOrder;
        this.timeOrder = timeOrder;
    }

    // Getter và Setter
    public String getOrderNo() {
        return orderNo;
    }

    public void setOrderNo(String orderNo) {
        this.orderNo = orderNo;
    }

    public String getCustomerID() {
        return customerID;
    }

    public void setCustomerID(String customerID) {
        this.customerID = customerID;
    }

    public String getCartID() {
        return cartID;
    }

    public void setCartID(String cartID) {
        this.cartID = cartID;
    }

    public int getTotalQuantityProduct() {
        return totalQuantityProduct;
    }

    public void setTotalQuantityProduct(int totalQuantityProduct) {
        this.totalQuantityProduct = totalQuantityProduct;
    }

    public BigDecimal getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(BigDecimal totalPrice) {
        this.totalPrice = totalPrice;
    }

    public String getPayment() {
        return payment;
    }

    public void setPayment(String payment) {
        this.payment = payment;
    }

    public LocalDate getDateOrder() {
        return dateOrder;
    }

    public void setDateOrder(LocalDate dateOrder) {
        this.dateOrder = dateOrder;
    }

    public LocalTime getTimeOrder() {
        return timeOrder;
    }

    public void setTimeOrder(LocalTime timeOrder) {
        this.timeOrder = timeOrder;
    }

    // toString()
    @Override
    public String toString() {
        return "DTO_Order{" +
                "orderNo='" + orderNo + '\'' +
                ", customerID='" + customerID + '\'' +
                ", cartID='" + cartID + '\'' +
                ", totalQuantityProduct=" + totalQuantityProduct +
                ", totalPrice=" + totalPrice +
                ", payment='" + payment + '\'' +
                ", dateOrder=" + dateOrder +
                ", timeOrder=" + timeOrder +
                '}';
    }
}
