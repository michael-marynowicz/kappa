package com.company.sprintreporter.infrastructure.persistence;

import com.company.sprintreporter.domain.entity.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<AppUser, UUID> {

    Optional<AppUser> findByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsByEmailAndOrganizationId(String email, UUID organizationId);

    Optional<AppUser> findByEmailAndOrganizationId(String email, UUID organizationId);

    @Query("SELECT u FROM AppUser u JOIN FETCH u.organization WHERE u.id = :userId")
    Optional<AppUser> findByIdWithOrganization(UUID userId);

    @Query("SELECT DISTINCT u FROM AppUser u LEFT JOIN FETCH u.dashboards WHERE u.organization.id = :organizationId")
    List<AppUser> findByOrganizationIdWithDashboards(UUID organizationId);

    List<AppUser> findByOrganizationId(UUID organizationId);

    long countByOrganizationId(UUID organizationId);
}
