package ru.privetdruk.restorder.service;

import org.springframework.stereotype.Service;

@Service
public class ValidationService {
    public boolean isValidPhone(String phone) {
        return true;
    }
}
