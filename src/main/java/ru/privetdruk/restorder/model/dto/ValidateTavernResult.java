package ru.privetdruk.restorder.model.dto;

import lombok.Getter;
import lombok.Setter;
import org.springframework.util.CollectionUtils;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Результат валидации заведения
 */
@Getter
@Setter
public class ValidateTavernResult {
    private boolean valid = false;
    private Set<String> messages;

    public void addMessage(String message) {
        if (CollectionUtils.isEmpty(messages)) {
            messages = new HashSet<>();
        }

        messages.add(message);
    }

    public boolean isValid() {
        return valid;
    }

    public String printMessages() {
        if (CollectionUtils.isEmpty(messages)) {
            return "";
        }

        return messages.stream()
                .collect(Collectors.joining(System.lineSeparator()));
    }
}
