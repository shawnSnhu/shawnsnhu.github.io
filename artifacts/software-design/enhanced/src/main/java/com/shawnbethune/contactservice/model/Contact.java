package com.shawnbethune.contactservice.model;

import com.shawnbethune.contactservice.exception.ContactValidationException;
import java.util.Objects;

/**
 * Immutable contact data. Validation here protects the domain even when a
 * contact is created outside the REST controller.
 */
public final class Contact {
    private final String contactId;
    private final String firstName;
    private final String lastName;
    private final String phone;
    private final String address;

    public Contact(String contactId, String firstName, String lastName, String phone, String address) {
        this.contactId = validateContactId(contactId);
        this.firstName = requireTextWithinLimit(firstName, 10, "First name");
        this.lastName = requireTextWithinLimit(lastName, 10, "Last name");
        this.phone = requirePhone(phone);
        this.address = requireTextWithinLimit(address, 30, "Address");
    }

    public static String validateContactId(String contactId) {
        if (contactId == null || !contactId.matches("[A-Za-z0-9_-]{1,10}")) {
            throw new ContactValidationException(
                    "Contact ID must contain 1 to 10 letters, numbers, underscores, or hyphens");
        }
        return contactId;
    }

    private static String requireTextWithinLimit(String value, int maximumLength, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new ContactValidationException(fieldName + " is required");
        }
        if (value.length() > maximumLength) {
            throw new ContactValidationException(
                    fieldName + " must not exceed " + maximumLength + " characters");
        }
        return value;
    }

    private static String requirePhone(String phone) {
        if (phone == null || !phone.matches("[0-9]{10}")) {
            throw new ContactValidationException("Phone must contain exactly 10 digits");
        }
        return phone;
    }

    public String getContactId() {
        return contactId;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getPhone() {
        return phone;
    }

    public String getAddress() {
        return address;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof Contact contact)) {
            return false;
        }
        return contactId.equals(contact.contactId)
                && firstName.equals(contact.firstName)
                && lastName.equals(contact.lastName)
                && phone.equals(contact.phone)
                && address.equals(contact.address);
    }

    @Override
    public int hashCode() {
        return Objects.hash(contactId, firstName, lastName, phone, address);
    }
}
