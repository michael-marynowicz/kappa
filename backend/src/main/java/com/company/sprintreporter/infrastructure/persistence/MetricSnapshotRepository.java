package com.company.sprintreporter.infrastructure.persistence;

import com.company.sprintreporter.domain.entity.MetricSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface MetricSnapshotRepository extends JpaRepository<MetricSnapshot, UUID> {

    List<MetricSnapshot> findByOrganizationIdOrderBySnapshotDateDesc(UUID organizationId);

    List<MetricSnapshot> findBySprintId(UUID sprintId);
}
