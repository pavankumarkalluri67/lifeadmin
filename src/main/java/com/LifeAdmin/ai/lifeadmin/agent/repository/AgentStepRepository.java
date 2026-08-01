package com.LifeAdmin.ai.lifeadmin.agent.repository;


import com.LifeAdmin.ai.lifeadmin.agent.domain.AgentStep;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AgentStepRepository extends JpaRepository<AgentStep, UUID> {
    List<AgentStep> findByAgentExecutionIdOrderByStepNumberAsc(UUID agentExecutionId);
}
