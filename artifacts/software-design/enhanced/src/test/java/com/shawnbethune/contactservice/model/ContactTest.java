package com.shawnbethune.contactservice.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class ContactTest {

    @Test
    void constructorAcceptsValidBoundaryValues() {
        Contact contact = new Contact(
                "ID_TEN_CHR",
                "First12345",
                "Last123456",
                "1234567890",
                "123456789012345678901234567890");

        assertEquals(10, contact.getContactId().length());
        assertEquals(10, contact.getFirstName().length());
        assertEquals(10, contact.getLastName().length());
        assertEquals(10, contact.getPhone().length());
        assertEquals(30, contact.getAddress().length());
    }

    @Test
    void constructorRejectsBlankAndOverlongFields() {
        assertThrows(IllegalArgumentException.class,
                () -> new Contact("", "Shawn", "Bethune", "1234567890", "123 Main St"));
        assertThrows(IllegalArgumentException.class,
                () -> new Contact("12345678901", "Shawn", "Bethune", "1234567890", "123 Main St"));
        assertThrows(IllegalArgumentException.class,
                () -> new Contact("1", "FirstTooLong", "Bethune", "1234567890", "123 Main St"));
        assertThrows(IllegalArgumentException.class,
                () -> new Contact("1", "Shawn", "LastNameTooLong", "1234567890", "123 Main St"));
        assertThrows(IllegalArgumentException.class,
                () -> new Contact("1", "Shawn", "Bethune", "1234567890", " "));
        assertThrows(IllegalArgumentException.class,
                () -> new Contact("1", "Shawn", "Bethune", "1234567890",
                        "1234567890123456789012345678901"));
    }

    @Test
    void constructorRejectsContactIdThatIsNotSafeForAUrlPath() {
        assertThrows(IllegalArgumentException.class,
                () -> new Contact("A/B", "Shawn", "Bethune", "1234567890", "123 Main St"));
        assertThrows(IllegalArgumentException.class,
                () -> new Contact("A B", "Shawn", "Bethune", "1234567890", "123 Main St"));
        assertThrows(IllegalArgumentException.class,
                () -> new Contact("A?B", "Shawn", "Bethune", "1234567890", "123 Main St"));
    }

    @Test
    void constructorRejectsPhoneThatIsNotExactlyTenDigits() {
        assertThrows(IllegalArgumentException.class,
                () -> new Contact("1", "Shawn", "Bethune", "12345", "123 Main St"));
        assertThrows(IllegalArgumentException.class,
                () -> new Contact("1", "Shawn", "Bethune", "12345678901", "123 Main St"));
        assertThrows(IllegalArgumentException.class,
                () -> new Contact("1", "Shawn", "Bethune", "abcdefghij", "123 Main St"));
        assertThrows(IllegalArgumentException.class,
                () -> new Contact("1", "Shawn", "Bethune", null, "123 Main St"));
    }
}
