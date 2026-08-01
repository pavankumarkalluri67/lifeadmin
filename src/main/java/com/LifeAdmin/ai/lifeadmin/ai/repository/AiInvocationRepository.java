package com.LifeAdmin.ai.lifeadmin.ai.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.LifeAdmin.ai.lifeadmin.ai.domain.AiInvocation;

/**
 * Persistence port for {@link AiInvocation} audit records (Req 27.1).
 */
public interface AiInvocationRepository extends JpaRepository<AiInvocation, UUID> {
}
