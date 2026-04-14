package com.vakilsahay.service;

import com.vakilsahay.entity.*;
import com.vakilsahay.entity.Document.DocumentStatus;
import com.vakilsahay.entity.Document.DocumentType;
import com.vakilsahay.exception.DocumentNotFoundException;
import com.vakilsahay.exception.DocumentProcessingException;
import com.vakilsahay.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * DocumentService — orchestrates the full document lifecycle:
 * Upload → Parse → Analyze → Generate Report → Persist
 * © 2025 VakilSahay
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DocumentService {

    private final DocumentRepository      documentRepository;
    private final ClauseRepository        clauseRepository;
    private final AnalysisReportRepository reportRepository;
    private final UsageLogRepository      usageLogRepository;
    private final DocumentParserService   parserService;
    private final ClauseAnalyzerService   analyzerService;
    private final ReportGeneratorService  reportGeneratorService;

    /**
     * Step 1: Validate and persist the uploaded file metadata.
     * Returns immediately — analysis is triggered asynchronously.
     */
    @Transactional
    public Document uploadDocument(MultipartFile file, User user) {
        parserService.validateFile(file);

        Document document = Document.builder()
                .user(user)
                .fileName(generateStoredName(file))
                .originalName(file.getOriginalFilename())
                .fileType(file.getContentType())
                .fileSize(file.getSize())
                .status(DocumentStatus.UPLOADED)
                .build();

        Document saved = documentRepository.save(document);
        logUsage(user, saved.getId(), "UPLOAD");

        // Kick off async analysis pipeline
        analyzeAsync(saved.getId(), file, user);

        return saved;
    }

    /**
     * Step 2 (async): Parse text → detect doc type → analyze clauses → generate report.
     */
    @Async
    @Transactional
    public void analyzeAsync(Long documentId, MultipartFile file, User user) {
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new DocumentNotFoundException(documentId));

        try {
            // Mark as analyzing
            document.setStatus(DocumentStatus.ANALYZING);
            documentRepository.save(document);

            // Extract text
            String rawText = parserService.extractText(file);
            document.setRawText(rawText);

            // Detect document type
            DocumentType docType = analyzerService.detectDocumentType(rawText);
            document.setDocumentType(docType);
            log.info("Document {} detected as {}", documentId, docType);

            // Split into clauses
            List<String> clauseTexts = parserService.splitIntoClauses(rawText);

            // Analyze each clause
            List<Clause> analyzedClauses = new java.util.ArrayList<>();
            for (int i = 0; i < clauseTexts.size(); i++) {
                Clause clause = Clause.builder()
                        .document(document)
                        .clauseNumber(i + 1)
                        .originalText(clauseTexts.get(i))
                        .build();

                Clause analyzed = analyzerService.analyzeClause(clause, docType);
                analyzedClauses.add(analyzed);
            }
            clauseRepository.saveAll(analyzedClauses);

            // Generate overall report
            AnalysisReport report = reportGeneratorService.generateReport(document, analyzedClauses);
            reportRepository.save(report);

            // Mark complete
            document.setStatus(DocumentStatus.COMPLETED);
            documentRepository.save(document);

            logUsage(user, documentId, "ANALYZE");
            log.info("Analysis complete for document {} — risk score: {}", documentId, report.getOverallRiskScore());

        } catch (Exception e) {
            log.error("Analysis failed for document {}: {}", documentId, e.getMessage(), e);
            document.setStatus(DocumentStatus.FAILED);
            documentRepository.save(document);
        }
    }

    /**
     * Get paginated list of documents for a user.
     */
    @Transactional(readOnly = true)
    public Page<Document> getUserDocuments(User user, Pageable pageable) {
        return documentRepository.findByUserIdOrderByCreatedAtDesc(user.getId(), pageable);
    }

    /**
     * Get a specific document (with ownership check).
     */
    @Transactional(readOnly = true)
    public Document getDocument(Long documentId, User user) {
        return documentRepository.findByIdAndUserId(documentId, user.getId())
                .orElseThrow(() -> new DocumentNotFoundException(documentId));
    }

    /**
     * Get all clauses for a document, optionally filtered by severity.
     */
    @Transactional(readOnly = true)
    public List<Clause> getClauses(Long documentId, User user, Clause.Severity severity) {
        // Verify ownership
        getDocument(documentId, user);

        if (severity != null) {
            return clauseRepository.findByDocumentIdAndSeverityOrderByClauseNumber(documentId, severity);
        }
        return clauseRepository.findByDocumentIdOrderByClauseNumber(documentId);
    }

    /**
     * Get analysis report for a document.
     */
    @Transactional(readOnly = true)
    public AnalysisReport getReport(Long documentId, User user) {
        getDocument(documentId, user);
        return reportRepository.findByDocumentId(documentId)
                .orElseThrow(() -> new DocumentProcessingException("Report not yet available. Analysis may still be in progress."));
    }

    /**
     * Re-run analysis on existing document text.
     */
    @Transactional
    public Document reanalyze(Long documentId, User user) {
        Document document = getDocument(documentId, user);

        if (document.getRawText() == null || document.getRawText().isBlank()) {
            throw new DocumentProcessingException("Cannot reanalyze — original text not available.");
        }
        if (document.getStatus() == DocumentStatus.ANALYZING) {
            throw new DocumentProcessingException("Analysis already in progress.");
        }

        // Clear old analysis
        clauseRepository.deleteAllByDocumentId(documentId);
        reportRepository.deleteByDocumentId(documentId);

        document.setStatus(DocumentStatus.ANALYZING);
        Document saved = documentRepository.save(document);

        reanalyzeAsync(saved, user);
        return saved;
    }

    @Async
    @Transactional
    public void reanalyzeAsync(Document document, User user) {
        try {
            String rawText = document.getRawText();
            DocumentType docType = analyzerService.detectDocumentType(rawText);
            document.setDocumentType(docType);

            List<String> clauseTexts = parserService.splitIntoClauses(rawText);
            List<Clause> analyzedClauses = new java.util.ArrayList<>();

            for (int i = 0; i < clauseTexts.size(); i++) {
                Clause clause = Clause.builder()
                        .document(document)
                        .clauseNumber(i + 1)
                        .originalText(clauseTexts.get(i))
                        .build();
                analyzedClauses.add(analyzerService.analyzeClause(clause, docType));
            }

            clauseRepository.saveAll(analyzedClauses);
            AnalysisReport report = reportGeneratorService.generateReport(document, analyzedClauses);
            reportRepository.save(report);

            document.setStatus(DocumentStatus.COMPLETED);
            documentRepository.save(document);

            logUsage(user, document.getId(), "REANALYZE");
        } catch (Exception e) {
            log.error("Reanalysis failed for document {}: {}", document.getId(), e.getMessage());
            document.setStatus(DocumentStatus.FAILED);
            documentRepository.save(document);
        }
    }

    /**
     * Delete document and all associated data (cascade).
     */
    @Transactional
    public void deleteDocument(Long documentId, User user) {
        Document document = getDocument(documentId, user);
        documentRepository.delete(document);
        logUsage(user, documentId, "DELETE");
        log.info("Document {} deleted by user {}", documentId, user.getId());
    }

    // -------------------------------------------------------------------------

    private String generateStoredName(MultipartFile file) {
        String ext = "";
        String original = file.getOriginalFilename();
        if (original != null && original.contains(".")) {
            ext = original.substring(original.lastIndexOf("."));
        }
        return java.util.UUID.randomUUID() + ext;
    }

    private void logUsage(User user, Long documentId, String action) {
        UsageLog log = UsageLog.builder()
                .user(user)
                .documentId(documentId)
                .action(action)
                .build();
        usageLogRepository.save(log);
    }
}