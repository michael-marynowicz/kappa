package com.company.sprintreporter.application.dto.subscription;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class CheckoutResponseDto {

    private String checkoutUrl;
    private String sessionId;
}
