package com.playmotech.ghostcoach.prompt.exception;

import com.playmotech.ghostcoach.prompt.PromptKey;

public class PromptVariableUnknownException extends PromptException {

    public PromptVariableUnknownException(PromptKey key, String variableName) {
        super("Caller passed unknown variable to prompt " + key + ": " + variableName);
    }
}
