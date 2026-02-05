package com.agriplatform.identity.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Biometric enrollment request DTO.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BiometricEnrollRequest {

    /**
     * Base64-encoded face image.
     */
    @NotBlank(message = "Face image is required")
    private String faceImage;

    /**
     * Indicates if liveness detection was performed on client.
     */
    private Boolean livenessVerified;
}
