package com.company.sprintreporter.service.jira;

import com.company.sprintreporter.application.dto.jira.DashboardDto;
import com.company.sprintreporter.domain.entity.AppUser;
import com.company.sprintreporter.domain.entity.Dashboard;
import com.company.sprintreporter.domain.entity.Organization;
import com.company.sprintreporter.domain.entity.Subscription;
import com.company.sprintreporter.infrastructure.persistence.DashboardRepository;
import com.company.sprintreporter.infrastructure.persistence.OrganizationRepository;
import com.company.sprintreporter.infrastructure.persistence.UserRepository;
import com.company.sprintreporter.service.subscription.SubscriptionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
public class DashboardService {

    private final DashboardRepository dashboardRepository;
    private final OrganizationRepository organizationRepository;
    private final UserRepository userRepository;
    private final SubscriptionService subscriptionService;

    public DashboardService(DashboardRepository dashboardRepository,
                            OrganizationRepository organizationRepository,
                            UserRepository userRepository,
                            @Lazy SubscriptionService subscriptionService) {
        this.dashboardRepository = dashboardRepository;
        this.organizationRepository = organizationRepository;
        this.userRepository = userRepository;
        this.subscriptionService = subscriptionService;
    }

    public List<DashboardDto> listDashboards(UUID orgId, UUID userId) {
        // All members of the org can see all of the org's dashboards — access to the
        // underlying Jira data is already gated by each user's own Jira credentials.
        return dashboardRepository.findByOrganizationIdOrderByPositionAsc(orgId)
                .stream().map(this::toDto).toList();
    }

    public Optional<Dashboard> findActiveDashboard(UUID orgId) {
        return dashboardRepository.findByOrganizationIdAndActiveTrue(orgId);
    }

    @Transactional
    public DashboardDto addDashboard(UUID orgId, UUID creatorId, String name, Integer boardId, String projectKey) {
        if (dashboardRepository.existsByOrganizationIdAndBoardId(orgId, boardId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Dashboard for board " + boardId + " already exists in this organization");
        }

        // Enforce max_dashboards limit from plan (-1 = unlimited)
        List<Dashboard> existing = dashboardRepository.findByOrganizationIdOrderByPositionAsc(orgId);
        Subscription sub = subscriptionService.getByOrganizationId(orgId);
        int maxDashboards = sub.getPlan().getMaxDashboards();
        if (maxDashboards != -1 && existing.size() >= maxDashboards) {
            throw new ResponseStatusException(HttpStatus.PAYMENT_REQUIRED,
                    "Your plan allows a maximum of " + maxDashboards + " dashboard(s). Upgrade to PRO or ENTERPRISE for more.");
        }

        Organization org = organizationRepository.getReferenceById(orgId);
        long count = existing.size();

        Dashboard dashboard = Dashboard.builder()
                .organization(org)
                .name(name)
                .boardId(boardId)
                .projectKey(projectKey)
                .active(count == 0)
                .position((int) count)
                .build();

        Dashboard saved = dashboardRepository.save(dashboard);

        // Grant access to the creator
        AppUser creator = userRepository.findById(creatorId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        creator.getDashboards().add(saved);
        userRepository.save(creator);

        return toDto(saved);
    }

    @Transactional
    public DashboardDto activate(UUID orgId, UUID dashboardId) {
        Dashboard dashboard = dashboardRepository.findById(dashboardId)
                .filter(d -> d.getOrganization().getId().equals(orgId))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Dashboard not found"));

        dashboardRepository.deactivateAllForOrg(orgId);
        dashboard.setActive(true);
        return toDto(dashboardRepository.save(dashboard));
    }

    @Transactional
    public void deleteDashboard(UUID orgId, UUID dashboardId) {
        Dashboard dashboard = dashboardRepository.findById(dashboardId)
                .filter(d -> d.getOrganization().getId().equals(orgId))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Dashboard not found"));

        boolean wasActive = Boolean.TRUE.equals(dashboard.getActive());
        dashboardRepository.delete(dashboard);

        // If deleted dashboard was active, activate the first remaining one
        if (wasActive) {
            dashboardRepository.findByOrganizationIdOrderByPositionAsc(orgId)
                    .stream().findFirst().ifPresent(first -> {
                        first.setActive(true);
                        dashboardRepository.save(first);
                    });
        }
    }

    private DashboardDto toDto(Dashboard d) {
        return DashboardDto.builder()
                .id(d.getId())
                .name(d.getName())
                .boardId(d.getBoardId())
                .projectKey(d.getProjectKey())
                .active(d.getActive())
                .position(d.getPosition())
                .createdAt(d.getCreatedAt())
                .build();
    }
}
