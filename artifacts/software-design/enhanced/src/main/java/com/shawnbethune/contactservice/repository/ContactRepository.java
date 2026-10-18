package com.shawnbethune.contactservice.repository;

import com.shawnbethune.contactservice.model.Contact;
import java.util.List;
import java.util.Optional;

public interface ContactRepository {
    boolean add(Contact contact);

    Optional<Contact> findById(String contactId);

    List<Contact> findAll();

    boolean update(Contact contact);

    boolean deleteById(String contactId);
}
