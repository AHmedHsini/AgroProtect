package tn.esprit.agroprotect.microassurance.exception;

/**
 * Exception levée quand l'accès à une ressource est interdit
 */
public class ForbiddenException extends RuntimeException {

    public ForbiddenException(String message) {
        super(message);
    }

    public ForbiddenException(String message, Throwable cause) {
        super(message, cause);
    }
}