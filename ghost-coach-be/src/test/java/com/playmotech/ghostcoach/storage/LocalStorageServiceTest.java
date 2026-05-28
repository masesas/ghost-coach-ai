package com.playmotech.ghostcoach.storage;

import com.playmotech.ghostcoach.common.exception.ApiException;
import com.playmotech.ghostcoach.support.TestConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.core.io.Resource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.util.unit.DataSize;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LocalStorageServiceTest {

    @TempDir
    Path tempDir;

    LocalStorageService service;

    @BeforeEach
    void setUp() {
        service = new LocalStorageService(tempDir, TestConfig.defaultAppConfigProp());
        service.init();
    }

    @Test
    @DisplayName("store null file → FILE_REQUIRED")
    void storeNullFile() {
        assertThatThrownBy(() -> service.store(null, 1L))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("Image file is required");
    }

    @Test
    @DisplayName("store empty file → FILE_REQUIRED")
    void storeEmptyFile() {
        MultipartFile empty = new MockMultipartFile("image", "img.jpg", "image/jpeg", new byte[0]);
        assertThatThrownBy(() -> service.store(empty, 1L))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("Image file is required");
    }

    @Test
    @DisplayName("store file > 5MB → FILE_TOO_LARGE (default config)")
    void storeTooLargeFile() {
        byte[] big = new byte[5 * 1024 * 1024 + 1];
        MultipartFile huge = new MockMultipartFile("image", "huge.jpg", "image/jpeg", big);
        assertThatThrownBy(() -> service.store(huge, 1L))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("File exceeds 5MB limit");
    }

    @Test
    @DisplayName("store respects configurable maxFileSize (1MB cap rejects 2MB file)")
    void storeRespectsConfiguredMaxSize() {
        LocalStorageService oneMbService = new LocalStorageService(tempDir,
                TestConfig.appConfigPropWithMaxSize(DataSize.ofMegabytes(1)));
        oneMbService.init();
        byte[] twoMb = new byte[2 * 1024 * 1024];
        MultipartFile file = new MockMultipartFile("image", "x.jpg", "image/jpeg", twoMb);

        assertThatThrownBy(() -> oneMbService.store(file, 1L))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("File exceeds 1MB limit");
    }

    @Test
    @DisplayName("store fake JPEG (text bytes) → INVALID_FILE_TYPE")
    void storeFakeJpeg() {
        MultipartFile fake = new MockMultipartFile(
                "image", "fake.jpg", "image/jpeg", "not really an image".getBytes());
        assertThatThrownBy(() -> service.store(fake, 1L))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("supported image type");
    }

    @Test
    @DisplayName("store real JPEG → stored with detected MIME and jpg extension")
    void storeRealJpeg() throws IOException {
        byte[] jpegBytes = generateImageBytes("jpg");
        // Declared MIME sengaja salah (image/png) → permissive mode: terima karena detected JPEG
        MultipartFile file = new MockMultipartFile("image", "photo.png", "image/png", jpegBytes);

        StoredFile stored = service.store(file, 42L);

        assertThat(stored.contentType()).isEqualTo("image/jpeg");
        assertThat(stored.relativePath()).matches("42/42_[a-f0-9-]+\\.jpg");
        assertThat(tempDir.resolve(stored.relativePath())).exists();
    }

    @Test
    @DisplayName("store real PNG → stored with detected MIME and png extension")
    void storeRealPng() throws IOException {
        byte[] pngBytes = generateImageBytes("png");
        MultipartFile file = new MockMultipartFile("image", "photo.png", "image/png", pngBytes);

        StoredFile stored = service.store(file, 7L);

        assertThat(stored.contentType()).isEqualTo("image/png");
        assertThat(stored.relativePath()).endsWith(".png");
        assertThat(tempDir.resolve(stored.relativePath())).exists();
    }

    @Test
    @DisplayName("store with null declared Content-Type but valid image bytes → still works (permissive)")
    void storeWithoutDeclaredType() throws IOException {
        byte[] pngBytes = generateImageBytes("png");
        MultipartFile file = new MockMultipartFile("image", "photo", null, pngBytes);

        StoredFile stored = service.store(file, 1L);

        assertThat(stored.contentType()).isEqualTo("image/png");
    }

    @Test
    @DisplayName("load file with path traversal → INVALID_PATH")
    void loadPathTraversal() {
        assertThatThrownBy(() -> service.load("../../etc/passwd"))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("Invalid file path");
    }

    @Test
    @DisplayName("load non-existent file → FILE_NOT_FOUND")
    void loadMissingFile() {
        assertThatThrownBy(() -> service.load("ghost/file.jpg"))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("File not found");
    }

    @Test
    @DisplayName("load existing file → Resource readable")
    void loadExistingFile() throws IOException {
        byte[] pngBytes = generateImageBytes("png");
        MultipartFile file = new MockMultipartFile("image", "p.png", "image/png", pngBytes);
        StoredFile stored = service.store(file, 99L);

        Resource resource = service.load(stored.relativePath());

        assertThat(resource.exists()).isTrue();
        assertThat(resource.isReadable()).isTrue();
        assertThat(resource.contentLength()).isEqualTo(pngBytes.length);
    }

    private static byte[] generateImageBytes(String format) throws IOException {
        BufferedImage img = new BufferedImage(16, 16, BufferedImage.TYPE_INT_RGB);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ImageIO.write(img, format, out);
        return out.toByteArray();
    }
}
