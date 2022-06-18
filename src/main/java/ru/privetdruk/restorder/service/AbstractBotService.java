package ru.privetdruk.restorder.service;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.privetdruk.restorder.handler.MessageHandler;
import ru.privetdruk.restorder.model.entity.UserEntity;
import ru.privetdruk.restorder.model.enums.State;

import java.util.Map;
import java.util.Optional;

@Slf4j
public abstract class AbstractBotService {
    protected final UserService userService;
    protected final Map<State, MessageHandler> handlers;

    public AbstractBotService(UserService userService, BotHandler handlerService) {
        this.userService = userService;
        this.handlers = handlerService.loadHandlers();
    }

    public abstract SendMessage handleUpdate(Update update);

    protected Optional<SendMessage> prepareUpdate(ShortUpdate shortUpdate) {
        Message message = shortUpdate.getMessage();
        CallbackQuery callback = shortUpdate.getCallback();

        if (message != null) {
            shortUpdate.setTelegramUserId(message.getFrom().getId());

            log.debug(message.toString());
        }

        if (message == null || (!message.hasText() && message.getContact() == null)) {
            if (callback == null) {
                return Optional.of(new SendMessage());
            }

            message = callback.getMessage();
            shortUpdate.setTelegramUserId(message.getChat().getId());
            shortUpdate.setMessage(message);
        }

        return Optional.empty();
    }

    protected SendMessage getSendMessage(ShortUpdate shortUpdate, UserEntity user, MessageHandler messageHandler) {
        try {
            return messageHandler
                    .handle(user, shortUpdate.getMessage(), shortUpdate.getCallback());
        } catch (Throwable t) {
            log.error(
                    "Произошла непредвиденная ошибка."
                            + System.lineSeparator() + System.lineSeparator()
                            + user + System.lineSeparator()
                            + "Сообщение: " + shortUpdate.getMessage().getText() + System.lineSeparator()
                            + (shortUpdate.getCallback() == null ? "" : shortUpdate.getCallback()),
                    t
            );
            return new SendMessage();
        }
    }

    @Builder
    @Getter
    @Setter
    public static class ShortUpdate {
        private Message message;
        private CallbackQuery callback;
        private Long telegramUserId;
    }
}
