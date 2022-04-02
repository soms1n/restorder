package ru.privetdruk.restorder.service;

import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import ru.privetdruk.restorder.model.enums.Button;
import ru.privetdruk.restorder.model.enums.Category;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
public class KeyboardService {
    public static final ReplyKeyboardRemove REMOVE_KEYBOARD = new ReplyKeyboardRemove(true);
    public static final ReplyKeyboardMarkup MAIN_MENU = new ReplyKeyboardMarkup();
    public static final ReplyKeyboardMarkup SHARE_PHONE_KEYBOARD = new ReplyKeyboardMarkup();
    public static final ReplyKeyboardMarkup YES_NO_KEYBOARD = new ReplyKeyboardMarkup();
    public static final ReplyKeyboardMarkup CANCEL_KEYBOARD = new ReplyKeyboardMarkup();
    public static final ReplyKeyboardMarkup SETTINGS_KEYBOARD = new ReplyKeyboardMarkup();
    public static final ReplyKeyboardMarkup GENERAL_KEYBOARD = new ReplyKeyboardMarkup();
    public static final ReplyKeyboardMarkup TAVERN_NAME_KEYBOARD = new ReplyKeyboardMarkup();
    public static final ReplyKeyboardMarkup TAVERN_CONTACTS_KEYBOARD = new ReplyKeyboardMarkup();
    public static final ReplyKeyboardMarkup TAVERN_ADDRESS_KEYBOARD = new ReplyKeyboardMarkup();
    public static final ReplyKeyboardMarkup PROFILE_KEYBOARD = new ReplyKeyboardMarkup();
    public static final ReplyKeyboardMarkup PROFILE_NAME_KEYBOARD = new ReplyKeyboardMarkup();
    public static final ReplyKeyboardMarkup USER_CONTACTS_KEYBOARD = new ReplyKeyboardMarkup();
    public static final ReplyKeyboardMarkup EMPLOYEE_KEYBOARD = new ReplyKeyboardMarkup();
    public static final ReplyKeyboardMarkup CATEGORIES_KEYBOARD = new ReplyKeyboardMarkup();
    public static final ReplyKeyboardMarkup CATEGORIES_LIST_WITH_CANCEL_KEYBOARD = new ReplyKeyboardMarkup();
    public static final ReplyKeyboardMarkup SCHEDULE_KEYBOARD = new ReplyKeyboardMarkup();
    public static final ReplyKeyboardMarkup DAY_WEEK_WITH_PERIOD_KEYBOARD = new ReplyKeyboardMarkup();
    public static final ReplyKeyboardMarkup HOURS_WITH_CANCEL_KEYBOARD = new ReplyKeyboardMarkup();
    public static final ReplyKeyboardMarkup MINUTES_WITH_CANCEL_KEYBOARD = new ReplyKeyboardMarkup();
    public static final ReplyKeyboardMarkup FREE_WITH_CANCEL_KEYBOARD = new ReplyKeyboardMarkup();
    public static final ReplyKeyboardMarkup TABLE_KEYBOARD = new ReplyKeyboardMarkup();
    public static final ReplyKeyboardMarkup RESERVE_LIST_KEYBOARD = new ReplyKeyboardMarkup();
    public static final ReplyKeyboardMarkup TODAY_TOMORROW_CANCEL_KEYBOARD = new ReplyKeyboardMarkup();
    public static final ReplyKeyboardMarkup NUMBERS_KEYBOARD = new ReplyKeyboardMarkup();
    public static final ReplyKeyboardMarkup WITHOUT_PHONE_KEYBOARD = new ReplyKeyboardMarkup();
    public static final ReplyKeyboardMarkup APPROVE_KEYBOARD = new ReplyKeyboardMarkup();

    {
        init();
    }

    private static void init() {
        MAIN_MENU.setKeyboard(List.of(
                new KeyboardRow(List.of(
                        new KeyboardButton(Button.RESERVE.getText())
                )),
                new KeyboardRow(List.of(
                        new KeyboardButton(Button.RESERVE_LIST.getText())
                )),
                new KeyboardRow(List.of(
                        new KeyboardButton(Button.SETTINGS.getText()),
                        new KeyboardButton(Button.INFORMATION.getText())
                ))
        ));
        MAIN_MENU.setResizeKeyboard(true);

        SHARE_PHONE_KEYBOARD.setKeyboard(List.of(
                new KeyboardRow(List.of(
                        KeyboardButton.builder()
                                .text(Button.SHARE_PHONE.getText())
                                .requestContact(true)
                                .build()
                ))
        ));
        SHARE_PHONE_KEYBOARD.setResizeKeyboard(true);

        YES_NO_KEYBOARD.setKeyboard(List.of(
                new KeyboardRow(List.of(
                        new KeyboardButton(Button.YES.getText()),
                        new KeyboardButton(Button.NO.getText())
                ))
        ));
        YES_NO_KEYBOARD.setResizeKeyboard(true);

        KeyboardRow CANCEL_ROW = new KeyboardRow(List.of(new KeyboardButton(Button.CANCEL.getText())));

        CANCEL_KEYBOARD.setKeyboard(List.of(CANCEL_ROW));
        CANCEL_KEYBOARD.setResizeKeyboard(true);

        SETTINGS_KEYBOARD.setKeyboard(List.of(
                new KeyboardRow(List.of(
                        new KeyboardButton(Button.PROFILE.getText())
                )),
                new KeyboardRow(List.of(
                        new KeyboardButton(Button.GENERAL.getText()),
                        new KeyboardButton(Button.EMPLOYEES.getText())
                )),
                new KeyboardRow(List.of(
                        new KeyboardButton(Button.SCHEDULE.getText()),
                        new KeyboardButton(Button.TABLES.getText())
                )),
                new KeyboardRow(List.of(
                        new KeyboardButton(Button.RETURN_MAIN_MENU.getText())
                ))
        ));
        SETTINGS_KEYBOARD.setResizeKeyboard(true);

        KeyboardRow BACK_AND_MAIN_MENU_ROW = new KeyboardRow(List.of(
                new KeyboardButton(Button.BACK.getText()),
                new KeyboardButton(Button.RETURN_MAIN_MENU.getText())
        ));
        GENERAL_KEYBOARD.setKeyboard(List.of(
                new KeyboardRow(List.of(
                        new KeyboardButton(Button.TAVERN_NAME.getText()),
                        new KeyboardButton(Button.CATEGORIES.getText())
                )),
                new KeyboardRow(List.of(
                        new KeyboardButton(Button.CONTACTS.getText()),
                        new KeyboardButton(Button.TAVERN_ADDRESS.getText())
                )),
                BACK_AND_MAIN_MENU_ROW
        ));
        GENERAL_KEYBOARD.setResizeKeyboard(true);

        TAVERN_NAME_KEYBOARD.setKeyboard(List.of(
                new KeyboardRow(List.of(
                        new KeyboardButton(Button.CHANGE.getText())
                )),
                BACK_AND_MAIN_MENU_ROW
        ));
        TAVERN_NAME_KEYBOARD.setResizeKeyboard(true);

        TAVERN_ADDRESS_KEYBOARD.setKeyboard(List.of(
                new KeyboardRow(List.of(
                        new KeyboardButton(Button.CHANGE.getText())
                )),
                BACK_AND_MAIN_MENU_ROW
        ));
        TAVERN_ADDRESS_KEYBOARD.setResizeKeyboard(true);

        TAVERN_CONTACTS_KEYBOARD.setKeyboard(List.of(
                new KeyboardRow(List.of(
                        new KeyboardButton(Button.ADD.getText()),
                        new KeyboardButton(Button.DELETE.getText())
                )),
                BACK_AND_MAIN_MENU_ROW
        ));
        TAVERN_CONTACTS_KEYBOARD.setResizeKeyboard(true);

        PROFILE_KEYBOARD.setKeyboard(List.of(
                new KeyboardRow(List.of(
                        new KeyboardButton(Button.USER_NAME.getText()),
                        new KeyboardButton(Button.CONTACTS.getText())
                )),
                new KeyboardRow(List.of(
                        new KeyboardButton(Button.DELETE_PROFILE.getText())
                )),
                BACK_AND_MAIN_MENU_ROW
        ));
        PROFILE_KEYBOARD.setResizeKeyboard(true);

        PROFILE_NAME_KEYBOARD.setKeyboard(List.of(
                new KeyboardRow(List.of(
                        new KeyboardButton(Button.CHANGE.getText())
                )),
                BACK_AND_MAIN_MENU_ROW
        ));
        PROFILE_NAME_KEYBOARD.setResizeKeyboard(true);

        USER_CONTACTS_KEYBOARD.setKeyboard(List.of(
                new KeyboardRow(List.of(
                        new KeyboardButton(Button.ADD.getText()),
                        new KeyboardButton(Button.DELETE.getText())
                )),
                BACK_AND_MAIN_MENU_ROW
        ));
        USER_CONTACTS_KEYBOARD.setResizeKeyboard(true);

        EMPLOYEE_KEYBOARD.setKeyboard(List.of(
                new KeyboardRow(List.of(
                        new KeyboardButton(Button.ADD.getText()),
                        new KeyboardButton(Button.DELETE.getText())
                )),
                BACK_AND_MAIN_MENU_ROW
        ));
        EMPLOYEE_KEYBOARD.setResizeKeyboard(true);

        CATEGORIES_KEYBOARD.setKeyboard(List.of(
                new KeyboardRow(List.of(
                        new KeyboardButton(Button.CHANGE.getText())
                )),
                BACK_AND_MAIN_MENU_ROW
        ));
        CATEGORIES_KEYBOARD.setResizeKeyboard(true);

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

        CATEGORIES_LIST_WITH_CANCEL_KEYBOARD.setKeyboard(rows);
        CATEGORIES_LIST_WITH_CANCEL_KEYBOARD.setResizeKeyboard(true);

        SCHEDULE_KEYBOARD.setKeyboard(List.of(
                new KeyboardRow(List.of(
                        new KeyboardButton(Button.ADD.getText()),
                        new KeyboardButton(Button.DELETE.getText())
                )),
                BACK_AND_MAIN_MENU_ROW
        ));
        SCHEDULE_KEYBOARD.setResizeKeyboard(true);

        DAY_WEEK_WITH_PERIOD_KEYBOARD.setKeyboard(List.of(
                new KeyboardRow(List.of(
                        new KeyboardButton(Button.MONDAY.getText()),
                        new KeyboardButton(Button.TUESDAY.getText()),
                        new KeyboardButton(Button.WEDNESDAY.getText())
                )),
                new KeyboardRow(List.of(
                        new KeyboardButton(Button.THURSDAY.getText()),
                        new KeyboardButton(Button.FRIDAY.getText()),
                        new KeyboardButton(Button.SATURDAY.getText())
                )),
                new KeyboardRow(List.of(
                        new KeyboardButton(Button.SUNDAY.getText()),
                        new KeyboardButton(Button.WEEKDAYS.getText()),
                        new KeyboardButton(Button.WEEKENDS.getText())
                )),
                BACK_AND_MAIN_MENU_ROW
        ));
        DAY_WEEK_WITH_PERIOD_KEYBOARD.setResizeKeyboard(true);

        HOURS_WITH_CANCEL_KEYBOARD.setKeyboard(List.of(
                new KeyboardRow(List.of(new KeyboardButton("12"))),
                new KeyboardRow(List.of(new KeyboardButton("13"))),
                new KeyboardRow(List.of(new KeyboardButton("14"))),
                new KeyboardRow(List.of(new KeyboardButton("15"))),
                new KeyboardRow(List.of(new KeyboardButton("16"))),
                new KeyboardRow(List.of(new KeyboardButton("17"))),
                new KeyboardRow(List.of(new KeyboardButton("18"))),
                new KeyboardRow(List.of(new KeyboardButton("19"))),
                new KeyboardRow(List.of(new KeyboardButton("20"))),
                new KeyboardRow(List.of(new KeyboardButton("21"))),
                new KeyboardRow(List.of(new KeyboardButton("22"))),
                new KeyboardRow(List.of(new KeyboardButton("23"))),
                new KeyboardRow(List.of(new KeyboardButton("24"))),
                new KeyboardRow(List.of(new KeyboardButton("1"))),
                new KeyboardRow(List.of(new KeyboardButton("2"))),
                new KeyboardRow(List.of(new KeyboardButton("3"))),
                new KeyboardRow(List.of(new KeyboardButton("4"))),
                new KeyboardRow(List.of(new KeyboardButton("5"))),
                new KeyboardRow(List.of(new KeyboardButton("6"))),
                new KeyboardRow(List.of(new KeyboardButton("7"))),
                new KeyboardRow(List.of(new KeyboardButton("8"))),
                new KeyboardRow(List.of(new KeyboardButton("9"))),
                new KeyboardRow(List.of(new KeyboardButton("10"))),
                new KeyboardRow(List.of(new KeyboardButton("11"))),
                CANCEL_ROW
        ));
        HOURS_WITH_CANCEL_KEYBOARD.setResizeKeyboard(true);

        MINUTES_WITH_CANCEL_KEYBOARD.setKeyboard(List.of(
                new KeyboardRow(List.of(new KeyboardButton("0"))),
                new KeyboardRow(List.of(new KeyboardButton("5"))),
                new KeyboardRow(List.of(new KeyboardButton("10"))),
                new KeyboardRow(List.of(new KeyboardButton("15"))),
                new KeyboardRow(List.of(new KeyboardButton("20"))),
                new KeyboardRow(List.of(new KeyboardButton("25"))),
                new KeyboardRow(List.of(new KeyboardButton("30"))),
                new KeyboardRow(List.of(new KeyboardButton("35"))),
                new KeyboardRow(List.of(new KeyboardButton("40"))),
                new KeyboardRow(List.of(new KeyboardButton("45"))),
                new KeyboardRow(List.of(new KeyboardButton("50"))),
                new KeyboardRow(List.of(new KeyboardButton("55"))),
                CANCEL_ROW
        ));
        MINUTES_WITH_CANCEL_KEYBOARD.setResizeKeyboard(true);

        FREE_WITH_CANCEL_KEYBOARD.setKeyboard(List.of(
                new KeyboardRow(List.of(new KeyboardButton(Button.FREE.getText()))),
                CANCEL_ROW
        ));
        FREE_WITH_CANCEL_KEYBOARD.setResizeKeyboard(true);

        TABLE_KEYBOARD.setKeyboard(List.of(
                new KeyboardRow(List.of(
                        new KeyboardButton(Button.ADD.getText()),
                        new KeyboardButton(Button.DELETE.getText())
                )),
                BACK_AND_MAIN_MENU_ROW
        ));
        TABLE_KEYBOARD.setResizeKeyboard(true);

        RESERVE_LIST_KEYBOARD.setKeyboard(List.of(
                new KeyboardRow(List.of(new KeyboardButton(Button.DELETE_RESERVE.getText()), new KeyboardButton(Button.RESERVE.getText()))),
                new KeyboardRow(List.of(new KeyboardButton(Button.RETURN_MAIN_MENU.getText())))
        ));
        RESERVE_LIST_KEYBOARD.setResizeKeyboard(true);

        TODAY_TOMORROW_CANCEL_KEYBOARD.setKeyboard(List.of(
                new KeyboardRow(List.of(new KeyboardButton(Button.TOMORROW.getText()), new KeyboardButton(Button.TODAY.getText()))),
                CANCEL_ROW
        ));
        TODAY_TOMORROW_CANCEL_KEYBOARD.setResizeKeyboard(true);

        NUMBERS_KEYBOARD.setKeyboard(List.of(
                new KeyboardRow(List.of(new KeyboardButton(Button.ONE.getText()), new KeyboardButton(Button.TWO.getText()), new KeyboardButton(Button.THREE.getText()))),
                new KeyboardRow(List.of(new KeyboardButton(Button.FOUR.getText()), new KeyboardButton(Button.FIVE.getText()), new KeyboardButton(Button.SIX.getText()))),
                new KeyboardRow(List.of(new KeyboardButton(Button.SEVEN.getText()), new KeyboardButton(Button.EIGHT.getText()), new KeyboardButton(Button.NINE.getText()))),
                CANCEL_ROW
        ));
        NUMBERS_KEYBOARD.setResizeKeyboard(true);

        WITHOUT_PHONE_KEYBOARD.setKeyboard(List.of(
                new KeyboardRow(List.of(new KeyboardButton(Button.WITHOUT_PHONE.getText()))),
                CANCEL_ROW
        ));
        WITHOUT_PHONE_KEYBOARD.setResizeKeyboard(true);

        APPROVE_KEYBOARD.setKeyboard(List.of(
                new KeyboardRow(List.of(new KeyboardButton(Button.CANCEL.getText()), new KeyboardButton(Button.APPROVE.getText())))
        ));
        APPROVE_KEYBOARD.setResizeKeyboard(true);
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
}
