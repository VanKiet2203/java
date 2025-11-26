package com.Admin.promotion.DTO;

import java.math.BigDecimal;
import java.time.LocalDate;

public class DTOPromotion {
    private String promotionCode;
    private String promotionName;
    private LocalDate startDate;
    private LocalDate endDate;
    private BigDecimal discountPercent;

    public DTOPromotion() {}

    public DTOPromotion(String promotionCode, String promotionName, LocalDate startDate, LocalDate endDate, BigDecimal discountPercent) {
        this.promotionCode = promotionCode;
        this.promotionName = promotionName;
        this.startDate = startDate;
        this.endDate = endDate;
        this.discountPercent = discountPercent;
    }

    public String getPromotionCode() { return promotionCode; }
    public void setPromotionCode(String promotionCode) { this.promotionCode = promotionCode; }

    public String getPromotionName() { return promotionName; }
    public void setPromotionName(String promotionName) { this.promotionName = promotionName; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

    public BigDecimal getDiscountPercent() { return discountPercent; }
    public void setDiscountPercent(BigDecimal discountPercent) { this.discountPercent = discountPercent; }
}