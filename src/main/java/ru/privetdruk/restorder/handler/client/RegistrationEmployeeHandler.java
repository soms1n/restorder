package ru.privetdruk.restorder.handler.client;

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
import ru.privetdruk.restorder.model.enums.ContractType;
import ru.privetdruk.restorder.model.enums.SubState;
import ru.privetdruk.restorder.service.ContactService;
import ru.privetdruk.restorder.service.UserService;
import ru.privetdruk.restorder.service.util.ValidationService;

import static org.springframework.util.StringUtils.hasText;
import static ru.privetdruk.restorder.service.KeyboardService.SHARE_PHONE_KEYBOARD;
import static ru.privetdruk.restorder.service.MessageService.toMessage;

@Component
@RequiredArgsConstructor
public class RegistrationEmployeeHandler implements MessageHandler {
    private final ContactService contactService;
    private final UserService userService;
    private final MainMenuHandler mainMenuHandler;
    private final ValidationService validationService;

    @Override
    public SendMessage handle(UserEntity user, Message message, CallbackQuery callback) {
        SendMessage sendMessage = new SendMessage();

        Long chatId = message.getChatId();
        String messageText = message.getText();
        SubState subState = user.getSubState();

        switch (subState) {
            case REGISTER_EMPLOYEE_BUTTON_PRESS -> {
                return toMessage(chatId, changeState(user, subState).getMessage());
            }
            case ENTER_EMPLOYEE_FULL_NAME -> {
                if (!hasText(messageText)) {
                    return toMessage(chatId, MessageText.ENTER_EMPTY_VALUE);
                }

                user.setName(messageText);

                sendMessage = toMessage(chatId, changeState(user, subState).getMessage());

                sendMessage.setReplyMarkup(SHARE_PHONE_KEYBOARD);

                return sendMessage;
            }
            case ENTER_EMPLOYEE_PHONE_NUMBER -> {
                Contact sendContact = message.getContact();

                if (sendContact == null) {
                    return toMessage(chatId, MessageText.SHARE_PHONE_NUMBER, SHARE_PHONE_KEYBOARD);
                }

                String phoneNumber = contactService.preparePhoneNumber(sendContact.getPhoneNumber());

                if (validationService.isNotValidPhone(phoneNumber)) {
                    return toMessage(chatId, MessageText.INCORRECT_ENTER_PHONE_NUMBER, SHARE_PHONE_KEYBOARD);
                }

                ContactEntity contact = ContactEntity.builder()
                        .user(user)
                        .type(ContractType.MOBILE)
                        .value(phoneNumber)
                        .build();

                contactService.save(contact);

                changeState(user, subState);

                return mainMenuHandler.handle(user, message, callback);
            }
        }

        return sendMessage;
    }

    private SubState changeState(UserEntity user, SubState subState) {
        SubState nextSubState = subState.getNextSubState();

        user.setState(nextSubState.getState());
        user.setSubState(nextSubState);

        userService.save(user);

        return nextSubState;
    }
}

