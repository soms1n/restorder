package ru.privetdruk.restorder.model.enums;

import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * Типы контактов
 */
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum ContractType {
    EMAIL("Электронная почта"),
    MOBILE("Мобильный телефон");

    private final String description;

    ContractType(String description) {
        this.description = description;
    }

    public static ContractType fromName(String name) {
        try {
            return ContractType.valueOf(name);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public String getName() {
        return name();
    }

    public String getDescription() {
        return description;
    }
}
