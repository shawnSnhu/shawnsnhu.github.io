package contact;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

class ContactTest {

    @Test
    void testContactClassSuccess() {
        Contact contact = new Contact("12345", "Shawn", "Bethune", "1234567890", "123 Main St");
        assertEquals("12345", contact.getContactId());
        assertEquals("Shawn", contact.getFirstName());
        assertEquals("Bethune", contact.getLastName());
        assertEquals("1234567890", contact.getPhone());
        assertEquals("123 Main St", contact.getAddress());
    }

    @Test
    void testContactBoundaryCases() {
        // 10/10/30 chars should be accepted
        Contact contact = new Contact(
                "ID_TEN_CHR",
                "Shawn12345",
                "Bethune123",
                "1234567890",
                "123456789012345678901234567890"
        );

        assertEquals(10, contact.getContactId().length());
        assertEquals(10, contact.getFirstName().length());
        assertEquals(10, contact.getLastName().length());
        assertEquals(10, contact.getPhone().length());
        assertEquals(30, contact.getAddress().length());
    }

    @Test
    void testBoundaryFailures() {
        // rejects 11 char ID and 31 char address
        assertThrows(IllegalArgumentException.class, () ->
            new Contact("12345678901", "Shawn", "Bethune", "1234567890", "123 Main St")
        );

        assertThrows(IllegalArgumentException.class, () ->
            new Contact("123", "Shawn", "Bethune", "1234567890", "longer than thirty character address test")
        );
    }

    @Test
    void testConstructorNullFirstName_throwsException() {
        assertThrows(IllegalArgumentException.class, () ->
            new Contact("123", null, "Bethune", "1234567890", "123 Main St")
        );
    }

    @Test
    void testConstructorNullLastName_throwsException() {
        assertThrows(IllegalArgumentException.class, () ->
            new Contact("123", "Shawn", null, "1234567890", "123 Main St")
        );
    }

    @Test
    void testConstructorNullPhone_throwsException() {
        assertThrows(IllegalArgumentException.class, () ->
            new Contact("123", "Shawn", "Bethune", null, "123 Main St")
        );
    }

    @Test
    void testConstructorNullAddress_throwsException() {
        assertThrows(IllegalArgumentException.class, () ->
            new Contact("123", "Shawn", "Bethune", "1234567890", null)
        );
    }

    @Test
    void testConstructorLastNameTooLong_throwsException() {
        assertThrows(IllegalArgumentException.class, () ->
            new Contact("123", "Shawn", "BethuneTooLong", "1234567890", "123 Main St")
        );
    }

    @Test
    void testConstructorPhoneNot10Digits_throwsException() {
        assertThrows(IllegalArgumentException.class, () ->
            new Contact("123", "Shawn", "Bethune", "12345", "123 Main St")
        );
    }

    @Test
    void testContactIdNotUpdatable_idStaysSameAfterFieldUpdates() {
        Contact contact = new Contact("ABC", "Shawn", "Bethune", "1234567890", "123 Main St");

        contact.setFirstName("NewName");
        contact.setLastName("NewLast");
        contact.setPhone("0987654321");
        contact.setAddress("456 New St");

        assertEquals("ABC", contact.getContactId());
    }
}