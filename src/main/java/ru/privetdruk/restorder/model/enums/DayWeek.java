package ru.privetdruk.restorder.model.enums;

import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.util.StringUtils;

import java.util.List;

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

    public static final List<DayWeek> SORTED_DAY_WEEK_LIST = List.of(MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY, SUNDAY);
    public static final List<DayWeek> WEEKDAYS_LIST = List.of(MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY);
    public static final List<DayWeek> WEEKENDS_LIST = List.of(SATURDAY, SUNDAY);

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

    public static DayWeek fromFullName(String fullName) {
        if (!StringUtils.hasText(fullName)) {
            return null;
        }

        for (DayWeek dayWeek : DayWeek.values()) {
            if (fullName.equalsIgnoreCase(dayWeek.getFullName())) {
                return dayWeek;
            }
        }

        return null;
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
