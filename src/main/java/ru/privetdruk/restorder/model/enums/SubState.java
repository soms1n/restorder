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
    },
    EDIT_PERSONAL_DATA(null) {
        @Override
        public State getState() {
            return State.REGISTRATION;
        }
    },
    VIEW_SETTINGS(null) {
        @Override
        public State getState() {
            return State.SETTINGS;
        }
    },
    VIEW_GENERAL_SETTINGS(null) {
        @Override
        public State getState() {
            return State.SETTINGS;
        }

        @Override
        public Button getParentButton() {
            return Button.SETTINGS;
        }
    },
    VIEW_TAVERN_NAME_GENERAL_SETTINGS(null) {
        @Override
        public State getState() {
            return State.SETTINGS;
        }

        @Override
        public Button getParentButton() {
            return Button.GENERAL;
        }
    },
    CHANGE_TAVERN_NAME_GENERAL_SETTINGS(null) {
        @Override
        public State getState() {
            return State.SETTINGS;
        }

        @Override
        public Button getParentButton() {
            return Button.GENERAL;
        }

        @Override
        public Button getAfterChangeButton() {
            return Button.TAVERN_NAME;
        }
    },
    VIEW_TAVERN_ADDRESS_GENERAL_SETTINGS(null) {
        @Override
        public State getState() {
            return State.SETTINGS;
        }

        @Override
        public Button getParentButton() {
            return Button.GENERAL;
        }
    },
    CHANGE_TAVERN_ADDRESS_GENERAL_SETTINGS(null) {
        @Override
        public State getState() {
            return State.SETTINGS;
        }

        @Override
        public Button getParentButton() {
            return Button.GENERAL;
        }

        @Override
        public Button getAfterChangeButton() {
            return Button.TAVERN_ADDRESS;
        }
    },
    VIEW_TAVERN_PHONES_GENERAL_SETTINGS(null) {
        @Override
        public State getState() {
            return State.SETTINGS;
        }

        @Override
        public Button getParentButton() {
            return Button.GENERAL;
        }
    },
    ADD_TAVERN_PHONES_GENERAL_SETTINGS(null) {
        @Override
        public State getState() {
            return State.SETTINGS;
        }

        @Override
        public Button getParentButton() {
            return Button.GENERAL;
        }

        @Override
        public Button getAfterChangeButton() {
            return Button.TAVERN_PHONES;
        }
    },
    DELETE_TAVERN_PHONES_GENERAL_SETTINGS(null) {
        @Override
        public State getState() {
            return State.SETTINGS;
        }

        @Override
        public Button getParentButton() {
            return Button.GENERAL;
        }

        @Override
        public Button getAfterChangeButton() {
            return Button.TAVERN_PHONES;
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

    public State getState() {
        return null;
    }

    public SubState getNextSubState() {
        return null;
    }

    public Button getParentButton() {
        return null;
    }

    public Button getAfterChangeButton() {
        return null;
    }
}
