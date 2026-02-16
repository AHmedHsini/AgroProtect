package tn.esprit.agroprotect.identity.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

/**
 * Authentication response with tokens.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {

    private String accessToken;
    private String refreshToken;
    private String tokenType;
    private Long expiresIn;
    private UserResponse user;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserResponse {
        private String uuid;
        private String email;
        private String firstName;
        private String lastName;
        private String phone;
        private Boolean emailVerified;
        private Boolean phoneVerified;
        private Boolean mfaEnabled;
        private Boolean biometricEnabled;
        private Set<String> roles;
    }
}
