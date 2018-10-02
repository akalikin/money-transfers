package com.revolut.akalikin.exception;

/**
 * Transient exception that should be retried in real-life implementation.
 */
public class TransientException extends Throwable {

    private static final String MESSAGE = "Transient exception occurred: %s ";

    public TransientException(String message) {
        this(message, null);
    }

    public TransientException(String message, Throwable cause) {
        super(String.format(MESSAGE, message), cause);
    }
}
