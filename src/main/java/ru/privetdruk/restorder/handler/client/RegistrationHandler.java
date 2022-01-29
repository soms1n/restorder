package ru.privetdruk.restorder.handler.client;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
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
import ru.privetdruk.restorder.service.TavernService;
import ru.privetdruk.restorder.service.UserService;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static ru.privetdruk.restorder.model.consts.MessageText.SELECT_ELEMENT_FOR_EDIT;
import static ru.privetdruk.restorder.model.enums.SubState.EDIT_PERSONAL_DATA;
import static java.util.stream.Collectors.toMap;

@RequiredArgsConstructor
@Component
public class RegistrationHandler implements MessageHandler {
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
            case SHOW_REGISTER_BUTTON -> {
                if (callback != null) {
                    subState = subState.getNextSubState();
                    sendMessage = messageService.configureMessage(chatId, changeState(user, subState).getMessage());

                    break;
                }

                sendMessage = messageService.configureMessage(chatId, subState.getMessage());

                sendMessage.setReplyMarkup(InlineKeyboardMarkup
                        .builder()
                        .keyboard(List.of(List.of(keyboardService.createInlineButton(Button.REGISTRATION))))
                        .build());
            }
            case ENTER_FULL_NAME -> {
                if (!StringUtils.hasText(messageText)) {
                    return messageService.configureMessage(chatId, MessageText.ENTER_EMPTY_VALUE);
                }

                user.setFirstName(messageText);

                sendMessage = messageService.configureMessage(chatId, changeState(user, subState).getMessage());
            }
            case ENTER_TAVERN_NAME -> {
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

                sendMessage = messageService.configureMessage(chatId, MessageText.CHOICE_CITY);

                sendMessage.setReplyMarkup(InlineKeyboardMarkup.builder()
                        .keyboard(keyboardService.createButtonList(Arrays.stream(City.values())
                                        .collect(toMap(City::getDescription, City::getName)),
                                MAX_BUTTONS_PER_ROW))
                        .build()
                );
            }
            case CHOICE_CITY -> {
                if (callback != null) {
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

                    sendMessage = messageService.configureMessage(chatId, changeState(user, subState).getMessage());
                } else {
                    sendMessage = messageService.configureMessage(chatId, subState.getMessage());
                    sendMessage.setReplyMarkup(InlineKeyboardMarkup.builder()
                            .keyboard(keyboardService.createButtonList(Arrays.stream(City.values())
                                            .collect(toMap(City::getDescription, City::getName)),
                                    MAX_BUTTONS_PER_ROW))
                            .build()
                    );
                }
            }
            case ENTER_ADDRESS -> {
                if (!StringUtils.hasText(messageText)) {
                    return messageService.configureMessage(chatId, MessageText.ENTER_EMPTY_VALUE);
                }

                AddressEntity address = user.getTavern().getAddress();
                address.setStreet(messageText);
                nextSubState = changeState(user, subState);

                sendMessage = messageService.configureMessage(chatId, nextSubState.getMessage());
            }
            case ENTER_PHONE_NUMBER -> {
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
            }
            case REGISTRATION_APPROVING -> {
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
            }
            case EDIT_PERSONAL_DATA -> {
                sendMessage = messageService.configureMessage(chatId, SELECT_ELEMENT_FOR_EDIT);

                sendMessage.setReplyMarkup(
                        ReplyKeyboardMarkup
                                .builder()
                                .keyboard(List.of(
                                        new KeyboardRow(List.of(
                                                new KeyboardButton(Button.NAME.getText()),
                                                new KeyboardButton(Button.TAVERN.getText()))),
                                        new KeyboardRow(List.of(
                                                new KeyboardButton(Button.ADDRESS.getText()),
                                                new KeyboardButton(Button.PHONE_NUMBER.getText()))),
                                        new KeyboardRow(List.of(
                                                new KeyboardButton(Button.COMPLETE_REGISTRATION.getText())))))
                                .build());

                Button button = Button.fromText(messageText)
                        .orElse(Button.NOTHING);

                switch (button) {
                    case NAME -> {
                        sendMessage = messageService.configureMessage(chatId, SubState.ENTER_FULL_NAME.getMessage());
                        user.setSubState(SubState.EDIT_NAME);
                        userService.save(user);

                        sendMessage.setReplyMarkup(
                                ReplyKeyboardMarkup
                                        .builder()
                                        .keyboard(List.of(
                                                new KeyboardRow(List.of(
                                                        new KeyboardButton(Button.EDIT_MENU.getText()),
                                                        new KeyboardButton(Button.COMPLETE_REGISTRATION.getText())))))
                                        .build());
                    }
                    case TAVERN -> {
                        sendMessage = messageService.configureMessage(chatId, SubState.ENTER_TAVERN_NAME.getMessage());
                        user.setSubState(SubState.EDIT_TAVERN);
                        userService.save(user);

                        sendMessage.setReplyMarkup(
                                ReplyKeyboardMarkup
                                        .builder()
                                        .keyboard(List.of(
                                                new KeyboardRow(List.of(
                                                        new KeyboardButton(Button.EDIT_MENU.getText()),
                                                        new KeyboardButton(Button.COMPLETE_REGISTRATION.getText())))))
                                        .build());
                    }
                    case PHONE_NUMBER -> {
                        sendMessage = messageService.configureMessage(chatId, SubState.ENTER_PHONE_NUMBER.getMessage());
                        user.setSubState(SubState.EDIT_PHONE_NUMBER);
                        userService.save(user);

                        sendMessage.setReplyMarkup(
                                ReplyKeyboardMarkup
                                        .builder()
                                        .keyboard(List.of(
                                                new KeyboardRow(List.of(
                                                        new KeyboardButton(Button.EDIT_MENU.getText()),
                                                        new KeyboardButton(Button.COMPLETE_REGISTRATION.getText())))))
                                        .build());
                    }
                    case ADDRESS -> {
                        sendMessage = messageService.configureMessage(chatId, SubState.ENTER_ADDRESS.getMessage());
                        user.setSubState(SubState.EDIT_ADDRESS);
                        userService.save(user);

                        sendMessage.setReplyMarkup(
                                ReplyKeyboardMarkup
                                        .builder()
                                        .keyboard(List.of(
                                                new KeyboardRow(List.of(
                                                        new KeyboardButton(Button.EDIT_MENU.getText()),
                                                        new KeyboardButton(Button.COMPLETE_REGISTRATION.getText())))))
                                        .build());
                    }

                       /* case CITY: {
                            sendMessage = messageService.configureMessage(chatId, MessageText.CHOICE_CITY);
                            sendMessage.setReplyMarkup(ReplyKeyboardRemove.builder().removeKeyboard(true).build());

                            user.setSubState(SubState.EDIT_CITY);
                            userService.save(user);

                            return sendMessage;
                        }*/
                    case COMPLETE_REGISTRATION -> {
                        user.setSubState(SubState.WAITING_APPROVE_APPLICATION);
                        userService.save(user);

                        sendMessage = messageService.configureMessage(chatId, SubState.WAITING_APPROVE_APPLICATION.getMessage());
                        sendMessage.setReplyMarkup(ReplyKeyboardRemove.builder().removeKeyboard(true).build());
                    }
                }
            }
            case EDIT_NAME -> {
                if (!StringUtils.hasText(messageText)) {
                    return messageService.configureMessage(chatId, MessageText.ENTER_EMPTY_VALUE);
                }

                Button button = Button.fromText(messageText)
                        .orElse(Button.NOTHING);

                switch (button) {
                    case EDIT_MENU -> {
                        user.setSubState(SubState.EDIT_PERSONAL_DATA);
                        userService.save(user);
                        sendMessage = messageService.configureMessage(chatId, SELECT_ELEMENT_FOR_EDIT);
                        sendMessage.setReplyMarkup(
                                ReplyKeyboardMarkup
                                        .builder()
                                        .keyboard(List.of(
                                                new KeyboardRow(List.of(
                                                        new KeyboardButton(Button.NAME.getText()),
                                                        new KeyboardButton(Button.TAVERN.getText()))),
                                                new KeyboardRow(List.of(
                                                        new KeyboardButton(Button.ADDRESS.getText()),
                                                        new KeyboardButton(Button.PHONE_NUMBER.getText()))),
                                                new KeyboardRow(List.of(
                                                        new KeyboardButton(Button.COMPLETE_REGISTRATION.getText())))))
                                        .build());

                        return sendMessage;
                    }
                    case COMPLETE_REGISTRATION -> {
                        user.setSubState(SubState.WAITING_APPROVE_APPLICATION);
                        userService.save(user);

                        sendMessage = messageService.configureMessage(chatId, SubState.WAITING_APPROVE_APPLICATION.getMessage());
                        sendMessage.setReplyMarkup(ReplyKeyboardRemove.builder().removeKeyboard(true).build());
                    }
                }

                user.setFirstName(messageText);

                sendMessage.setReplyMarkup(
                        ReplyKeyboardMarkup
                                .builder()
                                .keyboard(List.of(
                                        new KeyboardRow(List.of(
                                                new KeyboardButton(Button.EDIT_MENU.getText()),
                                                new KeyboardButton(Button.COMPLETE_REGISTRATION.getText())))))
                                .build());
            }
            case EDIT_TAVERN -> {
                if (!StringUtils.hasText(messageText)) {
                    return messageService.configureMessage(chatId, MessageText.ENTER_EMPTY_VALUE);
                }

                Button button = Button.fromText(messageText)
                        .orElse(Button.NOTHING);

                switch (button) {
                    case EDIT_MENU -> {
                        user.setSubState(SubState.EDIT_PERSONAL_DATA);
                        userService.save(user);
                        sendMessage = messageService.configureMessage(chatId, SELECT_ELEMENT_FOR_EDIT);
                        sendMessage.setReplyMarkup(
                                ReplyKeyboardMarkup
                                        .builder()
                                        .keyboard(List.of(
                                                new KeyboardRow(List.of(
                                                        new KeyboardButton(Button.NAME.getText()),
                                                        new KeyboardButton(Button.TAVERN.getText()))),
                                                new KeyboardRow(List.of(
                                                        new KeyboardButton(Button.ADDRESS.getText()),
                                                        new KeyboardButton(Button.PHONE_NUMBER.getText()))),
                                                new KeyboardRow(List.of(
                                                        new KeyboardButton(Button.COMPLETE_REGISTRATION.getText())))))
                                        .build());

                        return sendMessage;
                    }
                    case COMPLETE_REGISTRATION -> {
                        user.setSubState(SubState.WAITING_APPROVE_APPLICATION);
                        userService.save(user);

                        sendMessage = messageService.configureMessage(chatId, SubState.WAITING_APPROVE_APPLICATION.getMessage());
                        sendMessage.setReplyMarkup(ReplyKeyboardRemove.builder().removeKeyboard(true).build());
                    }
                }

                user.getTavern().setName(messageText);

                sendMessage.setReplyMarkup(
                        ReplyKeyboardMarkup
                                .builder()
                                .keyboard(List.of(
                                        new KeyboardRow(List.of(
                                                new KeyboardButton(Button.EDIT_MENU.getText()),
                                                new KeyboardButton(Button.COMPLETE_REGISTRATION.getText())))))
                                .build());
            }
            case EDIT_PHONE_NUMBER -> {
                if (!StringUtils.hasText(messageText)) {
                    return messageService.configureMessage(chatId, MessageText.ENTER_EMPTY_VALUE);
                }

                Button button = Button.fromText(messageText)
                        .orElse(Button.NOTHING);

                switch (button) {
                    case EDIT_MENU -> {
                        user.setSubState(SubState.EDIT_PERSONAL_DATA);
                        userService.save(user);
                        sendMessage = messageService.configureMessage(chatId, SELECT_ELEMENT_FOR_EDIT);
                        sendMessage.setReplyMarkup(
                                ReplyKeyboardMarkup
                                        .builder()
                                        .keyboard(List.of(
                                                new KeyboardRow(List.of(
                                                        new KeyboardButton(Button.NAME.getText()),
                                                        new KeyboardButton(Button.TAVERN.getText()))),
                                                new KeyboardRow(List.of(
                                                        new KeyboardButton(Button.ADDRESS.getText()),
                                                        new KeyboardButton(Button.PHONE_NUMBER.getText()))),
                                                new KeyboardRow(List.of(
                                                        new KeyboardButton(Button.COMPLETE_REGISTRATION.getText())))))
                                        .build());

                        return sendMessage;
                    }
                    case COMPLETE_REGISTRATION -> {
                        user.setSubState(SubState.WAITING_APPROVE_APPLICATION);
                        userService.save(user);

                        sendMessage = messageService.configureMessage(chatId, SubState.WAITING_APPROVE_APPLICATION.getMessage());
                        sendMessage.setReplyMarkup(ReplyKeyboardRemove.builder().removeKeyboard(true).build());
                    }
                }

                // TODO валидация номера

                ContactEntity contact = user.getContacts().stream()
                        .filter(contactEntity -> contactEntity.getType() == ContractType.MOBILE)
                        .findFirst()
                        .get();

                contact.setValue(messageText);

                sendMessage.setReplyMarkup(
                        ReplyKeyboardMarkup.builder()
                                .keyboard(List.of(
                                        new KeyboardRow(List.of(
                                                new KeyboardButton(Button.EDIT_MENU.getText()),
                                                new KeyboardButton(Button.COMPLETE_REGISTRATION.getText())))))
                                .build()
                );
            }
            case EDIT_ADDRESS -> {
                if (!StringUtils.hasText(messageText)) {
                    return messageService.configureMessage(chatId, MessageText.ENTER_EMPTY_VALUE);
                }

                Button button = Button.fromText(messageText)
                        .orElse(Button.NOTHING);

                switch (button) {
                    case EDIT_MENU -> {
                        user.setSubState(SubState.EDIT_PERSONAL_DATA);
                        userService.save(user);
                        sendMessage = messageService.configureMessage(chatId, SELECT_ELEMENT_FOR_EDIT);
                        sendMessage.setReplyMarkup(
                                ReplyKeyboardMarkup.builder()
                                        .keyboard(List.of(
                                                new KeyboardRow(List.of(
                                                        new KeyboardButton(Button.NAME.getText()),
                                                        new KeyboardButton(Button.TAVERN.getText()))),
                                                new KeyboardRow(List.of(
                                                        new KeyboardButton(Button.ADDRESS.getText()),
                                                        new KeyboardButton(Button.PHONE_NUMBER.getText()))),
                                                new KeyboardRow(List.of(
                                                        new KeyboardButton(Button.COMPLETE_REGISTRATION.getText())))))
                                        .build()
                        );

                        return sendMessage;
                    }
                    case COMPLETE_REGISTRATION -> {
                        user.setSubState(SubState.WAITING_APPROVE_APPLICATION);
                        userService.save(user);

                        sendMessage = messageService.configureMessage(chatId, SubState.WAITING_APPROVE_APPLICATION.getMessage());
                        sendMessage.setReplyMarkup(ReplyKeyboardRemove.builder().removeKeyboard(true).build());
                    }
                }


                AddressEntity address = user.getTavern().getAddress();
                address.setStreet(messageText);

                sendMessage.setReplyMarkup(
                        ReplyKeyboardMarkup
                                .builder()
                                .keyboard(List.of(
                                        new KeyboardRow(List.of(
                                                new KeyboardButton(Button.EDIT_MENU.getText()),
                                                new KeyboardButton(Button.COMPLETE_REGISTRATION.getText())))))
                                .build());
            }

    /*        case EDIT_CITY: {
                Button button = Button.fromText(messageText);

                if (button != null) {
                    switch (button) {
                        case EDIT_MENU: {
                            user.setSubState(SubState.EDIT_PERSONAL_DATA);
                            userService.save(user);
                            sendMessage = messageService.configureMessage(chatId, SELECT_ELEMENT_FOR_EDIT);
                            sendMessage.setReplyMarkup(
                                    ReplyKeyboardMarkup
                                            .builder()
                                            .keyboard(List.of(
                                                    new KeyboardRow(List.of(
                                                            new KeyboardButton(Button.NAME.getText()),
                                                            new KeyboardButton(Button.TAVERN.getText()),
                                                            new KeyboardButton(Button.PHONE_NUMBER.getText()))),
                                                    new KeyboardRow(List.of(
                                                            new KeyboardButton(Button.ADDRESS.getText()),
                                                            new KeyboardButton(Button.CITY.getText()))),
                                                    new KeyboardRow(List.of(
                                                            new KeyboardButton(Button.COMPLETE_REGISTRATION.getText())))))
                                            .build());

                            return sendMessage;
                        }
                        case COMPLETE_REGISTRATION: {
                            user.setSubState(SubState.WAITING_APPROVE_APPLICATION);
                            userService.save(user);

                            sendMessage = messageService.configureMessage(chatId, SubState.WAITING_APPROVE_APPLICATION.getMessage());
                            break;
                        }
                    }
                }

                if (callback != null) {
                    String data = callback.getData();
                    City city = City.fromName(data);

                    if (city == null) {
                        return messageService.configureMessage(chatId, MessageText.CITY_IS_EMPTY);
                    }

                    TavernEntity tavern = user.getTavern();
                    tavern.getAddress().setCity(city);
                    tavernService.save(tavern);

                    sendMessage.setReplyMarkup(
                            ReplyKeyboardMarkup
                                    .builder()
                                    .keyboard(List.of(
                                            new KeyboardRow(List.of(
                                                    new KeyboardButton(Button.EDIT_MENU.getText()),
                                                    new KeyboardButton(Button.COMPLETE_REGISTRATION.getText())))))
                                    .build());
                } else {
                    sendMessage = messageService.configureMessage(chatId, MessageText.CHOICE_CITY);
                    sendMessage.setReplyMarkup(InlineKeyboardMarkup.builder()
                            .keyboard(keyboardService.createButtonList(Arrays.stream(City.values())
                                            .collect(toMap(City::getDescription, City::getName)),
                                    MAX_BUTTONS_PER_ROW))
                            .build()
                    );
                }
            }
*/
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
