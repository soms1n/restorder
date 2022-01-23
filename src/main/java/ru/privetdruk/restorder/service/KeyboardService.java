package ru.privetdruk.restorder.service;

import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import ru.privetdruk.restorder.model.enums.Button;

@Service
public class KeyboardService {
    public InlineKeyboardButton createButton(Button button) {
        InlineKeyboardButton inlineKeyboardButton = new InlineKeyboardButton(button.getText());
        inlineKeyboardButton.setCallbackData(button.name());
        return inlineKeyboardButton;
    }
}
