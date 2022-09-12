package ru.privetdruk.restorder.model.enums;

import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.util.StringUtils;

/**
 * Категории заведений
 */
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum Category {
    CAFE_BAR_RESTAURANT("Кафе - Бар - Ресторан", Button.CAFE_BAR_RESTAURANT),
    NIGHT_CLUB("Ночной клуб", Button.NIGHT_CLUB),
    BILLIARDS("Бильярд", Button.BILLIARDS),
    BOWLING("Боулинг", Button.BOWLING),
    HOOKAH_BAR("Кальянная", Button.HOOKAH_BAR);

    private final String description;
    private final Button button;

    Category(String description, Button button) {
        this.description = description;
        this.button = button;
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

    public static Category fromButton(Button button) {
        if (button == null) {
            return null;
        }

        for (Category category : Category.values()) {
            if (button == category.getButton()) {
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

    public Button getButton() {
        return button;
    }
}
