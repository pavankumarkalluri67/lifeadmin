package com.LifeAdmin.ai.lifeadmin.document.repository;


import com.LifeAdmin.ai.lifeadmin.document.domain.DocumentContents;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface DocumentContentsRepository extends JpaRepository<DocumentContents, UUID> {
    Optional<DocumentContents> findByDocumentId(UUID documentId);
}
