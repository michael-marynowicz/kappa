package com.company.sprintreporter.controller;

import com.company.sprintreporter.config.jwt.JwtAuthenticationToken;
import com.company.sprintreporter.service.subscription.SubscriptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final SubscriptionService subscriptionService;

    @GetMapping("/me/permissions")
    public ResponseEntity<Set<String>> getMyPermissions() {
        var auth = (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        var permissions = subscriptionService.getGrantedFeatures(auth.getOrganizationId());
        return ResponseEntity.ok(permissions);
    }
}
