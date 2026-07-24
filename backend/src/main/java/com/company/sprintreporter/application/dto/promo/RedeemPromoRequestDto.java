package com.company.sprintreporter.application.dto.promo;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RedeemPromoRequestDto {

    @NotBlank
    private String code;
}
