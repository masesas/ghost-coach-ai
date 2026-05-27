package com.playmotech.ghostcoach.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.util.unit.DataSize;

@Component
@ConfigurationProperties("app")
@Data
public final class AppConfigProp {
    private Jwt jwt;
    private Storage storage;
    private Gemini gemini;


    @Data
    public static class Jwt {
        private String secret;
        private int expirationHours;
        /**
         * Token issuer claim ({@code iss}). Validated on parse to reject tokens minted
         * by other services that happen to share the secret.
         */
        private String issuer = "ghost-coach";
        /**
         * Token audience claim ({@code aud}). Validated on parse.
         */
        private String audience = "ghost-coach-client";
    }

    @Data
    public static class Storage {
        private String uploadDir;
        /**
         * Maximum upload size enforced by the storage service. Must be kept in sync
         * with {@code spring.servlet.multipart.max-file-size}; the recommended pattern
         * is to drive both via the {@code APP_MAX_FILE_SIZE} env var.
         */
        private DataSize maxFileSize = DataSize.ofMegabytes(5);
    }

    @Data
    public static class Gemini {
        private String apiKey;
        private String model;
        private String baseUrl;
        private String timeoutSeconds;
    }
}
