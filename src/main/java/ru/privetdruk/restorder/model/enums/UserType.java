package ru.privetdruk.restorder.model.enums;

import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * Типы пользователей
 */
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum UserType {
    ADMIN("Администратор"),
    CLIENT("Клиент"),
    USER("Пользователь");

    private final String description;

    UserType(String description) {
        this.description = description;
    }

    public static UserType fromName(String name) {
        try {
            return UserType.valueOf(name);
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
