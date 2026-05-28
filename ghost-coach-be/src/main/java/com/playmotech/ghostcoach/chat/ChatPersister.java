package com.playmotech.ghostcoach.chat;

import com.playmotech.ghostcoach.common.exception.ApiException;
import com.playmotech.ghostcoach.session.CoachingSession;
import com.playmotech.ghostcoach.session.SessionRepository;
import com.playmotech.ghostcoach.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Owns the two short transactions that bracket the Gemini chat call.
 *
 * Why: Splitting persistence into its own component is the only reliable way to
 * make {@link Transactional} fire when invoked across bean boundaries. The user
 * message is committed BEFORE the long-running AI call so a Gemini failure
 * never loses the user's input.
 */
@Component
@RequiredArgsConstructor
public class ChatPersister {

    private final ChatRepository chatRepository;
    private final SessionRepository sessionRepository;

    /**
     * Transaction 1: verify ownership, eagerly load {@link User}, persist user message,
     * and return everything the caller needs to build the AI prompt without ever
     * touching a lazy proxy again.
     */
    @Transactional
    public UserMessagePersisted saveUserMessage(Long userId, Long sessionId, String content) {
        CoachingSession session = sessionRepository.findByIdAndUserId(sessionId, userId)
                .orElseThrow(() -> ApiException.notFound("SESSION_NOT_FOUND", "Session not found"));

        // Force initialization while session is attached so the caller can use it outside the tx.
        User user = session.getUser();
        user.getFullName();
        user.getSport();

        ChatMessage userMsg = chatRepository.save(ChatMessage.builder()
                .session(session)
                .role(ChatRole.USER)
                .content(content)
                .build());

        return new UserMessagePersisted(userMsg, session, user);
    }

    /**
     * Transaction 2: persist assistant reply. Runs after the AI call returned successfully.
     * Re-fetches the session reference inside the new transaction to avoid using a detached
     * entity across transaction boundaries.
     */
    @Transactional
    public ChatMessage saveAssistantMessage(Long sessionId, String content) {
        CoachingSession sessionRef = sessionRepository.getReferenceById(sessionId);
        return chatRepository.save(ChatMessage.builder()
                .session(sessionRef)
                .role(ChatRole.ASSISTANT)
                .content(content)
                .build());
    }

    public record UserMessagePersisted(ChatMessage userMsg, CoachingSession session, User user) {}
}
