package contact;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ContactServiceTest {
    private ContactService service;

    @BeforeEach
    void setUp() {
        service = new ContactService();
    }

    @Test
    void testAddAndDelete() {
        Contact contact = new Contact("1", "Shawn", "Bethune", "1234567890", "123 Main St");

        service.addContact(contact);
        service.deleteContact("1");

        assertFalse(service.getContacts().containsKey("1"));
    }

    @Test
    void testAddDuplicateId_throwsException() {
        Contact contact1 = new Contact("1", "Shawn", "Bethune", "1234567890", "123 Main St");
        Contact contact2 = new Contact("1", "Other", "Person", "0987654321", "456 New St");

        service.addContact(contact1);

        assertThrows(IllegalArgumentException.class, () ->
            service.addContact(contact2)
        );
    }

    @Test
    void testUpdateContactValidValues_updatesSuccessfully() {
        Contact contact = new Contact("1", "Shawn", "Bethune", "1234567890", "123 Main St");
        service.addContact(contact);

        service.updateContact("1", "NewFirst", "NewLast", "0987654321", "456 New St");

        Contact updated = service.getContacts().get("1");
        assertEquals("1", updated.getContactId());
        assertEquals("NewFirst", updated.getFirstName());
        assertEquals("NewLast", updated.getLastName());
        assertEquals("0987654321", updated.getPhone());
        assertEquals("456 New St", updated.getAddress());
    }

    @Test
    void testServiceNegativePaths() {
        // unknown IDs
        assertThrows(IllegalArgumentException.class, () -> service.deleteContact("NONE"));
        assertThrows(IllegalArgumentException.class, () -> service.updateContact("NONE", "S", "B", "1234567890", "Addy"));
    }

    @Test
    void testInvalidUpdateViaService() {
        service.addContact(new Contact("1", "Shawn", "Bethune", "1234567890", "123 Main St"));

        // verify setters catch bad data
        assertThrows(IllegalArgumentException.class, () ->
            service.updateContact("1", "Shawn_Too_Long", "B", "123", "Addy")
        );
    }

    @Test
    void testUpdateNullPhone_throwsException() {
        service.addContact(new Contact("1", "Shawn", "Bethune", "1234567890", "123 Main St"));

        assertThrows(IllegalArgumentException.class, () ->
            service.updateContact("1", "Shawn", "Bethune", null, "123 Main St")
        );
    }
}