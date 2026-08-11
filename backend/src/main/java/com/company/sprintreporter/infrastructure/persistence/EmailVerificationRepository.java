package com.company.sprintreporter.infrastructure.persistence;

import com.company.sprintreporter.domain.entity.EmailVerification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface EmailVerificationRepository extends JpaRepository<EmailVerification, UUID> {

    Optional<EmailVerification> findByTokenHashAndVerifiedAtIsNull(String tokenHash);

    void deleteByUserId(UUID userId);
}
