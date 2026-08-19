package com.company.sprintreporter.domain.entity;

import com.company.sprintreporter.domain.entity.enums.UserRole;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AppUser {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;

    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @Column(name = "first_name", length = 100)
    private String firstName;

    @Column(name = "last_name", length = 100)
    private String lastName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private UserRole role = UserRole.MEMBER;

    @Column(nullable = false)
    @Builder.Default
    private Boolean enabled = true;

    @Column(name = "email_verified", nullable = false)
    @Builder.Default
    private Boolean emailVerified = false;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;

    @Column(name = "language_preference", nullable = false, length = 5)
    @Builder.Default
    private String languagePreference = "en";

    /** Per-user Jira credentials (BASIC auth: username + password). */
    @Column(name = "jira_base_url", length = 512)
    private String jiraBaseUrl;

    @Column(name = "jira_username", length = 255)
    private String jiraUsername;

    @Column(name = "jira_encrypted_password", length = 1024)
    private String jiraEncryptedPassword;

    @Column(name = "jira_connected", nullable = false)
    @Builder.Default
    private Boolean jiraConnected = false;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "user_dashboard_access",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "dashboard_id")
    )
    @Builder.Default
    private Set<Dashboard> dashboards = new HashSet<>();
}
