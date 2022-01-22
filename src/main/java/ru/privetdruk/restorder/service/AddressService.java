package ru.privetdruk.restorder.service;

import org.springframework.stereotype.Service;
import ru.privetdruk.restorder.repository.AddressRepository;

@Service
public class AddressService {
    private final AddressRepository addressRepository;

    public AddressService(AddressRepository addressRepository) {
        this.addressRepository = addressRepository;
    }
}
