package com.company.sprintreporter.infrastructure.store;

import com.company.sprintreporter.domain.entity.Organization;
import com.company.sprintreporter.domain.entity.RemainingSpOverride;
import com.company.sprintreporter.domain.port.RemainingStoryPointsStore;
import com.company.sprintreporter.infrastructure.persistence.RemainingSpOverrideRepository;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
@Transactional
public class JpaRemainingStoryPointsStore implements RemainingStoryPointsStore {

    private final RemainingSpOverrideRepository repository;
    private final EntityManager entityManager;

    @Override
    public void save(UUID organizationId, String issueKey, int remainingStoryPoints) {
        log.debug("Storing remaining SP override: {} -> {} (org={})", issueKey, remainingStoryPoints, organizationId);

        RemainingSpOverride entity = repository
                .findByOrganizationIdAndIssueKey(organizationId, issueKey)
                .orElseGet(() -> {
                    Organization orgRef = entityManager.getReference(Organization.class, organizationId);
                    return RemainingSpOverride.builder()
                            .organization(orgRef)
                            .issueKey(issueKey)
                            .build();
                });

        entity.setRemainingSp(remainingStoryPoints);
        repository.save(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Integer> find(UUID organizationId, String issueKey) {
        return repository.findByOrganizationIdAndIssueKey(organizationId, issueKey)
                .map(RemainingSpOverride::getRemainingSp);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Integer> findAll(UUID organizationId) {
        return repository.findByOrganizationId(organizationId).stream()
                .collect(Collectors.toMap(
                        RemainingSpOverride::getIssueKey,
                        RemainingSpOverride::getRemainingSp,
                        (a, b) -> b
                ));
    }

    @Override
    public void clear(UUID organizationId) {
        log.info("Clearing all remaining SP overrides for org {}", organizationId);
        repository.deleteByOrganizationId(organizationId);
    }
}
