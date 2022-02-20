package ru.privetdruk.restorder.handler.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
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
import ru.privetdruk.restorder.model.entity.*;
import ru.privetdruk.restorder.model.enums.*;
import ru.privetdruk.restorder.service.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
public class SettingsHandler implements MessageHandler {
    private final MainMenuHandler mainMenuHandler;
    private final MessageService messageService;
    private final UserService userService;
    private final TavernService tavernService;
    private final ContactService contactService;
    private final EventService eventService;

    @Value("${bot.client.username}")
    private String botName;

    public SettingsHandler(@Lazy MainMenuHandler mainMenuHandler,
                           MessageService messageService,
                           UserService userService,
                           TavernService tavernService,
                           ContactService contactService,
                           EventService eventService) {
        this.mainMenuHandler = mainMenuHandler;
        this.messageService = messageService;
        this.userService = userService;
        this.tavernService = tavernService;
        this.contactService = contactService;
        this.eventService = eventService;
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
            case BACK, CANCEL, NO -> user.setSubState(subState.getParentSubState());
            case YES -> {
                if (subState == SubState.DELETE_PROFILE_SETTINGS) {
                    if (user.getRoles().contains(Role.CLIENT_ADMIN)) {
                        tavernService.delete(tavern);
                    } else {
                        userService.delete(user);
                    }

                    return messageService.configureMessage(chatId, "Данные успешно удалены. Хорошего дня!", KeyboardService.REMOVE_KEYBOARD);
                }
            }
            case MAIN_MENU -> {
                user.setState(State.MAIN_MENU);
                updateSubState(user, SubState.VIEW_MAIN_MENU);

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
                    case VIEW_GENERAL_SETTINGS_CATEGORIES:
                        updateSubState(user, SubState.CHANGE_GENERAL_SETTINGS_CATEGORIES);

                        return messageService.configureMessage(chatId, "Выберите новую категорию.", KeyboardService.CATEGORIES_LIST_WITH_CANCEL);
                }
            }
            case ADD -> {
                switch (subState) {
                    case VIEW_GENERAL_SETTINGS_TAVERN_CONTACTS:
                        return configureMessage(user, chatId, SubState.ADD_GENERAL_SETTINGS_TAVERN_CONTACTS, "Введите новый номер телефона:");
                    case VIEW_PROFILE_SETTINGS_USER_CONTACTS:
                        return configureMessage(user, chatId, SubState.ADD_PROFILE_SETTINGS_USER_CONTACTS, "Введите новый номер телефона:");
                    case VIEW_EMPLOYEE_SETTINGS: {
                        EventEntity event = EventEntity.builder()
                                .params(Map.of(JsonbKey.TAVERN_ID.getKey(), tavern.getId()))
                                .type(EventType.REGISTER_EMPLOYEE)
                                .expirationDate(LocalDateTime.now().plusHours(1))
                                .build();

                        event = eventService.save(event);

                        return messageService.configureHtmlMessage(
                                chatId,
                                String.format(
                                        "Регистрация по ссылке доступна в течении одного часа и только для одного человека.\nПерешлите данное сообщение вашему сотруднику.\n\n<a href=\"https://t.me/%s?start=%s\">> РЕГИСТРАЦИЯ</a>",
                                        botName,
                                        event.getUuid()
                                )
                        );
                    }
                }
            }
            case DELETE -> {
                switch (subState) {
                    case VIEW_GENERAL_SETTINGS_TAVERN_CONTACTS:
                        return configureDeleteContacts(user, SubState.DELETE_GENERAL_SETTINGS_TAVERN_CONTACTS, chatId, tavern.getContacts(), KeyboardService.TAVERN_CONTACTS_KEYBOARD);
                    case VIEW_PROFILE_SETTINGS_USER_CONTACTS:
                        return configureDeleteContacts(user, SubState.DELETE_PROFILE_SETTINGS_USER_CONTACTS, chatId, user.getContacts(), KeyboardService.USER_CONTACTS_KEYBOARD);
                    case VIEW_EMPLOYEE_SETTINGS: {
                        return configureDeleteEmployees(user, chatId, tavern.getEmployees());
                    }
                }
            }
        }

        // обновление состояния
        if (button != Button.BACK && button != Button.CANCEL && button != Button.NO) {
            switch (user.getSubState()) {
                case VIEW_MAIN_MENU -> {
                    if (button == Button.SETTINGS) {
                        user.setState(State.SETTINGS);
                        updateSubState(user, SubState.VIEW_SETTINGS);
                    }
                }
                case VIEW_SETTINGS -> {
                    switch (button) {
                        case GENERAL -> updateSubState(user, SubState.VIEW_GENERAL_SETTINGS);
                        case PROFILE -> updateSubState(user, SubState.VIEW_PROFILE_SETTINGS);
                        case EMPLOYEES -> updateSubState(user, SubState.VIEW_EMPLOYEE_SETTINGS);
                    }
                }
                case VIEW_GENERAL_SETTINGS -> {
                    switch (button) {
                        case TAVERN_NAME -> updateSubState(user, SubState.VIEW_GENERAL_SETTINGS_TAVERN_NAME);
                        case CONTACTS -> updateSubState(user, SubState.VIEW_GENERAL_SETTINGS_TAVERN_CONTACTS);
                        case TAVERN_ADDRESS -> updateSubState(user, SubState.VIEW_GENERAL_SETTINGS_TAVERN_ADDRESS);
                        case CATEGORIES -> updateSubState(user, SubState.VIEW_GENERAL_SETTINGS_CATEGORIES);
                    }
                }
                case CHANGE_GENERAL_SETTINGS_TAVERN_NAME -> {
                    if (!StringUtils.hasText(messageText)) {
                        return messageService.configureMessage(chatId, "Вы ввели пустое значение! Повторите попытку.");
                    }

                    tavern.setName(messageText);
                    tavernService.save(tavern);

                    updateSubState(user, user.getSubState().getParentSubState());
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

                    updateSubState(user, user.getSubState().getParentSubState());
                }
                case DELETE_GENERAL_SETTINGS_TAVERN_CONTACTS -> {
                    updateSubState(user, user.getSubState().getParentSubState());

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

                    updateSubState(user, user.getSubState().getParentSubState());
                }
                case CHANGE_GENERAL_SETTINGS_CATEGORIES -> {
                    Category category = Category.fromDescription(messageText);
                    if (category == null) {
                        return messageService.configureMessage(chatId, "Выбрано некорректное значение.");
                    }

                    tavern.setCategory(category);
                    tavernService.save(tavern);

                    updateSubState(user, user.getSubState().getParentSubState());
                }
                case VIEW_PROFILE_SETTINGS -> {
                    switch (button) {
                        case USER_NAME -> updateSubState(user, SubState.VIEW_PROFILE_SETTINGS_USER_NAME);
                        case CONTACTS -> updateSubState(user, SubState.VIEW_PROFILE_SETTINGS_USER_CONTACTS);
                        case DELETE_PROFILE -> {
                            updateSubState(user, SubState.DELETE_PROFILE_SETTINGS);

                            if (user.getRoles().contains(Role.CLIENT_EMPLOYEE)) {
                                return messageService.configureMessage(chatId, "Вы действительно хотите удалить профиль?", KeyboardService.YES_NO_KEYBOARD);
                            }

                            return messageService.configureMessage(chatId, "Вы является владельцем заведения X. Вместе с вашим профилем будет удалено и заведение. Продолжить удаление?", KeyboardService.YES_NO_KEYBOARD);
                        }
                    }
                }
                case CHANGE_PROFILE_SETTINGS_USER_NAME -> {
                    if (!StringUtils.hasText(messageText)) {
                        return messageService.configureMessage(chatId, "Вы ввели пустое значение! Повторите попытку.");
                    }

                    user.setName(messageText);
                    updateSubState(user, user.getSubState().getParentSubState());
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
                    updateSubState(user, user.getSubState().getParentSubState());
                }
                case DELETE_PROFILE_SETTINGS_USER_CONTACTS -> {
                    if (!StringUtils.hasText(messageText)) {
                        updateSubState(user, user.getSubState().getParentSubState());

                        return messageService.configureMessage(chatId, "Вы не выбрали номер! Операция отменяется.");
                    }

                    user.getContacts()
                            .removeIf(contact -> contact.getValue().equals(messageText));

                    updateSubState(user, user.getSubState().getParentSubState());
                }
                case DELETE_EMPLOYEE_SETTINGS -> {
                    Long employeeId;

                    try {
                        employeeId = Long.valueOf(messageText.split(" ")[1]);
                    } catch (Throwable t) {
                        employeeId = null;
                    }

                    if (!StringUtils.hasText(messageText) || employeeId == null) {
                        updateSubState(user, user.getSubState().getParentSubState());

                        return messageService.configureMessage(chatId, "Вы ни кого не выбрали! Операция отменяется.", KeyboardService.EMPLOYEE_KEYBOARD);
                    }

                    if (user.getId().equals(employeeId)) {
                        updateSubState(user, user.getSubState().getParentSubState());

                        return messageService.configureMessage(chatId, "Себя нельзя удалить.", KeyboardService.EMPLOYEE_KEYBOARD);
                    }

                    final Long finalEmployeeId = employeeId;
                    tavern.getEmployees()
                            .removeIf(employee -> employee.getId().equals(finalEmployeeId));

                    tavernService.save(tavern);

                    updateSubState(user, user.getSubState().getParentSubState());
                }
            }
        }

        // отрисовка меню
        return switch (user.getSubState()) {
            case VIEW_SETTINGS -> messageService.configureMessage(chatId, "Открываем все настройки...", KeyboardService.SETTINGS_KEYBOARD);

            case VIEW_GENERAL_SETTINGS -> messageService.configureMessage(chatId, fillGeneralInfo(tavern), KeyboardService.GENERAL_KEYBOARD);
            case VIEW_GENERAL_SETTINGS_TAVERN_NAME -> messageService.configureMessage(chatId, fillTavernInfo(tavern.getName()), KeyboardService.TAVERN_NAME_KEYBOARD);
            case VIEW_GENERAL_SETTINGS_TAVERN_CONTACTS -> messageService.configureMessage(chatId, fillContactInfo(tavern.getContacts()), KeyboardService.TAVERN_CONTACTS_KEYBOARD);
            case VIEW_GENERAL_SETTINGS_TAVERN_ADDRESS -> messageService.configureMessage(chatId, fillAddressInfo(tavern.getAddress()), KeyboardService.TAVERN_ADDRESS_KEYBOARD);
            case VIEW_GENERAL_SETTINGS_CATEGORIES -> messageService.configureMessage(chatId, fillCategory(tavern.getCategory()), KeyboardService.CATEGORIES);

            case VIEW_PROFILE_SETTINGS -> messageService.configureMessage(chatId, fillProfileInfo(user), KeyboardService.PROFILE_KEYBOARD);
            case VIEW_PROFILE_SETTINGS_USER_NAME -> messageService.configureMessage(chatId, fillUserInfo(user.getName()), KeyboardService.PROFILE_NAME_KEYBOARD);
            case VIEW_PROFILE_SETTINGS_USER_CONTACTS -> messageService.configureMessage(chatId, fillContactInfo(user.getContacts()), KeyboardService.USER_CONTACTS_KEYBOARD);

            case VIEW_EMPLOYEE_SETTINGS -> messageService.configureMessage(chatId, fillEmployeeInfo(tavern.getEmployees()), KeyboardService.EMPLOYEE_KEYBOARD);

            default -> new SendMessage();
        };
    }

    private String fillCategory(Category category) {
        return Optional.ofNullable(category)
                .map(Category::getDescription)
                .map(description -> "Категория: <b>" + description + "</b>")
                .orElse("Категория не выбрана.");
    }

    private String fillEmployeeInfo(Set<UserEntity> employees) {
        return employees.stream()
                .sorted(Comparator.comparing(UserEntity::getId))
                .map(employee -> String.format("ID: <b>%d</b>, Имя: <b>%s</b>, %s", employee.getId(), employee.getName(), fillRoleInfo(employee.getRoles())))
                .collect(Collectors.joining("\n"));
    }

    private void updateSubState(UserEntity user, SubState subState) {
        user.setSubState(subState);
        userService.save(user);
    }

    private SendMessage configureMessage(UserEntity user, Long chatId, SubState subState, String text) {
        updateSubState(user, subState);
        return messageService.configureMessage(chatId, text, KeyboardService.CANCEL_KEYBOARD);
    }

    private SendMessage configureDeleteContacts(UserEntity user,
                                                SubState subState,
                                                Long chatId,
                                                Set<ContactEntity> contacts,
                                                ReplyKeyboardMarkup keyboard) {
        if (CollectionUtils.isEmpty(contacts)) {
            return messageService.configureMessage(chatId, "Нечего удалять.", keyboard);
        }

        updateSubState(user, subState);

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

    private SendMessage configureDeleteEmployees(UserEntity user,
                                                 Long chatId,
                                                 Set<UserEntity> employees) {
        if (CollectionUtils.isEmpty(employees)) {
            return messageService.configureMessage(chatId, "Некого удалять.", KeyboardService.EMPLOYEE_KEYBOARD);
        }

        updateSubState(user, SubState.DELETE_EMPLOYEE_SETTINGS);

        ReplyKeyboardMarkup employeesKeyboard = new ReplyKeyboardMarkup();
        List<KeyboardRow> rows = new ArrayList<>();
        employees.forEach(employee ->
                rows.add(new KeyboardRow(List.of(new KeyboardButton("ID: " + employee.getId() + " " + employee.getName()))))
        );

        rows.add(new KeyboardRow(List.of(new KeyboardButton(Button.CANCEL.getText()))));

        employeesKeyboard.setKeyboard(rows);
        employeesKeyboard.setResizeKeyboard(true);

        return messageService.configureMessage(chatId, "Выберите сотрудника, которого хотите удалить.", employeesKeyboard);
    }

    private String fillProfileInfo(UserEntity user) {
        return fillUserInfo(user.getName()) + "\n\n" +
                fillRoleInfo(user.getRoles()) + "\n\n" +
                fillContactInfo(user.getContacts()) + "\n\n";
    }

    private String fillGeneralInfo(TavernEntity tavern) {
        return fillTavernInfo(tavern.getName()) + "\n\n" +
                fillCategory(tavern.getCategory()) + "\n\n" +
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

        return "Роль: <b>" + rolesString + "</b>";
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
}
