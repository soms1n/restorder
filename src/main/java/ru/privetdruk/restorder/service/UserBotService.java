package ru.privetdruk.restorder.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.privetdruk.restorder.handler.client.BookingHandler;
import ru.privetdruk.restorder.model.entity.UserEntity;
import ru.privetdruk.restorder.model.enums.Role;
import ru.privetdruk.restorder.model.enums.State;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserBotService {
    private final UserService userService;
    private final BookingHandler bookingHandler;

    public SendMessage handleUpdate(Update update) {
        Message message = update.getMessage();
        CallbackQuery callback = update.getCallbackQuery();
        log.info("user: " + message.getFrom());
        Long telegramUserId = message.getFrom().getId();

        UserEntity user = userService.findByTelegramId(telegramUserId)
                .orElseGet(() -> userService.create(telegramUserId,
                        State.BOOKING,
                        State.BOOKING.getInitialSubState(),
                        Role.USER));

        return bookingHandler.handle(user, message, callback);
    }
}
