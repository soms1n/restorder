package ru.privetdruk.restorder.handler.client;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import ru.privetdruk.restorder.handler.MessageHandler;
import ru.privetdruk.restorder.model.consts.MessageText;
import ru.privetdruk.restorder.model.entity.AddressEntity;
import ru.privetdruk.restorder.model.entity.ContactEntity;
import ru.privetdruk.restorder.model.entity.TavernEntity;
import ru.privetdruk.restorder.model.entity.UserEntity;
import ru.privetdruk.restorder.model.enums.Button;
import ru.privetdruk.restorder.model.enums.City;
import ru.privetdruk.restorder.model.enums.ContractType;
import ru.privetdruk.restorder.model.enums.SubState;
import ru.privetdruk.restorder.service.KeyboardService;
import ru.privetdruk.restorder.service.MessageService;
import ru.privetdruk.restorder.service.UserService;

import java.util.List;

@RequiredArgsConstructor
@Component
public class RegistrationHandler implements MessageHandler {
    private final static int LAST_NAME_INDEX = 0;
    private final static int FIRST_NAME_INDEX = 1;
    private final static int MIDDLE_NAME_INDEX = 2;

    private final KeyboardService keyboardService;
    private final MessageService messageService;
    private final UserService userService;

    @Override
    public SendMessage handle(UserEntity user, Message message, CallbackQuery callback) {
        String messageText = message.getText();
        Long chatId = message.getChatId();
        SubState subState = user.getSubState();

        switch (subState) {
            case SHOW_REGISTER_BUTTON: {
                if (callback != null) {
                    subState = subState.getNextSubState();
                    break;
                }

                SendMessage sendMessage = messageService.configureMessage(chatId, subState.getMessage());

                InlineKeyboardMarkup keyboard = new InlineKeyboardMarkup();
                keyboard.setKeyboard(List.of(List.of(
                        keyboardService.createButton(Button.REGISTRATION)
                )));

                sendMessage.setReplyMarkup(keyboard);

                return sendMessage;
            }
            case ENTER_FULL_NAME: {
                if (!StringUtils.hasText(messageText)) {
                    return messageService.configureMessage(chatId, MessageText.ENTER_EMPTY_VALUE);
                }

                String[] fullName = messageText.split(" ");

                if (fullName.length == 1) {
                    return messageService.configureMessage(chatId, MessageText.FIRST_MIDDLE_NAME_IS_EMPTY);
                } else if (fullName.length == 2) {
                    return messageService.configureMessage(chatId, MessageText.MIDDLE_NAME_IS_EMPTY);
                }

                user.setLastName(fullName[LAST_NAME_INDEX]);
                user.setFirstName(fullName[FIRST_NAME_INDEX]);
                user.setMiddleName(fullName[MIDDLE_NAME_INDEX]);

                break;
            }
            case ENTER_TAVERN_NAME: {
                if (!StringUtils.hasText(messageText)) {
                    return messageService.configureMessage(chatId, MessageText.ENTER_EMPTY_VALUE);
                }

                TavernEntity tavern = TavernEntity.builder()
                        .name(messageText)
                        .owner(user)
                        .build();

                tavern.addEmployee(user);
                user.setTavern(tavern);

                break;
            }
            case CHOICE_CITY: {
                if (callback == null) {
                    return messageService.configureMessage(chatId, MessageText.CITY_IS_EMPTY); // TODO добавить кнопки
                }

                String data = callback.getData();
                City city = City.fromName(data);

                if (city == null) {
                    return messageService.configureMessage(chatId, MessageText.UNABLE_DETERMINE_CITY); // TODO добавить кнопки
                }

                TavernEntity tavern = user.getTavern();

                AddressEntity address = AddressEntity.builder()
                        .tavern(tavern)
                        .city(city)
                        .build();

                tavern.setAddress(address);

                break;
            }
            case ENTER_ADDRESS: {
                if (!StringUtils.hasText(messageText)) {
                    return messageService.configureMessage(chatId, MessageText.ENTER_EMPTY_VALUE);
                }


                AddressEntity address = user.getTavern().getAddress();
                address.setStreet(messageText);

                break;
            }
            case ENTER_PHONE_NUMBER: {
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

                break;
            }
            default: break;
        }

        SubState nextSubState = subState.getNextSubState();

        user.setState(nextSubState.getState());
        user.setSubState(nextSubState);

        userService.save(user);

        return messageService.configureMessage(chatId, nextSubState.getMessage());
    }
}
