package com.revolut.akalikin.exception;

public class InvalidRequestException extends PermanentException {

    public static final String MESSAGE = "Invalid request received: %s";

    public InvalidRequestException(String message) {
        super(String.format(MESSAGE, message));
    }
}
