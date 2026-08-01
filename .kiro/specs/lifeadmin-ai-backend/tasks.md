# Implementation Plan: LifeAdmin AI Backend

## Overview

This plan converts the LifeAdmin AI design into a sequence of incremental, testable Java 21 / Spring Boot coding tasks. Work proceeds bottom-up through the strict layered architecture (Controller → Application Service → Domain → Repository Interface → Infrastructure), starting with the common foundation and database schema, then building each functional module so that every layer is wired into a working end-to-end flow before the next module is added. Each task builds on prior tasks; there is no orphaned code. Testing is embedded as optional sub-tasks (marked with `*`) close to the code they validate. The design defines pure domain helpers (validators, classifiers, dedup identity, retry/backoff) that are covered by focused unit tests, and behavioral requirements that are covered by integration tests.

## Tasks

- [x] 1. Establish project foundation and common cross-cutting module
  - [x] 1.1 Configure build, dependencies, profiles, and application configuration
    - Update `pom.xml` for Java 21 with Spring Boot, Spring Web, Spring Security, Spring Data JPA, Spring AI, PostgreSQL driver, Flyway, PDFBox, Tika, Bean Validation, and Actuator
    - Create `application.properties` plus `application-local`, `application-test`, `application-prod` profiles reading env vars (DATABASE_URL, DATABASE_USERNAME, DATABASE_PASSWORD, JWT_SECRET, AI_PROVIDER, OPENAI_API_KEY, GEMINI_API_KEY, OLLAMA_BASE_URL, FILE_STORAGE_PATH)
    - Set Hibernate `ddl-auto=validate` and configure Flyway
    - _Requirements: 30.1, 30.2, 32.3, 32.4, 32.5_
  - [x] 1.2 Implement persistence base and conventions
    - Create `com.lifeadmin.common.persistence.BaseEntity` with UUID id, `@Version` long version, `Instant createdAt`, `Instant updatedAt`, TIMESTAMPTZ mapping
    - Configure JPA auditing for created_at/updated_at
    - _Requirements: 28.1, 30.3, 30.4_
  - [x] 1.3 Implement standardized error model and central exception handling
    - Create `ErrorCode` enum with all defined codes, `ApiError` body (timestamp, status, code, message, path, traceId, errors[])
    - Implement `GlobalExceptionHandler` (`@RestControllerAdvice`) mapping validation and domain exceptions, including VALIDATION_ERROR field/message array
    - Define shared exceptions (ResourceNotFoundException, StaleUpdateException, etc.)
    - _Requirements: 29.1, 29.2, 29.3, 28.2_
  - [x] 1.4 Write unit tests for error mapping
    - Test exception-to-ApiError mapping and VALIDATION_ERROR errors array shape
    - _Requirements: 29.1, 29.2_

- [x] 2. Create database schema via Flyway migrations
  - [x] 2.1 Author Flyway migrations for all MUST-BUILD entities
    - Create versioned SQL migrations under `src/main/resources/db/migration` for users, refresh_tokens, documents, document_contents, processing_jobs, extracted_entities, obligations, action_items, reminders, conversations, messages, agent_executions, agent_steps, ai_invocations
    - Use UUID PKs, TIMESTAMPTZ columns, explicit foreign keys, version columns
    - Add unique constraint `(user_id, checksum_sha256)` on documents and unique constraint `(document_id, type, normalized_due_date, normalized_reference)` on obligations
    - _Requirements: 30.1, 30.3, 30.4, 7.9, 17.5_

- [ ] 3. Implement security foundation and authentication enforcement
  - [x] 3.1 Implement token and hashing infrastructure
    - Create `TokenService` (JWT issue/verify Access_Token), `TokenHasher` (refresh-token hashing), `PasswordHasher` (BCrypt) adapters
    - _Requirements: 2.1, 3.4, 1.2_
  - [x] 3.2 Implement JWT filter, security context accessor, and security config
    - Create JWT auth filter that validates Access_Token and populates SecurityContext with userId
    - Create `CurrentUserProvider` reading authenticated userId as the single identity source
    - Configure Spring Security to protect endpoints and reject missing/invalid tokens with 401 UNAUTHENTICATED
    - _Requirements: 6.1, 24.4, 26.2_
  - [-] 3.3 Write unit tests for token service and hashing
    - Test token issue/verify, expiry handling, and refresh-token hash comparison without raw storage
    - _Requirements: 2.1, 3.4_
  - [-] 3.4 Write integration test for unauthenticated access rejection
    - Assert protected endpoint returns 401 UNAUTHENTICATED without a valid token
    - _Requirements: 6.1_

- [ ] 4. Implement authentication module (Auth_Service)
  - [x] 4.1 Implement User and RefreshToken entities and repositories
    - Create User entity (email unique ≤320, password_hash, first_name, last_name?, timezone, status, email_verified, last_login_at?) and RefreshToken entity (token_hash, expires_at, revoked, revoked_at?)
    - Create `UserRepository` and `RefreshTokenRepository` ports
    - _Requirements: 1.2, 1.6, 2.5, 3.4_
  - [-] 4.2 Implement pure domain validators
    - Implement `PasswordPolicy.isValid`, `EmailFormat.isValid`, `TimezoneValidator.isValid` (IANA)
    - _Requirements: 1.3, 1.5, 5.3_
  - [~] 4.3 Implement registration and login
    - Implement `AuthService.register` (validate, ensure email uniqueness, BCrypt hash, create ACTIVE user with email_verified=false and UUID PK)
    - Implement `AuthService.login` (verify credentials, enforce status, issue token pair, set last_login_at, store hashed refresh token)
    - _Requirements: 1.1, 1.2, 1.4, 1.5, 1.6, 2.1, 2.2, 2.3, 2.4, 2.5_
  - [~] 4.4 Implement refresh with rotation and logout
    - Implement `AuthService.refresh` (validate hash/expiry/revocation, issue new pair, revoke previous token)
    - Implement `AuthService.logout` (revoke token, idempotent for already-revoked/unknown)
    - _Requirements: 3.1, 3.2, 3.3, 4.1, 4.2_
  - [~] 4.5 Implement AuthController endpoints
    - Expose register, login, refresh, logout under `/api/v1` with DTO mapping and status codes
    - _Requirements: 1.1, 2.1, 3.1, 4.1, 31.1, 31.4_
  - [~] 4.6 Write unit tests for domain validators
    - Test password policy, email format, and timezone validation edge cases
    - _Requirements: 1.3, 1.5_
  - [~] 4.7 Write integration tests for auth flows
    - Test registration success/duplicate/validation, login success/invalid/locked, refresh rotation, and logout idempotency
    - _Requirements: 1.1, 1.4, 1.5, 2.1, 2.3, 2.4, 3.1, 3.2, 3.3, 4.1, 4.2_

- [ ] 5. Implement user profile module (User_Service)
  - [~] 5.1 Implement profile retrieval and update
    - Implement `UserService.getProfile` and `updateProfile` restricted to the authenticated user, validating timezone on update
    - Expose UserController endpoints under `/api/v1`
    - _Requirements: 5.1, 5.2, 5.3, 5.4_
  - [~] 5.2 Write integration tests for profile endpoints
    - Test retrieval, valid update, and invalid timezone rejection
    - _Requirements: 5.1, 5.2, 5.3_

- [~] 6. Checkpoint - Ensure all tests pass
  - Ensure all tests pass, ask the user if questions arise.

- [ ] 7. Implement storage abstraction and document module (Document_Service)
  - [~] 7.1 Implement Storage_Service port and filesystem adapter
    - Define `StorageService` port (store, retrieve, delete) and filesystem adapter under FILE_STORAGE_PATH; surface FILE_NOT_FOUND for missing files
    - _Requirements: 35.3_
  - [~] 7.2 Implement document entities and repositories
    - Create Document, DocumentContents, and ProcessingJob entities with owner-scoped `findByIdAndUserId` repository ports
    - _Requirements: 6.2, 7.2, 8.1, 11.1_
  - [~] 7.3 Implement pure upload validation and filename sanitization
    - Implement `UploadValidator` pipeline in order: EMPTY_FILE → UNSUPPORTED_FILE_TYPE → FILE_TOO_LARGE → CONTENT_TYPE_MISMATCH (magic bytes) → checksum + per-user DUPLICATE_DOCUMENT
    - Implement `FilenameSanitizer` (strip path separators/control chars, truncate to 255)
    - _Requirements: 7.2, 7.3, 7.4, 7.7, 7.8, 7.9_
  - [~] 7.4 Implement document upload use case
    - Implement `DocumentService.upload` computing SHA-256, storing via Storage_Service, creating Document (UPLOADED, document_type UNKNOWN) and Processing_Job (PENDING/QUEUED), returning 202
    - _Requirements: 7.1, 7.5, 7.6_
  - [~] 7.5 Implement list, get, download, delete, and reprocess use cases
    - Implement owner-scoped paginated list with document_type/processing_status filters, get, download (stored content_type), delete (record + file), and reprocess (create/reset job PENDING/QUEUED)
    - Return 404 RESOURCE_NOT_FOUND for non-owned identifiers
    - _Requirements: 8.1, 8.2, 8.3, 8.4, 35.1, 35.2, 9.1, 9.2, 10.1, 10.3_
  - [~] 7.6 Implement DocumentController endpoints
    - Expose upload, list, get, download, delete, reprocess under `/api/v1`
    - _Requirements: 7.1, 8.1, 35.1, 9.1, 10.1, 31.1, 31.4_
  - [~] 7.7 Write unit tests for upload validation and sanitization
    - Test each rejection branch order and filename sanitization edge cases
    - _Requirements: 7.2, 7.3, 7.4, 7.7, 7.8, 7.9_
  - [~] 7.8 Write integration tests for document endpoints and ownership
    - Test upload success/duplicate, list/get/download/delete/reprocess, and cross-user 404
    - _Requirements: 7.1, 8.1, 8.4, 35.1, 35.2, 9.1, 9.2, 10.1, 10.3, 6.2, 6.3_

- [ ] 8. Implement AI service abstraction (AI_Service)
  - [x] 8.1 Implement AiClient port, structured output DTO, and validation
    - Define `AiClient` port (analyze, chat), `DocumentAnalysisResult` DTO (documentType, confidence, entities[], obligations[]), and Bean Validation + domain rules used before persistence
    - _Requirements: 13.1, 13.2, 22.3_
  - [-] 8.2 Implement provider adapters and configuration-based selection
    - Implement Ollama, OpenAI, and Gemini adapters selected by AI_PROVIDER; keep all provider SDK types in `ai.infrastructure`; surface AiProviderUnavailableException when unreachable
    - _Requirements: 22.1, 22.2, 22.4, 22.5_
  - [-] 8.3 Implement AI invocation auditing
    - Implement `AiInvocationAuditor` recording operation_type, provider, model, prompt_version, status, latency_ms, tokens; never store full prompt text; record FAILED/TIMEOUT/RATE_LIMITED with error_code
    - _Requirements: 27.1, 27.2, 27.3_
  - [~] 8.4 Write unit tests for structured output validation
    - Test acceptance of valid results and rejection of invalid DocumentAnalysisResult instances
    - _Requirements: 13.2_
  - [~] 8.5 Write integration tests for provider selection and unavailability
    - Test provider selection by AI_PROVIDER and provider-unavailable error surfacing
    - _Requirements: 22.1, 22.2, 22.4_

- [ ] 9. Implement extraction module (Extraction_Service)
  - [~] 9.1 Implement pure text normalization
    - Implement `TextNormalizer` deterministic normalization used for content and reference normalization
    - _Requirements: 12.3, 17.2_
  - [~] 9.2 Implement text extraction and content persistence
    - Implement `ExtractionService.extract` (PDF via PDFBox with Tika fallback → PDFBOX/TIKA; plain text → PLAIN_TEXT); persist document_contents (raw_text, normalized_text, character_count, extracted_at); empty text → FAILED/EXTRACTION_EMPTY without retry
    - _Requirements: 12.1, 12.2, 12.3, 12.4_
  - [~] 9.3 Implement entity persistence with allowed-type validation
    - Implement `ExtractionService.persistEntities` validating entity_type against the allowed set, storing confidence in [0,1], and replacing prior entities on reprocess
    - _Requirements: 14.1, 14.2, 14.3, 14.4_
  - [~] 9.4 Write unit tests for normalization and entity validation
    - Test normalization determinism and rejection of unknown entity types / out-of-range confidence
    - _Requirements: 12.3, 14.2, 14.3_

- [ ] 10. Implement obligation module (Obligation_Service)
  - [~] 10.1 Implement Obligation entity, repository, and dedup mapping
    - Create Obligation entity with dedup columns (normalized_due_date?, normalized_reference) and owner-scoped repository; map the DB unique constraint
    - _Requirements: 15.5, 17.5, 18.1_
  - [~] 10.2 Implement pure confidence classifier and dedup identity
    - Implement `ConfidenceClassifier` (>=high → DETECTED, requires_confirmation=false; >=review and <high → DETECTED, requires_confirmation=true; <review → do not persist, audit) reading configurable thresholds
    - Implement `DedupIdentity` value object (documentId, type, normalizedDueDate truncated to day, normalizedReference trimmed+lowercased)
    - _Requirements: 15.1, 15.2, 15.3, 15.4, 17.1, 17.2_
  - [~] 10.3 Implement obligation creation from analysis with idempotency
    - Implement `ObligationService.createFromAnalysis` applying classification, source_type AI, priority default fallback, unset due_date handling, and dedup (at most one per Dedup_Identity, constraint violation treated as successful dedup leaving confirmed/dismissed unchanged)
    - _Requirements: 15.5, 15.6, 15.7, 17.1, 17.3, 17.4, 17.6, 10.2_
  - [~] 10.4 Implement obligation management and controller
    - Implement owner-scoped list (filter type/status/due_date range), get, confirm, dismiss, complete, update; return 404 for non-owned; expose ObligationController under `/api/v1`
    - _Requirements: 18.1, 18.2, 18.3, 18.4, 18.5, 18.6, 31.1_
  - [~] 10.5 Write unit tests for classifier and dedup identity
    - Test threshold boundaries and normalized due-date/reference derivation
    - _Requirements: 15.1, 15.2, 15.3, 17.2_
  - [~] 10.6 Write integration tests for obligation management and idempotency
    - Test lifecycle transitions, ownership 404, and no duplicate obligations on reprocess
    - _Requirements: 18.2, 18.3, 18.4, 18.6, 17.3, 17.4_

- [~] 11. Checkpoint - Ensure all tests pass
  - Ensure all tests pass, ask the user if questions arise.

- [ ] 12. Implement processing orchestration and wire the full pipeline (Processing_Service)
  - [~] 12.1 Implement pure retry policy and backoff calculator
    - Implement `RetryPolicy` classifying transient vs permanent failures and dead-lettering at max_attempts; implement `BackoffCalculator.nextDelay` (monotonic base delay, capped, with jitter)
    - _Requirements: 16.1, 16.2, 16.3_
  - [~] 12.2 Implement processing orchestrator stage machine
    - Implement `ProcessingOrchestrator.execute` on a Spring TaskExecutor advancing TEXT_EXTRACTION → TEXT_NORMALIZATION → AI_ANALYSIS → ENTITY_PERSISTENCE → OBLIGATION_PERSISTENCE; set RUNNING/COMPLETED, Document PROCESSED, and reflect QUEUED/PROCESSING status
    - Perform AI network call outside any open transaction (read→commit→call AI→validate→transaction→persist→commit); apply retry/FAILED/DEAD_LETTER and record error_code/error_message
    - Wire Extraction_Service, AI_Service, and Obligation_Service into the pipeline
    - _Requirements: 11.1, 11.2, 11.3, 11.5, 13.1, 13.2, 13.3, 13.4, 13.5, 16.1, 16.2, 16.3, 16.5_
  - [~] 12.3 Implement job recovery on startup
    - Implement `JobRecoveryService` on `ApplicationReadyEvent` re-queuing jobs left RUNNING or RETRY_PENDING; allow manual reprocess of DEAD_LETTER documents
    - _Requirements: 11.4, 16.4_
  - [~] 12.4 Write unit tests for retry policy and backoff
    - Test transient/permanent classification, dead-letter threshold, and monotonic capped backoff
    - _Requirements: 16.1, 16.2, 16.3_
  - [~] 12.5 Write integration tests for end-to-end processing
    - Test full pipeline success, extraction-empty failure, retry/dead-letter, reprocess idempotency, and startup recovery
    - _Requirements: 11.1, 11.3, 11.4, 12.4, 13.3, 16.1, 16.3, 10.2_

- [ ] 13. Implement action item module (Action_Service)
  - [~] 13.1 Implement ActionItem entity, repository, service, and controller
    - Create ActionItem entity and owner-scoped repository; implement create (TODO/USER), list (filter status/priority), complete, update, delete with obligation-ownership verification when obligation_id is linked; return 404 for non-owned; expose controller under `/api/v1`
    - _Requirements: 19.1, 19.2, 19.3, 19.4, 19.5, 19.6, 31.1_
  - [~] 13.2 Write integration tests for action items
    - Test create/list/complete/update/delete, obligation-link ownership, and cross-user 404
    - _Requirements: 19.1, 19.2, 19.3, 19.5, 19.6_

- [ ] 14. Implement reminder and notification modules
  - [~] 14.1 Implement Reminder entity, repository, service, and controller
    - Create Reminder entity and owner-scoped repository; implement create (IN_APP → SCHEDULED, require owned obligation_id or action_item_id), list (filter status), update, delete; return 404 for non-owned; expose controller under `/api/v1`
    - _Requirements: 20.1, 20.2, 20.3, 20.4, 20.6, 31.1_
  - [~] 14.2 Implement notification scheduler
    - Implement `NotificationScheduler` (`@Scheduled`) scanning due SCHEDULED IN_APP reminders and marking SENT with sent_at
    - _Requirements: 20.5_
  - [~] 14.3 Write integration tests for reminders
    - Test create with ownership requirement, list/update/delete, cross-user 404, and scheduled delivery marking SENT
    - _Requirements: 20.1, 20.2, 20.5, 20.6_

- [ ] 15. Implement dashboard module (Dashboard_Service)
  - [~] 15.1 Implement dashboard summary service and controller
    - Implement `DashboardService.summary` computing counts and upcoming items exclusively from the user's own data (upcoming obligations, obligations requiring confirmation, open action items, scheduled reminders); expose controller under `/api/v1`
    - _Requirements: 21.1, 21.2, 31.1_
  - [~] 15.2 Write integration tests for dashboard
    - Test summary is scoped to the authenticated user and excludes other users' data
    - _Requirements: 21.1, 21.2_

- [ ] 16. Implement assistant conversations and messages (Assistant_Service)
  - [~] 16.1 Implement Conversation and Message entities and repositories
    - Create Conversation (status ACTIVE|ARCHIVED) and Message (role, content, agent_execution_id?) entities with owner-scoped repositories
    - _Requirements: 23.1, 23.2_
  - [~] 16.2 Implement assistant service and controller (message wiring deferred to agent)
    - Implement createConversation (ACTIVE), listConversations (owner-scoped), and postMessage skeleton that verifies conversation ownership and persists the USER message; return 404 for non-owned; expose controller under `/api/v1`
    - _Requirements: 23.1, 23.2, 23.3, 23.5, 31.1_
  - [~] 16.3 Write integration tests for conversation ownership
    - Test conversation creation/listing scope and cross-user 404
    - _Requirements: 23.1, 23.2, 23.5_

- [ ] 17. Implement agent orchestration, tools, SSE, and assistant wiring (Agent_Orchestrator)
  - [~] 17.1 Implement AgentExecution and AgentStep entities and repositories
    - Create AgentExecution (status, model_provider, model_name, tokens, failure fields) and AgentStep entities with owner-scoped repositories
    - _Requirements: 25.1, 25.2_
  - [~] 17.2 Implement the seven Java tools with server-side identity binding
    - Implement searchDocuments, getDocument, searchObligations, getUpcomingObligations, searchActions, getUpcomingActions, searchDocumentContent; each executes in Java, binds identity to the server-side authenticated userId (ignoring any LLM-supplied user_id), validates auth/ownership/input, and returns error indication on failure; never grant DB writes
    - _Requirements: 24.1, 24.2, 24.3, 24.4, 24.5, 24.8, 26.2, 26.3_
  - [~] 17.3 Implement agent orchestrator loop and answer composition
    - Implement `AgentOrchestrator.run` creating AgentExecution RUNNING, driving Spring AI tool-calling loop with IterationGuard (≤10 iterations → error indication), recording each AgentStep, composing answers only from tool-returned data (else "not available"), and setting COMPLETED (tokens) or FAILED (failure_code/message)
    - _Requirements: 24.6, 24.7, 25.1, 25.2, 25.3, 25.4, 26.1_
  - [~] 17.4 Wire orchestrator into assistant postMessage
    - Update `AssistantService.postMessage` to invoke the orchestrator and persist the ASSISTANT reply linked to agent_execution_id
    - _Requirements: 23.3, 23.4_
  - [~] 17.5 Implement SSE agent event streaming
    - Implement `AgentEventController.stream` emitting AgentStep updates for owner-owned executions; non-owned → 404 RESOURCE_NOT_FOUND
    - _Requirements: 25.5, 25.6_
  - [~] 17.6 Write unit tests for tool identity binding and iteration guard
    - Test that LLM-supplied user_id is ignored, ownership is enforced, and the 10-iteration limit stops with an error indication
    - _Requirements: 24.4, 24.5, 24.7, 26.3_
  - [~] 17.7 Write integration tests for agent, prompt injection, and SSE ownership
    - Test grounded answers from tool data, prompt-injection instructions ignored, no-data "not available" response, and SSE 404 for non-owned executions
    - _Requirements: 24.6, 26.1, 26.2, 25.5, 25.6_

- [~] 18. Checkpoint - Ensure all tests pass
  - Ensure all tests pass, ask the user if questions arise.

- [ ] 19. Implement remaining cross-cutting operational concerns
  - [~] 19.1 Implement optimistic locking conflict handling
    - Map optimistic-locking failures to 409 STALE_UPDATE in the global exception handler across mutable entities
    - _Requirements: 28.1, 28.2_
  - [~] 19.2 Configure API surface, CORS, and OpenAPI documentation
    - Confirm all endpoints under `/api/v1`, add CORS for the configured frontend origin, and publish OpenAPI spec + Swagger UI
    - _Requirements: 31.1, 31.2, 31.3_
  - [~] 19.3 Implement observability: structured logging, metrics, and trace IDs
    - Emit structured logs with per-request trace identifier and agent execution identifier; register metrics for HTTP latency/errors, processing duration/failures, AI latency/failures/tokens, agent duration, tool calls, and retry counts; expose Actuator health and metrics
    - _Requirements: 33.1, 33.2, 32.2_
  - [~] 19.4 Provide containerization and environment configuration
    - Create Dockerfile and Docker Compose starting the service and PostgreSQL; wire all env-var-based configuration without committing secrets
    - _Requirements: 32.1, 32.3, 32.4_
  - [~] 19.5 Write integration tests for error responses and optimistic locking
    - Test standardized error body fields, VALIDATION_ERROR errors array, and 409 STALE_UPDATE on stale version updates
    - _Requirements: 29.1, 29.2, 28.2_

- [~] 20. Final checkpoint - Ensure all tests pass
  - Ensure all tests pass, ask the user if questions arise.

## Notes

- Tasks marked with `*` are optional test tasks and can be skipped for a faster MVP; core implementation tasks are never optional.
- The design does not define a "Correctness Properties" section, so testing uses unit tests (for pure domain helpers such as validators, classifiers, dedup identity, and retry/backoff) and integration tests (for behavioral and ownership requirements) rather than property-based tests.
- Each task references specific requirements clauses for traceability.
- Checkpoints ensure incremental validation at natural module boundaries.
- Performance target Requirement 34.1 (p95 latency) and degraded-AI availability (34.2, 34.3) are satisfied structurally by the async processing, retry policy, and non-AI endpoints already covered above; they are validated through the existing integration tests rather than standalone performance-measurement tasks (which are out of coding scope).
- Optional Requirements 36–38 (PGVector/RAG, email delivery, Redis, OCR, cloud storage, advanced rate limiting) are intentionally excluded from this MUST-BUILD plan.

## Task Dependency Graph

```json
{
  "waves": [
    { "id": 0, "tasks": ["1.1"] },
    { "id": 1, "tasks": ["1.2", "1.3"] },
    { "id": 2, "tasks": ["1.4", "2.1", "3.1"] },
    { "id": 3, "tasks": ["3.2", "4.1", "8.1"] },
    { "id": 4, "tasks": ["3.3", "3.4", "4.2", "8.2", "8.3", "9.1"] },
    { "id": 5, "tasks": ["4.3", "8.4", "8.5", "9.2", "9.3", "7.1", "7.2"] },
    { "id": 6, "tasks": ["4.4", "7.3", "9.4", "10.1"] },
    { "id": 7, "tasks": ["4.5", "7.4", "10.2"] },
    { "id": 8, "tasks": ["4.6", "4.7", "5.1", "7.5", "10.3"] },
    { "id": 9, "tasks": ["5.2", "7.6", "10.4", "12.1"] },
    { "id": 10, "tasks": ["7.7", "7.8", "10.5", "10.6", "12.2"] },
    { "id": 11, "tasks": ["12.3", "13.1", "14.1", "15.1", "16.1", "17.1"] },
    { "id": 12, "tasks": ["12.4", "12.5", "13.2", "14.2", "15.2", "16.2", "17.2"] },
    { "id": 13, "tasks": ["14.3", "16.3", "17.3", "17.5"] },
    { "id": 14, "tasks": ["17.4", "17.6", "17.7", "19.1", "19.2", "19.3", "19.4"] },
    { "id": 15, "tasks": ["19.5"] }
  ]
}
```
