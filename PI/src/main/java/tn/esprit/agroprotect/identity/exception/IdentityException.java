package tn.esprit.agroprotect.identity.exception;

/**
 * Base exception for identity service.
 */
public abstract class IdentityException extends RuntimeException {

    private final String errorCode;

    protected IdentityException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    protected IdentityException(String message, String errorCode, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
