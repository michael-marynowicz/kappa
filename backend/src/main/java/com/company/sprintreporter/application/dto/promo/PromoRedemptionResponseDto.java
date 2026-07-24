package com.company.sprintreporter.application.dto.promo;

import com.company.sprintreporter.domain.entity.enums.DiscountType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
public class PromoRedemptionResponseDto {

    private DiscountType discountType;
    private BigDecimal discountValue;
    private String effect;
}
