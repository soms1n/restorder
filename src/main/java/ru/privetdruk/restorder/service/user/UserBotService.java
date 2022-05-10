package ru.privetdruk.restorder.service.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.privetdruk.restorder.handler.MessageHandler;
import ru.privetdruk.restorder.model.entity.UserEntity;
import ru.privetdruk.restorder.model.enums.Command;
import ru.privetdruk.restorder.model.enums.Role;
import ru.privetdruk.restorder.model.enums.State;
import ru.privetdruk.restorder.model.enums.UserType;
import ru.privetdruk.restorder.service.UserService;

import java.util.Map;

@Service
@Slf4j
public class UserBotService {
    private final UserService userService;

    private final Map<State, MessageHandler> handlers;

    public UserBotService(UserService userService, UserHandlerService handlerService) {
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

        if (message == null || (!message.hasText() && message.getContact() == null)) {
            if (callback == null) {
                return new SendMessage();
            }

            message = callback.getMessage();
            telegramUserId = message.getChat().getId();
        }

        final Long finalTelegramUserId = telegramUserId;

        UserEntity user = userService.findByTelegramId(telegramUserId, UserType.USER)
                .orElseGet(() -> userService.create(
                        finalTelegramUserId,
                        State.REGISTRATION_USER,
                        Role.USER,
                        UserType.USER
                ));

        prepareState(message, user);

        return handlers.get(user.getState())
                .handle(user, message, callback);
    }

    private void prepareState(Message message, UserEntity user) {
        if (!user.isRegistered() && user.getState() != State.REGISTRATION_USER) {
            userService.updateState(user, State.REGISTRATION_USER);
            return;
        }

        if (user.isRegistered()) {
            String[] messageSplit = message.getText().split(" ");
            Command command = Command.fromCommand(messageSplit[Command.MESSAGE_INDEX]);
            if (command == Command.MAIN_MENU) {
                userService.updateState(user, State.MAIN_MENU);
            }
        }
    }
}
