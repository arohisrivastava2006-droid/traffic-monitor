package com.traffic.exception;

/** Thrown when a traffic reading fails validation. */
public class InvalidTrafficDataException extends Exception {
    public InvalidTrafficDataException(String message) {
        super(message);
    }
}
