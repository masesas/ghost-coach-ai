package com.playmotech.ghostcoach.systemvar;

import com.playmotech.ghostcoach.common.dto.ApiResponse;
import com.playmotech.ghostcoach.common.exception.ApiException;
import com.playmotech.ghostcoach.systemvar.dto.SystemVarItem;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/api/v1/system-vars")
@RequiredArgsConstructor
public class SystemVarController {

    private static final Pattern CATEGORY_PATTERN = Pattern.compile("^[A-Z][A-Z0-9_]{0,49}$");
    private static final Pattern SCOPE_PATTERN = Pattern.compile("^[A-Z][A-Z0-9_]{0,28}$");

    private final SystemVarService systemVarService;

    @GetMapping("/{category}")
    public ApiResponse<List<SystemVarItem>> list(
            @PathVariable String category,
            @RequestParam(required = false) String scope
    ) {
        if (!CATEGORY_PATTERN.matcher(category).matches()) {
            throw ApiException.badRequest("INVALID_PARAMETER", "Invalid category");
        }
        if (scope != null && !SCOPE_PATTERN.matcher(scope).matches()) {
            throw ApiException.badRequest("INVALID_PARAMETER", "Invalid scope");
        }
        return ApiResponse.ok(systemVarService.list(category, scope));
    }
}
