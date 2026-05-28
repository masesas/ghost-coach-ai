package com.playmotech.ghostcoach.session;

import com.playmotech.ghostcoach.ai.dto.FeedbackReport;
import com.playmotech.ghostcoach.storage.StorageService;
import com.playmotech.ghostcoach.storage.StoredFile;
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
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SessionPersisterTest {

    @Mock SessionRepository sessionRepository;
    @Mock StorageService storageService;

    @InjectMocks SessionPersister persister;

    private User user;
    private MultipartFile image;

    @BeforeEach
    void setUp() {
        user = TestData.user();
        image = new MockMultipartFile("image", "stance.jpg", "image/jpeg", new byte[]{1, 2, 3});
    }

    @Test
    @DisplayName("persist writes file FIRST, then saves session row pointing to it")
    void persistOrderingFileBeforeRow() {
        FeedbackReport report = TestData.feedbackReport();
        when(storageService.store(image, 1L)).thenReturn(new StoredFile("1/x.jpg", "image/jpeg", 3L));
        when(sessionRepository.save(org.mockito.ArgumentMatchers.any(CoachingSession.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        CoachingSession result = persister.persist(user, image, report, "raw-ai-response");

        InOrder ordered = inOrder(storageService, sessionRepository);
        ordered.verify(storageService).store(image, 1L);
        ordered.verify(sessionRepository).save(org.mockito.ArgumentMatchers.any(CoachingSession.class));

        ArgumentCaptor<CoachingSession> captor = ArgumentCaptor.forClass(CoachingSession.class);
        org.mockito.Mockito.verify(sessionRepository).save(captor.capture());
        CoachingSession saved = captor.getValue();

        assertThat(saved.getUser()).isEqualTo(user);
        assertThat(saved.getImagePath()).isEqualTo("1/x.jpg");
        assertThat(saved.getOverallScore()).isEqualTo(report.overallScore());
        assertThat(saved.getRawAiResponse()).isEqualTo("raw-ai-response");
        assertThat(saved.getConfidenceLevel()).isEqualTo(ConfidenceLevel.HIGH);
        assertThat(result).isEqualTo(saved);
    }

    @Test
    @DisplayName("persist maps confidence 'invalid' → LOW via fromStringOrLow")
    void persistConfidenceFallback() {
        FeedbackReport report = new FeedbackReport(null, null, null, null, null, "WEIRD", null);
        when(storageService.store(image, 1L)).thenReturn(new StoredFile("1/x.jpg", "image/jpeg", 3L));
        when(sessionRepository.save(org.mockito.ArgumentMatchers.any(CoachingSession.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        ArgumentCaptor<CoachingSession> captor = ArgumentCaptor.forClass(CoachingSession.class);

        persister.persist(user, image, report, "raw");

        org.mockito.Mockito.verify(sessionRepository).save(captor.capture());
        assertThat(captor.getValue().getConfidenceLevel()).isEqualTo(ConfidenceLevel.LOW);
    }
}
