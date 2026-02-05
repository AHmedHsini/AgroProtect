package com.agriplatform.identity.exception;

public class PasswordReusedException extends IdentityException {
    public PasswordReusedException(String message) {
        super(message, "PASSWORD_REUSED");
    }
}
