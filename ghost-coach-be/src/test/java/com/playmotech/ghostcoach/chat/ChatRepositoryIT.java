package com.playmotech.ghostcoach.chat;

import com.playmotech.ghostcoach.session.CoachingSession;
import com.playmotech.ghostcoach.session.ConfidenceLevel;
import com.playmotech.ghostcoach.session.SessionRepository;
import com.playmotech.ghostcoach.support.PostgresIntegrationTest;
import com.playmotech.ghostcoach.user.ExperienceLevel;
import com.playmotech.ghostcoach.user.Sport;
import com.playmotech.ghostcoach.user.User;
import com.playmotech.ghostcoach.user.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ChatRepositoryIT extends PostgresIntegrationTest {

    @Autowired ChatRepository chatRepository;
    @Autowired SessionRepository sessionRepository;
    @Autowired UserRepository userRepository;

    @Test
    @DisplayName("findBySessionIdOrderByCreatedAtAsc returns oldest-first within a paged window")
    void paginatedAscOrdering() {
        User user = persistUser("chat@example.com");
        CoachingSession session = persistSession(user);

        ChatMessage m1 = chatRepository.save(message(session, ChatRole.USER, "hi"));
        ChatMessage m2 = chatRepository.save(message(session, ChatRole.ASSISTANT, "hello!"));
        ChatMessage m3 = chatRepository.save(message(session, ChatRole.USER, "how do I improve?"));

        Page<ChatMessage> page0 =
                chatRepository.findBySessionIdOrderByCreatedAtAsc(session.getId(), PageRequest.of(0, 2));

        assertThat(page0.getTotalElements()).isEqualTo(3);
        assertThat(page0.getContent()).hasSize(2);
        assertThat(page0.getContent().get(0).getId()).isEqualTo(m1.getId());
        assertThat(page0.getContent().get(1).getId()).isEqualTo(m2.getId());

        Page<ChatMessage> page1 =
                chatRepository.findBySessionIdOrderByCreatedAtAsc(session.getId(), PageRequest.of(1, 2));
        assertThat(page1.getContent()).hasSize(1);
        assertThat(page1.getContent().get(0).getId()).isEqualTo(m3.getId());
    }

    @Test
    @DisplayName("messages are scoped per session — querying another session id returns empty")
    void scopedPerSession() {
        User user = persistUser("scoped@example.com");
        CoachingSession a = persistSession(user);
        CoachingSession b = persistSession(user);
        chatRepository.save(message(a, ChatRole.USER, "in A"));

        Page<ChatMessage> bPage =
                chatRepository.findBySessionIdOrderByCreatedAtAsc(b.getId(), PageRequest.of(0, 10));

        assertThat(bPage.getTotalElements()).isZero();
        assertThat(bPage.getContent()).isEmpty();
    }

    @Test
    @DisplayName("findBySessionIdAndIdLessThanOrderByIdDesc respects limit and excludes the cutoff id")
    void findRecentBeforeId_respectsLimitAndExcludes() {
        User user = persistUser("memory@example.com");
        CoachingSession session = persistSession(user);

        List<ChatMessage> all = new ArrayList<>();
        for (int i = 1; i <= 25; i++) {
            ChatRole role = (i % 2 == 0) ? ChatRole.ASSISTANT : ChatRole.USER;
            all.add(chatRepository.save(message(session, role, "msg-" + i)));
        }
        Long latestId = all.get(24).getId(); // 25th message — the cutoff (exclusive)

        List<ChatMessage> result = chatRepository.findBySessionIdAndIdLessThanOrderByIdDesc(
                session.getId(), latestId, PageRequest.of(0, 20));

        assertThat(result).hasSize(20);
        // Returned DESC: first element should be the 24th persisted (just before cutoff).
        assertThat(result.get(0).getId()).isEqualTo(all.get(23).getId());
        // Last element of the 20-item window: the 5th persisted.
        assertThat(result.get(19).getId()).isEqualTo(all.get(4).getId());
        // Cutoff id itself must not appear.
        assertThat(result).noneMatch(m -> m.getId().equals(latestId));
    }

    @Test
    @DisplayName("findBySessionIdAndIdLessThanOrderByIdDesc is scoped per session")
    void findRecentBeforeId_scopedPerSession() {
        User user = persistUser("scope-mem@example.com");
        CoachingSession a = persistSession(user);
        CoachingSession b = persistSession(user);

        ChatMessage aMsg = chatRepository.save(message(a, ChatRole.USER, "session-a-msg"));
        chatRepository.save(message(b, ChatRole.USER, "session-b-msg"));

        // Use a very large cutoff to include everything below; verify session a returns only its msg.
        List<ChatMessage> result = chatRepository.findBySessionIdAndIdLessThanOrderByIdDesc(
                a.getId(), Long.MAX_VALUE, PageRequest.of(0, 20));

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(aMsg.getId());
    }

    private User persistUser(String email) {
        return userRepository.save(User.builder()
                .email(email)
                .passwordHash("$2a$10$dummyhashfortest")
                .fullName("Test User")
                .sport(Sport.FOOTBALL)
                .position("MIDFIELDER")
                .experienceLevel(ExperienceLevel.INTERMEDIATE)
                .build());
    }

    private CoachingSession persistSession(User user) {
        return sessionRepository.save(CoachingSession.builder()
                .user(user)
                .imagePath(user.getId() + "/" + System.nanoTime() + ".jpg")
                .overallScore(BigDecimal.valueOf(7.0))
                .strengths(List.of("a"))
                .areasToImprove(List.of())
                .priorityFix("fix")
                .drillSuggestion("drill")
                .confidenceLevel(ConfidenceLevel.MEDIUM)
                .rawAiResponse("{}")
                .build());
    }

    private ChatMessage message(CoachingSession session, ChatRole role, String content) {
        return ChatMessage.builder()
                .session(session)
                .role(role)
                .content(content)
                .build();
    }
}
