package com.playmotech.ghostcoach.chat;

import com.playmotech.ghostcoach.ai.GeminiClient;
import com.playmotech.ghostcoach.ai.dto.GeminiRequest;
import com.playmotech.ghostcoach.common.exception.ApiException;
import com.playmotech.ghostcoach.prompt.ModelConfig;
import com.playmotech.ghostcoach.prompt.Prompt;
import com.playmotech.ghostcoach.prompt.PromptKey;
import com.playmotech.ghostcoach.prompt.PromptResponseFormat;
import com.playmotech.ghostcoach.prompt.PromptService;
import com.playmotech.ghostcoach.session.CoachingSession;
import com.playmotech.ghostcoach.session.SessionService;
import com.playmotech.ghostcoach.support.TestData;
import com.playmotech.ghostcoach.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ChatServiceTest {

    @Mock ChatRepository chatRepository;
    @Mock SessionService sessionService;
    @Mock ChatPersister chatPersister;
    @Mock GeminiClient geminiClient;
    @Mock PromptService promptService;

    @InjectMocks ChatService chatService;

    private User user;
    private CoachingSession session;
    private ChatMessage userMsg;
    private Prompt chatPrompt;
    private ModelConfig chatModelConfig;

    @BeforeEach
    void setUp() {
        user = TestData.user();
        session = TestData.session(user);
        userMsg = TestData.chatMessage(session, ChatRole.USER, "How to improve?");
        chatModelConfig = new ModelConfig("gemini-2.0-flash", 0.5, 512);
        chatPrompt = Prompt.builder()
                .promptKey(PromptKey.CHAT_COACHING)
                .description("d")
                .template("Coach {{fullName}} sport={{sport}}/{{sportLower}} pos={{position}} lvl={{experienceLevel}}"
                        + " ctx={{sessionContext}} history={{chatHistory}} msg={{userMessage}}")
                .variables(List.of("fullName", "sport", "sportLower", "position", "experienceLevel",
                        "sessionContext", "chatHistory", "userMessage"))
                .modelConfig(chatModelConfig)
                .responseFormat(PromptResponseFormat.TEXT)
                .build();
    }

    @Test
    @DisplayName("sendMessage: user msg saved BEFORE Gemini call, assistant msg AFTER")
    void sendMessageCallOrder() {
        ChatPersister.UserMessagePersisted persisted =
                new ChatPersister.UserMessagePersisted(userMsg, session, user);
        ChatMessage assistantMsg = TestData.chatMessage(session, ChatRole.ASSISTANT, "AI reply text");

        when(chatPersister.saveUserMessage(1L, 100L, "How to improve?")).thenReturn(persisted);
        when(chatRepository.findBySessionIdAndIdLessThanOrderByIdDesc(anyLong(), anyLong(), any(Pageable.class)))
                .thenReturn(List.of());
        when(promptService.get(PromptKey.CHAT_COACHING)).thenReturn(chatPrompt);
        when(geminiClient.generate(any(GeminiRequest.class), eq(chatModelConfig), eq(PromptResponseFormat.TEXT)))
                .thenReturn("AI reply text");
        when(chatPersister.saveAssistantMessage(100L, "AI reply text")).thenReturn(assistantMsg);

        List<ChatMessage> result = chatService.sendMessage(1L, 100L, "How to improve?");

        assertThat(result).containsExactly(userMsg, assistantMsg);

        // CRITICAL ordering: user msg persisted BEFORE Gemini, assistant AFTER Gemini.
        InOrder ordered = inOrder(chatPersister, geminiClient);
        ordered.verify(chatPersister).saveUserMessage(1L, 100L, "How to improve?");
        ordered.verify(geminiClient).generate(any(GeminiRequest.class), eq(chatModelConfig), eq(PromptResponseFormat.TEXT));
        ordered.verify(chatPersister).saveAssistantMessage(100L, "AI reply text");

        ArgumentCaptor<GeminiRequest> requestCaptor = ArgumentCaptor.forClass(GeminiRequest.class);
        verify(geminiClient).generate(requestCaptor.capture(), eq(chatModelConfig), eq(PromptResponseFormat.TEXT));
        String promptText = requestCaptor.getValue().contents().get(0).parts().get(0).text();
        assertThat(promptText)
                .contains("Test User")
                .contains("Rotate hips more")
                .contains("How to improve?")
                .contains("FOOTBALL")
                .contains(ChatService.EMPTY_HISTORY_MARKER);
    }

    @Test
    @DisplayName("sendMessage: Gemini fails → user msg STAYS in DB, no assistant msg saved")
    void sendMessageGeminiFailsUserMsgPersisted() {
        ChatPersister.UserMessagePersisted persisted =
                new ChatPersister.UserMessagePersisted(userMsg, session, user);
        when(chatPersister.saveUserMessage(1L, 100L, "How to improve?")).thenReturn(persisted);
        when(chatRepository.findBySessionIdAndIdLessThanOrderByIdDesc(anyLong(), anyLong(), any(Pageable.class)))
                .thenReturn(List.of());
        when(promptService.get(PromptKey.CHAT_COACHING)).thenReturn(chatPrompt);
        when(geminiClient.generate(any(GeminiRequest.class), any(), any()))
                .thenThrow(ApiException.badGateway("AI_ERROR", "Gemini down"));

        assertThatThrownBy(() -> chatService.sendMessage(1L, 100L, "How to improve?"))
                .isInstanceOfSatisfying(ApiException.class, ex -> {
                    assertThat(ex.getStatus()).isEqualTo(HttpStatus.BAD_GATEWAY);
                    assertThat(ex.getCode()).isEqualTo("AI_ERROR");
                });

        // User message was already committed by saveUserMessage; we must NOT save assistant.
        verify(chatPersister).saveUserMessage(1L, 100L, "How to improve?");
        verify(chatPersister, never()).saveAssistantMessage(eq(100L), any());
    }

    @Test
    @DisplayName("sendMessage: session not found → propagate, no AI call, no assistant save")
    void sendMessageSessionNotFound() {
        when(chatPersister.saveUserMessage(1L, 999L, "msg"))
                .thenThrow(ApiException.notFound("SESSION_NOT_FOUND", "Session not found"));

        assertThatThrownBy(() -> chatService.sendMessage(1L, 999L, "msg"))
                .isInstanceOfSatisfying(ApiException.class, ex -> {
                    assertThat(ex.getCode()).isEqualTo("SESSION_NOT_FOUND");
                });

        verify(geminiClient, never()).generate(any(), any(), any());
        verify(chatPersister, never()).saveAssistantMessage(any(), any());
    }

    @Test
    @DisplayName("history paginated: ownership check + delegate to repo with Pageable")
    void historyDelegates() {
        ChatMessage m1 = TestData.chatMessage(session, ChatRole.USER, "hi");
        ChatMessage m2 = TestData.chatMessage(session, ChatRole.ASSISTANT, "hello");
        Pageable pageable = PageRequest.of(0, 50);
        org.springframework.data.domain.Page<ChatMessage> page =
                new org.springframework.data.domain.PageImpl<>(List.of(m1, m2));
        when(sessionService.getOwned(100L, 1L)).thenReturn(session);
        when(chatRepository.findBySessionIdOrderByCreatedAtAsc(100L, pageable)).thenReturn(page);

        org.springframework.data.domain.Page<ChatMessage> result =
                chatService.history(1L, 100L, pageable);

        assertThat(result.getContent()).containsExactly(m1, m2);
        verify(sessionService).getOwned(100L, 1L);
    }

    // ===== New tests for sliding-window memory =====

    @Test
    @DisplayName("sendMessage: history with 5 prior messages — chronological in prompt")
    void sendMessage_with5History_includesAllChronological() {
        ChatPersister.UserMessagePersisted persisted =
                new ChatPersister.UserMessagePersisted(userMsg, session, user);

        // Repo returns DESC (newest first). ChatService should reverse to chronological.
        List<ChatMessage> desc = List.of(
                TestData.chatMessage(session, ChatRole.ASSISTANT, "reply-3"),
                TestData.chatMessage(session, ChatRole.USER, "question-3"),
                TestData.chatMessage(session, ChatRole.ASSISTANT, "reply-1"),
                TestData.chatMessage(session, ChatRole.USER, "question-2"),
                TestData.chatMessage(session, ChatRole.USER, "question-1")
        );

        when(chatPersister.saveUserMessage(1L, 100L, "How to improve?")).thenReturn(persisted);
        when(chatRepository.findBySessionIdAndIdLessThanOrderByIdDesc(anyLong(), anyLong(), any(Pageable.class)))
                .thenReturn(desc);
        when(promptService.get(PromptKey.CHAT_COACHING)).thenReturn(chatPrompt);
        when(geminiClient.generate(any(GeminiRequest.class), any(), any())).thenReturn("ok");
        when(chatPersister.saveAssistantMessage(eq(100L), any()))
                .thenReturn(TestData.chatMessage(session, ChatRole.ASSISTANT, "ok"));

        chatService.sendMessage(1L, 100L, "How to improve?");

        ArgumentCaptor<GeminiRequest> requestCaptor = ArgumentCaptor.forClass(GeminiRequest.class);
        verify(geminiClient).generate(requestCaptor.capture(), any(), any());
        String promptText = requestCaptor.getValue().contents().get(0).parts().get(0).text();

        // Verify chronological order (reversed from DESC input).
        int idx1 = promptText.indexOf("question-1");
        int idx2 = promptText.indexOf("question-2");
        int idx3a = promptText.indexOf("reply-1");
        int idx3b = promptText.indexOf("question-3");
        int idx3c = promptText.indexOf("reply-3");
        assertThat(idx1).isLessThan(idx2);
        assertThat(idx2).isLessThan(idx3a);
        assertThat(idx3a).isLessThan(idx3b);
        assertThat(idx3b).isLessThan(idx3c);

        // Verify speaker prefixes.
        assertThat(promptText).contains("User: question-1");
        assertThat(promptText).contains("Coach: reply-1");
    }

    @Test
    @DisplayName("sendMessage: respects MAX_HISTORY_MESSAGES limit via Pageable")
    void sendMessage_callsRepoWithCappedPageable() {
        ChatPersister.UserMessagePersisted persisted =
                new ChatPersister.UserMessagePersisted(userMsg, session, user);
        when(chatPersister.saveUserMessage(1L, 100L, "How to improve?")).thenReturn(persisted);
        when(chatRepository.findBySessionIdAndIdLessThanOrderByIdDesc(anyLong(), anyLong(), any(Pageable.class)))
                .thenReturn(List.of());
        when(promptService.get(PromptKey.CHAT_COACHING)).thenReturn(chatPrompt);
        when(geminiClient.generate(any(GeminiRequest.class), any(), any())).thenReturn("ok");
        when(chatPersister.saveAssistantMessage(eq(100L), any()))
                .thenReturn(TestData.chatMessage(session, ChatRole.ASSISTANT, "ok"));

        chatService.sendMessage(1L, 100L, "How to improve?");

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(chatRepository).findBySessionIdAndIdLessThanOrderByIdDesc(
                eq(100L), eq(userMsg.getId()), pageableCaptor.capture());
        Pageable used = pageableCaptor.getValue();
        assertThat(used.getPageNumber()).isZero();
        assertThat(used.getPageSize()).isEqualTo(ChatService.MAX_HISTORY_MESSAGES);
    }

    @Test
    @DisplayName("sendMessage: no prior history → EMPTY_HISTORY_MARKER injected")
    void sendMessage_noHistory_usesEmptyMarker() {
        ChatPersister.UserMessagePersisted persisted =
                new ChatPersister.UserMessagePersisted(userMsg, session, user);
        when(chatPersister.saveUserMessage(1L, 100L, "How to improve?")).thenReturn(persisted);
        when(chatRepository.findBySessionIdAndIdLessThanOrderByIdDesc(anyLong(), anyLong(), any(Pageable.class)))
                .thenReturn(List.of());
        when(promptService.get(PromptKey.CHAT_COACHING)).thenReturn(chatPrompt);
        when(geminiClient.generate(any(GeminiRequest.class), any(), any())).thenReturn("ok");
        when(chatPersister.saveAssistantMessage(eq(100L), any()))
                .thenReturn(TestData.chatMessage(session, ChatRole.ASSISTANT, "ok"));

        chatService.sendMessage(1L, 100L, "How to improve?");

        ArgumentCaptor<GeminiRequest> requestCaptor = ArgumentCaptor.forClass(GeminiRequest.class);
        verify(geminiClient).generate(requestCaptor.capture(), any(), any());
        String promptText = requestCaptor.getValue().contents().get(0).parts().get(0).text();
        assertThat(promptText).contains(ChatService.EMPTY_HISTORY_MARKER);
    }

    @Test
    @DisplayName("formatChatHistory: chronological order, User/Coach prefixes, no trailing newline")
    void formatChatHistory_chronologicalOrder() {
        List<ChatMessage> chrono = List.of(
                TestData.chatMessage(session, ChatRole.USER, "A"),
                TestData.chatMessage(session, ChatRole.ASSISTANT, "B"),
                TestData.chatMessage(session, ChatRole.USER, "C")
        );

        String formatted = ChatService.formatChatHistory(chrono);

        assertThat(formatted).isEqualTo("User: A\nCoach: B\nUser: C");
    }

    @Test
    @DisplayName("formatChatHistory: empty list returns EMPTY_HISTORY_MARKER")
    void formatChatHistory_emptyList() {
        assertThat(ChatService.formatChatHistory(List.of()))
                .isEqualTo(ChatService.EMPTY_HISTORY_MARKER);
    }

    @Test
    @DisplayName("sendMessage: DB failure during history load → propagate, no Gemini call, no assistant save")
    void sendMessage_dbFailureDuringHistoryLoad_propagates() {
        ChatPersister.UserMessagePersisted persisted =
                new ChatPersister.UserMessagePersisted(userMsg, session, user);
        when(chatPersister.saveUserMessage(1L, 100L, "How to improve?")).thenReturn(persisted);
        when(chatRepository.findBySessionIdAndIdLessThanOrderByIdDesc(anyLong(), anyLong(), any(Pageable.class)))
                .thenThrow(new DataAccessResourceFailureException("DB down"));

        assertThatThrownBy(() -> chatService.sendMessage(1L, 100L, "How to improve?"))
                .isInstanceOf(DataAccessResourceFailureException.class);

        verify(chatPersister).saveUserMessage(1L, 100L, "How to improve?");
        verify(geminiClient, never()).generate(any(), any(), any());
        verify(chatPersister, never()).saveAssistantMessage(any(), any());
    }

    @Test
    @DisplayName("sendMessage: excludes the just-saved user msg id when querying history")
    void sendMessage_excludesJustSavedUserMessageId() {
        ChatPersister.UserMessagePersisted persisted =
                new ChatPersister.UserMessagePersisted(userMsg, session, user);
        when(chatPersister.saveUserMessage(1L, 100L, "How to improve?")).thenReturn(persisted);
        when(chatRepository.findBySessionIdAndIdLessThanOrderByIdDesc(anyLong(), anyLong(), any(Pageable.class)))
                .thenReturn(new ArrayList<>());
        when(promptService.get(PromptKey.CHAT_COACHING)).thenReturn(chatPrompt);
        when(geminiClient.generate(any(GeminiRequest.class), any(), any())).thenReturn("ok");
        when(chatPersister.saveAssistantMessage(eq(100L), any()))
                .thenReturn(TestData.chatMessage(session, ChatRole.ASSISTANT, "ok"));

        chatService.sendMessage(1L, 100L, "How to improve?");

        // The excludeId arg MUST equal the id of the user msg just persisted.
        verify(chatRepository).findBySessionIdAndIdLessThanOrderByIdDesc(
                eq(100L), eq(userMsg.getId()), any(Pageable.class));
    }
}
