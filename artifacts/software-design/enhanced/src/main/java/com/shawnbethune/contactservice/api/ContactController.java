package com.shawnbethune.contactservice.api;

import com.shawnbethune.contactservice.model.Contact;
import com.shawnbethune.contactservice.service.ContactService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/contacts")
public class ContactController {
    private final ContactService service;

    public ContactController(ContactService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<Contact> createContact(@Valid @RequestBody CreateContactRequest request) {
        Contact created = service.createContact(
                request.contactId(),
                request.firstName(),
                request.lastName(),
                request.phone(),
                request.address());
        return ResponseEntity
                .created(URI.create("/api/contacts/" + created.getContactId()))
                .body(created);
    }

    @GetMapping
    public List<Contact> getAllContacts() {
        return service.getAllContacts();
    }

    @GetMapping("/{contactId}")
    public Contact getContact(@PathVariable String contactId) {
        return service.getContact(contactId);
    }

    @PutMapping("/{contactId}")
    public Contact updateContact(
            @PathVariable String contactId,
            @Valid @RequestBody UpdateContactRequest request) {
        return service.updateContact(
                contactId,
                request.firstName(),
                request.lastName(),
                request.phone(),
                request.address());
    }

    @DeleteMapping("/{contactId}")
    public ResponseEntity<Void> deleteContact(@PathVariable String contactId) {
        service.deleteContact(contactId);
        return ResponseEntity.noContent().build();
    }
}
