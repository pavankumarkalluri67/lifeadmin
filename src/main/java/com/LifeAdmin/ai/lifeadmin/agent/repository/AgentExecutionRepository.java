package com.LifeAdmin.ai.lifeadmin.agent.repository;


import com.LifeAdmin.ai.lifeadmin.agent.domain.AgentExecution;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface AgentExecutionRepository extends JpaRepository<AgentExecution, UUID> {
    Optional<AgentExecution> findByIdAndUserId(UUID id, UUID userId);
}
