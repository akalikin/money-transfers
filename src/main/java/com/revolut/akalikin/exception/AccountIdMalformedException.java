package com.revolut.akalikin.exception;

/**
 * Exception thrown when an invalid Account is received in API.
 */
public class AccountIdMalformedException extends InvalidRequestException {

    public static final String MESSAGE = "Malformed account ID encountered: %s.";

    public AccountIdMalformedException(String accountId) {
        super(String.format(MESSAGE, accountId));
    }
}
