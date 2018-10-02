package com.revolut.akalikin.exception;

/**
 * An exception thrown when the provided initial balance is invalid.
 */
public class InvalidInitialBalanceException extends InvalidRequestException {

    public static final String MESSAGE = "Invalid initial balance encountered: %d.";

    public InvalidInitialBalanceException(Long balance) {
        super(String.format(MESSAGE, balance));
    }
}
