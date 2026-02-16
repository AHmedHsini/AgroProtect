package tn.esprit.agroprotect.identity.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Set;

/**
 * User profile response DTO.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileResponse {

    private String uuid;
    private String email;
    private String firstName;
    private String lastName;
    private String phone;
    private String status;
    private String authProvider;
    private Boolean emailVerified;
    private Boolean phoneVerified;
    private Boolean mfaEnabled;
    private Boolean biometricEnabled;
    private Set<String> roles;
    private Set<String> permissions;
    private Boolean consentMarketing;
    private String consentPolicyVersion;
    private Instant consentAcceptedAt;
    private Instant createdAt;
    private Instant lastLoginAt;
}
