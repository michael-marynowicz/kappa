package com.company.sprintreporter.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "backlog_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BacklogItem {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sprint_id")
    private Sprint sprint;

    @Column(name = "external_key", length = 50)
    private String externalKey;

    @Column(nullable = false, length = 500)
    private String summary;

    @Column(name = "issue_type", length = 50)
    private String issueType;

    @Column(nullable = false, length = 50)
    @Builder.Default
    private String status = "To Do";

    @Column(length = 200)
    private String assignee;

    @Column(name = "story_points", precision = 5, scale = 1)
    private BigDecimal storyPoints;

    @Column(name = "remaining_story_points", precision = 5, scale = 1)
    private BigDecimal remainingStoryPoints;

    @Column(length = 100)
    private String topic;

    @Column(length = 20)
    private String priority;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;
}
