package ru.privetdruk.restorder.service;

import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import ru.privetdruk.restorder.model.enums.Button;
import ru.privetdruk.restorder.model.enums.Category;
import ru.privetdruk.restorder.model.enums.City;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
public class KeyboardService {
    public static final ReplyKeyboardRemove REMOVE_KEYBOARD = new ReplyKeyboardRemove(true);
    public static final ReplyKeyboardMarkup CLIENT_MAIN_MENU = new ReplyKeyboardMarkup();
    public static final ReplyKeyboardMarkup USER_MAIN_MENU = new ReplyKeyboardMarkup();
    public static final ReplyKeyboardMarkup SHARE_PHONE_KEYBOARD = new ReplyKeyboardMarkup();
    public static final ReplyKeyboardMarkup YES_NO_KEYBOARD = new ReplyKeyboardMarkup();
    public static final ReplyKeyboardMarkup CANCEL_KEYBOARD = new ReplyKeyboardMarkup();
    public static final ReplyKeyboardMarkup SETTINGS_KEYBOARD = new ReplyKeyboardMarkup();
    public static final ReplyKeyboardMarkup GENERAL_KEYBOARD = new ReplyKeyboardMarkup();
    public static final ReplyKeyboardMarkup CHANGE_KEYBOARD = new ReplyKeyboardMarkup();
    public static final ReplyKeyboardMarkup CHANGE_DELETE_KEYBOARD = new ReplyKeyboardMarkup();
    public static final ReplyKeyboardMarkup PROFILE_KEYBOARD = new ReplyKeyboardMarkup();
    public static final ReplyKeyboardMarkup BLACKLIST_KEYBOARD = new ReplyKeyboardMarkup();
    public static final ReplyKeyboardMarkup MANAGE_USER_BLACKLIST_KEYBOARD = new ReplyKeyboardMarkup();
    public static final ReplyKeyboardMarkup SETTINGS_BLACKLIST_KEYBOARD = new ReplyKeyboardMarkup();
    public static final ReplyKeyboardMarkup REASON_BLACKLIST_KEYBOARD = new ReplyKeyboardMarkup();
    public static final ReplyKeyboardMarkup ADD_DELETE_KEYBOARD = new ReplyKeyboardMarkup();
    public static final ReplyKeyboardMarkup CATEGORIES_LIST_KEYBOARD = new ReplyKeyboardMarkup();
    public static final ReplyKeyboardMarkup CATEGORIES_LIST_WITH_CANCEL_KEYBOARD = new ReplyKeyboardMarkup();
    public static final ReplyKeyboardMarkup DAY_WEEK_WITH_PERIOD_KEYBOARD = new ReplyKeyboardMarkup();
    public static final ReplyKeyboardMarkup HOURS_WITH_CANCEL_KEYBOARD = new ReplyKeyboardMarkup();
    public static final ReplyKeyboardMarkup MINUTES_WITH_CANCEL_KEYBOARD = new ReplyKeyboardMarkup();
    public static final ReplyKeyboardMarkup FREE_WITH_CANCEL_KEYBOARD = new ReplyKeyboardMarkup();
    public static final ReplyKeyboardMarkup RESERVE_LIST_KEYBOARD = new ReplyKeyboardMarkup();
    public static final ReplyKeyboardMarkup USER_RESERVE_LIST_KEYBOARD = new ReplyKeyboardMarkup();
    public static final ReplyKeyboardMarkup TODAY_TOMORROW_CANCEL_KEYBOARD = new ReplyKeyboardMarkup();
    public static final ReplyKeyboardMarkup NUMBERS_CANCEL_KEYBOARD = new ReplyKeyboardMarkup();
    public static final ReplyKeyboardMarkup NUMBERS_KEYBOARD = new ReplyKeyboardMarkup();
    public static final ReplyKeyboardMarkup WITHOUT_PHONE_KEYBOARD = new ReplyKeyboardMarkup();
    public static final ReplyKeyboardMarkup APPROVE_KEYBOARD = new ReplyKeyboardMarkup();
    public static final ReplyKeyboardMarkup APPROVE_BEFORE_KEYBOARD = new ReplyKeyboardMarkup();
    public static final ReplyKeyboardMarkup CITIES_KEYBOARD = new ReplyKeyboardMarkup();
    public static final ReplyKeyboardMarkup TAVERN_INFO_KEYBOARD = new ReplyKeyboardMarkup();
    public static final ReplyKeyboardMarkup BOOKING_CHOICE_DATE_KEYBOARD = new ReplyKeyboardMarkup();
    public static final ReplyKeyboardMarkup BOOKING_CHOICE_TIME_KEYBOARD = new ReplyKeyboardMarkup();
    public static final ReplyKeyboardMarkup RESERVE_CHOICE_TIME_KEYBOARD = new ReplyKeyboardMarkup();
    public static final ReplyKeyboardMarkup WITHOUT_DESCRIPTION_KEYBOARD = new ReplyKeyboardMarkup();
    public static final ReplyKeyboardMarkup REGISTRATION_TAVERN_KEYBOARD = new ReplyKeyboardMarkup();
    public static final ReplyKeyboardMarkup REGISTRATION_APPROVING = new ReplyKeyboardMarkup();
    public static final ReplyKeyboardMarkup BOOKING_CHOICE_TABLE_ANSWER_KEYBOARD = new ReplyKeyboardMarkup();
    public static final KeyboardRow BACK_AND_MAIN_MENU_ROW = new KeyboardRow(newKeyboardRow(Button.BACK, Button.RETURN_MAIN_MENU));

    {
        init();
    }

    public static void fillKeyboard(ReplyKeyboardMarkup keyboard, KeyboardRow... rows) {
        keyboard.setKeyboard(List.of(rows));
        keyboard.setResizeKeyboard(true);
    }

    private static void init() {
        KeyboardRow CANCEL_ROW = new KeyboardRow(List.of(new KeyboardButton(Button.CANCEL.getText())));

        fillKeyboard(BOOKING_CHOICE_TIME_KEYBOARD, BACK_AND_MAIN_MENU_ROW);

        fillKeyboard(CITIES_KEYBOARD, newKeyboardRow(City.YOSHKAR_OLA.getDescription()));
        fillKeyboard(REGISTRATION_TAVERN_KEYBOARD, newKeyboardRow(Button.REGISTRATION));
        fillKeyboard(CITIES_KEYBOARD, newKeyboardRow(City.YOSHKAR_OLA.getDescription()));
        fillKeyboard(WITHOUT_DESCRIPTION_KEYBOARD, newKeyboardRow(Button.WITHOUT_DESCRIPTION));

        fillKeyboard(YES_NO_KEYBOARD, newKeyboardRow(Button.YES, Button.NO));
        fillKeyboard(APPROVE_KEYBOARD, newKeyboardRow(Button.CANCEL, Button.ACCEPT));
        fillKeyboard(REGISTRATION_APPROVING, newKeyboardRow(Button.EDIT, Button.APPROVE));
        fillKeyboard(APPROVE_BEFORE_KEYBOARD, newKeyboardRow(Button.CANCEL, Button.ACCEPT));

        fillKeyboard(
                BOOKING_CHOICE_TABLE_ANSWER_KEYBOARD,
                newKeyboardRow(Button.MANUALLY, Button.AUTOMATIC),
                BACK_AND_MAIN_MENU_ROW
        );

        fillKeyboard(
                RESERVE_CHOICE_TIME_KEYBOARD,
                newKeyboardRow(Button.NOW),
                BACK_AND_MAIN_MENU_ROW
        );

        fillKeyboard(
                BOOKING_CHOICE_DATE_KEYBOARD,
                newKeyboardRow(Button.TODAY, Button.TOMORROW),
                BACK_AND_MAIN_MENU_ROW
        );

        fillKeyboard(
                CLIENT_MAIN_MENU,
                newKeyboardRow(Button.RESERVE),
                newKeyboardRow(Button.RESERVE_LIST),
                newKeyboardRow(Button.SETTINGS, Button.INFORMATION)
        );

        fillKeyboard(
                USER_MAIN_MENU,
                newKeyboardRow(Button.CAFE_BAR_RESTAURANT),
                newKeyboardRow(Button.NIGHT_CLUB, Button.HOOKAH_BAR),
                newKeyboardRow(Button.BILLIARDS, Button.BOWLING),
                newKeyboardRow(Button.MY_RESERVE)
        );

        final KeyboardButton SHARE_PHONE = KeyboardButton.builder()
                .text(Button.SHARE_PHONE.getText())
                .requestContact(true)
                .build();

        SHARE_PHONE_KEYBOARD.setKeyboard(List.of(new KeyboardRow(List.of(SHARE_PHONE))));
        SHARE_PHONE_KEYBOARD.setResizeKeyboard(true);

        CANCEL_KEYBOARD.setKeyboard(List.of(CANCEL_ROW));
        CANCEL_KEYBOARD.setResizeKeyboard(true);

        fillKeyboard(
                SETTINGS_KEYBOARD,
                newKeyboardRow(Button.PROFILE, Button.BLACKLIST),
                newKeyboardRow(Button.GENERAL, Button.EMPLOYEES),
                newKeyboardRow(Button.SCHEDULE, Button.TABLES),
                newKeyboardRow(Button.RETURN_MAIN_MENU)
        );

        fillKeyboard(
                GENERAL_KEYBOARD,
                newKeyboardRow(Button.TAVERN_NAME, Button.DESCRIPTION, Button.CATEGORIES),
                newKeyboardRow(Button.PHONE_NUMBER, Button.TAVERN_ADDRESS, Button.TAVERN_TABLE_LAYOUT),
                BACK_AND_MAIN_MENU_ROW
        );

        fillKeyboard(
                CHANGE_KEYBOARD,
                newKeyboardRow(Button.CHANGE),
                BACK_AND_MAIN_MENU_ROW
        );

        fillKeyboard(
                CHANGE_DELETE_KEYBOARD,
                newKeyboardRow(Button.CHANGE, Button.DELETE),
                BACK_AND_MAIN_MENU_ROW
        );

        fillKeyboard(
                ADD_DELETE_KEYBOARD,
                newKeyboardRow(Button.ADD, Button.DELETE),
                BACK_AND_MAIN_MENU_ROW
        );

        fillKeyboard(
                PROFILE_KEYBOARD,
                newKeyboardRow(Button.USER_NAME, Button.PHONE_NUMBER),
                newKeyboardRow(Button.DELETE_PROFILE),
                BACK_AND_MAIN_MENU_ROW
        );

        int MAX_COLS_ON_ROW = 2;
        List<KeyboardRow> rows = new ArrayList<>();

        int countCols = 1;
        KeyboardRow keyboardRow = new KeyboardRow();
        Category[] categories = Category.values();
        for (int index = 0; index < categories.length; index++, countCols++) {
            keyboardRow.add(categories[index].getDescription());

            if (countCols == MAX_COLS_ON_ROW || index == categories.length - 1) {
                countCols = 0;
                rows.add(keyboardRow);
                keyboardRow = new KeyboardRow();
            }
        }

        CATEGORIES_LIST_KEYBOARD.setKeyboard(rows);
        CATEGORIES_LIST_KEYBOARD.setResizeKeyboard(true);

        rows.add(CANCEL_ROW);

        CATEGORIES_LIST_WITH_CANCEL_KEYBOARD.setKeyboard(rows);
        CATEGORIES_LIST_WITH_CANCEL_KEYBOARD.setResizeKeyboard(true);

        fillKeyboard(
                DAY_WEEK_WITH_PERIOD_KEYBOARD,
                newKeyboardRow(Button.MONDAY, Button.TUESDAY, Button.WEDNESDAY),
                newKeyboardRow(Button.THURSDAY, Button.FRIDAY, Button.SATURDAY),
                newKeyboardRow(Button.SUNDAY, Button.WEEKDAYS, Button.WEEKENDS),
                BACK_AND_MAIN_MENU_ROW
        );

        HOURS_WITH_CANCEL_KEYBOARD.setKeyboard(List.of(
                newKeyboardRow("12"),
                newKeyboardRow("13"),
                newKeyboardRow("14"),
                newKeyboardRow("15"),
                newKeyboardRow("16"),
                newKeyboardRow("17"),
                newKeyboardRow("18"),
                newKeyboardRow("19"),
                newKeyboardRow("20"),
                newKeyboardRow("21"),
                newKeyboardRow("22"),
                newKeyboardRow("23"),
                newKeyboardRow("00"),
                newKeyboardRow("1"),
                newKeyboardRow("2"),
                newKeyboardRow("3"),
                newKeyboardRow("4"),
                newKeyboardRow("5"),
                newKeyboardRow("6"),
                newKeyboardRow("7"),
                newKeyboardRow("8"),
                newKeyboardRow("9"),
                newKeyboardRow("10"),
                newKeyboardRow("11"),
                CANCEL_ROW
        ));
        HOURS_WITH_CANCEL_KEYBOARD.setResizeKeyboard(true);

        MINUTES_WITH_CANCEL_KEYBOARD.setKeyboard(List.of(
                newKeyboardRow("0"),
                newKeyboardRow("5"),
                newKeyboardRow("10"),
                newKeyboardRow("15"),
                newKeyboardRow("20"),
                newKeyboardRow("25"),
                newKeyboardRow("30"),
                newKeyboardRow("35"),
                newKeyboardRow("40"),
                newKeyboardRow("45"),
                newKeyboardRow("50"),
                newKeyboardRow("55"),
                CANCEL_ROW
        ));
        MINUTES_WITH_CANCEL_KEYBOARD.setResizeKeyboard(true);

        fillKeyboard(
                FREE_WITH_CANCEL_KEYBOARD,
                newKeyboardRow(Button.FREE),
                CANCEL_ROW
        );

        fillKeyboard(
                RESERVE_LIST_KEYBOARD,
                newKeyboardRow(Button.CANCEL_RESERVE, Button.RESERVE),
                newKeyboardRow(Button.RETURN_MAIN_MENU)
        );

        fillKeyboard(
                USER_RESERVE_LIST_KEYBOARD,
                newKeyboardRow(Button.CANCEL_RESERVE),
                newKeyboardRow(Button.RETURN_MAIN_MENU)
        );

        fillKeyboard(
                TODAY_TOMORROW_CANCEL_KEYBOARD,
                newKeyboardRow(Button.TODAY, Button.TOMORROW),
                CANCEL_ROW
        );

        fillKeyboard(
                NUMBERS_CANCEL_KEYBOARD,
                newKeyboardRow(Button.ONE, Button.TWO, Button.THREE),
                newKeyboardRow(Button.FOUR, Button.FIVE, Button.SIX),
                newKeyboardRow(Button.SEVEN, Button.EIGHT, Button.NINE),
                CANCEL_ROW
        );

        fillKeyboard(
                NUMBERS_KEYBOARD,
                newKeyboardRow(Button.ONE, Button.TWO, Button.THREE),
                newKeyboardRow(Button.FOUR, Button.FIVE, Button.SIX),
                newKeyboardRow(Button.SEVEN, Button.EIGHT, Button.NINE),
                BACK_AND_MAIN_MENU_ROW
        );

        fillKeyboard(
                WITHOUT_PHONE_KEYBOARD,
                newKeyboardRow(Button.WITHOUT_PHONE),
                CANCEL_ROW
        );

        fillKeyboard(
                TAVERN_INFO_KEYBOARD,
                newKeyboardRow(Button.RESERVE),
                BACK_AND_MAIN_MENU_ROW
        );

        fillKeyboard(
                BLACKLIST_KEYBOARD,
                newKeyboardRow(Button.UNBLOCK, Button.BLOCK),
                newKeyboardRow(Button.BLACKLIST_LIST, Button.SETTINGS),
                BACK_AND_MAIN_MENU_ROW
        );

        fillKeyboard(
                MANAGE_USER_BLACKLIST_KEYBOARD,
                newKeyboardRow(Button.UNBLOCK),
                BACK_AND_MAIN_MENU_ROW
        );

        fillKeyboard(
                REASON_BLACKLIST_KEYBOARD,
                newKeyboardRow(Button.DOESNT_COME),
                newKeyboardRow(Button.INAPPROPRIATE_BEHAVIOUR),
                CANCEL_ROW
        );

        fillKeyboard(
                SETTINGS_BLACKLIST_KEYBOARD,
                newKeyboardRow(Button.NUMBER_TIMES, Button.NUMBER_DAYS),
                BACK_AND_MAIN_MENU_ROW
        );
    }

    public static KeyboardRow newKeyboardRow(Button... buttons) {
        return new KeyboardRow(
                Arrays.stream(buttons)
                        .map(KeyboardService::toKeyboardButton)
                        .toList()
        );
    }

    public static KeyboardRow newKeyboardRow(String... buttons) {
        return new KeyboardRow(
                Arrays.stream(buttons)
                        .map(KeyboardService::toKeyboardButton)
                        .toList()
        );
    }

    private static KeyboardButton toKeyboardButton(String button) {
        return new KeyboardButton(button);
    }

    private static KeyboardButton toKeyboardButton(Button button) {
        return new KeyboardButton(button.getText());
    }

    public InlineKeyboardButton createInlineButton(Button button) {
        InlineKeyboardButton inlineKeyboardButton = new InlineKeyboardButton(button.getText());
        inlineKeyboardButton.setCallbackData(button.name());
        return inlineKeyboardButton;
    }

    public List<List<InlineKeyboardButton>> createButtonList(Map<String, String> buttons, int maxButtonsPerRow) {
        final AtomicInteger counter = new AtomicInteger(0);

        return new ArrayList<>(buttons.entrySet().stream()
                .map(e -> {
                    InlineKeyboardButton inlineKeyboardButton = new InlineKeyboardButton(e.getKey());
                    inlineKeyboardButton.setCallbackData(e.getValue());
                    return inlineKeyboardButton;
                })
                .collect(Collectors.groupingBy(e -> counter.getAndIncrement() / maxButtonsPerRow))
                .values());
    }

    public InlineKeyboardMarkup createInlineKeyboard(Map<String, String> buttons, int maxButtonsPerRow) {
        final AtomicInteger counter = new AtomicInteger(0);

        return InlineKeyboardMarkup.builder()
                .keyboard(new ArrayList<>(
                        buttons.entrySet().stream()
                                .map(e -> {
                                    InlineKeyboardButton inlineKeyboardButton = new InlineKeyboardButton(e.getKey());
                                    inlineKeyboardButton.setCallbackData(e.getValue());
                                    return inlineKeyboardButton;
                                })
                                .collect(Collectors.groupingBy(e -> counter.getAndIncrement() / maxButtonsPerRow))
                                .values()
                ))
                .build();
    }
}
