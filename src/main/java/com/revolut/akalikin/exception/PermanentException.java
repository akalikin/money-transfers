package com.revolut.akalikin.exception;

/**
 * Type of exception signifying that it shouldn't be retried.
 */
public class PermanentException extends Throwable {

    public static final String MESSAGE = "A permanent fault encountered: %s";

    public PermanentException(String message) {
        this(message, null);
    }

    public PermanentException(String message, Throwable cause) {
        super(String.format(MESSAGE, message), cause);
    }
}
