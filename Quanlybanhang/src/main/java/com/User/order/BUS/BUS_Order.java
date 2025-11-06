
package com.User.order.BUS;


import com.User.order.DAO.DAO_Order;
import com.User.order.DTO.DTO_Order;
import java.util.ArrayList;

public class BUS_Order {
    private DAO_Order daoOrder; // Sửa tên biến cho đúng chính tả
    
    // Khởi tạo DAO trong constructor
    public BUS_Order() {
        this.daoOrder = new DAO_Order();
    }
    
    private String lastErrorMessage = null; // Lưu error message cuối cùng
    
    /**
     * Add order và trả về error message nếu có lỗi, null nếu thành công
     */
    public String addOrderDetail(DTO_Order order) {
        // Kiểm tra null để đảm bảo an toàn
        if (daoOrder == null) {
            lastErrorMessage = "System error: Order service is not initialized!";
            System.err.println("DAO_Order is not initialized!");
            return lastErrorMessage;
        }
        lastErrorMessage = daoOrder.insertOrder(order);
        return lastErrorMessage; // null nếu thành công, error message nếu có lỗi
    }
    
    /**
     * Get last error message (for backward compatibility)
     */
    public String getLastErrorMessage() {
        return lastErrorMessage;
    }
    
    /**
     * Backward compatibility method - returns true if success, false if error
     */
    public boolean addOrderDetailBoolean(DTO_Order order) {
        String error = addOrderDetail(order);
        return error == null;
    }
    
      public ArrayList<DTO_Order> getOrdersByCustomer(String customerID){
          return daoOrder.getOrdersByCustomer(customerID);
      }
      
      public ArrayList<DTO_Order> getSortedOrdersByCustomer(String customerID) {
          return daoOrder.getSortedOrdersByCustomer(customerID);
      }
      
      /**
       * Lấy promotion code từ order theo OrderNo
       */
      public String getPromotionCodeByOrderNo(String orderNo) {
          return daoOrder.getPromotionCodeByOrderNo(orderNo);
      }

      public boolean cancelOrder(String orderNo, String customerID) {
          return daoOrder.cancelOrder(orderNo, customerID);
      }
}
