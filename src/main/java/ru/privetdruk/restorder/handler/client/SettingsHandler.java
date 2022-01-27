package ru.privetdruk.restorder.handler.client;

import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import ru.privetdruk.restorder.handler.MessageHandler;
import ru.privetdruk.restorder.model.entity.AddressEntity;
import ru.privetdruk.restorder.model.entity.ContactEntity;
import ru.privetdruk.restorder.model.entity.TavernEntity;
import ru.privetdruk.restorder.model.entity.UserEntity;
import ru.privetdruk.restorder.model.enums.Button;
import ru.privetdruk.restorder.model.enums.ContractType;
import ru.privetdruk.restorder.model.enums.State;
import ru.privetdruk.restorder.model.enums.SubState;
import ru.privetdruk.restorder.service.ContactService;
import ru.privetdruk.restorder.service.MessageService;
import ru.privetdruk.restorder.service.TavernService;
import ru.privetdruk.restorder.service.UserService;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class SettingsHandler implements MessageHandler {
    private final MainMenuHandler mainMenuHandler;
    private final MessageService messageService;
    private final UserService userService;
    private final TavernService tavernService;
    private final ContactService contactService;

    private final ReplyKeyboardMarkup cancelKeyboard = new ReplyKeyboardMarkup();
    private final ReplyKeyboardMarkup allKeyboard = new ReplyKeyboardMarkup();
    private final ReplyKeyboardMarkup generalKeyboard = new ReplyKeyboardMarkup();
    private final ReplyKeyboardMarkup tavernNameKeyboard = new ReplyKeyboardMarkup();
    private final ReplyKeyboardMarkup tavernPhonesKeyboard = new ReplyKeyboardMarkup();
    private final ReplyKeyboardMarkup tavernAddressKeyboard = new ReplyKeyboardMarkup();
    private final ReplyKeyboardMarkup profileKeyboard = new ReplyKeyboardMarkup();
    private final ReplyKeyboardMarkup employeeKeyboard = new ReplyKeyboardMarkup();
    private final ReplyKeyboardMarkup categoryKeyboard = new ReplyKeyboardMarkup();
    private final ReplyKeyboardMarkup scheduleKeyboard = new ReplyKeyboardMarkup();
    private final ReplyKeyboardMarkup tableKeyboard = new ReplyKeyboardMarkup();

    public SettingsHandler(@Lazy MainMenuHandler mainMenuHandler, MessageService messageService, UserService userService, TavernService tavernService, ContactService contactService) {
        this.mainMenuHandler = mainMenuHandler;
        this.messageService = messageService;
        this.userService = userService;
        this.tavernService = tavernService;
        this.contactService = contactService;
        configureKeyboards();
    }

    @Override
    public SendMessage handle(UserEntity user, Message message, CallbackQuery callback) {
        SubState subState = user.getSubState();
        String messageText = message.getText();
        Button button = Button.fromText(messageText);
        TavernEntity tavern = user.getTavern();

        if (button != Button.CANCEL) {
            switch (subState) {
                case CHANGE_TAVERN_NAME_GENERAL_SETTINGS: {
                    if (!StringUtils.hasText(messageText)) {
                        return messageService.configureMessage(message.getChatId(), "Вы ввели пустое значение! Повторите попытку.");
                    }

                    tavern.setName(messageText);

                    tavernService.save(tavern);

                    break;
                }
                case ADD_TAVERN_PHONES_GENERAL_SETTINGS: {
                    if (!StringUtils.hasText(messageText)) {
                        return messageService.configureMessage(message.getChatId(), "Вы ввели пустое значение! Повторите попытку.");
                    }

                    // TODO валидация номера

                    ContactEntity contact = ContactEntity.builder()
                            .tavern(tavern)
                            .type(ContractType.MOBILE)
                            .value(messageText)
                            .build();

                    tavern.addContact(contact);

                    contactService.save(contact);

                    break;
                }
                case DELETE_TAVERN_PHONES_GENERAL_SETTINGS: {
                    if (!StringUtils.hasText(messageText)) {
                        user.setSubState(SubState.VIEW_TAVERN_PHONES_GENERAL_SETTINGS);
                        userService.save(user);

                        SendMessage sendMessage = messageService.configureMessage(message.getChatId(), "Вы не выбрали номер! Операция отменяется.");
                        sendMessage.setReplyMarkup(tavernPhonesKeyboard);
                        return sendMessage;
                    }

                    tavern.getContacts().removeIf(contact -> contact.getValue().equals(messageText));

                    tavernService.save(tavern);

                    break;
                }
                case CHANGE_TAVERN_ADDRESS_GENERAL_SETTINGS: {
                    if (!StringUtils.hasText(messageText)) {
                        return messageService.configureMessage(message.getChatId(), "Вы ввели пустое значение! Повторите попытку.");
                    }

                    tavern.getAddress().setStreet(messageText);

                    tavernService.save(tavern);

                    break;
                }
            }
        }

        Button afterChangeButton = subState.getAfterChangeButton();
        if (afterChangeButton != null) {
            button = afterChangeButton;
        }

        if (button == null) {
            return new SendMessage();
        }

        if (button == Button.BACK) {
            Button parentButton = subState.getParentButton();
            if (parentButton != null) {
                button = parentButton;
            }
        } else if (button == Button.CHANGE) {
            if (subState == SubState.VIEW_TAVERN_NAME_GENERAL_SETTINGS) {
                user.setSubState(SubState.CHANGE_TAVERN_NAME_GENERAL_SETTINGS);
                userService.save(user);
                SendMessage sendMessage = messageService.configureMessage(message.getChatId(), "Введите новое название:");
                sendMessage.setReplyMarkup(cancelKeyboard);
                return sendMessage;
            } else if (subState == SubState.VIEW_TAVERN_ADDRESS_GENERAL_SETTINGS) {
                user.setSubState(SubState.CHANGE_TAVERN_ADDRESS_GENERAL_SETTINGS);
                userService.save(user);
                SendMessage sendMessage = messageService.configureMessage(message.getChatId(), "Введите новый адрес:");
                sendMessage.setReplyMarkup(cancelKeyboard);
                return sendMessage;
            }
        } else if (button == Button.ADD) {
            if (subState == SubState.VIEW_TAVERN_PHONES_GENERAL_SETTINGS) {
                user.setSubState(SubState.ADD_TAVERN_PHONES_GENERAL_SETTINGS);
                userService.save(user);
                SendMessage sendMessage = messageService.configureMessage(message.getChatId(), "Введите новый номер телефона:");
                sendMessage.setReplyMarkup(cancelKeyboard);
                return sendMessage;
            }
        } else if (button == Button.DELETE) {
            if (subState == SubState.VIEW_TAVERN_PHONES_GENERAL_SETTINGS) {
                Set<ContactEntity> contacts = tavern.getContacts();

                if (CollectionUtils.isEmpty(contacts)) {
                    SendMessage sendMessage = messageService.configureMessage(message.getChatId(), "Нечего удалять.");
                    sendMessage.setReplyMarkup(tavernPhonesKeyboard);
                    return sendMessage;
                }

                user.setSubState(SubState.DELETE_TAVERN_PHONES_GENERAL_SETTINGS);
                userService.save(user);
                SendMessage sendMessage = messageService.configureMessage(message.getChatId(), "Выберите номер телефона, который хотите удалить.");

                final ReplyKeyboardMarkup phoneKeyboard = new ReplyKeyboardMarkup();
                List<KeyboardRow> rows = new ArrayList<>();
                contacts.forEach(contact ->
                        rows.add(new KeyboardRow(List.of(new KeyboardButton(contact.getValue()))))
                );
                rows.add(new KeyboardRow(List.of(new KeyboardButton(Button.CANCEL.getText()))));
                phoneKeyboard.setKeyboard(rows);
                phoneKeyboard.setResizeKeyboard(true);

                sendMessage.setReplyMarkup(phoneKeyboard);
                return sendMessage;
            }
        }

        switch (button) {
            case SETTINGS: {
                SendMessage sendMessage = messageService.configureMessage(message.getChatId(), "Открываем все настройки...");
                sendMessage.setReplyMarkup(allKeyboard);

                user.setState(State.SETTINGS);
                user.setSubState(SubState.VIEW_SETTINGS);
                userService.save(user);

                return sendMessage;
            }
            case GENERAL: {
                SendMessage sendMessage = messageService.configureMessage(message.getChatId(), "Открываем основные настройки...");
                sendMessage.setReplyMarkup(generalKeyboard);

                user.setSubState(SubState.VIEW_GENERAL_SETTINGS);
                userService.save(user);

                return sendMessage;
            }
            case MAIN_MENU: {
                user.setState(State.MAIN_MENU);
                user.setSubState(State.MAIN_MENU.getInitialSubState());
                userService.save(user);

                return mainMenuHandler.handle(user, message, callback);
            }
            case TAVERN_NAME: {
                user.setSubState(SubState.VIEW_TAVERN_NAME_GENERAL_SETTINGS);
                userService.save(user);

                SendMessage sendMessage = messageService.configureMessage(message.getChatId(), "Название вашего заведения: <b>" + user.getTavern().getName() + "</b>");
                sendMessage.enableHtml(true);
                sendMessage.setReplyMarkup(tavernNameKeyboard);

                return sendMessage;
            }
            case TAVERN_PHONES: {
                user.setSubState(SubState.VIEW_TAVERN_PHONES_GENERAL_SETTINGS);
                userService.save(user);

                SendMessage sendMessage = messageService.configureMessage(message.getChatId(), "");

                Set<ContactEntity> contacts = user.getTavern().getContacts();
                if (CollectionUtils.isEmpty(contacts)) {
                    sendMessage.setText("Контактная информация отсутствует.");
                } else {
                    String text = contacts.stream()
                            .map(contact -> contact.getType().getDescription() + " - <b>" + contact.getValue() + "</b>")
                            .collect(Collectors.joining("\n"));

                    sendMessage.setText("Контактная информация:\n" + text);
                }

                sendMessage.enableHtml(true);
                sendMessage.setReplyMarkup(tavernPhonesKeyboard);

                return sendMessage;
            }
            case TAVERN_ADDRESS: {
                user.setSubState(SubState.VIEW_TAVERN_ADDRESS_GENERAL_SETTINGS);
                userService.save(user);

                SendMessage sendMessage = messageService.configureMessage(message.getChatId(), "");

                AddressEntity address = user.getTavern().getAddress();
                if (address == null) {
                    sendMessage.setText("Информация об адресе отсутствует.");
                } else {
                    sendMessage.setText("<b>Город:</b> " + address.getCity().getDescription() + "\n<b>Адрес</b>: " + address.getStreet());
                }

                sendMessage.enableHtml(true);
                sendMessage.setReplyMarkup(tavernAddressKeyboard);

                return sendMessage;
            }
            default:
                break;
        }

        return new SendMessage();
    }

    private void configureKeyboards() {
        this.cancelKeyboard.setKeyboard(List.of(
                new KeyboardRow(List.of(
                        new KeyboardButton(Button.CANCEL.getText())
                ))
        ));
        this.cancelKeyboard.setResizeKeyboard(true);

        this.allKeyboard.setKeyboard(List.of(
                new KeyboardRow(List.of(
                        new KeyboardButton(Button.GENERAL.getText()),
                        new KeyboardButton(Button.PROFILE.getText())
                )),
                new KeyboardRow(List.of(
                        new KeyboardButton(Button.EMPLOYEES.getText()),
                        new KeyboardButton(Button.CATEGORIES.getText())
                )),
                new KeyboardRow(List.of(
                        new KeyboardButton(Button.SCHEDULE.getText()),
                        new KeyboardButton(Button.TABLES.getText())
                )),
                new KeyboardRow(List.of(
                        new KeyboardButton(Button.MAIN_MENU.getText())
                ))
        ));
        this.allKeyboard.setResizeKeyboard(true);

        this.generalKeyboard.setKeyboard(List.of(
                new KeyboardRow(List.of(
                        new KeyboardButton(Button.TAVERN_NAME.getText()),
                        new KeyboardButton(Button.TAVERN_PHONES.getText()),
                        new KeyboardButton(Button.TAVERN_ADDRESS.getText())
                )),
                new KeyboardRow(List.of(
                        new KeyboardButton(Button.BACK.getText()),
                        new KeyboardButton(Button.MAIN_MENU.getText())
                ))
        ));
        this.generalKeyboard.setResizeKeyboard(true);

        this.tavernNameKeyboard.setKeyboard(List.of(
                new KeyboardRow(List.of(
                        new KeyboardButton(Button.CHANGE.getText())
                )),
                new KeyboardRow(List.of(
                        new KeyboardButton(Button.BACK.getText()),
                        new KeyboardButton(Button.MAIN_MENU.getText())
                ))
        ));
        this.tavernNameKeyboard.setResizeKeyboard(true);

        this.tavernAddressKeyboard.setKeyboard(List.of(
                new KeyboardRow(List.of(
                        new KeyboardButton(Button.CHANGE.getText())
                )),
                new KeyboardRow(List.of(
                        new KeyboardButton(Button.BACK.getText()),
                        new KeyboardButton(Button.MAIN_MENU.getText())
                ))
        ));
        this.tavernAddressKeyboard.setResizeKeyboard(true);

        this.tavernPhonesKeyboard.setKeyboard(List.of(
                new KeyboardRow(List.of(
                        new KeyboardButton(Button.ADD.getText()),
                        new KeyboardButton(Button.DELETE.getText())
                )),
                new KeyboardRow(List.of(
                        new KeyboardButton(Button.BACK.getText()),
                        new KeyboardButton(Button.MAIN_MENU.getText())
                ))
        ));
        this.tavernPhonesKeyboard.setResizeKeyboard(true);
    }
}
