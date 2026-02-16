package tn.esprit.agroprotect.identity.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * JWT Authentication Filter.
 * 
 * Extracts and validates JWT tokens from Authorization header.
 * Populates SecurityContext with authenticated user details.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtTokenProvider jwtTokenProvider;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        // Extract token from header
        final String authHeader = request.getHeader(AUTHORIZATION_HEADER);

        if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
            filterChain.doFilter(request, response);
            return;
        }

        final String jwt = authHeader.substring(BEARER_PREFIX.length());

        try {
            // Validate token
            JwtTokenProvider.TokenValidationResult validationResult = jwtTokenProvider.validateTokenFull(jwt);

            if (!validationResult.isValid()) {
                log.debug("Invalid token: {}", validationResult.errorMessage());
                filterChain.doFilter(request, response);
                return;
            }

            // Only process access tokens (not refresh tokens)
            if (!"access".equals(validationResult.tokenType()) && !"service".equals(validationResult.tokenType())) {
                log.debug("Not an access token, skipping authentication");
                filterChain.doFilter(request, response);
                return;
            }

            final String userUuid = validationResult.subject();

            // Check if already authenticated
            if (SecurityContextHolder.getContext().getAuthentication() == null) {
                // Handle service tokens differently
                if ("service".equals(validationResult.tokenType())) {
                    authenticateService(request, jwt, userUuid);
                } else {
                    authenticateUser(request, jwt, userUuid);
                }
            }

        } catch (Exception e) {
            log.error("JWT authentication error", e);
        }

        filterChain.doFilter(request, response);
    }

    private void authenticateUser(HttpServletRequest request, String jwt, String userUuid) {
        UserDetails userDetails = userDetailsService.loadUserByUsername(userUuid);

        if (jwtTokenProvider.isTokenValid(jwt, userDetails)) {
            // Extract roles and permissions from token
            Set<String> roles = jwtTokenProvider.extractRoles(jwt);
            Set<String> permissions = jwtTokenProvider.extractPermissions(jwt);

            // Build authorities from both roles and permissions
            var authorities = Stream.concat(
                    roles.stream().map(role -> new SimpleGrantedAuthority("ROLE_" + role)),
                    permissions.stream().map(SimpleGrantedAuthority::new)).collect(Collectors.toList());

            UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                    userDetails,
                    null,
                    authorities);

            authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

            // Store device ID in security context for session validation
            String deviceId = jwtTokenProvider.extractDeviceId(jwt);
            request.setAttribute("deviceId", deviceId);

            SecurityContextHolder.getContext().setAuthentication(authToken);
            log.debug("Authenticated user: {}", userUuid);
        }
    }

    private void authenticateService(HttpServletRequest request, String jwt, String serviceName) {
        Set<String> permissions = jwtTokenProvider.extractPermissions(jwt);

        var authorities = Stream.concat(
                Stream.of(new SimpleGrantedAuthority("ROLE_SERVICE")),
                permissions.stream().map(SimpleGrantedAuthority::new)).collect(Collectors.toList());

        // Create service authentication token
        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                serviceName,
                null,
                authorities);

        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authToken);
        log.debug("Authenticated service: {}", serviceName);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        // Skip filter for public endpoints
        return path.startsWith("/v3/api-docs") ||
                path.startsWith("/swagger-ui") ||
                path.equals("/actuator/health");
    }
}
