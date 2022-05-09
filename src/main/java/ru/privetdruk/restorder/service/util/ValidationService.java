package ru.privetdruk.restorder.service.util;

import org.springframework.stereotype.Service;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class ValidationService {
    private final Pattern MOBILE_PHONE_PATTERN = Pattern.compile("^((8|\\+7)[\\- ]?)?(\\(?\\d{3}\\)?[\\- ]?)?[\\d\\- ]{7,10}$");

    public boolean isValidPhone(String phone) {
        Matcher matcher = MOBILE_PHONE_PATTERN.matcher(phone);

        return matcher.matches();
    }
}
