package com.playmotech.ghostcoach.session;

import com.playmotech.ghostcoach.ai.dto.FeedbackReport;
import com.playmotech.ghostcoach.common.exception.ApiException;
import com.playmotech.ghostcoach.storage.StorageService;
import com.playmotech.ghostcoach.support.TestData;
import com.playmotech.ghostcoach.user.User;
import com.playmotech.ghostcoach.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SessionServiceTest {

    @Mock SessionRepository sessionRepository;
    @Mock UserRepository userRepository;
    @Mock StorageService storageService;
    @Mock SessionAnalyzer sessionAnalyzer;
    @Mock SessionPersister sessionPersister;

    @InjectMocks SessionService service;

    private User user;
    private CoachingSession session;
    private MultipartFile image;

    @BeforeEach
    void setUp() throws IOException {
        user = TestData.user();
        session = TestData.session(user);
        image = new MockMultipartFile("image", "stance.jpg", "image/jpeg", generateJpeg());
    }

    @Test
    @DisplayName("analyzeAndStore happy path → analyzer THEN persister (no orphan)")
    void happyPath() {
        FeedbackReport report = TestData.feedbackReport();
        SessionAnalyzer.AnalysisResult result = new SessionAnalyzer.AnalysisResult(report, "raw");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(sessionAnalyzer.analyze(eq(user), anyString(), anyString())).thenReturn(result);
        when(sessionPersister.persist(eq(user), eq(image), eq(report), eq("raw"))).thenReturn(session);

        CoachingSession out = service.analyzeAndStore(1L, image);

        assertThat(out).isEqualTo(session);
        InOrder ordered = inOrder(sessionAnalyzer, sessionPersister);
        ordered.verify(sessionAnalyzer).analyze(eq(user), anyString(), anyString());
        ordered.verify(sessionPersister).persist(eq(user), eq(image), eq(report), eq("raw"));
    }

    @Test
    @DisplayName("user not found → ApiException 404, no AI call, no persist")
    void userNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.analyzeAndStore(1L, image))
                .isInstanceOfSatisfying(ApiException.class, ex -> {
                    assertThat(ex.getStatus()).isEqualTo(HttpStatus.NOT_FOUND);
                    assertThat(ex.getCode()).isEqualTo("USER_NOT_FOUND");
                });

        verify(sessionAnalyzer, never()).analyze(any(), any(), any());
        verify(sessionPersister, never()).persist(any(), any(), any(), any());
        verify(storageService, never()).store(any(), anyLong());
    }

    @Test
    @DisplayName("invalid MIME → 400 INVALID_FILE_TYPE before AI call")
    void invalidMime() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        MultipartFile fake = new MockMultipartFile("image", "fake.jpg", "image/jpeg",
                "not really an image".getBytes());

        assertThatThrownBy(() -> service.analyzeAndStore(1L, fake))
                .isInstanceOfSatisfying(ApiException.class, ex -> {
                    assertThat(ex.getCode()).isEqualTo("INVALID_FILE_TYPE");
                });

        verify(sessionAnalyzer, never()).analyze(any(), any(), any());
        verify(sessionPersister, never()).persist(any(), any(), any(), any());
    }

    @Test
    @DisplayName("analyzer throws AI_PARSE_FAILED → no persist, no file written")
    void analyzerFailsNoOrphan() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(sessionAnalyzer.analyze(eq(user), anyString(), anyString()))
                .thenThrow(ApiException.badGateway("AI_PARSE_FAILED", "bad parse"));

        assertThatThrownBy(() -> service.analyzeAndStore(1L, image))
                .isInstanceOfSatisfying(ApiException.class, ex -> {
                    assertThat(ex.getCode()).isEqualTo("AI_PARSE_FAILED");
                });

        verify(sessionPersister, never()).persist(any(), any(), any(), any());
        verify(storageService, never()).store(any(), anyLong());
    }

    @Test
    @DisplayName("analyzer throws IMAGE_QUALITY_INSUFFICIENT → no persist, no file written")
    void analyzerInsufficientQualityNoPersist() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(sessionAnalyzer.analyze(eq(user), anyString(), anyString()))
                .thenThrow(ApiException.unprocessableEntity("IMAGE_QUALITY_INSUFFICIENT", "Too blurry"));

        assertThatThrownBy(() -> service.analyzeAndStore(1L, image))
                .isInstanceOfSatisfying(ApiException.class, ex -> {
                    assertThat(ex.getStatus()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
                    assertThat(ex.getCode()).isEqualTo("IMAGE_QUALITY_INSUFFICIENT");
                });

        verify(sessionPersister, never()).persist(any(), any(), any(), any());
        verify(storageService, never()).store(any(), anyLong());
    }

    @Test
    @DisplayName("listForUser delegates to repository with pageable")
    void listForUser() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<CoachingSession> page = new PageImpl<>(List.of(session));
        when(sessionRepository.findByUserIdOrderByCreatedAtDesc(1L, pageable)).thenReturn(page);

        Page<CoachingSession> result = service.listForUser(1L, pageable);

        assertThat(result.getContent()).containsExactly(session);
    }

    @Test
    @DisplayName("getOwned existing returns session")
    void getOwnedExisting() {
        when(sessionRepository.findByIdAndUserId(100L, 1L)).thenReturn(Optional.of(session));

        CoachingSession result = service.getOwned(100L, 1L);

        assertThat(result).isEqualTo(session);
    }

    @Test
    @DisplayName("getOwned missing throws ApiException 404")
    void getOwnedMissing() {
        when(sessionRepository.findByIdAndUserId(eq(999L), anyLong())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getOwned(999L, 1L))
                .isInstanceOfSatisfying(ApiException.class, ex -> {
                    assertThat(ex.getStatus()).isEqualTo(HttpStatus.NOT_FOUND);
                    assertThat(ex.getCode()).isEqualTo("SESSION_NOT_FOUND");
                });
    }

    @Test
    @DisplayName("loadImage delegates to storage with session path")
    void loadImageDelegates() {
        when(sessionRepository.findByIdAndUserId(100L, 1L)).thenReturn(Optional.of(session));
        Resource mockResource = org.mockito.Mockito.mock(Resource.class);
        when(storageService.load(session.getImagePath())).thenReturn(mockResource);

        Resource result = service.loadImage(100L, 1L);

        assertThat(result).isEqualTo(mockResource);
    }

    private static byte[] generateJpeg() throws IOException {
        BufferedImage img = new BufferedImage(16, 16, BufferedImage.TYPE_INT_RGB);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ImageIO.write(img, "jpg", out);
        return out.toByteArray();
    }
}
