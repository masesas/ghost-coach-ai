package com.playmotech.ghostcoach.session;

import com.playmotech.ghostcoach.common.exception.ApiException;
import com.playmotech.ghostcoach.storage.StorageService;
import com.playmotech.ghostcoach.user.User;
import com.playmotech.ghostcoach.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.apache.tika.Tika;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class SessionService {

    private static final Set<String> ALLOWED_MIME = Set.of("image/jpeg", "image/png");
    private static final Tika TIKA = new Tika();

    private final SessionRepository sessionRepository;
    private final UserRepository userRepository;
    private final StorageService storageService;
    private final SessionAnalyzer sessionAnalyzer;
    private final SessionPersister sessionPersister;

    public CoachingSession analyzeAndStore(Long userId, MultipartFile image) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> ApiException.notFound("USER_NOT_FOUND", "User not found"));

        String contentType = detectAndValidateMime(image);
        String base64 = encodeBase64Streaming(image);

        SessionAnalyzer.AnalysisResult result = sessionAnalyzer.analyze(user, contentType, base64);

        return sessionPersister.persist(user, image, result.report(), result.rawResponse());
    }

    @Transactional(readOnly = true)
    public Page<CoachingSession> listForUser(Long userId, Pageable pageable) {
        return sessionRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
    }

    @Transactional(readOnly = true)
    public CoachingSession getOwned(Long sessionId, Long userId) {
        return sessionRepository.findByIdAndUserId(sessionId, userId)
                .orElseThrow(() -> ApiException.notFound("SESSION_NOT_FOUND", "Session not found"));
    }

    @Transactional(readOnly = true)
    public Resource loadImage(Long sessionId, Long userId) {
        CoachingSession session = getOwned(sessionId, userId);
        return storageService.load(session.getImagePath());
    }

    private String detectAndValidateMime(MultipartFile image) {
        if (image == null || image.isEmpty()) {
            throw ApiException.badRequest("FILE_REQUIRED", "Image file is required");
        }
        try (InputStream in = new BufferedInputStream(image.getInputStream())) {
            String detected = TIKA.detect(in);
            if (!ALLOWED_MIME.contains(detected)) {
                throw ApiException.badRequest("INVALID_FILE_TYPE",
                        "File content is not a supported image type. Allowed: " + ALLOWED_MIME);
            }
            return detected;
        } catch (IOException e) {
            throw ApiException.badRequest("INVALID_FILE_TYPE", "Unable to read file content");
        }
    }

    /**
     * Streaming base64 encode — avoids the double allocation of
     * {@code Base64.getEncoder().encodeToString(file.getBytes())}.
     */
    private String encodeBase64Streaming(MultipartFile image) {
        try (InputStream in = image.getInputStream();
             ByteArrayOutputStream out = new ByteArrayOutputStream();
             OutputStream b64 = Base64.getEncoder().wrap(out)) {
            in.transferTo(b64);
            b64.close();
            return out.toString(StandardCharsets.US_ASCII);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to read upload bytes", e);
        }
    }
}
