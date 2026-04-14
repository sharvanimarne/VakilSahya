package com.vakilsahay.controller;

import com.vakilsahay.dto.response.Responses.DocumentResponse;
import com.vakilsahay.dto.response.Responses.AnalysisReportResponse;
import com.vakilsahay.dto.response.Responses.ClauseResponse;
import com.vakilsahay.entity.Clause;
import com.vakilsahay.entity.Document;
import com.vakilsahay.entity.User;
import com.vakilsahay.service.DocumentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * DocumentController — full CRUD + analysis for legal documents.
 *
 * POST   /api/documents/upload              — upload & trigger analysis
 * GET    /api/documents                     — list user's documents
 * GET    /api/documents/{id}               — get document + report
 * GET    /api/documents/{id}/clauses        — all clauses (filterable)
 * GET    /api/documents/{id}/report         — full analysis report
 * PUT    /api/documents/{id}/reanalyze      — rerun analysis
 * DELETE /api/documents/{id}               — delete
 *
 * © 2025 VakilSahay
 */
@RestController
@RequestMapping("/documents")
@RequiredArgsConstructor
@SecurityRequirement(name = "Bearer Authentication")
@Tag(name = "Documents", description = "Upload and analyze legal documents")
public class DocumentController {

    private final DocumentService documentService;

    // ─── CREATE ─────────────────────────────────────────────────────────────

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload a legal document for analysis",
            description = "Accepts PDF, DOCX, or TXT. Analysis runs asynchronously — poll GET /documents/{id} for status.")
    public ResponseEntity<DocumentResponse> upload(
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal User currentUser) {

        Document doc = documentService.uploadDocument(file, currentUser);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(DocumentResponse.from(doc));
    }

    // ─── READ ────────────────────────────────────────────────────────────────

    @GetMapping
    @Operation(summary = "List all documents for the authenticated user")
    public ResponseEntity<Page<DocumentResponse>> listDocuments(
            @AuthenticationPrincipal User currentUser,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<DocumentResponse> result = documentService
                .getUserDocuments(currentUser, pageable)
                .map(DocumentResponse::from);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a specific document with its analysis report")
    public ResponseEntity<DocumentResponse> getDocument(
            @PathVariable Long id,
            @AuthenticationPrincipal User currentUser) {

        Document doc = documentService.getDocument(id, currentUser);
        return ResponseEntity.ok(DocumentResponse.from(doc));
    }

    @GetMapping("/{id}/clauses")
    @Operation(summary = "Get all clauses for a document",
            description = "Optionally filter by severity: LOW, MEDIUM, HIGH, CRITICAL")
    public ResponseEntity<List<ClauseResponse>> getClauses(
            @PathVariable Long id,
            @Parameter(description = "Filter by severity level")
            @RequestParam(required = false) Clause.Severity severity,
            @AuthenticationPrincipal User currentUser) {

        List<ClauseResponse> clauses = documentService
                .getClauses(id, currentUser, severity)
                .stream()
                .map(ClauseResponse::from)
                .toList();
        return ResponseEntity.ok(clauses);
    }

    @GetMapping("/{id}/report")
    @Operation(summary = "Get the full analysis report for a document")
    public ResponseEntity<AnalysisReportResponse> getReport(
            @PathVariable Long id,
            @AuthenticationPrincipal User currentUser) {

        return ResponseEntity.ok(
                AnalysisReportResponse.from(documentService.getReport(id, currentUser))
        );
    }

    // ─── UPDATE ──────────────────────────────────────────────────────────────

    @PutMapping("/{id}/reanalyze")
    @Operation(summary = "Re-run analysis on a previously uploaded document")
    public ResponseEntity<DocumentResponse> reanalyze(
            @PathVariable Long id,
            @AuthenticationPrincipal User currentUser) {

        Document doc = documentService.reanalyze(id, currentUser);
        return ResponseEntity.accepted().body(DocumentResponse.from(doc));
    }

    // ─── DELETE ──────────────────────────────────────────────────────────────

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a document and all associated analysis data")
    public ResponseEntity<Void> deleteDocument(
            @PathVariable Long id,
            @AuthenticationPrincipal User currentUser) {

        documentService.deleteDocument(id, currentUser);
        return ResponseEntity.noContent().build();
    }
}