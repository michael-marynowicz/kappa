package com.company.sprintreporter.controller;

import com.company.sprintreporter.application.dto.promo.PromoRedemptionResponseDto;
import com.company.sprintreporter.application.dto.promo.RedeemPromoRequestDto;
import com.company.sprintreporter.config.jwt.JwtAuthenticationToken;
import com.company.sprintreporter.service.promo.PromoCodeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/promo")
@RequiredArgsConstructor
public class PromoController {

    private final PromoCodeService promoCodeService;

    @PostMapping("/redeem")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PromoRedemptionResponseDto> redeem(@Valid @RequestBody RedeemPromoRequestDto request) {
        var auth = (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        var result = promoCodeService.redeem(request.getCode(), auth.getOrganizationId(), auth.getUserId());

        return ResponseEntity.ok(PromoRedemptionResponseDto.builder()
                .discountType(result.type())
                .discountValue(result.value())
                .effect(result.effect())
                .build());
    }
}
