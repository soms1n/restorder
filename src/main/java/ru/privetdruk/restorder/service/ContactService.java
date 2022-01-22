package ru.privetdruk.restorder.service;

import org.springframework.stereotype.Service;
import ru.privetdruk.restorder.repository.ContactRepository;

@Service
public class ContactService {
    private final ContactRepository contactRepository;

    public ContactService(ContactRepository contactRepository) {
        this.contactRepository = contactRepository;
    }
}
