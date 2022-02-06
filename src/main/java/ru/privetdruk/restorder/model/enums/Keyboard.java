package ru.privetdruk.restorder.model.enums;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.List;

public enum Keyboard {
    MAIN_MENU_VIEW_MENU(List.of(new KeyboardRow(List.of(
            new KeyboardButton(Button.SETTINGS.getText()),
            new KeyboardButton(Button.INFORMATION.getText()))
    )));

    private final List<KeyboardRow> keyboardRows;

    Keyboard(List<KeyboardRow> keyboardRows) {
        this.keyboardRows = keyboardRows;
    }

    public List<KeyboardRow> getKeyboardRows() {
        return this.keyboardRows;
    }
}
