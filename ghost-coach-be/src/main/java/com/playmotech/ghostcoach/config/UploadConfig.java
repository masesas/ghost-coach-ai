package com.playmotech.ghostcoach.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class UploadConfig {

    @Value("${app.storage.upload-dir}")
    private String uploadDir;

    @Bean
    public Path uploadDirRoot() {
        return Paths.get(uploadDir).toAbsolutePath().normalize();
    }
}
