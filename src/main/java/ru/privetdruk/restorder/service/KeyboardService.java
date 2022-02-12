package ru.privetdruk.restorder.service;

import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import ru.privetdruk.restorder.model.enums.Button;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
public class KeyboardService {
    public static final ReplyKeyboardMarkup SHARE_PHONE_KEYBOARD = new ReplyKeyboardMarkup();

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
