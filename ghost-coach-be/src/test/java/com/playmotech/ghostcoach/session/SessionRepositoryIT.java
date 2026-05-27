package com.playmotech.ghostcoach.session;

import com.playmotech.ghostcoach.ai.dto.FeedbackReport;
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
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Exercises {@link SessionRepository} against a real PostgreSQL container so that
 * the JSONB-backed columns ({@code strengths}, {@code areas_to_improve}) behave like
 * they do in production. H2 cannot emulate JSONB.
 */
class SessionRepositoryIT extends PostgresIntegrationTest {

    @Autowired SessionRepository sessionRepository;
    @Autowired UserRepository userRepository;

    @Test
    @DisplayName("save + findByIdAndUserId round-trips JSONB columns")
    void saveAndFindByIdAndUserId() {
        User user = persistUser("a@example.com");
        CoachingSession saved = sessionRepository.save(buildSession(user, BigDecimal.valueOf(8.5)));

        Optional<CoachingSession> found =
                sessionRepository.findByIdAndUserId(saved.getId(), user.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getStrengths()).containsExactly("good stance", "balanced grip");
        assertThat(found.get().getAreasToImprove())
                .hasSize(1)
                .first()
                .extracting(FeedbackReport.AreaToImprove::flaw)
                .isEqualTo("hip rotation");
        assertThat(found.get().getConfidenceLevel()).isEqualTo(ConfidenceLevel.HIGH);
    }

    @Test
    @DisplayName("findByIdAndUserId rejects cross-user access (ownership guard)")
    void findByIdRejectsForeignUser() {
        User alice = persistUser("alice@example.com");
        User bob = persistUser("bob@example.com");
        CoachingSession aliceSession = sessionRepository.save(buildSession(alice, BigDecimal.valueOf(7.0)));

        assertThat(sessionRepository.findByIdAndUserId(aliceSession.getId(), bob.getId()))
                .isEmpty();
    }

    @Test
    @DisplayName("findByUserIdOrderByCreatedAtDesc returns newest first and respects paging")
    void paginatedDescOrdering() {
        User user = persistUser("paged@example.com");
        CoachingSession s1 = sessionRepository.save(buildSession(user, BigDecimal.valueOf(1.0)));
        CoachingSession s2 = sessionRepository.save(buildSession(user, BigDecimal.valueOf(2.0)));
        CoachingSession s3 = sessionRepository.save(buildSession(user, BigDecimal.valueOf(3.0)));

        Page<CoachingSession> page0 =
                sessionRepository.findByUserIdOrderByCreatedAtDesc(user.getId(), PageRequest.of(0, 2));

        assertThat(page0.getTotalElements()).isEqualTo(3);
        assertThat(page0.getContent()).hasSize(2);
        // Newest first; the three rows were inserted in order so s3 leads, then s2.
        assertThat(page0.getContent().get(0).getId()).isEqualTo(s3.getId());
        assertThat(page0.getContent().get(1).getId()).isEqualTo(s2.getId());

        Page<CoachingSession> page1 =
                sessionRepository.findByUserIdOrderByCreatedAtDesc(user.getId(), PageRequest.of(1, 2));
        assertThat(page1.getContent()).hasSize(1);
        assertThat(page1.getContent().get(0).getId()).isEqualTo(s1.getId());
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

    private CoachingSession buildSession(User user, BigDecimal score) {
        return CoachingSession.builder()
                .user(user)
                .imagePath(user.getId() + "/" + score + ".jpg")
                .overallScore(score)
                .strengths(List.of("good stance", "balanced grip"))
                .areasToImprove(List.of(new FeedbackReport.AreaToImprove(
                        "hip rotation", "rotate hips more during follow-through")))
                .priorityFix("Rotate hips more")
                .drillSuggestion("Mirror drill 10x")
                .confidenceLevel(ConfidenceLevel.HIGH)
                .rawAiResponse("{\"score\":" + score + "}")
                .build();
    }
}
