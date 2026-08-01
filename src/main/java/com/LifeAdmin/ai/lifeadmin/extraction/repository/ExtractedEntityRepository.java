package com.LifeAdmin.ai.lifeadmin.extraction.repository;


import com.LifeAdmin.ai.lifeadmin.extraction.domain.ExtractedEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ExtractedEntityRepository extends JpaRepository<ExtractedEntity, UUID> {
    List<ExtractedEntity> findByDocumentId(UUID documentId);
    void deleteByDocumentId(UUID documentId);
}
