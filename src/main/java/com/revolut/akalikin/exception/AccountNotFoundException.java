package com.revolut.akalikin.exception;

/**
 * Exception that occurs when a requested account doesn't exist.
 */
public class AccountNotFoundException extends PermanentException {

    private static String MESSAGE = "Account with id %s not found.";

    public AccountNotFoundException(String accountId) {
        super(String.format(MESSAGE, accountId));
    }
}
