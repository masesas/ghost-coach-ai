package com.playmotech.ghostcoach.storage;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

public interface StorageService {

    StoredFile store(MultipartFile file, Long userId);

    Resource load(String relativePath);
}
