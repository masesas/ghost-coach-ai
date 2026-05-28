package com.playmotech.ghostcoach.support;

import com.playmotech.ghostcoach.security.UserPrincipal;
import org.springframework.core.MethodParameter;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

/**
 * Resolves {@link AuthenticationPrincipal} arguments in standalone MockMvc tests
 * without bootstrapping Spring Security infrastructure.
 */
public class AuthenticationPrincipalSupport implements HandlerMethodArgumentResolver {

    private final UserPrincipal principal;

    public AuthenticationPrincipalSupport(UserPrincipal principal) {
        this.principal = principal;
    }

    public static AuthenticationPrincipalSupport withPrincipal(Long userId, String email) {
        return new AuthenticationPrincipalSupport(new UserPrincipal(userId, email, null));
    }

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(AuthenticationPrincipal.class);
    }

    @Override
    public Object resolveArgument(MethodParameter parameter,
                                  ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest,
                                  WebDataBinderFactory binderFactory) {
        return principal;
    }
}
