package com.LifeAdmin.ai.lifeadmin.processing.repository;


import com.LifeAdmin.ai.lifeadmin.processing.domain.ProcessingJob;
import com.LifeAdmin.ai.lifeadmin.processing.domain.ProcessingJobStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProcessingJobRepository extends JpaRepository<ProcessingJob, UUID> {
    Optional<ProcessingJob> findByDocumentId(UUID documentId);
    List<ProcessingJob> findByStatus(ProcessingJobStatus status);
    List<ProcessingJob> findByStatusIn(List<ProcessingJobStatus> statuses);
}
