package ru.privetdruk.restorder.model.enums;

import com.fasterxml.jackson.annotation.JsonFormat;
import ru.privetdruk.restorder.model.consts.MessageText;

@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum SubState {
    ENTER_FULL_NAME(MessageText.ENTER_FULL_NAME),
    ENTER_TAVERN_NAME(MessageText.ENTER_TAVERN_NAME),
    CHOICE_CITY(MessageText.CHOICE_CITY),
    ENTER_ADDRESS(MessageText.ENTER_ADDRESS),
    ENTER_PHONE_NUMBER(MessageText.ENTER_PHONE_NUMBER);

    private final String text;

    SubState(String text) {
        this.text = text;
    }

    public static SubState fromName(String name) {
        try {
            return SubState.valueOf(name);
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
