package ru.privetdruk.restorder.handler.user;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Contact;
import org.telegram.telegrambots.meta.api.objects.Message;
import ru.privetdruk.restorder.handler.MessageHandler;
import ru.privetdruk.restorder.model.consts.MessageText;
import ru.privetdruk.restorder.model.entity.ContactEntity;
import ru.privetdruk.restorder.model.entity.UserEntity;
import ru.privetdruk.restorder.model.enums.City;
import ru.privetdruk.restorder.model.enums.ContractType;
import ru.privetdruk.restorder.model.enums.State;
import ru.privetdruk.restorder.model.enums.SubState;
import ru.privetdruk.restorder.service.KeyboardService;
import ru.privetdruk.restorder.service.UserService;
import ru.privetdruk.restorder.service.util.ValidationService;

import static ru.privetdruk.restorder.service.MessageService.configureMessage;

@Component
@RequiredArgsConstructor
public class RegistrationHandler implements MessageHandler {
    private final BookingHandler bookingHandler;
    private final UserService userService;
    private final ValidationService validationService;

    @Override
    public SendMessage handle(UserEntity user, Message message, CallbackQuery callback) {
        String messageText = message.getText();
        Long chatId = message.getChatId();

        switch (user.getSubState()) {
            case SHOW_REGISTER_BUTTON -> {
                userService.updateSubState(user, SubState.CITY_SELECT);

                return configureMessage(chatId, MessageText.GREETING, KeyboardService.CITIES_KEYBOARD);
            }
            case CITY_SELECT -> {
                City city = City.fromDescription(messageText);
                if (city == null) {
                    return configureMessage(chatId, MessageText.INCORRECT_VALUE_TRY_AGAIN, KeyboardService.CITIES_KEYBOARD);
                }

                user.setCity(city);

                userService.updateSubState(user, SubState.ENTER_PHONE_NUMBER);

                return configureMessage(chatId, MessageText.ENTER_PHONE_NUMBER, KeyboardService.SHARE_PHONE_KEYBOARD);
            }
            case ENTER_PHONE_NUMBER -> {
                Contact sendContact = message.getContact();
                if (sendContact != null) {
                    messageText = sendContact.getPhoneNumber().replace("+", "");
                }

                if (!StringUtils.hasText(messageText)) {
                    return configureMessage(chatId, MessageText.ENTER_EMPTY_VALUE);
                }

                if (validationService.isNotValidPhone(messageText)) {
                    return configureMessage(
                            chatId,
                            "Вы ввели некорректный номер мобильного телефона. Повторите попытку.",
                            KeyboardService.SHARE_PHONE_KEYBOARD
                    );
                }

                ContactEntity contact = ContactEntity.builder()
                        .user(user)
                        .type(ContractType.MOBILE)
                        .value(messageText)
                        .build();

                user.addContact(contact);

                userService.updateSubState(user, SubState.ENTER_FULL_NAME);

                return configureMessage(chatId, MessageText.ENTER_NAME, KeyboardService.REMOVE_KEYBOARD);
            }
            case ENTER_FULL_NAME -> {
                user.setName(messageText);
                user.setRegistered(true);

                userService.updateState(user, State.BOOKING);

                return bookingHandler.handle(user, message, callback);
            }
        }

        return new SendMessage(chatId.toString(), "Что-то пошло не так...");
    }
}
