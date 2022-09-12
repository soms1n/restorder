package ru.privetdruk.restorder.model.enums;

import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * Роли пользователей
 */
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum Role {
    ADMIN("Создатель"),
    CLIENT_ADMIN("Управляющий"),
    CLIENT_EMPLOYEE("Администратор"),
    USER("Пользователь");

    private final String description;

    Role(String description) {
        this.description = description;
    }

    public static Role fromName(String name) {
        try {
            return Role.valueOf(name);
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
