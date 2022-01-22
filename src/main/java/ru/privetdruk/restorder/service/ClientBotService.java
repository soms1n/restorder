package ru.privetdruk.restorder.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import ru.privetdruk.restorder.model.enums.Button;
import ru.privetdruk.restorder.model.enums.State;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class ClientBotService {
    private final UserService userService;
    private final KeyboardService keyboardService;
    private final InlineKeyboardMarkup keyboard = new InlineKeyboardMarkup();
    private final Map<Long, State> stateMap = new HashMap<>();

    public SendMessage handleUpdate(Update update) {
        Message message = update.getMessage();
        ru.privetdruk.restorder.model.entity.User user = null;

        if (message != null) {
            User from = message.getFrom();
            log.info("user: " + from);
            log.info("message: " + message.getText());

            user = userService.findByTelegramId(from.getId());

            if (user == null) {

                SendMessage sendMessage = new SendMessage(message.getChatId().toString(), "Регистрация");
                keyboard.setKeyboard(List.of(List.of(
                        keyboardService.createButton(Button.REGISTRATION)
                )));
                sendMessage.setReplyMarkup(keyboard);
                return sendMessage;
            }
        }

        CallbackQuery callbackQuery = update.getCallbackQuery();


        if (callbackQuery != null) {
            Button button = Button.fromName(callbackQuery.getData());

            if(button == Button.REGISTRATION) {
                user = new ru.privetdruk.restorder.model.entity.User();
                user.setMiddleName("middle name");
                user.setTelegramId(callbackQuery.getFrom().getId());
                userService.save(user);
                stateMap.put(user.getTelegramId(), State.REGISTRATION);
            }

        }

        if (user != null) {
            State state = stateMap.get(user.getTelegramId());
            String chatId = message != null ? message.getChatId().toString() : callbackQuery.getMessage().getChatId().toString();

            if (state == State.REGISTRATION) {
                return new SendMessage(chatId, "Введите ФИО:");
            }
        }

        return new SendMessage();
    }
}
