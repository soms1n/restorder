package ru.privetdruk.restorder.model.enums;

import com.fasterxml.jackson.annotation.JsonFormat;

@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum State {
    REGISTRATION(SubState.ENTER_FULL_NAME);

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