package com.shawnbethune.contactservice.exception;

public class ContactValidationException extends IllegalArgumentException {
    public ContactValidationException(String message) {
        super(message);
    }
}
