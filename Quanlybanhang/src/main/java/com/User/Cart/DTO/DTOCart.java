package com.User.Cart.DTO;

public class DTOCart {
    private String cartID;
    private String customerID;
    private String productID;
    private int quantity;

    // Constructor mặc định
    public DTOCart() {
    }

    // Constructor đầy đủ
    public DTOCart(String customerID, String productID, int quantity) {
        this.customerID = customerID;
        this.productID = productID;
        this.quantity = quantity;
    }
    
    // Constructor với Cart_ID
    public DTOCart(String cartID, String customerID, String productID, int quantity) {
        this.cartID = cartID;
        this.customerID = customerID;
        this.productID = productID;
        this.quantity = quantity;
    }

    // Getter & Setter
    public String getCartID() {
        return cartID;
    }

    public void setCartID(String cartID) {
        this.cartID = cartID;
    }

    public String getCustomerID() {
        return customerID;
    }

    public void setCustomerID(String customerID) {
        this.customerID = customerID;
    }

    public String getProductID() {
        return productID;
    }

    public void setProductID(String productID) {
        this.productID = productID;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    // toString (giúp in ra dễ debug/log)
    @Override
    public String toString() {
        return "DTOCart{" +
                "cartID='" + cartID + '\'' +
                ", customerID='" + customerID + '\'' +
                ", productID='" + productID + '\'' +
                ", quantity=" + quantity +
                '}';
    }
}
