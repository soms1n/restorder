package ru.privetdruk.restorder.model.enums;

import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.util.StringUtils;

import java.util.Optional;

@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum Button {
    REGISTRATION("Регистрация"),
    EDIT("✏  Редактировать"),
    APPROVE("\uD83D\uDFE2 Утвердить"),
    NAME("\uD83D\uDC64 Имя"),
    CITY("\uD83C\uDFE0 Город"),
    EDIT_MENU("✏  Меню редактирования"),
    COMPLETE_REGISTRATION("✅ Завершить регистрацию"),
    SETTINGS("⚙ Настройки"),
    INFORMATION("\uD83D\uDCAC Информация"),
    GENERAL("\uD83D\uDEE0 Основное"),
    PROFILE("\uD83D\uDC64 Профиль"),
    EMPLOYEES("\uD83D\uDD74 Сотрудники"),
    CATEGORIES("\uD83C\uDFA8 Категории"),
    SCHEDULE("\uD83D\uDCC5 Расписание"),
    TABLES("\uD83E\uDE91 Столы"),
    BACK("⬅ Назад"),
    MORE("Далее ➡"),
    TAVERN_NAME("™ Название"),
    PHONE_NUMBER("\uD83D\uDCDE Телефон"),
    TAVERN_ADDRESS("\uD83D\uDDFA Адрес"),
    TAVERN_TABLE_LAYOUT("\uD83D\uDCC8 Схема столов"),
    RETURN_MAIN_MENU("↩ Главное меню"),
    CHANGE("〰 Изменить"),
    CANCEL("⭕ Отменить"),
    DELETE("❌ Удалить"),
    ADD("🟢 Добавить"),
    USER_NAME("™ Имя"),
    DELETE_PROFILE("❌ Удалить профиль"),
    YES("⭕ Да"),
    NO("🟢 Нет"),
    ACCEPT("\uD83D\uDFE2 Подтвердить"),
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
    CANCEL_RESERVE("❌ Отменить"),
    RESERVE_LIST("📋 Список бронирований"),
    PICK_ALL("➿ Выбрать все"),
    TODAY("\uD83D\uDDD3 Сегодня"),
    TOMORROW("\uD83D\uDDD2 Завтра"),
    WITHOUT_PHONE("\uD83D\uDD15 Без телефона"),
    ONE("1️⃣"),
    TWO("2️⃣"),
    THREE("3️⃣"),
    FOUR("4️⃣"),
    FIVE("5️⃣"),
    SIX("6️⃣"),
    SEVEN("7️⃣"),
    EIGHT("8️⃣"),
    NINE("9️⃣"),
    HOOKAH_BAR("\uD83D\uDEAC Кальянные"),
    CAFE_BAR_RESTAURANT("☕ Кафе \uD83C\uDF7A Бары \uD83E\uDD42 Рестораны"),
    NIGHT_CLUB("\uD83C\uDF78 Ночные клубы"),
    BILLIARDS("\uD83C\uDFB1 Бильярдные"),
    BOWLING("\uD83C\uDFB3 Боулинг"),
    MY_RESERVE("📋 Мои бронирования"),
    NOW("\uD83D\uDD51 Сейчас"),
    DESCRIPTION("\uD83D\uDCC3 Описание"),
    WITHOUT_DESCRIPTION("\uD83D\uDDEF Без описания");

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

    public Integer getNumber() {
        if (this == ONE) return 1;
        if (this == TWO) return 2;
        if (this == THREE) return 3;
        if (this == FOUR) return 4;
        if (this == FIVE) return 5;
        if (this == SIX) return 6;
        if (this == SEVEN) return 7;
        if (this == EIGHT) return 8;
        if (this == NINE) return 9;
        return null;
    }
}
