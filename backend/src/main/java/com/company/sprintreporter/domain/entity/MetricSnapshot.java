package com.company.sprintreporter.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "metric_snapshots")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MetricSnapshot {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sprint_id", nullable = false)
    private Sprint sprint;

    @Column(name = "committed_points", precision = 6, scale = 1, nullable = false)
    @Builder.Default
    private BigDecimal committedPoints = BigDecimal.ZERO;

    @Column(name = "delivered_points", precision = 6, scale = 1, nullable = false)
    @Builder.Default
    private BigDecimal deliveredPoints = BigDecimal.ZERO;

    @Column(precision = 6, scale = 1, nullable = false)
    @Builder.Default
    private BigDecimal velocity = BigDecimal.ZERO;

    @Column(precision = 5, scale = 2, nullable = false)
    @Builder.Default
    private BigDecimal ratio = BigDecimal.ZERO;

    @Column(name = "scope_change_points", precision = 6, scale = 1, nullable = false)
    @Builder.Default
    private BigDecimal scopeChangePoints = BigDecimal.ZERO;

    @Column(name = "snapshot_date", nullable = false)
    @Builder.Default
    private LocalDate snapshotDate = LocalDate.now();

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;
}
