package com.vakilsahay.service;

import com.vakilsahay.exception.DocumentProcessingException;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.Tika;
import org.apache.tika.exception.TikaException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * DocumentParserService — extracts raw text from PDF/DOCX and splits into clauses.
 * Uses Apache Tika for format-agnostic text extraction.
 * © 2025 VakilSahay
 */
@Service
@Slf4j
public class DocumentParserService {

    private final Tika tika = new Tika();

    // Tika default max string length (set large for long legal docs)
    private static final int MAX_STRING_LENGTH = 500_000;

    // Patterns that indicate a clause boundary in legal documents
    private static final List<Pattern> CLAUSE_DELIMITERS = List.of(
            Pattern.compile("^\\d+\\.\\s+[A-Z]", Pattern.MULTILINE),          // "1. TERMINATION"
            Pattern.compile("^\\d+\\.\\d+\\s+[A-Z]", Pattern.MULTILINE),      // "1.1 Definitions"
            Pattern.compile("^Article\\s+\\d+", Pattern.MULTILINE | Pattern.CASE_INSENSITIVE),
            Pattern.compile("^Section\\s+\\d+", Pattern.MULTILINE | Pattern.CASE_INSENSITIVE),
            Pattern.compile("^Clause\\s+\\d+", Pattern.MULTILINE | Pattern.CASE_INSENSITIVE),
            Pattern.compile("^\\([a-z]\\)\\s", Pattern.MULTILINE),             // "(a) ..."
            Pattern.compile("^[A-Z][A-Z\\s]{5,}\\n", Pattern.MULTILINE)       // "TERMINATION\n"
    );

    private static final int MIN_CLAUSE_LENGTH = 50;   // ignore very short fragments
    private static final int MAX_CLAUSE_LENGTH = 2000; // split very long blocks

    /**
     * Extract text from uploaded file (PDF, DOCX, TXT).
     */
    public String extractText(MultipartFile file) {
        try (InputStream is = file.getInputStream()) {
            tika.setMaxStringLength(MAX_STRING_LENGTH);
            String text = tika.parseToString(is);
            log.debug("Extracted {} chars from {}", text.length(), file.getOriginalFilename());
            return cleanText(text);
        } catch (IOException | TikaException e) {
            log.error("Failed to parse document {}: {}", file.getOriginalFilename(), e.getMessage());
            throw new DocumentProcessingException("Failed to extract text from document: " + e.getMessage());
        }
    }

    /**
     * Split document text into individual clauses.
     * Returns list of clause texts, each representing one legal obligation.
     */
    public List<String> splitIntoClauses(String documentText) {
        List<String> clauses = new ArrayList<>();
        String[] paragraphs = documentText.split("\\n{2,}");

        for (String para : paragraphs) {
            String trimmed = para.trim();
            if (trimmed.length() < MIN_CLAUSE_LENGTH) continue;

            if (trimmed.length() > MAX_CLAUSE_LENGTH) {
                // Split long paragraphs at sentence boundaries
                List<String> sentences = splitAtSentences(trimmed);
                clauses.addAll(sentences);
            } else {
                clauses.add(trimmed);
            }
        }

        // If no paragraph breaks found (single-block document), split at sentences
        if (clauses.isEmpty() || (clauses.size() == 1 && clauses.get(0).length() > MAX_CLAUSE_LENGTH)) {
            clauses = splitAtSentences(documentText);
        }

        log.debug("Split document into {} clauses", clauses.size());
        return clauses;
    }

    private List<String> splitAtSentences(String text) {
        List<String> result = new ArrayList<>();
        String[] sentences = text.split("(?<=[.!?])\\s+(?=[A-Z])");
        StringBuilder buffer = new StringBuilder();

        for (String sentence : sentences) {
            buffer.append(sentence).append(" ");
            if (buffer.length() >= MIN_CLAUSE_LENGTH * 3) {
                result.add(buffer.toString().trim());
                buffer.setLength(0);
            }
        }
        if (!buffer.isEmpty()) result.add(buffer.toString().trim());
        return result;
    }

    private String cleanText(String raw) {
        return raw
                .replaceAll("\r\n", "\n")
                .replaceAll("\r", "\n")
                .replaceAll("\t", " ")
                .replaceAll(" {2,}", " ")
                .replaceAll("\n{3,}", "\n\n")
                .trim();
    }

    /**
     * Validate file type and size.
     */
    public void validateFile(MultipartFile file) {
        if (file.isEmpty()) throw new DocumentProcessingException("File is empty");

        String contentType = file.getContentType();
        if (contentType == null || !isAllowedType(contentType)) {
            throw new DocumentProcessingException(
                    "Unsupported file type: " + contentType + ". Allowed: PDF, DOCX, TXT");
        }

        long maxSize = 10L * 1024 * 1024; // 10MB
        if (file.getSize() > maxSize) {
            throw new DocumentProcessingException("File too large. Maximum size is 10MB.");
        }
    }

    private boolean isAllowedType(String contentType) {
        return contentType.equals("application/pdf")
                || contentType.equals("application/vnd.openxmlformats-officedocument.wordprocessingml.document")
                || contentType.equals("text/plain")
                || contentType.startsWith("text/");
    }
}