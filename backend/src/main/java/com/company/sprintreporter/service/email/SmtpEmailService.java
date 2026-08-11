package com.company.sprintreporter.service.email;

import com.company.sprintreporter.domain.entity.AppUser;
import com.company.sprintreporter.domain.entity.Invitation;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class SmtpEmailService implements EmailService {

    private static final String HTML_WRAPPER = "<html><body style=\"font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto;\">%s</body></html>";
    private static final String BUTTON_STYLE = "style=\"background-color: #4F46E5; color: white; padding: 12px 24px; text-decoration: none; border-radius: 6px; font-weight: bold;\"";
    private static final String FOOTER_STYLE = "style=\"color: #666; font-size: 12px;\"";
    private static final String LANG_FR = "fr";

    private final JavaMailSender mailSender;

    @Value("${app.base-url:http://localhost:4200}")
    private String baseUrl;

    @Value("${spring.mail.from:noreply@sprintreporter.com}")
    private String fromAddress;

    @Override
    @Async
    public void sendInvitationEmail(Invitation invitation, String organizationName) {
        String language = extractLanguage(invitation.getInvitedBy() != null 
            ? invitation.getInvitedBy().getLanguagePreference() 
            : null);
        String inviterName = invitation.getInvitedBy() != null 
            ? invitation.getInvitedBy().getFirstName() 
            : "Un administrateur";
        String registerUrl = baseUrl + "/register?email=" + invitation.getEmail();

        EmailTemplate template = buildInvitationTemplate(language, inviterName, organizationName, invitation.getRole().name(), registerUrl);
        sendHtmlEmail(invitation.getEmail(), template.subject(), template.body());
    }

    @Override
    @Async
    public void sendVerificationEmail(AppUser user, String verificationUrl) {
        String language = extractLanguage(user.getLanguagePreference());
        EmailTemplate template = buildVerificationTemplate(language, user.getFirstName(), verificationUrl);
        sendHtmlEmail(user.getEmail(), template.subject(), template.body());
    }

    private EmailTemplate buildInvitationTemplate(String language, String inviterName, String organizationName, String role, String registerUrl) {
        if (LANG_FR.equalsIgnoreCase(language)) {
            return new EmailTemplate(
                "Vous êtes invité à rejoindre l'équipe " + organizationName + " dans Kappa",
                formatInvitationBodyFr(inviterName, organizationName, role, registerUrl)
            );
        }
        return new EmailTemplate(
            "You're invited to join team " + organizationName + " in Kappa",
            formatInvitationBodyEn(inviterName, organizationName, role, registerUrl)
        );
    }

    private EmailTemplate buildVerificationTemplate(String language, String firstName, String verificationUrl) {
        if (LANG_FR.equalsIgnoreCase(language)) {
            return new EmailTemplate(
                "Vérifiez votre adresse email — Kappa",
                formatVerificationBodyFr(firstName, verificationUrl)
            );
        }
        return new EmailTemplate(
            "Verify your email address — Kappa",
            formatVerificationBodyEn(firstName, verificationUrl)
        );
    }

    private String formatInvitationBodyFr(String inviterName, String organizationName, String role, String registerUrl) {
        String content = """
            <h2>Vous avez été invité !</h2>
            <p>Bonjour,</p>
            <p><strong>%s</strong> vous invite à rejoindre l'équipe <strong>%s</strong> dans Kappa en tant que <strong>%s</strong>.</p>
            <p>Pour accepter l'invitation, créez votre compte en cliquant sur le bouton ci-dessous :</p>
            <p style="text-align: center; margin: 30px 0;">
                <a href="%s" %s>Créer mon compte</a>
            </p>
            <p %s>Si vous n'attendiez pas cette invitation, ignorez simplement cet email.</p>
            """.formatted(inviterName, organizationName, role, registerUrl, BUTTON_STYLE, FOOTER_STYLE);
        return HTML_WRAPPER.formatted(content);
    }

    private String formatInvitationBodyEn(String inviterName, String organizationName, String role, String registerUrl) {
        String content = """
            <h2>You have been invited!</h2>
            <p>Hello,</p>
            <p><strong>%s</strong> invites you to join the team <strong>%s</strong> in Kappa as <strong>%s</strong>.</p>
            <p>To accept the invitation, create your account by clicking the button below:</p>
            <p style="text-align: center; margin: 30px 0;">
                <a href="%s" %s>Create my account</a>
            </p>
            <p %s>If you did not expect this invitation, simply ignore this email.</p>
            """.formatted(inviterName, organizationName, role, registerUrl, BUTTON_STYLE, FOOTER_STYLE);
        return HTML_WRAPPER.formatted(content);
    }

    private String formatVerificationBodyFr(String firstName, String verificationUrl) {
        String content = """
            <h2>Vérification de votre email</h2>
            <p>Bonjour %s,</p>
            <p>Merci de vous être inscrit sur Kappa. Cliquez sur le bouton ci-dessous pour vérifier votre adresse email :</p>
            <p style="text-align: center; margin: 30px 0;">
                <a href="%s" %s>Vérifier mon email</a>
            </p>
            <p %s>Ce lien expire dans 24 heures. Si vous n'avez pas créé de compte, ignorez cet email.</p>
            """.formatted(firstName, verificationUrl, BUTTON_STYLE, FOOTER_STYLE);
        return HTML_WRAPPER.formatted(content);
    }

    private String formatVerificationBodyEn(String firstName, String verificationUrl) {
        String content = """
            <h2>Email Verification</h2>
            <p>Hello %s,</p>
            <p>Thank you for signing up on Kappa. Click the button below to verify your email address:</p>
            <p style="text-align: center; margin: 30px 0;">
                <a href="%s" %s>Verify my email</a>
            </p>
            <p %s>This link expires in 24 hours. If you did not create an account, ignore this email.</p>
            """.formatted(firstName, verificationUrl, BUTTON_STYLE, FOOTER_STYLE);
        return HTML_WRAPPER.formatted(content);
    }

    private String extractLanguage(String languagePreference) {
        return (languagePreference != null && LANG_FR.equalsIgnoreCase(languagePreference)) ? LANG_FR : "en";
    }

    private void sendHtmlEmail(String to, String subject, String htmlBody) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromAddress);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlBody, true);
            mailSender.send(message);
            log.info("Email sent to {}: {}", to, subject);
        } catch (MessagingException e) {
            log.error("Failed to send email to {}: {}", to, e.getMessage());
        }
    }

    private record EmailTemplate(String subject, String body) {}
}
