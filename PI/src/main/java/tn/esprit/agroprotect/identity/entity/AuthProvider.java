package tn.esprit.agroprotect.identity.entity;

/**
 * Authentication provider enumeration.
 * 
 * LOCAL - Email/password authentication
 * GOOGLE - Google OAuth2 authentication
 * PHONE - Phone OTP authentication
 */
public enum AuthProvider {
    LOCAL,
    GOOGLE,
    PHONE
}
