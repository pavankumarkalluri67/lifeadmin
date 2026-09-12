package com.LifeAdmin.ai.lifeadmin.ai.infrastructure;

/**
 * Prompt text and JSON response schema used by {@link GeminiAiClient}.
 */
final class GeminiPrompts {

    private GeminiPrompts() {
    }

    static final String ANALYSIS_SYSTEM_PROMPT = """
            You are a document analysis engine for a personal life-admin assistant.
            Read the supplied document text and return ONLY JSON matching the provided schema.

            Rules:
            - documentType: one of INVOICE, BILL, CONTRACT, INSURANCE, TAX, LEGAL_NOTICE, BANK_STATEMENT, MEDICAL, OTHER.
            - entities: concrete values found verbatim in the text. entityType is one of
              DATE, AMOUNT, ACCOUNT_NUMBER, REFERENCE, ORGANIZATION, PERSON, ADDRESS, PHONE, EMAIL.
              normalizedValue: ISO-8601 (yyyy-MM-dd) for dates, plain decimal for amounts, else the cleaned value.
            - obligations: things the reader must DO (pay, renew, file, respond, submit).
              type is one of PAYMENT, RENEWAL, FILING, RESPONSE, APPOINTMENT, OTHER.
              dueDate must be yyyy-MM-dd or omitted when the document states no date.
              priority is one of LOW, MEDIUM, HIGH.
              reference is the invoice/policy/case number tied to the obligation when present.
            - confidence values are decimals between 0.0 and 1.0.
            - Never invent facts. If nothing is found, return empty arrays.
            """;

    /** Gemini responseSchema (OpenAPI subset) forcing structured output. */
    static final String ANALYSIS_RESPONSE_SCHEMA = """
            {
              "type": "OBJECT",
              "properties": {
                "documentType": { "type": "STRING" },
                "confidence": { "type": "NUMBER" },
                "entities": {
                  "type": "ARRAY",
                  "items": {
                    "type": "OBJECT",
                    "properties": {
                      "entityType": { "type": "STRING" },
                      "entityValue": { "type": "STRING" },
                      "normalizedValue": { "type": "STRING" },
                      "confidence": { "type": "NUMBER" }
                    },
                    "required": ["entityType", "entityValue", "confidence"]
                  }
                },
                "obligations": {
                  "type": "ARRAY",
                  "items": {
                    "type": "OBJECT",
                    "properties": {
                      "type": { "type": "STRING" },
                      "title": { "type": "STRING" },
                      "dueDate": { "type": "STRING" },
                      "priority": { "type": "STRING" },
                      "reference": { "type": "STRING" },
                      "confidence": { "type": "NUMBER" }
                    },
                    "required": ["type", "title", "confidence"]
                  }
                }
              },
              "required": ["documentType", "confidence", "entities", "obligations"]
            }
            """;

    static final String CHAT_SYSTEM_PROMPT = """
            You are the LifeAdmin assistant. Answer questions about the user's documents,
            obligations and actions. When a registered tool can supply the data you need,
            request the tool instead of guessing. Keep answers short and factual.
            """;
}
