package com.company.sprintreporter.infrastructure.persistence;

import com.company.sprintreporter.domain.entity.BacklogItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BacklogItemRepository extends JpaRepository<BacklogItem, UUID> {

    List<BacklogItem> findBySprintId(UUID sprintId);

    List<BacklogItem> findByOrganizationId(UUID organizationId);

    Optional<BacklogItem> findByOrganizationIdAndExternalKey(UUID organizationId, String externalKey);
}
