package ru.privetdruk.restorder.model.enums;

import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * Города
 */
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum City {
    BRYANSK("Брянск"),
    YOSHKAR_OLA("Йошкар-Ола");

    private final String description;

    City(String description) {
        this.description = description;
    }

    public static City fromName(String name) {
        try {
            return City.valueOf(name);
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
