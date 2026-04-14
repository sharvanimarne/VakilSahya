-- =============================================
-- VakilSahay Database Schema V1
-- © 2025 VakilSahay. All rights reserved.
-- =============================================

-- Users table
CREATE TABLE users (
    id          BIGSERIAL PRIMARY KEY,
    email       VARCHAR(255) NOT NULL UNIQUE,
    password    VARCHAR(255) NOT NULL,
    full_name   VARCHAR(255) NOT NULL,
    role        VARCHAR(20)  NOT NULL DEFAULT 'USER',
    is_active   BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_users_email ON users(email);

-- Documents table
CREATE TABLE documents (
    id              BIGSERIAL PRIMARY KEY,
    user_id         BIGINT       NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    file_name       VARCHAR(500) NOT NULL,
    original_name   VARCHAR(500) NOT NULL,
    file_type       VARCHAR(100) NOT NULL,
    file_size       BIGINT       NOT NULL,
    raw_text        TEXT,
    document_type   VARCHAR(50),   -- RENTAL, EMPLOYMENT, LOAN, GENERAL
    status          VARCHAR(30)  NOT NULL DEFAULT 'UPLOADED',
    -- UPLOADED | ANALYZING | COMPLETED | FAILED
    created_at      TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_documents_user_id   ON documents(user_id);
CREATE INDEX idx_documents_status    ON documents(status);
CREATE INDEX idx_documents_created   ON documents(created_at DESC);

-- Clauses table (proprietary extraction — © VakilSahay)
CREATE TABLE clauses (
    id              BIGSERIAL PRIMARY KEY,
    document_id     BIGINT      NOT NULL REFERENCES documents(id) ON DELETE CASCADE,
    clause_number   INTEGER     NOT NULL,
    original_text   TEXT        NOT NULL,
    plain_english   TEXT,         -- AI/algorithm generated plain language
    clause_type     VARCHAR(100), -- TERMINATION, PENALTY, LIABILITY, ARBITRATION, etc.
    severity        VARCHAR(10),  -- LOW | MEDIUM | HIGH | CRITICAL
    severity_score  INTEGER,      -- 0–100 (proprietary scoring — © VakilSahay)
    law_reference   VARCHAR(500), -- e.g. "Indian Contract Act 1872, Section 74"
    recommendation  TEXT,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_clauses_document_id ON clauses(document_id);
CREATE INDEX idx_clauses_severity    ON clauses(severity);

-- Analysis reports table
CREATE TABLE analysis_reports (
    id                  BIGSERIAL PRIMARY KEY,
    document_id         BIGINT  NOT NULL REFERENCES documents(id) ON DELETE CASCADE UNIQUE,
    overall_risk_score  INTEGER NOT NULL DEFAULT 0,  -- 0–100
    total_clauses       INTEGER NOT NULL DEFAULT 0,
    critical_count      INTEGER NOT NULL DEFAULT 0,
    high_count          INTEGER NOT NULL DEFAULT 0,
    medium_count        INTEGER NOT NULL DEFAULT 0,
    low_count           INTEGER NOT NULL DEFAULT 0,
    summary             TEXT,
    top_concerns        TEXT,    -- JSON array of top 3 concern strings
    recommendations     TEXT,    -- JSON array of recommendation strings
    created_at          TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_reports_document_id ON analysis_reports(document_id);

-- Usage tracking (for monetization)
CREATE TABLE usage_logs (
    id          BIGSERIAL PRIMARY KEY,
    user_id     BIGINT      NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    document_id BIGINT      REFERENCES documents(id) ON DELETE SET NULL,
    action      VARCHAR(50) NOT NULL, -- UPLOAD, ANALYZE, DOWNLOAD_REPORT, DELETE
    created_at  TIMESTAMP   NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_usage_user_id   ON usage_logs(user_id);
CREATE INDEX idx_usage_created   ON usage_logs(created_at DESC);