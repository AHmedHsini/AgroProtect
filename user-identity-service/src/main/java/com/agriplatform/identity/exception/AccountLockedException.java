package com.agriplatform.identity.exception;

public class AccountLockedException extends IdentityException {
    public AccountLockedException(String message) {
        super(message, "ACCOUNT_LOCKED");
    }
}
