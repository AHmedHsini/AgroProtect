package com.agriplatform.identity.entity;

/**
 * User account status enumeration.
 * 
 * PENDING - Account created but not yet verified
 * ACTIVE - Fully verified and active account
 * LOCKED - Temporarily locked due to security concerns (failed logins, etc.)
 * DISABLED - Administratively disabled account
 * DELETED - Soft deleted account (pending permanent deletion)
 */
public enum UserStatus {
    PENDING,
    ACTIVE,
    LOCKED,
    DISABLED,
    DELETED
}
