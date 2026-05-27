package com.playmotech.ghostcoach.chat;

import com.playmotech.ghostcoach.common.exception.ApiException;
import com.playmotech.ghostcoach.session.CoachingSession;
import com.playmotech.ghostcoach.session.SessionRepository;
import com.playmotech.ghostcoach.support.TestData;
import com.playmotech.ghostcoach.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ChatPersisterTest {

    @Mock ChatRepository chatRepository;
    @Mock SessionRepository sessionRepository;

    @InjectMocks ChatPersister persister;

    private User user;
    private CoachingSession session;

    @BeforeEach
    void setUp() {
        user = TestData.user();
        session = TestData.session(user);
    }

    @Test
    @DisplayName("saveUserMessage: ownership ok → returns persisted msg + session + user")
    void saveUserMessageOk() {
        when(sessionRepository.findByIdAndUserId(100L, 1L)).thenReturn(Optional.of(session));
        when(chatRepository.save(org.mockito.ArgumentMatchers.any(ChatMessage.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        ChatPersister.UserMessagePersisted result = persister.saveUserMessage(1L, 100L, "hi");

        assertThat(result.session()).isEqualTo(session);
        assertThat(result.user()).isEqualTo(user);
        assertThat(result.userMsg().getRole()).isEqualTo(ChatRole.USER);
        assertThat(result.userMsg().getContent()).isEqualTo("hi");

        ArgumentCaptor<ChatMessage> captor = ArgumentCaptor.forClass(ChatMessage.class);
        verify(chatRepository).save(captor.capture());
        assertThat(captor.getValue().getSession()).isEqualTo(session);
    }

    @Test
    @DisplayName("saveUserMessage: session not owned → ApiException 404 SESSION_NOT_FOUND")
    void saveUserMessageOwnershipFail() {
        when(sessionRepository.findByIdAndUserId(999L, 1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> persister.saveUserMessage(1L, 999L, "hi"))
                .isInstanceOfSatisfying(ApiException.class, ex -> {
                    assertThat(ex.getStatus()).isEqualTo(HttpStatus.NOT_FOUND);
                    assertThat(ex.getCode()).isEqualTo("SESSION_NOT_FOUND");
                });

        verify(chatRepository, org.mockito.Mockito.never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    @DisplayName("saveAssistantMessage: persists assistant role pointing to session reference")
    void saveAssistantMessage() {
        when(sessionRepository.getReferenceById(100L)).thenReturn(session);
        when(chatRepository.save(org.mockito.ArgumentMatchers.any(ChatMessage.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        ChatMessage saved = persister.saveAssistantMessage(100L, "ai reply");

        assertThat(saved.getRole()).isEqualTo(ChatRole.ASSISTANT);
        assertThat(saved.getContent()).isEqualTo("ai reply");
        assertThat(saved.getSession()).isEqualTo(session);
    }
}
