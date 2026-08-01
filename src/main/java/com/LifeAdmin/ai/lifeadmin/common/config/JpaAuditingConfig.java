package com.LifeAdmin.ai.lifeadmin.common.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Enables Spring Data JPA auditing so that {@code @CreatedDate} and
 * {@code @LastModifiedDate} fields on {@code BaseEntity} (created_at /
 * updated_at) are populated automatically on persist and update (Req 30.4).
 */
@Configuration
@EnableJpaAuditing
public class JpaAuditingConfig {
}
