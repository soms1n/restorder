package ru.privetdruk.restorder.model.enums;

import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.util.StringUtils;

import java.util.Optional;

@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum Button {
    REGISTRATION("Регистрация"),
    EDIT("Редактировать"),
    APPROVE("Утвердить"),
    NAME("Имя"),
    TAVERN("Заведение"),
    ADDRESS("Адрес"),
    CITY("Город"),
    EDIT_MENU("Меню редактирования"),
    PHONE_NUMBER("Номер телефона"),
    COMPLETE_REGISTRATION("Завершить регистрацию"),
    SETTINGS("⚙ Настройки"),
    INFORMATION("\uD83D\uDCAC Информация"),
    GENERAL("\uD83D\uDEE0 Основное"),
    PROFILE("\uD83D\uDC64 Профиль"),
    EMPLOYEES("\uD83D\uDD74 Сотрудники"),
    CATEGORIES("\uD83C\uDFA8 Категории"),
    SCHEDULE("\uD83D\uDCC5 Расписание"),
    TABLES("\uD83E\uDE91 Столы"),
    BACK("⬅ Назад"),
    TAVERN_NAME("™ Название"),
    CONTACTS("\uD83D\uDCDE Телефоны"),
    TAVERN_ADDRESS("\uD83C\uDFE2 Адрес"),
    RETURN_MAIN_MENU("↩ Главное меню"),
    CHANGE("〰 Изменить"),
    CANCEL("⭕ Отменить"),
    DELETE("❌ Удалить"),
    ADD("🟢 Добавить"),
    USER_NAME("™ Имя"),
    DELETE_PROFILE("❌ Удалить профиль"),
    YES("⭕ Да"),
    NO("🟢 Нет"),
    REGISTRATION_ACCEPT("Подтвердить регистрацию"),
    SHARE_PHONE("\uD83D\uDCDE Поделиться номером"),
    NOTHING(null),
    MONDAY("Понедельник"),
    TUESDAY("Вторник"),
    WEDNESDAY("Среда"),
    THURSDAY("Четверг"),
    FRIDAY("Пятница"),
    SATURDAY("Суббота"),
    SUNDAY("Воскресенье"),
    SELECT_RANGE("\uD83D\uDCC5 Выбрать диапазон"),
    WEEKDAYS("⚪ Будни"),
    WEEKENDS("⚫ Выходные"),
    FREE("Бесплатно"),
    RESERVE("✏  Забронировать"),
    DELETE_RESERVE("\uD83D\uDD34 Убрать бронь"),
    RESERVE_LIST("📋 Список бронирований"),
    PICK_ALL("➿ Выбрать все"),
    TODAY("\uD83D\uDDD3 Сегодня"),
    TOMORROW("\uD83D\uDDD2 Завтра");

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

    public static Optional<Button> fromText(String text) {
        if (!StringUtils.hasText(text)) {
            return Optional.empty();
        }

        for (Button button : Button.values()) {
            if (text.equalsIgnoreCase(button.getText())) {
                return Optional.of(button);
            }
        }

        return Optional.empty();
    }

    public String getName() {
        return name();
    }

    public String getText() {
        return text;
    }
}
