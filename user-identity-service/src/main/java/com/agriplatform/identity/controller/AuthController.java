package com.agriplatform.identity.controller;

import com.agriplatform.identity.dto.request.*;
import com.agriplatform.identity.dto.response.ApiResponse;
import com.agriplatform.identity.dto.response.AuthResponse;
import com.agriplatform.identity.service.AuthService;
import com.agriplatform.identity.service.OtpService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

/**
 * Authentication controller handling registration, login, and token operations.
 */
@RestController
@RequestMapping("/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "User authentication and token management")
public class AuthController {

    private final AuthService authService;
    private final OtpService otpService;

    @PostMapping("/register")
    @Operation(summary = "Register new user", description = "Register with email/password")
    public ResponseEntity<ApiResponse<AuthResponse>> register(
            @Valid @RequestBody RegisterRequest request,
            HttpServletRequest httpRequest) {

        AuthResponse response = authService.register(request, httpRequest);
        return ResponseEntity.ok(ApiResponse.success("Registration successful", response));
    }

    @PostMapping("/login")
    @Operation(summary = "Login", description = "Authenticate with email/password")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest) {

        AuthResponse response = authService.login(request, httpRequest);
        return ResponseEntity.ok(ApiResponse.success("Login successful", response));
    }

    @PostMapping("/refresh")
    @Operation(summary = "Refresh token", description = "Get new access token using refresh token")
    public ResponseEntity<ApiResponse<AuthResponse>> refreshToken(
            @Valid @RequestBody RefreshTokenRequest request,
            HttpServletRequest httpRequest) {

        AuthResponse response = authService.refreshToken(request, httpRequest);
        return ResponseEntity.ok(ApiResponse.success("Token refreshed", response));
    }

    @PostMapping("/logout")
    @Operation(summary = "Logout", description = "Logout current session")
    public ResponseEntity<ApiResponse<Void>> logout(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestHeader(value = "X-Device-Id", required = false) String deviceId,
            HttpServletRequest httpRequest) {

        authService.logout(userDetails.getUsername(), deviceId, httpRequest);
        return ResponseEntity.ok(ApiResponse.success("Logged out successfully"));
    }

    @PostMapping("/logout-all")
    @Operation(summary = "Logout all sessions", description = "Logout from all devices")
    public ResponseEntity<ApiResponse<Void>> logoutAll(
            @AuthenticationPrincipal UserDetails userDetails,
            HttpServletRequest httpRequest) {

        authService.logoutAll(userDetails.getUsername(), httpRequest);
        return ResponseEntity.ok(ApiResponse.success("Logged out from all devices"));
    }

    @PostMapping("/otp/send")
    @Operation(summary = "Send OTP", description = "Send OTP to phone number")
    public ResponseEntity<ApiResponse<Void>> sendOtp(
            @Valid @RequestBody OtpSendRequest request) {

        if (!otpService.canRequestOtp(request.getPhone())) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Too many OTP requests. Please wait."));
        }

        String otp = otpService.generateOtp(request.getPhone(), request.getPurpose());
        // In production, send via SMS provider
        // smsService.sendOtp(request.getPhone(), otp);

        return ResponseEntity.ok(ApiResponse.success("OTP sent successfully"));
    }

    @PostMapping("/otp/verify")
    @Operation(summary = "Verify OTP", description = "Verify OTP and authenticate")
    public ResponseEntity<ApiResponse<?>> verifyOtp(
            @Valid @RequestBody OtpVerifyRequest request,
            HttpServletRequest httpRequest) {

        OtpService.OtpVerificationResult result = otpService.verifyOtp(
                request.getPhone(), "login", request.getOtp());

        if (!result.isSuccess()) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(result.getMessage()));
        }

        // OTP verified - would authenticate user here
        // For now, return success
        return ResponseEntity.ok(ApiResponse.success("OTP verified successfully"));
    }

    @GetMapping("/email/verify")
    @Operation(summary = "Verify email", description = "Verify email with token")
    public ResponseEntity<ApiResponse<Void>> verifyEmail(
            @RequestParam String token) {

        authService.verifyEmail(token);
        return ResponseEntity.ok(ApiResponse.success("Email verified successfully"));
    }

    @PostMapping("/password/reset-request")
    @Operation(summary = "Request password reset", description = "Send password reset email")
    public ResponseEntity<ApiResponse<Void>> requestPasswordReset(
            @Valid @RequestBody PasswordResetRequest request,
            HttpServletRequest httpRequest) {

        authService.requestPasswordReset(request, httpRequest);
        return ResponseEntity.ok(ApiResponse.success(
                "If the email exists, a password reset link has been sent"));
    }

    @PostMapping("/password/reset")
    @Operation(summary = "Reset password", description = "Reset password with token")
    public ResponseEntity<ApiResponse<Void>> resetPassword(
            @Valid @RequestBody PasswordResetConfirmRequest request,
            HttpServletRequest httpRequest) {

        authService.resetPassword(request, httpRequest);
        return ResponseEntity.ok(ApiResponse.success("Password reset successful"));
    }
}
