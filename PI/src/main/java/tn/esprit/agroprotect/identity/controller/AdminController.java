package tn.esprit.agroprotect.identity.controller;

import tn.esprit.agroprotect.identity.dto.response.ApiResponse;
import tn.esprit.agroprotect.identity.dto.response.TokenValidationResponse;
import tn.esprit.agroprotect.identity.dto.response.UserProfileResponse;
import tn.esprit.agroprotect.identity.entity.UserStatus;
import tn.esprit.agroprotect.identity.security.JwtTokenProvider;
import tn.esprit.agroprotect.identity.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

/**
 * Admin controller for user management and service-to-service operations.
 */
@RestController
@RequestMapping("/v1/admin")
@RequiredArgsConstructor
@Tag(name = "Admin", description = "Admin operations and internal service endpoints")
public class AdminController {

    private final UserService userService;
    private final JwtTokenProvider jwtTokenProvider;

    // ==================== User Management ====================

    @GetMapping("/users/{uuid}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get user by UUID", description = "Admin: Get user details")
    public ResponseEntity<ApiResponse<UserProfileResponse>> getUser(
            @PathVariable String uuid) {

        UserProfileResponse profile = userService.getUserByUuid(uuid);
        return ResponseEntity.ok(ApiResponse.success(profile));
    }

    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Search users", description = "Admin: Search users with filters")
    public ResponseEntity<ApiResponse<Page<UserProfileResponse>>> searchUsers(
            @RequestParam(required = false) UserStatus status,
            @RequestParam(required = false) String search,
            @PageableDefault(size = 20) Pageable pageable) {

        Page<UserProfileResponse> users = userService.searchUsers(status, search, pageable);
        return ResponseEntity.ok(ApiResponse.success(users));
    }

    @PutMapping("/users/{uuid}/status")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update user status", description = "Admin: Enable/disable/lock user")
    public ResponseEntity<ApiResponse<Void>> updateUserStatus(
            @PathVariable String uuid,
            @RequestParam UserStatus status,
            @AuthenticationPrincipal UserDetails adminDetails,
            HttpServletRequest httpRequest) {

        // Get admin user ID from context
        Long adminId = jwtTokenProvider.getUserIdFromContext();
        userService.updateUserStatus(uuid, status, adminId, httpRequest);
        return ResponseEntity.ok(ApiResponse.success("User status updated"));
    }

    @PutMapping("/users/{uuid}/roles")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Assign roles", description = "Admin: Assign roles to user")
    public ResponseEntity<ApiResponse<Void>> assignRoles(
            @PathVariable String uuid,
            @RequestBody Set<String> roles,
            @AuthenticationPrincipal UserDetails adminDetails,
            HttpServletRequest httpRequest) {

        Long adminId = jwtTokenProvider.getUserIdFromContext();
        userService.assignRoles(uuid, roles, adminId, httpRequest);
        return ResponseEntity.ok(ApiResponse.success("Roles assigned"));
    }

    // ==================== Service-to-Service ====================

    @PostMapping("/token/validate")
    @Operation(summary = "Validate token", description = "Internal: Validate JWT token")
    public ResponseEntity<ApiResponse<TokenValidationResponse>> validateToken(
            @RequestHeader("Authorization") String authHeader) {

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.ok(ApiResponse.success(TokenValidationResponse.builder()
                    .valid(false)
                    .message("Invalid authorization header")
                    .build()));
        }

        String token = authHeader.substring(7);

        try {
            if (jwtTokenProvider.validateToken(token)) {
                String userUuid = jwtTokenProvider.getUserUuidFromToken(token);
                Set<String> roles = jwtTokenProvider.getRolesFromToken(token);
                Set<String> permissions = jwtTokenProvider.getPermissionsFromToken(token);

                return ResponseEntity.ok(ApiResponse.success(TokenValidationResponse.builder()
                        .valid(true)
                        .userUuid(userUuid)
                        .roles(roles)
                        .permissions(permissions)
                        .build()));
            }
        } catch (Exception e) {
            // Token validation failed
        }

        return ResponseEntity.ok(ApiResponse.success(TokenValidationResponse.builder()
                .valid(false)
                .message("Token validation failed")
                .build()));
    }

    @GetMapping("/users/{uuid}/internal")
    @PreAuthorize("hasAuthority('SERVICE')")
    @Operation(summary = "Get user internal", description = "Internal: Get user for services")
    public ResponseEntity<ApiResponse<UserProfileResponse>> getUserInternal(
            @PathVariable String uuid) {

        UserProfileResponse profile = userService.getUserByUuid(uuid);
        return ResponseEntity.ok(ApiResponse.success(profile));
    }
}
