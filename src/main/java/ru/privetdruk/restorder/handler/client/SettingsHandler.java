package ru.privetdruk.restorder.handler.client;

import lombok.extern.slf4j.Slf4j;
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
import ru.privetdruk.restorder.model.enums.*;
import ru.privetdruk.restorder.service.ContactService;
import ru.privetdruk.restorder.service.MessageService;
import ru.privetdruk.restorder.service.TavernService;
import ru.privetdruk.restorder.service.UserService;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
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
    private final ReplyKeyboardMarkup tavernContactsKeyboard = new ReplyKeyboardMarkup();
    private final ReplyKeyboardMarkup tavernAddressKeyboard = new ReplyKeyboardMarkup();
    private final ReplyKeyboardMarkup profileKeyboard = new ReplyKeyboardMarkup();
    private final ReplyKeyboardMarkup profileNameKeyboard = new ReplyKeyboardMarkup();
    private final ReplyKeyboardMarkup userContactsKeyboard = new ReplyKeyboardMarkup();
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
        Button button = Button.fromText(messageText)
                .orElse(Button.NOTHING);
        Long chatId = message.getChatId();
        TavernEntity tavern = user.getTavern();

        // обработка функциональных клавиш
        switch (button) {
            case BACK, CANCEL -> user.setSubState(subState.getParentSubState());
            case MAIN_MENU -> {
                user.setState(State.MAIN_MENU);
                user.setSubState(SubState.VIEW_MAIN_MENU);
                userService.save(user);

                return mainMenuHandler.handle(user, message, callback);
            }
            case CHANGE -> {
                switch (subState) {
                    case VIEW_GENERAL_SETTINGS_TAVERN_NAME:
                        return configureMessage(user, chatId, SubState.CHANGE_GENERAL_SETTINGS_TAVERN_NAME, "Введите новое название:");
                    case VIEW_GENERAL_SETTINGS_TAVERN_ADDRESS:
                        return configureMessage(user, chatId, SubState.CHANGE_GENERAL_SETTINGS_TAVERN_ADDRESS, "Введите новый адрес:");
                    case VIEW_PROFILE_SETTINGS_USER_NAME:
                        return configureMessage(user, chatId, SubState.CHANGE_PROFILE_SETTINGS_USER_NAME, "Введите новое имя:");
                }
            }
            case ADD -> {
                switch (subState) {
                    case VIEW_GENERAL_SETTINGS_TAVERN_CONTACTS:
                        return configureMessage(user, chatId, SubState.ADD_GENERAL_SETTINGS_TAVERN_CONTACTS, "Введите новый номер телефона:");
                    case VIEW_PROFILE_SETTINGS_USER_CONTACTS:
                        return configureMessage(user, chatId, SubState.ADD_PROFILE_SETTINGS_USER_CONTACTS, "Введите новый номер телефона:");
                }
            }
            case DELETE -> {
                switch (subState) {
                    case VIEW_GENERAL_SETTINGS_TAVERN_CONTACTS:
                        return configureDeleteContacts(
                                user,
                                SubState.DELETE_GENERAL_SETTINGS_TAVERN_CONTACTS,
                                chatId,
                                tavern.getContacts(),
                                tavernContactsKeyboard
                        );
                    case VIEW_PROFILE_SETTINGS_USER_CONTACTS:
                        return configureDeleteContacts(
                                user,
                                SubState.DELETE_PROFILE_SETTINGS_USER_CONTACTS,
                                chatId,
                                user.getContacts(),
                                userContactsKeyboard
                        );
                }
            }
        }

        // обновление состояния
        if (button != Button.BACK && button != Button.CANCEL) {
            switch (user.getSubState()) {
                case VIEW_MAIN_MENU -> {
                    if (button == Button.SETTINGS) {
                        user.setState(State.SETTINGS);
                        user.setSubState(SubState.VIEW_SETTINGS);
                        userService.save(user);
                    }
                }
                case VIEW_SETTINGS -> {
                    switch (button) {
                        case GENERAL -> {
                            user.setSubState(SubState.VIEW_GENERAL_SETTINGS);
                            userService.save(user);
                        }
                        case PROFILE -> {
                            user.setSubState(SubState.VIEW_PROFILE_SETTINGS);
                            userService.save(user);
                        }
                    }
                }
                case VIEW_GENERAL_SETTINGS -> {
                    switch (button) {
                        case TAVERN_NAME -> {
                            user.setSubState(SubState.VIEW_GENERAL_SETTINGS_TAVERN_NAME);
                            userService.save(user);
                        }
                        case CONTACTS -> {
                            user.setSubState(SubState.VIEW_GENERAL_SETTINGS_TAVERN_CONTACTS);
                            userService.save(user);
                        }
                        case TAVERN_ADDRESS -> {
                            user.setSubState(SubState.VIEW_GENERAL_SETTINGS_TAVERN_ADDRESS);
                            userService.save(user);
                        }
                    }
                }
                case CHANGE_GENERAL_SETTINGS_TAVERN_NAME -> {
                    if (!StringUtils.hasText(messageText)) {
                        return messageService.configureMessage(chatId, "Вы ввели пустое значение! Повторите попытку.");
                    }

                    tavern.setName(messageText);
                    tavernService.save(tavern);

                    user.setSubState(user.getSubState().getParentSubState());
                    userService.save(user);
                }
                case ADD_GENERAL_SETTINGS_TAVERN_CONTACTS -> {
                    if (!StringUtils.hasText(messageText)) {
                        return messageService.configureMessage(chatId, "Вы ввели пустое значение! Повторите попытку.");
                    }

                    // TODO валидация номера

                    ContactEntity contact = ContactEntity.builder()
                            .tavern(tavern)
                            .type(ContractType.MOBILE)
                            .value(messageText)
                            .build();

                    tavern.addContact(contact);
                    contactService.save(contact);

                    user.setSubState(user.getSubState().getParentSubState());
                    userService.save(user);
                }
                case DELETE_GENERAL_SETTINGS_TAVERN_CONTACTS -> {
                    user.setSubState(user.getSubState().getParentSubState());
                    userService.save(user);

                    if (!StringUtils.hasText(messageText)) {
                        return messageService.configureMessage(chatId, "Вы не выбрали номер! Операция отменяется.");
                    }

                    tavern.getContacts().removeIf(contact -> contact.getValue().equals(messageText));
                    tavernService.save(tavern);
                }
                case CHANGE_GENERAL_SETTINGS_TAVERN_ADDRESS -> {
                    if (!StringUtils.hasText(messageText)) {
                        return messageService.configureMessage(chatId, "Вы ввели пустое значение! Повторите попытку.");
                    }

                    tavern.getAddress().setStreet(messageText);
                    tavernService.save(tavern);

                    user.setSubState(user.getSubState().getParentSubState());
                    userService.save(user);
                }

                case VIEW_PROFILE_SETTINGS -> {
                    switch (button) {
                        case USER_NAME -> {
                            user.setSubState(SubState.VIEW_PROFILE_SETTINGS_USER_NAME);
                            userService.save(user);
                        }
                        case CONTACTS -> {
                            user.setSubState(SubState.VIEW_PROFILE_SETTINGS_USER_CONTACTS);
                            userService.save(user);
                        }
                    }
                }

                case CHANGE_PROFILE_SETTINGS_USER_NAME -> {
                    if (!StringUtils.hasText(messageText)) {
                        return messageService.configureMessage(chatId, "Вы ввели пустое значение! Повторите попытку.");
                    }

                    user.setFirstName(messageText);
                    user.setSubState(user.getSubState().getParentSubState());
                    userService.save(user);
                }
                case ADD_PROFILE_SETTINGS_USER_CONTACTS -> {
                    if (!StringUtils.hasText(messageText)) {
                        return messageService.configureMessage(chatId, "Вы ввели пустое значение! Повторите попытку.");
                    }

                    // TODO валидация номера

                    ContactEntity contact = ContactEntity.builder()
                            .user(user)
                            .type(ContractType.MOBILE)
                            .value(messageText)
                            .build();

                    user.addContact(contact);
                    user.setSubState(user.getSubState().getParentSubState());
                    userService.save(user);
                }
                case DELETE_PROFILE_SETTINGS_USER_CONTACTS -> {
                    if (!StringUtils.hasText(messageText)) {
                        user.setSubState(user.getSubState().getParentSubState());
                        userService.save(user);

                        return messageService.configureMessage(chatId, "Вы не выбрали номер! Операция отменяется.");
                    }

                    user.getContacts().removeIf(contact -> contact.getValue().equals(messageText));
                    user.setSubState(user.getSubState().getParentSubState());
                    userService.save(user);
                }
            }
        }

        // отрисовка меню
        return switch (user.getSubState()) {
            case VIEW_SETTINGS -> messageService.configureMessage(chatId, "Открываем все настройки...", allKeyboard);
            case VIEW_GENERAL_SETTINGS -> messageService.configureMessage(chatId, fillGeneralInfo(tavern), generalKeyboard);
            case VIEW_GENERAL_SETTINGS_TAVERN_NAME -> messageService.configureMessage(chatId, fillTavernInfo(tavern.getName()), tavernNameKeyboard);
            case VIEW_GENERAL_SETTINGS_TAVERN_CONTACTS -> messageService.configureMessage(chatId, fillContactInfo(tavern.getContacts()), tavernContactsKeyboard);
            case VIEW_GENERAL_SETTINGS_TAVERN_ADDRESS -> messageService.configureMessage(chatId, fillAddressInfo(tavern.getAddress()), tavernAddressKeyboard);
            case VIEW_PROFILE_SETTINGS -> messageService.configureMessage(chatId, fillProfileInfo(user), profileKeyboard);
            case VIEW_PROFILE_SETTINGS_USER_NAME -> messageService.configureMessage(chatId, fillUserInfo(user.getFirstName()), profileNameKeyboard);
            case VIEW_PROFILE_SETTINGS_USER_CONTACTS -> messageService.configureMessage(chatId, fillContactInfo(user.getContacts()), userContactsKeyboard);
            default -> new SendMessage();
        };
    }

    private SendMessage configureMessage(UserEntity user, Long chatId, SubState addGeneralSettingsTavernContacts, String s) {
        user.setSubState(addGeneralSettingsTavernContacts);
        userService.save(user);
        return messageService.configureMessage(chatId, s, cancelKeyboard);
    }

    private SendMessage configureDeleteContacts(UserEntity user,
                                                SubState subState,
                                                Long chatId,
                                                Set<ContactEntity> contacts,
                                                ReplyKeyboardMarkup keyboard) {
        if (CollectionUtils.isEmpty(contacts)) {
            return messageService.configureMessage(chatId, "Нечего удалять.", keyboard);
        }

        user.setSubState(subState);
        userService.save(user);

        ReplyKeyboardMarkup contactKeyboard = new ReplyKeyboardMarkup();
        List<KeyboardRow> rows = new ArrayList<>();

        contacts.forEach(contact ->
                rows.add(new KeyboardRow(List.of(new KeyboardButton(contact.getValue()))))
        );

        rows.add(new KeyboardRow(List.of(new KeyboardButton(Button.CANCEL.getText()))));

        contactKeyboard.setKeyboard(rows);
        contactKeyboard.setResizeKeyboard(true);

        return messageService.configureMessage(chatId, "Выберите номер телефона, который хотите удалить.", contactKeyboard);
    }

    private String fillProfileInfo(UserEntity user) {
        return fillUserInfo(user.getFirstName()) + "\n\n" +
                fillRoleInfo(user.getRoles()) + "\n\n" +
                fillContactInfo(user.getContacts()) + "\n\n";
    }

    private String fillGeneralInfo(TavernEntity tavern) {
        return fillTavernInfo(tavern.getName()) + "\n\n" +
                fillContactInfo(tavern.getContacts()) + "\n\n" +
                fillAddressInfo(tavern.getAddress());
    }

    private String fillTavernInfo(String name) {
        return "Название вашего заведения: <b>" + name + "</b>";
    }

    private String fillUserInfo(String name) {
        return "Ваше имя: <b>" + name + "</b>";
    }

    private String fillRoleInfo(Set<Role> roles) {
        String rolesString = roles.stream()
                .map(Role::getDescription)
                .collect(Collectors.joining("\n"));

        return "Роли: <b>" + rolesString + "</b>";
    }

    private String fillAddressInfo(AddressEntity address) {
        if (address == null) {
            return "Информация об адресе отсутствует.";
        }

        return "Адресная информация:\n<b>Город</b> -  " + address.getCity().getDescription() + "\n<b>Адрес</b> - " + address.getStreet();

    }

    private String fillContactInfo(Set<ContactEntity> contacts) {
        if (CollectionUtils.isEmpty(contacts)) {
            return "Контактная информация отсутствует.";
        }

        return "Контактная информация:\n" + contacts.stream()
                .map(contact -> contact.getType().getDescription() + " - <b>" + contact.getValue() + "</b>")
                .collect(Collectors.joining("\n"));
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
                        new KeyboardButton(Button.CONTACTS.getText()),
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

        this.tavernContactsKeyboard.setKeyboard(List.of(
                new KeyboardRow(List.of(
                        new KeyboardButton(Button.ADD.getText()),
                        new KeyboardButton(Button.DELETE.getText())
                )),
                new KeyboardRow(List.of(
                        new KeyboardButton(Button.BACK.getText()),
                        new KeyboardButton(Button.MAIN_MENU.getText())
                ))
        ));
        this.tavernContactsKeyboard.setResizeKeyboard(true);

        this.profileKeyboard.setKeyboard(List.of(
                new KeyboardRow(List.of(
                        new KeyboardButton(Button.USER_NAME.getText()),
                        new KeyboardButton(Button.CONTACTS.getText())
                )),
                new KeyboardRow(List.of(
                        new KeyboardButton(Button.BACK.getText()),
                        new KeyboardButton(Button.MAIN_MENU.getText())
                ))
        ));
        this.profileKeyboard.setResizeKeyboard(true);

        this.profileNameKeyboard.setKeyboard(List.of(
                new KeyboardRow(List.of(
                        new KeyboardButton(Button.CHANGE.getText())
                )),
                new KeyboardRow(List.of(
                        new KeyboardButton(Button.BACK.getText()),
                        new KeyboardButton(Button.MAIN_MENU.getText())
                ))
        ));
        this.profileNameKeyboard.setResizeKeyboard(true);

        this.userContactsKeyboard.setKeyboard(List.of(
                new KeyboardRow(List.of(
                        new KeyboardButton(Button.ADD.getText()),
                        new KeyboardButton(Button.DELETE.getText())
                )),
                new KeyboardRow(List.of(
                        new KeyboardButton(Button.BACK.getText()),
                        new KeyboardButton(Button.MAIN_MENU.getText())
                ))
        ));
        this.userContactsKeyboard.setResizeKeyboard(true);
    }
}
