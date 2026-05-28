package com.playmotech.ghostcoach.chat.dto;

import com.playmotech.ghostcoach.chat.ChatMessage;
import com.playmotech.ghostcoach.chat.ChatRole;

import java.time.Instant;

public record ChatMessageResponse(
        Long id,
        ChatRole role,
        String content,
        Instant createdAt
) {
    public static ChatMessageResponse from(ChatMessage m) {
        return new ChatMessageResponse(m.getId(), m.getRole(), m.getContent(), m.getCreatedAt());
    }
}
