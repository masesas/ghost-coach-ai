package com.playmotech.ghostcoach.chat;

import com.playmotech.ghostcoach.ai.GeminiClient;
import com.playmotech.ghostcoach.ai.dto.GeminiRequest;
import com.playmotech.ghostcoach.prompt.Prompt;
import com.playmotech.ghostcoach.prompt.PromptKey;
import com.playmotech.ghostcoach.prompt.PromptService;
import com.playmotech.ghostcoach.prompt.PromptTemplateRenderer;
import com.playmotech.ghostcoach.session.CoachingSession;
import com.playmotech.ghostcoach.session.SessionService;
import com.playmotech.ghostcoach.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ChatService {

    static final int MAX_HISTORY_MESSAGES = 20;
    static final String EMPTY_HISTORY_MARKER = "(no previous messages in this session)";

    private final ChatRepository chatRepository;
    private final SessionService sessionService;
    private final ChatPersister chatPersister;
    private final GeminiClient geminiClient;
    private final PromptService promptService;

    public List<ChatMessage> sendMessage(Long userId, Long sessionId, String userMessage) {
        // Tx 1: ownership check + user message persisted BEFORE the AI call.
        ChatPersister.UserMessagePersisted persisted =
                chatPersister.saveUserMessage(userId, sessionId, userMessage);

        // Load sliding-window chat history excluding the user msg just saved.
        // Propagates DB errors per design: AI memory loss should fail the chat, not silently degrade.
        String chatHistory = loadAndFormatHistory(sessionId, persisted.userMsg().getId());

        // No transaction held while we wait on Gemini.
        Prompt prompt = promptService.get(PromptKey.CHAT_COACHING);
        String rendered = PromptTemplateRenderer.render(prompt, buildVars(
                persisted.user(),
                buildContext(persisted.session()),
                chatHistory,
                userMessage));
        String aiReply = geminiClient.generate(
                GeminiRequest.textOnly(rendered),
                prompt.getModelConfig(),
                prompt.getResponseFormat()
        );

        // Tx 2: assistant reply persisted only after AI returns successfully.
        ChatMessage assistantMsg = chatPersister.saveAssistantMessage(sessionId, aiReply);

        return List.of(persisted.userMsg(), assistantMsg);
    }

    @Transactional(readOnly = true)
    public Page<ChatMessage> history(Long userId, Long sessionId, Pageable pageable) {
        sessionService.getOwned(sessionId, userId); // ownership check
        return chatRepository.findBySessionIdOrderByCreatedAtAsc(sessionId, pageable);
    }

    private String loadAndFormatHistory(Long sessionId, Long excludeId) {
        List<ChatMessage> desc = chatRepository.findBySessionIdAndIdLessThanOrderByIdDesc(
                sessionId, excludeId, PageRequest.of(0, MAX_HISTORY_MESSAGES));
        // List.reversed() (Java 21+) returns a chronological view without copying.
        return formatChatHistory(desc.reversed());
    }

    static String formatChatHistory(List<ChatMessage> chronological) {
        if (chronological.isEmpty()) {
            return EMPTY_HISTORY_MARKER;
        }
        StringBuilder sb = new StringBuilder();
        for (ChatMessage m : chronological) {
            String speaker = m.getRole() == ChatRole.USER ? "User" : "Coach";
            sb.append(speaker).append(": ").append(m.getContent()).append('\n');
        }
        return sb.toString().stripTrailing();
    }

    // WARNING: Map.of supports max 10 entries; current 8.
    // Do NOT add more without migrating to Map.ofEntries(...).
    private static Map<String, Object> buildVars(
            User user, String sessionContext, String chatHistory, String userMessage) {
        return Map.of(
                "sport", user.getSport().name(),
                "sportLower", user.getSport().name().toLowerCase(),
                "fullName", user.getFullName(),
                "position", user.getPosition(),
                "experienceLevel", user.getExperienceLevel().name(),
                "sessionContext", sessionContext,
                "chatHistory", chatHistory,
                "userMessage", userMessage
        );
    }

    private String buildContext(CoachingSession s) {
        return """
                - Overall Score: %s/10
                - Priority Fix: %s
                - Drill Suggestion: %s
                - Confidence: %s
                """.formatted(
                s.getOverallScore() != null ? s.getOverallScore() : "N/A",
                s.getPriorityFix() != null ? s.getPriorityFix() : "N/A",
                s.getDrillSuggestion() != null ? s.getDrillSuggestion() : "N/A",
                s.getConfidenceLevel() != null ? s.getConfidenceLevel() : "N/A"
        );
    }
}
