package ru.privetdruk.restorder.handler.client;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
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
import ru.privetdruk.restorder.model.enums.*;
import ru.privetdruk.restorder.service.*;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static ru.privetdruk.restorder.model.consts.MessageText.SELECT_ELEMENT_FOR_EDIT;
import static ru.privetdruk.restorder.model.enums.SubState.EDIT_PERSONAL_DATA;
import static java.util.stream.Collectors.toMap;

@RequiredArgsConstructor
@Component
public class RegistrationTavernHandler implements MessageHandler {
    private final static int MAX_BUTTONS_PER_ROW = 8;

    private final KeyboardService keyboardService;
    private final MessageService messageService;
    private final UserService userService;
    private final TelegramApiService telegramApiService;
    private final TavernService tavernService;

    @Value("${bot.client.token}")
    private String botClientToken;

    @Override
    @Transactional
    public SendMessage handle(UserEntity user, Message message, CallbackQuery callback) {
        String messageText = message.getText();
        Long chatId = message.getChatId();
        SubState subState = user.getSubState();
        SubState nextSubState;
        SendMessage sendMessage = new SendMessage();

        /*
        Если сообщение от админа с подтверждением регистрации, отправляем пользователю сообщение и
        переводим в главное меню
        */
        if (user.getRoles().contains(Role.ADMIN)) {
            if (callback != null) {
                String userId = callback.getData().substring(callback.getData().lastIndexOf(" ") + 1);
                Long userTelegramId = Long.valueOf(userId);

                Optional<UserEntity> optionalUserEntity = userService.findByTelegramIdWithLock(userTelegramId);

                if (optionalUserEntity.isPresent()) {
                    UserEntity userEntity = optionalUserEntity.get();

                    if (userEntity.getSubState() == SubState.WAITING_APPROVE_APPLICATION) {
                        telegramApiService.sendMessage(
                                userTelegramId,
                                MessageText.YOUR_CLAIM_WAS_APPROVED,
                                botClientToken,
                                new ReplyKeyboardMarkup(Keyboard.MAIN_MENU_VIEW_MENU.getKeyboardRows())
                        ).subscribe();

                        userEntity.setState(State.MAIN_MENU);
                        userEntity.setSubState(SubState.VIEW_MAIN_MENU);
                        userService.save(userEntity);
                    }
                }

                sendMessage.setChatId(String.valueOf(chatId));
                sendMessage.setText(MessageText.ADMIN_APPROVED_CLAIM);
                sendMessage.setReplyMarkup(ReplyKeyboardRemove.builder().removeKeyboard(true).build());
            }

            return sendMessage;
        }

        switch (subState) {
            case SHOW_REGISTER_BUTTON -> {
                if (callback != null) {
                    subState = subState.getNextSubState();
                    sendMessage = messageService.configureMessage(chatId, changeState(user, subState).getMessage());

                    break;
                }

                sendMessage = messageService.configureMessage(chatId, subState.getMessage());

                sendMessage.setReplyMarkup(InlineKeyboardMarkup.builder()
                        .keyboard(List.of(List.of(keyboardService.createInlineButton(Button.REGISTRATION))))
                        .build());
            }
            case ENTER_FULL_NAME -> {
                if (!StringUtils.hasText(messageText)) {
                    return messageService.configureMessage(chatId, MessageText.ENTER_EMPTY_VALUE);
                }

                user.setName(messageText);

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

                tavernService.save(tavern);

                changeState(user, subState);

                sendMessage = messageService.configureMessage(chatId, MessageText.CHOICE_CITY);

                sendMessage.setReplyMarkup(
                        InlineKeyboardMarkup.builder()
                                .keyboard(keyboardService.createButtonList(Arrays.stream(City.values())
                                        .collect(toMap(City::getDescription, City::getName)), MAX_BUTTONS_PER_ROW))
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

                    sendMessage = messageService.configureMessage(chatId, changeState(user, subState).getMessage());
                } else {
                    sendMessage = messageService.configureMessage(chatId, subState.getMessage());
                    sendMessage.setReplyMarkup(InlineKeyboardMarkup.builder()
                            .keyboard(keyboardService.createButtonList(Arrays.stream(City.values())
                                    .collect(toMap(City::getDescription, City::getName)), MAX_BUTTONS_PER_ROW))
                            .build());
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

                sendMessage = showPersonalData(user, chatId);
            }
            case REGISTRATION_APPROVING -> {
                if (callback != null) {
                    if (Button.fromName(callback.getData()) == Button.APPROVE) {
                        changeState(user, subState);
                        sendMessage = messageService.configureMessage(chatId, SubState.WAITING_APPROVE_APPLICATION.getMessage());

                        sendClaimToApprove(user);
                    } else {
                        sendMessage = messageService.configureMessage(chatId, SELECT_ELEMENT_FOR_EDIT);

                        attachMainEditMenu(sendMessage);

                        user.setSubState(EDIT_PERSONAL_DATA);
                        userService.save(user);
                    }
                } else {
                    sendMessage = showPersonalData(user, chatId);
                }
            }
            case EDIT_PERSONAL_DATA -> {
                sendMessage = messageService.configureMessage(chatId, SELECT_ELEMENT_FOR_EDIT);

                attachMainEditMenu(sendMessage);

                Button button = Button.fromText(messageText)
                        .orElse(Button.NOTHING);

                switch (button) {
                    case NAME -> {
                        sendMessage = messageService.configureMessage(chatId, SubState.ENTER_FULL_NAME.getMessage());
                        user.setSubState(SubState.EDIT_NAME);
                        userService.save(user);

                        attachEditMenu(sendMessage);
                    }
                    case TAVERN -> {
                        sendMessage = messageService.configureMessage(chatId, SubState.ENTER_TAVERN_NAME.getMessage());
                        user.setSubState(SubState.EDIT_TAVERN);
                        userService.save(user);

                        attachEditMenu(sendMessage);
                    }
                    case PHONE_NUMBER -> {
                        sendMessage = messageService.configureMessage(chatId, SubState.ENTER_PHONE_NUMBER.getMessage());
                        user.setSubState(SubState.EDIT_PHONE_NUMBER);
                        userService.save(user);

                        attachEditMenu(sendMessage);
                    }
                    case ADDRESS -> {
                        sendMessage = messageService.configureMessage(chatId, SubState.ENTER_ADDRESS.getMessage());
                        user.setSubState(SubState.EDIT_ADDRESS);
                        userService.save(user);

                        attachEditMenu(sendMessage);
                    }
                    case CITY -> {
                        sendMessage = messageService.configureMessage(chatId, MessageText.CHOICE_CITY);
                        sendMessage.setReplyMarkup(ReplyKeyboardRemove.builder()
                                .removeKeyboard(true)
                                .build());

                        user.setSubState(SubState.EDIT_CITY);
                        userService.save(user);

                        return sendMessage;
                    }
                    case COMPLETE_REGISTRATION -> {
                        user.setSubState(SubState.WAITING_APPROVE_APPLICATION);
                        userService.save(user);

                        sendMessage = messageService.configureMessage(chatId, SubState.WAITING_APPROVE_APPLICATION.getMessage());
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
                }
            }
            case EDIT_TAVERN -> {
                if (!isUserPressKeyBoardElement(sendMessage, user, messageText, chatId)) {
                    user.getTavern().setName(messageText);
                }
            }
            case EDIT_PHONE_NUMBER -> {
                if (!isUserPressKeyBoardElement(sendMessage, user, messageText, chatId)) {
                    // TODO валидация номера
                    user.getContacts().stream()
                            .filter(contactEntity -> contactEntity.getType() == ContractType.MOBILE)
                            .findFirst()
                            .ifPresent(contact -> contact.setValue(messageText));

                }
            }
            case EDIT_ADDRESS -> {
                if (!isUserPressKeyBoardElement(sendMessage, user, messageText, chatId)) {
                    AddressEntity address = user.getTavern().getAddress();
                    address.setStreet(messageText);
                }
            }
        }

        return sendMessage;
    }

    private void attachMainEditMenu(SendMessage sendMessage) {
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
                        .resizeKeyboard(true)
                        .build());
    }

    private void attachEditMenu(SendMessage sendMessage) {
        sendMessage.setReplyMarkup(
                ReplyKeyboardMarkup.builder()
                        .keyboard(List.of(
                                new KeyboardRow(List.of(
                                        new KeyboardButton(Button.EDIT_MENU.getText()),
                                        new KeyboardButton(Button.COMPLETE_REGISTRATION.getText())))
                        ))
                        .resizeKeyboard(true)
                        .build());
    }

    private void sendClaimToApprove(UserEntity user) {
        // TODO вынести в механизм апрува
        if (CollectionUtils.isEmpty(user.getRoles())) {
            user.getRoles().add(Role.CLIENT_ADMIN);
        }

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
                                        botClientToken,
                                        InlineKeyboardMarkup.builder()
                                                .keyboard(List.of(List.of(
                                                        InlineKeyboardButton.builder()
                                                                .callbackData(Button.REGISTRATION_ACCEPT.getName() + " " + user.getTelegramId())
                                                                .text(Button.REGISTRATION_ACCEPT.getText() + " " + user.getTelegramId())
                                                                .build()
                                                )))
                                                .build()
                                )
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
            }

            result = true;
        } else {
            attachEditMenu(sendMessage);
        }

        return result;
    }

    private SendMessage showPersonalData(UserEntity user, Long chatId) {
        String yourPersonalData = "Ваши данные:" + System.lineSeparator() +
                "Имя: " + user.getName() + System.lineSeparator() +
                "Заведение: " + user.getTavern().getName() + System.lineSeparator() +
                "Адрес: " + user.getTavern().getAddress().getStreet() + System.lineSeparator() +
                "Номер телефона: " + user.getContacts()
                .stream()
                .filter(contactEntity -> contactEntity.getType() == ContractType.MOBILE)
                .map(ContactEntity::getValue)
                .findFirst()
                .orElse("") + System.lineSeparator();

        SendMessage sendMessage = messageService.configureMessage(chatId, yourPersonalData);

        sendMessage.setReplyMarkup(InlineKeyboardMarkup.builder()
                .keyboard(List.of(
                                List.of(
                                        keyboardService.createInlineButton(Button.EDIT),
                                        keyboardService.createInlineButton(Button.APPROVE)
                                )
                        )
                ).build());

        return sendMessage;
    }
}
