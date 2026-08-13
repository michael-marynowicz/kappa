package com.company.sprintreporter.service.email;

import com.company.sprintreporter.domain.entity.AppUser;
import com.company.sprintreporter.domain.entity.Invitation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;

/**
 * Shared template-rendering logic for transactional emails. Concrete subclasses
 * only need to implement {@link #sendHtmlEmail} for their transport mechanism:
 * <ul>
 *   <li>{@link SmtpEmailService} — direct SMTP, used outside production (Mailpit)</li>
 *   <li>{@link BrevoApiEmailService} — Brevo HTTP API, used in production</li>
 * </ul>
 */
public abstract class AbstractEmailService implements EmailService {

    private static final String HTML_WRAPPER = "<html><body style=\"font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto;\">%s</body></html>";
    private static final String BUTTON_STYLE = "style=\"background-color: #4F46E5; color: white; padding: 12px 24px; text-decoration: none; border-radius: 6px; font-weight: bold;\"";
    private static final String FOOTER_STYLE = "style=\"color: #666; font-size: 12px;\"";
    private static final String LANG_FR = "fr";

    @Value("${app.base-url:http://localhost:4200}")
    private String baseUrl;

    @Value("${app.email.from:noreply@sprintreporter.com}")
    private String fromAddress;

    protected String getFromAddress() {
        return fromAddress;
    }

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

    /** Sends the rendered HTML email through this implementation's transport. */
    protected abstract void sendHtmlEmail(String to, String subject, String htmlBody);

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

    private record EmailTemplate(String subject, String body) {}
}
