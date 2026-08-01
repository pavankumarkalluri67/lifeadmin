package com.LifeAdmin.ai.lifeadmin.agent.domain;

/**
 * Tool execution result wrapper.
 * Requirement 24
 */
public class ToolExecutionResult {
    private final boolean success;
    private final Object data;
    private final String errorMessage;

    public ToolExecutionResult(Object data) {
        this.success = true;
        this.data = data;
        this.errorMessage = null;
    }

    public ToolExecutionResult(String errorMessage) {
        this.success = false;
        this.data = null;
        this.errorMessage = errorMessage;
    }

    public boolean isSuccess() {
        return success;
    }

    public Object getData() {
        return data;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    @Override
    public String toString() {
        if (success) {
            return "Success: " + data;
        } else {
            return "Error: " + errorMessage;
        }
    }
}
