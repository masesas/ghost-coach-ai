package com.playmotech.ghostcoach.prompt.exception;

public abstract class PromptException extends RuntimeException {

    protected PromptException(String message) {
        super(message);
    }
}
