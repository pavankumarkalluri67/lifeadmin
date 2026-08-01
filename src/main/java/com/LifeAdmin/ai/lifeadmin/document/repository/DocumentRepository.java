package com.LifeAdmin.ai.lifeadmin.document.repository;


import com.LifeAdmin.ai.lifeadmin.document.domain.Document;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface DocumentRepository extends JpaRepository<Document, UUID> {
    Page<Document> findByUserId(UUID userId, Pageable pageable);
    Optional<Document> findByIdAndUserId(UUID id, UUID userId);
    Optional<Document> findByChecksumSha256AndUserId(String checksumSha256, UUID userId);
}
