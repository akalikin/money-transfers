package com.revolut.akalikin.exception;

/**
 * Exception thrown when the transfer amount requested is invalid.
 */
public class InvalidTransferAmountException extends InvalidRequestException {

    public static final String MESSAGE = "Invalid transfer amount encountered: %d.";

    public InvalidTransferAmountException(Long amount) {
        super(String.format(MESSAGE, amount));
    }
}
