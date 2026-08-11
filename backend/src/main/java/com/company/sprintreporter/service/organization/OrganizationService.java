package com.company.sprintreporter.service.organization;

import com.company.sprintreporter.domain.entity.AppUser;
import com.company.sprintreporter.domain.entity.Organization;
import com.company.sprintreporter.domain.entity.enums.UserRole;
import com.company.sprintreporter.infrastructure.persistence.DashboardRepository;
import com.company.sprintreporter.infrastructure.persistence.OrganizationRepository;
import com.company.sprintreporter.infrastructure.persistence.SubscriptionRepository;
import com.company.sprintreporter.infrastructure.persistence.UserRepository;
import com.company.sprintreporter.service.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrganizationService {

    private final OrganizationRepository organizationRepository;
    private final UserRepository userRepository;
    private final DashboardRepository dashboardRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final PasswordEncoder passwordEncoder;

    public Organization getById(UUID orgId) {
        return organizationRepository.findById(orgId)
                .orElseThrow(() -> new BusinessException("Organization not found", HttpStatus.NOT_FOUND));
    }

    @Transactional
    public Organization update(UUID orgId, String name, String email, String logoUrl) {
        Organization org = getById(orgId);
        if (name != null) org.setName(name);
        if (email != null) org.setEmail(email);
        if (logoUrl != null) org.setLogoUrl(logoUrl);
        return organizationRepository.save(org);
    }

    public List<AppUser> getMembers(UUID orgId) {
        return userRepository.findByOrganizationIdWithDashboards(orgId);
    }

    @Transactional
    public AppUser inviteMember(UUID orgId, String email, String firstName, String lastName, UserRole role, String tempPassword) {
        if (userRepository.existsByEmail(email)) {
            throw new BusinessException("Email already in use", HttpStatus.CONFLICT);
        }

        // Check member limit
        var subscription = subscriptionRepository.findByOrganizationIdWithPlan(orgId)
                .orElseThrow(() -> new BusinessException("No subscription", HttpStatus.INTERNAL_SERVER_ERROR));
        long currentCount = userRepository.countByOrganizationId(orgId);
        int maxMembers = subscription.getPlan().getMaxMembers();
        if (maxMembers != -1 && currentCount >= maxMembers) {
            throw new BusinessException("Member limit reached for your plan (" + maxMembers + " max). Upgrade to add more members.", HttpStatus.PAYMENT_REQUIRED);
        }

        Organization org = organizationRepository.getReferenceById(orgId);
        AppUser user = AppUser.builder()
                .organization(org)
                .email(email)
                .passwordHash(passwordEncoder.encode(tempPassword))
                .firstName(firstName)
                .lastName(lastName)
                .role(role != null ? role : UserRole.MEMBER)
            .dashboards(new java.util.LinkedHashSet<>(dashboardRepository.findByOrganizationIdOrderByPositionAsc(orgId)))
                .build();

        return userRepository.save(user);
    }

    @Transactional
    public void removeMember(UUID orgId, UUID userId) {
        AppUser user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("User not found", HttpStatus.NOT_FOUND));
        if (!user.getOrganization().getId().equals(orgId)) {
            throw new BusinessException("User does not belong to this organization", HttpStatus.FORBIDDEN);
        }
        if (user.getRole() == UserRole.ADMIN) {
            long adminCount = userRepository.findByOrganizationId(orgId).stream()
                    .filter(u -> u.getRole() == UserRole.ADMIN)
                    .count();
            if (adminCount <= 1) {
                throw new BusinessException("Cannot remove the last admin", HttpStatus.BAD_REQUEST);
            }
        }
        userRepository.delete(user);
        userRepository.flush();
    }
}
