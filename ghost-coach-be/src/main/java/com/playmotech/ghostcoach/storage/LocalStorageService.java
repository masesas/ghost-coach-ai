package com.playmotech.ghostcoach.storage;

import com.playmotech.ghostcoach.common.exception.ApiException;
import com.playmotech.ghostcoach.config.AppConfigProp;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.Tika;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.unit.DataSize;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class LocalStorageService implements StorageService {

    /** Map detected MIME type → file extension. Single source of truth for allowed image types. */
    private static final Map<String, String> EXT_MAP = Map.of(
            "image/jpeg", "jpg",
            "image/png", "png"
    );

    private static final Set<String> ALLOWED_TYPES = EXT_MAP.keySet();

    private static final Tika TIKA = new Tika();

    private final Path uploadDirRoot;
    private final AppConfigProp appConfigProp;

    @PostConstruct
    void init() {
        try {
            Files.createDirectories(uploadDirRoot);
        } catch (IOException e) {
            throw new IllegalStateException("Cannot create upload dir: " + uploadDirRoot, e);
        }
    }

    @Override
    public StoredFile store(MultipartFile file, Long userId) {
        if (file == null || file.isEmpty()) {
            throw ApiException.badRequest("FILE_REQUIRED", "Image file is required");
        }
        DataSize maxSize = appConfigProp.getStorage().getMaxFileSize();
        if (file.getSize() > maxSize.toBytes()) {
            throw ApiException.badRequest("FILE_TOO_LARGE",
                    "File exceeds " + formatSize(maxSize) + " limit");
        }

        String detected = detectMimeType(file);
        if (!ALLOWED_TYPES.contains(detected)) {
            throw ApiException.badRequest("INVALID_FILE_TYPE",
                    "File content is not a supported image type. Allowed: " + ALLOWED_TYPES);
        }

        String declared = file.getContentType();
        if (declared != null && !declared.equalsIgnoreCase(detected)) {
            log.warn("MIME mismatch declared={} detected={} userId={}", declared, detected, userId);
        }

        String ext = EXT_MAP.get(detected);
        String filename = "%d_%s.%s".formatted(userId, UUID.randomUUID(), ext);
        Path userDir = uploadDirRoot.resolve(String.valueOf(userId));
        Path target = userDir.resolve(filename);

        try {
            Files.createDirectories(userDir);
            try (InputStream in = file.getInputStream()) {
                Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            throw new IllegalStateException("Failed to store file", e);
        }

        String relative = uploadDirRoot.relativize(target).toString().replace('\\', '/');
        return new StoredFile(relative, detected, file.getSize());
    }

    @Override
    public Resource load(String relativePath) {
        try {
            Path absolute = uploadDirRoot.resolve(relativePath).normalize();
            if (!absolute.startsWith(uploadDirRoot)) {
                throw ApiException.forbidden("INVALID_PATH", "Invalid file path");
            }
            Resource resource = new UrlResource(absolute.toUri());
            if (!resource.exists() || !resource.isReadable()) {
                throw ApiException.notFound("FILE_NOT_FOUND", "File not found");
            }
            return resource;
        } catch (IOException e) {
            throw ApiException.notFound("FILE_NOT_FOUND", "File not found");
        }
    }

    private String detectMimeType(MultipartFile file) {
        try (InputStream in = new BufferedInputStream(file.getInputStream())) {
            return TIKA.detect(in);
        } catch (IOException e) {
            throw ApiException.badRequest("INVALID_FILE_TYPE", "Unable to read file content");
        }
    }

    public static String formatSize(DataSize size) {
        long bytes = size.toBytes();
        if (bytes >= 1024L * 1024L && bytes % (1024L * 1024L) == 0) {
            return (bytes / (1024L * 1024L)) + "MB";
        }
        if (bytes >= 1024L && bytes % 1024L == 0) {
            return (bytes / 1024L) + "KB";
        }
        return bytes + "B";
    }
}
