package ru.privetdruk.restorder.model.enums;

/**
 * Типы событий
 */
public enum EventType {
    REGISTER_EMPLOYEE(State.REGISTRATION_EMPLOYEE, SubState.REGISTER_EMPLOYEE_BUTTON_PRESS);

    private final State state;
    private final SubState subState;

    EventType(State state, SubState subState) {
        this.state = state;
        this.subState = subState;
    }

    public State getState() {
        return state;
    }

    public SubState getSubState() {
        return subState;
    }
}
