package com.vakilsahay.service;

import com.vakilsahay.entity.Clause;
import com.vakilsahay.entity.Clause.ClauseType;
import com.vakilsahay.entity.Clause.Severity;
import com.vakilsahay.entity.Document.DocumentType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Pattern;

/**
 * ClauseAnalyzerService — VakilSahay's Proprietary Clause Severity Scoring Engine
 *
 * COPYRIGHT NOTICE: This service implements the "VakilSahay Indian Legal Clause
 * Taxonomy and Weighted Severity Scoring Algorithm" which is protected under the
 * Indian Copyright Act 1957, Section 2(o) as an original literary work.
 *
 * PATENT PENDING: Provisional patent application filed at the Indian Patent Office.
 * Application of a multi-factor weighted scoring system for legal clause risk
 * assessment mapped to Indian statutory law references.
 *
 * © 2025 VakilSahay. All rights reserved. Unauthorized reproduction or use of
 * this algorithm is strictly prohibited.
 *
 * Algorithm Overview:
 * 1. Classify clause type using keyword taxonomy (Indian law-specific)
 * 2. Apply base severity score per clause type
 * 3. Apply document-context multipliers
 * 4. Apply red-flag keyword penalties
 * 5. Apply protective-clause bonuses
 * 6. Normalize to 0–100
 * 7. Map score to Severity enum + generate plain English + law reference
 */
@Service
@Slf4j
public class ClauseAnalyzerService {

    // =========================================================================
    // PROPRIETARY: Indian Legal Clause Taxonomy
    // Base severity scores per clause type (0–100 scale)
    // These scores are derived from analysis of Indian court judgements and
    // consumer protection guidelines — © 2025 VakilSahay
    // =========================================================================
    private static final Map<ClauseType, Integer> BASE_SCORES = new EnumMap<>(ClauseType.class);
    static {
        // High-risk universal clauses
        BASE_SCORES.put(ClauseType.INDEMNIFICATION,   75);
        BASE_SCORES.put(ClauseType.LIABILITY,         70);
        BASE_SCORES.put(ClauseType.TERMINATION,       60);
        BASE_SCORES.put(ClauseType.PENALTY,           65);
        BASE_SCORES.put(ClauseType.ARBITRATION,       55);
        BASE_SCORES.put(ClauseType.NON_COMPETE,       72);
        BASE_SCORES.put(ClauseType.IP_OWNERSHIP,      68);
        // Medium-risk clauses
        BASE_SCORES.put(ClauseType.CONFIDENTIALITY,   45);
        BASE_SCORES.put(ClauseType.JURISDICTION,      40);
        BASE_SCORES.put(ClauseType.ASSIGNMENT,        42);
        BASE_SCORES.put(ClauseType.GOVERNING_LAW,     18);
        BASE_SCORES.put(ClauseType.FORCE_MAJEURE,     35);
        BASE_SCORES.put(ClauseType.DISPUTE_RESOLUTION,38);
        // Rental-specific
        BASE_SCORES.put(ClauseType.RENT_ESCALATION,  62);
        BASE_SCORES.put(ClauseType.SECURITY_DEPOSIT,  55);
        BASE_SCORES.put(ClauseType.LOCK_IN_PERIOD,    58);
        BASE_SCORES.put(ClauseType.SUBLETTING,        45);
        BASE_SCORES.put(ClauseType.MAINTENANCE,       35);
        // Employment-specific
        BASE_SCORES.put(ClauseType.NOTICE_PERIOD,     40);
        BASE_SCORES.put(ClauseType.PROBATION,         30);
        BASE_SCORES.put(ClauseType.PERFORMANCE,       38);
        // Loan-specific
        BASE_SCORES.put(ClauseType.INTEREST_RATE,     70);
        BASE_SCORES.put(ClauseType.PREPAYMENT,        55);
        BASE_SCORES.put(ClauseType.DEFAULT,           80);
        BASE_SCORES.put(ClauseType.COLLATERAL,        75);
        BASE_SCORES.put(ClauseType.GUARANTOR,         72);
        // Fallbacks
        BASE_SCORES.put(ClauseType.AMENDMENT,         25);
        BASE_SCORES.put(ClauseType.WAIVER,            28);
        BASE_SCORES.put(ClauseType.ENTIRE_AGREEMENT,  20);
        BASE_SCORES.put(ClauseType.GENERAL,           20);
    }

    // =========================================================================
    // PROPRIETARY: Keyword taxonomy — maps text patterns to clause types
    // Keywords selected based on Indian legal drafting conventions
    // © 2025 VakilSahay Indian Legal Clause Taxonomy v1.0
    // =========================================================================
    // IMPORTANT: Order matters — more specific multi-word phrases MUST come before
    // generic single-word matchers to avoid false positives.
    // e.g. NON_COMPETE before TERMINATION (clause may contain "after termination")
    //      SECURITY_DEPOSIT before CONFIDENTIALITY
    // © 2025 VakilSahay Indian Legal Clause Taxonomy v1.1
    private static final List<ClauseMatcher> MATCHERS = List.of(
            // ── Specific compound phrases first ──────────────────────────────────
            new ClauseMatcher(ClauseType.NON_COMPETE,
                    List.of("non-compete", "non compete", "not compete", "shall not compete",
                            "shall not engage in", "competing business", "not engage in any competing")),
            new ClauseMatcher(ClauseType.SECURITY_DEPOSIT,
                    List.of("security deposit", "advance deposit", "refundable deposit", "caution deposit")),
            new ClauseMatcher(ClauseType.LOCK_IN_PERIOD,
                    List.of("lock-in period", "lock in period", "minimum stay", "cannot vacate before",
                            "lock-in of", "lock in of")),
            new ClauseMatcher(ClauseType.RENT_ESCALATION,
                    List.of("rent shall increase", "annual increment", "escalation of rent",
                            "revision of rent", "rent hike")),
            new ClauseMatcher(ClauseType.ARBITRATION,
                    List.of("arbitrat", "arbitrator", "arbitral")),
            new ClauseMatcher(ClauseType.DISPUTE_RESOLUTION,
                    List.of("dispute resolution", "resolve dispute", "mediation", "conciliation")),
            new ClauseMatcher(ClauseType.FORCE_MAJEURE,
                    List.of("force majeure", "act of god", "natural disaster", "circumstances beyond")),
            new ClauseMatcher(ClauseType.IP_OWNERSHIP,
                    List.of("intellectual property", "work for hire", "all inventions",
                            "moral rights", "copyright vests")),
            new ClauseMatcher(ClauseType.INDEMNIFICATION,
                    List.of("indemnif", "indemnity", "hold harmless", "save harmless")),
            new ClauseMatcher(ClauseType.GOVERNING_LAW,
                    List.of("governed by", "laws of india", "indian law", "law of the land")),
            new ClauseMatcher(ClauseType.JURISDICTION,
                    List.of("jurisdiction", "courts of", "subject to courts")),
            new ClauseMatcher(ClauseType.NOTICE_PERIOD,
                    List.of("notice period", "prior notice", "days' notice", "months notice", "serving notice")),
            new ClauseMatcher(ClauseType.INTEREST_RATE,
                    List.of("interest rate", "rate of interest", "per annum", "monthly interest", "compound interest")),
            new ClauseMatcher(ClauseType.DEFAULT,
                    List.of("event of default", "default payment", "failure to pay", "on default")),
            new ClauseMatcher(ClauseType.COLLATERAL,
                    List.of("collateral", "mortgage", "pledge", "hypothecate", "lien")),
            new ClauseMatcher(ClauseType.GUARANTOR,
                    List.of("guarantor", "surety", "personal guarantee")),
            new ClauseMatcher(ClauseType.SUBLETTING,
                    List.of("sublet", "sub-let", "sublease", "underlet")),
            new ClauseMatcher(ClauseType.AMENDMENT,
                    List.of("amendment", "modification", "shall be amended", "changes to this agreement")),
            new ClauseMatcher(ClauseType.ASSIGNMENT,
                    List.of("transfer rights", "novation", "assign this agreement")),
            // ── Generic single-word matchers last ────────────────────────────────
            new ClauseMatcher(ClauseType.CONFIDENTIALITY,
                    List.of("confidential", "non-disclosure", "nda", "proprietary information", "trade secret")),
            new ClauseMatcher(ClauseType.PENALTY,
                    List.of("penalty", "penalt", "liquidated damage", "forfeit", "fine of")),
            new ClauseMatcher(ClauseType.LIABILITY,
                    List.of("liabilit", "liable", "not responsible", "no liability", "limitation of liability")),
            new ClauseMatcher(ClauseType.TERMINATION,
                    List.of("terminat", "cancel", "end of agreement", "expiry", "expire", "end this agreement"))
    );

    // =========================================================================
    // PROPRIETARY: Red-flag penalty keywords — add to base score
    // Words that indicate unfair/one-sided clauses — © 2025 VakilSahay
    // =========================================================================
    private static final Map<Pattern, Integer> RED_FLAG_PENALTIES = new LinkedHashMap<>();
    static {
        addPenalty("sole discretion",               +20);
        addPenalty("without notice",                +18);
        addPenalty("without liability",             +15);
        addPenalty("irrevocable",                   +12);
        addPenalty("perpetual",                     +10);
        addPenalty("unconditional",                 +10);
        addPenalty("at any time",                   +8);
        addPenalty("not refundable",                +15);
        addPenalty("non-refundable",                +15);
        addPenalty("waive all rights",              +18);
        addPenalty("worldwide",                     +8);
        addPenalty("in perpetuity",                 +10);
        addPenalty("absolute discretion",           +18);
        addPenalty("notwithstanding",               +5);
        addPenalty("shall not be entitled",         +12);
        addPenalty("no right to",                   +12);
        addPenalty("shall bear all costs",          +14);
        addPenalty("without prior notice",          +18);
        addPenalty("penalty.*exceeding",            +15);
        addPenalty("interest.*24%|interest.*36%",   +20);
    }

    // =========================================================================
    // PROPRIETARY: Protective clause bonuses — reduce from base score
    // Words that indicate fair/balanced/legally protective language
    // =========================================================================
    private static final Map<Pattern, Integer> PROTECTIVE_BONUSES = new LinkedHashMap<>();
    static {
        addBonus("mutual agreement",      -10);
        addBonus("both parties",          -8);
        addBonus("reasonable notice",     -10);
        addBonus("as per law",            -8);
        addBonus("statutory rights",      -10);
        addBonus("consumer protection",   -12);
        addBonus("negotiated",            -8);
        addBonus("either party",          -6);
        addBonus("refundable",            -10);
        addBonus("written consent",       -8);
    }

    // =========================================================================
    // PROPRIETARY: Indian law reference database
    // Maps clause types to relevant Indian statutes — © 2025 VakilSahay
    // =========================================================================
    private static final Map<ClauseType, String> LAW_REFERENCES = new EnumMap<>(ClauseType.class);
    static {
        LAW_REFERENCES.put(ClauseType.TERMINATION,       "Indian Contract Act 1872, §73 (Compensation for breach)");
        LAW_REFERENCES.put(ClauseType.PENALTY,           "Indian Contract Act 1872, §74 (Penalty stipulations)");
        LAW_REFERENCES.put(ClauseType.INDEMNIFICATION,   "Indian Contract Act 1872, §124–125 (Indemnity)");
        LAW_REFERENCES.put(ClauseType.LIABILITY,         "Consumer Protection Act 2019, §2(7) (Deficiency of service)");
        LAW_REFERENCES.put(ClauseType.ARBITRATION,       "Arbitration & Conciliation Act 1996 (as amended 2021)");
        LAW_REFERENCES.put(ClauseType.NON_COMPETE,       "Indian Contract Act 1872, §27 (Agreement in restraint of trade — VOID)");
        LAW_REFERENCES.put(ClauseType.IP_OWNERSHIP,      "Copyright Act 1957, §17 (First owner); Patents Act 1970, §6");
        LAW_REFERENCES.put(ClauseType.CONFIDENTIALITY,   "Information Technology Act 2000, §43A; Trade Secrets (common law)");
        LAW_REFERENCES.put(ClauseType.RENT_ESCALATION,   "Model Tenancy Act 2021, §9 (Revision of Rent)");
        LAW_REFERENCES.put(ClauseType.SECURITY_DEPOSIT,  "Model Tenancy Act 2021, §11 (Security deposit — max 2 months)");
        LAW_REFERENCES.put(ClauseType.LOCK_IN_PERIOD,    "Model Tenancy Act 2021 (no statutory max — check state rules)");
        LAW_REFERENCES.put(ClauseType.NOTICE_PERIOD,     "Industrial Disputes Act 1947, §25F; State Shop Acts");
        LAW_REFERENCES.put(ClauseType.INTEREST_RATE,     "Usurious Loans Act 1918; RBI Guidelines on MCLR");
        LAW_REFERENCES.put(ClauseType.DEFAULT,           "SARFAESI Act 2002; Recovery of Debts Act 1993");
        LAW_REFERENCES.put(ClauseType.COLLATERAL,        "Transfer of Property Act 1882, §58 (Mortgage)");
        LAW_REFERENCES.put(ClauseType.GUARANTOR,         "Indian Contract Act 1872, §126–147 (Guarantee)");
        LAW_REFERENCES.put(ClauseType.GOVERNING_LAW,     "Code of Civil Procedure 1908, §20 (Jurisdiction)");
        LAW_REFERENCES.put(ClauseType.FORCE_MAJEURE,     "Indian Contract Act 1872, §56 (Frustration of contract)");
        LAW_REFERENCES.put(ClauseType.JURISDICTION,      "Code of Civil Procedure 1908, §20");
        LAW_REFERENCES.put(ClauseType.SUBLETTING,        "Model Tenancy Act 2021, §23 (Subletting with consent)");
        LAW_REFERENCES.put(ClauseType.GENERAL,           "Indian Contract Act 1872");
    }

    // =========================================================================
    // PUBLIC API
    // =========================================================================

    /**
     * Analyze a single clause and return an enriched Clause object.
     *
     * PROPRIETARY ALGORITHM — © 2025 VakilSahay
     */
    public Clause analyzeClause(Clause clause, DocumentType documentType) {
        String text = clause.getOriginalText().toLowerCase();

        // Step 1: Classify clause type
        ClauseType type = classifyClause(text, documentType);
        clause.setClauseType(type);

        // Step 2: Base score from taxonomy
        int score = BASE_SCORES.getOrDefault(type, 20);

        // Step 3: Red-flag keyword penalties
        for (Map.Entry<Pattern, Integer> entry : RED_FLAG_PENALTIES.entrySet()) {
            if (entry.getKey().matcher(text).find()) {
                score += entry.getValue();
            }
        }

        // Step 4: Protective clause bonuses
        for (Map.Entry<Pattern, Integer> entry : PROTECTIVE_BONUSES.entrySet()) {
            if (entry.getKey().matcher(text).find()) {
                score += entry.getValue();
            }
        }

        // Step 5: Document-type context multiplier
        score = applyContextMultiplier(score, type, documentType);

        // Step 6: Normalize to 0–100
        score = Math.max(0, Math.min(100, score));

        // Step 7: Map to Severity enum
        Severity severity = scoreToSeverity(score);

        clause.setSeverityScore(score);
        clause.setSeverity(severity);
        clause.setLawReference(LAW_REFERENCES.getOrDefault(type, "Indian Contract Act 1872"));
        clause.setPlainEnglish(generatePlainEnglish(clause.getOriginalText(), type, severity));
        clause.setRecommendation(generateRecommendation(type, severity, score));

        return clause;
    }

    /**
     * Detect document type from full text.
     */
    public DocumentType detectDocumentType(String text) {
        String lower = text.toLowerCase();
        if (containsAny(lower, List.of("rental agreement", "tenancy agreement", "landlord", "tenant", "rent", "premises")))
            return DocumentType.RENTAL;
        if (containsAny(lower, List.of("employment agreement", "offer letter", "employee", "employer", "salary", "designation")))
            return DocumentType.EMPLOYMENT;
        if (containsAny(lower, List.of("loan agreement", "borrower", "lender", "principal amount", "repayment", "emi")))
            return DocumentType.LOAN;
        if (containsAny(lower, List.of("non-disclosure", "nda", "confidential information")))
            return DocumentType.NDA;
        if (containsAny(lower, List.of("partnership deed", "partners", "profit sharing", "capital contribution")))
            return DocumentType.PARTNERSHIP;
        return DocumentType.GENERAL;
    }

    // =========================================================================
    // PRIVATE PROPRIETARY METHODS
    // =========================================================================

    private ClauseType classifyClause(String text, DocumentType docType) {
        for (ClauseMatcher matcher : MATCHERS) {
            for (String keyword : matcher.keywords()) {
                if (text.contains(keyword)) return matcher.type();
            }
        }
        return ClauseType.GENERAL;
    }

    private int applyContextMultiplier(int score, ClauseType type, DocumentType docType) {
        // Rental context — security deposit and lock-in are bigger concerns
        if (docType == DocumentType.RENTAL &&
                (type == ClauseType.SECURITY_DEPOSIT || type == ClauseType.LOCK_IN_PERIOD)) {
            return (int)(score * 1.15);
        }
        // Employment context — non-compete clauses are often void under §27
        if (docType == DocumentType.EMPLOYMENT && type == ClauseType.NON_COMPETE) {
            return (int)(score * 1.2);
        }
        // Loan context — default and interest clauses are critical
        if (docType == DocumentType.LOAN &&
                (type == ClauseType.DEFAULT || type == ClauseType.INTEREST_RATE)) {
            return (int)(score * 1.25);
        }
        return score;
    }

    private Severity scoreToSeverity(int score) {
        if (score >= 76) return Severity.CRITICAL;
        if (score >= 51) return Severity.HIGH;
        if (score >= 26) return Severity.MEDIUM;
        return Severity.LOW;
    }

    private String generatePlainEnglish(String originalText, ClauseType type, Severity severity) {
        String intro = switch (severity) {
            case CRITICAL -> "⚠️ CRITICAL — This clause is highly risky for you. ";
            case HIGH     -> "🔴 HIGH RISK — This clause may be unfair or restrictive. ";
            case MEDIUM   -> "🟡 MODERATE — Review this clause carefully. ";
            case LOW      -> "🟢 LOW RISK — This is a standard clause. ";
        };

        String explanation = switch (type) {
            case TERMINATION      -> "This clause explains how either party can end this agreement and what happens when they do.";
            case PENALTY          -> "If you break any condition, you'll have to pay extra money as a penalty.";
            case INDEMNIFICATION  -> "You agree to pay for any losses or legal costs the other party faces because of your actions.";
            case LIABILITY        -> "This limits what the other party is responsible for if something goes wrong.";
            case ARBITRATION      -> "If there's a dispute, it must go to a private arbitrator instead of a regular court.";
            case NON_COMPETE      -> "After the agreement ends, this restricts you from working in similar businesses. Under Section 27 of the Indian Contract Act 1872, overly broad non-compete clauses are void. Courts often refuse to enforce them.";
            case IP_OWNERSHIP     -> "Everything you create during this agreement legally belongs to the other party.";
            case CONFIDENTIALITY  -> "You cannot share information about this agreement or business secrets with outsiders.";
            case RENT_ESCALATION  -> "The rent will automatically increase by a fixed amount each year.";
            case SECURITY_DEPOSIT -> "This is a refundable amount held as security. The Model Tenancy Act caps this at 2 months' rent.";
            case LOCK_IN_PERIOD   -> "You cannot leave before this period ends, and if you do, you may lose your deposit.";
            case NOTICE_PERIOD    -> "You must inform the other party this many days before leaving or making changes.";
            case INTEREST_RATE    -> "You will be charged this rate of interest on any outstanding amount.";
            case DEFAULT          -> "If you miss a payment or break a condition, the lender can immediately demand full repayment.";
            case COLLATERAL       -> "You are pledging an asset as security. The lender can seize it if you default.";
            case GUARANTOR        -> "A third person agrees to pay your debt if you cannot. This is a serious commitment.";
            case FORCE_MAJEURE    -> "Neither party is responsible for failure caused by events beyond control (floods, pandemics, etc.).";
            case DISPUTE_RESOLUTION -> "Disputes should first be resolved through negotiation or mediation before going to court.";
            case GOVERNING_LAW    -> "Indian law applies to this agreement.";
            case JURISDICTION     -> "Any legal dispute will be handled by courts in a specific city.";
            default               -> "This is a standard legal clause. Read it carefully to understand your obligations.";
        };

        return intro + explanation;
    }

    private String generateRecommendation(ClauseType type, Severity severity, int score) {
        if (severity == Severity.LOW) return "No immediate action needed. Standard industry clause.";

        return switch (type) {
            case NON_COMPETE      -> "Ask for clause removal or limit it to 6 months and 50km radius. Section 27 of Indian Contract Act makes overly broad non-competes void.";
            case SECURITY_DEPOSIT -> "Ensure deposit is limited to 2 months' rent (Model Tenancy Act 2021). Demand written receipt.";
            case PENALTY          -> "Negotiate a reasonable cap. Penalties must be a genuine pre-estimate of loss under Section 74.";
            case INDEMNIFICATION  -> "Negotiate mutual indemnification — both parties should be equally liable.";
            case INTEREST_RATE    -> "Compare with RBI's MCLR rate. Interest above 18% annually may be challenged under Usurious Loans Act.";
            case DEFAULT          -> "Ask for a cure period (15–30 days) before the default clause triggers.";
            case ARBITRATION      -> "Check location of arbitration — it should be in your city. Ask for institutional arbitration (DIAC/ICC).";
            case IP_OWNERSHIP     -> "Negotiate to retain rights to work created before employment. Get a carve-out for personal projects.";
            case LOCK_IN_PERIOD   -> "Negotiate lock-in to maximum 11 months for rentals. Ensure force majeure exit clause.";
            case COLLATERAL       -> "Ensure the collateral value is proportionate to the loan amount. Get independent valuation.";
            default               -> "Consult a lawyer before signing. Consider negotiating more balanced terms.";
        };
    }

    private static void addPenalty(String keyword, int penalty) {
        RED_FLAG_PENALTIES.put(Pattern.compile(keyword, Pattern.CASE_INSENSITIVE), penalty);
    }

    private static void addBonus(String keyword, int bonus) {
        PROTECTIVE_BONUSES.put(Pattern.compile(keyword, Pattern.CASE_INSENSITIVE), bonus);
    }

    private boolean containsAny(String text, List<String> keywords) {
        return keywords.stream().anyMatch(text::contains);
    }

    private record ClauseMatcher(ClauseType type, List<String> keywords) {}
}