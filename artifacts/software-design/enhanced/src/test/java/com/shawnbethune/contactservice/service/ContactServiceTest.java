package com.shawnbethune.contactservice.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.shawnbethune.contactservice.exception.ContactNotFoundException;
import com.shawnbethune.contactservice.exception.DuplicateContactException;
import com.shawnbethune.contactservice.model.Contact;
import com.shawnbethune.contactservice.repository.InMemoryContactRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ContactServiceTest {
    private ContactService service;

    @BeforeEach
    void setUp() {
        service = new ContactService(new InMemoryContactRepository());
    }

    @Test
    void createGetUpdateAndDeleteContact() {
        service.createContact("1", "Shawn", "Bethune", "1234567890", "123 Main St");

        Contact updated = service.updateContact(
                "1", "Updated", "Person", "0987654321", "456 New St");

        assertEquals("Updated", updated.getFirstName());
        assertEquals(updated, service.getContact("1"));
        assertEquals(1, service.getAllContacts().size());

        service.deleteContact("1");
        assertThrows(ContactNotFoundException.class, () -> service.getContact("1"));
    }

    @Test
    void createRejectsDuplicateId() {
        service.createContact("1", "Shawn", "Bethune", "1234567890", "123 Main St");

        assertThrows(DuplicateContactException.class, () ->
                service.createContact("1", "Other", "Person", "0987654321", "456 New St"));
    }

    @Test
    void missingContactOperationsThrowNotFound() {
        assertThrows(ContactNotFoundException.class, () -> service.getContact("NONE"));
        assertThrows(ContactNotFoundException.class, () -> service.updateContact(
                "NONE", "Shawn", "Bethune", "1234567890", "123 Main St"));
        assertThrows(ContactNotFoundException.class, () -> service.deleteContact("NONE"));
    }

    @Test
    void contactIdValidationIsConsistentAcrossServiceOperations() {
        assertThrows(IllegalArgumentException.class, () -> service.getContact(null));
        assertThrows(IllegalArgumentException.class, () -> service.getContact("A/B"));
        assertThrows(IllegalArgumentException.class, () -> service.deleteContact(" "));
    }

    @Test
    void invalidUpdateDoesNotPartiallyChangeStoredContact() {
        Contact original = service.createContact(
                "1", "Shawn", "Bethune", "1234567890", "123 Main St");

        assertThrows(IllegalArgumentException.class, () -> service.updateContact(
                "1", "Changed", "Person", "abcdefghij", "456 New St"));

        Contact stored = service.getContact("1");
        assertEquals(original, stored);
        assertEquals("Shawn", stored.getFirstName());
        assertEquals("Bethune", stored.getLastName());
        assertEquals("1234567890", stored.getPhone());
        assertEquals("123 Main St", stored.getAddress());
    }
}
