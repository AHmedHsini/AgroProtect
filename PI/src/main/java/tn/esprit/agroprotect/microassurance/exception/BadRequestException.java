package tn.esprit.agroprotect.microassurance.exception;

/**
 * Exception levée en cas de requête invalide
 */
public class BadRequestException extends RuntimeException {

    public BadRequestException(String message) {
        super(message);
    }

    public BadRequestException(String message, Throwable cause) {
        super(message, cause);
    }
}