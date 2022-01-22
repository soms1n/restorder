package ru.privetdruk.restorder.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.privetdruk.restorder.repository.ContactRepository;

@Service
@RequiredArgsConstructor
public class ContactService {
    private final ContactRepository contactRepository;
}
