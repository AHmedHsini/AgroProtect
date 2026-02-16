package tn.esprit.agroprotect.identity.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Biometric verification response DTO.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BiometricResponse {

    private Boolean verified;
    private Double confidenceScore;
    private Boolean livenessVerified;
    private String message;
}
