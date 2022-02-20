package ru.privetdruk.restorder.model.enums;

import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.util.StringUtils;

/**
 * Категории заведений
 */
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum Category {
    CAFE("Кафе"),
    NIGHT_CLUB("Ночной клуб"),
    BILLIARDS("Бильярд"),
    BOWLING("Боулинг"),
    BAR("Бар"),
    HOOKAH_BAR("Кальянная"),
    RESTAURANT("Ресторан");

    private final String description;

    Category(String description) {
        this.description = description;
    }

    public static Category fromName(String name) {
        try {
            return Category.valueOf(name);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public static Category fromDescription(String description) {
        if (!StringUtils.hasText(description)) {
            return null;
        }

        for (Category category : Category.values()) {
            if (description.equalsIgnoreCase(category.getDescription())) {
                return category;
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
