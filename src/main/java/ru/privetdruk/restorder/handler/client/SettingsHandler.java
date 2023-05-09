package ru.privetdruk.restorder.handler.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import reactor.core.scheduler.Schedulers;
import ru.privetdruk.restorder.handler.MessageHandler;
import ru.privetdruk.restorder.mapper.ScheduleMapper;
import ru.privetdruk.restorder.model.consts.MessageText;
import ru.privetdruk.restorder.model.dto.ScheduleDto;
import ru.privetdruk.restorder.model.dto.ValidateTavernResult;
import ru.privetdruk.restorder.model.entity.*;
import ru.privetdruk.restorder.model.enums.*;
import ru.privetdruk.restorder.service.*;
import ru.privetdruk.restorder.service.util.TypeService;
import ru.privetdruk.restorder.service.util.ValidationService;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

import static org.springframework.util.CollectionUtils.isEmpty;
import static org.springframework.util.StringUtils.hasText;
import static ru.privetdruk.restorder.model.consts.MessageText.INCORRECT_ENTER_PHONE_NUMBER;
import static ru.privetdruk.restorder.model.consts.MessageText.PHONE_NUMBER_DUPLICATE;
import static ru.privetdruk.restorder.service.KeyboardService.*;
import static ru.privetdruk.restorder.service.MessageService.configureMarkdownMessage;
import static ru.privetdruk.restorder.service.MessageService.configureMessage;

@Slf4j
@Component
@RequiredArgsConstructor
public class SettingsHandler implements MessageHandler {
    private final BlacklistService blacklistService;
    private final BlacklistSettingService blacklistSettingService;
    private final ContactService contactService;
    private final EventService eventService;
    private final InfoService infoService;
    private final MainMenuHandler mainMenuHandler;
    private final MessageService messageService;
    private final UserService userService;
    private final ScheduleMapper scheduleMapper;
    private final ScheduleService scheduleService;
    private final TavernService tavernService;
    private final TelegramApiService telegramApiService;
    private final TypeService typeService;
    private final ValidationService validationService;

    private final Map<UserEntity, ScheduleDto> scheduleCache = new HashMap<>();
    private final Map<UserEntity, TableEntity> tableCache = new HashMap<>();
    private final Map<UserEntity, BlacklistEntity> blacklistCache = new HashMap<>();
    private final Map<UserEntity, String> blacklistPhoneNumberCache = new HashMap<>();

    @Value("${bot.client.username}")
    private String botName;

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
            case BACK, CANCEL, NO -> userService.updateSubState(user, subState.getParentSubState());
            case YES -> {
                switch (subState) {
                    case DELETE_PROFILE_SETTINGS -> {
                        if (user.getRoles().contains(Role.CLIENT_ADMIN)) {
                            tavernService.delete(tavern);
                        } else {
                            userService.delete(user);
                        }
                        return configureMessage(chatId, "Данные успешно удалены. Хорошего дня!", KeyboardService.REMOVE_KEYBOARD);
                    }
                    case DELETE_GENERAL_SETTINGS_TAVERN_LINK_TABLE_LAYOUT -> {
                        return configureDeleteLinkTableLayout(user, chatId);
                    }
                }
            }
            case RETURN_MAIN_MENU -> {
                userService.update(user, State.MAIN_MENU, SubState.VIEW_MAIN_MENU);

                return mainMenuHandler.handle(user, message, callback);
            }
            case CHANGE -> {
                switch (subState) {
                    case VIEW_GENERAL_SETTINGS_TAVERN_NAME:
                        return configureMessageWithCancel(user, chatId, SubState.CHANGE_GENERAL_SETTINGS_TAVERN_NAME, "Введите новое название:");
                    case VIEW_GENERAL_SETTINGS_TAVERN_DESCRIPTION:
                        return configureMessageWithCancel(user, chatId, SubState.CHANGE_GENERAL_SETTINGS_TAVERN_DESCRIPTION, "Введите новое описание:");
                    case VIEW_GENERAL_SETTINGS_TAVERN_LINK_TABLE_LAYOUT:
                        return configureMessageWithCancel(user, chatId, SubState.CHANGE_GENERAL_SETTINGS_TAVERN_LINK_TABLE_LAYOUT, "Введите новую ссылку:");
                    case VIEW_GENERAL_SETTINGS_TAVERN_ADDRESS:
                        return configureMessageWithCancel(user, chatId, SubState.CHANGE_GENERAL_SETTINGS_TAVERN_ADDRESS, "Введите новый адрес:");
                    case VIEW_PROFILE_SETTINGS_USER_NAME:
                        return configureMessageWithCancel(user, chatId, SubState.CHANGE_PROFILE_SETTINGS_USER_NAME, "Введите новое имя:");
                    case VIEW_GENERAL_SETTINGS_CATEGORIES:
                        userService.updateSubState(user, SubState.CHANGE_GENERAL_SETTINGS_CATEGORIES);

                        return configureMessage(
                                chatId,
                                "Выберите новую категорию.",
                                KeyboardService.CATEGORIES_LIST_WITH_CANCEL_KEYBOARD
                        );
                }
            }
            case ADD -> {
                switch (subState) {
                    case VIEW_GENERAL_SETTINGS_TAVERN_CONTACTS:
                        return configureMessageWithCancel(user, chatId, SubState.ADD_GENERAL_SETTINGS_TAVERN_CONTACTS, MessageText.ENTER_PHONE_NUMBER);
                    case VIEW_PROFILE_SETTINGS_USER_CONTACTS:
                        return configureMessageWithCancel(user, chatId, SubState.ADD_PROFILE_SETTINGS_USER_CONTACTS, MessageText.ENTER_PHONE_NUMBER);
                    case VIEW_EMPLOYEE_SETTINGS: {
                        EventEntity event = EventEntity.builder()
                                .params(Map.of(JsonbKey.TAVERN_ID.getKey(), tavern.getId()))
                                .type(EventType.REGISTER_EMPLOYEE)
                                .expirationDate(LocalDateTime.now().plusHours(1))
                                .build();

                        event = eventService.save(event);

                        return MessageService.configureHtmlMessage(
                                chatId,
                                String.format(
                                        "Регистрация по ссылке доступна в течении одного часа и только для одного человека." +
                                                System.lineSeparator() +
                                                "Перешлите данное сообщение вашему сотруднику." +
                                                System.lineSeparator() + System.lineSeparator() +
                                                "<a href=\"https://t.me/%s?start=%s\">> РЕГИСТРАЦИЯ</a>",
                                        botName,
                                        event.getUuid()
                                )
                        );
                    }
                    case VIEW_SCHEDULE_SETTINGS: {
                        return configureMessageWithCancel(
                                user,
                                chatId,
                                SubState.ADD_DAY_WEEK_SCHEDULE_SETTINGS,
                                "Выберите день недели.",
                                KeyboardService.DAY_WEEK_WITH_PERIOD_KEYBOARD
                        );
                    }
                    case VIEW_TABLE_SETTINGS: {
                        return configureMessageWithCancel(
                                user,
                                chatId,
                                SubState.ADD_LABEL_TABLE_SETTINGS,
                                "Введите маркер стола:"
                        );
                    }
                }
            }
            case DELETE -> {
                switch (subState) {
                    case VIEW_GENERAL_SETTINGS_TAVERN_CONTACTS -> {
                        return configureDeleteContacts(user, tavern, chatId);
                    }
                    case VIEW_PROFILE_SETTINGS_USER_CONTACTS -> {
                        return configureDeleteContacts(user, chatId);
                    }
                    case VIEW_EMPLOYEE_SETTINGS -> {
                        return configureDeleteEmployees(user, tavern, chatId);
                    }
                    case VIEW_SCHEDULE_SETTINGS -> {
                        return configureDeleteSchedule(user, tavern, chatId);
                    }
                    case VIEW_TABLE_SETTINGS -> {
                        return configureDeleteTables(user, tavern, chatId);
                    }
                    case VIEW_GENERAL_SETTINGS_TAVERN_LINK_TABLE_LAYOUT -> {
                        if (!hasText(tavern.getLinkTableLayout())) {
                            return configureMessage(chatId, "Нечего удалять.", CHANGE_DELETE_KEYBOARD);
                        }

                        userService.updateSubState(user, SubState.DELETE_GENERAL_SETTINGS_TAVERN_LINK_TABLE_LAYOUT);
                        return configureMessage(chatId, "Вы действительно хотите удалить ссылку?", KeyboardService.YES_NO_KEYBOARD);
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
                        userService.updateSubState(user, SubState.VIEW_SETTINGS);
                    }
                }
                case VIEW_SETTINGS -> {
                    switch (button) {
                        case GENERAL -> {
                            if (user.getRoles().contains(Role.CLIENT_ADMIN)) {
                                userService.updateSubState(user, SubState.VIEW_GENERAL_SETTINGS);
                            } else {
                                return configureMessage(
                                        chatId,
                                        "Выбранный пункт меню недоступен вашей роли.",
                                        SETTINGS_KEYBOARD
                                );
                            }
                        }
                        case PROFILE -> userService.updateSubState(user, SubState.VIEW_PROFILE_SETTINGS);
                        case BLACKLIST -> userService.updateSubState(user, SubState.VIEW_BLACKLIST_SETTINGS);
                        case EMPLOYEES -> {
                            if (user.getRoles().contains(Role.CLIENT_ADMIN)) {
                                userService.updateSubState(user, SubState.VIEW_EMPLOYEE_SETTINGS);
                            } else {
                                return configureMessage(
                                        chatId,
                                        "Выбранный пункт меню недоступен вашей роли.",
                                        SETTINGS_KEYBOARD
                                );
                            }
                        }
                        case SCHEDULE -> userService.updateSubState(user, SubState.VIEW_SCHEDULE_SETTINGS);
                        case TABLES -> userService.updateSubState(user, SubState.VIEW_TABLE_SETTINGS);
                    }
                }
                case VIEW_GENERAL_SETTINGS -> {
                    switch (button) {
                        case TAVERN_NAME -> userService.updateSubState(user, SubState.VIEW_GENERAL_SETTINGS_TAVERN_NAME);
                        case DESCRIPTION -> userService.updateSubState(user, SubState.VIEW_GENERAL_SETTINGS_TAVERN_DESCRIPTION);
                        case PHONE_NUMBER -> userService.updateSubState(user, SubState.VIEW_GENERAL_SETTINGS_TAVERN_CONTACTS);
                        case TAVERN_ADDRESS -> userService.updateSubState(user, SubState.VIEW_GENERAL_SETTINGS_TAVERN_ADDRESS);
                        case TAVERN_TABLE_LAYOUT -> userService.updateSubState(user, SubState.VIEW_GENERAL_SETTINGS_TAVERN_LINK_TABLE_LAYOUT);
                        case CATEGORIES -> userService.updateSubState(user, SubState.VIEW_GENERAL_SETTINGS_CATEGORIES);
                    }
                }
                case CHANGE_GENERAL_SETTINGS_TAVERN_NAME -> {
                    if (!hasText(messageText)) {
                        return configureMessage(chatId, MessageText.ENTER_EMPTY_VALUE_RETRY);
                    }

                    tavern = tavernService.findWithDataWithoutEmployees(tavern);

                    tavern.setName(messageText);

                    ValidateTavernResult validate = validationService.validate(tavern);
                    tavern.setValid(validate.isValid());

                    tavernService.save(tavern);

                    userService.updateSubState(user, user.getSubState().getParentSubState());
                }
                case CHANGE_GENERAL_SETTINGS_TAVERN_DESCRIPTION -> {
                    if (!hasText(messageText)) {
                        return configureMessage(chatId, MessageText.ENTER_EMPTY_VALUE_RETRY);
                    }

                    tavern.setDescription(messageText);
                    tavernService.save(tavern);

                    userService.updateSubState(user, user.getSubState().getParentSubState());
                }
                case CHANGE_GENERAL_SETTINGS_TAVERN_LINK_TABLE_LAYOUT -> {
                    if (!hasText(messageText)) {
                        return configureMessage(chatId, MessageText.ENTER_EMPTY_VALUE_RETRY);
                    }

                    tavern.setLinkTableLayout(messageText);
                    tavernService.save(tavern);

                    userService.updateSubState(user, user.getSubState().getParentSubState());
                }
                case ADD_GENERAL_SETTINGS_TAVERN_CONTACTS -> {
                    if (!hasText(messageText)) {
                        return configureMessage(chatId, MessageText.ENTER_EMPTY_VALUE_RETRY);
                    }

                    if (validationService.isNotValidPhone(messageText)) {
                        return configureMessage(chatId, "Вы ввели некорректный номер. Повторите попытку:");
                    }

                    tavern = tavernService.findWithDataWithoutEmployees(tavern);

                    ContactEntity contact = ContactEntity.builder()
                            .tavern(tavern)
                            .type(ContractType.MOBILE)
                            .value(messageText)
                            .build();

                    tavern.addContact(contact);

                    ValidateTavernResult validate = validationService.validate(tavern);
                    tavern.setValid(validate.isValid());

                    contactService.save(contact);

                    userService.updateSubState(user, user.getSubState().getParentSubState());
                }
                case DELETE_GENERAL_SETTINGS_TAVERN_CONTACTS -> {
                    userService.updateSubState(user, user.getSubState().getParentSubState());

                    if (!hasText(messageText)) {
                        return configureMessage(chatId, "Вы не выбрали номер! Операция отменяется.");
                    }

                    tavern = tavernService.findWithDataWithoutEmployees(tavern);

                    tavern.getContacts()
                            .removeIf(contact -> contact.getValue().equals(messageText));

                    ValidateTavernResult validate = validationService.validate(tavern);
                    tavern.setValid(validate.isValid());

                    tavernService.save(tavern);
                }
                case CHANGE_GENERAL_SETTINGS_TAVERN_ADDRESS -> {
                    if (!hasText(messageText)) {
                        return configureMessage(chatId, MessageText.ENTER_EMPTY_VALUE_RETRY);
                    }

                    tavern = tavernService.findWithDataWithoutEmployees(tavern);

                    tavern.getAddress().setStreet(messageText);

                    ValidateTavernResult validate = validationService.validate(tavern);
                    tavern.setValid(validate.isValid());

                    tavernService.save(tavern);

                    userService.updateSubState(user, user.getSubState().getParentSubState());
                }
                case CHANGE_GENERAL_SETTINGS_CATEGORIES -> {
                    Category category = Category.fromDescription(messageText);
                    if (category == null) {
                        return configureMessage(chatId, "Выбрано некорректное значение.");
                    }

                    tavern = tavernService.findWithDataWithoutEmployees(tavern);

                    tavern.setCategory(category);

                    ValidateTavernResult validate = validationService.validate(tavern);
                    tavern.setValid(validate.isValid());

                    tavernService.save(tavern);

                    userService.updateSubState(user, user.getSubState().getParentSubState());
                }
                case VIEW_PROFILE_SETTINGS -> {
                    switch (button) {
                        case USER_NAME -> userService.updateSubState(user, SubState.VIEW_PROFILE_SETTINGS_USER_NAME);
                        case PHONE_NUMBER -> userService.updateSubState(user, SubState.VIEW_PROFILE_SETTINGS_USER_CONTACTS);
                        case DELETE_PROFILE -> {
                            userService.updateSubState(user, SubState.DELETE_PROFILE_SETTINGS);

                            if (user.getRoles().contains(Role.CLIENT_EMPLOYEE)) {
                                return configureMessage(chatId, "Вы действительно хотите удалить профиль?", KeyboardService.YES_NO_KEYBOARD);
                            }

                            return configureMessage(
                                    chatId,
                                    "Вы является владельцем заведения. Вместе с вашим профилем будет удалено и заведение. Продолжить удаление?",
                                    KeyboardService.YES_NO_KEYBOARD
                            );
                        }
                    }
                }
                case CHANGE_PROFILE_SETTINGS_USER_NAME -> {
                    if (!hasText(messageText)) {
                        return configureMessage(chatId, "Вы ввели пустое значение! Повторите попытку.");
                    }

                    if (validationService.isNotValidName(messageText)) {
                        return configureMessage(
                                chatId,
                                "Имя должно содержать только символы кириллицы. Повторите попытку.",
                                KeyboardService.CANCEL_KEYBOARD
                        );
                    }

                    user.setName(messageText);
                    userService.updateSubState(user, user.getSubState().getParentSubState());
                }
                case ADD_PROFILE_SETTINGS_USER_CONTACTS -> {
                    if (!hasText(messageText)) {
                        return configureMessage(chatId, "Вы ввели пустое значение! Повторите попытку.");
                    }

                    if (validationService.isNotValidPhone(messageText)) {
                        return configureMessage(
                                chatId,
                                INCORRECT_ENTER_PHONE_NUMBER,
                                KeyboardService.CANCEL_KEYBOARD
                        );
                    }

                    List<ContactEntity> contacts = contactService.findByUser(user);

                    ContactEntity contact = ContactEntity.builder()
                            .user(user)
                            .type(ContractType.MOBILE)
                            .value(messageText)
                            .build();

                    if (contacts.contains(contact)) {
                        return configureMessage(
                                chatId,
                                PHONE_NUMBER_DUPLICATE,
                                KeyboardService.CANCEL_KEYBOARD
                        );
                    }

                    contactService.save(contact);

                    userService.updateSubState(user, user.getSubState().getParentSubState());
                }
                case DELETE_PROFILE_SETTINGS_USER_CONTACTS -> {
                    if (!hasText(messageText)) {
                        return cancelOperation(
                                user,
                                chatId,
                                "Вы не выбрали номер! Операция отменяется.",
                                ADD_DELETE_KEYBOARD
                        );
                    }

                    List<ContactEntity> contacts = contactService.findByUser(user);
                    List<ContactEntity> foundedContacts = contacts.stream()
                            .filter(contact -> contact.getValue().equals(messageText))
                            .toList();

                    if (isEmpty(foundedContacts)) {
                        return cancelOperation(
                                user,
                                chatId,
                                "Нечего удалять! Операция отменяется.",
                                ADD_DELETE_KEYBOARD
                        );
                    }

                    if (foundedContacts.size() > 1) {
                        return cancelOperation(
                                user,
                                chatId,
                                "Найдено больше одного номера! Операция отменяется. Обратитесь в поддержку.",
                                ADD_DELETE_KEYBOARD
                        );
                    }

                    contactService.delete(contacts.get(0));

                    userService.updateSubState(user, user.getSubState().getParentSubState());
                }
                case VIEW_BLACKLIST_SETTINGS -> {
                    switch (button) {
                        case BLOCK -> {
                            userService.updateSubState(user, SubState.ENTER_PHONE_NUMBER_BLACKLIST_SETTINGS);

                            return configureMessage(
                                    chatId,
                                    MessageText.ENTER_PHONE_NUMBER,
                                    KeyboardService.CANCEL_KEYBOARD
                            );
                        }
                        case UNBLOCK -> {
                            userService.updateSubState(user, SubState.UNBLOCK_BLACKLIST_SETTINGS);

                            return configureMessage(
                                    chatId,
                                    MessageText.ENTER_PHONE_NUMBER,
                                    KeyboardService.CANCEL_KEYBOARD
                            );
                        }
                        case BLACKLIST_LIST -> userService.updateSubState(user, SubState.VIEW_MANAGE_BLACKLIST_SETTINGS);
                        case SETTINGS -> userService.updateSubState(user, SubState.VIEW_SETTINGS_BLACKLIST_SETTINGS);
                    }
                }
                case VIEW_SETTINGS_BLACKLIST_SETTINGS -> {
                    switch (button) {
                        case NUMBER_TIMES -> {
                            userService.updateSubState(user, SubState.ENTER_NUMBER_TIMES_BLACKLIST_SETTINGS);

                            return configureMessage(
                                    chatId,
                                    "Введите кол-во раз, когда человек не пришел в заведение (0 отключить), чтобы автоматически его заблокировать:",
                                    KeyboardService.CANCEL_KEYBOARD
                            );
                        }
                        case NUMBER_DAYS -> {
                            userService.updateSubState(user, SubState.ENTER_NUMBER_DAYS_BLACKLIST_SETTINGS);

                            return configureMessage(
                                    chatId,
                                    "Введите кол-во дней блокировки (0 навсегда). По истечению установленных дней, блокировка будет автоматически снята.",
                                    KeyboardService.CANCEL_KEYBOARD
                            );
                        }
                    }
                }
                case ENTER_NUMBER_TIMES_BLACKLIST_SETTINGS -> {
                    try {
                        int times = Integer.parseInt(messageText);

                        if (times < 0) {
                            return configureMessage(
                                    chatId,
                                    MessageText.INCORRECT_VALUE_TRY_AGAIN,
                                    KeyboardService.CANCEL_KEYBOARD
                            );
                        }

                        BlacklistSettingEntity setting = blacklistSettingService.findByTavern(tavern)
                                .map(foundSetting -> {
                                    foundSetting.setTimes(times);
                                    return foundSetting;
                                })
                                .orElseGet(() -> BlacklistSettingEntity.builder()
                                        .id(user.getTavern().getId())
                                        .tavern(user.getTavern())
                                        .days(0)
                                        .times(times)
                                        .build());

                        blacklistSettingService.save(setting);

                        userService.updateSubState(user, SubState.VIEW_SETTINGS_BLACKLIST_SETTINGS);
                    } catch (NumberFormatException exception) {
                        return configureMessage(
                                chatId,
                                MessageText.INCORRECT_VALUE_TRY_AGAIN,
                                KeyboardService.CANCEL_KEYBOARD
                        );
                    }
                }
                case ENTER_NUMBER_DAYS_BLACKLIST_SETTINGS -> {
                    try {
                        int days = Integer.parseInt(messageText);

                        if (days < 0) {
                            return configureMessage(
                                    chatId,
                                    MessageText.INCORRECT_VALUE_TRY_AGAIN,
                                    KeyboardService.CANCEL_KEYBOARD
                            );
                        }

                        BlacklistSettingEntity setting = blacklistSettingService.findByTavern(tavern)
                                .map(foundSetting -> {
                                    foundSetting.setDays(days);
                                    return foundSetting;
                                })
                                .orElseGet(() -> BlacklistSettingEntity.builder()
                                        .times(0)
                                        .days(days)
                                        .tavern(user.getTavern())
                                        .build());

                        blacklistSettingService.save(setting);

                        userService.updateSubState(user, SubState.VIEW_SETTINGS_BLACKLIST_SETTINGS);
                    } catch (NumberFormatException exception) {
                        return configureMessage(
                                chatId,
                                MessageText.INCORRECT_VALUE_TRY_AGAIN,
                                KeyboardService.CANCEL_KEYBOARD
                        );
                    }
                }
                case UNBLOCK_BLACKLIST_SETTINGS -> {
                    if (validationService.isNotValidPhone(messageText)) {
                        return configureMessage(chatId, "Вы ввели некорректный номер. Повторите попытку:");
                    }

                    BlacklistEntity blacklist = blacklistService.findActiveByPhoneNumber(tavern, messageText);

                    if (blacklist == null) {
                        return configureMessage(chatId, "Блокировка для указанного номера не найдена.");
                    }

                    blacklistCache.remove(user);

                    return unblockUser(user, chatId, tavern, blacklist);
                }
                case ENTER_PHONE_NUMBER_BLACKLIST_SETTINGS -> {
                    if (validationService.isNotValidPhone(messageText)) {
                        return configureMessage(chatId, "Вы ввели некорректный номер. Повторите попытку:");
                    }

                    BlacklistEntity blacklist = blacklistService.findActiveByPhoneNumber(tavern, messageText);
                    if (blacklist != null) {
                        userService.updateSubState(user, SubState.VIEW_BLACKLIST_SETTINGS);
                        return configureMessage(chatId, "Указанный номер уже есть в чёрном списке вашего заведения." + System.lineSeparator()
                                + infoService.fillBlacklist(blacklist), BLACKLIST_KEYBOARD);
                    }

                    blacklistPhoneNumberCache.put(user, messageText);
                    userService.updateSubState(user, SubState.ENTER_REASON_BLACKLIST_SETTINGS);

                    return configureMessage(chatId, "Введите причину блокировки или выберите в меню.", REASON_BLACKLIST_KEYBOARD);
                }
                case ENTER_REASON_BLACKLIST_SETTINGS -> {
                    String phoneNumber = blacklistPhoneNumberCache.get(user);

                    if (!StringUtils.hasText(phoneNumber)) {
                        userService.updateSubState(user, SubState.VIEW_BLACKLIST_SETTINGS);
                        return configureMessage(chatId, "Произошла внутренняя ошибка, повторите попытку.", BLACKLIST_KEYBOARD);
                    }

                    UserEntity blockUser = userService.findByPhoneNumber(phoneNumber);

                    int days = blacklistSettingService.findByTavern(tavern)
                            .map(BlacklistSettingEntity::getDays)
                            .orElse(0);

                    LocalDateTime unlockDate = days > 0 ? LocalDateTime.now().plusDays(days) : LocalDateTime.of(9999, 12, 12, 0, 0, 0, 0);

                    BlacklistEntity blacklist = BlacklistEntity.builder()
                            .phoneNumber(phoneNumber)
                            .reason(messageText)
                            .user(blockUser)
                            .tavern(tavern)
                            .unlockDate(unlockDate)
                            .build();

                    blacklistService.save(blacklist);

                    sendMessageAboutLockUser(blockUser, tavern, MessageText.NOTIFY_USER_BLOCK);

                    userService.updateSubState(user, SubState.VIEW_BLACKLIST_SETTINGS);

                    return configureMessage(chatId, "Номер заблокирован.", BLACKLIST_KEYBOARD);
                }
                case VIEW_MANAGE_BLACKLIST_SETTINGS -> {
                    BlacklistEntity blacklist = blacklistService.findActiveByPhoneNumber(tavern, messageText);

                    if (blacklist == null) {
                        return configureMessage(chatId, "Блокировка для указанного номера не найдена.");
                    }

                    blacklistCache.put(user, blacklist);
                    userService.updateSubState(user, SubState.VIEW_MANAGE_USER_BLACKLIST_SETTINGS);
                }
                case VIEW_MANAGE_USER_BLACKLIST_SETTINGS -> {
                    BlacklistEntity blacklist = blacklistCache.get(user);

                    if (blacklist == null) {
                        userService.updateSubState(user, SubState.VIEW_BLACKLIST_SETTINGS);
                        return configureMessage(chatId, "Произошла внутренняя ошибка, повторите попытку.", BLACKLIST_KEYBOARD);
                    }

                    if (button == Button.UNBLOCK) {
                        return unblockUser(user, chatId, tavern, blacklist);
                    }
                }
                case DELETE_EMPLOYEE_SETTINGS -> {
                    if (user.getRoles().stream()
                            .noneMatch(role -> role == Role.CLIENT_ADMIN)) {
                        userService.updateSubState(user, user.getSubState().getParentSubState());

                        return configureMessage(chatId, "Данный функционал доступен только владельцу заведения.", ADD_DELETE_KEYBOARD);
                    }

                    Long employeeId = messageService.parseId(messageText);
                    if (!hasText(messageText) || employeeId == null) {
                        userService.updateSubState(user, user.getSubState().getParentSubState());

                        return configureMessage(chatId, "Вы ни кого не выбрали! Операция отменяется.", ADD_DELETE_KEYBOARD);
                    }

                    if (user.getId().equals(employeeId)) {
                        userService.updateSubState(user, user.getSubState().getParentSubState());

                        return configureMessage(chatId, "Себя нельзя удалить.", ADD_DELETE_KEYBOARD);
                    }

                    final Long finalEmployeeId = employeeId;
                    tavern = tavernService.findWithEmployees(tavern);

                    UserEntity deleteUser = tavern.getEmployees().stream()
                            .filter(employee -> employee.getId().equals(finalEmployeeId))
                            .findFirst()
                            .orElseThrow();

                    tavernService.save(tavern);

                    userService.delete(deleteUser);

                    userService.updateSubState(user, user.getSubState().getParentSubState());
                }
                case DELETE_SCHEDULE_SETTINGS -> {
                    final Long scheduleId = messageService.parseId(messageText);
                    if (!hasText(messageText) || scheduleId == null) {
                        userService.updateSubState(user, user.getSubState().getParentSubState());

                        return configureMessage(chatId, "Вы ничего не выбрали! Операция отменяется.", ADD_DELETE_KEYBOARD);
                    }

                    tavern = tavernService.findWithDataWithoutEmployees(tavern);

                    tavern.getSchedules().removeIf(schedule -> schedule.getId().equals(scheduleId));

                    ValidateTavernResult validate = validationService.validate(tavern);
                    tavern.setValid(validate.isValid());

                    tavernService.save(tavern);

                    userService.updateSubState(user, user.getSubState().getParentSubState());
                }
                case ADD_DAY_WEEK_SCHEDULE_SETTINGS -> {
                    scheduleCache.remove(user);

                    DayWeek dayWeek = DayWeek.fromFullName(messageText);
                    if (dayWeek == null && (button != Button.WEEKDAYS && button != Button.WEEKENDS)) {
                        return configureMessage(chatId, MessageText.INCORRECT_VALUE_TRY_AGAIN, KeyboardService.DAY_WEEK_WITH_PERIOD_KEYBOARD);
                    }

                    ScheduleDto schedule = new ScheduleDto();

                    if (dayWeek != null) {
                        schedule.setDayWeek(dayWeek);
                    } else if (button == Button.WEEKDAYS) {
                        schedule.setWeekdays(true);
                    } else {
                        schedule.setWeekends(true);
                    }

                    scheduleCache.put(user, schedule);

                    return configureMessageWithCancel(
                            user,
                            chatId,
                            SubState.ADD_START_HOUR_SCHEDULE_SETTINGS,
                            "Выберите час начала периода.",
                            KeyboardService.HOURS_WITH_CANCEL_KEYBOARD
                    );
                }
                case ADD_START_HOUR_SCHEDULE_SETTINGS -> {
                    if (!hasText(messageText) || !typeService.isInteger(messageText)) {
                        return configureMessage(chatId, MessageText.INCORRECT_VALUE_TRY_AGAIN, KeyboardService.HOURS_WITH_CANCEL_KEYBOARD);
                    }

                    LocalTime startPeriod = LocalTime.of(Integer.parseInt(messageText), 0);
                    scheduleCache.get(user).setStartPeriod(startPeriod);

                    return configureMessageWithCancel(
                            user,
                            chatId,
                            SubState.ADD_START_MINUTE_SCHEDULE_SETTINGS,
                            "Выберите минуту начала периода.",
                            KeyboardService.MINUTES_WITH_CANCEL_KEYBOARD
                    );
                }
                case ADD_START_MINUTE_SCHEDULE_SETTINGS -> {
                    if (!hasText(messageText) || !typeService.isInteger(messageText)) {
                        return configureMessage(chatId, MessageText.INCORRECT_VALUE_TRY_AGAIN, KeyboardService.MINUTES_WITH_CANCEL_KEYBOARD);
                    }

                    LocalTime startPeriod = scheduleCache.get(user)
                            .getStartPeriod()
                            .plusMinutes(Long.parseLong(messageText));
                    scheduleCache.get(user).setStartPeriod(startPeriod);

                    return configureMessageWithCancel(
                            user,
                            chatId,
                            SubState.ADD_END_HOUR_SCHEDULE_SETTINGS,
                            "Выберите час окончания периода.",
                            KeyboardService.HOURS_WITH_CANCEL_KEYBOARD
                    );
                }
                case ADD_END_HOUR_SCHEDULE_SETTINGS -> {
                    if (!hasText(messageText) || !typeService.isInteger(messageText)) {
                        return configureMessage(chatId, MessageText.INCORRECT_VALUE_TRY_AGAIN, KeyboardService.HOURS_WITH_CANCEL_KEYBOARD);
                    }

                    LocalTime endPeriod = LocalTime.of(Integer.parseInt(messageText), 0);
                    scheduleCache.get(user).setEndPeriod(endPeriod);

                    return configureMessageWithCancel(
                            user,
                            chatId,
                            SubState.ADD_END_MINUTE_SCHEDULE_SETTINGS,
                            "Выберите минуту окончания периода.",
                            KeyboardService.MINUTES_WITH_CANCEL_KEYBOARD
                    );
                }
                case ADD_END_MINUTE_SCHEDULE_SETTINGS -> {
                    if (!hasText(messageText) || !typeService.isInteger(messageText)) {
                        return configureMessage(chatId, MessageText.INCORRECT_VALUE_TRY_AGAIN, KeyboardService.MINUTES_WITH_CANCEL_KEYBOARD);
                    }

                    LocalTime endPeriod = scheduleCache.get(user)
                            .getEndPeriod()
                            .plusMinutes(Long.parseLong(messageText));
                    scheduleCache.get(user).setEndPeriod(endPeriod);

                    return configureMessageWithCancel(
                            user,
                            chatId,
                            SubState.ADD_PRICE_SCHEDULE_SETTINGS,
                            "Введите стоимость входа:",
                            KeyboardService.FREE_WITH_CANCEL_KEYBOARD
                    );
                }
                case ADD_PRICE_SCHEDULE_SETTINGS -> {
                    if (!hasText(messageText) || (button != Button.FREE && !typeService.isInteger(messageText))) {
                        return configureMessage(chatId, MessageText.INCORRECT_VALUE_TRY_AGAIN, KeyboardService.MINUTES_WITH_CANCEL_KEYBOARD);
                    }

                    ScheduleDto schedule = scheduleCache.get(user);
                    Integer price = button == Button.FREE ? 0 : Integer.parseInt(messageText);
                    schedule.setPrice(price);

                    List<ScheduleDto> schedules;
                    if (schedule.isWeekdays()) {
                        schedules = DayWeek.WEEKDAYS_LIST.stream()
                                .map(dayWeek -> new ScheduleDto(dayWeek, schedule))
                                .toList();
                    } else if (schedule.isWeekends()) {
                        schedules = DayWeek.WEEKENDS_LIST.stream()
                                .map(dayWeek -> new ScheduleDto(dayWeek, schedule))
                                .toList();
                    } else {
                        schedules = List.of(schedule);
                    }

                    TavernEntity finalTavern = tavernService.findWithDataWithoutEmployees(tavern);

                    schedules = schedules.stream()
                            .filter(scheduleDto -> scheduleService.checkTimePeriodAvailability(
                                    finalTavern.getSchedules(),
                                    scheduleDto.getDayWeek(),
                                    scheduleDto.getStartPeriod(),
                                    scheduleDto.getEndPeriod())
                            )
                            .toList();

                    List<ScheduleEntity> scheduleEntities = schedules.stream()
                            .map(scheduleMapper::toEntity)
                            .peek(scheduleEntity -> scheduleEntity.setTavern(finalTavern))
                            .toList();

                    finalTavern.getSchedules().addAll(scheduleEntities);

                    ValidateTavernResult validate = validationService.validate(finalTavern);
                    finalTavern.setValid(validate.isValid());

                    tavernService.save(finalTavern);

                    userService.updateSubState(user, subState.getParentSubState());
                }
                case ADD_LABEL_TABLE_SETTINGS -> {
                    tableCache.remove(user);

                    if (!hasText(messageText)) {
                        return configureMessage(chatId, MessageText.ENTER_EMPTY_VALUE, KeyboardService.DAY_WEEK_WITH_PERIOD_KEYBOARD);
                    }

                    TableEntity table = new TableEntity();
                    table.setTavern(user.getTavern());
                    table.setLabel(messageText);

                    tableCache.put(user, table);

                    return configureMessageWithCancel(user, chatId, SubState.ADD_NUMBER_SEATS_TABLE_SETTINGS, "Введите кол-во мест:");
                }
                case ADD_NUMBER_SEATS_TABLE_SETTINGS -> {
                    if (!hasText(messageText) || !typeService.isInteger(messageText) || Integer.parseInt(messageText) < 1) {
                        return configureMessage(chatId, MessageText.INCORRECT_VALUE_TRY_AGAIN, KeyboardService.MINUTES_WITH_CANCEL_KEYBOARD);
                    }

                    userService.updateSubState(user, subState.getParentSubState());

                    TableEntity table = tableCache.get(user);
                    table.setNumberSeats(Integer.parseInt(messageText));

                    tavern = tavernService.findWithDataWithoutEmployees(tavern);

                    final String label = table.getLabel();
                    boolean isExists = tavern.getTables().stream()
                            .anyMatch(tableEntity -> label.equalsIgnoreCase(tableEntity.getLabel()));

                    if (isExists) {
                        return configureMessage(
                                chatId,
                                "Стол с указанным маркером уже существует.\n\n" + infoService.fillTables(tavern.getTables()),
                                ADD_DELETE_KEYBOARD
                        );
                    }

                    tavern.getTables().add(table);

                    ValidateTavernResult validate = validationService.validate(tavern);
                    tavern.setValid(validate.isValid());

                    tavernService.save(tavern);
                }
                case DELETE_TABLE_SETTINGS -> {
                    final Long tableId = messageService.parseId(messageText);
                    if (!hasText(messageText) || tableId == null) {
                        userService.updateSubState(user, user.getSubState().getParentSubState());

                        return configureMessage(chatId, "Вы ничего не выбрали! Операция отменяется.", ADD_DELETE_KEYBOARD);
                    }

                    tavern = tavernService.findWithDataWithoutEmployees(tavern);

                    List<TableEntity> tables = tavern.getTables();

                    tables.removeIf(table -> table.getId().equals(tableId));
                    tavern.setTables(tables);

                    ValidateTavernResult validate = validationService.validate(tavern);
                    tavern.setValid(validate.isValid());

                    tavernService.save(tavern);

                    userService.updateSubState(user, user.getSubState().getParentSubState());
                }
            }
        }

        // отрисовка меню
        return switch (user.getSubState()) {
            case VIEW_SETTINGS -> configureMessage(chatId, "Открываю все настройки.", SETTINGS_KEYBOARD);
            case VIEW_GENERAL_SETTINGS -> configureMessage(chatId, infoService.fillGeneralWithLoadData(tavern), GENERAL_KEYBOARD);
            case VIEW_GENERAL_SETTINGS_TAVERN_NAME -> configureMessage(chatId, infoService.fillTavernName(tavern.getName()), CHANGE_KEYBOARD);
            case VIEW_GENERAL_SETTINGS_TAVERN_DESCRIPTION ->
                    configureMessage(chatId, infoService.fillTavernDescription(tavern.getDescription()), CHANGE_KEYBOARD);
            case VIEW_GENERAL_SETTINGS_TAVERN_LINK_TABLE_LAYOUT ->
                    configureMarkdownMessage(chatId, infoService.fillTavernLinkTableLayout(tavern.getLinkTableLayout()), CHANGE_DELETE_KEYBOARD);
            case VIEW_GENERAL_SETTINGS_TAVERN_CONTACTS -> configureMessage(chatId, infoService.fillContact(tavern), ADD_DELETE_KEYBOARD);
            case VIEW_GENERAL_SETTINGS_TAVERN_ADDRESS ->
                    configureMessage(chatId, infoService.fillAddress(tavern.getAddress()), CHANGE_KEYBOARD);
            case VIEW_GENERAL_SETTINGS_CATEGORIES -> configureMessage(chatId, infoService.fillCategory(tavern.getCategory()), CHANGE_KEYBOARD);
            case VIEW_PROFILE_SETTINGS -> configureMessage(chatId, infoService.fillProfile(user), PROFILE_KEYBOARD);
            case VIEW_PROFILE_SETTINGS_USER_NAME -> configureMessage(chatId, infoService.fillUser(user.getName()), CHANGE_KEYBOARD);
            case VIEW_PROFILE_SETTINGS_USER_CONTACTS -> configureMessage(chatId, infoService.fillContact(user), ADD_DELETE_KEYBOARD);
            case VIEW_BLACKLIST_SETTINGS -> configureMessage(chatId, "Открываю управление блокировками.", BLACKLIST_KEYBOARD);
            case VIEW_MANAGE_BLACKLIST_SETTINGS -> configureBlacklistManage(user, chatId);
            case VIEW_MANAGE_USER_BLACKLIST_SETTINGS ->
                    configureMessage(chatId, infoService.fillBlacklist(blacklistCache.get(user)), MANAGE_USER_BLACKLIST_KEYBOARD);
            case VIEW_SETTINGS_BLACKLIST_SETTINGS ->
                    configureMessage(chatId, infoService.fillBlacklistSettings(tavern), SETTINGS_BLACKLIST_KEYBOARD);
            case VIEW_EMPLOYEE_SETTINGS -> configureMessage(chatId, infoService.fillEmployee(tavern), ADD_DELETE_KEYBOARD);
            case VIEW_SCHEDULE_SETTINGS -> configureMessage(chatId, infoService.fillSchedules(tavern), ADD_DELETE_KEYBOARD);
            case VIEW_TABLE_SETTINGS -> configureMessage(chatId, infoService.fillTables(tavern), ADD_DELETE_KEYBOARD);


            default -> new SendMessage();
        };
    }

    private SendMessage unblockUser(UserEntity user, Long chatId, TavernEntity tavern, BlacklistEntity blacklist) {
        blacklistService.unlock(blacklist);

        sendMessageAboutLockUser(
                userService.findByPhoneNumber(blacklist.getPhoneNumber()),
                tavern,
                "Вы удалены из чёрного списка в заведении "
        );

        userService.updateSubState(user, SubState.VIEW_BLACKLIST_SETTINGS);

        return configureMessage(chatId, "Номер разблокирован.", BLACKLIST_KEYBOARD);
    }

    private void sendMessageAboutLockUser(UserEntity user, TavernEntity tavern, String message) {
        if (user == null) {
            return;
        }

        telegramApiService.sendMessage(
                        user.getTelegramId(),
                        message + tavern.getName(),
                        false
                )
                .subscribeOn(Schedulers.boundedElastic())
                .subscribe();
    }

    private SendMessage cancelOperation(UserEntity user, Long chatId, String message, ReplyKeyboardMarkup keyboardMarkup) {
        userService.updateSubState(user, user.getSubState().getParentSubState());

        return configureMessage(chatId, message, keyboardMarkup);
    }

    private SendMessage configureDeleteLinkTableLayout(UserEntity user, Long chatId) {
        TavernEntity tavern = user.getTavern();
        tavern.setLinkTableLayout(null);

        tavernService.save(tavern);

        userService.updateSubState(user, user.getSubState().getParentSubState());

        return configureMessage(chatId, "Ссылка успешно удалена.", CHANGE_DELETE_KEYBOARD);
    }

    private SendMessage configureMessageWithCancel(UserEntity user, Long chatId, SubState subState, String text) {
        userService.updateSubState(user, subState);
        return configureMessage(chatId, text, KeyboardService.CANCEL_KEYBOARD);
    }

    private SendMessage configureMessageWithCancel(UserEntity user, Long chatId, SubState subState, String text, ReplyKeyboardMarkup keyboard) {
        SendMessage sendMessage = configureMessageWithCancel(user, chatId, subState, text);
        sendMessage.setReplyMarkup(keyboard);
        return sendMessage;
    }

    private SendMessage configureDeleteContacts(UserEntity user, Long chatId) {
        return configureDeleteContacts(
                user,
                contactService.findByUser(user),
                SubState.DELETE_PROFILE_SETTINGS_USER_CONTACTS,
                chatId,
                ADD_DELETE_KEYBOARD
        );
    }

    private SendMessage configureDeleteContacts(UserEntity user,
                                                TavernEntity tavern,
                                                Long chatId) {
        return configureDeleteContacts(
                user,
                contactService.findByTavern(tavern),
                SubState.DELETE_GENERAL_SETTINGS_TAVERN_CONTACTS,
                chatId,
                ADD_DELETE_KEYBOARD
        );
    }

    private SendMessage configureDeleteContacts(UserEntity user,
                                                Collection<ContactEntity> contacts,
                                                SubState subState,
                                                Long chatId,
                                                ReplyKeyboardMarkup keyboard) {
        if (isEmpty(contacts)) {
            return configureMessage(chatId, "Нечего удалять.", keyboard);
        }

        userService.updateSubState(user, subState);

        ReplyKeyboardMarkup contactKeyboard = new ReplyKeyboardMarkup();
        List<KeyboardRow> rows = new ArrayList<>();

        contacts.forEach(contact -> rows.add(newKeyboardRow(contact.getValue())));

        rows.add(newKeyboardRow(Button.CANCEL));

        contactKeyboard.setKeyboard(rows);
        contactKeyboard.setResizeKeyboard(true);

        return configureMessage(chatId, "Выберите номер телефона, который хотите удалить.", contactKeyboard);
    }

    private SendMessage configureBlacklistManage(UserEntity user, Long chatId) {
        List<BlacklistEntity> blacklist = blacklistService.findActiveByTavern(user.getTavern());

        if (CollectionUtils.isEmpty(blacklist)) {
            userService.updateSubState(user, SubState.VIEW_BLACKLIST_SETTINGS);
            return configureMessage(chatId, "Нет заблокированных номеров.", BLACKLIST_KEYBOARD);
        }

        ReplyKeyboardMarkup blacklistKeyboard = new ReplyKeyboardMarkup();
        List<KeyboardRow> rows = new ArrayList<>();

        if (blacklist.size() > 10) {
            rows.add(KeyboardService.BACK_AND_MAIN_MENU_ROW);
        }
        blacklist.forEach(block -> rows.add(newKeyboardRow(block.getPhoneNumber())));
        rows.add(KeyboardService.BACK_AND_MAIN_MENU_ROW);

        blacklistKeyboard.setKeyboard(rows);
        blacklistKeyboard.setResizeKeyboard(true);

        return configureMessage(chatId, "Выберите или введите номер телефона для получения дополнительной информации.", blacklistKeyboard);
    }

    private SendMessage configureDeleteEmployees(UserEntity user, TavernEntity tavern, Long chatId) {
        List<UserEntity> employees = tavernService.findWithEmployees(tavern).getEmployees();
        if (isEmpty(employees)) {
            return configureMessage(chatId, "Некого удалять.", ADD_DELETE_KEYBOARD);
        }

        userService.updateSubState(user, SubState.DELETE_EMPLOYEE_SETTINGS);

        ReplyKeyboardMarkup employeesKeyboard = new ReplyKeyboardMarkup();
        List<KeyboardRow> rows = new ArrayList<>();
        employees.forEach(employee ->
                rows.add(newKeyboardRow(employee.getName() + " [" + employee.getId() + "]"))
        );

        rows.add(newKeyboardRow(Button.CANCEL));

        employeesKeyboard.setKeyboard(rows);
        employeesKeyboard.setResizeKeyboard(true);

        return configureMessage(chatId, "Выберите сотрудника, которого хотите удалить.", employeesKeyboard);
    }

    private SendMessage configureDeleteTables(UserEntity user,
                                              TavernEntity tavern,
                                              Long chatId) {
        Collection<TableEntity> tables = tavernService.findWithTables(tavern).getTables();
        if (isEmpty(tables)) {
            return configureMessage(chatId, "Нечего удалять.", ADD_DELETE_KEYBOARD);
        }

        userService.updateSubState(user, SubState.DELETE_TABLE_SETTINGS);

        ReplyKeyboardMarkup tablesKeyboard = new ReplyKeyboardMarkup();
        List<KeyboardRow> rows = new ArrayList<>();
        tables.forEach(table -> rows.add(newKeyboardRow(table.getLabel() + " [" + table.getId() + "]")));
        rows.add(newKeyboardRow(Button.CANCEL));

        tablesKeyboard.setKeyboard(rows);
        tablesKeyboard.setResizeKeyboard(true);

        return configureMessage(chatId, "Выберите стол, который хотите удалить.", tablesKeyboard);
    }

    private SendMessage configureDeleteSchedule(UserEntity user,
                                                TavernEntity tavern,
                                                Long chatId) {
        Collection<ScheduleEntity> schedules = tavernService.findWithSchedules(tavern).getSchedules();
        if (isEmpty(schedules)) {
            return configureMessage(chatId, "Нечего удалять.", ADD_DELETE_KEYBOARD);
        }

        userService.updateSubState(user, SubState.DELETE_SCHEDULE_SETTINGS);

        ReplyKeyboardMarkup employeesKeyboard = new ReplyKeyboardMarkup();
        List<KeyboardRow> rows = new ArrayList<>();
        Map<DayWeek, List<ScheduleEntity>> groupingSchedules = schedules.stream()
                .collect(Collectors.groupingBy(ScheduleEntity::getDayWeek));

        for (DayWeek dayWeek : DayWeek.SORTED_DAY_WEEK_LIST) {
            List<ScheduleEntity> schedulesByDayWeek = groupingSchedules.get(dayWeek);
            if (isEmpty(schedulesByDayWeek)) {
                continue;
            }

            schedulesByDayWeek.stream()
                    .sorted(Comparator.comparing(ScheduleEntity::getStartPeriod))
                    .forEach(schedule ->
                            rows.add(newKeyboardRow(
                                    String.format(
                                            "%s %s - %s %sр. [%s]",
                                            schedule.getDayWeek().getFullName(),
                                            schedule.getStartPeriod(),
                                            schedule.getEndPeriod(),
                                            schedule.getPrice(),
                                            schedule.getId()
                                    )
                            ))
                    );
        }

        rows.add(newKeyboardRow(Button.CANCEL));

        employeesKeyboard.setKeyboard(rows);
        employeesKeyboard.setResizeKeyboard(true);

        return configureMessage(chatId, "Выберите запись, которую хотите удалить.", employeesKeyboard);
    }
}
