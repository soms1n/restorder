package ru.privetdruk.restorder.model.enums;

import com.fasterxml.jackson.annotation.JsonFormat;
import ru.privetdruk.restorder.model.consts.MessageText;

@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum SubState {
    EVENT_INITIAL(),
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
    REGISTER_BUTTON_PRESS() {
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
    REGISTRATION_APPROVING() {
        @Override
        public State getState() {
            return State.REGISTRATION_TAVERN;
        }

        @Override
        public SubState getNextSubState() {
            return WAITING_APPROVE_APPLICATION;
        }
    },
    REGISTRATION_EDITING() {
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
    EDIT_PERSONAL_DATA() {
        @Override
        public State getState() {
            return State.REGISTRATION_TAVERN;
        }
    },
    EDIT_NAME() {
        @Override
        public State getState() {
            return State.REGISTRATION_TAVERN;
        }
    },
    EDIT_ADDRESS() {
        @Override
        public State getState() {
            return State.REGISTRATION_TAVERN;
        }
    },
    EDIT_CITY() {
        @Override
        public State getState() {
            return State.REGISTRATION_TAVERN;
        }
    },
    EDIT_TAVERN() {
        @Override
        public State getState() {
            return State.REGISTRATION_TAVERN;
        }
    },
    EDIT_PHONE_NUMBER() {
        @Override
        public State getState() {
            return State.REGISTRATION_TAVERN;
        }
    },
    VIEW_SETTINGS() {
        @Override
        public State getState() {
            return State.SETTINGS;
        }
    },
    VIEW_GENERAL_SETTINGS() {
        @Override
        public State getState() {
            return State.SETTINGS;
        }

        @Override
        public SubState getParentSubState() {
            return VIEW_SETTINGS;
        }
    },
    VIEW_GENERAL_SETTINGS_TAVERN_NAME() {
        @Override
        public State getState() {
            return State.SETTINGS;
        }

        @Override
        public SubState getParentSubState() {
            return VIEW_GENERAL_SETTINGS;
        }
    },
    CHANGE_GENERAL_SETTINGS_TAVERN_NAME() {
        @Override
        public State getState() {
            return State.SETTINGS;
        }

        @Override
        public SubState getParentSubState() {
            return VIEW_GENERAL_SETTINGS_TAVERN_NAME;
        }
    },
    VIEW_GENERAL_SETTINGS_TAVERN_ADDRESS() {
        @Override
        public State getState() {
            return State.SETTINGS;
        }

        @Override
        public SubState getParentSubState() {
            return VIEW_GENERAL_SETTINGS;
        }
    },
    CHANGE_GENERAL_SETTINGS_TAVERN_ADDRESS() {
        @Override
        public State getState() {
            return State.SETTINGS;
        }

        @Override
        public SubState getParentSubState() {
            return VIEW_GENERAL_SETTINGS_TAVERN_ADDRESS;
        }
    },
    VIEW_GENERAL_SETTINGS_TAVERN_CONTACTS() {
        @Override
        public State getState() {
            return State.SETTINGS;
        }

        @Override
        public SubState getParentSubState() {
            return VIEW_GENERAL_SETTINGS;
        }
    },
    ADD_GENERAL_SETTINGS_TAVERN_CONTACTS() {
        @Override
        public State getState() {
            return State.SETTINGS;
        }

        @Override
        public SubState getParentSubState() {
            return VIEW_GENERAL_SETTINGS_TAVERN_CONTACTS;
        }
    },
    DELETE_GENERAL_SETTINGS_TAVERN_CONTACTS() {
        @Override
        public State getState() {
            return State.SETTINGS;
        }

        @Override
        public SubState getParentSubState() {
            return VIEW_GENERAL_SETTINGS_TAVERN_CONTACTS;
        }
    },
    VIEW_PROFILE_SETTINGS() {
        @Override
        public State getState() {
            return State.SETTINGS;
        }

        @Override
        public SubState getParentSubState() {
            return VIEW_SETTINGS;
        }
    },
    VIEW_PROFILE_SETTINGS_USER_NAME() {
        @Override
        public State getState() {
            return State.SETTINGS;
        }

        @Override
        public SubState getParentSubState() {
            return VIEW_PROFILE_SETTINGS;
        }
    },
    CHANGE_PROFILE_SETTINGS_USER_NAME() {
        @Override
        public State getState() {
            return State.SETTINGS;
        }

        @Override
        public SubState getParentSubState() {
            return VIEW_PROFILE_SETTINGS_USER_NAME;
        }
    },
    VIEW_PROFILE_SETTINGS_USER_CONTACTS() {
        @Override
        public State getState() {
            return State.SETTINGS;
        }

        @Override
        public SubState getParentSubState() {
            return VIEW_PROFILE_SETTINGS;
        }
    },
    ADD_PROFILE_SETTINGS_USER_CONTACTS() {
        @Override
        public State getState() {
            return State.SETTINGS;
        }

        @Override
        public SubState getParentSubState() {
            return VIEW_PROFILE_SETTINGS_USER_CONTACTS;
        }
    },
    DELETE_PROFILE_SETTINGS_USER_CONTACTS() {
        @Override
        public State getState() {
            return State.SETTINGS;
        }

        @Override
        public SubState getParentSubState() {
            return VIEW_PROFILE_SETTINGS_USER_CONTACTS;
        }
    },
    DELETE_PROFILE_SETTINGS() {
        @Override
        public State getState() {
            return State.SETTINGS;
        }

        @Override
        public SubState getParentSubState() {
            return VIEW_PROFILE_SETTINGS;
        }
    },
    VIEW_EMPLOYEE_SETTINGS() {
        @Override
        public State getState() {
            return State.SETTINGS;
        }

        @Override
        public SubState getParentSubState() {
            return VIEW_SETTINGS;
        }
    },
    DELETE_EMPLOYEE_SETTINGS() {
        @Override
        public State getState() {
            return State.SETTINGS;
        }

        @Override
        public SubState getParentSubState() {
            return VIEW_EMPLOYEE_SETTINGS;
        }
    },
    REGISTER_EMPLOYEE_BUTTON_PRESS() {
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
    VIEW_GENERAL_SETTINGS_CATEGORIES() {
        @Override
        public State getState() {
            return State.SETTINGS;
        }

        @Override
        public SubState getParentSubState() {
            return VIEW_GENERAL_SETTINGS;
        }
    },
    CHANGE_GENERAL_SETTINGS_CATEGORIES() {
        @Override
        public State getState() {
            return State.SETTINGS;
        }

        @Override
        public SubState getParentSubState() {
            return VIEW_GENERAL_SETTINGS_CATEGORIES;
        }
    },
    VIEW_SCHEDULE_SETTINGS() {
        @Override
        public State getState() {
            return State.SETTINGS;
        }

        @Override
        public SubState getParentSubState() {
            return VIEW_SETTINGS;
        }
    },
    DELETE_SCHEDULE_SETTINGS() {
        @Override
        public State getState() {
            return State.SETTINGS;
        }

        @Override
        public SubState getParentSubState() {
            return VIEW_SCHEDULE_SETTINGS;
        }
    },
    ADD_DAY_WEEK_SCHEDULE_SETTINGS() {
        @Override
        public State getState() {
            return State.SETTINGS;
        }

        @Override
        public SubState getParentSubState() {
            return VIEW_SCHEDULE_SETTINGS;
        }
    },
    ADD_START_HOUR_SCHEDULE_SETTINGS() {
        @Override
        public State getState() {
            return State.SETTINGS;
        }

        @Override
        public SubState getParentSubState() {
            return VIEW_SCHEDULE_SETTINGS;
        }
    },
    ADD_START_MINUTE_SCHEDULE_SETTINGS() {
        @Override
        public State getState() {
            return State.SETTINGS;
        }

        @Override
        public SubState getParentSubState() {
            return VIEW_SCHEDULE_SETTINGS;
        }
    },
    ADD_END_HOUR_SCHEDULE_SETTINGS() {
        @Override
        public State getState() {
            return State.SETTINGS;
        }

        @Override
        public SubState getParentSubState() {
            return VIEW_SCHEDULE_SETTINGS;
        }
    },
    ADD_END_MINUTE_SCHEDULE_SETTINGS() {
        @Override
        public State getState() {
            return State.SETTINGS;
        }

        @Override
        public SubState getParentSubState() {
            return VIEW_SCHEDULE_SETTINGS;
        }
    },
    ADD_PRICE_SCHEDULE_SETTINGS() {
        @Override
        public State getState() {
            return State.SETTINGS;
        }

        @Override
        public SubState getParentSubState() {
            return VIEW_SCHEDULE_SETTINGS;
        }
    },
    VIEW_TABLE_SETTINGS() {
        @Override
        public State getState() {
            return State.SETTINGS;
        }

        @Override
        public SubState getParentSubState() {
            return VIEW_SETTINGS;
        }
    },
    DELETE_TABLE_SETTINGS() {
        @Override
        public State getState() {
            return State.SETTINGS;
        }

        @Override
        public SubState getParentSubState() {
            return VIEW_TABLE_SETTINGS;
        }
    },
    ADD_LABEL_TABLE_SETTINGS() {
        @Override
        public State getState() {
            return State.SETTINGS;
        }

        @Override
        public SubState getParentSubState() {
            return VIEW_TABLE_SETTINGS;
        }
    },
    ADD_NUMBER_SEATS_TABLE_SETTINGS() {
        @Override
        public State getState() {
            return State.SETTINGS;
        }

        @Override
        public SubState getParentSubState() {
            return VIEW_TABLE_SETTINGS;
        }
    },
    GREETING() {
        @Override
        public State getState() {
            return State.BOOKING;
        }

        @Override
        public SubState getNextSubState() {
            return SubState.CITY_SELECT;
        }
    },
    CITY_SELECT(MessageText.CHOICE_CITY) {
        @Override
        public State getState() {
            return State.BOOKING;
        }
    },
    VIEW_RESERVE_LIST() {
        @Override
        public State getState() {
            return State.RESERVE;
        }

        @Override
        public SubState getParentSubState() {
            return VIEW_MAIN_MENU;
        }
    },
    DELETE_RESERVE_CHOICE_DATE() {
        @Override
        public State getState() {
            return State.RESERVE;
        }

        @Override
        public SubState getParentSubState() {
            return VIEW_RESERVE_LIST;
        }
    },
    DELETE_RESERVE_CHOICE_TABLE() {
        @Override
        public State getState() {
            return State.RESERVE;
        }

        @Override
        public SubState getParentSubState() {
            return VIEW_RESERVE_LIST;
        }
    },
    ADD_RESERVE_CHOICE_DATE() {
        @Override
        public State getState() {
            return State.RESERVE;
        }

        @Override
        public SubState getParentSubState() {
            return VIEW_MAIN_MENU;
        }
    },
    ADD_RESERVE_CHOICE_TIME() {
        @Override
        public State getState() {
            return State.RESERVE;
        }

        @Override
        public SubState getParentSubState() {
            return VIEW_MAIN_MENU;
        }
    },
    ADD_RESERVE_CHOICE_PERSONS() {
        @Override
        public State getState() {
            return State.RESERVE;
        }

        @Override
        public SubState getParentSubState() {
            return VIEW_MAIN_MENU;
        }
    },
    ADD_RESERVE_CHOICE_TABLE() {
        @Override
        public State getState() {
            return State.RESERVE;
        }

        @Override
        public SubState getParentSubState() {
            return VIEW_MAIN_MENU;
        }
    },
    ADD_RESERVE_CHOICE_NAME() {
        @Override
        public State getState() {
            return State.RESERVE;
        }

        @Override
        public SubState getParentSubState() {
            return VIEW_MAIN_MENU;
        }
    },
    ADD_RESERVE_CHOICE_PHONE() {
        @Override
        public State getState() {
            return State.RESERVE;
        }

        @Override
        public SubState getParentSubState() {
            return VIEW_MAIN_MENU;
        }
    },
    ADD_RESERVE_INFO() {
        @Override
        public State getState() {
            return State.RESERVE;
        }

        @Override
        public SubState getParentSubState() {
            return VIEW_MAIN_MENU;
        }
    },
    DELETE_RESERVE_CHOICE_TAVERN() {
        @Override
        public State getState() {
            return State.BOOKING;
        }

        @Override
        public SubState getParentSubState() {
            return VIEW_RESERVE_LIST;
        }
    },
    VIEW_TAVERN_LIST() {
        @Override
        public State getState() {
            return State.BOOKING;
        }

        @Override
        public SubState getParentSubState() {
            return VIEW_MAIN_MENU;
        }
    },
    CHOICE_TAVERN() {
        @Override
        public State getState() {
            return State.BOOKING;
        }

        @Override
        public SubState getParentSubState() {
            return VIEW_MAIN_MENU;
        }
    },
    VIEW_TAVERN() {
        @Override
        public State getState() {
            return State.BOOKING;
        }

        @Override
        public SubState getParentSubState() {
            return VIEW_TAVERN_LIST;
        }
    },
    BOOKING_CHOICE_DATE() {
        @Override
        public State getState() {
            return State.BOOKING;
        }

        @Override
        public SubState getParentSubState() {
            return VIEW_TAVERN;
        }
    },
    BOOKING_CHOICE_TIME() {
        @Override
        public State getState() {
            return State.BOOKING;
        }

        @Override
        public SubState getParentSubState() {
            return BOOKING_CHOICE_DATE;
        }
    },
    BOOKING_CHOICE_PERSONS() {
        @Override
        public State getState() {
            return State.BOOKING;
        }

        @Override
        public SubState getParentSubState() {
            return BOOKING_CHOICE_TIME;
        }
    },
    BOOKING_APPROVE() {
        @Override
        public State getState() {
            return State.BOOKING;
        }

        @Override
        public SubState getParentSubState() {
            return BOOKING_CHOICE_PERSONS;
        }
    };

    private String message;

    SubState() {
    }

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
