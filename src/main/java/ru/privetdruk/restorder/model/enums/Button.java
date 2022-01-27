package ru.privetdruk.restorder.model.enums;

import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.util.StringUtils;

@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum Button {
    REGISTRATION("Регистрация"),
    EDIT("Редактировать"),
    APPROVE("Утвердить"),
    NAME("Имя"),
    TAVERN("Заведение"),
    PHONE_NUMBER("Номер телефона"),
    COMPLETE_REGISTRATION("Завершить регистрацию");
    REGISTRATION("Регистрация"),
    SETTINGS("⚙ Настройки"),
    INFORMATION("\uD83D\uDCAC Информация"),
    GENERAL("\uD83D\uDEE0 Основное"),
    PROFILE("\uD83D\uDC64 Профиль"),
    EMPLOYEES("\uD83D\uDD74 Сотрудники"),
    CATEGORIES("\uD83C\uDFA8 Категории"),
    SCHEDULE("\uD83D\uDCC5 Расписание"),
    TABLES("\uD83E\uDE91 Управление столами"),
    BACK("⬅ Назад"),
    TAVERN_NAME("™ Название"),
    TAVERN_PHONES("\uD83D\uDCDE Телефоны"),
    TAVERN_ADDRESS("\uD83C\uDFE2 Адрес"),
    MAIN_MENU("↩ Главное меню"),
    CHANGE("〰 Изменить"),
    CANCEL("⭕ Отменить"),
    DELETE("❌ Удалить"),
    ADD("🟢 Добавить"),;

    private final String text;

    Button(String text) {
        this.text = text;
    }

    public static Button fromName(String name) {
        try {
            return Button.valueOf(name);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public static Button fromText(String text) {
        if (!StringUtils.hasText(text)) {
            return null;
        }

        for (Button button : Button.values()) {
            if (text.equalsIgnoreCase(button.getText())) {
                return button;
            }
        }

        return null;
    }

    public String getName() {
        return name();
    }

    public String getText() {
        return text;
    }
}
