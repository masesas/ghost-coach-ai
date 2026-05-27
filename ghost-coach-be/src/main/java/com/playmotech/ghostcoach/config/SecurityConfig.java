package com.playmotech.ghostcoach.config;

import com.playmotech.ghostcoach.common.dto.ApiResponse;
import com.playmotech.ghostcoach.common.dto.ErrorResponse;
import com.playmotech.ghostcoach.security.JwtAuthFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;
import tools.jackson.databind.ObjectMapper;

@Configuration
public class SecurityConfig {

    private static final String[] PUBLIC_ENDPOINTS = {
            "/api/v1/auth/**",
            "/api/v1/system-vars/**",
            "/v3/api-docs/**",
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/actuator/health"
    };

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http,
                                           JwtAuthFilter jwtAuthFilter,
                                           ObjectMapper mapper,
                                           CorsConfigurationSource corsConfigurationSource) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(c -> c.configurationSource(corsConfigurationSource))
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(PUBLIC_ENDPOINTS).permitAll()
                        .anyRequest().authenticated()
                )
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((req, res, e) -> {
                            res.setStatus(HttpStatus.UNAUTHORIZED.value());
                            res.setContentType(MediaType.APPLICATION_JSON_VALUE);
                            mapper.writeValue(res.getWriter(),
                                    ApiResponse.fail(
                                            HttpStatus.UNAUTHORIZED,
                                            "Authentication required",
                                            ErrorResponse.of("AUTHENTICATION_REQUIRED")
                                    )
                            );
                        })
                        .accessDeniedHandler((req, res, e) -> {
                            res.setStatus(HttpStatus.FORBIDDEN.value());
                            res.setContentType(MediaType.APPLICATION_JSON_VALUE);
                            mapper.writeValue(res.getWriter(),
                                    ApiResponse.fail(
                                            HttpStatus.FORBIDDEN,
                                            "Access denied",
                                            ErrorResponse.of("ACCESS_DENIED")
                                    )
                            );
                        })
                )
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
