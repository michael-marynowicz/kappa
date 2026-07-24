package com.company.sprintreporter.domain.entity;

import com.company.sprintreporter.domain.entity.Dashboard;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "capacity_team_members")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CapacityTeamMember {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "dashboard_id", nullable = false)
    private Dashboard dashboard;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 10)
    private String role;

    @Column(name = "time_override", nullable = false, columnDefinition = "NUMERIC(3,2)")
    @Builder.Default
    private double timeOverride = 1.0;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;
}
