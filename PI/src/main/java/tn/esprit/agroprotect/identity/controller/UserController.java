package tn.esprit.agroprotect.identity.controller;

import tn.esprit.agroprotect.identity.dto.request.ChangePasswordRequest;
import tn.esprit.agroprotect.identity.dto.request.UpdateProfileRequest;
import tn.esprit.agroprotect.identity.dto.response.ApiResponse;
import tn.esprit.agroprotect.identity.dto.response.DeviceSessionResponse;
import tn.esprit.agroprotect.identity.dto.response.UserProfileResponse;
import tn.esprit.agroprotect.identity.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * User management controller for profile and session operations.
 */
@RestController
@RequestMapping("/v1/users")
@RequiredArgsConstructor
@Tag(name = "User Management", description = "User profile and session management")
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    @Operation(summary = "Get current user", description = "Get authenticated user's profile")
    public ResponseEntity<ApiResponse<UserProfileResponse>> getCurrentUser(
            @AuthenticationPrincipal UserDetails userDetails) {

        UserProfileResponse profile = userService.getCurrentUser(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(profile));
    }

    @PutMapping("/me")
    @Operation(summary = "Update profile", description = "Update current user's profile")
    public ResponseEntity<ApiResponse<UserProfileResponse>> updateProfile(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody UpdateProfileRequest request,
            HttpServletRequest httpRequest) {

        UserProfileResponse profile = userService.updateProfile(
                userDetails.getUsername(), request, httpRequest);
        return ResponseEntity.ok(ApiResponse.success("Profile updated", profile));
    }

    @PostMapping("/me/password")
    @Operation(summary = "Change password", description = "Change current user's password")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody ChangePasswordRequest request,
            HttpServletRequest httpRequest) {

        userService.changePassword(userDetails.getUsername(), request, httpRequest);
        return ResponseEntity.ok(ApiResponse.success("Password changed successfully"));
    }

    @GetMapping("/me/sessions")
    @Operation(summary = "Get sessions", description = "Get all active sessions")
    public ResponseEntity<ApiResponse<List<DeviceSessionResponse>>> getSessions(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestHeader(value = "X-Device-Id", required = false) String deviceId) {

        List<DeviceSessionResponse> sessions = userService.getUserSessions(
                userDetails.getUsername(), deviceId);
        return ResponseEntity.ok(ApiResponse.success(sessions));
    }

    @DeleteMapping("/me/sessions/{sessionId}")
    @Operation(summary = "Revoke session", description = "Revoke a specific session")
    public ResponseEntity<ApiResponse<Void>> revokeSession(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long sessionId,
            HttpServletRequest httpRequest) {

        userService.revokeSession(userDetails.getUsername(), sessionId, httpRequest);
        return ResponseEntity.ok(ApiResponse.success("Session revoked"));
    }

    @DeleteMapping("/me")
    @Operation(summary = "Delete account", description = "Request account deletion (GDPR)")
    public ResponseEntity<ApiResponse<Void>> deleteAccount(
            @AuthenticationPrincipal UserDetails userDetails,
            HttpServletRequest httpRequest) {

        userService.requestAccountDeletion(userDetails.getUsername(), httpRequest);
        return ResponseEntity.ok(ApiResponse.success(
                "Account marked for deletion. You will be logged out."));
    }
}
