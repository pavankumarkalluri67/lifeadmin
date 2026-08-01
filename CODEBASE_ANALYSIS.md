# LifeAdmin AI Backend - Codebase Analysis Report

**Date:** August 1, 2026  
**Project:** LifeAdmin AI Backend  
**Stack:** Java 21, Spring Boot 4.1.0, PostgreSQL, Spring AI, Spring Security, Flyway

---

## Executive Summary

The LifeAdmin AI backend is a **modular monolith** currently in early implementation stage with **core infrastructure and authentication systems** partially complete. The project follows the principle **"AI proposes. Software validates. Users control."** with strict separation between LLM responsibility and application responsibility.

**Current State:**

- ✅ Database schema fully designed (14 tables)
- ✅ Core security & auth framework implemented
- ✅ Base entity and persistence layer established
- ⚠️ **Majority of service layer NOT YET implemented** (document, processing, extraction, obligation, action, reminder, notification, dashboard, assistant services)
- ⚠️ **Controller/API endpoints NOT YET implemented**
- ⚠️ **Limited test coverage** (6 unit tests for security only)

---

## 1. MODULES IMPLEMENTED vs. REQUIRED

### Implemented Modules

#### 1.1 **Authentication Module** (PARTIAL)

**Location:** `src/main/java/.../auth/`  
**Status:** ✅ 60% Complete (Domain + Repositories only)

**Implemented:**

- `User` (JPA entity with email, password hash, name, timezone, status)
- `RefreshToken` (JPA entity with token hash, expiration, revocation)
- `UserStatus` (enum: ACTIVE, LOCKED, DISABLED)
- `PasswordPolicy` (validator: 8-128 chars, mixed case, digit, special char)
- `EmailFormat` (validator: RFC 5322 compliance)
- `TimezoneValidator` (IANA timezone validation)
- `UserRepository` (Spring Data interface)
- `RefreshTokenRepository` (Spring Data interface)

**Missing:**

- ❌ AuthService (registration, login, refresh, logout logic)
- ❌ AuthController (REST endpoints)
- ❌ DTOs (RegisterRequest, LoginResponse, RefreshRequest)
- ❌ Email verification flow
- ❌ Password reset flow

---

#### 1.2 **Common/Shared Infrastructure** (PARTIAL)

**Location:** `src/main/java/.../common/`  
**Status:** ✅ 70% Complete (Security + Error Handling + Base)

**Implemented:**

**Persistence (`common.persistence`):**

- `BaseEntity` - Abstract JPA entity with UUID id, version (optimistic locking), created_at, updated_at

**Security (`common.security`):**

- `TokenService` - JWT creation/verification using JJWT library
- `TokenHasher` - SHA-256 hashing for refresh token storage
- `PasswordHasher` - BCrypt password hashing/verification
- `VerifiedToken` - Record containing verified JWT claims
- `JwtAuthenticationFilter` - Spring Security filter for token extraction & validation
- `CurrentUserProvider` - Extract current user UUID from SecurityContext
- `RestAuthenticationEntryPoint` - Return 401 JSON response for unauthenticated access
- `InvalidTokenException` - Custom exception for token errors
- `SecurityProperties` - Configuration properties (JWT secret, TTL values)

**Config (`common.config`):**

- `SecurityConfig` - Spring Security filter chain configuration
- `JpaAuditingConfig` - Enable @CreatedDate/@LastModifiedDate auditing

**Error Handling (`common.error`):**

- `ErrorCode` (enum) - Standardized error codes (EMAIL_ALREADY_EXISTS, VALIDATION_ERROR, etc.)
- `ApiError` - Error response DTO
- `ApiException` - Base checked exception
- `ResourceNotFoundException` - 404 error
- `FileNotFoundException` - File storage error
- `StaleUpdateException` - Optimistic lock failure
- `GlobalExceptionHandler` - @ControllerAdvice mapping exceptions to HTTP responses

**Missing:**

- ❌ Logging/observability integration
- ❌ Request validation aspect
- ❌ Audit trail aspect

---

#### 1.3 **AI Integration Module** (MINIMAL)

**Location:** `src/main/java/.../ai/`  
**Status:** ⚠️ 20% Complete (Domain + minimal Application)

**Implemented:**

**Domain (`ai.domain`):**

- `AiInvocation` - Audit record (operation type, provider, model, latency, token counts, status)
- `AiInvocationStatus` (enum) - SUCCESS, FAILURE, TIMEOUT, RATE_LIMITED
- `AiOperationType` (enum) - DOCUMENT_ANALYSIS, ENTITY_EXTRACTION, OBLIGATION_GENERATION, CHAT_COMPLETION
- `AiClient` (enum) - OPENAI, OLLAMA (provider selection)
- `AgentTurn` - Single turn in agent reasoning loop
- `ChatContext` - Context for multi-turn conversation
- `PromptContext` - Wrapper for prompt templates
- `DocumentAnalysisResult` - AI output (documentType, confidence, entities, obligations)
- `DocumentAnalysisValidator` - Validates structured AI output
- `InvalidAnalysisResultException` - Validation failure
- `ToolSpec` - Tool definition for Spring AI tool calling

**Application (`ai.application`):**

- `AiInvocationAuditor` - Records AI calls to database (Requirement 27.1, 27.2, 27.3)

**Repository (`ai.repository`):**

- `AiInvocationRepository` - Spring Data JPA interface for audit records

**Missing (CRITICAL):**

- ❌ `AiService` / `AiProvider` abstraction (OpenAI, Ollama client)
- ❌ `DocumentAnalysisService` (call AI for document analysis)
- ❌ `ExtractionService` (text extraction via PDFBox/Tika, normalization)
- ❌ `EntityExtractionService` (structured entity extraction)
- ❌ `AgentOrchestrator` (agent reasoning, tool calling, SSE streaming)
- ❌ Controllers for AI operations
- ❌ Spring AI configuration

---

### Missing Modules (NOT IMPLEMENTED)

| Module                   | Purpose                                   | Status | Priority |
| ------------------------ | ----------------------------------------- | ------ | -------- |
| **User Service**         | Profile retrieval, updates                | ❌ 0%  | HIGH     |
| **Document Service**     | Upload, list, retrieve, delete, reprocess | ❌ 0%  | HIGH     |
| **Storage Service**      | File persistence abstraction (S3/local)   | ❌ 0%  | HIGH     |
| **Processing Service**   | Orchestrate async document jobs           | ❌ 0%  | HIGH     |
| **Extraction Service**   | PDF/text extraction, normalization        | ❌ 0%  | HIGH     |
| **Obligation Service**   | CRUD, confirmation, completion            | ❌ 0%  | HIGH     |
| **Action Service**       | Action item management                    | ❌ 0%  | HIGH     |
| **Reminder Service**     | Reminder scheduling                       | ❌ 0%  | HIGH     |
| **Notification Service** | Reminder delivery (email, SMS, push)      | ❌ 0%  | MEDIUM   |
| **Dashboard Service**    | Summary metrics                           | ❌ 0%  | MEDIUM   |
| **Assistant Service**    | Conversation management                   | ❌ 0%  | HIGH     |
| **Agent Orchestrator**   | Multi-step AI reasoning, tool calling     | ⚠️ 10% | HIGH     |

---

## 2. DATABASE SCHEMA & ENTITIES

### Database State

**Database Type:** PostgreSQL  
**Migration Tool:** Flyway (V1\_\_initial_schema.sql)  
**Approach:** Flyway owns schema; Hibernate validates only (ddl-auto=validate)  
**Audit Columns:** All entities have `id (UUID)`, `version (BIGINT)`, `created_at (TIMESTAMPTZ)`, `updated_at (TIMESTAMPTZ)`

### Implemented Tables (14 total)

```
users
├── id, version, created_at, updated_at
├── email (unique), password_hash
├── first_name, last_name, timezone
├── status (ACTIVE|LOCKED|DISABLED)
├── email_verified, last_login_at
└── CONSTRAINT: uq_users_email

refresh_tokens
├── id, version, created_at, updated_at
├── user_id (FK → users)
├── token_hash (indexed), expires_at
├── revoked, revoked_at
└── INDEX: idx_refresh_tokens_user_id, idx_refresh_tokens_token_hash

documents
├── id, version, created_at, updated_at
├── user_id (FK → users, indexed)
├── original_file_name, stored_file_name
├── storage_key, content_type, file_extension
├── file_size_bytes, checksum_sha256
├── document_type (UNKNOWN|...), processing_status (UPLOADED|QUEUED|PROCESSING|PROCESSED|FAILED)
├── processing_error_code, uploaded_at
└── CONSTRAINT: uq_documents_user_checksum (per-user dedup)

document_contents
├── id, version, created_at, updated_at
├── document_id (FK → documents, unique)
├── raw_text, normalized_text
├── character_count, extraction_method (PDFBOX|TIKA|PLAIN_TEXT)
├── extracted_at
└── CONSTRAINT: uq_document_contents_document

processing_jobs
├── id, version, created_at, updated_at
├── document_id (FK → documents, indexed)
├── status (PENDING|RUNNING|COMPLETED|FAILED|RETRY_PENDING), indexed
├── current_stage (QUEUED|TEXT_EXTRACTION|TEXT_NORMALIZATION|AI_ANALYSIS|ENTITY_PERSISTENCE|OBLIGATION_PERSISTENCE|COMPLETED)
├── attempt_count, max_attempts
├── next_retry_at, error_code, error_message
├── completed_at
└── INDEX: idx_processing_jobs_document_id, idx_processing_jobs_status

extracted_entities
├── id, version, created_at, updated_at
├── document_id (FK → documents, indexed)
├── entity_type, entity_value (TEXT)
├── normalized_value, confidence (0.0-1.0)
├── extraction_method, ai_model, prompt_version
└── INDEX: idx_extracted_entities_document_id

obligations
├── id, version, created_at, updated_at
├── user_id (FK → users, indexed), document_id (FK → documents, indexed)
├── type, title, due_date, priority
├── confidence (0.0-1.0), source_type (AI|USER|MANUAL)
├── status (PENDING|CONFIRMED|DISMISSED|COMPLETED)
├── requires_confirmation, confirmed_at, dismissed_at, completed_at
├── normalized_due_date (DATE), normalized_reference (VARCHAR)
└── CONSTRAINT: uq_obligations_dedup (document_id, type, normalized_due_date, normalized_reference)

action_items
├── id, version, created_at, updated_at
├── user_id (FK → users, indexed), obligation_id (FK → obligations, indexed)
├── title, priority, status, source_type
├── completed_at
└── INDEX: idx_action_items_user_id, idx_action_items_obligation_id

reminders
├── id, version, created_at, updated_at
├── user_id (FK → users, indexed)
├── obligation_id (FK → obligations), action_item_id (FK → action_items)
├── remind_at, channel (EMAIL|SMS|PUSH), status
├── sent_at, failure_reason
└── INDEX: idx_reminders_user_id, idx_reminders_status_remind_at

conversations
├── id, version, created_at, updated_at
├── user_id (FK → users, indexed)
├── status (ACTIVE|ARCHIVED)
└── INDEX: idx_conversations_user_id

agent_executions
├── id, version, created_at, updated_at
├── user_id (FK → users, indexed)
├── status (RUNNING|COMPLETED|FAILED)
├── model_provider, model_name
├── input_tokens, output_tokens
├── failure_code, failure_message
├── completed_at
└── INDEX: idx_agent_executions_user_id

messages
├── id, version, created_at, updated_at
├── conversation_id (FK → conversations, indexed)
├── agent_execution_id (FK → agent_executions)
├── role (USER|ASSISTANT|SYSTEM), content (TEXT)
└── INDEX: idx_messages_conversation_id

agent_steps
├── id, version, created_at, updated_at
├── agent_execution_id (FK → agent_executions, indexed)
├── step_number, step_type (TOOL_CALL|RESPONSE|REASONING)
├── name, ... (truncated)
└── INDEX: idx_agent_steps_execution_id

ai_invocations
├── id, version, created_at, updated_at
├── operation_type, provider, model
├── prompt_version, status
├── latency_ms, input_tokens, output_tokens
├── error_code
└── NOTE: No prompt/response text stored (privacy by design)
```

### Entity-Repository Mapping

| JPA Entity        | Repository               | Status         |
| ----------------- | ------------------------ | -------------- |
| `User`            | `UserRepository`         | ✅ Implemented |
| `RefreshToken`    | `RefreshTokenRepository` | ✅ Implemented |
| `AiInvocation`    | `AiInvocationRepository` | ✅ Implemented |
| `Document`        | ❌ MISSING               | ❌ Not created |
| `DocumentContent` | ❌ MISSING               | ❌ Not created |
| `ProcessingJob`   | ❌ MISSING               | ❌ Not created |
| `ExtractedEntity` | ❌ MISSING               | ❌ Not created |
| `Obligation`      | ❌ MISSING               | ❌ Not created |
| `ActionItem`      | ❌ MISSING               | ❌ Not created |
| `Reminder`        | ❌ MISSING               | ❌ Not created |
| `Conversation`    | ❌ MISSING               | ❌ Not created |
| `AgentExecution`  | ❌ MISSING               | ❌ Not created |
| `Message`         | ❌ MISSING               | ❌ Not created |
| `AgentStep`       | ❌ MISSING               | ❌ Not created |

---

## 3. KEY FILES & DIRECTORY STRUCTURE

### Directory Layout

```
src/main/java/com/LifeAdmin/ai/lifeadmin/
├── LifeadminApplication.java (Spring Boot entry point)
├── ai/
│   ├── application/
│   │   └── AiInvocationAuditor.java
│   ├── domain/
│   │   ├── AiClient.java (enum)
│   │   ├── AiInvocation.java (entity)
│   │   ├── AiInvocationStatus.java (enum)
│   │   ├── AiOperationType.java (enum)
│   │   ├── AgentTurn.java
│   │   ├── ChatContext.java
│   │   ├── DocumentAnalysisResult.java
│   │   ├── DocumentAnalysisValidator.java
│   │   ├── InvalidAnalysisResultException.java
│   │   ├── PromptContext.java
│   │   └── ToolSpec.java
│   └── repository/
│       └── AiInvocationRepository.java
│
├── auth/
│   ├── domain/
│   │   ├── EmailFormat.java (validator)
│   │   ├── PasswordPolicy.java (validator)
│   │   ├── RefreshToken.java (entity)
│   │   ├── TimezoneValidator.java (validator)
│   │   ├── User.java (entity)
│   │   └── UserStatus.java (enum)
│   └── repository/
│       ├── RefreshTokenRepository.java
│       └── UserRepository.java
│
└── common/
    ├── config/
    │   ├── JpaAuditingConfig.java
    │   └── SecurityConfig.java
    ├── error/
    │   ├── ApiError.java
    │   ├── ApiException.java
    │   ├── ErrorCode.java (enum)
    │   ├── FileNotFoundException.java
    │   ├── GlobalExceptionHandler.java (@ControllerAdvice)
    │   ├── ResourceNotFoundException.java
    │   └── StaleUpdateException.java
    ├── persistence/
    │   └── BaseEntity.java (abstract with @CreatedDate, @LastModifiedDate)
    └── security/
        ├── CurrentUserProvider.java
        ├── InvalidTokenException.java
        ├── JwtAuthenticationFilter.java (OncePerRequestFilter)
        ├── PasswordHasher.java
        ├── RestAuthenticationEntryPoint.java
        ├── SecurityProperties.java (@ConfigurationProperties)
        ├── TokenHasher.java
        ├── TokenService.java
        └── VerifiedToken.java (record)

src/main/resources/
├── application.properties (common config)
├── application-local.properties (dev overrides)
├── application-prod.properties (production)
├── application-test.properties (test)
└── db/migration/
    └── V1__initial_schema.sql (Flyway migration, 14 tables)

src/test/java/
└── com/LifeAdmin/ai/lifeadmin/
    ├── LifeadminApplicationTests.java (context load test)
    └── common/
        ├── error/GlobalExceptionHandlerTest.java
        └── security/
            ├── PasswordHasherTest.java
            ├── TokenHasherTest.java
            ├── TokenServiceTest.java
            └── UnauthenticatedAccessRejectionTest.java
```

### Total Source Files: 41 Java files

- **Implemented:** 41
- **Tests:** 6 (all security-focused)

---

## 4. KEY CLASSES & THEIR RESPONSIBILITIES

### Core Security Classes

| Class                     | Package           | Responsibility                      | Lines | Status      |
| ------------------------- | ----------------- | ----------------------------------- | ----- | ----------- |
| `User`                    | `auth.domain`     | JPA entity, user record             | ~100  | ✅ Complete |
| `RefreshToken`            | `auth.domain`     | JPA entity, refresh token storage   | ~80   | ✅ Complete |
| `TokenService`            | `common.security` | JWT issue/verify using JJWT         | ~120  | ✅ Complete |
| `PasswordHasher`          | `common.security` | BCrypt hashing                      | ~50   | ✅ Complete |
| `JwtAuthenticationFilter` | `common.security` | Extract & validate token            | ~80   | ✅ Complete |
| `SecurityConfig`          | `common.config`   | Spring Security filter chain        | ~60   | ✅ Complete |
| `GlobalExceptionHandler`  | `common.error`    | @ControllerAdvice exception mapping | ~100  | ✅ Complete |

### Domain Model Classes (AI)

| Class                       | Package     | Responsibility                | Status                       |
| --------------------------- | ----------- | ----------------------------- | ---------------------------- |
| `AiInvocation`              | `ai.domain` | Audit record for AI calls     | ✅ Complete                  |
| `DocumentAnalysisResult`    | `ai.domain` | Structured AI output          | ⚠️ Partial (needs hydration) |
| `DocumentAnalysisValidator` | `ai.domain` | Validates AI output           | ✅ Complete                  |
| `AgentTurn`                 | `ai.domain` | Single agent step             | ⚠️ Partial                   |
| `ChatContext`               | `ai.domain` | Multi-turn conversation state | ⚠️ Partial                   |

---

## 5. CURRENT IMPLEMENTATION GAPS

### CRITICAL GAPS (MUST-BUILD)

#### 5.1 User Service Module

**Required by:** Requirement 5  
**Missing:**

- `UserService` (CRUD operations, ownership validation)
- `UserController` (REST endpoints)
- `UserUpdateRequest` / `UserResponse` DTOs

#### 5.2 Document Service Module

**Required by:** Requirements 7-10, 35  
**Missing:**

- `Document` JPA entity
- `DocumentContent` JPA entity
- `DocumentRepository` & `DocumentContentRepository`
- `DocumentService` (upload, list, retrieve, delete, reprocess)
- `DocumentController` (REST endpoints)
- Document upload validation & file signature checking

#### 5.3 Storage Service Module

**Required by:** Requirement 7, 35  
**Missing:**

- `StorageService` abstraction (interface)
- `LocalFileStorageService` implementation (file system)
- S3 storage implementation (optional, but required for production)
- File encryption at rest (optional but recommended)

#### 5.4 Processing Service Module

**Required by:** Requirement 11  
**Missing:**

- `ProcessingJob` JPA entity
- `ProcessingJobRepository`
- `ProcessingService` (async job orchestration)
- Spring `TaskExecutor` bean for async processing
- Job recovery on startup

#### 5.5 Extraction Service Module

**Required by:** Requirement 12  
**Missing:**

- `Extraction_Service` (text extraction from PDFs/plain text)
- `TextNormalizer` (text cleanup & normalization)
- PDFBox integration (added to pom.xml but not used)
- Tika integration (fallback)

#### 5.6 AI Service & Provider Integration

**Required by:** Requirements 13-14, 22-27  
**Missing:**

- `AiService` / `AiProvider` abstraction
- `OpenAiProvider` implementation
- `OllamaProvider` implementation (local LLM)
- Structured output parsing (DocumentAnalysisResult deserialization)
- Prompt templates & versioning
- Retry logic (exponential backoff)
- Rate limiting handling
- Token counting

#### 5.7 Obligation Service Module

**Required by:** Requirements 15-18  
**Missing:**

- `Obligation` JPA entity
- `ObligationRepository`
- `ObligationService` (CRUD, confirmation, dismissal, completion)
- `ObligationController` (REST endpoints)
- Deduplication logic (composite key: document_id + type + normalized_due_date + normalized_reference)

#### 5.8 Action Service Module

**Required by:** Requirement 19  
**Missing:**

- `ActionItem` JPA entity
- `ActionItemRepository`
- `ActionService` (CRUD)
- `ActionItemController`

#### 5.9 Reminder Service Module

**Required by:** Requirement 20  
**Missing:**

- `Reminder` JPA entity
- `ReminderRepository`
- `ReminderService` (scheduling, status management)
- `ReminderController`

#### 5.10 Notification Service Module

**Required by:** Requirement 21  
**Missing:**

- `NotificationService` (email, SMS, push delivery)
- Email integration (JavaMail or SendGrid)
- SMS integration (Twilio)
- Push notification integration (Firebase)

#### 5.11 Assistant & Conversation Module

**Required by:** Requirements 23-24  
**Missing:**

- `Conversation` JPA entity
- `Message` JPA entity
- `ConversationRepository` & `MessageRepository`
- `ConversationService` (conversation management)
- `AssistantService` (natural-language interaction)
- `ConversationController` (REST + SSE endpoints)

#### 5.12 Agent Orchestrator

**Required by:** Requirement 26  
**Missing:**

- `AgentExecution` JPA entity (partial schema only)
- `AgentStep` JPA entity (partial schema only)
- `AgentOrchestrator` (multi-step reasoning, tool calling)
- Spring AI tool definition & registration
- SSE streaming for agent steps
- Tool execution sandboxing

#### 5.13 Dashboard Service Module

**Required by:** Requirement 31  
**Missing:**

- `DashboardService` (metrics aggregation)
- `DashboardController`

---

## 6. CONFIGURATION & PROPERTIES

### Application Properties (application.properties)

| Property                               | Value                                                        | Purpose                        |
| -------------------------------------- | ------------------------------------------------------------ | ------------------------------ |
| `spring.application.name`              | `lifeadmin`                                                  | App name                       |
| `spring.profiles.active`               | `${SPRING_PROFILES_ACTIVE:local}`                            | Environment selector           |
| `spring.datasource.url`                | `${DATABASE_URL:jdbc:postgresql://localhost:5432/lifeadmin}` | PostgreSQL connection          |
| `spring.datasource.username`           | `${DATABASE_USERNAME:lifeadmin}`                             | DB user                        |
| `spring.datasource.password`           | `${DATABASE_PASSWORD:lifeadmin}`                             | DB password                    |
| `spring.jpa.hibernate.ddl-auto`        | `validate`                                                   | Flyway owns schema             |
| `spring.flyway.enabled`                | `true`                                                       | Enable Flyway migrations       |
| `lifeadmin.security.jwt-secret`        | `${JWT_SECRET:}`                                             | JWT signing key (env required) |
| `lifeadmin.security.access-token-ttl`  | `15m`                                                        | Access token lifetime          |
| `lifeadmin.security.refresh-token-ttl` | `30d`                                                        | Refresh token lifetime         |
| `lifeadmin.storage.path`               | `${FILE_STORAGE_PATH:./data/documents}`                      | Document storage directory     |
| `lifeadmin.ai.provider`                | `${AI_PROVIDER:ollama}`                                      | AI backend (ollama\|openai)    |
| `spring.ai.openai.api-key`             | `${OPENAI_API_KEY:}`                                         | OpenAI API key (if used)       |

### Maven Dependencies (Installed)

**Core Framework:**

- Spring Boot 4.1.0 (parent POM)
- Java 21

**Data & Persistence:**

- spring-boot-starter-data-jpa
- spring-boot-starter-flyway
- flyway-database-postgresql
- postgresql (driver)

**Security:**

- spring-boot-starter-security
- jjwt-api, jjwt-impl, jjwt-jackson (JWT)

**AI/ML:**

- spring-ai-starter-model-openai (OpenAI integration)
- spring-ai-starter-model-ollama (Local LLM)

**Document Processing:**

- pdfbox (version 3.0.3)
- tika (version 3.1.0)

**Utilities:**

- spring-boot-starter-validation
- spring-boot-starter-webmvc
- spring-boot-starter-actuator

**Tests:**

- (No test framework explicitly listed; likely JUnit 5 from parent)

---

## 7. TEST COVERAGE

### Current Test Suite (6 tests)

| Test Class                           | Location          | Scope                                     | Status     |
| ------------------------------------ | ----------------- | ----------------------------------------- | ---------- |
| `GlobalExceptionHandlerTest`         | `common.error`    | Exception mapping to HTTP responses       | ✅ Passing |
| `PasswordHasherTest`                 | `common.security` | BCrypt hashing & verification             | ✅ Passing |
| `TokenHasherTest`                    | `common.security` | SHA-256 token hashing                     | ✅ Passing |
| `TokenServiceTest`                   | `common.security` | JWT creation & verification               | ✅ Passing |
| `UnauthenticatedAccessRejectionTest` | `common.security` | 401 response for unauthenticated requests | ✅ Passing |
| `LifeadminApplicationTests`          | (root)            | Context loading                           | ✅ Passing |

### Missing Test Coverage

**NOT TESTED:**

- User registration, login, logout, refresh
- Document upload, listing, deletion
- File storage operations
- Document processing jobs
- Text extraction
- AI provider integration
- Obligation CRUD
- Reminder scheduling
- Conversation management
- Agent orchestration
- Error scenarios (all modules)
- Ownership validation enforcement
- Concurrency & race conditions
- Database integrity constraints

**Test Coverage Estimate:** < 10%

---

## 8. IMPLEMENTATION ROADMAP

### Phase 1: Authentication Service (Week 1)

1. ✅ Domain entities & validators (DONE)
2. ✅ JWT/password security (DONE)
3. ⏳ **AuthService** (registration, login, refresh, logout)
4. ⏳ **AuthController** (REST endpoints)
5. ⏳ **Integration tests** (auth flows)

### Phase 2: Document Management (Week 2)

1. ⏳ **Document & DocumentContent** JPA entities
2. ⏳ **DocumentRepository** & **DocumentContentRepository**
3. ⏳ **StorageService** abstraction + local implementation
4. ⏳ **DocumentService** (upload, list, retrieve, delete, reprocess)
5. ⏳ **DocumentController** (REST endpoints)
6. ⏳ **ProcessingJob** entity & **ProcessingService**
7. ⏳ **ExtractionService** (text extraction + normalization)
8. ⏳ Integration tests

### Phase 3: AI Analysis & Obligation Extraction (Week 2-3)

1. ⏳ **AiProvider** abstraction (OpenAI, Ollama)
2. ⏳ **AiService** (structured output, prompt templates)
3. ⏳ **Obligation** & **ExtractedEntity** JPA entities
4. ⏳ **ObligationService** (deduplication, CRUD)
5. ⏳ Obligation extraction from AI results
6. ⏳ Integration tests

### Phase 4: Reminders, Actions & Notifications (Week 3)

1. ⏳ **ActionItem** & **Reminder** JPA entities
2. ⏳ **ActionService** & **ReminderService**
3. ⏳ **NotificationService** (email/SMS/push)
4. ⏳ Controllers for actions & reminders
5. ⏳ Integration tests

### Phase 5: Assistant & Agent (Optional/Future)

1. ⏳ **Conversation** & **Message** JPA entities
2. ⏳ **AgentExecution** & **AgentStep** completion
3. ⏳ **AgentOrchestrator** (tool calling, multi-turn reasoning)
4. ⏳ **AssistantService** & **ConversationService**
5. ⏳ SSE streaming for agent execution
6. ⏳ Integration tests

### Phase 6: Dashboard (Optional/Future)

1. ⏳ **DashboardService** (metrics aggregation)
2. ⏳ **DashboardController**
3. ⏳ Integration tests

---

## 9. IDENTIFIED RISKS & NOTES

### High-Risk Areas

1. **AI Provider Integration:**
   - Spring AI 2.0 API is still evolving; breaking changes possible
   - Ollama performance & availability for local deployments
   - Token counting inconsistencies across providers
   - Rate limiting & retry logic critical for production

2. **Document Processing:**
   - PDFBox/Tika text extraction quality varies by document format
   - Malformed PDFs can cause memory exhaustion
   - No timeout mechanism for extraction jobs yet
   - Large file handling not implemented

3. **Concurrency:**
   - Multiple users uploading same document → race condition on deduplication
   - Agent execution state tracking during concurrent turns
   - Database version column relies on optimistic locking (potential deadlocks)

4. **Security:**
   - No CSRF protection (assumes stateless JWT auth, should verify)
   - File upload validation incomplete (content-type mismatch check missing)
   - No rate limiting on auth endpoints
   - JWT secret injection from environment required (will fail if missing)

5. **Performance:**
   - No pagination on document listing yet
   - No search/filtering optimization
   - AI token counting may block response (latency critical)
   - Obligation deduplication query (4-column unique) needs indexing strategy

### Design Principles to Uphold

1. ✅ **"AI proposes. Software validates. Users control."** → Enforce in every AI integration
2. ✅ **Privacy by Design** → AI invocation audit table has no prompt/response storage
3. ✅ **Optimistic Locking** → Version column prevents lost updates
4. ✅ **Ownership Validation** → Always use findByIdAndUserId patterns
5. ✅ **Async Processing** → Document jobs are non-blocking
6. ✅ **Idempotency** → Deduplication guards prevent duplicate obligations

---

## 10. QUICK REFERENCE: WHAT'S READY vs. NOT READY

### ✅ READY TO USE

- User & RefreshToken entities with validators
- JWT token generation/verification
- Password & token hashing
- Spring Security configuration
- Exception handling framework
- Base entity for JPA auditing
- Database schema & migrations (Flyway)
- AiInvocation audit entity
- Environment-based configuration

### ❌ NOT READY (Implementation Needed)

- **All REST controllers** (no endpoints deployed)
- **All service layers** (auth, document, processing, extraction, obligation, reminder, notification, dashboard, assistant, agent)
- **All remaining JPA entities** (Document, ProcessingJob, Obligation, ActionItem, Reminder, Conversation, Message, AgentExecution, AgentStep, ExtractedEntity)
- **All repositories** except User, RefreshToken, AiInvocation
- **All DTOs** (request/response models)
- **File storage** (local or cloud)
- **AI provider integration** (OpenAI, Ollama clients)
- **Text extraction** (PDFBox, Tika integration)
- **Async job processing** (TaskExecutor, job recovery)
- **Comprehensive test suite** (only security tests exist)

---

## 11. NEXT STEPS

### Immediate Priority

1. **Complete AuthService** with registration, login, refresh, logout
2. **Create AuthController** with REST endpoints (POST /auth/register, /auth/login, etc.)
3. **Write auth integration tests** (happy path & error cases)
4. **Verify JWT token flow end-to-end**

### Short-term (Next Week)

1. **Implement Document module** (entity, repository, controller, storage service)
2. **Implement ProcessingService** with async job execution
3. **Implement ExtractionService** (PDFBox/Tika integration)
4. **Add comprehensive tests** for document operations

### Parallel Work

- Set up environment variables & deployment configs
- Configure PostgreSQL for local & test environments
- Document API contract (OpenAPI/Swagger)
- Plan AI provider integration (which model to use by default)

---

**Generated:** August 1, 2026  
**Analysis Tool:** GitHub Copilot  
**Java Version:** 21  
**Spring Boot Version:** 4.1.0
