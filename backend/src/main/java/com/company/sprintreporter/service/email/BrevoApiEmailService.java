package com.company.sprintreporter.service.email;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.List;
import java.util.Map;

/**
 * Sends transactional emails via Brevo's HTTPS API instead of raw SMTP.
 *
 * Used in production because Railway blocks outbound SMTP ports (587/465) at
 * the network level, causing {@code MailConnectException: ... Operation timed out}.
 * The API travels over HTTPS (443), which is never blocked.
 *
 * Requires an API key generated in Brevo under Settings → SMTP & API → API Keys
 * (different from the SMTP key used by {@link SmtpEmailService}).
 */
@Service
@Profile("prod")
@Slf4j
public class BrevoApiEmailService extends AbstractEmailService {

    private static final String BREVO_ENDPOINT = "https://api.brevo.com/v3/smtp/email";

    @Value("${app.email.brevo-api-key}")
    private String apiKey;

    @Value("${app.email.from-name:Kappa}")
    private String fromName;

    private final RestClient restClient = RestClient.builder()
            .baseUrl(BREVO_ENDPOINT)
            .requestFactory(buildRequestFactory())
            .build();

    private static ClientHttpRequestFactory buildRequestFactory() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(5_000);
        factory.setReadTimeout(10_000);
        return factory;
    }

    @Override
    protected void sendHtmlEmail(String to, String subject, String htmlBody) {
        try {
            restClient.post()
                    .header("api-key", apiKey)
                    .header("accept", "application/json")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of(
                            "sender", Map.of("email", getFromAddress(), "name", fromName),
                            "to", List.of(Map.of("email", to)),
                            "subject", subject,
                            "htmlContent", htmlBody
                    ))
                    .retrieve()
                    .toBodilessEntity();
            log.info("Email sent via Brevo API to {}: {}", to, subject);
        } catch (RestClientException e) {
            log.error("Failed to send email via Brevo API to {} (subject: {}): {}", to, subject, e.getMessage(), e);
        }
    }
}
