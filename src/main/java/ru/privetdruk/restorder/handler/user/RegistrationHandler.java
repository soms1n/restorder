package ru.privetdruk.restorder.handler.user;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
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
import ru.privetdruk.restorder.service.ContactService;
import ru.privetdruk.restorder.service.KeyboardService;
import ru.privetdruk.restorder.service.UserService;
import ru.privetdruk.restorder.service.util.ValidationService;

import static ru.privetdruk.restorder.model.consts.MessageText.SOMETHING_WENT_WRONG;
import static ru.privetdruk.restorder.service.MessageService.configureMessage;

@Component
@RequiredArgsConstructor
public class RegistrationHandler implements MessageHandler {
    private final BookingHandler bookingHandler;
    private final ContactService contactService;
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

                return configureMessage(chatId, MessageText.SHARE_PHONE_NUMBER, KeyboardService.SHARE_PHONE_KEYBOARD);
            }
            case ENTER_PHONE_NUMBER -> {
                Contact sendContact = message.getContact();

                if (sendContact == null) {
                    return configureMessage(
                            chatId,
                            MessageText.SHARE_PHONE_NUMBER,
                            KeyboardService.SHARE_PHONE_KEYBOARD
                    );
                }

                String phoneNumber = contactService.preparePhoneNumber(sendContact.getPhoneNumber());

                if (validationService.isNotValidPhone(phoneNumber)) {
                    return configureMessage(
                            chatId,
                            MessageText.INCORRECT_PHONE_NUMBER,
                            KeyboardService.SHARE_PHONE_KEYBOARD
                    );
                }

                ContactEntity contact = ContactEntity.builder()
                        .user(user)
                        .type(ContractType.MOBILE)
                        .value(phoneNumber)
                        .build();

                contactService.save(contact);

                userService.updateSubState(user, SubState.ENTER_FULL_NAME);

                return configureMessage(chatId, MessageText.ENTER_NAME, KeyboardService.REMOVE_KEYBOARD);
            }
            case ENTER_FULL_NAME -> {
                if (validationService.isNotValidName(messageText)) {
                    return configureMessage(
                            chatId,
                            "Имя должно содержать только символы кириллицы. Повторите попытку.",
                            KeyboardService.REMOVE_KEYBOARD
                    );
                }

                user.setName(messageText);
                user.setRegistered(true);

                userService.updateState(user, State.BOOKING);

                return bookingHandler.handle(user, message, callback);
            }
        }

        return new SendMessage(chatId.toString(), SOMETHING_WENT_WRONG);
    }
}
