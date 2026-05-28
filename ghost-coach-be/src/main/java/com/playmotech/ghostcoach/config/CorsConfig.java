package com.playmotech.ghostcoach.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;

@Configuration
public class CorsConfig {

    @Value("${app.cors.allowed-origins:http://localhost:5173}")
    private String allowedOrigins;

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        List<String> patterns = parseOriginPatterns(allowedOrigins);

        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);
        config.setAllowedOriginPatterns(patterns);
        config.setAllowedHeaders(List.of("Authorization", "Content-Type", "Accept"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setExposedHeaders(List.of("Authorization"));
        config.setMaxAge(Duration.ofHours(1));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    /**
     * Parse and validate origin patterns.
     * Accepts: exact ("https://app.com") or subdomain wildcard ("https://*.app.com").
     * Rejects: standalone "*", blanks, and patterns without a host part.
     */
    static List<String> parseOriginPatterns(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new IllegalStateException("app.cors.allowed-origins must not be blank");
        }
        List<String> patterns = Arrays.stream(raw.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();

        if (patterns.isEmpty()) {
            throw new IllegalStateException("app.cors.allowed-origins must contain at least one entry");
        }

        for (String p : patterns) {
            if ("*".equals(p)) {
                throw new IllegalStateException(
                        "app.cors.allowed-origins cannot contain standalone '*' when credentials are enabled");
            }
            if (!p.contains("://")) {
                throw new IllegalStateException(
                        "app.cors.allowed-origins entry must contain scheme (http:// or https://): " + p);
            }
            String afterScheme = p.substring(p.indexOf("://") + 3);
            if (afterScheme.isEmpty() || "*".equals(afterScheme)) {
                throw new IllegalStateException(
                        "app.cors.allowed-origins entry must have a host part: " + p);
            }
        }
        return patterns;
    }
}
