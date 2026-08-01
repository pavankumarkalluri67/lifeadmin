-- =====================================================================
-- V1__initial_schema.sql
-- LifeAdmin AI Backend - initial database schema (MUST-BUILD entities).
--
-- Conventions (see common.persistence.BaseEntity and design Data Models):
--   * UUID primary keys (application-generated via Hibernate GenerationType.UUID)
--   * version BIGINT NOT NULL for optimistic locking (Req 28.1)
--   * created_at / updated_at TIMESTAMPTZ NOT NULL audit columns (Req 30.3, 30.4)
--   * All timestamps stored as TIMESTAMPTZ (mapped to java.time.Instant)
--   * snake_case column names (Hibernate default naming strategy)
--   * Explicit foreign keys; tables ordered so FKs reference existing tables
--
-- Schema is owned by Flyway; Hibernate runs with ddl-auto=validate (Req 30.1, 30.2).
-- =====================================================================

-- ---------------------------------------------------------------------
-- users
-- ---------------------------------------------------------------------
CREATE TABLE users (
    id                UUID         NOT NULL,
    version           BIGINT       NOT NULL,
    created_at        TIMESTAMPTZ  NOT NULL,
    updated_at        TIMESTAMPTZ  NOT NULL,
    email             VARCHAR(320) NOT NULL,
    password_hash     VARCHAR(255) NOT NULL,
    first_name        VARCHAR(255) NOT NULL,
    last_name         VARCHAR(255),
    timezone          VARCHAR(64)  NOT NULL,
    status            VARCHAR(32)  NOT NULL,
    email_verified    BOOLEAN      NOT NULL,
    last_login_at     TIMESTAMPTZ,
    CONSTRAINT pk_users PRIMARY KEY (id),
    CONSTRAINT uq_users_email UNIQUE (email)
);

-- ---------------------------------------------------------------------
-- refresh_tokens
-- ---------------------------------------------------------------------
CREATE TABLE refresh_tokens (
    id            UUID         NOT NULL,
    version       BIGINT       NOT NULL,
    created_at    TIMESTAMPTZ  NOT NULL,
    updated_at    TIMESTAMPTZ  NOT NULL,
    user_id       UUID         NOT NULL,
    token_hash    VARCHAR(255) NOT NULL,
    expires_at    TIMESTAMPTZ  NOT NULL,
    revoked       BOOLEAN      NOT NULL,
    revoked_at    TIMESTAMPTZ,
    CONSTRAINT pk_refresh_tokens PRIMARY KEY (id),
    CONSTRAINT fk_refresh_tokens_user FOREIGN KEY (user_id) REFERENCES users (id)
);

CREATE INDEX idx_refresh_tokens_user_id ON refresh_tokens (user_id);
CREATE INDEX idx_refresh_tokens_token_hash ON refresh_tokens (token_hash);

-- ---------------------------------------------------------------------
-- documents
-- ---------------------------------------------------------------------
CREATE TABLE documents (
    id                     UUID         NOT NULL,
    version                BIGINT       NOT NULL,
    created_at             TIMESTAMPTZ  NOT NULL,
    updated_at             TIMESTAMPTZ  NOT NULL,
    user_id                UUID         NOT NULL,
    original_file_name     VARCHAR(255) NOT NULL,
    stored_file_name       VARCHAR(255) NOT NULL,
    storage_key            VARCHAR(512) NOT NULL,
    content_type           VARCHAR(128) NOT NULL,
    file_extension         VARCHAR(32),
    file_size_bytes        BIGINT       NOT NULL,
    checksum_sha256        VARCHAR(64)  NOT NULL,
    document_type          VARCHAR(64)  NOT NULL,
    processing_status      VARCHAR(32)  NOT NULL,
    processing_error_code  VARCHAR(64),
    uploaded_at            TIMESTAMPTZ  NOT NULL,
    CONSTRAINT pk_documents PRIMARY KEY (id),
    CONSTRAINT fk_documents_user FOREIGN KEY (user_id) REFERENCES users (id),
    -- Per-user deduplication guard (Req 7.9)
    CONSTRAINT uq_documents_user_checksum UNIQUE (user_id, checksum_sha256)
);

CREATE INDEX idx_documents_user_id ON documents (user_id);

-- ---------------------------------------------------------------------
-- document_contents
-- ---------------------------------------------------------------------
CREATE TABLE document_contents (
    id                 UUID         NOT NULL,
    version            BIGINT       NOT NULL,
    created_at         TIMESTAMPTZ  NOT NULL,
    updated_at         TIMESTAMPTZ  NOT NULL,
    document_id        UUID         NOT NULL,
    raw_text           TEXT,
    normalized_text    TEXT,
    character_count    INTEGER      NOT NULL,
    extraction_method  VARCHAR(32)  NOT NULL,
    extracted_at       TIMESTAMPTZ  NOT NULL,
    CONSTRAINT pk_document_contents PRIMARY KEY (id),
    CONSTRAINT fk_document_contents_document FOREIGN KEY (document_id) REFERENCES documents (id),
    CONSTRAINT uq_document_contents_document UNIQUE (document_id)
);

-- ---------------------------------------------------------------------
-- processing_jobs
-- ---------------------------------------------------------------------
CREATE TABLE processing_jobs (
    id             UUID         NOT NULL,
    version        BIGINT       NOT NULL,
    created_at     TIMESTAMPTZ  NOT NULL,
    updated_at     TIMESTAMPTZ  NOT NULL,
    document_id    UUID         NOT NULL,
    status         VARCHAR(32)  NOT NULL,
    current_stage  VARCHAR(32)  NOT NULL,
    attempt_count  INTEGER      NOT NULL,
    max_attempts   INTEGER      NOT NULL,
    next_retry_at  TIMESTAMPTZ,
    error_code     VARCHAR(64),
    error_message  TEXT,
    completed_at   TIMESTAMPTZ,
    CONSTRAINT pk_processing_jobs PRIMARY KEY (id),
    CONSTRAINT fk_processing_jobs_document FOREIGN KEY (document_id) REFERENCES documents (id)
);

CREATE INDEX idx_processing_jobs_document_id ON processing_jobs (document_id);
CREATE INDEX idx_processing_jobs_status ON processing_jobs (status);

-- ---------------------------------------------------------------------
-- extracted_entities
-- ---------------------------------------------------------------------
CREATE TABLE extracted_entities (
    id                 UUID              NOT NULL,
    version            BIGINT            NOT NULL,
    created_at         TIMESTAMPTZ       NOT NULL,
    updated_at         TIMESTAMPTZ       NOT NULL,
    document_id        UUID              NOT NULL,
    entity_type        VARCHAR(64)       NOT NULL,
    entity_value       TEXT              NOT NULL,
    normalized_value   TEXT,
    confidence         DOUBLE PRECISION  NOT NULL,
    extraction_method  VARCHAR(32)       NOT NULL,
    ai_model           VARCHAR(128),
    prompt_version     VARCHAR(64),
    CONSTRAINT pk_extracted_entities PRIMARY KEY (id),
    CONSTRAINT fk_extracted_entities_document FOREIGN KEY (document_id) REFERENCES documents (id)
);

CREATE INDEX idx_extracted_entities_document_id ON extracted_entities (document_id);

-- ---------------------------------------------------------------------
-- obligations
-- ---------------------------------------------------------------------
CREATE TABLE obligations (
    id                     UUID              NOT NULL,
    version                BIGINT            NOT NULL,
    created_at             TIMESTAMPTZ       NOT NULL,
    updated_at             TIMESTAMPTZ       NOT NULL,
    user_id                UUID              NOT NULL,
    document_id            UUID,
    type                   VARCHAR(64)       NOT NULL,
    title                  VARCHAR(512)      NOT NULL,
    due_date               TIMESTAMPTZ,
    priority               VARCHAR(32)       NOT NULL,
    confidence             DOUBLE PRECISION  NOT NULL,
    source_type            VARCHAR(16)       NOT NULL,
    status                 VARCHAR(32)       NOT NULL,
    requires_confirmation  BOOLEAN           NOT NULL,
    confirmed_at           TIMESTAMPTZ,
    dismissed_at           TIMESTAMPTZ,
    completed_at           TIMESTAMPTZ,
    normalized_due_date    DATE,
    normalized_reference   VARCHAR(512)      NOT NULL,
    CONSTRAINT pk_obligations PRIMARY KEY (id),
    CONSTRAINT fk_obligations_user FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT fk_obligations_document FOREIGN KEY (document_id) REFERENCES documents (id),
    -- Final deduplication guard on reprocess (Req 17.5)
    CONSTRAINT uq_obligations_dedup UNIQUE (document_id, type, normalized_due_date, normalized_reference)
);

CREATE INDEX idx_obligations_user_id ON obligations (user_id);
CREATE INDEX idx_obligations_document_id ON obligations (document_id);

-- ---------------------------------------------------------------------
-- action_items
-- ---------------------------------------------------------------------
CREATE TABLE action_items (
    id             UUID          NOT NULL,
    version        BIGINT        NOT NULL,
    created_at     TIMESTAMPTZ   NOT NULL,
    updated_at     TIMESTAMPTZ   NOT NULL,
    user_id        UUID          NOT NULL,
    obligation_id  UUID,
    title          VARCHAR(512)  NOT NULL,
    priority       VARCHAR(32)   NOT NULL,
    status         VARCHAR(32)   NOT NULL,
    source_type    VARCHAR(16)   NOT NULL,
    completed_at   TIMESTAMPTZ,
    CONSTRAINT pk_action_items PRIMARY KEY (id),
    CONSTRAINT fk_action_items_user FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT fk_action_items_obligation FOREIGN KEY (obligation_id) REFERENCES obligations (id)
);

CREATE INDEX idx_action_items_user_id ON action_items (user_id);
CREATE INDEX idx_action_items_obligation_id ON action_items (obligation_id);

-- ---------------------------------------------------------------------
-- reminders
-- ---------------------------------------------------------------------
CREATE TABLE reminders (
    id              UUID         NOT NULL,
    version         BIGINT       NOT NULL,
    created_at      TIMESTAMPTZ  NOT NULL,
    updated_at      TIMESTAMPTZ  NOT NULL,
    user_id         UUID         NOT NULL,
    obligation_id   UUID,
    action_item_id  UUID,
    remind_at       TIMESTAMPTZ  NOT NULL,
    channel         VARCHAR(16)  NOT NULL,
    status          VARCHAR(16)  NOT NULL,
    sent_at         TIMESTAMPTZ,
    failure_reason  TEXT,
    CONSTRAINT pk_reminders PRIMARY KEY (id),
    CONSTRAINT fk_reminders_user FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT fk_reminders_obligation FOREIGN KEY (obligation_id) REFERENCES obligations (id),
    CONSTRAINT fk_reminders_action_item FOREIGN KEY (action_item_id) REFERENCES action_items (id)
);

CREATE INDEX idx_reminders_user_id ON reminders (user_id);
CREATE INDEX idx_reminders_status_remind_at ON reminders (status, remind_at);

-- ---------------------------------------------------------------------
-- conversations
-- ---------------------------------------------------------------------
CREATE TABLE conversations (
    id          UUID         NOT NULL,
    version     BIGINT       NOT NULL,
    created_at  TIMESTAMPTZ  NOT NULL,
    updated_at  TIMESTAMPTZ  NOT NULL,
    user_id     UUID         NOT NULL,
    status      VARCHAR(16)  NOT NULL,
    CONSTRAINT pk_conversations PRIMARY KEY (id),
    CONSTRAINT fk_conversations_user FOREIGN KEY (user_id) REFERENCES users (id)
);

CREATE INDEX idx_conversations_user_id ON conversations (user_id);

-- ---------------------------------------------------------------------
-- agent_executions
-- (created before messages because messages.agent_execution_id references it)
-- ---------------------------------------------------------------------
CREATE TABLE agent_executions (
    id               UUID          NOT NULL,
    version          BIGINT        NOT NULL,
    created_at       TIMESTAMPTZ   NOT NULL,
    updated_at       TIMESTAMPTZ   NOT NULL,
    user_id          UUID          NOT NULL,
    status           VARCHAR(16)   NOT NULL,
    model_provider   VARCHAR(64)   NOT NULL,
    model_name       VARCHAR(128)  NOT NULL,
    input_tokens     INTEGER,
    output_tokens    INTEGER,
    failure_code     VARCHAR(64),
    failure_message  TEXT,
    completed_at     TIMESTAMPTZ,
    CONSTRAINT pk_agent_executions PRIMARY KEY (id),
    CONSTRAINT fk_agent_executions_user FOREIGN KEY (user_id) REFERENCES users (id)
);

CREATE INDEX idx_agent_executions_user_id ON agent_executions (user_id);

-- ---------------------------------------------------------------------
-- messages
-- ---------------------------------------------------------------------
CREATE TABLE messages (
    id                  UUID         NOT NULL,
    version             BIGINT       NOT NULL,
    created_at          TIMESTAMPTZ  NOT NULL,
    updated_at          TIMESTAMPTZ  NOT NULL,
    conversation_id     UUID         NOT NULL,
    role                VARCHAR(16)  NOT NULL,
    content             TEXT         NOT NULL,
    agent_execution_id  UUID,
    CONSTRAINT pk_messages PRIMARY KEY (id),
    CONSTRAINT fk_messages_conversation FOREIGN KEY (conversation_id) REFERENCES conversations (id),
    CONSTRAINT fk_messages_agent_execution FOREIGN KEY (agent_execution_id) REFERENCES agent_executions (id)
);

CREATE INDEX idx_messages_conversation_id ON messages (conversation_id);

-- ---------------------------------------------------------------------
-- agent_steps
-- ---------------------------------------------------------------------
CREATE TABLE agent_steps (
    id                  UUID         NOT NULL,
    version             BIGINT       NOT NULL,
    created_at          TIMESTAMPTZ  NOT NULL,
    updated_at          TIMESTAMPTZ  NOT NULL,
    agent_execution_id  UUID         NOT NULL,
    step_number         INTEGER      NOT NULL,
    step_type           VARCHAR(32)  NOT NULL,
    name                VARCHAR(255) NOT NULL,
    status              VARCHAR(16)  NOT NULL,
    tool_name           VARCHAR(128),
    input_summary       TEXT,
    output_summary      TEXT,
    CONSTRAINT pk_agent_steps PRIMARY KEY (id),
    CONSTRAINT fk_agent_steps_execution FOREIGN KEY (agent_execution_id) REFERENCES agent_executions (id)
);

CREATE INDEX idx_agent_steps_execution_id ON agent_steps (agent_execution_id);

-- ---------------------------------------------------------------------
-- ai_invocations
-- ---------------------------------------------------------------------
CREATE TABLE ai_invocations (
    id              UUID          NOT NULL,
    version         BIGINT        NOT NULL,
    created_at      TIMESTAMPTZ   NOT NULL,
    updated_at      TIMESTAMPTZ   NOT NULL,
    operation_type  VARCHAR(64)   NOT NULL,
    provider        VARCHAR(64)   NOT NULL,
    model           VARCHAR(128)  NOT NULL,
    prompt_version  VARCHAR(64),
    status          VARCHAR(16)   NOT NULL,
    latency_ms      BIGINT        NOT NULL,
    input_tokens    INTEGER,
    output_tokens   INTEGER,
    error_code      VARCHAR(64),
    CONSTRAINT pk_ai_invocations PRIMARY KEY (id)
);
