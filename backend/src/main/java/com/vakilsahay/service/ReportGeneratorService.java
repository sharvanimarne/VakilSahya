package com.vakilsahay.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vakilsahay.entity.AnalysisReport;
import com.vakilsahay.entity.Clause;
import com.vakilsahay.entity.Clause.Severity;
import com.vakilsahay.entity.Document;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * ReportGeneratorService — aggregates analyzed clauses into a risk report.
 *
 * PROPRIETARY: The overall risk score formula uses a weighted average that
 * assigns higher weight to CRITICAL and HIGH severity clauses.
 * © 2025 VakilSahay
 */
@Service
@Slf4j
public class ReportGeneratorService {

    private final ObjectMapper objectMapper = new ObjectMapper();

    // Severity weights for overall risk calculation — © 2025 VakilSahay
    private static final Map<Severity, Double> SEVERITY_WEIGHTS = Map.of(
            Severity.CRITICAL, 4.0,
            Severity.HIGH,     2.5,
            Severity.MEDIUM,   1.5,
            Severity.LOW,      0.5
    );

    public AnalysisReport generateReport(Document document, List<Clause> clauses) {
        int critical = countBySeverity(clauses, Severity.CRITICAL);
        int high     = countBySeverity(clauses, Severity.HIGH);
        int medium   = countBySeverity(clauses, Severity.MEDIUM);
        int low      = countBySeverity(clauses, Severity.LOW);
        int total    = clauses.size();

        int overallScore = calculateOverallScore(clauses);

        List<String> topConcerns   = buildTopConcerns(clauses);
        List<String> recommendations = buildRecommendations(clauses, document.getDocumentType());
        String summary = buildSummary(document, overallScore, critical, high, total);

        return AnalysisReport.builder()
                .document(document)
                .overallRiskScore(overallScore)
                .totalClauses(total)
                .criticalCount(critical)
                .highCount(high)
                .mediumCount(medium)
                .lowCount(low)
                .summary(summary)
                .topConcerns(toJson(topConcerns))
                .recommendations(toJson(recommendations))
                .build();
    }

    /**
     * Weighted risk score — CRITICAL clauses dominate the score.
     * Formula: Σ(clauseScore × severityWeight) / Σ(severityWeight)
     * © 2025 VakilSahay Weighted Severity Formula
     */
    private int calculateOverallScore(List<Clause> clauses) {
        if (clauses.isEmpty()) return 0;

        double weightedSum   = 0;
        double totalWeight   = 0;

        for (Clause c : clauses) {
            if (c.getSeverityScore() == null || c.getSeverity() == null) continue;
            double weight = SEVERITY_WEIGHTS.getOrDefault(c.getSeverity(), 1.0);
            weightedSum += c.getSeverityScore() * weight;
            totalWeight += weight;
        }

        if (totalWeight == 0) return 0;
        return (int) Math.min(100, Math.round(weightedSum / totalWeight));
    }

    private List<String> buildTopConcerns(List<Clause> clauses) {
        return clauses.stream()
                .filter(c -> c.getSeverity() == Severity.CRITICAL || c.getSeverity() == Severity.HIGH)
                .sorted(Comparator.comparingInt(Clause::getSeverityScore).reversed())
                .limit(3)
                .map(c -> "Clause " + c.getClauseNumber() + ": " +
                        (c.getClauseType() != null ? c.getClauseType().name().replace("_", " ") : "General") +
                        " — Score " + c.getSeverityScore() + "/100")
                .collect(Collectors.toList());
    }

    private List<String> buildRecommendations(List<Clause> clauses, Document.DocumentType docType) {
        List<String> recs = new ArrayList<>();

        boolean hasNonCompete  = clauses.stream().anyMatch(c -> c.getClauseType() == Clause.ClauseType.NON_COMPETE);
        boolean hasPenalty     = clauses.stream().anyMatch(c -> c.getClauseType() == Clause.ClauseType.PENALTY && c.getSeverityScore() != null && c.getSeverityScore() > 60);
        boolean hasIndemnity   = clauses.stream().anyMatch(c -> c.getClauseType() == Clause.ClauseType.INDEMNIFICATION);
        boolean hasArbitration = clauses.stream().anyMatch(c -> c.getClauseType() == Clause.ClauseType.ARBITRATION);
        boolean hasDeposit     = clauses.stream().anyMatch(c -> c.getClauseType() == Clause.ClauseType.SECURITY_DEPOSIT);

        if (hasNonCompete)
            recs.add("Get a lawyer to review the non-compete clause — it may be void under Section 27 of the Indian Contract Act.");
        if (hasPenalty)
            recs.add("Negotiate the penalty clause — courts can reduce grossly disproportionate penalties under Section 74 of the Indian Contract Act.");
        if (hasIndemnity)
            recs.add("The indemnification clause is one-sided. Request mutual indemnification to balance liability.");
        if (hasArbitration)
            recs.add("Verify the arbitration seat city. Ensure arbitration is institutional (DIAC/ICC) rather than ad-hoc.");
        if (hasDeposit && docType == Document.DocumentType.RENTAL)
            recs.add("Security deposit must not exceed 2 months' rent under the Model Tenancy Act 2021. Verify this cap is honored.");

        if (recs.isEmpty())
            recs.add("Review all highlighted clauses carefully before signing. Consider consulting a lawyer for clauses scored above 60.");

        recs.add("Always get the signed agreement in writing and retain a copy for your records.");
        return recs;
    }

    private String buildSummary(Document document, int score, int critical, int high, int total) {
        String riskLevel = score >= 76 ? "CRITICAL RISK" : score >= 51 ? "HIGH RISK" : score >= 26 ? "MODERATE RISK" : "LOW RISK";
        String docName   = document.getDocumentType() != null ?
                document.getDocumentType().name().replace("_", " ").toLowerCase() + " agreement" : "document";

        return String.format(
                "VakilSahay analyzed your %s and found %d clauses. Overall risk score: %d/100 (%s). " +
                        "%d clause(s) require immediate attention (%d critical, %d high risk). " +
                        "Review the flagged clauses carefully before signing.",
                docName, total, score, riskLevel, critical + high, critical, high
        );
    }

    private int countBySeverity(List<Clause> clauses, Severity severity) {
        return (int) clauses.stream().filter(c -> severity.equals(c.getSeverity())).count();
    }

    private String toJson(List<String> list) {
        try { return objectMapper.writeValueAsString(list); }
        catch (Exception e) { return "[]"; }
    }
}