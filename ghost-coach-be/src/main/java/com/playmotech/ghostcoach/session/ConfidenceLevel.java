package com.playmotech.ghostcoach.session;

public enum ConfidenceLevel {
    LOW, MEDIUM, HIGH;

    public static ConfidenceLevel fromStringOrLow(String value) {
        if (value == null) return LOW;
        try {
            return ConfidenceLevel.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return LOW;
        }
    }
}
