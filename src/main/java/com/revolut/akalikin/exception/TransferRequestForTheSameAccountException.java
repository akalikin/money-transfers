package com.revolut.akalikin.exception;

/**
 * Exception thrown when transferring from account to the same account.
 */
public class TransferRequestForTheSameAccountException extends InvalidRequestException {

    public static final String MESSAGE = "Requesting a transfer in which from and to are the same account: %s.";

    public TransferRequestForTheSameAccountException(String accountId) {
        super(String.format(MESSAGE, accountId));
    }
}
