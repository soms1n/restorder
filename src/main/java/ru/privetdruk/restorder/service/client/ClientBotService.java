package ru.privetdruk.restorder.service.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.privetdruk.restorder.handler.MessageHandler;
import ru.privetdruk.restorder.model.entity.UserEntity;
import ru.privetdruk.restorder.model.enums.*;
import ru.privetdruk.restorder.service.UserService;

import java.util.Map;

@Slf4j
@Service
public class ClientBotService {
    private final UserService userService;

    private final boolean debugMessage;
    private final Map<State, MessageHandler> handlers;

    public ClientBotService(UserService userService,
                            ClientHandlerService handlerService,
                            @Value("${bot.client.debug.message:false}") String debugMessage) {
        this.userService = userService;
        this.handlers = handlerService.loadHandlers();
        this.debugMessage = Boolean.parseBoolean(debugMessage);
    }

    public SendMessage handleUpdate(Update update) {
        Message message = update.getMessage();
        CallbackQuery callback = update.getCallbackQuery();

        Long telegramUserId = null;

        if (message != null) {
            telegramUserId = message.getFrom().getId();

            if (debugMessage) {
                log.info(message.toString());
            }
        }

        if (message == null || (!message.hasText() && message.getContact() == null)) {
            if (callback == null) {
                return new SendMessage();
            }

            message = callback.getMessage();
            telegramUserId = message.getChat().getId();
        }

        final Long finalTelegramUserId = telegramUserId;

        UserEntity user = userService.findByTelegramId(telegramUserId, UserType.CLIENT)
                .orElseGet(() -> userService.create(
                        finalTelegramUserId,
                        State.REGISTRATION_TAVERN,
                        UserType.CLIENT
                ));

        State state = prepareState(message, user, callback);

        try {
            return handlers.get(state)
                    .handle(user, message, callback);
        } catch (Throwable t) {
            log.error(
                    "Произошла непредвиденная ошибка."
                            + System.lineSeparator() + System.lineSeparator()
                            + user + System.lineSeparator()
                            + "Сообщение: " + message.getText() + System.lineSeparator()
                            + (callback == null ? "" : callback)
            );
            return new SendMessage();
        }
    }

    private State prepareState(Message message, UserEntity user, CallbackQuery callback) {
        if (!StringUtils.hasText(message.getText())) {
            return user.getState();
        }

        String[] messageSplit = message.getText().split(" ");
        Command command = Command.fromCommand(messageSplit[Command.MESSAGE_INDEX]);
        if (command == Command.START && messageSplit.length == 2) {
            return State.EVENT;
        } else if (user.isRegistered() && command == Command.MAIN_MENU) {
            userService.updateState(user, State.MAIN_MENU);
        }

        if (callback != null && StringUtils.hasText(callback.getData())) {
            String[] callbackData = callback.getData().split(" ");

            Button button = Button.fromName(callbackData[0]);
            if (button == Button.ACCEPT && callbackData.length == 2) {
                userService.update(user, State.ADMIN, SubState.APPROVE_TAVERN);
            }
        }

        return user.getState();
    }
}
