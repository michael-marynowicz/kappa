package com.company.sprintreporter.service.email;

import com.company.sprintreporter.domain.entity.Invitation;
import com.company.sprintreporter.domain.entity.AppUser;

public interface EmailService {

    void sendInvitationEmail(Invitation invitation, String organizationName);

    void sendVerificationEmail(AppUser user, String verificationUrl);
}
