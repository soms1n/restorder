package ru.privetdruk.restorder.service;

import org.springframework.stereotype.Service;

@Service
public class TypeService {
    public boolean isInteger(String value) {
        try {
            Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return false;
        }

        return true;
    }

    public boolean isDouble(String value) {
        try {
            Double.parseDouble(value);
        } catch (NumberFormatException e) {
            return false;
        }

        return true;
    }
}
