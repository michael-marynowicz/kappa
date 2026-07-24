package com.company.sprintreporter.domain.entity;

import com.company.sprintreporter.domain.entity.enums.JiraAuthType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "jira_configurations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JiraConfiguration {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "organization_id", nullable = false, unique = true)
    private Organization organization;

    @Column(name = "base_url", nullable = false, length = 512)
    private String baseUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "auth_type", nullable = false, length = 20)
    @Builder.Default
    private JiraAuthType authType = JiraAuthType.PAT;

    @Column(name = "user_email", length = 255)
    private String userEmail;

    @Column(name = "encrypted_token", nullable = false, length = 1024)
    private String encryptedToken;

    @Column(name = "project_key", length = 50)
    private String projectKey;

    @Column(name = "board_id")
    private Integer boardId;

    @Column(nullable = false)
    @Builder.Default
    private Boolean active = true;

    @Column(name = "oauth_refresh_token", length = 2048)
    private String oauthRefreshToken;

    @Column(name = "oauth_token_expiry")
    private Instant oauthTokenExpiry;

    @Column(name = "oauth_cloud_id", length = 255)
    private String oauthCloudId;

    @Column(name = "last_sync_at")
    private Instant lastSyncAt;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;
}
