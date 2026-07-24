package com.company.sprintreporter.config;

import org.springframework.context.annotation.Configuration;

/**
 * Web MVC configuration.
 *
 * CORS is fully managed by {@link SecurityConfig#corsConfigurationSource()}.
 * Defining it here as well would cause a conflict:
 *   - SecurityConfig sets allowCredentials=true (required for JWT Bearer headers)
 *   - WebMvcConfigurer sets allowCredentials=false
 * In Spring Security 6, the Security filter chain CORS config takes precedence,
 * so the WebMvcConfigurer mapping is ignored for /api/** — keeping it is misleading.
 */
@Configuration
public class WebConfig {
}
