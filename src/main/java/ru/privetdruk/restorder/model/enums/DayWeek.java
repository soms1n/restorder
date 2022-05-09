package ru.privetdruk.restorder.model.enums;

import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.util.StringUtils;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;

/**
 * День недели
 */
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum DayWeek {
    MONDAY(DayOfWeek.MONDAY, "Понедельник", "Пн"),
    TUESDAY(DayOfWeek.TUESDAY, "Вторник", "Вт"),
    WEDNESDAY(DayOfWeek.WEDNESDAY, "Среда", "Ср"),
    THURSDAY(DayOfWeek.THURSDAY, "Четверг", "Чт"),
    FRIDAY(DayOfWeek.FRIDAY, "Пятница", "Пт"),
    SATURDAY(DayOfWeek.SATURDAY, "Суббота", "Сб"),
    SUNDAY(DayOfWeek.SUNDAY, "Воскресенье", "Вс");

    public static final List<DayWeek> SORTED_DAY_WEEK_LIST = List.of(MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY, SUNDAY);
    public static final List<DayWeek> WEEKDAYS_LIST = List.of(MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY);
    public static final List<DayWeek> WEEKENDS_LIST = List.of(SATURDAY, SUNDAY);

    private final DayOfWeek dayOfWeek;
    private final String fullName;
    private final String shortName;

    DayWeek(DayOfWeek dayOfWeek, String fullName, String shortName) {
        this.dayOfWeek = dayOfWeek;
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

    public static DayWeek fromDate(LocalDate date) {
        if (date == null) {
            return null;
        }

        for (DayWeek dayWeek : DayWeek.values()) {
            if (date.getDayOfWeek() == dayWeek.getDayOfWeek()) {
                return dayWeek;
            }
        }

        return null;
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

    public DayOfWeek getDayOfWeek() {
        return dayOfWeek;
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
