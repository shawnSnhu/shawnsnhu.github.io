package com.shawnbethune.contactservice.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.shawnbethune.contactservice.model.Contact;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class InMemoryContactRepositoryTest {
    private InMemoryContactRepository repository;

    @BeforeEach
    void setUp() {
        repository = new InMemoryContactRepository();
    }

    @Test
    void addPreventsDuplicateIds() {
        Contact original = contact("A2", "Shawn");
        Contact duplicate = contact("A2", "Other");

        assertTrue(repository.add(original));
        assertFalse(repository.add(duplicate));
        assertEquals(original, repository.findById("A2").orElseThrow());
    }

    @Test
    void findAllReturnsSortedUnmodifiableSnapshot() {
        repository.add(contact("B2", "Second"));
        repository.add(contact("A1", "First"));

        List<Contact> result = repository.findAll();
        repository.add(contact("C3", "Third"));

        assertEquals(List.of("A1", "B2"),
                result.stream().map(Contact::getContactId).toList());
        assertThrows(UnsupportedOperationException.class,
                () -> result.add(contact("C3", "Third")));
    }

    @Test
    void updateAndDeleteReportWhetherContactExists() {
        Contact original = contact("A1", "Shawn");
        Contact replacement = contact("A1", "Updated");

        assertFalse(repository.update(replacement));
        assertTrue(repository.add(original));
        assertTrue(repository.update(replacement));
        assertEquals(replacement, repository.findById("A1").orElseThrow());
        assertTrue(repository.deleteById("A1"));
        assertFalse(repository.deleteById("A1"));
    }

    private Contact contact(String id, String firstName) {
        return new Contact(id, firstName, "Bethune", "1234567890", "123 Main St");
    }
}
