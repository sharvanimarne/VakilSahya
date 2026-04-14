package com.vakilsahay.dto.response;

import com.vakilsahay.entity.*;
import com.vakilsahay.entity.Clause.ClauseType;
import com.vakilsahay.entity.Clause.Severity;
import com.vakilsahay.entity.Document.DocumentStatus;
import com.vakilsahay.entity.Document.DocumentType;
import java.time.LocalDateTime;
import java.util.List;

public class Responses {

    public record AuthResponse(String token, String tokenType, UserSummary user) {
        public static AuthResponse of(String token, User user) {
            return new AuthResponse(token, "Bearer", UserSummary.from(user));
        }
    }

    public record UserSummary(Long id, String email, String fullName, String role, LocalDateTime createdAt) {
        public static UserSummary from(User u) {
            return new UserSummary(u.getId(), u.getEmail(), u.getFullName(), u.getRole().name(), u.getCreatedAt());
        }
    }

    public record DocumentResponse(Long id, String originalName, String fileType, Long fileSize,
                                   DocumentType documentType, DocumentStatus status, LocalDateTime createdAt,
                                   LocalDateTime updatedAt, AnalysisReportResponse report) {
        public static DocumentResponse from(Document d) {
            AnalysisReportResponse report = d.getAnalysisReport() != null
                    ? AnalysisReportResponse.from(d.getAnalysisReport()) : null;
            return new DocumentResponse(d.getId(), d.getOriginalName(), d.getFileType(),
                    d.getFileSize(), d.getDocumentType(), d.getStatus(),
                    d.getCreatedAt(), d.getUpdatedAt(), report);
        }
    }

    public record AnalysisReportResponse(Long id, int overallRiskScore, String riskLevel,
                                         int totalClauses, int criticalCount, int highCount, int mediumCount, int lowCount,
                                         String summary, List<String> topConcerns, List<String> recommendations,
                                         LocalDateTime createdAt) {
        @SuppressWarnings("unchecked")
        public static AnalysisReportResponse from(AnalysisReport r) {
            com.fasterxml.jackson.databind.ObjectMapper om = new com.fasterxml.jackson.databind.ObjectMapper();
            List<String> concerns = List.of();
            List<String> recs = List.of();
            try {
                if (r.getTopConcerns() != null) concerns = om.readValue(r.getTopConcerns(), List.class);
                if (r.getRecommendations() != null) recs = om.readValue(r.getRecommendations(), List.class);
            } catch (Exception ignored) {}
            String riskLevel = r.getOverallRiskScore() >= 76 ? "CRITICAL"
                    : r.getOverallRiskScore() >= 51 ? "HIGH"
                    : r.getOverallRiskScore() >= 26 ? "MEDIUM" : "LOW";
            return new AnalysisReportResponse(r.getId(), r.getOverallRiskScore(), riskLevel,
                    r.getTotalClauses(), r.getCriticalCount(), r.getHighCount(),
                    r.getMediumCount(), r.getLowCount(), r.getSummary(),
                    concerns, recs, r.getCreatedAt());
        }
    }

    public record ClauseResponse(Long id, int clauseNumber, String originalText,
                                 String plainEnglish, ClauseType clauseType, Severity severity,
                                 Integer severityScore, String lawReference, String recommendation) {
        public static ClauseResponse from(Clause c) {
            return new ClauseResponse(c.getId(), c.getClauseNumber(), c.getOriginalText(),
                    c.getPlainEnglish(), c.getClauseType(), c.getSeverity(),
                    c.getSeverityScore(), c.getLawReference(), c.getRecommendation());
        }
    }

    public record ErrorResponse(int status, String error, String message, LocalDateTime timestamp) {
        public static ErrorResponse of(int status, String error, String message) {
            return new ErrorResponse(status, error, message, LocalDateTime.now());
        }
    }

    public record AdminStatsResponse(long totalUsers, long totalDocuments, long totalAnalyses) {}
}