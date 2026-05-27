package com.playmotech.ghostcoach.prompt.exception;

import com.playmotech.ghostcoach.prompt.PromptKey;

public class PromptVariableMissingException extends PromptException {

    public PromptVariableMissingException(PromptKey key, String variableName) {
        super("Prompt " + key + " requires variable but value was null: " + variableName);
    }
}
