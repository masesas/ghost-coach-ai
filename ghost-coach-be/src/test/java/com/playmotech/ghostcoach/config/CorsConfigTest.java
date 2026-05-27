package com.playmotech.ghostcoach.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CorsConfigTest {

    @Test
    @DisplayName("single exact origin parsed")
    void exactOrigin() {
        List<String> result = CorsConfig.parseOriginPatterns("http://localhost:5173");
        assertThat(result).containsExactly("http://localhost:5173");
    }

    @Test
    @DisplayName("multiple origins comma-separated")
    void multipleOrigins() {
        List<String> result = CorsConfig.parseOriginPatterns(
                "https://a.example.com,https://b.example.com");
        assertThat(result).containsExactly("https://a.example.com", "https://b.example.com");
    }

    @Test
    @DisplayName("origin with whitespace trimmed")
    void trimsWhitespace() {
        List<String> result = CorsConfig.parseOriginPatterns(
                " https://a.example.com , https://b.example.com ");
        assertThat(result).containsExactly("https://a.example.com", "https://b.example.com");
    }

    @Test
    @DisplayName("subdomain wildcard pattern accepted")
    void subdomainPattern() {
        List<String> result = CorsConfig.parseOriginPatterns("https://*.example.com");
        assertThat(result).containsExactly("https://*.example.com");
    }

    @Test
    @DisplayName("standalone '*' rejected")
    void rejectStandaloneWildcard() {
        assertThatThrownBy(() -> CorsConfig.parseOriginPatterns("*"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("standalone '*'");
    }

    @Test
    @DisplayName("blank input rejected")
    void rejectBlank() {
        assertThatThrownBy(() -> CorsConfig.parseOriginPatterns(""))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must not be blank");
    }

    @Test
    @DisplayName("null input rejected")
    void rejectNull() {
        assertThatThrownBy(() -> CorsConfig.parseOriginPatterns(null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("origin without scheme rejected")
    void rejectNoScheme() {
        assertThatThrownBy(() -> CorsConfig.parseOriginPatterns("example.com"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("scheme");
    }

    @Test
    @DisplayName("scheme without host rejected")
    void rejectSchemeWithoutHost() {
        assertThatThrownBy(() -> CorsConfig.parseOriginPatterns("https://"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("host");
    }

    @Test
    @DisplayName("scheme + * host rejected (similar to standalone *)")
    void rejectSchemeStarHost() {
        assertThatThrownBy(() -> CorsConfig.parseOriginPatterns("https://*"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("host");
    }
}
