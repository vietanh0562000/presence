package com.presence.security;

import com.clerk.backend_api.helpers.jwks.AuthenticateRequest;
import com.clerk.backend_api.helpers.jwks.AuthenticateRequestOptions;
import com.clerk.backend_api.helpers.jwks.RequestState;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.net.URI;
import java.util.List;

/**
 * Intercepts every request and verifies the Clerk JWT from the
 * Authorization: Bearer <token> header using Clerk's JWKS endpoint.
 *
 * On success: puts a ClerkPrincipal into the SecurityContext.
 * On failure: lets the request proceed unauthenticated
 *             (Spring Security will return 401 for protected routes).
 */
@Slf4j
@Component
public class ClerkAuthFilter extends OncePerRequestFilter {

    private final AuthenticateRequestOptions clerkOptions;

    public ClerkAuthFilter(@Value("${clerk.secret-key}") String secretKey) {
        this.clerkOptions = AuthenticateRequestOptions.secretKey(secretKey).build();
    }

    @Override
    protected void doFilterInternal(
        HttpServletRequest  request,
        HttpServletResponse response,
        FilterChain         chain
    ) throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            try {
                java.net.http.HttpRequest jdkRequest = java.net.http.HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost" + request.getRequestURI()))
                    .header("Authorization", authHeader)
                    .GET()
                    .build();

                RequestState state = AuthenticateRequest.authenticateRequest(jdkRequest, clerkOptions);

                if (state.isSignedIn()) {
                    Claims claims = state.claims().orElseThrow();
                    String userId = claims.getSubject();
                    String email  = extractEmail(claims);

                    ClerkPrincipal principal = new ClerkPrincipal(userId, email);

                    UsernamePasswordAuthenticationToken auth =
                        new UsernamePasswordAuthenticationToken(principal, null, List.of());
                    auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    SecurityContextHolder.getContext().setAuthentication(auth);
                    log.debug("Clerk auth OK: userId={}", userId);
                }

            } catch (Exception e) {
                // Invalid token — leave SecurityContext empty, Spring returns 401
                log.debug("Clerk token verification failed: {}", e.getMessage());
            }
        }

        chain.doFilter(request, response);
    }

    private String extractEmail(Claims claims) {
        try {
            Object email = claims.get("email");
            return email != null ? email.toString() : null;
        } catch (Exception e) {
            return null;
        }
    }
}
