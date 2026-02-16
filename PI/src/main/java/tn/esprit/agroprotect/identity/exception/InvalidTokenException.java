package tn.esprit.agroprotect.identity.exception;

public class InvalidTokenException extends IdentityException {
    public InvalidTokenException(String message) {
        super(message, "INVALID_TOKEN");
    }
}
