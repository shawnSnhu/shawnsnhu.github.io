package com.shawnbethune.contactservice.service;

import com.shawnbethune.contactservice.exception.ContactNotFoundException;
import com.shawnbethune.contactservice.exception.DuplicateContactException;
import com.shawnbethune.contactservice.model.Contact;
import com.shawnbethune.contactservice.repository.ContactRepository;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class ContactService {
    private final ContactRepository repository;

    public ContactService(ContactRepository repository) {
        this.repository = repository;
    }

    public Contact createContact(
            String contactId,
            String firstName,
            String lastName,
            String phone,
            String address) {
        Contact contact = new Contact(contactId, firstName, lastName, phone, address);
        if (!repository.add(contact)) {
            throw new DuplicateContactException(contactId);
        }
        return contact;
    }

    public Contact getContact(String contactId) {
        Contact.validateContactId(contactId);
        return repository.findById(contactId)
                .orElseThrow(() -> new ContactNotFoundException(contactId));
    }

    public List<Contact> getAllContacts() {
        return repository.findAll();
    }

    public Contact updateContact(
            String contactId,
            String firstName,
            String lastName,
            String phone,
            String address) {
        // Construct and validate the complete replacement before storage changes.
        Contact replacement = new Contact(contactId, firstName, lastName, phone, address);
        if (!repository.update(replacement)) {
            throw new ContactNotFoundException(contactId);
        }
        return replacement;
    }

    public void deleteContact(String contactId) {
        Contact.validateContactId(contactId);
        if (!repository.deleteById(contactId)) {
            throw new ContactNotFoundException(contactId);
        }
    }
}
