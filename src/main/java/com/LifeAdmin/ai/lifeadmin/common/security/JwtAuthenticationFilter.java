package com.LifeAdmin.ai.lifeadmin.common.security;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Validates the {@code Authorization: Bearer <Access_Token>} header on every
 * request and, on success, populates the {@link SecurityContextHolder} with an
 * {@link org.springframework.security.core.Authentication} whose principal is
 * the authenticated user's {@link UUID} (Req 6.1, 24.4, 26.2).
 *
 * <p>The filter never rejects a request itself: a missing, malformed, or
 * expired token simply leaves the security context unauthenticated. Spring
 * Security's authorization rules then reject protected endpoints with a
 * 401 {@code UNAUTHENTICATED} response via the configured
 * {@code AuthenticationEntryPoint}. This keeps token verification and HTTP
 * response shaping cleanly separated.
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private final TokenService tokenService;

    public JwtAuthenticationFilter(TokenService tokenService) {
        this.tokenService = tokenService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String token = extractBearerToken(request);
        if (token != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            authenticate(token, request);
        }
        filterChain.doFilter(request, response);
    }

    private void authenticate(String token, HttpServletRequest request) {
        try {
            VerifiedToken verified = tokenService.verify(token);
            UUID userId = verified.userId();
            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    userId, null, List.of(new SimpleGrantedAuthority("ROLE_USER")));
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authentication);
        } catch (InvalidTokenException e) {
            // Invalid/expired token: leave the context unauthenticated so the
            // security config rejects protected endpoints with 401.
            SecurityContextHolder.clearContext();
        }
    }

    private static String extractBearerToken(HttpServletRequest request) {
        String header = request.getHeader(AUTHORIZATION_HEADER);
        if (StringUtils.hasText(header) && header.startsWith(BEARER_PREFIX)) {
            String token = header.substring(BEARER_PREFIX.length()).trim();
            return StringUtils.hasText(token) ? token : null;
        }
        return null;
    }
}
