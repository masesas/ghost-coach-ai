package com.playmotech.ghostcoach.session;

import com.playmotech.ghostcoach.common.dto.ApiResponse;
import com.playmotech.ghostcoach.security.UserPrincipal;
import com.playmotech.ghostcoach.session.dto.PageResponse;
import com.playmotech.ghostcoach.session.dto.SessionDetail;
import com.playmotech.ghostcoach.session.dto.SessionSummary;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@Validated
@RequestMapping("/api/v1/sessions")
public class SessionController {

    private final SessionService sessionService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<SessionDetail> upload(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam("image") MultipartFile image) {
        CoachingSession session = sessionService.analyzeAndStore(principal.id(), image);
        return ApiResponse.ok(
                HttpStatus.CREATED,
                "Session created successfully",
                SessionDetail.from(session)
        );
    }

    @GetMapping
    public ApiResponse<PageResponse<SessionSummary>> list(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(50) int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<CoachingSession> result = sessionService.listForUser(principal.id(), pageable);
        return ApiResponse.ok(PageResponse.from(result, SessionSummary::from));
    }

    @GetMapping("/{id}")
    public ApiResponse<SessionDetail> detail(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id) {
        return ApiResponse.ok(SessionDetail.from(sessionService.getOwned(id, principal.id())));
    }

    @GetMapping("/{id}/image")
    public ResponseEntity<Resource> image(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id) {
        Resource resource = sessionService.loadImage(id, principal.id());
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline")
                .body(resource);
    }
}
