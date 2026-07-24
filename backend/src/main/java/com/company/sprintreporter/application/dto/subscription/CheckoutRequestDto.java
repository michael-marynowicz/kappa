package com.company.sprintreporter.application.dto.subscription;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CheckoutRequestDto {

    @NotBlank
    private String planCode;

    private String promoCode;
}
