
package com.User.Cart.BUS;
import com.User.Cart.DTO.DTOCart;
import com.User.Cart.DAO.DAOCart;
import java.util.ArrayList;

public class BUSCart {
    private DAOCart daoCart = new DAOCart();
    
    public boolean addToCart(DTOCart cartItem) {
        System.out.println("=== BUS CART ADD ===");
        System.out.println("Customer: " + cartItem.getCustomerID());
        System.out.println("Product: " + cartItem.getProductID());
        System.out.println("Quantity: " + cartItem.getQuantity());
        
        // Kiểm tra số lượng hợp lệ và không vượt tồn kho hiện tại
        if (cartItem.getQuantity() <= 0) {
            System.out.println("❌ Invalid quantity: " + cartItem.getQuantity());
            return false;
        }
        
        int currentStock = daoCart.getCurrentStock(cartItem.getProductID());
        if (cartItem.getQuantity() > currentStock) {
            System.out.println("❌ Quantity exceeds stock: " + cartItem.getQuantity() + " > " + currentStock);
            return false;
        }

        // Kiểm tra sản phẩm đã có trong giỏ chưa
        if (daoCart.isProductInCart(cartItem.getCustomerID(), cartItem.getProductID())) {
            System.out.println("🔄 Product already in cart, updating...");
            return daoCart.updateCartItem(cartItem);
        } else {
            System.out.println("➕ Adding new product to cart...");
            return daoCart.addToCart(cartItem);
        }
    }
    
    public ArrayList<DTOCart> getCartItemsByCustomer(String customerID) {
        return daoCart.getCartItemsByCustomer(customerID);
    }
    
    public boolean removeFromCart(String customerId, String productId) {
        return daoCart.deleteCartItem(customerId, productId);
    }
    
    public boolean clearCart(String customerID) {
        return daoCart.clearCart(customerID);
    }
}