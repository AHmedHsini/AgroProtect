package tn.esprit.agroprotect.identity.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Biometric verification request DTO.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BiometricVerifyRequest {

    /**
     * Base64-encoded face image for verification.
     */
    @NotBlank(message = "Face image is required")
    private String faceImage;

    /**
     * User UUID to verify against.
     */
    @NotBlank(message = "User UUID is required")
    private String userUuid;
}
