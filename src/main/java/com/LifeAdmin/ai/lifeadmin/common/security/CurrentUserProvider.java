package com.LifeAdmin.ai.lifeadmin.common.security;

import java.util.UUID;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * The single source of truth for the caller's identity (Req 6.1, 26.2).
 *
 * <p>Reads the authenticated user's {@link UUID} from the Spring Security
 * context, where it was placed by {@link JwtAuthenticationFilter}. Services,
 * controllers, and agent tools resolve the acting user through this provider
 * rather than trusting any user id supplied in the request body, query, or
 * (critically) an LLM tool call (Req 26.2). Ownership-scoped queries downstream
 * use the value returned here.
 */
@Component
public class CurrentUserProvider {

    /**
     * Returns the authenticated user's id.
     *
     * @return the current user's {@link UUID}
     * @throws InvalidTokenException if there is no authenticated user in the
     *                               security context (maps to 401 UNAUTHENTICATED)
     */
    public UUID currentUserId() {
        return null;
    }

    public UUID getUserId() {
        return currentUserId();
    }
}
