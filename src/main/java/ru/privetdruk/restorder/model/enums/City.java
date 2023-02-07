package ru.privetdruk.restorder.model.enums;

import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.util.StringUtils;

/**
 * Города
 */
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum City {
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

    public static City fromDescription(String description) {
        if (!StringUtils.hasText(description)) {
            return null;
        }

        for (City city : City.values()) {
            if (description.equalsIgnoreCase(city.getDescription())) {
                return city;
            }
        }

        return null;
    }

    public String getName() {
        return name();
    }

    public String getDescription() {
        return description;
    }
}
