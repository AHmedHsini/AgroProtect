package com.agriplatform.identity.exception;

public class BiometricException extends IdentityException {
    public BiometricException(String message) {
        super(message, "BIOMETRIC_ERROR");
    }
}
