package ru.privetdruk.restorder.service.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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

    private final Map<State, MessageHandler> handlers;

    public ClientBotService(UserService userService, ClientHandlerService handlerService) {
        this.userService = userService;
        this.handlers = handlerService.loadHandlers();
    }

    //TODO Transactional отсюда перенести минимум в handler, а лучше в конкретный сервис, надо обсуждать, как лучше. Чтобы долго не стопориться оставляю пока тут
    @Transactional
    public SendMessage handleUpdate(Update update) {
        Message message = update.getMessage();
        CallbackQuery callback = update.getCallbackQuery();

        Long telegramUserId = null;

        if (message != null) {
            telegramUserId = message.getFrom().getId();

            log.info("user: " + message.getFrom());
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

        return handlers.get(state)
                .handle(user, message, callback);
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
