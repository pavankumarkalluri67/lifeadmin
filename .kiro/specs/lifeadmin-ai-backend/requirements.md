# Requirements Document

## Introduction

LifeAdmin AI is an AI-powered personal life administration backend service that helps users manage responsibilities hidden inside everyday documents such as invoices, receipts, warranty documents, insurance policies, rental agreements, subscription invoices, utility bills, memberships, service contracts, and travel documents. The service converts unstructured document information (payment deadlines, expiration dates, renewal dates, warranty periods, return windows, notice periods) into structured, actionable data (extracted entities, obligations, action items, reminders). Users interact with a natural-language AI assistant to ask questions such as "What warranties expire in the next 90 days?" or "What should I take care of this month?".

The system is governed by one core engineering principle: **"AI proposes. Software validates. Users control."** The Large Language Model (LLM) handles understanding, extraction, classification, reasoning, planning, natural-language generation, and tool selection. The Java application owns authentication, authorization, validation, database access, transactions, business rules, persistence, data consistency, idempotency, security, and auditability. The LLM is never a trusted component, never receives unrestricted database access, and never performs database writes directly.

This document defines the requirements for the MUST-BUILD scope of a three-week project built with Java 21, Spring Boot, Spring AI, PostgreSQL, Flyway, Spring Data JPA, and Spring Security. The architecture is a modular monolith under base package `com.lifeadmin` with a strict dependency direction of Controller → Application Service → Domain → Repository Interface → Infrastructure. Requirements that fall outside MUST-BUILD scope are captured in the "Optional and Future Requirements" section and are explicitly marked as optional.

## Glossary

- **System**: The LifeAdmin AI backend service as a whole.
- **Auth_Service**: The module responsible for user registration, login, token issuance, token refresh, and logout.
- **User_Service**: The module responsible for user profile retrieval and updates.
- **Document_Service**: The module responsible for document upload, storage, listing, retrieval, download, deletion, and reprocessing.
- **Storage_Service**: The storage abstraction responsible for persisting and retrieving document binary content.
- **Processing_Service**: The module responsible for orchestrating asynchronous document processing jobs.
- **Extraction_Service**: The module responsible for text extraction, text normalization, and persistence of extracted entities.
- **Obligation_Service**: The module responsible for creating, listing, retrieving, updating, confirming, dismissing, and completing obligations.
- **Action_Service**: The module responsible for managing action items.
- **Reminder_Service**: The module responsible for managing reminders.
- **Dashboard_Service**: The module responsible for producing dashboard summaries.
- **Assistant_Service**: The module responsible for conversations, messages, and natural-language interaction with the user.
- **Agent_Orchestrator**: The single component that orchestrates AI assistant reasoning using Spring AI tool calling and records agent execution steps.
- **AI_Service**: The module that abstracts LLM provider access, structured output generation, and tool-calling integration.
- **AI_Provider**: The configured LLM backend (local via Ollama, or cloud via OpenAI or Gemini) selected through the `AI_PROVIDER` configuration value.
- **Notification_Service**: The module responsible for delivering reminders through configured channels.
- **LLM**: The Large Language Model invoked through the AI_Provider.
- **Obligation**: A structured, actionable responsibility derived from a document or created by a user (for example, a payment due or a warranty expiration).
- **Action_Item**: A user- or AI-suggested task, optionally linked to an obligation.
- **Reminder**: A scheduled notification linked to an obligation or action item.
- **Extracted_Entity**: A structured value extracted from document text (for example, a due date or an amount) with an associated confidence score.
- **Processing_Job**: A database-backed record tracking the asynchronous processing of a single document.
- **DocumentAnalysisResult**: The structured AI output containing documentType, confidence, entities, and obligations.
- **Confidence**: A decimal value between 0 and 1 (inclusive) representing the AI's certainty about an extraction or classification.
- **Access_Token**: A short-lived JWT used to authenticate API requests.
- **Refresh_Token**: A long-lived token used to obtain a new Access_Token, stored hashed and subject to rotation.
- **Ownership_Validation**: The rule that a user-facing resource is retrieved only when it belongs to the requesting user (using findByIdAndUserId semantics).
- **Dead_Letter**: The terminal state of a Processing_Job that has exhausted its retry attempts.
- **SSE**: Server-Sent Events, used to stream agent execution progress to the frontend.
- **Dedup_Identity**: The composite key used to detect duplicate obligations: Document ID + Obligation Type + Normalized Due Date + Normalized Reference.
- **Untrusted_Data**: Any text extracted from an uploaded document, which may contain adversarial instructions and must never influence authorization.
- **p95**: The 95th percentile of a measured latency distribution.

## Requirements

### Requirement 1: User Registration

**User Story:** As a new user, I want to register an account with my email and password, so that I can access the LifeAdmin AI platform.

#### Acceptance Criteria

1. WHEN a registration request is received with an email of at most 320 characters in RFC 5322 addr-spec format that is not already registered, a password satisfying the password rules, a non-empty first_name, and a valid IANA timezone identifier, THE Auth_Service SHALL create a user record with status ACTIVE and return a 201 Created response.
2. THE Auth_Service SHALL store the user password only as a BCrypt hash in the password_hash field and SHALL NOT store or return the plaintext password.
3. THE Auth_Service SHALL enforce a password rule of 8 to 128 characters containing at least one uppercase letter, one lowercase letter, one digit, and one special character.
4. IF a registration request is received with an email that is already registered, THEN THE Auth_Service SHALL reject the request with a 409 Conflict response and code EMAIL_ALREADY_EXISTS and SHALL NOT create a user record.
5. IF a registration request is received with an invalid email format, a password that violates the password rule, a missing first_name, or an invalid timezone, THEN THE Auth_Service SHALL reject the request with a 400 Bad Request response and code VALIDATION_ERROR and SHALL NOT create a user record.
6. WHEN a user record is created, THE Auth_Service SHALL set email_verified to false, assign a generated UUID as the primary key, and persist first_name, timezone, and the optional last_name when provided.

### Requirement 2: User Login

**User Story:** As a registered user, I want to log in with my credentials, so that I can obtain tokens to access protected resources.

#### Acceptance Criteria

1. WHEN a login request is received with an email and password that match an ACTIVE user, THE Auth_Service SHALL return a 200 OK response containing an Access_Token and a Refresh_Token.
2. WHEN a login succeeds, THE Auth_Service SHALL set the user last_login_at to the current timestamp.
3. IF a login request is received with credentials that do not match any user, THEN THE Auth_Service SHALL reject the request with a 401 Unauthorized response and code INVALID_CREDENTIALS.
4. IF a login request is received for a user whose status is LOCKED or DISABLED, THEN THE Auth_Service SHALL reject the request with a 403 Forbidden response and code ACCOUNT_NOT_ACTIVE.
5. WHEN a Refresh_Token is issued, THE Auth_Service SHALL store the token as a hash in the refresh_tokens table with an expires_at timestamp and revoked set to false.

### Requirement 3: Access Token Refresh and Rotation

**User Story:** As an authenticated user, I want to refresh my access token, so that I can maintain my session without re-entering credentials.

#### Acceptance Criteria

1. WHEN a refresh request is received with a valid, non-revoked, non-expired Refresh_Token, THE Auth_Service SHALL return a new Access_Token and a new Refresh_Token.
2. WHEN a new Refresh_Token is issued through refresh, THE Auth_Service SHALL revoke the previous Refresh_Token by setting revoked to true and revoked_at to the current timestamp.
3. IF a refresh request is received with a Refresh_Token that is expired, revoked, or unknown, THEN THE Auth_Service SHALL reject the request with a 401 Unauthorized response and code INVALID_REFRESH_TOKEN.
4. THE Auth_Service SHALL compare presented Refresh_Tokens against stored token hashes without storing the raw token value.

### Requirement 4: Logout

**User Story:** As an authenticated user, I want to log out, so that my refresh token can no longer be used.

#### Acceptance Criteria

1. WHEN a logout request is received with a valid Refresh_Token, THE Auth_Service SHALL revoke the Refresh_Token and return a 200 OK response.
2. IF a logout request references a Refresh_Token that is already revoked or unknown, THEN THE Auth_Service SHALL return a 200 OK response without changing any additional state.

### Requirement 5: User Profile Retrieval and Update

**User Story:** As an authenticated user, I want to view and update my profile, so that my personal details and timezone are accurate.

#### Acceptance Criteria

1. WHEN an authenticated request is received for the current user profile, THE User_Service SHALL return a 200 OK response containing the user email, first_name, last_name, timezone, status, and email_verified fields.
2. WHEN an authenticated update request is received with valid first_name, last_name, or timezone values, THE User_Service SHALL persist the changes and return the updated profile.
3. IF an update request contains an invalid timezone value, THEN THE User_Service SHALL reject the request with a 400 Bad Request response and code VALIDATION_ERROR.
4. THE User_Service SHALL restrict profile retrieval and update to the authenticated user identified by the Access_Token.

### Requirement 6: Authentication and Authorization Enforcement

**User Story:** As a platform operator, I want every protected endpoint to enforce authentication and ownership, so that user data remains isolated and secure.

#### Acceptance Criteria

1. IF a request to a protected endpoint is received without a valid Access_Token, THEN THE System SHALL reject the request with a 401 Unauthorized response and code UNAUTHENTICATED.
2. WHEN a request accesses a user-owned resource, THE System SHALL retrieve the resource using Ownership_Validation such that resources not belonging to the authenticated user are treated as not found.
3. IF an authenticated user requests a resource owned by another user, THEN THE System SHALL respond with a 404 Not Found response and code RESOURCE_NOT_FOUND.
4. THE System SHALL apply Ownership_Validation to documents, obligations, action items, reminders, conversations, and agent executions.

### Requirement 7: Document Upload

**User Story:** As an authenticated user, I want to upload documents, so that the system can process them and detect my obligations.

#### Acceptance Criteria

1. WHEN an authenticated upload request is received with a file of content type application/pdf or text/plain, THE Document_Service SHALL compute a SHA-256 checksum, store the file through the Storage_Service, create a Document record with processing_status UPLOADED, create a Processing_Job, and return a 202 Accepted response.
2. WHEN a Document record is created, THE Document_Service SHALL persist a sanitized original_file_name (at most 255 characters, with path separators and control characters removed), stored_file_name, storage_key, content_type, file_extension, file_size_bytes, checksum_sha256, and uploaded_at.
3. IF an upload request contains a file whose content type is neither application/pdf nor text/plain, THEN THE Document_Service SHALL reject the request with a 400 Bad Request response and code UNSUPPORTED_FILE_TYPE and SHALL NOT store the file or create a Document record.
4. IF an upload request contains a file whose size exceeds the configured maximum (default 10,485,760 bytes), THEN THE Document_Service SHALL reject the request with a 400 Bad Request response and code FILE_TOO_LARGE and SHALL NOT store the file or create a Document record.
5. WHEN a Document record is created, THE Document_Service SHALL set document_type to UNKNOWN until processing completes.
6. WHEN a Processing_Job is created for an uploaded document, THE Document_Service SHALL set the job status to PENDING and the current_stage to QUEUED.
7. IF an upload request contains a file of 0 bytes, THEN THE Document_Service SHALL reject the request with a 400 Bad Request response and code EMPTY_FILE.
8. IF the file signature (magic bytes) does not match the declared content type, THEN THE Document_Service SHALL reject the request with a 400 Bad Request response and code CONTENT_TYPE_MISMATCH.
9. IF an authenticated upload request contains a file whose SHA-256 checksum matches an existing document owned by the same user, THEN THE Document_Service SHALL reject the request with a 409 Conflict response and code DUPLICATE_DOCUMENT.

### Requirement 8: Document Listing and Retrieval

**User Story:** As an authenticated user, I want to list and view my documents, so that I can review what I have uploaded and its processing status.

#### Acceptance Criteria

1. WHEN an authenticated request lists documents, THE Document_Service SHALL return only documents owned by the authenticated user in a paginated response.
2. THE Document_Service SHALL support pagination parameters and filtering by document_type and processing_status when listing documents.
3. WHEN an authenticated request retrieves a document by identifier owned by the user, THE Document_Service SHALL return the document metadata including processing_status and document_type.
4. IF a document retrieval request references an identifier not owned by the authenticated user, THEN THE Document_Service SHALL respond with a 404 Not Found response and code RESOURCE_NOT_FOUND.

### Requirement 35: Document Download

**User Story:** As an authenticated user, I want to download the original file of a document I uploaded, so that I can retrieve the source content.

#### Acceptance Criteria

1. WHEN an authenticated download request references a document owned by the user, THE Document_Service SHALL retrieve the file through the Storage_Service and return the file content with the stored content_type.
2. IF a download request references a document not owned by the authenticated user, THEN THE Document_Service SHALL respond with a 404 Not Found response and code RESOURCE_NOT_FOUND.
3. IF the stored file cannot be located by the Storage_Service, THEN THE Document_Service SHALL respond with a 404 Not Found response and code FILE_NOT_FOUND.

### Requirement 9: Document Deletion

**User Story:** As an authenticated user, I want to delete a document, so that I can remove data I no longer need.

#### Acceptance Criteria

1. WHEN an authenticated deletion request references a document owned by the user, THE Document_Service SHALL remove the Document record and the associated stored file and return a 204 No Content response.
2. IF a deletion request references a document not owned by the authenticated user, THEN THE Document_Service SHALL respond with a 404 Not Found response and code RESOURCE_NOT_FOUND.

### Requirement 10: Document Reprocessing

**User Story:** As an authenticated user, I want to reprocess a document, so that I can re-run analysis after a failure or model change.

#### Acceptance Criteria

1. WHEN an authenticated reprocess request references a document owned by the user, THE Document_Service SHALL create or reset a Processing_Job to status PENDING with current_stage QUEUED and return a 202 Accepted response.
2. WHEN a document is reprocessed, THE Processing_Service SHALL NOT create duplicate obligations for the document, as defined by Requirement 15.
3. IF a reprocess request references a document not owned by the authenticated user, THEN THE Document_Service SHALL respond with a 404 Not Found response and code RESOURCE_NOT_FOUND.

### Requirement 11: Asynchronous Processing Orchestration

**User Story:** As a platform operator, I want documents processed asynchronously through database-backed jobs, so that uploads return quickly and processing survives restarts.

#### Acceptance Criteria

1. WHEN a Processing_Job has status PENDING, THE Processing_Service SHALL execute the job asynchronously using a Spring TaskExecutor without blocking the upload request.
2. WHILE a Processing_Job is executing, THE Processing_Service SHALL set the job status to RUNNING and update current_stage as the job advances through TEXT_EXTRACTION, TEXT_NORMALIZATION, AI_ANALYSIS, ENTITY_PERSISTENCE, and OBLIGATION_PERSISTENCE.
3. WHEN a Processing_Job completes all stages successfully, THE Processing_Service SHALL set the job status to COMPLETED, set current_stage to COMPLETED, set completed_at, and set the Document processing_status to PROCESSED.
4. WHEN the System starts, THE Processing_Service SHALL recover Processing_Jobs left in status RUNNING or RETRY_PENDING so that processing can resume.
5. WHILE a document is being processed, THE Document_Service SHALL reflect the document processing_status as QUEUED or PROCESSING.

### Requirement 12: Text Extraction and Normalization

**User Story:** As a platform operator, I want text extracted and normalized from documents, so that the AI can analyze consistent content.

#### Acceptance Criteria

1. WHEN a Processing_Job reaches the TEXT_EXTRACTION stage for a PDF document, THE Extraction_Service SHALL extract text and record the extraction_method as PDFBOX or TIKA.
2. WHEN a Processing_Job reaches the TEXT_EXTRACTION stage for a plain text document, THE Extraction_Service SHALL read text and record the extraction_method as PLAIN_TEXT.
3. WHEN text is extracted, THE Extraction_Service SHALL persist a document_contents record containing raw_text, normalized_text, character_count, and extracted_at.
4. IF text extraction produces no readable text, THEN THE Processing_Service SHALL set the Document processing_status to FAILED with processing_error_code EXTRACTION_EMPTY and SHALL NOT retry the job.

### Requirement 13: AI Document Analysis and Structured Output Validation

**User Story:** As a platform operator, I want the AI to analyze document text and produce validated structured output, so that only trustworthy data is persisted.

#### Acceptance Criteria

1. WHEN a Processing_Job reaches the AI_ANALYSIS stage, THE AI_Service SHALL request a DocumentAnalysisResult containing documentType, confidence, entities, and obligations from the configured AI_Provider.
2. WHEN a DocumentAnalysisResult is received, THE Processing_Service SHALL validate the result against Java validation rules before any persistence occurs.
3. IF a received DocumentAnalysisResult fails Java validation, THEN THE Processing_Service SHALL treat the result as invalid and apply the retry policy defined in Requirement 16.
4. THE Processing_Service SHALL perform AI network calls outside any open database transaction, following the pattern of read data, commit, call AI, validate, begin transaction, persist, commit.
5. WHEN a valid DocumentAnalysisResult is persisted, THE Processing_Service SHALL set the Document document_type to the classified type.

### Requirement 14: Entity Extraction and Persistence

**User Story:** As a user, I want the system to extract structured entities from my documents, so that key facts like due dates and amounts are captured.

#### Acceptance Criteria

1. WHEN a valid DocumentAnalysisResult contains entities, THE Extraction_Service SHALL persist each entity as an Extracted_Entity with entity_type, entity_value, normalized_value, confidence, extraction_method, ai_model, and prompt_version.
2. THE Extraction_Service SHALL store each Extracted_Entity confidence as a decimal value between 0 and 1 inclusive.
3. IF a proposed entity has an entity_type not defined in the allowed entity type set, THEN THE Extraction_Service SHALL reject that entity and exclude it from persistence.
4. WHEN a document is reprocessed, THE Extraction_Service SHALL replace prior Extracted_Entity records for the document so that stale entities are not retained.

### Requirement 15: Obligation Detection with Human-in-the-Loop Thresholds

**User Story:** As a user, I want the system to detect obligations from my documents with appropriate confirmation, so that I stay in control of low-confidence items.

#### Acceptance Criteria

1. WHEN a valid DocumentAnalysisResult contains an obligation with confidence greater than or equal to the configured high-confidence threshold (default 0.90), THE Obligation_Service SHALL create an Obligation with status DETECTED and requires_confirmation set to false.
2. WHEN a valid DocumentAnalysisResult contains an obligation with confidence greater than or equal to the configured review threshold (default 0.70) and less than the high-confidence threshold, THE Obligation_Service SHALL create an Obligation with status DETECTED and requires_confirmation set to true.
3. IF a valid DocumentAnalysisResult contains an obligation with confidence less than the configured review threshold, THEN THE Obligation_Service SHALL NOT persist an Obligation and SHALL record an audit log entry containing document_id, type, and confidence.
4. THE Obligation_Service SHALL read the high-confidence and review thresholds from configuration as decimal values between 0 and 1 inclusive so that they can be adjusted without code changes.
5. WHEN an Obligation is created from a DocumentAnalysisResult, THE Obligation_Service SHALL set source_type to AI and populate document_id, type, title, and confidence.
6. WHEN an Obligation is created from a DocumentAnalysisResult, THE Obligation_Service SHALL set priority to the value provided by the obligation and SHALL fall back to a configurable default priority when none is provided.
7. WHEN a DocumentAnalysisResult obligation has no due date, THE Obligation_Service SHALL create the Obligation with due_date unset while still applying the confidence and requires_confirmation rules.

### Requirement 16: Processing Retry, Backoff, and Dead-Letter Handling

**User Story:** As a platform operator, I want transient failures retried and permanent failures dead-lettered, so that processing is resilient without infinite loops.

#### Acceptance Criteria

1. IF a Processing_Job fails due to an LLM timeout, an HTTP 429 response, or a temporary provider or storage failure, THEN THE Processing_Service SHALL set the job status to RETRY_PENDING, increment attempt_count, and set next_retry_at using exponential backoff with jitter.
2. IF a Processing_Job fails due to an unsupported file, a corrupted document, or invalid permanent input, THEN THE Processing_Service SHALL set the job status to FAILED without scheduling a retry.
3. WHEN a Processing_Job attempt_count reaches max_attempts, THE Processing_Service SHALL set the job status to DEAD_LETTER.
4. WHEN a Processing_Job enters DEAD_LETTER, THE Document_Service SHALL allow the user to manually reprocess the document.
5. WHEN a Processing_Job fails, THE Processing_Service SHALL record error_code and error_message on the job.

### Requirement 17: Obligation Idempotency

**User Story:** As a user, I want reprocessing to avoid duplicate obligations, so that my obligation list stays accurate.

#### Acceptance Criteria

1. WHEN the Obligation_Service creates obligations during processing, THE Obligation_Service SHALL compute the Dedup_Identity as the combination of Document ID, Obligation Type, Normalized Due Date, and Normalized Reference, and SHALL create at most one Obligation per Dedup_Identity.
2. WHEN the Obligation_Service computes a Dedup_Identity, THE Obligation_Service SHALL derive the Normalized Due Date by truncating the due date to calendar day (dropping any time-of-day component) and SHALL derive the Normalized Reference by trimming leading and trailing whitespace and converting all characters to lowercase.
3. WHEN a document is reprocessed, THE Obligation_Service SHALL NOT create a new Obligation for a Dedup_Identity that already exists for that document.
4. IF a reprocessed document produces an Obligation whose Dedup_Identity matches an existing Obligation that a user has confirmed or dismissed, THEN THE Obligation_Service SHALL NOT create a new Obligation and SHALL NOT change the existing confirmed or dismissed state.
5. WHERE the database can enforce uniqueness on the Dedup_Identity, THE System SHALL apply a database uniqueness constraint on the Dedup_Identity as the final protection against duplicate obligations.
6. IF an insert violates the Dedup_Identity uniqueness constraint, THEN THE Obligation_Service SHALL reject the insert, SHALL retain the existing Obligation unchanged, and SHALL treat the operation as a successful deduplication rather than an error to the user.

### Requirement 18: Obligation Management

**User Story:** As a user, I want to list, view, update, confirm, dismiss, and complete obligations, so that I can act on my responsibilities.

#### Acceptance Criteria

1. WHEN an authenticated request lists obligations, THE Obligation_Service SHALL return only obligations owned by the authenticated user in a paginated response supporting filtering by type, status, and due_date range.
2. WHEN an authenticated request confirms an Obligation owned by the user, THE Obligation_Service SHALL set status to CONFIRMED, set confirmed_at, and set requires_confirmation to false.
3. WHEN an authenticated request dismisses an Obligation owned by the user, THE Obligation_Service SHALL set status to DISMISSED and set dismissed_at.
4. WHEN an authenticated request completes an Obligation owned by the user, THE Obligation_Service SHALL set status to COMPLETED and set completed_at.
5. WHEN an authenticated request updates editable fields of an Obligation owned by the user, THE Obligation_Service SHALL persist the changes and return the updated Obligation.
6. IF an obligation operation references an identifier not owned by the authenticated user, THEN THE Obligation_Service SHALL respond with a 404 Not Found response and code RESOURCE_NOT_FOUND.

### Requirement 19: Action Item Management

**User Story:** As a user, I want to create and manage action items, so that I can track tasks related to my obligations.

#### Acceptance Criteria

1. WHEN an authenticated request creates an Action_Item with valid fields, THE Action_Service SHALL persist the Action_Item with status TODO and source_type USER and return a 201 Created response.
2. WHEN an authenticated request lists action items, THE Action_Service SHALL return only action items owned by the authenticated user in a paginated response supporting filtering by status and priority.
3. WHEN an authenticated request completes an Action_Item owned by the user, THE Action_Service SHALL set status to COMPLETED and set completed_at.
4. WHEN an authenticated request updates or deletes an Action_Item owned by the user, THE Action_Service SHALL apply the change and return the appropriate success response.
5. IF an Action_Item references an obligation_id, THEN THE Action_Service SHALL verify the referenced Obligation is owned by the authenticated user before persisting the link.
6. IF an action item operation references an identifier not owned by the authenticated user, THEN THE Action_Service SHALL respond with a 404 Not Found response and code RESOURCE_NOT_FOUND.

### Requirement 20: Reminder Management

**User Story:** As a user, I want to create and manage reminders, so that I am notified about obligations and actions at the right time.

#### Acceptance Criteria

1. WHEN an authenticated request creates a Reminder with a remind_at timestamp and a channel of IN_APP, THE Reminder_Service SHALL persist the Reminder with status SCHEDULED and return a 201 Created response.
2. WHEN a Reminder is created, THE Reminder_Service SHALL require that at least one of obligation_id or action_item_id references a resource owned by the authenticated user.
3. WHEN an authenticated request lists reminders, THE Reminder_Service SHALL return only reminders owned by the authenticated user in a paginated response supporting filtering by status.
4. WHEN an authenticated request updates or deletes a Reminder owned by the user, THE Reminder_Service SHALL apply the change and return the appropriate success response.
5. WHEN a scheduled Reminder reaches its remind_at time and the channel is IN_APP, THE Notification_Service SHALL mark the Reminder status as SENT and set sent_at.
6. IF a reminder operation references an identifier not owned by the authenticated user, THEN THE Reminder_Service SHALL respond with a 404 Not Found response and code RESOURCE_NOT_FOUND.

### Requirement 21: Dashboard Summary

**User Story:** As a user, I want a dashboard summary, so that I can quickly see what needs my attention.

#### Acceptance Criteria

1. WHEN an authenticated request retrieves the dashboard summary, THE Dashboard_Service SHALL return counts and upcoming items scoped to the authenticated user, including upcoming obligations, obligations requiring confirmation, open action items, and scheduled reminders.
2. THE Dashboard_Service SHALL compute the summary using only data owned by the authenticated user.

### Requirement 22: AI Provider Configuration

**User Story:** As a platform operator, I want to select the AI provider through configuration, so that the system can run against local or cloud models.

#### Acceptance Criteria

1. WHERE the AI_PROVIDER configuration value selects a local model, THE AI_Service SHALL invoke the LLM through Ollama using OLLAMA_BASE_URL.
2. WHERE the AI_PROVIDER configuration value selects a cloud model, THE AI_Service SHALL invoke the LLM through OpenAI or Gemini using the corresponding API key.
3. THE AI_Service SHALL expose structured output generation and tool-calling capabilities regardless of the selected AI_Provider.
4. IF the configured AI_Provider is unreachable, THEN THE AI_Service SHALL surface a provider unavailable error so that dependent flows can apply their retry or degradation rules.
5. THE domain layer SHALL NOT depend on any model provider SDK.

### Requirement 23: AI Assistant Conversations and Messages

**User Story:** As a user, I want to converse with an AI assistant, so that I can ask natural-language questions about my life administration data.

#### Acceptance Criteria

1. WHEN an authenticated request creates a conversation, THE Assistant_Service SHALL persist a Conversation with status ACTIVE owned by the authenticated user and return a 201 Created response.
2. WHEN an authenticated request lists conversations, THE Assistant_Service SHALL return only conversations owned by the authenticated user.
3. WHEN an authenticated request posts a message to a conversation owned by the user, THE Assistant_Service SHALL persist a message with role USER, invoke the Agent_Orchestrator, and persist the assistant reply with role ASSISTANT.
4. WHEN an assistant reply is generated, THE Assistant_Service SHALL link the message to its agent_execution_id.
5. IF a conversation operation references an identifier not owned by the authenticated user, THEN THE Assistant_Service SHALL respond with a 404 Not Found response and code RESOURCE_NOT_FOUND.

### Requirement 24: Agent Orchestration with Tool Calling

**User Story:** As a user, I want the assistant to answer using my structured data through validated tools, so that answers are grounded and secure.

#### Acceptance Criteria

1. WHEN the Agent_Orchestrator processes a user query, THE Agent_Orchestrator SHALL make exactly the following seven tools available to the LLM through Spring AI tool calling: searchDocuments, getDocument, searchObligations, getUpcomingObligations, searchActions, getUpcomingActions, and searchDocumentContent.
2. WHEN the LLM selects a tool, THE System SHALL execute the tool in Java rather than allowing the LLM to access the database directly.
3. WHEN any tool is executed, THE tool SHALL validate authentication, authorization, user ownership, and input before returning data, and IF any of these validations fails, THEN THE tool SHALL return no data and return an error indication describing the failed validation.
4. WHEN a tool is executed, THE System SHALL bind the user identity used for that execution to the authenticated identity established server-side from the request security context, independent of any value supplied by the LLM.
5. IF the LLM supplies a user_id (or any equivalent user-identity) parameter in a tool call, THEN THE System SHALL ignore the supplied value and use the server-side authenticated identity for tool execution.
6. WHEN the Agent_Orchestrator composes an answer for a user query, THE Agent_Orchestrator SHALL derive the answer from data returned by the tools rather than from unverified model-generated content, and IF no tool returns data relevant to the query, THEN THE Agent_Orchestrator SHALL respond that the requested information is not available rather than generating unsupported content.
7. WHILE processing a single user query, THE Agent_Orchestrator SHALL execute at most 10 tool-call iterations, and IF the limit of 10 iterations is reached without a final answer, THEN THE Agent_Orchestrator SHALL stop further tool calls and return an error indication that the query could not be resolved.
8. THE System SHALL NOT grant the LLM the ability to perform database writes.

### Requirement 25: Agent Execution Tracking and SSE Progress

**User Story:** As a user, I want to see the assistant's progress as it works, so that I understand how my answer is produced.

#### Acceptance Criteria

1. WHEN the Agent_Orchestrator begins processing a query, THE System SHALL create an AgentExecution with status RUNNING and record model_provider and model_name.
2. WHILE the Agent_Orchestrator processes a query, THE System SHALL record each AgentStep with step_number, step_type, name, status, and where applicable tool_name, input_summary, and output_summary.
3. WHEN an AgentExecution completes, THE System SHALL set status to COMPLETED, set completed_at, and record input_tokens and output_tokens.
4. IF an AgentExecution fails, THEN THE System SHALL set status to FAILED and record failure_code and failure_message.
5. WHEN a client subscribes to agent execution events for an AgentExecution owned by the user, THE System SHALL stream AgentStep progress updates over SSE.
6. IF a client requests agent execution events for an AgentExecution not owned by the authenticated user, THEN THE System SHALL respond with a 404 Not Found response and code RESOURCE_NOT_FOUND.

### Requirement 26: Prompt Injection Protection

**User Story:** As a platform operator, I want document text treated as untrusted, so that embedded instructions cannot compromise security or authorization.

#### Acceptance Criteria

1. THE System SHALL treat all text extracted from uploaded documents as Untrusted_Data.
2. IF Untrusted_Data contains instructions that attempt to alter authorization or access other users' data, THEN THE System SHALL ignore those instructions and enforce authorization in Java.
3. WHEN a tool is invoked during agent orchestration, THE tool SHALL enforce Ownership_Validation independently of any content produced by the LLM.

### Requirement 27: AI Invocation Auditing

**User Story:** As a platform operator, I want AI invocations recorded, so that I can audit usage, latency, and failures without storing sensitive prompt content.

#### Acceptance Criteria

1. WHEN the AI_Service invokes the LLM, THE System SHALL record an ai_invocations entry with operation_type, provider, model, prompt_version, status, latency_ms, and where available input_tokens and output_tokens.
2. THE System SHALL NOT store full prompt text in ai_invocations by default.
3. IF an AI invocation fails, times out, or is rate limited, THEN THE System SHALL record the corresponding status of FAILED, TIMEOUT, or RATE_LIMITED and an error_code.

### Requirement 28: Optimistic Locking and Concurrency

**User Story:** As a user, I want concurrent updates handled safely, so that I do not silently overwrite changes.

#### Acceptance Criteria

1. THE System SHALL apply optimistic locking using a version field on mutable primary entities.
2. IF a mutable entity is updated with a stale version, THEN THE System SHALL reject the update with a 409 Conflict response and code STALE_UPDATE.

### Requirement 29: Standardized Error Responses

**User Story:** As a frontend developer, I want consistent error responses, so that I can handle failures reliably.

#### Acceptance Criteria

1. WHEN a request results in an error, THE System SHALL return a response body containing timestamp, status, code, message, path, and traceId.
2. WHEN a request fails Bean Validation, THE System SHALL return code VALIDATION_ERROR and an errors array where each entry contains field and message.
3. THE System SHALL produce error responses through central exception handling rather than per-controller handling.

### Requirement 30: Schema Management and Database Conventions

**User Story:** As a platform operator, I want the database schema managed by Flyway with consistent conventions, so that schema changes are controlled and validated.

#### Acceptance Criteria

1. THE System SHALL manage all database schema changes through Flyway migrations.
2. THE System SHALL run with Hibernate ddl-auto set to validate.
3. THE System SHALL use UUID primary keys and TIMESTAMPTZ timestamps mapped to Instant for all primary entities.
4. THE System SHALL define explicit foreign keys for entity relationships and maintain created_at and updated_at where applicable.

### Requirement 31: API Surface, CORS, and Documentation

**User Story:** As a frontend developer, I want documented REST APIs under a stable base path with CORS support, so that I can integrate the frontend.

#### Acceptance Criteria

1. THE System SHALL expose REST endpoints under the base path /api/v1.
2. THE System SHALL apply CORS configuration so that the configured frontend origin can call the API.
3. THE System SHALL publish an OpenAPI specification and a Swagger UI for the exposed endpoints.
4. THE System SHALL keep controllers free of business logic and SHALL NOT allow controllers to access repositories directly.

### Requirement 32: Operational Readiness

**User Story:** As a platform operator, I want the service containerized and observable, so that I can deploy and monitor it.

#### Acceptance Criteria

1. THE System SHALL provide a Docker image and a Docker Compose configuration that starts the service and its PostgreSQL dependency.
2. THE System SHALL expose Spring Actuator health and metrics endpoints.
3. THE System SHALL read configuration from environment variables including DATABASE_URL, DATABASE_USERNAME, DATABASE_PASSWORD, JWT_SECRET, AI_PROVIDER, OPENAI_API_KEY, GEMINI_API_KEY, OLLAMA_BASE_URL, and FILE_STORAGE_PATH.
4. THE System SHALL NOT commit secret values to source control.
5. THE System SHALL support the profiles local, test, and prod.

### Requirement 33: Observability and Traceability

**User Story:** As a platform operator, I want structured logs and metrics with trace identifiers, so that I can diagnose issues across requests and processing.

#### Acceptance Criteria

1. THE System SHALL emit structured logs that include a trace identifier for each request and an agent execution identifier for agent operations.
2. THE System SHALL record metrics for HTTP latency and errors, document processing duration and failures, AI latency and failures and token usage, agent duration, tool calls, and retry counts.

### Requirement 34: Performance and Degraded AI Availability

**User Story:** As a user, I want core features fast and available even when AI is down, so that the platform remains useful.

#### Acceptance Criteria

1. THE System SHALL target a p95 latency below 300 milliseconds for normal REST APIs, excluding AI calls, file processing, and external provider calls.
2. WHILE the AI_Provider is unavailable, THE System SHALL keep login, document upload, document viewing, manual obligation creation, action management, and reminder management available.
3. WHILE the AI_Provider is unavailable, THE Processing_Service SHALL apply the retry policy defined in Requirement 16 to AI-dependent processing stages.

## Optional and Future Requirements

The following capabilities are explicitly out of MUST-BUILD scope for this three-week project. They are documented for future planning and are marked optional. They SHALL NOT block completion of the MUST-BUILD scope.

### Requirement 36: Semantic Search and Retrieval-Augmented Generation (Optional)

**User Story:** As a user, I want semantic document search, so that the assistant can retrieve relevant passages by meaning.

#### Acceptance Criteria

1. WHERE PGVector and embedding generation are enabled, THE System SHALL generate document_chunks with embeddings and support the searchDocumentContent tool through semantic retrieval.
2. WHERE retrieval-augmented generation is enabled, THE Agent_Orchestrator SHALL incorporate retrieved passages as grounding context.

### Requirement 37: Email Reminder Delivery (Optional)

**User Story:** As a user, I want email reminders, so that I am notified outside the application.

#### Acceptance Criteria

1. WHERE the email channel is enabled, THE Notification_Service SHALL deliver reminders with channel EMAIL and set status SENT on success.
2. IF email delivery fails, THEN THE Notification_Service SHALL set the Reminder status to FAILED and record failure_reason.

### Requirement 38: Additional Optional Capabilities (Optional)

**User Story:** As a platform operator, I want optional enhancements available when time permits, so that the platform can scale and improve.

#### Acceptance Criteria

1. WHERE Redis is enabled, THE System SHALL use Redis for caching or distributed coordination.
2. WHERE OCR or multimodal analysis is enabled, THE Extraction_Service SHALL extract text from image-based documents such as PNG and JPG using extraction_method OCR or MULTIMODAL_AI.
3. WHERE cloud object storage is enabled, THE Storage_Service SHALL persist files to the configured cloud object store through the storage abstraction.
4. WHERE advanced rate limiting is enabled, THE System SHALL enforce per-user request rate limits.
5. WHERE an AI evaluation dashboard is enabled, THE System SHALL present AI quality and usage metrics.
