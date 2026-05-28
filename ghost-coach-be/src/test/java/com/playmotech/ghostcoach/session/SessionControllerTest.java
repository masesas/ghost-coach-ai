package com.playmotech.ghostcoach.session;

import com.playmotech.ghostcoach.common.exception.ApiException;
import com.playmotech.ghostcoach.common.exception.GlobalExceptionHandler;
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
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class SessionControllerTest {

    @Mock SessionService sessionService;

    MockMvc mvc;
    User user;
    CoachingSession session;

    @BeforeEach
    void setUp() {
        user = TestData.user();
        session = TestData.session(user);
        mvc = MockMvcBuilders.standaloneSetup(new SessionController(sessionService))
                .setControllerAdvice(new GlobalExceptionHandler(TestConfig.defaultAppConfigProp()))
                .setCustomArgumentResolvers(
                        AuthenticationPrincipalSupport.withPrincipal(user.getId(), user.getEmail()))
                .build();
    }

    @Test
    @DisplayName("POST /sessions upload → 201 with SessionDetail")
    void uploadSession() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "image", "stance.jpg", "image/jpeg", new byte[]{1, 2, 3});
        when(sessionService.analyzeAndStore(eq(1L), any())).thenReturn(session);

        mvc.perform(multipart("/api/v1/sessions").file(file))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.id").value(100))
                .andExpect(jsonPath("$.data.imageUrl").value("/api/v1/sessions/100/image"))
                .andExpect(jsonPath("$.data.priorityFix").value("Rotate hips more"));
    }

    @Test
    @DisplayName("POST /sessions insufficient quality → 422 IMAGE_QUALITY_INSUFFICIENT")
    void uploadInsufficientQuality() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "image", "blurry.jpg", "image/jpeg", new byte[]{1, 2, 3});
        when(sessionService.analyzeAndStore(eq(1L), any()))
                .thenThrow(ApiException.unprocessableEntity(
                        "IMAGE_QUALITY_INSUFFICIENT", "Photo is too blurry. Retake."));

        mvc.perform(multipart("/api/v1/sessions").file(file))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.error.code").value("IMAGE_QUALITY_INSUFFICIENT"))
                .andExpect(jsonPath("$.message").value("Photo is too blurry. Retake."));
    }

    @Test
    @DisplayName("GET /sessions paginated → 200 with PageResponse")
    void listSessions() throws Exception {
        when(sessionService.listForUser(eq(1L), any()))
                .thenReturn(new PageImpl<>(List.of(session), PageRequest.of(0, 20), 1));

        mvc.perform(get("/api/v1/sessions").param("page", "0").param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].id").value(100))
                .andExpect(jsonPath("$.data.totalElements").value(1));
    }

    @Test
    @DisplayName("GET /sessions/{id} found → 200 with SessionDetail")
    void getDetail() throws Exception {
        when(sessionService.getOwned(100L, 1L)).thenReturn(session);

        mvc.perform(get("/api/v1/sessions/100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(100))
                .andExpect(jsonPath("$.data.imageUrl").value("/api/v1/sessions/100/image"));
    }

    @Test
    @DisplayName("GET /sessions/{id} not found → 404 SESSION_NOT_FOUND")
    void getDetailNotFound() throws Exception {
        when(sessionService.getOwned(eq(999L), anyLong()))
                .thenThrow(ApiException.notFound("SESSION_NOT_FOUND", "Session not found"));

        mvc.perform(get("/api/v1/sessions/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error.code").value("SESSION_NOT_FOUND"));
    }

    @Test
    @DisplayName("GET /sessions/{id}/image → 200 with inline Content-Disposition")
    void getImage() throws Exception {
        Resource resource = new ByteArrayResource(new byte[]{10, 20, 30});
        when(sessionService.loadImage(100L, 1L)).thenReturn(resource);

        mvc.perform(get("/api/v1/sessions/100/image"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", "inline"));
    }

    private static org.springframework.test.web.servlet.result.HeaderResultMatchers header() {
        return org.springframework.test.web.servlet.result.MockMvcResultMatchers.header();
    }
}
