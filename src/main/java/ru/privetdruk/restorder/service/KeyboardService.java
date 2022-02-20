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
    public static final ReplyKeyboardMarkup CATEGORIES  = new ReplyKeyboardMarkup();
    public static final ReplyKeyboardMarkup CATEGORIES_LIST_WITH_CANCEL  = new ReplyKeyboardMarkup();
    public static final ReplyKeyboardMarkup SCHEDULE_KEYBOARD = new ReplyKeyboardMarkup();
    public static final ReplyKeyboardMarkup TABLE_KEYBOARD = new ReplyKeyboardMarkup();

    {
        init();
    }

    private static void init() {
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

        CANCEL_KEYBOARD.setKeyboard(List.of(
                new KeyboardRow(List.of(
                        new KeyboardButton(Button.CANCEL.getText())
                ))
        ));
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
                        new KeyboardButton(Button.MAIN_MENU.getText())
                ))
        ));
        SETTINGS_KEYBOARD.setResizeKeyboard(true);

        GENERAL_KEYBOARD.setKeyboard(List.of(
                new KeyboardRow(List.of(
                        new KeyboardButton(Button.TAVERN_NAME.getText()),
                        new KeyboardButton(Button.CATEGORIES.getText())
                )),
                new KeyboardRow(List.of(
                        new KeyboardButton(Button.CONTACTS.getText()),
                        new KeyboardButton(Button.TAVERN_ADDRESS.getText())
                )),
                new KeyboardRow(List.of(
                        new KeyboardButton(Button.BACK.getText()),
                        new KeyboardButton(Button.MAIN_MENU.getText())
                ))
        ));
        GENERAL_KEYBOARD.setResizeKeyboard(true);

        TAVERN_NAME_KEYBOARD.setKeyboard(List.of(
                new KeyboardRow(List.of(
                        new KeyboardButton(Button.CHANGE.getText())
                )),
                new KeyboardRow(List.of(
                        new KeyboardButton(Button.BACK.getText()),
                        new KeyboardButton(Button.MAIN_MENU.getText())
                ))
        ));
        TAVERN_NAME_KEYBOARD.setResizeKeyboard(true);

        TAVERN_ADDRESS_KEYBOARD.setKeyboard(List.of(
                new KeyboardRow(List.of(
                        new KeyboardButton(Button.CHANGE.getText())
                )),
                new KeyboardRow(List.of(
                        new KeyboardButton(Button.BACK.getText()),
                        new KeyboardButton(Button.MAIN_MENU.getText())
                ))
        ));
        TAVERN_ADDRESS_KEYBOARD.setResizeKeyboard(true);

        TAVERN_CONTACTS_KEYBOARD.setKeyboard(List.of(
                new KeyboardRow(List.of(
                        new KeyboardButton(Button.ADD.getText()),
                        new KeyboardButton(Button.DELETE.getText())
                )),
                new KeyboardRow(List.of(
                        new KeyboardButton(Button.BACK.getText()),
                        new KeyboardButton(Button.MAIN_MENU.getText())
                ))
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
                new KeyboardRow(List.of(
                        new KeyboardButton(Button.BACK.getText()),
                        new KeyboardButton(Button.MAIN_MENU.getText())
                ))
        ));
        PROFILE_KEYBOARD.setResizeKeyboard(true);

        PROFILE_NAME_KEYBOARD.setKeyboard(List.of(
                new KeyboardRow(List.of(
                        new KeyboardButton(Button.CHANGE.getText())
                )),
                new KeyboardRow(List.of(
                        new KeyboardButton(Button.BACK.getText()),
                        new KeyboardButton(Button.MAIN_MENU.getText())
                ))
        ));
        PROFILE_NAME_KEYBOARD.setResizeKeyboard(true);

        USER_CONTACTS_KEYBOARD.setKeyboard(List.of(
                new KeyboardRow(List.of(
                        new KeyboardButton(Button.ADD.getText()),
                        new KeyboardButton(Button.DELETE.getText())
                )),
                new KeyboardRow(List.of(
                        new KeyboardButton(Button.BACK.getText()),
                        new KeyboardButton(Button.MAIN_MENU.getText())
                ))
        ));
        USER_CONTACTS_KEYBOARD.setResizeKeyboard(true);

        EMPLOYEE_KEYBOARD.setKeyboard(List.of(
                new KeyboardRow(List.of(
                        new KeyboardButton(Button.ADD.getText()),
                        new KeyboardButton(Button.DELETE.getText())
                )),
                new KeyboardRow(List.of(
                        new KeyboardButton(Button.BACK.getText()),
                        new KeyboardButton(Button.MAIN_MENU.getText())
                ))
        ));
        EMPLOYEE_KEYBOARD.setResizeKeyboard(true);

        CATEGORIES.setKeyboard(List.of(
                new KeyboardRow(List.of(
                        new KeyboardButton(Button.CHANGE.getText())
                )),
                new KeyboardRow(List.of(
                        new KeyboardButton(Button.BACK.getText()),
                        new KeyboardButton(Button.MAIN_MENU.getText())
                ))
        ));
        CATEGORIES.setResizeKeyboard(true);

        List<KeyboardRow> rows = new ArrayList<>();
        for (Category category : Category.values()) {
            rows.add(new KeyboardRow(List.of(new KeyboardButton(category.getDescription()))));
        }

        rows.add(new KeyboardRow(List.of(new KeyboardButton(Button.CANCEL.getText()))));

        CATEGORIES_LIST_WITH_CANCEL.setKeyboard(rows);
        CATEGORIES_LIST_WITH_CANCEL.setResizeKeyboard(true);
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
