package ru.privetdruk.restorder.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import ru.privetdruk.restorder.handler.MessageHandler;
import ru.privetdruk.restorder.model.entity.UserEntity;
import ru.privetdruk.restorder.model.enums.State;

import java.util.Map;

@Slf4j
@Service
public class ClientBotService {
    private final UserService userService;
    private final Map<State, MessageHandler> handlers;

    public ClientBotService(UserService userService, ClientHandlerService handlerService) {
        this.userService = userService;
        this.handlers = handlerService.loadHandlers();
    }

    public SendMessage handleUpdate(Update update) {
        Message message = update.getMessage();
        CallbackQuery callbackQuery = update.getCallbackQuery();

        if (message == null || !message.hasText()) {
            if (callbackQuery == null) {
                return new SendMessage();
            }

            message = callbackQuery.getMessage();
        }

        User telegramUser = message.getFrom();

        UserEntity user = userService.findByTelegramId(telegramUser.getId())
                .orElseGet(() -> userService.createClientAdmin(telegramUser.getId()));

        return handlers.get(user.getState())
                .handle(user, message);
    }
}
