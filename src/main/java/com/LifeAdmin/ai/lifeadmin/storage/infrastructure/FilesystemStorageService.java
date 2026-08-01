package com.LifeAdmin.ai.lifeadmin.storage.infrastructure;


import com.LifeAdmin.ai.lifeadmin.storage.domain.StorageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.UUID;

/**
 * FilesystemStorageService: stores documents on the filesystem.
 * Requirement 35
 */
@Component
public class FilesystemStorageService implements StorageService {

    private final Path storagePath;

    public FilesystemStorageService(@Value("${lifeadmin.storage.path:./data/documents}") String storagePath) {
        this.storagePath = Paths.get(storagePath);
        try {
            Files.createDirectories(this.storagePath);
        } catch (IOException e) {
            throw new RuntimeException("Failed to create storage directory: " + storagePath, e);
        }
    }

    @Override
    public String store(byte[] fileBytes, String fileName) {
        // Generate a unique storage key
        String storageKey = UUID.randomUUID() + "_" + System.nanoTime();
        Path filePath = storagePath.resolve(storageKey);

        try {
            Files.write(filePath, fileBytes);
            return storageKey;
        } catch (IOException e) {
            throw new RuntimeException("Failed to store file: " + fileName, e);
        }
    }

    @Override
    public Optional<Resource> retrieve(String storageKey) {
        Path filePath = storagePath.resolve(storageKey);
        if (Files.exists(filePath)) {
            return Optional.of(new FileSystemResource(filePath));
        }
        return Optional.empty();
    }

    @Override
    public void delete(String storageKey) {
        Path filePath = storagePath.resolve(storageKey);
        try {
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            throw new RuntimeException("Failed to delete file: " + storageKey, e);
        }
    }
}
