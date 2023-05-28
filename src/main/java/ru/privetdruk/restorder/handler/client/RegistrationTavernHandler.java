package ru.privetdruk.restorder.handler.client;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Contact;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import ru.privetdruk.restorder.handler.MessageHandler;
import ru.privetdruk.restorder.model.consts.Constant;
import ru.privetdruk.restorder.model.consts.MessageText;
import ru.privetdruk.restorder.model.entity.AddressEntity;
import ru.privetdruk.restorder.model.entity.ContactEntity;
import ru.privetdruk.restorder.model.entity.TavernEntity;
import ru.privetdruk.restorder.model.entity.UserEntity;
import ru.privetdruk.restorder.model.enums.Button;
import ru.privetdruk.restorder.model.enums.City;
import ru.privetdruk.restorder.model.enums.ContractType;
import ru.privetdruk.restorder.model.enums.SubState;
import ru.privetdruk.restorder.service.*;
import ru.privetdruk.restorder.service.util.ValidationService;

import java.util.Optional;

import static java.util.List.of;
import static org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton.builder;
import static ru.privetdruk.restorder.model.consts.MessageText.*;
import static ru.privetdruk.restorder.model.enums.Role.ADMIN;
import static ru.privetdruk.restorder.model.enums.SubState.EDIT_PERSONAL_DATA;
import static ru.privetdruk.restorder.service.KeyboardService.*;
import static ru.privetdruk.restorder.service.MessageService.toMessage;

@RequiredArgsConstructor
@Component
public class RegistrationTavernHandler implements MessageHandler {
    private final ContactService contactService;
    private final InfoService infoService;
    private final TelegramApiService telegramApiService;
    private final TavernService tavernService;
    private final UserService userService;
    private final ValidationService validationService;

    @Override
    public SendMessage handle(UserEntity user, Message message, CallbackQuery callback) {
        String messageText = message.getText();
        Long chatId = message.getChatId();
        SubState subState = user.getSubState();
        SendMessage sendMessage = new SendMessage();

        Button button = Button.fromText(messageText).orElse(Button.NOTHING);

        TavernEntity tavern = user.getTavern();

        switch (subState) {
            case SHOW_REGISTER_BUTTON -> {
                if (button == Button.REGISTRATION) {
                    subState = subState.getNextSubState();
                    sendMessage = toMessage(chatId, changeState(user, subState), REMOVE_KEYBOARD);
                } else {
                    sendMessage = toMessage(chatId, subState, REGISTRATION_KEYBOARD);
                }
            }
            case ENTER_FULL_NAME -> {
                user.setName(messageText);

                sendMessage = toMessage(chatId, changeState(user, subState).getMessage());
            }
            case ENTER_TAVERN_NAME -> {
                tavern = tavernService.create(messageText, user.getId());
                user.setTavern(tavern);

                changeState(user, subState);

                sendMessage = toMessage(chatId, MessageText.ENTER_TAVERN_DESCRIPTION, KeyboardService.WITHOUT_DESCRIPTION_KEYBOARD);
            }
            case ENTER_TAVERN_DESCRIPTION -> {
                if (button != Button.WITHOUT_DESCRIPTION) {
                    tavern.setDescription(messageText);
                    tavernService.save(tavern);
                }

                changeState(user, subState);

                sendMessage = toMessage(chatId, MessageText.CHOICE_CITY, CITIES_KEYBOARD);
            }
            case CHOICE_CITY -> {
                City city = City.fromDescription(messageText);

                if (city == null) {
                    sendMessage = toMessage(chatId, subState, CITIES_KEYBOARD);
                } else {
                    AddressEntity address = AddressEntity.builder()
                            .tavern(tavern)
                            .city(city)
                            .build();

                    tavern.setAddress(address);

                    tavernService.save(tavern);

                    sendMessage = toMessage(chatId, changeState(user, subState), REMOVE_KEYBOARD);
                }
            }
            case ENTER_ADDRESS -> {
                tavern.getAddress().setStreet(messageText);
                tavernService.save(tavern);

                changeState(user, subState);

                sendMessage = toMessage(chatId, SHARE_PHONE_NUMBER, SHARE_PHONE_KEYBOARD);
            }
            case ENTER_PHONE_NUMBER -> {
                Contact sendContact = message.getContact();

                if (sendContact == null) {
                    return toMessage(chatId, SHARE_PHONE_NUMBER, SHARE_PHONE_KEYBOARD);
                }

                String phoneNumber = contactService.preparePhoneNumber(sendContact.getPhoneNumber());

                if (validationService.isNotValidPhone(phoneNumber)) {
                    return toMessage(chatId, INCORRECT_ENTER_PHONE_NUMBER, SHARE_PHONE_KEYBOARD);
                }

                ContactEntity contact = ContactEntity.builder()
                        .user(user)
                        .type(ContractType.MOBILE)
                        .value(phoneNumber)
                        .build();

                contactService.save(contact);

                changeState(user, subState);

                sendMessage = showPersonalData(user, chatId);
            }
            case REGISTRATION_APPROVING -> {
                if (button == Button.APPROVE) {
                    changeState(user, subState);
                    sendMessage = toMessage(chatId, SubState.WAITING_APPROVE_APPLICATION, REMOVE_KEYBOARD);

                    sendClaimToApprove(user);
                } else if (button == Button.EDIT) {
                    sendMessage = toMessage(chatId, SELECT_ELEMENT_FOR_EDIT, TAVERN_REGISTRATION_KEYBOARD);

                    user.setSubState(EDIT_PERSONAL_DATA);
                    userService.save(user);
                } else {
                    sendMessage = showPersonalData(user, chatId);
                }
            }
            case EDIT_PERSONAL_DATA -> {
                sendMessage = toMessage(chatId, SELECT_ELEMENT_FOR_EDIT, TAVERN_REGISTRATION_KEYBOARD);

                switch (button) {
                    case NAME -> {
                        sendMessage = toMessage(chatId, SubState.ENTER_FULL_NAME, TAVERN_EDIT_REGISTRATION_KEYBOARD);
                        userService.updateSubState(user, SubState.EDIT_NAME);
                    }
                    case TAVERN_NAME -> {
                        sendMessage = toMessage(chatId, SubState.ENTER_TAVERN_NAME, TAVERN_EDIT_REGISTRATION_KEYBOARD);
                        userService.updateSubState(user, SubState.EDIT_TAVERN);
                    }
                    case DESCRIPTION -> {
                        sendMessage = toMessage(chatId, SubState.ENTER_TAVERN_DESCRIPTION, TAVERN_EDIT_REGISTRATION_KEYBOARD);
                        userService.updateSubState(user, SubState.EDIT_DESCRIPTION);
                    }
                    case TAVERN_ADDRESS -> {
                        sendMessage = toMessage(chatId, SubState.ENTER_ADDRESS, TAVERN_EDIT_REGISTRATION_KEYBOARD);
                        userService.updateSubState(user, SubState.EDIT_ADDRESS);
                    }
                    case CITY -> {
                        sendMessage = toMessage(chatId, MessageText.CHOICE_CITY, REMOVE_KEYBOARD);

                        userService.updateSubState(user, SubState.EDIT_CITY);

                        return sendMessage;
                    }
                    case COMPLETE_REGISTRATION -> {
                        userService.updateSubState(user, SubState.WAITING_APPROVE_APPLICATION);

                        sendMessage = toMessage(chatId, SubState.WAITING_APPROVE_APPLICATION, REMOVE_KEYBOARD);

                        sendClaimToApprove(user);
                    }
                }
            }
            case EDIT_NAME, EDIT_TAVERN, EDIT_DESCRIPTION, EDIT_ADDRESS -> {
                if (isNotPressKeyboardElement(sendMessage, user, messageText, chatId)) {
                    switch (subState) {
                        case EDIT_NAME -> {
                            user.setName(messageText);
                            userService.save(user);
                        }
                        case EDIT_TAVERN -> {
                            tavern.setName(messageText);
                            tavernService.save(tavern);
                        }
                        case EDIT_DESCRIPTION -> {
                            tavern.setDescription(messageText);
                            tavernService.save(tavern);
                        }
                        case EDIT_ADDRESS -> {
                            tavern.getAddress().setStreet(messageText);
                            tavernService.save(tavern);
                        }
                    }

                    return toMessage(chatId, infoService.fillPersonalData(user), TAVERN_EDIT_REGISTRATION_KEYBOARD);
                }
            }
            case WAITING_APPROVE_APPLICATION -> sendMessage = toMessage(chatId, CLAIM_APPROVE_WAIT);
        }

        return sendMessage;
    }

    private void sendClaimToApprove(UserEntity user) {
        InlineKeyboardMarkup approveKeyboard = InlineKeyboardMarkup.builder()
                .keyboard(of(of(builder()
                        .callbackData(Button.ACCEPT.getName() + Constant.SPACE + user.getTelegramId())
                        .text(Button.ACCEPT.getText())
                        .build())))
                .build();

        userService.findUsersByRole(ADMIN).stream()
                .map(UserEntity::getTelegramId)
                .forEach(telegramId -> telegramApiService.sendMessage(telegramId, infoService.fillApprove(user), true, approveKeyboard));
    }

    private SubState changeState(UserEntity user, SubState subState) {
        SubState nextSubState = subState.getNextSubState();
        user.setState(nextSubState.getState());

        userService.updateSubState(user, nextSubState);

        return nextSubState;
    }

    private boolean isNotPressKeyboardElement(SendMessage sendMessage, UserEntity user, String messageText, Long chatId) {
        boolean result = false;

        Optional<Button> button = Button.fromText(messageText);

        if (button.isPresent()) {
            switch (button.get()) {
                case EDIT_MENU -> {
                    userService.updateSubState(user, SubState.EDIT_PERSONAL_DATA);
                    toMessage(sendMessage, chatId, SELECT_ELEMENT_FOR_EDIT, TAVERN_REGISTRATION_KEYBOARD);
                }
                case COMPLETE_REGISTRATION -> {
                    SubState subState = SubState.WAITING_APPROVE_APPLICATION;
                    userService.updateSubState(user, subState);

                    toMessage(sendMessage, chatId, subState.getMessage(), REMOVE_KEYBOARD);

                    sendClaimToApprove(user);
                }
                default -> sendMessage.setReplyMarkup(TAVERN_EDIT_REGISTRATION_KEYBOARD);
            }

            result = true;
        } else {
            sendMessage.setReplyMarkup(TAVERN_EDIT_REGISTRATION_KEYBOARD);
        }

        return !result;
    }

    private SendMessage showPersonalData(UserEntity user, Long chatId) {
        return toMessage(chatId, infoService.fillPersonalData(user), KeyboardService.REGISTRATION_APPROVING_KEYBOARD);
    }
}
