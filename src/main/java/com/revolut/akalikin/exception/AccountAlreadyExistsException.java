package com.revolut.akalikin.exception;

/**
 * Exception throws for an attempt to create an account that already exists.
 */
public class AccountAlreadyExistsException extends PermanentException {

    public static final String MESSAGE = "Account with id %s already exists.";

    public AccountAlreadyExistsException(String id) {
        super(String.format(MESSAGE, id));
    }
}
