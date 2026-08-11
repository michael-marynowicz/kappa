package com.company.sprintreporter.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "stripe")
public class StripeProperties {

    private boolean enabled = false;
    private String secretKey;
    private String webhookSecret;
    private String successUrl;
    private String cancelUrl;
}
