package com.playmotech.ghostcoach.user;

import com.playmotech.ghostcoach.common.dto.ApiResponse;
import com.playmotech.ghostcoach.security.UserPrincipal;
import com.playmotech.ghostcoach.user.dto.ProfileResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public ApiResponse<ProfileResponse> me(@AuthenticationPrincipal UserPrincipal principal) {
        return ApiResponse.ok(ProfileResponse.from(userService.getById(principal.id())));
    }
}
