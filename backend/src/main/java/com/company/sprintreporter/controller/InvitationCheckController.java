package com.company.sprintreporter.controller;

import com.company.sprintreporter.domain.entity.Invitation;
import com.company.sprintreporter.domain.entity.enums.InvitationStatus;
import com.company.sprintreporter.infrastructure.persistence.InvitationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/invitations")
@RequiredArgsConstructor
public class InvitationCheckController {

    private final InvitationRepository invitationRepository;

    /**
     * Public endpoint: check if an email has a pending invitation.
     * Used during registration to inform the user they'll join an existing org after email verification.
     */
    @GetMapping("/check")
    @Transactional(readOnly = true)
    public ResponseEntity<Map<String, Object>> checkInvitation(@RequestParam String email) {
        Optional<Invitation> invitation = invitationRepository.findByEmailAndStatus(
                email.toLowerCase().trim(), InvitationStatus.PENDING);

        if (invitation.isPresent()) {
            Invitation inv = invitation.get();
            return ResponseEntity.ok(Map.of(
                    "invited", true,
                    "organizationName", inv.getOrganization().getName(),
                    "role", inv.getRole()
            ));
        }
        return ResponseEntity.ok(Map.of("invited", false));
    }
}
