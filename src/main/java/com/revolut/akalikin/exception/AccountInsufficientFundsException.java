package com.revolut.akalikin.exception;

/**
 * Exception thrown when a transfer is attempted for an amount that is larger than available in the account.
 */
public class AccountInsufficientFundsException extends PermanentException {

    public static final String MESSAGE = "Insufficient funds for account %s, available: %d, requested: %d";

    public AccountInsufficientFundsException(String accountId, Long availableFunds, Long requestedAmount) {
        super(String.format(MESSAGE, accountId, availableFunds, requestedAmount));
    }
}
