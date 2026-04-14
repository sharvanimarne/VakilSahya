package com.vakilsahay.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

/**
 * Clause entity — individual clause extracted from a legal document.
 *
 * PROPRIETARY: The severity_score field is populated by VakilSahay's
 * proprietary ClauseAnalyzerService using the Indian Legal Clause
 * Taxonomy and Severity Scoring Algorithm.
 * © 2025 VakilSahay. All rights reserved.
 */
@Entity
@Table(name = "clauses")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Clause {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "document_id", nullable = false)
    private Document document;

    @Column(name = "clause_number", nullable = false)
    private Integer clauseNumber;

    @Column(name = "original_text", columnDefinition = "TEXT", nullable = false)
    private String originalText;

    @Column(name = "plain_english", columnDefinition = "TEXT")
    private String plainEnglish;

    @Enumerated(EnumType.STRING)
    @Column(name = "clause_type")
    private ClauseType clauseType;

    @Enumerated(EnumType.STRING)
    private Severity severity;

    /**
     * Proprietary severity score 0–100.
     * 0–25: LOW, 26–50: MEDIUM, 51–75: HIGH, 76–100: CRITICAL
     * © 2025 VakilSahay Severity Scoring Algorithm
     */
    @Column(name = "severity_score")
    private Integer severityScore;

    @Column(name = "law_reference")
    private String lawReference;

    @Column(columnDefinition = "TEXT")
    private String recommendation;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    /**
     * ClauseType Taxonomy — © 2025 VakilSahay Indian Legal Clause Taxonomy
     * Covers: Rental Agreements, Employment Contracts, Loan Agreements,
     *         NDAs, Partnership Deeds
     */
    public enum ClauseType {
        // Universal
        TERMINATION, PENALTY, LIABILITY, ARBITRATION, JURISDICTION, GOVERNING_LAW,
        CONFIDENTIALITY, INDEMNIFICATION, FORCE_MAJEURE, DISPUTE_RESOLUTION,
        AMENDMENT, ASSIGNMENT, WAIVER, ENTIRE_AGREEMENT,
        // Rental specific
        RENT_ESCALATION, SECURITY_DEPOSIT, LOCK_IN_PERIOD, MAINTENANCE, SUBLETTING,
        // Employment specific
        NOTICE_PERIOD, NON_COMPETE, IP_OWNERSHIP, PROBATION, PERFORMANCE,
        // Loan specific
        INTEREST_RATE, PREPAYMENT, DEFAULT, COLLATERAL, GUARANTOR,
        // General
        GENERAL
    }

    public enum Severity { LOW, MEDIUM, HIGH, CRITICAL }
}