package AgroProtect.useradapter;

/**
 * MINIMAL USER STATUS - Exact copy from identity module
 *
 * When integrating: DELETE and use tn.esprit.agroprotect.identity.entity.UserStatus
 */
public enum UserStatus {
    PENDING,
    ACTIVE,
    LOCKED,
    DISABLED,
    DELETED
}