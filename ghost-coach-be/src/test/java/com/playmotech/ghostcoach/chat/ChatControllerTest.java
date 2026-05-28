package com.playmotech.ghostcoach.chat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.playmotech.ghostcoach.chat.dto.ChatRequest;
import com.playmotech.ghostcoach.common.exception.GlobalExceptionHandler;
import com.playmotech.ghostcoach.session.CoachingSession;
import com.playmotech.ghostcoach.support.AuthenticationPrincipalSupport;
import com.playmotech.ghostcoach.support.TestConfig;
import com.playmotech.ghostcoach.support.TestData;
import com.playmotech.ghostcoach.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class ChatControllerTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Mock ChatService chatService;

    MockMvc mvc;
    User user;
    CoachingSession session;

    @BeforeEach
    void setUp() {
        user = TestData.user();
        session = TestData.session(user);
        mvc = MockMvcBuilders.standaloneSetup(new ChatController(chatService))
                .setControllerAdvice(new GlobalExceptionHandler(TestConfig.defaultAppConfigProp()))
                .setCustomArgumentResolvers(
                        AuthenticationPrincipalSupport.withPrincipal(user.getId(), user.getEmail()))
                .build();
    }

    @Test
    @DisplayName("POST /sessions/{id}/chat valid → 201 with two messages")
    void sendMessageValid() throws Exception {
        ChatRequest req = new ChatRequest("How to improve?");
        ChatMessage userMsg = TestData.chatMessage(session, ChatRole.USER, "How to improve?");
        ChatMessage assistantMsg = TestData.chatMessage(session, ChatRole.ASSISTANT, "Practice drills.");
        when(chatService.sendMessage(eq(1L), eq(100L), anyString()))
                .thenReturn(List.of(userMsg, assistantMsg));

        mvc.perform(post("/api/v1/sessions/100/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(MAPPER.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].role").value("USER"))
                .andExpect(jsonPath("$.data[1].role").value("ASSISTANT"));
    }

    @Test
    @DisplayName("POST /sessions/{id}/chat blank message → 400 VALIDATION_FAILED")
    void sendMessageBlank() throws Exception {
        mvc.perform(post("/api/v1/sessions/100/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"message\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("VALIDATION_FAILED"));
    }

    @Test
    @DisplayName("POST /sessions/{id}/chat message > 1000 chars → 400 VALIDATION_FAILED")
    void sendMessageTooLong() throws Exception {
        String tooLong = "x".repeat(1001);
        ChatRequest req = new ChatRequest(tooLong);

        mvc.perform(post("/api/v1/sessions/100/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(MAPPER.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("VALIDATION_FAILED"));
    }

    @Test
    @DisplayName("GET /sessions/{id}/chat → 200 with paginated PageResponse")
    void chatHistory() throws Exception {
        ChatMessage m1 = TestData.chatMessage(session, ChatRole.USER, "hi");
        ChatMessage m2 = TestData.chatMessage(session, ChatRole.ASSISTANT, "hello");
        org.springframework.data.domain.Page<ChatMessage> page =
                new org.springframework.data.domain.PageImpl<>(List.of(m1, m2),
                        org.springframework.data.domain.PageRequest.of(0, 50), 2);
        when(chatService.history(org.mockito.ArgumentMatchers.eq(1L),
                org.mockito.ArgumentMatchers.eq(100L),
                org.mockito.ArgumentMatchers.any()))
                .thenReturn(page);

        mvc.perform(get("/api/v1/sessions/100/chat"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content.length()").value(2))
                .andExpect(jsonPath("$.data.totalElements").value(2));
    }
}
