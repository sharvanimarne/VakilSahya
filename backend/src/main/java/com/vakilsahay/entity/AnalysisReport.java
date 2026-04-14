package com.vakilsahay.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

/**
 * AnalysisReport — aggregated risk report for a legal document.
 * © 2025 VakilSahay
 */
@Entity
@Table(name = "analysis_reports")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AnalysisReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "document_id", nullable = false, unique = true)
    private Document document;

    @Column(name = "overall_risk_score", nullable = false)
    @Builder.Default
    private Integer overallRiskScore = 0;

    @Column(name = "total_clauses", nullable = false)
    @Builder.Default
    private Integer totalClauses = 0;

    @Column(name = "critical_count", nullable = false)
    @Builder.Default
    private Integer criticalCount = 0;

    @Column(name = "high_count", nullable = false)
    @Builder.Default
    private Integer highCount = 0;

    @Column(name = "medium_count", nullable = false)
    @Builder.Default
    private Integer mediumCount = 0;

    @Column(name = "low_count", nullable = false)
    @Builder.Default
    private Integer lowCount = 0;

    @Column(columnDefinition = "TEXT")
    private String summary;

    @Column(name = "top_concerns", columnDefinition = "TEXT")
    private String topConcerns;  // JSON array

    @Column(columnDefinition = "TEXT")
    private String recommendations;  // JSON array

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at", nullable = false)
    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();

    @PreUpdate
    protected void onUpdate() { this.updatedAt = LocalDateTime.now(); }
}