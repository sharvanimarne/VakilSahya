package com.vakilsahay.service;

import com.vakilsahay.entity.Clause;
import com.vakilsahay.entity.Clause.ClauseType;
import com.vakilsahay.entity.Clause.Severity;
import com.vakilsahay.entity.Document.DocumentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * ClauseAnalyzerServiceTest — 17 unit tests for the proprietary
 * severity scoring algorithm and Indian legal clause taxonomy.
 *
 * © 2025 VakilSahay. All rights reserved.
 */
@DisplayName("ClauseAnalyzerService — Proprietary Scoring Algorithm Tests")
class ClauseAnalyzerServiceTest {

    private ClauseAnalyzerService analyzer;

    @BeforeEach
    void setUp() {
        analyzer = new ClauseAnalyzerService();
    }

    // =========================================================================
    // Clause Type Classification Tests
    // =========================================================================

    @Nested
    @DisplayName("Clause Type Classification")
    class ClassificationTests {

        @Test
        @DisplayName("Should classify non-compete clause correctly")
        void shouldClassifyNonCompete() {
            // Clause uses "shall not compete" and "competing business" — no TERMINATION keywords
            Clause c = buildClause("The Employee shall not compete with the Company or engage in any competing business for a period of 2 years after leaving the Company.");
            Clause result = analyzer.analyzeClause(c, DocumentType.EMPLOYMENT);
            assertThat(result.getClauseType()).isEqualTo(ClauseType.NON_COMPETE);
        }

        @Test
        @DisplayName("Should classify security deposit clause correctly")
        void shouldClassifySecurityDeposit() {
            // Clause uses "security deposit" which is the first keyword in SECURITY_DEPOSIT matcher
            Clause c = buildClause("The Tenant shall pay a security deposit equivalent to three months rent before taking possession of the premises.");
            Clause result = analyzer.analyzeClause(c, DocumentType.RENTAL);
            assertThat(result.getClauseType()).isEqualTo(ClauseType.SECURITY_DEPOSIT);
        }

        @Test
        @DisplayName("Should classify indemnification clause correctly")
        void shouldClassifyIndemnification() {
            Clause c = buildClause("The Service Provider shall indemnify and hold harmless the Client from any claims, damages, or liabilities arising from its services.");
            Clause result = analyzer.analyzeClause(c, DocumentType.GENERAL);
            assertThat(result.getClauseType()).isEqualTo(ClauseType.INDEMNIFICATION);
        }

        @Test
        @DisplayName("Should classify arbitration clause correctly")
        void shouldClassifyArbitration() {
            Clause c = buildClause("Any dispute shall be submitted to arbitration in accordance with the Arbitration and Conciliation Act 1996.");
            Clause result = analyzer.analyzeClause(c, DocumentType.GENERAL);
            assertThat(result.getClauseType()).isEqualTo(ClauseType.ARBITRATION);
        }

        @Test
        @DisplayName("Should classify interest rate clause correctly")
        void shouldClassifyInterestRate() {
            Clause c = buildClause("The Borrower shall pay interest at the rate of 18% per annum on the outstanding principal amount.");
            Clause result = analyzer.analyzeClause(c, DocumentType.LOAN);
            assertThat(result.getClauseType()).isEqualTo(ClauseType.INTEREST_RATE);
        }

        @Test
        @DisplayName("Should classify unknown clause as GENERAL")
        void shouldClassifyUnknownAsGeneral() {
            Clause c = buildClause("This is some random text that does not match any known legal clause pattern.");
            Clause result = analyzer.analyzeClause(c, DocumentType.GENERAL);
            assertThat(result.getClauseType()).isEqualTo(ClauseType.GENERAL);
        }
    }

    // =========================================================================
    // Severity Scoring Tests
    // =========================================================================

    @Nested
    @DisplayName("Severity Scoring Algorithm")
    class SeverityScoringTests {

        @Test
        @DisplayName("Non-compete with 'sole discretion' red flag should score HIGH or CRITICAL")
        void shouldEscalateNonCompeteWithRedFlag() {
            Clause c = buildClause("The Employee shall not compete with the Company, at the sole discretion of the management, for any period deemed necessary.");
            Clause result = analyzer.analyzeClause(c, DocumentType.EMPLOYMENT);
            assertThat(result.getSeverityScore()).isGreaterThan(75);
            assertThat(result.getSeverity()).isIn(Severity.HIGH, Severity.CRITICAL);
        }

        @Test
        @DisplayName("Default clause in loan context should score CRITICAL")
        void shouldScoreLoanDefaultAsCritical() {
            Clause c = buildClause("On event of default of payment, the Lender may demand immediate repayment of the entire outstanding amount without notice.");
            Clause result = analyzer.analyzeClause(c, DocumentType.LOAN);
            assertThat(result.getSeverity()).isEqualTo(Severity.CRITICAL);
        }

        @Test
        @DisplayName("Protective clause with 'mutual agreement' should lower score")
        void shouldReduceScoreForProtectiveClauses() {
            Clause c1 = buildClause("Either party may terminate this agreement by mutual agreement with 30 days reasonable notice.");
            Clause c2 = buildClause("The Company may terminate this agreement at any time without notice at its sole discretion.");
            Clause r1 = analyzer.analyzeClause(c1, DocumentType.GENERAL);
            Clause r2 = analyzer.analyzeClause(c2, DocumentType.GENERAL);
            assertThat(r1.getSeverityScore()).isLessThan(r2.getSeverityScore());
        }

        @Test
        @DisplayName("Score should always be in range 0–100")
        void scoreShouldAlwaysBeBounded() {
            Clause c = buildClause("The Employee shall not compete, shall waive all rights, irrevocable, perpetual, worldwide, in perpetuity, without notice, unconditional, absolute discretion.");
            Clause result = analyzer.analyzeClause(c, DocumentType.EMPLOYMENT);
            assertThat(result.getSeverityScore()).isBetween(0, 100);
        }

        @Test
        @DisplayName("LOW score should map to LOW severity")
        void shouldMapLowScoreToLowSeverity() {
            Clause c = buildClause("This agreement shall be governed by the laws of India.");
            Clause result = analyzer.analyzeClause(c, DocumentType.GENERAL);
            assertThat(result.getSeverity()).isEqualTo(Severity.LOW);
        }
    }

    // =========================================================================
    // Plain English and Law Reference Tests
    // =========================================================================

    @Nested
    @DisplayName("Plain English and Law References")
    class PlainEnglishTests {

        @Test
        @DisplayName("Should generate non-null plain English for every clause")
        void shouldGeneratePlainEnglish() {
            Clause c = buildClause("The Tenant shall pay a security deposit of three months rent.");
            Clause result = analyzer.analyzeClause(c, DocumentType.RENTAL);
            assertThat(result.getPlainEnglish()).isNotBlank();
        }

        @Test
        @DisplayName("Should generate non-null recommendation for high severity clauses")
        void shouldGenerateRecommendation() {
            Clause c = buildClause("The Employee shall not compete with the Company for two years after termination.");
            Clause result = analyzer.analyzeClause(c, DocumentType.EMPLOYMENT);
            assertThat(result.getRecommendation()).isNotBlank();
        }

        @Test
        @DisplayName("Should attach correct Indian law reference for arbitration")
        void shouldAttachCorrectLawReference() {
            Clause c = buildClause("All disputes shall be resolved through arbitration under the Arbitration Act.");
            Clause result = analyzer.analyzeClause(c, DocumentType.GENERAL);
            assertThat(result.getLawReference()).contains("Arbitration");
        }

        @Test
        @DisplayName("Non-compete plain English should mention Section 27")
        void plainEnglishForNonCompeteShouldMentionSection27() {
            Clause c = buildClause("The Employee shall not compete with the Company in any capacity.");
            Clause result = analyzer.analyzeClause(c, DocumentType.EMPLOYMENT);
            assertThat(result.getPlainEnglish()).containsIgnoringCase("27");
        }
    }

    // =========================================================================
    // Document Type Detection Tests
    // =========================================================================

    @Nested
    @DisplayName("Document Type Detection")
    class DocumentTypeDetectionTests {

        @Test
        @DisplayName("Should detect rental agreement")
        void shouldDetectRentalAgreement() {
            String text = "This Rental Agreement is entered into between the Landlord and Tenant for the premises located at...";
            assertThat(analyzer.detectDocumentType(text)).isEqualTo(DocumentType.RENTAL);
        }

        @Test
        @DisplayName("Should detect employment agreement")
        void shouldDetectEmploymentAgreement() {
            String text = "This Employment Agreement between the Employee and Employer covers salary, designation, and terms...";
            assertThat(analyzer.detectDocumentType(text)).isEqualTo(DocumentType.EMPLOYMENT);
        }

        @Test
        @DisplayName("Should detect loan agreement")
        void shouldDetectLoanAgreement() {
            String text = "This Loan Agreement between the Borrower and Lender covers principal amount, EMI, and repayment schedule...";
            assertThat(analyzer.detectDocumentType(text)).isEqualTo(DocumentType.LOAN);
        }

        @Test
        @DisplayName("Should default to GENERAL for unrecognized documents")
        void shouldDefaultToGeneral() {
            String text = "This is some random text with no legal document indicators.";
            assertThat(analyzer.detectDocumentType(text)).isEqualTo(DocumentType.GENERAL);
        }
    }

    // =========================================================================
    // Helper
    // =========================================================================

    private Clause buildClause(String text) {
        return Clause.builder()
                .clauseNumber(1)
                .originalText(text)
                .build();
    }
}