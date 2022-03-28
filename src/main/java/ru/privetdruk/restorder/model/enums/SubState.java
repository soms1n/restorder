package ru.privetdruk.restorder.model.enums;

import com.fasterxml.jackson.annotation.JsonFormat;
import ru.privetdruk.restorder.model.consts.MessageText;

@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum SubState {
    EVENT_INITIAL(null),
    SHOW_REGISTER_BUTTON(MessageText.REGISTER) {
        @Override
        public State getState() {
            return State.REGISTRATION_TAVERN;
        }

        @Override
        public SubState getNextSubState() {
            return REGISTER_BUTTON_PRESS;
        }
    },
    REGISTER_BUTTON_PRESS(null) {
        @Override
        public State getState() {
            return State.REGISTRATION_TAVERN;
        }

        @Override
        public SubState getNextSubState() {
            return ENTER_FULL_NAME;
        }
    },
    ENTER_FULL_NAME(MessageText.ENTER_FULL_NAME) {
        @Override
        public State getState() {
            return State.REGISTRATION_TAVERN;
        }

        @Override
        public SubState getNextSubState() {
            return ENTER_TAVERN_NAME;
        }
    },
    ENTER_TAVERN_NAME(MessageText.ENTER_TAVERN_NAME) {
        @Override
        public State getState() {
            return State.REGISTRATION_TAVERN;
        }

        @Override
        public SubState getNextSubState() {
            return CHOICE_CITY;
        }
    },
    CHOICE_CITY(MessageText.CHOICE_CITY) {
        @Override
        public State getState() {
            return State.REGISTRATION_TAVERN;
        }

        @Override
        public SubState getNextSubState() {
            return ENTER_ADDRESS;
        }
    },
    ENTER_ADDRESS(MessageText.ENTER_ADDRESS) {
        @Override
        public State getState() {
            return State.REGISTRATION_TAVERN;
        }

        @Override
        public SubState getNextSubState() {
            return ENTER_PHONE_NUMBER;
        }
    },
    ENTER_PHONE_NUMBER(MessageText.ENTER_PHONE_NUMBER) {
        @Override
        public State getState() {
            return State.REGISTRATION_TAVERN;
        }

        @Override
        public SubState getNextSubState() {
            return REGISTRATION_APPROVING;
        }
    },
    REGISTRATION_APPROVING(null) {
        @Override
        public State getState() {
            return State.REGISTRATION_TAVERN;
        }

        @Override
        public SubState getNextSubState() {
            return WAITING_APPROVE_APPLICATION;
        }
    },
    REGISTRATION_EDITING(null) {
        @Override
        public State getState() {
            return State.REGISTRATION_TAVERN;
        }
    },
    WAITING_APPROVE_APPLICATION(MessageText.WAITING_APPROVE_APPLICATION) {
        @Override
        public State getState() {
            return State.REGISTRATION_TAVERN;
        }

        @Override
        public SubState getNextSubState() {
            return VIEW_MAIN_MENU;
        }
    },
    VIEW_MAIN_MENU("Переход в главное меню.") {
        @Override
        public State getState() {
            return State.MAIN_MENU;
        }
    },
    EDIT_PERSONAL_DATA(null) {
        @Override
        public State getState() {
            return State.REGISTRATION_TAVERN;
        }
    },
    EDIT_NAME(null) {
        @Override
        public State getState() {
            return State.REGISTRATION_TAVERN;
        }
    },
    EDIT_ADDRESS(null) {
        @Override
        public State getState() {
            return State.REGISTRATION_TAVERN;
        }
    },
    EDIT_CITY(null) {
        @Override
        public State getState() {
            return State.REGISTRATION_TAVERN;
        }
    },
    EDIT_TAVERN(null) {
        @Override
        public State getState() {
            return State.REGISTRATION_TAVERN;
        }
    },
    EDIT_PHONE_NUMBER(null) {
        @Override
        public State getState() {
            return State.REGISTRATION_TAVERN;
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
    },
    DELETE_PROFILE_SETTINGS(null) {
        @Override
        public State getState() {
            return State.SETTINGS;
        }

        @Override
        public SubState getParentSubState() {
            return VIEW_PROFILE_SETTINGS;
        }
    },
    VIEW_EMPLOYEE_SETTINGS(null) {
        @Override
        public State getState() {
            return State.SETTINGS;
        }

        @Override
        public SubState getParentSubState() {
            return VIEW_SETTINGS;
        }
    },
    DELETE_EMPLOYEE_SETTINGS(null) {
        @Override
        public State getState() {
            return State.SETTINGS;
        }

        @Override
        public SubState getParentSubState() {
            return VIEW_EMPLOYEE_SETTINGS;
        }
    },
    REGISTER_EMPLOYEE_BUTTON_PRESS(null) {
        @Override
        public State getState() {
            return State.REGISTRATION_EMPLOYEE;
        }

        @Override
        public SubState getNextSubState() {
            return ENTER_EMPLOYEE_FULL_NAME;
        }
    },
    ENTER_EMPLOYEE_FULL_NAME(MessageText.ENTER_FULL_NAME) {
        @Override
        public State getState() {
            return State.REGISTRATION_EMPLOYEE;
        }

        @Override
        public SubState getNextSubState() {
            return ENTER_EMPLOYEE_PHONE_NUMBER;
        }
    },
    ENTER_EMPLOYEE_PHONE_NUMBER(MessageText.ENTER_PHONE_NUMBER) {
        @Override
        public State getState() {
            return State.REGISTRATION_EMPLOYEE;
        }

        @Override
        public SubState getNextSubState() {
            return VIEW_MAIN_MENU;
        }
    },
    VIEW_GENERAL_SETTINGS_CATEGORIES(null) {
        @Override
        public State getState() {
            return State.SETTINGS;
        }

        @Override
        public SubState getParentSubState() {
            return VIEW_GENERAL_SETTINGS;
        }
    },
    CHANGE_GENERAL_SETTINGS_CATEGORIES(null) {
        @Override
        public State getState() {
            return State.SETTINGS;
        }

        @Override
        public SubState getParentSubState() {
            return VIEW_GENERAL_SETTINGS_CATEGORIES;
        }
    },
    VIEW_SCHEDULE_SETTINGS(null) {
        @Override
        public State getState() {
            return State.SETTINGS;
        }

        @Override
        public SubState getParentSubState() {
            return VIEW_SETTINGS;
        }
    },
    DELETE_SCHEDULE_SETTINGS(null) {
        @Override
        public State getState() {
            return State.SETTINGS;
        }

        @Override
        public SubState getParentSubState() {
            return VIEW_SCHEDULE_SETTINGS;
        }
    },
    ADD_DAY_WEEK_SCHEDULE_SETTINGS(null) {
        @Override
        public State getState() {
            return State.SETTINGS;
        }

        @Override
        public SubState getParentSubState() {
            return VIEW_SCHEDULE_SETTINGS;
        }
    },
    ADD_START_HOUR_SCHEDULE_SETTINGS(null) {
        @Override
        public State getState() {
            return State.SETTINGS;
        }

        @Override
        public SubState getParentSubState() {
            return VIEW_SCHEDULE_SETTINGS;
        }
    },
    ADD_START_MINUTE_SCHEDULE_SETTINGS(null) {
        @Override
        public State getState() {
            return State.SETTINGS;
        }

        @Override
        public SubState getParentSubState() {
            return VIEW_SCHEDULE_SETTINGS;
        }
    },
    ADD_END_HOUR_SCHEDULE_SETTINGS(null) {
        @Override
        public State getState() {
            return State.SETTINGS;
        }

        @Override
        public SubState getParentSubState() {
            return VIEW_SCHEDULE_SETTINGS;
        }
    },
    ADD_END_MINUTE_SCHEDULE_SETTINGS(null) {
        @Override
        public State getState() {
            return State.SETTINGS;
        }

        @Override
        public SubState getParentSubState() {
            return VIEW_SCHEDULE_SETTINGS;
        }
    },
    ADD_PRICE_SCHEDULE_SETTINGS(null) {
        @Override
        public State getState() {
            return State.SETTINGS;
        }

        @Override
        public SubState getParentSubState() {
            return VIEW_SCHEDULE_SETTINGS;
        }
    },
    VIEW_TABLE_SETTINGS(null) {
        @Override
        public State getState() {
            return State.SETTINGS;
        }

        @Override
        public SubState getParentSubState() {
            return VIEW_SETTINGS;
        }
    },
    DELETE_TABLE_SETTINGS(null) {
        @Override
        public State getState() {
            return State.SETTINGS;
        }

        @Override
        public SubState getParentSubState() {
            return VIEW_TABLE_SETTINGS;
        }
    },
    ADD_LABEL_TABLE_SETTINGS(null) {
        @Override
        public State getState() {
            return State.SETTINGS;
        }

        @Override
        public SubState getParentSubState() {
            return VIEW_TABLE_SETTINGS;
        }
    },
    ADD_NUMBER_SEATS_TABLE_SETTINGS(null) {
        @Override
        public State getState() {
            return State.SETTINGS;
        }

        @Override
        public SubState getParentSubState() {
            return VIEW_TABLE_SETTINGS;
        }
    },
    VIEW_RESERVE_LIST(null) {
        @Override
        public State getState() {
            return State.RESERVE;
        }

        @Override
        public SubState getParentSubState() {
            return VIEW_MAIN_MENU;
        }
    },
    DELETE_RESERVE_CHOICE_DATE(null) {
        @Override
        public State getState() {
            return State.RESERVE;
        }

        @Override
        public SubState getParentSubState() {
            return VIEW_RESERVE_LIST;
        }
    },
    DELETE_RESERVE_CHOICE_TABLE(null) {
        @Override
        public State getState() {
            return State.RESERVE;
        }

        @Override
        public SubState getParentSubState() {
            return VIEW_RESERVE_LIST;
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
