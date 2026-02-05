package com.agriplatform.identity.controller;

import com.agriplatform.identity.dto.request.BiometricEnrollRequest;
import com.agriplatform.identity.dto.request.BiometricVerifyRequest;
import com.agriplatform.identity.dto.response.ApiResponse;
import com.agriplatform.identity.dto.response.BiometricResponse;
import com.agriplatform.identity.service.BiometricService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

/**
 * Biometric enrollment and verification controller.
 */
@RestController
@RequestMapping("/v1/biometric")
@RequiredArgsConstructor
@Tag(name = "Biometric", description = "Face enrollment and verification")
public class BiometricController {

    private final BiometricService biometricService;

    @PostMapping("/enroll")
    @Operation(summary = "Enroll face", description = "Enroll face biometric for authentication")
    public ResponseEntity<ApiResponse<BiometricResponse>> enrollFace(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody BiometricEnrollRequest request,
            HttpServletRequest httpRequest) {

        BiometricResponse response = biometricService.enrollFace(
                userDetails.getUsername(), request, httpRequest);
        return ResponseEntity.ok(ApiResponse.success("Face enrolled successfully", response));
    }

    @PostMapping("/verify")
    @Operation(summary = "Verify face", description = "Verify face against enrolled biometric")
    public ResponseEntity<ApiResponse<BiometricResponse>> verifyFace(
            @Valid @RequestBody BiometricVerifyRequest request,
            HttpServletRequest httpRequest) {

        BiometricResponse response = biometricService.verifyFace(request, httpRequest);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @DeleteMapping("/me")
    @Operation(summary = "Remove biometric", description = "Remove enrolled biometric data")
    public ResponseEntity<ApiResponse<Void>> removeBiometric(
            @AuthenticationPrincipal UserDetails userDetails,
            HttpServletRequest httpRequest) {

        biometricService.removeBiometric(userDetails.getUsername(), httpRequest);
        return ResponseEntity.ok(ApiResponse.success("Biometric data removed"));
    }

    @GetMapping("/me/status")
    @Operation(summary = "Get biometric status", description = "Check if biometric is enrolled")
    public ResponseEntity<ApiResponse<Boolean>> getBiometricStatus(
            @AuthenticationPrincipal UserDetails userDetails) {

        boolean enrolled = biometricService.isBiometricEnrolled(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(enrolled));
    }

    // Internal endpoint for service-to-service verification
    @PostMapping("/internal/verify")
    @PreAuthorize("hasAuthority('SERVICE')")
    @Operation(summary = "Internal verify", description = "Internal biometric verification for services")
    public ResponseEntity<ApiResponse<BiometricResponse>> internalVerify(
            @Valid @RequestBody BiometricVerifyRequest request,
            HttpServletRequest httpRequest) {

        BiometricResponse response = biometricService.verifyFace(request, httpRequest);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
