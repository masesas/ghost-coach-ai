package com.playmotech.ghostcoach.storage;

public record StoredFile(String relativePath, String contentType, long size) {}
