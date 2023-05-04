package ru.privetdruk.restorder.handler.client;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Contact;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import reactor.core.scheduler.Schedulers;
import ru.privetdruk.restorder.handler.MessageHandler;
import ru.privetdruk.restorder.model.consts.MessageText;
import ru.privetdruk.restorder.model.entity.AddressEntity;
import ru.privetdruk.restorder.model.entity.ContactEntity;
import ru.privetdruk.restorder.model.entity.TavernEntity;
import ru.privetdruk.restorder.model.entity.UserEntity;
import ru.privetdruk.restorder.model.enums.*;
import ru.privetdruk.restorder.service.KeyboardService;
import ru.privetdruk.restorder.service.TavernService;
import ru.privetdruk.restorder.service.TelegramApiService;
import ru.privetdruk.restorder.service.UserService;
import ru.privetdruk.restorder.service.util.ValidationService;

import java.util.List;
import java.util.Optional;

import static ru.privetdruk.restorder.model.consts.MessageText.*;
import static ru.privetdruk.restorder.model.enums.SubState.EDIT_PERSONAL_DATA;
import static ru.privetdruk.restorder.service.MessageService.configureMessage;

@RequiredArgsConstructor
@Component
public class RegistrationTavernHandler implements MessageHandler {
    private final UserService userService;
    private final TelegramApiService telegramApiService;
    private final TavernService tavernService;
    private final ValidationService validationService;

    final String CLAIM_APPROVE_WAIT = "Ваша заявка на модерации, ожидайте.";

    @Override
    @Transactional
    public SendMessage handle(UserEntity user, Message message, CallbackQuery callback) {
        String messageText = message.getText();
        Long chatId = message.getChatId();
        SubState subState = user.getSubState();
        SubState nextSubState;
        SendMessage sendMessage = new SendMessage();

        Button button = Button.fromText(messageText)
                .orElse(Button.NOTHING);

        TavernEntity tavern = user.getTavern();

        switch (subState) {
            case SHOW_REGISTER_BUTTON -> {
                if (button == Button.REGISTRATION) {
                    subState = subState.getNextSubState();
                    sendMessage = configureMessage(chatId, changeState(user, subState).getMessage(), KeyboardService.REMOVE_KEYBOARD);
                } else {
                    sendMessage = configureMessage(chatId, subState.getMessage(), KeyboardService.REGISTRATION_TAVERN);
                }
            }
            case ENTER_FULL_NAME -> {
                user.setName(messageText);

                sendMessage = configureMessage(chatId, changeState(user, subState).getMessage());
            }
            case ENTER_TAVERN_NAME -> {
                tavern = TavernEntity.builder()
                        .name(messageText)
                        .owner(user)
                        .build();

                tavernService.save(tavern);

                changeState(user, subState);

                sendMessage = configureMessage(
                        chatId,
                        MessageText.ENTER_TAVERN_DESCRIPTION,
                        KeyboardService.WITHOUT_DESCRIPTION_KEYBOARD
                );
            }
            case ENTER_TAVERN_DESCRIPTION -> {
                if (button != Button.WITHOUT_DESCRIPTION) {
                    tavern.setDescription(messageText);
                    tavernService.save(tavern);
                }

                changeState(user, subState);

                sendMessage = configureMessage(chatId, MessageText.CHOICE_CITY, KeyboardService.CITIES_KEYBOARD);
            }
            case CHOICE_CITY -> {
                City city = City.fromDescription(messageText);

                if (city == null) {
                    sendMessage = configureMessage(chatId, subState.getMessage(), KeyboardService.CITIES_KEYBOARD);
                } else {
                    AddressEntity address = AddressEntity.builder()
                            .tavern(tavern)
                            .city(city)
                            .build();

                    tavern.setAddress(address);

                    tavernService.save(tavern);

                    sendMessage = configureMessage(chatId, changeState(user, subState).getMessage(), KeyboardService.REMOVE_KEYBOARD);
                }
            }
            case ENTER_ADDRESS -> {
                tavern.getAddress().setStreet(messageText);
                tavernService.save(tavern);

                changeState(user, subState);

                sendMessage = configureMessage(chatId, SHARE_PHONE_NUMBER, KeyboardService.SHARE_PHONE_KEYBOARD);
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

                messageText = sendContact.getPhoneNumber().replace("+", "");

                if (validationService.isNotValidPhone(messageText)) {
                    return configureMessage(
                            chatId,
                            INCORRECT_ENTER_PHONE_NUMBER,
                            KeyboardService.SHARE_PHONE_KEYBOARD
                    );
                }

                ContactEntity contact = ContactEntity.builder()
                        .user(user)
                        .type(ContractType.MOBILE)
                        .value(messageText)
                        .build();

                user.addContact(contact);

                changeState(user, subState);

                sendMessage = showPersonalData(user, chatId);
            }
            case REGISTRATION_APPROVING -> {
                if (button == Button.APPROVE) {
                    changeState(user, subState);
                    sendMessage = configureMessage(chatId, SubState.WAITING_APPROVE_APPLICATION.getMessage(), KeyboardService.REMOVE_KEYBOARD);

                    sendClaimToApprove(user);
                } else if (button == Button.EDIT) {
                    sendMessage = configureMessage(chatId, SELECT_ELEMENT_FOR_EDIT);

                    attachMainEditMenu(sendMessage);

                    user.setSubState(EDIT_PERSONAL_DATA);
                    userService.save(user);
                } else {
                    sendMessage = showPersonalData(user, chatId);
                }
            }
            case EDIT_PERSONAL_DATA -> {
                sendMessage = configureMessage(chatId, SELECT_ELEMENT_FOR_EDIT);

                attachMainEditMenu(sendMessage);

                switch (button) {
                    case NAME -> {
                        sendMessage = configureMessage(chatId, SubState.ENTER_FULL_NAME.getMessage());
                        userService.updateSubState(user, SubState.EDIT_NAME);

                        attachEditMenu(sendMessage);
                    }
                    case TAVERN_NAME -> {
                        sendMessage = configureMessage(chatId, SubState.ENTER_TAVERN_NAME.getMessage());
                        userService.updateSubState(user, SubState.EDIT_TAVERN);

                        attachEditMenu(sendMessage);
                    }
                    case DESCRIPTION -> {
                        sendMessage = configureMessage(chatId, SubState.ENTER_TAVERN_DESCRIPTION.getMessage());
                        userService.updateSubState(user, SubState.EDIT_DESCRIPTION);

                        attachEditMenu(sendMessage);
                    }
                    case PHONE_NUMBER -> {
                        sendMessage = configureMessage(chatId, SubState.ENTER_PHONE_NUMBER.getMessage());
                        userService.updateSubState(user, SubState.EDIT_PHONE_NUMBER);

                        attachEditMenu(sendMessage);
                    }
                    case TAVERN_ADDRESS -> {
                        sendMessage = configureMessage(chatId, SubState.ENTER_ADDRESS.getMessage());
                        userService.updateSubState(user, SubState.EDIT_ADDRESS);

                        attachEditMenu(sendMessage);
                    }
                    case CITY -> {
                        sendMessage = configureMessage(chatId, MessageText.CHOICE_CITY);
                        sendMessage.setReplyMarkup(ReplyKeyboardRemove.builder()
                                .removeKeyboard(true)
                                .build());

                        userService.updateSubState(user, SubState.EDIT_CITY);

                        return sendMessage;
                    }
                    case COMPLETE_REGISTRATION -> {
                        userService.updateSubState(user, SubState.WAITING_APPROVE_APPLICATION);

                        sendMessage = configureMessage(chatId, SubState.WAITING_APPROVE_APPLICATION.getMessage());
                        sendMessage.setReplyMarkup(ReplyKeyboardRemove.builder()
                                .removeKeyboard(true)
                                .build());

                        sendClaimToApprove(user);
                    }
                }
            }
            case EDIT_NAME -> {
                if (!isUserPressKeyBoardElement(sendMessage, user, messageText, chatId)) {
                    user.setName(messageText);
                    userService.save(user);
                }
            }
            case EDIT_TAVERN -> {
                if (!isUserPressKeyBoardElement(sendMessage, user, messageText, chatId)) {
                    tavern.setName(messageText);
                    tavernService.save(tavern);
                }
            }
            case EDIT_DESCRIPTION -> {
                if (!isUserPressKeyBoardElement(sendMessage, user, messageText, chatId)) {
                    tavern.setDescription(messageText);
                    tavernService.save(tavern);
                }
            }
            case EDIT_PHONE_NUMBER -> {
                if (!isUserPressKeyBoardElement(sendMessage, user, messageText, chatId)) {
                    final String finalMessageText = messageText;

                    user.getContacts().stream()
                            .filter(contactEntity -> contactEntity.getType() == ContractType.MOBILE)
                            .findFirst()
                            .ifPresent(contact -> contact.setValue(finalMessageText));

                    userService.save(user);
                }
            }
            case EDIT_ADDRESS -> {
                if (!isUserPressKeyBoardElement(sendMessage, user, messageText, chatId)) {
                    tavern.getAddress().setStreet(messageText);
                    tavernService.save(tavern);
                }
            }
            case WAITING_APPROVE_APPLICATION ->
                sendMessage = configureMessage(chatId, CLAIM_APPROVE_WAIT);
        }

        return sendMessage;
    }

    private void attachMainEditMenu(SendMessage sendMessage) {
        sendMessage.setReplyMarkup(
                ReplyKeyboardMarkup.builder()
                        .keyboard(List.of(
                                new KeyboardRow(List.of(
                                        new KeyboardButton(Button.NAME.getText()),
                                        new KeyboardButton(Button.TAVERN_NAME.getText())

                                )),
                                new KeyboardRow(List.of(
                                        new KeyboardButton(Button.DESCRIPTION.getText()),
                                        new KeyboardButton(Button.TAVERN_ADDRESS.getText()),
                                        new KeyboardButton(Button.PHONE_NUMBER.getText())
                                )),
                                new KeyboardRow(List.of(
                                        new KeyboardButton(Button.COMPLETE_REGISTRATION.getText())
                                ))
                        ))
                        .resizeKeyboard(true)
                        .build());
    }

    private void attachEditMenu(SendMessage sendMessage) {
        sendMessage.setReplyMarkup(
                ReplyKeyboardMarkup.builder()
                        .keyboard(List.of(
                                new KeyboardRow(List.of(
                                        new KeyboardButton(Button.EDIT_MENU.getText()),
                                        new KeyboardButton(Button.COMPLETE_REGISTRATION.getText())
                                ))
                        ))
                        .resizeKeyboard(true)
                        .build()
        );
    }

    private void sendClaimToApprove(UserEntity user) {
        String message = "Пользователь с telegramId " + user.getTelegramId() + " запросил подтверждение регистрации. " + System.lineSeparator() + System.lineSeparator()
                + "Данные пользователя " + System.lineSeparator() + System.lineSeparator()
                + "Имя пользователя: " + user.getName() + System.lineSeparator()
                + "Город: " + user.getTavern().getAddress().getCity().getDescription() + System.lineSeparator()
                + "Название заведения: " + user.getTavern().getName() + System.lineSeparator()
                + "Адрес: " + user.getTavern().getAddress().getStreet() + System.lineSeparator() + System.lineSeparator()
                + "Необходимо проверить адрес на валидность и подтвердить регистрацию";

        userService.findUsersByRole(Role.ADMIN).stream()
                .map(UserEntity::getTelegramId)
                .forEach(id ->
                        telegramApiService.sendMessage(
                                        id,
                                        message,
                                        true,
                                        InlineKeyboardMarkup.builder()
                                                .keyboard(List.of(List.of(
                                                        InlineKeyboardButton.builder()
                                                                .callbackData(Button.ACCEPT.getName() + " " + user.getTelegramId())
                                                                .text(Button.ACCEPT.getText())
                                                                .build()
                                                )))
                                                .build()
                                )
                                .subscribeOn(Schedulers.boundedElastic())
                                .subscribe()
                );
    }

    private SubState changeState(UserEntity user, SubState subState) {
        SubState nextSubState = subState.getNextSubState();
        user.setState(nextSubState.getState());
        user.setSubState(nextSubState);

        userService.save(user);

        return nextSubState;
    }

    private boolean isUserPressKeyBoardElement(SendMessage sendMessage, UserEntity user, String messageText, Long chatId) {
        boolean result = false;

        Optional<Button> button = Button.fromText(messageText);

        if (button.isPresent()) {
            switch (button.get()) {
                case EDIT_MENU -> {
                    user.setSubState(SubState.EDIT_PERSONAL_DATA);
                    userService.save(user);
                    sendMessage.setChatId(chatId.toString());
                    sendMessage.setText(SELECT_ELEMENT_FOR_EDIT);
                    attachMainEditMenu(sendMessage);
                }
                case COMPLETE_REGISTRATION -> {
                    user.setSubState(SubState.WAITING_APPROVE_APPLICATION);
                    userService.save(user);

                    sendMessage.setChatId(chatId.toString());
                    sendMessage.setText(SubState.WAITING_APPROVE_APPLICATION.getMessage());
                    sendMessage.setReplyMarkup(ReplyKeyboardRemove.builder()
                            .removeKeyboard(true)
                            .build());

                    sendClaimToApprove(user);
                }
                default -> attachEditMenu(sendMessage);
            }

            result = true;
        } else {
            attachEditMenu(sendMessage);
        }

        return result;
    }

    private SendMessage showPersonalData(UserEntity user, Long chatId) {
        TavernEntity tavern = user.getTavern();

        String yourPersonalData = "<b>Ваши данные</b>" + System.lineSeparator() +
                "Имя: <i>" + user.getName() + "</i>" + System.lineSeparator() +
                "Заведение: <i>" + tavern.getName() + "</i>" + System.lineSeparator() +
                "Описание: <i>" + Optional.ofNullable(tavern.getDescription()).orElse("отсутствует") + "</i>" + System.lineSeparator() +
                "Адрес: <i>" + tavern.getAddress().getStreet() + "</i>" + System.lineSeparator() +
                "Номер телефона: <i>" + user.getContacts().stream()
                .filter(contactEntity -> contactEntity.getType() == ContractType.MOBILE)
                .map(ContactEntity::getValue)
                .findFirst()
                .orElse("") + "</i>" + System.lineSeparator();

        return configureMessage(chatId, yourPersonalData, KeyboardService.REGISTRATION_APPROVING);
    }
}
