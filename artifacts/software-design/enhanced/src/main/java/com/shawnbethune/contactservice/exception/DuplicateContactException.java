package com.shawnbethune.contactservice.exception;

public class DuplicateContactException extends RuntimeException {
    public DuplicateContactException(String contactId) {
        super("Contact ID already exists: " + contactId);
    }
}
