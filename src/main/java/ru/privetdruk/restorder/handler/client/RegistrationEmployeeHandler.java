package ru.privetdruk.restorder.handler.client;

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
import ru.privetdruk.restorder.model.enums.ContractType;
import ru.privetdruk.restorder.model.enums.SubState;
import ru.privetdruk.restorder.service.KeyboardService;
import ru.privetdruk.restorder.service.MessageService;
import ru.privetdruk.restorder.service.UserService;

@Component
@RequiredArgsConstructor
public class RegistrationEmployeeHandler implements MessageHandler {
    private final MessageService messageService;
    private final UserService userService;
    private final MainMenuHandler mainMenuHandler;

    @Override
    public SendMessage handle(UserEntity user, Message message, CallbackQuery callback) {
        SendMessage sendMessage = new SendMessage();

        Long chatId = message.getChatId();
        String messageText = message.getText();
        SubState subState = user.getSubState();

        switch (subState) {
            case REGISTER_EMPLOYEE_BUTTON_PRESS -> {
                return messageService.configureMessage(chatId, changeState(user, subState).getMessage());
            }
            case ENTER_EMPLOYEE_FULL_NAME -> {
                if (!StringUtils.hasText(messageText)) {
                    return messageService.configureMessage(chatId, MessageText.ENTER_EMPTY_VALUE);
                }

                user.setName(messageText);

                sendMessage = messageService.configureMessage(chatId, changeState(user, subState).getMessage());

                sendMessage.setReplyMarkup(KeyboardService.SHARE_PHONE_KEYBOARD);

                return sendMessage;
            }
            case ENTER_EMPLOYEE_PHONE_NUMBER -> {
                Contact sendContact = message.getContact();
                if (sendContact != null) {
                    messageText = sendContact.getPhoneNumber().replace("+", "");
                }

                if (!StringUtils.hasText(messageText)) {
                    return messageService.configureMessage(chatId, MessageText.ENTER_EMPTY_VALUE);
                }

                // TODO валидация номера

                ContactEntity contact = ContactEntity.builder()
                        .user(user)
                        .type(ContractType.MOBILE)
                        .value(messageText)
                        .build();

                user.addContact(contact);

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

