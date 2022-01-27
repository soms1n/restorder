package ru.privetdruk.restorder.handler.client;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
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

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static ru.privetdruk.restorder.model.consts.MessageText.SELECT_ELEMENT_FOR_EDIT;
import static ru.privetdruk.restorder.model.enums.SubState.EDIT_PERSONAL_DATA;

@RequiredArgsConstructor
@Component
public class RegistrationHandler implements MessageHandler {
    private final static int LAST_NAME_INDEX = 0;
    private final static int FIRST_NAME_INDEX = 1;
    private final static int MIDDLE_NAME_INDEX = 2;
    private final static int MAX_BUTTONS_PER_ROW = 8;

    private final KeyboardService keyboardService;
    private final MessageService messageService;
    private final UserService userService;

    @Override
    @Transactional
    public SendMessage handle(UserEntity user, Message message, CallbackQuery callback) {
        String messageText = message.getText();
        Long chatId = message.getChatId();
        SubState subState = user.getSubState();
        SubState nextSubState;
        SendMessage sendMessage = new SendMessage();

        switch (subState) {
            case SHOW_REGISTER_BUTTON: {
                if (callback != null) {
                    subState = subState.getNextSubState();
                    changeState(user, subState);

                    break;
                }

                sendMessage = messageService.configureMessage(chatId, subState.getMessage());

                sendMessage.setReplyMarkup(InlineKeyboardMarkup
                        .builder()
                        .keyboard(List.of(List.of(keyboardService.createInlineButton(Button.REGISTRATION))))
                        .build());

                break;
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

                nextSubState = changeState(user, subState);

                sendMessage = messageService.configureMessage(chatId, nextSubState.getMessage());

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

                changeState(user, subState);

                InlineKeyboardMarkup keyboard = new InlineKeyboardMarkup();
                sendMessage = messageService.configureMessage(chatId, MessageText.CHOICE_CITY);

                List<List<InlineKeyboardButton>> citiesForSelect = keyboardService.createButtonList(
                        Arrays.stream(City.values()).collect(Collectors.toMap(City::getDescription, City::getName)),
                        MAX_BUTTONS_PER_ROW
                );

                keyboard.setKeyboard(citiesForSelect);

                sendMessage.setReplyMarkup(keyboard);

                break;
            }
            case CHOICE_CITY: {
                String data = callback.getData();
                City city = City.fromName(data);

                if (city == null) {
                    return messageService.configureMessage(chatId, MessageText.CITY_IS_EMPTY);
                }

                TavernEntity tavern = user.getTavern();

                AddressEntity address = AddressEntity.builder()
                        .tavern(tavern)
                        .city(city)
                        .build();

                tavern.setAddress(address);

                userService.save(user);

                nextSubState = changeState(user, subState);

                sendMessage = messageService.configureMessage(chatId, nextSubState.getMessage());

                break;
            }
            case ENTER_ADDRESS: {
                if (!StringUtils.hasText(messageText)) {
                    return messageService.configureMessage(chatId, MessageText.ENTER_EMPTY_VALUE);
                }

                AddressEntity address = user.getTavern().getAddress();
                address.setStreet(messageText);
                nextSubState = changeState(user, subState);

                sendMessage = messageService.configureMessage(chatId, nextSubState.getMessage());

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

                changeState(user, subState);

                String yourPersonalData = "Ваши данные:" + System.lineSeparator() +
                        "Имя: " + user.getFirstName() + System.lineSeparator() +
                        "Заведение: " + user.getTavern().getName() + System.lineSeparator() +
                        "Адрес: " + user.getTavern().getAddress().getStreet() + System.lineSeparator() +
                        "Номер телефона: " + user.getContacts()
                        .stream()
                        .filter(contactEntity -> contactEntity.getType() == ContractType.MOBILE)
                        .findFirst().get().getValue() + System.lineSeparator();

                InlineKeyboardMarkup keyboard = new InlineKeyboardMarkup();

                keyboard.setKeyboard(List.of(List.of(keyboardService.createInlineButton(Button.EDIT),
                        keyboardService.createInlineButton(Button.APPROVE))));

                sendMessage = messageService.configureMessage(chatId, yourPersonalData);
                sendMessage.setReplyMarkup(keyboard);

                break;
            }
            case REGISTRATION_APPROVING: {
                if (callback != null) {
                    if (Button.fromName(callback.getData()) == Button.APPROVE) {
                        changeState(user, subState);
                        sendMessage = messageService.configureMessage(chatId, SubState.WAITING_APPROVE_APPLICATION.getMessage());

                    } else {
                        sendMessage = messageService.configureMessage(chatId, SELECT_ELEMENT_FOR_EDIT);

                        List<List<InlineKeyboardButton>> buttons = keyboardService.createButtonList(
                                Stream.of(Button.NAME, Button.TAVERN, Button.PHONE_NUMBER, Button.COMPLETE_REGISTRATION)
                                        .collect(Collectors.toMap(Button::getText, Button::getName)),
                                1);

                        sendMessage.setReplyMarkup(InlineKeyboardMarkup.builder().keyboard(buttons).build());

                        user.setSubState(EDIT_PERSONAL_DATA);
                        userService.save(user);
                    }
                }
                break;
            }
            case EDIT_PERSONAL_DATA: {
                if (callback != null) {
                    switch (Objects.requireNonNull(Button.fromName(callback.getData()))) {
                        case NAME: {
                            changeState(user, SubState.ENTER_FULL_NAME);
                            sendMessage = messageService.configureMessage(chatId, SubState.ENTER_FULL_NAME.getMessage());
                            break;
                        }
                        case TAVERN: {
                            changeState(user, SubState.ENTER_TAVERN_NAME);
                            sendMessage = messageService.configureMessage(chatId, SubState.ENTER_TAVERN_NAME.getMessage());
                            break;
                        }
                        case PHONE_NUMBER: {
                            changeState(user, SubState.ENTER_PHONE_NUMBER);
                            sendMessage = messageService.configureMessage(chatId, SubState.ENTER_PHONE_NUMBER.getMessage());
                            break;
                        }
                        case COMPLETE_REGISTRATION: {
                            changeState(user, SubState.WAITING_APPROVE_APPLICATION);
                            sendMessage = messageService.configureMessage(chatId, SubState.WAITING_APPROVE_APPLICATION.getMessage());
                            break;
                        }
                        default: {
                            break;
                        }
                    }
                }
            }
            default:
                break;
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
