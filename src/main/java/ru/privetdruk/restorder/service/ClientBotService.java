package ru.privetdruk.restorder.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
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
        CallbackQuery callback = update.getCallbackQuery();

        Long telegramUserId = null;

        if (message != null) {
            telegramUserId = message.getFrom().getId();

            log.info("user: " + message.getFrom());
        }

        if (message == null || !message.hasText()) {
            if (callback == null) {
                return new SendMessage();
            }

            message = callback.getMessage();
            telegramUserId = message.getChat().getId();
        }

        final Long finalTelegramUserId = telegramUserId;

        UserEntity user = userService.findByTelegramId(telegramUserId)
                .orElseGet(() -> userService.createClientAdmin(finalTelegramUserId));

        return handlers.get(user.getState())
                .handle(user, message, callback);
    }
}
