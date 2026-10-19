package com.shawnbethune.contactservice.repository;

import com.shawnbethune.contactservice.model.Contact;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.springframework.stereotype.Repository;

@Repository
public class InMemoryContactRepository implements ContactRepository {
    private final ConcurrentMap<String, Contact> contacts = new ConcurrentHashMap<>();

    @Override
    public boolean add(Contact contact) {
        return contacts.putIfAbsent(contact.getContactId(), contact) == null;
    }

    @Override
    public Optional<Contact> findById(String contactId) {
        return Optional.ofNullable(contacts.get(contactId));
    }

    @Override
    public List<Contact> findAll() {
        // Return a sorted snapshot instead of exposing the concurrent map itself.
        return contacts.values().stream()
                .sorted(Comparator.comparing(Contact::getContactId))
                .toList();
    }

    @Override
    public boolean update(Contact contact) {
        return contacts.replace(contact.getContactId(), contact) != null;
    }

    @Override
    public boolean deleteById(String contactId) {
        return contacts.remove(contactId) != null;
    }
}
