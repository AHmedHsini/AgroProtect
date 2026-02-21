package tn.esprit.agroprotect.microassurance.exception;

/**
 * Exception levée en cas de conflit d'état
 */
public class ConflictException extends RuntimeException {

    public ConflictException(String message) {
        super(message);
    }

    public ConflictException(String message, Throwable cause) {
        super(message, cause);
    }
}