package com.playmotech.ghostcoach.prompt.exception;

import com.playmotech.ghostcoach.prompt.PromptKey;

public class PromptNotFoundException extends PromptException {

    public PromptNotFoundException(PromptKey key) {
        super("Prompt not found in database: " + key);
    }
}
