package com.agriplatform.identity.exception;

public class UserNotFoundException extends IdentityException {
    public UserNotFoundException(String message) {
        super(message, "USER_NOT_FOUND");
    }
}
