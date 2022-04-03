package ru.privetdruk.restorder.handler.user;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import ru.privetdruk.restorder.handler.MessageHandler;
import ru.privetdruk.restorder.model.consts.MessageText;
import ru.privetdruk.restorder.model.entity.UserEntity;
import ru.privetdruk.restorder.model.enums.City;
import ru.privetdruk.restorder.model.enums.State;
import ru.privetdruk.restorder.model.enums.SubState;
import ru.privetdruk.restorder.service.KeyboardService;
import ru.privetdruk.restorder.service.MessageService;
import ru.privetdruk.restorder.service.UserService;

@Component
@RequiredArgsConstructor
public class RegistrationHandler implements MessageHandler {
    private final MessageService messageService;
    private final UserService userService;
    private final BookingHandler bookingHandler;

    @Override
    public SendMessage handle(UserEntity user, Message message, CallbackQuery callback) {
        String messageText = message.getText();
        Long chatId = message.getChatId();

        switch (user.getSubState()) {
            case SHOW_REGISTER_BUTTON -> {
                userService.updateSubState(user, SubState.CITY_SELECT);

                return messageService.configureMessage(chatId, MessageText.GREETING, KeyboardService.CITIES_KEYBOARD);
            }
            case CITY_SELECT -> {
                City city = City.fromDescription(messageText);
                if (city == null) {
                    return messageService.configureMessage(chatId, MessageText.INCORRECT_VALUE_TRY_AGAIN, KeyboardService.CITIES_KEYBOARD);
                }

                user.setCity(city);
                userService.updateState(user, State.BOOKING);

                return bookingHandler.handle(user, message, callback);
            }
        }

        return new SendMessage();
    }
}
