package com.Admin.promotion.BUS;

import com.Admin.promotion.DAO.DAOPromotion;
import com.Admin.promotion.DTO.DTOPromotion;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class BUSPromotion {
    private final DAOPromotion dao = new DAOPromotion();

    // ============================================
    // READ Operations
    // ============================================
    
    /**
     * Lấy tất cả mã giảm giá
     */
    public List<DTOPromotion> getAllPromotions() throws Exception {
        try {
            return dao.getAllPromotions();
        } catch (Exception e) {
            throw new Exception("Lỗi khi tải danh sách mã giảm giá: " + e.getMessage());
        }
    }
    
    /**
     * Tìm mã giảm giá theo code
     */
    public DTOPromotion getPromotionByCode(String code) throws Exception {
        if (code == null || code.trim().isEmpty()) {
            throw new Exception("Mã giảm giá không được để trống!");
        }
        try {
            return dao.getPromotionByCode(code);
        } catch (Exception e) {
            throw new Exception("Lỗi khi tìm mã giảm giá: " + e.getMessage());
        }
    }
    
    /**
     * Tìm kiếm mã giảm giá
     */
    public List<DTOPromotion> searchPromotions(String searchBy, String keyword) throws Exception {
        try {
            return dao.searchPromotions(searchBy, keyword);
        } catch (Exception e) {
            throw new Exception("Lỗi khi tìm kiếm: " + e.getMessage());
        }
    }
    
    /**
     * Tìm mã giảm giá đang hoạt động
     */
    public DTOPromotion findActivePromotion(String code) throws Exception {
        if (code == null || code.isBlank()) return null;
        DTOPromotion p = dao.getPromotionByCode(code);
        if (p == null) return null;
        if (dao.isPromotionActive(code, LocalDate.now())) {
            return p;
        }
        return null;
    }
    
    /**
     * Đếm số lượng mã giảm giá đang hoạt động
     */
    public int countActivePromotions() throws Exception {
        try {
            return dao.countActivePromotions();
        } catch (Exception e) {
            throw new Exception("Lỗi khi đếm mã giảm giá: " + e.getMessage());
        }
    }

    // ============================================
    // CREATE Operation
    // ============================================
    
    /**
     * Thêm mã giảm giá mới với validation
     */
    public boolean addPromotion(DTOPromotion promotion) throws Exception {
        // Validation
        validatePromotion(promotion);
        
        // Kiểm tra mã đã tồn tại chưa
        if (dao.isPromotionCodeExists(promotion.getPromotionCode())) {
            throw new Exception("Mã giảm giá '" + promotion.getPromotionCode() + "' đã tồn tại!");
        }
        
        // Kiểm tra ngày hợp lệ
        if (promotion.getStartDate().isAfter(promotion.getEndDate())) {
            throw new Exception("Ngày bắt đầu không được sau ngày kết thúc!");
        }
        
        try {
            return dao.addPromotion(promotion);
        } catch (Exception e) {
            throw new Exception("Lỗi khi thêm mã giảm giá: " + e.getMessage());
        }
    }

    // ============================================
    // UPDATE Operation
    // ============================================
    
    /**
     * Cập nhật mã giảm giá với validation
     */
    public boolean updatePromotion(DTOPromotion promotion) throws Exception {
        // Validation
        validatePromotion(promotion);
        
        // Kiểm tra mã có tồn tại không
        if (!dao.isPromotionCodeExists(promotion.getPromotionCode())) {
            throw new Exception("Mã giảm giá không tồn tại!");
        }
        
        // Kiểm tra ngày hợp lệ
        if (promotion.getStartDate().isAfter(promotion.getEndDate())) {
            throw new Exception("Ngày bắt đầu không được sau ngày kết thúc!");
        }
        
        try {
            return dao.updatePromotion(promotion);
        } catch (Exception e) {
            throw new Exception("Lỗi khi cập nhật mã giảm giá: " + e.getMessage());
        }
    }

    // ============================================
    // DELETE Operation
    // ============================================
    
    /**
     * Xóa mã giảm giá
     */
    public boolean deletePromotion(String code) throws Exception {
        if (code == null || code.trim().isEmpty()) {
            throw new Exception("Mã giảm giá không được để trống!");
        }
        
        // Kiểm tra mã có tồn tại không
        if (!dao.isPromotionCodeExists(code)) {
            throw new Exception("Mã giảm giá không tồn tại!");
        }
        
        try {
            return dao.deletePromotion(code);
        } catch (Exception e) {
            throw new Exception("Lỗi khi xóa mã giảm giá: " + e.getMessage());
        }
    }

    // ============================================
    // Validation Helper
    // ============================================
    
    /**
     * Kiểm tra tính hợp lệ của dữ liệu promotion
     */
    private void validatePromotion(DTOPromotion promotion) throws Exception {
        if (promotion == null) {
            throw new Exception("Thông tin mã giảm giá không được để trống!");
        }
        
        if (promotion.getPromotionCode() == null || promotion.getPromotionCode().trim().isEmpty()) {
            throw new Exception("Mã giảm giá không được để trống!");
        }
        
        if (promotion.getPromotionName() == null || promotion.getPromotionName().trim().isEmpty()) {
            throw new Exception("Tên chương trình không được để trống!");
        }
        
        if (promotion.getStartDate() == null) {
            throw new Exception("Ngày bắt đầu không được để trống!");
        }
        
        if (promotion.getEndDate() == null) {
            throw new Exception("Ngày kết thúc không được để trống!");
        }
        
        if (promotion.getDiscountPercent() == null) {
            throw new Exception("Phần trăm giảm giá không được để trống!");
        }
        
        // Kiểm tra phần trăm giảm giá hợp lệ (0-100)
        if (promotion.getDiscountPercent().compareTo(BigDecimal.ZERO) <= 0 || 
            promotion.getDiscountPercent().compareTo(new BigDecimal("100")) > 0) {
            throw new Exception("Phần trăm giảm giá phải từ 0.01% đến 100%!");
        }
    }
    
    /**
     * Lấy trạng thái của mã giảm giá
     */
    public String getPromotionStatus(DTOPromotion promotion) {
        LocalDate now = LocalDate.now();
        
        if (now.isBefore(promotion.getStartDate())) {
            return "Sắp diễn ra";
        } else if (now.isAfter(promotion.getEndDate())) {
            return "Đã hết hạn";
        } else {
            return "Đang hoạt động";
        }
    }
}