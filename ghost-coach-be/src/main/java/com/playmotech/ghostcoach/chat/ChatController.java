package com.playmotech.ghostcoach.chat;

import com.playmotech.ghostcoach.chat.dto.ChatMessageResponse;
import com.playmotech.ghostcoach.chat.dto.ChatRequest;
import com.playmotech.ghostcoach.common.dto.ApiResponse;
import com.playmotech.ghostcoach.security.UserPrincipal;
import com.playmotech.ghostcoach.session.dto.PageResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Validated
@RequestMapping("/api/v1/sessions/{sessionId}/chat")
public class ChatController {

    private final ChatService chatService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<List<ChatMessageResponse>> send(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long sessionId,
            @Valid @RequestBody ChatRequest request) {
        List<ChatMessage> result = chatService.sendMessage(principal.id(), sessionId, request.message());
        return ApiResponse.ok(
                HttpStatus.CREATED,
                "Message sent successfully",
                result.stream().map(ChatMessageResponse::from).toList()
        );
    }

    @GetMapping
    public ApiResponse<PageResponse<ChatMessageResponse>> history(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long sessionId,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "50") @Min(1) @Max(100) int size) {
        Page<ChatMessage> result = chatService.history(
                principal.id(), sessionId, PageRequest.of(page, size));
        return ApiResponse.ok(PageResponse.from(result, ChatMessageResponse::from));
    }
}
