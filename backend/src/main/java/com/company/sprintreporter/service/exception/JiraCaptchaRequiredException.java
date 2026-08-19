package com.company.sprintreporter.service.exception;

import org.springframework.http.HttpStatus;

/**
 * Thrown when Jira rejects Basic Authentication with reason "AUTHENTICATION_DENIED"
 * (or an equivalent CAPTCHA challenge). Jira Data Center locks an account after
 * repeated failed login attempts and requires the user to solve a CAPTCHA via the
 * standard web login form before ANY authentication (including REST/API calls)
 * will succeed again — even with fully correct credentials.
 */
public class JiraCaptchaRequiredException extends JiraException {

    public JiraCaptchaRequiredException(String baseUrl) {
        super(
                "Jira has temporarily locked this account after too many failed login attempts " +
                "and now requires solving a CAPTCHA before any authentication will work \u2014 including " +
                "this app. Please open " + baseUrl + " in your browser, log in with your usual Jira " +
                "credentials, complete the CAPTCHA if one is shown, then come back here and try again.",
                HttpStatus.FORBIDDEN
        );
    }
}
