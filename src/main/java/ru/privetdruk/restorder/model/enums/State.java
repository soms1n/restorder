package ru.privetdruk.restorder.model.enums;

import com.fasterxml.jackson.annotation.JsonFormat;

@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum State {
    REGISTRATION(SubState.SHOW_REGISTER_BUTTON),
    MAIN_MENU(SubState.VIEW_MAIN_MENU),
    SETTINGS(SubState.VIEW_SETTINGS);

    private final SubState initialSubState;

    State(SubState initialSubState) {
        this.initialSubState = initialSubState;
    }

    public static State fromName(String name) {
        try {
            return State.valueOf(name);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public String getName() {
        return name();
    }

    public SubState getInitialSubState() {
        return initialSubState;
    }
}