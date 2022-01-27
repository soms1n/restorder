package ru.privetdruk.restorder.model.enums;

import com.fasterxml.jackson.annotation.JsonFormat;

@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum Button {
    REGISTRATION("Регистрация"),
    EDIT("Редактировать"),
    APPROVE("Утвердить"),
    NAME("Имя"),
    TAVERN("Заведение"),
    PHONE_NUMBER("Номер телефона"),
    COMPLETE_REGISTRATION("Завершить регистрацию");

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

    public String getName() {
        return name();
    }

    public String getText() {
        return text;
    }
}
