package com.revolut.akalikin.exception;

/**
 * Exception thrown when the lock, required to perform write operations on the account, could not be acquired
 */
public class AccountLockNotAcquiredException extends TransientException {

    private static final String MESSAGE = "Could not acquire lock for account %s";

    public AccountLockNotAcquiredException(String accountId) {
        super(String.format(MESSAGE, accountId));
    }

    public AccountLockNotAcquiredException(String accountId, Throwable cause) {
        super(String.format(MESSAGE, accountId), cause);
    }
}
