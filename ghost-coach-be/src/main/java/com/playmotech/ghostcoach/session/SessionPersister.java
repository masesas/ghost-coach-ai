package com.playmotech.ghostcoach.session;

import com.playmotech.ghostcoach.ai.dto.FeedbackReport;
import com.playmotech.ghostcoach.storage.StorageService;
import com.playmotech.ghostcoach.storage.StoredFile;
import com.playmotech.ghostcoach.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

/**
 * Persists a coaching session AFTER the external AI call has succeeded.
 *
 * Why: Spring's @Transactional only activates when invoked across bean boundaries.
 * Keeping persist logic in a separate component ensures the transaction wraps only
 * the storage write + DB save, never the long-running Gemini RPC.
 */
@Component
@RequiredArgsConstructor
public class SessionPersister {

    private final SessionRepository sessionRepository;
    private final StorageService storageService;

    @Transactional
    public CoachingSession persist(User user,
                                   MultipartFile image,
                                   FeedbackReport report,
                                   String rawResponse) {
        StoredFile stored = storageService.store(image, user.getId());

        CoachingSession session = CoachingSession.builder()
                .user(user)
                .imagePath(stored.relativePath())
                .overallScore(report.overallScore())
                .strengths(report.strengths())
                .areasToImprove(report.areasToImprove())
                .priorityFix(report.priorityFix())
                .drillSuggestion(report.drillSuggestion())
                .confidenceLevel(ConfidenceLevel.fromStringOrLow(report.confidenceLevel()))
                .rawAiResponse(rawResponse)
                .build();

        return sessionRepository.save(session);
    }
}
