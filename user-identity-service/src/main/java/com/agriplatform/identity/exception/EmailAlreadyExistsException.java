package com.agriplatform.identity.exception;

public class EmailAlreadyExistsException extends IdentityException {
    public EmailAlreadyExistsException(String message) {
        super(message, "EMAIL_EXISTS");
    }
}
