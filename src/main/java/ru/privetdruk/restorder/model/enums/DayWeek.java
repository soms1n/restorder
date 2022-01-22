package ru.privetdruk.restorder.model.enums;

import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * День недели
 */
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum DayWeek {
    MONDAY("Понедельник", "Пн"),
    TUESDAY("Вторник", "Вт"),
    WEDNESDAY("Среда", "Ср"),
    THURSDAY("Четверг", "Чт"),
    FRIDAY("Пятница", "Пт"),
    SATURDAY("Суббота", "Сб"),
    SUNDAY("Воскресенье", "Вс");

    private final String fullName;
    private final String shortName;

    DayWeek(String fullName, String shortName) {
        this.fullName = fullName;
        this.shortName = shortName;
    }

    public static DayWeek fromName(String name) {
        try {
            return DayWeek.valueOf(name);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public String getName() {
        return name();
    }

    public String getFullName() {
        return fullName;
    }

    public String getShortName() {
        return shortName;
    }
}
