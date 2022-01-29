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
            return State.REGISTRATION;
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
    EDIT_NAME(null) {
        @Override
        public State getState() {
            return State.REGISTRATION;
        }
    },
    EDIT_ADDRESS(null) {
        @Override
        public State getState() {
            return State.REGISTRATION;
        }
    },
    EDIT_CITY(null) {
        @Override
        public State getState() {
            return State.REGISTRATION;
        }
    },
    EDIT_TAVERN(null) {
        @Override
        public State getState() {
            return State.REGISTRATION;
        }
    },
    EDIT_PHONE_NUMBER(null) {
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
        public SubState getParentSubState() {
            return VIEW_SETTINGS;
        }
    },
    VIEW_GENERAL_SETTINGS_TAVERN_NAME(null) {
        @Override
        public State getState() {
            return State.SETTINGS;
        }

        @Override
        public SubState getParentSubState() {
            return VIEW_GENERAL_SETTINGS;
        }
    },
    CHANGE_GENERAL_SETTINGS_TAVERN_NAME(null) {
        @Override
        public State getState() {
            return State.SETTINGS;
        }

        @Override
        public SubState getParentSubState() {
            return VIEW_GENERAL_SETTINGS_TAVERN_NAME;
        }
    },
    VIEW_GENERAL_SETTINGS_TAVERN_ADDRESS(null) {
        @Override
        public State getState() {
            return State.SETTINGS;
        }

        @Override
        public SubState getParentSubState() {
            return VIEW_GENERAL_SETTINGS;
        }
    },
    CHANGE_GENERAL_SETTINGS_TAVERN_ADDRESS(null) {
        @Override
        public State getState() {
            return State.SETTINGS;
        }

        @Override
        public SubState getParentSubState() {
            return VIEW_GENERAL_SETTINGS_TAVERN_ADDRESS;
        }
    },
    VIEW_GENERAL_SETTINGS_TAVERN_CONTACTS(null) {
        @Override
        public State getState() {
            return State.SETTINGS;
        }

        @Override
        public SubState getParentSubState() {
            return VIEW_GENERAL_SETTINGS;
        }
    },
    ADD_GENERAL_SETTINGS_TAVERN_CONTACTS(null) {
        @Override
        public State getState() {
            return State.SETTINGS;
        }

        @Override
        public SubState getParentSubState() {
            return VIEW_GENERAL_SETTINGS_TAVERN_CONTACTS;
        }
    },
    DELETE_GENERAL_SETTINGS_TAVERN_CONTACTS(null) {
        @Override
        public State getState() {
            return State.SETTINGS;
        }

        @Override
        public SubState getParentSubState() {
            return VIEW_GENERAL_SETTINGS_TAVERN_CONTACTS;
        }
    },
    VIEW_PROFILE_SETTINGS(null) {
        @Override
        public State getState() {
            return State.SETTINGS;
        }

        @Override
        public SubState getParentSubState() {
            return VIEW_SETTINGS;
        }
    },
    VIEW_PROFILE_SETTINGS_USER_NAME(null) {
        @Override
        public State getState() {
            return State.SETTINGS;
        }

        @Override
        public SubState getParentSubState() {
            return VIEW_PROFILE_SETTINGS;
        }
    },
    CHANGE_PROFILE_SETTINGS_USER_NAME(null) {
        @Override
        public State getState() {
            return State.SETTINGS;
        }

        @Override
        public SubState getParentSubState() {
            return VIEW_PROFILE_SETTINGS_USER_NAME;
        }
    },
    VIEW_PROFILE_SETTINGS_USER_CONTACTS(null) {
        @Override
        public State getState() {
            return State.SETTINGS;
        }

        @Override
        public SubState getParentSubState() {
            return VIEW_PROFILE_SETTINGS;
        }
    },
    ADD_PROFILE_SETTINGS_USER_CONTACTS(null) {
        @Override
        public State getState() {
            return State.SETTINGS;
        }

        @Override
        public SubState getParentSubState() {
            return VIEW_PROFILE_SETTINGS_USER_CONTACTS;
        }
    },
    DELETE_PROFILE_SETTINGS_USER_CONTACTS(null) {
        @Override
        public State getState() {
            return State.SETTINGS;
        }

        @Override
        public SubState getParentSubState() {
            return VIEW_PROFILE_SETTINGS_USER_CONTACTS;
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

    public SubState getParentSubState() {
        return null;
    }
}
