package com.company.sprintreporter.controller;

import com.company.sprintreporter.config.jwt.JwtAuthenticationToken;
import com.company.sprintreporter.domain.entity.AppUser;
import com.company.sprintreporter.infrastructure.persistence.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/settings")
@RequiredArgsConstructor
public class SettingsController {

    private static final Set<String> SUPPORTED_LANGUAGES = Set.of("fr", "en");

    private final UserRepository userRepository;

    /**
     * GET /api/v1/settings/language
     * Returns the language preference for the authenticated user.
     */
    @GetMapping("/language")
    public ResponseEntity<Map<String, String>> getLanguage() {
        AppUser user = resolveUser();
        String lang = user.getLanguagePreference() != null ? user.getLanguagePreference() : "en";
        log.debug("GET language for user {}: {}", user.getId(), lang);
        return ResponseEntity.ok(Map.of("language", lang));
    }

    /**
     * PUT /api/v1/settings/language
     * Persists the language preference for the authenticated user.
     * Validates that language ∈ {"fr", "en"}.
     */
    @PutMapping("/language")
    public ResponseEntity<Map<String, String>> setLanguage(@RequestBody Map<String, String> body) {
        String lang = body.get("language");
        if (lang == null || !SUPPORTED_LANGUAGES.contains(lang)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Invalid language. Supported values: " + SUPPORTED_LANGUAGES);
        }

        AppUser user = resolveUser();
        user.setLanguagePreference(lang);
        userRepository.save(user);
        log.info("Updated language preference for user {} → {}", user.getId(), lang);
        return ResponseEntity.ok(Map.of("language", lang));
    }

    private AppUser resolveUser() {
        UUID userId = ((JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication()).getUserId();
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
    }
}
