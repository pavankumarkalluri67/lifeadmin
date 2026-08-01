package com.LifeAdmin.ai.lifeadmin.storage.domain;

import org.springframework.core.io.Resource;

import java.util.Optional;

/**
 * StorageService port: abstraction for document storage.
 * Requirement 35
 */
public interface StorageService {
    /**
     * Store file bytes and return the storage key.
     */
    String store(byte[] fileBytes, String fileName);

    /**
     * Retrieve file by storage key.
     */
    Optional<Resource> retrieve(String storageKey);

    /**
     * Delete file by storage key.
     */
    void delete(String storageKey);
}
