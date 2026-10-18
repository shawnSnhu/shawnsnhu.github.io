package com.shawnbethune.contactservice.exception;

public class ContactNotFoundException extends RuntimeException {
    public ContactNotFoundException(String contactId) {
        super("Contact not found: " + contactId);
    }
}
