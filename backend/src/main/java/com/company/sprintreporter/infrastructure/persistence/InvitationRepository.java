package com.company.sprintreporter.infrastructure.persistence;

import com.company.sprintreporter.domain.entity.Invitation;
import com.company.sprintreporter.domain.entity.enums.InvitationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface InvitationRepository extends JpaRepository<Invitation, UUID> {

    List<Invitation> findByOrganizationIdAndStatus(UUID organizationId, InvitationStatus status);

    @Query("SELECT DISTINCT i FROM Invitation i LEFT JOIN FETCH i.dashboards WHERE i.organization.id = :organizationId AND i.status = :status")
    List<Invitation> findByOrganizationIdAndStatusWithDashboards(UUID organizationId, InvitationStatus status);

    @Query("SELECT i FROM Invitation i JOIN FETCH i.organization WHERE i.email = :email AND i.status = :status")
    Optional<Invitation> findByEmailAndStatus(String email, InvitationStatus status);

    @Query("SELECT DISTINCT i FROM Invitation i JOIN FETCH i.organization LEFT JOIN FETCH i.dashboards WHERE i.email = :email AND i.status = :status")
    Optional<Invitation> findByEmailAndStatusWithDashboards(String email, InvitationStatus status);

    boolean existsByOrganizationIdAndEmailAndStatus(UUID organizationId, String email, InvitationStatus status);

    long countByOrganizationIdAndStatus(UUID organizationId, InvitationStatus status);
}
