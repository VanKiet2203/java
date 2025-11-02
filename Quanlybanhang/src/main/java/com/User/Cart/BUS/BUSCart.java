
package com.User.Cart.BUS;
import com.User.Cart.DTO.DTOCart;
import com.User.Cart.DAO.DAOCart;
import com.ComponentandDatabase.Components.CustomDialog;
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
            CustomDialog.showError("Quantity must be greater than 0!");
            return false;
        }
        
        int currentStock = daoCart.getCurrentStock(cartItem.getProductID());
        System.out.println("Current Stock: " + currentStock);
        
        // Kiểm tra sản phẩm đã có trong giỏ chưa để tính tổng số lượng
        if (daoCart.isProductInCart(cartItem.getCustomerID(), cartItem.getProductID())) {
            System.out.println("🔄 Product already in cart, checking total quantity...");
            // Lấy số lượng hiện tại trong cart
            int existingQuantity = daoCart.getQuantityInCart(cartItem.getCustomerID(), cartItem.getProductID());
            int newTotalQuantity = existingQuantity + cartItem.getQuantity();
            
            // Kiểm tra tổng số lượng không vượt quá tồn kho
            if (newTotalQuantity > currentStock) {
                System.out.println("❌ Total quantity exceeds stock: " + newTotalQuantity + " > " + currentStock);
                CustomDialog.showError(
                    "Total quantity exceeds available stock!\n\n" +
                    "Already in cart: " + existingQuantity + "\n" +
                    "Adding: " + cartItem.getQuantity() + "\n" +
                    "Total: " + newTotalQuantity + "\n" +
                    "Available: " + currentStock + "\n\n" +
                    "Maximum you can add: " + (currentStock - existingQuantity)
                );
                return false;
            }
            
            // Update với số lượng mới (tổng cộng)
            DTOCart updatedItem = new DTOCart(cartItem.getCustomerID(), cartItem.getProductID(), newTotalQuantity);
            return daoCart.updateCartItem(updatedItem);
        } else {
            // Kiểm tra số lượng mới không vượt quá tồn kho
            if (cartItem.getQuantity() > currentStock) {
                System.out.println("❌ Quantity exceeds stock: " + cartItem.getQuantity() + " > " + currentStock);
                CustomDialog.showError(
                    "Quantity exceeds available stock!\n\n" +
                    "Requested: " + cartItem.getQuantity() + "\n" +
                    "Available: " + currentStock + "\n\n" +
                    "Please reduce the quantity."
                );
                return false;
            }
            
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