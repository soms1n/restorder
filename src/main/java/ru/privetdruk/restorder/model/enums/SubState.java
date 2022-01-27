package ru.privetdruk.restorder.model.enums;

import com.fasterxml.jackson.annotation.JsonFormat;
import ru.privetdruk.restorder.model.consts.MessageText;

@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum SubState {
    SHOW_REGISTER_BUTTON(MessageText.REGISTER) {
        @Override
        public State getState() {
            return State.REGISTRATION;
        }

        @Override
        public SubState getNextSubState() {
            return REGISTER_BUTTON_PRESS;
        }
    },
    REGISTER_BUTTON_PRESS(null) {
        @Override
        public State getState() {
            return State.REGISTRATION;
        }

        @Override
        public SubState getNextSubState() {
            return ENTER_FULL_NAME;
        }
    },
    ENTER_FULL_NAME(MessageText.ENTER_FULL_NAME) {
        @Override
        public State getState() {
            return State.REGISTRATION;
        }

        @Override
        public SubState getNextSubState() {
            return ENTER_TAVERN_NAME;
        }
    },
    ENTER_TAVERN_NAME(MessageText.ENTER_TAVERN_NAME) {
        @Override
        public State getState() {
            return State.REGISTRATION;
        }

        @Override
        public SubState getNextSubState() {
            return CHOICE_CITY;
        }
    },
    CHOICE_CITY(MessageText.CHOICE_CITY) {
        @Override
        public State getState() {
            return State.REGISTRATION;
        }

        @Override
        public SubState getNextSubState() {
            return ENTER_ADDRESS;
        }
    },
    ENTER_ADDRESS(MessageText.ENTER_ADDRESS) {
        @Override
        public State getState() {
            return State.REGISTRATION;
        }

        @Override
        public SubState getNextSubState() {
            return ENTER_PHONE_NUMBER;
        }
    },
    ENTER_PHONE_NUMBER(MessageText.ENTER_PHONE_NUMBER) {
        @Override
        public State getState() {
            return State.REGISTRATION;
        }

        @Override
        public SubState getNextSubState() {
            return REGISTRATION_APPROVING;
        }
    },
    REGISTRATION_APPROVING(null) {
        @Override
        public State getState() {
            return State.REGISTRATION;
        }

        @Override
        public SubState getNextSubState() {
            return WAITING_APPROVE_APPLICATION;
        }
    },
    REGISTRATION_EDITING(null) {
        @Override
        public State getState() {
            return State.REGISTRATION;
        }

        @Override
        public SubState getNextSubState() {
            return null;
        }
    },
    WAITING_APPROVE_APPLICATION(MessageText.WAITING_APPROVE_APPLICATION) {
        @Override
        public State getState() {
            return State.WAITING_APPROVE_APPLICATION;
        }

        @Override
        public SubState getNextSubState() {
            return VIEW_MAIN_MENU;
        }
    },
    VIEW_MAIN_MENU(null) {
        @Override
        public State getState() {
            return State.MAIN_MENU;
        }

        @Override
        public SubState getNextSubState() {
            return null;
        }
    }, EDIT_PERSONAL_DATA(null) {
        @Override
        public State getState() {
            return State.REGISTRATION;
        }

        @Override
        public SubState getNextSubState() {
            return null;
        }
    };

    private final String message;

    SubState(String message) {
        this.message = message;
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

    public String getMessage() {
        return message;
    }

    public abstract State getState();
    public abstract SubState getNextSubState();
}
