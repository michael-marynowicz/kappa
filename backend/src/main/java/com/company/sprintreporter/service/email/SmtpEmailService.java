package com.company.sprintreporter.service.email;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

/**
 * Sends emails via direct SMTP. Used outside production (local/dev with Mailpit),
 * where a direct SMTP connection on the Docker network is reliable.
 *
 * Not used in production — see {@link BrevoApiEmailService}.
 */
@Service
@Profile("!prod")
@RequiredArgsConstructor
@Slf4j
public class SmtpEmailService extends AbstractEmailService {

    private final JavaMailSender mailSender;

    @Override
    protected void sendHtmlEmail(String to, String subject, String htmlBody) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(getFromAddress());
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlBody, true);
            mailSender.send(message);
            log.info("Email sent to {}: {}", to, subject);
        } catch (Exception e) {
            log.error("Failed to send email to {} (subject: {}): {}", to, subject, e.getMessage(), e);
        }
    }
}
