package tn.esprit.agroprotect.identity.exception;

public class InvalidCredentialsException extends IdentityException {
    public InvalidCredentialsException(String message) {
        super(message, "INVALID_CREDENTIALS");
    }
}
