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
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import ru.privetdruk.restorder.handler.MessageHandler;
import ru.privetdruk.restorder.mapper.ScheduleMapper;
import ru.privetdruk.restorder.model.consts.MessageText;
import ru.privetdruk.restorder.model.dto.ScheduleDto;
import ru.privetdruk.restorder.model.entity.*;
import ru.privetdruk.restorder.model.enums.*;
import ru.privetdruk.restorder.service.*;
import ru.privetdruk.restorder.service.util.TypeService;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class SettingsHandler implements MessageHandler {
    private final MainMenuHandler mainMenuHandler;
    private final MessageService messageService;
    private final UserService userService;
    private final TavernService tavernService;
    private final ContactService contactService;
    private final EventService eventService;
    private final TypeService typeService;
    private final ScheduleService scheduleService;
    private final ScheduleMapper scheduleMapper;
    private final TableService tableService;

    private final Map<UserEntity, ScheduleDto> scheduleTemporary = new HashMap<>();
    private final Map<UserEntity, TableEntity> tableTemporary = new HashMap<>();

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
            case BACK, CANCEL, NO -> user.setSubState(subState.getParentSubState());
            case YES -> {
                if (subState == SubState.DELETE_PROFILE_SETTINGS) {
                    if (user.getRoles().contains(Role.CLIENT_ADMIN)) {
                        tavernService.delete(tavern);
                    } else {
                        userService.delete(user);
                    }

                    return MessageService.configureMessage(chatId, "Данные успешно удалены. Хорошего дня!", KeyboardService.REMOVE_KEYBOARD);
                }
            }
            case RETURN_MAIN_MENU -> {
                user.setState(State.MAIN_MENU);
                userService.updateSubState(user, SubState.VIEW_MAIN_MENU);

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
                        userService.updateSubState(user, SubState.CHANGE_GENERAL_SETTINGS_CATEGORIES);

                        return MessageService.configureMessage(chatId, "Выберите новую категорию.", KeyboardService.CATEGORIES_LIST_WITH_CANCEL_KEYBOARD);
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
                        return configureMessage(user, chatId, SubState.ADD_DAY_WEEK_SCHEDULE_SETTINGS, "Выберите день недели.", KeyboardService.DAY_WEEK_WITH_PERIOD_KEYBOARD);
                    }
                    case VIEW_TABLE_SETTINGS: {
                        return configureMessage(user, chatId, SubState.ADD_LABEL_TABLE_SETTINGS, "Введите маркер стола:");
                    }
                }
            }
            case DELETE -> {
                switch (subState) {
                    case VIEW_GENERAL_SETTINGS_TAVERN_CONTACTS:
                        return configureDeleteContacts(user, SubState.DELETE_GENERAL_SETTINGS_TAVERN_CONTACTS, chatId, KeyboardService.TAVERN_CONTACTS_KEYBOARD);
                    case VIEW_PROFILE_SETTINGS_USER_CONTACTS:
                        return configureDeleteContacts(user, SubState.DELETE_PROFILE_SETTINGS_USER_CONTACTS, chatId, KeyboardService.USER_CONTACTS_KEYBOARD);
                    case VIEW_EMPLOYEE_SETTINGS: {
                        return configureDeleteEmployees(user, chatId);
                    }
                    case VIEW_SCHEDULE_SETTINGS: {
                        return configureDeleteSchedule(user, chatId);
                    }
                    case VIEW_TABLE_SETTINGS: {
                        return configureDeleteTables(user, chatId);
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
                        case GENERAL -> userService.updateSubState(user, SubState.VIEW_GENERAL_SETTINGS);
                        case PROFILE -> userService.updateSubState(user, SubState.VIEW_PROFILE_SETTINGS);
                        case EMPLOYEES -> userService.updateSubState(user, SubState.VIEW_EMPLOYEE_SETTINGS);
                        case SCHEDULE -> userService.updateSubState(user, SubState.VIEW_SCHEDULE_SETTINGS);
                        case TABLES -> userService.updateSubState(user, SubState.VIEW_TABLE_SETTINGS);
                    }
                }
                case VIEW_GENERAL_SETTINGS -> {
                    switch (button) {
                        case TAVERN_NAME -> userService.updateSubState(user, SubState.VIEW_GENERAL_SETTINGS_TAVERN_NAME);
                        case CONTACTS -> userService.updateSubState(user, SubState.VIEW_GENERAL_SETTINGS_TAVERN_CONTACTS);
                        case TAVERN_ADDRESS -> userService.updateSubState(user, SubState.VIEW_GENERAL_SETTINGS_TAVERN_ADDRESS);
                        case CATEGORIES -> userService.updateSubState(user, SubState.VIEW_GENERAL_SETTINGS_CATEGORIES);
                    }
                }
                case CHANGE_GENERAL_SETTINGS_TAVERN_NAME -> {
                    if (!StringUtils.hasText(messageText)) {
                        return MessageService.configureMessage(chatId, "Вы ввели пустое значение! Повторите попытку.");
                    }

                    tavern.setName(messageText);
                    tavernService.save(tavern);

                    userService.updateSubState(user, user.getSubState().getParentSubState());
                }
                case ADD_GENERAL_SETTINGS_TAVERN_CONTACTS -> {
                    if (!StringUtils.hasText(messageText)) {
                        return MessageService.configureMessage(chatId, "Вы ввели пустое значение! Повторите попытку.");
                    }

                    // TODO валидация номера

                    ContactEntity contact = ContactEntity.builder()
                            .tavern(tavern)
                            .type(ContractType.MOBILE)
                            .value(messageText)
                            .build();

                    tavern.addContact(contact);
                    contactService.save(contact);

                    userService.updateSubState(user, user.getSubState().getParentSubState());
                }
                case DELETE_GENERAL_SETTINGS_TAVERN_CONTACTS -> {
                    userService.updateSubState(user, user.getSubState().getParentSubState());

                    if (!StringUtils.hasText(messageText)) {
                        return MessageService.configureMessage(chatId, "Вы не выбрали номер! Операция отменяется.");
                    }

                    tavern.getContacts().removeIf(contact -> contact.getValue().equals(messageText));
                    tavernService.save(tavern);
                }
                case CHANGE_GENERAL_SETTINGS_TAVERN_ADDRESS -> {
                    if (!StringUtils.hasText(messageText)) {
                        return MessageService.configureMessage(chatId, "Вы ввели пустое значение! Повторите попытку.");
                    }

                    tavern.getAddress().setStreet(messageText);
                    tavernService.save(tavern);

                    userService.updateSubState(user, user.getSubState().getParentSubState());
                }
                case CHANGE_GENERAL_SETTINGS_CATEGORIES -> {
                    Category category = Category.fromDescription(messageText);
                    if (category == null) {
                        return MessageService.configureMessage(chatId, "Выбрано некорректное значение.");
                    }

                    tavern.setCategory(category);
                    tavernService.save(tavern);

                    userService.updateSubState(user, user.getSubState().getParentSubState());
                }
                case VIEW_PROFILE_SETTINGS -> {
                    switch (button) {
                        case USER_NAME -> userService.updateSubState(user, SubState.VIEW_PROFILE_SETTINGS_USER_NAME);
                        case CONTACTS -> userService.updateSubState(user, SubState.VIEW_PROFILE_SETTINGS_USER_CONTACTS);
                        case DELETE_PROFILE -> {
                            userService.updateSubState(user, SubState.DELETE_PROFILE_SETTINGS);

                            if (user.getRoles().contains(Role.CLIENT_EMPLOYEE)) {
                                return MessageService.configureMessage(chatId, "Вы действительно хотите удалить профиль?", KeyboardService.YES_NO_KEYBOARD);
                            }

                            return MessageService.configureMessage(chatId, "Вы является владельцем заведения X. Вместе с вашим профилем будет удалено и заведение. Продолжить удаление?", KeyboardService.YES_NO_KEYBOARD);
                        }
                    }
                }
                case CHANGE_PROFILE_SETTINGS_USER_NAME -> {
                    if (!StringUtils.hasText(messageText)) {
                        return MessageService.configureMessage(chatId, "Вы ввели пустое значение! Повторите попытку.");
                    }

                    user.setName(messageText);
                    userService.updateSubState(user, user.getSubState().getParentSubState());
                }
                case ADD_PROFILE_SETTINGS_USER_CONTACTS -> {
                    if (!StringUtils.hasText(messageText)) {
                        return MessageService.configureMessage(chatId, "Вы ввели пустое значение! Повторите попытку.");
                    }

                    // TODO валидация номера

                    ContactEntity contact = ContactEntity.builder()
                            .user(user)
                            .type(ContractType.MOBILE)
                            .value(messageText)
                            .build();

                    user.addContact(contact);
                    userService.updateSubState(user, user.getSubState().getParentSubState());
                }
                case DELETE_PROFILE_SETTINGS_USER_CONTACTS -> {
                    if (!StringUtils.hasText(messageText)) {
                        userService.updateSubState(user, user.getSubState().getParentSubState());

                        return MessageService.configureMessage(chatId, "Вы не выбрали номер! Операция отменяется.");
                    }

                    user.getContacts()
                            .removeIf(contact -> contact.getValue().equals(messageText));

                    userService.updateSubState(user, user.getSubState().getParentSubState());
                }
                case DELETE_EMPLOYEE_SETTINGS -> {
                    if (user.getRoles().stream()
                            .noneMatch(role -> role == Role.CLIENT_ADMIN)) {
                        userService.updateSubState(user, user.getSubState().getParentSubState());

                        return MessageService.configureMessage(chatId, "Данный функционал доступен только владельцу заведения.", KeyboardService.EMPLOYEE_KEYBOARD);
                    }

                    Long employeeId = messageService.parseId(messageText);
                    if (!StringUtils.hasText(messageText) || employeeId == null) {
                        userService.updateSubState(user, user.getSubState().getParentSubState());

                        return MessageService.configureMessage(chatId, "Вы ни кого не выбрали! Операция отменяется.", KeyboardService.EMPLOYEE_KEYBOARD);
                    }

                    if (user.getId().equals(employeeId)) {
                        userService.updateSubState(user, user.getSubState().getParentSubState());

                        return MessageService.configureMessage(chatId, "Себя нельзя удалить.", KeyboardService.EMPLOYEE_KEYBOARD);
                    }

                    final Long finalEmployeeId = employeeId;
                    tavern.getEmployees()
                            .removeIf(employee -> employee.getId().equals(finalEmployeeId));

                    tavernService.save(tavern);

                    userService.updateSubState(user, user.getSubState().getParentSubState());
                }
                case DELETE_SCHEDULE_SETTINGS -> {
                    final Long scheduleId = messageService.parseId(messageText);
                    if (!StringUtils.hasText(messageText) || scheduleId == null) {
                        userService.updateSubState(user, user.getSubState().getParentSubState());

                        return MessageService.configureMessage(chatId, "Вы ничего не выбрали! Операция отменяется.", KeyboardService.SCHEDULE_KEYBOARD);
                    }

                    Set<ScheduleEntity> schedules = tavern.getSchedules();

                    schedules.removeIf(schedule -> schedule.getId().equals(scheduleId));

                    scheduleService.save(schedules);

                    userService.updateSubState(user, user.getSubState().getParentSubState());
                }
                case ADD_DAY_WEEK_SCHEDULE_SETTINGS -> {
                    scheduleTemporary.remove(user);

                    DayWeek dayWeek = DayWeek.fromFullName(messageText);
                    if (dayWeek == null && (button != Button.WEEKDAYS && button != Button.WEEKENDS)) {
                        return MessageService.configureMessage(chatId, MessageText.INCORRECT_VALUE_TRY_AGAIN, KeyboardService.DAY_WEEK_WITH_PERIOD_KEYBOARD);
                    }

                    ScheduleDto schedule = new ScheduleDto();

                    if (dayWeek != null) {
                        schedule.setDayWeek(dayWeek);
                    } else if (button == Button.WEEKDAYS) {
                        schedule.setWeekdays(true);
                    } else {
                        schedule.setWeekends(true);
                    }

                    scheduleTemporary.put(user, schedule);

                    return configureMessage(user, chatId, SubState.ADD_START_HOUR_SCHEDULE_SETTINGS, "Выберите час начала периода.", KeyboardService.HOURS_WITH_CANCEL_KEYBOARD);
                }
                case ADD_START_HOUR_SCHEDULE_SETTINGS -> {
                    if (!StringUtils.hasText(messageText) || !typeService.isInteger(messageText)) {
                        return MessageService.configureMessage(chatId, MessageText.INCORRECT_VALUE_TRY_AGAIN, KeyboardService.HOURS_WITH_CANCEL_KEYBOARD);
                    }

                    LocalTime startPeriod = LocalTime.of(Integer.parseInt(messageText), 0);
                    scheduleTemporary.get(user).setStartPeriod(startPeriod);

                    return configureMessage(user, chatId, SubState.ADD_START_MINUTE_SCHEDULE_SETTINGS, "Выберите минуту начала периода.", KeyboardService.MINUTES_WITH_CANCEL_KEYBOARD);
                }
                case ADD_START_MINUTE_SCHEDULE_SETTINGS -> {
                    if (!StringUtils.hasText(messageText) || !typeService.isInteger(messageText)) {
                        return MessageService.configureMessage(chatId, MessageText.INCORRECT_VALUE_TRY_AGAIN, KeyboardService.MINUTES_WITH_CANCEL_KEYBOARD);
                    }

                    LocalTime startPeriod = scheduleTemporary.get(user)
                            .getStartPeriod()
                            .plusMinutes(Long.parseLong(messageText));
                    scheduleTemporary.get(user).setStartPeriod(startPeriod);

                    return configureMessage(user, chatId, SubState.ADD_END_HOUR_SCHEDULE_SETTINGS, "Выберите час окончания периода.", KeyboardService.HOURS_WITH_CANCEL_KEYBOARD);
                }
                case ADD_END_HOUR_SCHEDULE_SETTINGS -> {
                    if (!StringUtils.hasText(messageText) || !typeService.isInteger(messageText)) {
                        return MessageService.configureMessage(chatId, MessageText.INCORRECT_VALUE_TRY_AGAIN, KeyboardService.HOURS_WITH_CANCEL_KEYBOARD);
                    }

                    LocalTime endPeriod = LocalTime.of(Integer.parseInt(messageText), 0);
                    scheduleTemporary.get(user).setEndPeriod(endPeriod);

                    return configureMessage(user, chatId, SubState.ADD_END_MINUTE_SCHEDULE_SETTINGS, "Выберите минуту окончания периода.", KeyboardService.MINUTES_WITH_CANCEL_KEYBOARD);
                }
                case ADD_END_MINUTE_SCHEDULE_SETTINGS -> {
                    if (!StringUtils.hasText(messageText) || !typeService.isInteger(messageText)) {
                        return MessageService.configureMessage(chatId, MessageText.INCORRECT_VALUE_TRY_AGAIN, KeyboardService.MINUTES_WITH_CANCEL_KEYBOARD);
                    }

                    LocalTime endPeriod = scheduleTemporary.get(user)
                            .getEndPeriod()
                            .plusMinutes(Long.parseLong(messageText));
                    scheduleTemporary.get(user).setEndPeriod(endPeriod);

                    return configureMessage(user, chatId, SubState.ADD_PRICE_SCHEDULE_SETTINGS, "Введите стоимость входа:", KeyboardService.FREE_WITH_CANCEL_KEYBOARD);
                }
                case ADD_PRICE_SCHEDULE_SETTINGS -> {
                    if (!StringUtils.hasText(messageText) || (button != Button.FREE && !typeService.isInteger(messageText))) {
                        return MessageService.configureMessage(chatId, MessageText.INCORRECT_VALUE_TRY_AGAIN, KeyboardService.MINUTES_WITH_CANCEL_KEYBOARD);
                    }

                    ScheduleDto schedule = scheduleTemporary.get(user);
                    Integer price = button == Button.FREE ? 0 : Integer.parseInt(messageText);
                    schedule.setPrice(price);

                    Set<ScheduleDto> schedules;
                    if (schedule.isWeekdays()) {
                        schedules = DayWeek.WEEKDAYS_LIST.stream()
                                .map(dayWeek -> new ScheduleDto(dayWeek, schedule))
                                .collect(Collectors.toSet());
                    } else if (schedule.isWeekends()) {
                        schedules = DayWeek.WEEKENDS_LIST.stream()
                                .map(dayWeek -> new ScheduleDto(dayWeek, schedule))
                                .collect(Collectors.toSet());
                    } else {
                        schedules = Set.of(schedule);
                    }

                    schedules = schedules.stream()
                            .filter(scheduleDto -> scheduleService.checkTimePeriodAvailability(
                                    tavern.getSchedules(),
                                    scheduleDto.getDayWeek(),
                                    scheduleDto.getStartPeriod(),
                                    scheduleDto.getEndPeriod())
                            )
                            .collect(Collectors.toSet());

                    Set<ScheduleEntity> scheduleEntities = schedules.stream()
                            .map(scheduleMapper::toEntity)
                            .peek(scheduleEntity -> scheduleEntity.setTavern(tavern))
                            .collect(Collectors.toSet());

                    scheduleService.save(scheduleEntities);
                    tavern.getSchedules().addAll(scheduleEntities);

                    userService.updateSubState(user, subState.getParentSubState());
                }
                case ADD_LABEL_TABLE_SETTINGS -> {
                    tableTemporary.remove(user);

                    if (!StringUtils.hasText(messageText)) {
                        return MessageService.configureMessage(chatId, MessageText.ENTER_EMPTY_VALUE, KeyboardService.DAY_WEEK_WITH_PERIOD_KEYBOARD);
                    }

                    TableEntity table = new TableEntity();
                    table.setTavern(user.getTavern());
                    table.setLabel(messageText);

                    tableTemporary.put(user, table);

                    return configureMessage(user, chatId, SubState.ADD_NUMBER_SEATS_TABLE_SETTINGS, "Введите кол-во мест:");
                }
                case ADD_NUMBER_SEATS_TABLE_SETTINGS -> {
                    if (!StringUtils.hasText(messageText) || !typeService.isInteger(messageText) || Integer.parseInt(messageText) < 1) {
                        return MessageService.configureMessage(chatId, MessageText.INCORRECT_VALUE_TRY_AGAIN, KeyboardService.MINUTES_WITH_CANCEL_KEYBOARD);
                    }

                    userService.updateSubState(user, subState.getParentSubState());

                    TableEntity table = tableTemporary.get(user);
                    table.setNumberSeats(Integer.parseInt(messageText));

                    final String label = table.getLabel();
                    boolean isExists = tavern.getTables().stream()
                            .anyMatch(tableEntity -> label.equalsIgnoreCase(tableEntity.getLabel()));

                    if (isExists) {
                        return MessageService.configureMessage(chatId, "Стол с указанным маркером уже существует.\n\n" + fillTables(tavern.getTables()), KeyboardService.TABLE_KEYBOARD);
                    }

                    table = tableService.save(table);
                    tavern.getTables().add(table);
                }
                case DELETE_TABLE_SETTINGS -> {
                    final Long tableId = messageService.parseId(messageText);
                    if (!StringUtils.hasText(messageText) || tableId == null) {
                        userService.updateSubState(user, user.getSubState().getParentSubState());

                        return MessageService.configureMessage(chatId, "Вы ничего не выбрали! Операция отменяется.", KeyboardService.TABLE_KEYBOARD);
                    }

                    Set<TableEntity> tables = tavern.getTables();

                    tables.removeIf(table -> table.getId().equals(tableId));

                    tableService.save(tables);

                    userService.updateSubState(user, user.getSubState().getParentSubState());
                }
            }
        }

        // отрисовка меню
        return switch (user.getSubState()) {
            case VIEW_SETTINGS -> MessageService.configureMessage(chatId, "Открываем все настройки...", KeyboardService.SETTINGS_KEYBOARD);

            case VIEW_GENERAL_SETTINGS -> MessageService.configureMessage(chatId, fillGeneralInfo(tavern), KeyboardService.GENERAL_KEYBOARD);
            case VIEW_GENERAL_SETTINGS_TAVERN_NAME -> MessageService.configureMessage(chatId, fillTavernInfo(tavern.getName()), KeyboardService.TAVERN_NAME_KEYBOARD);
            case VIEW_GENERAL_SETTINGS_TAVERN_CONTACTS -> MessageService.configureMessage(chatId, fillContactInfo(tavern.getContacts()), KeyboardService.TAVERN_CONTACTS_KEYBOARD);
            case VIEW_GENERAL_SETTINGS_TAVERN_ADDRESS -> MessageService.configureMessage(chatId, fillAddressInfo(tavern.getAddress()), KeyboardService.TAVERN_ADDRESS_KEYBOARD);
            case VIEW_GENERAL_SETTINGS_CATEGORIES -> MessageService.configureMessage(chatId, fillCategory(tavern.getCategory()), KeyboardService.CATEGORIES_KEYBOARD);

            case VIEW_PROFILE_SETTINGS -> MessageService.configureMessage(chatId, fillProfileInfo(user), KeyboardService.PROFILE_KEYBOARD);
            case VIEW_PROFILE_SETTINGS_USER_NAME -> MessageService.configureMessage(chatId, fillUserInfo(user.getName()), KeyboardService.PROFILE_NAME_KEYBOARD);
            case VIEW_PROFILE_SETTINGS_USER_CONTACTS -> MessageService.configureMessage(chatId, fillContactInfo(user.getContacts()), KeyboardService.USER_CONTACTS_KEYBOARD);

            case VIEW_EMPLOYEE_SETTINGS -> MessageService.configureMessage(chatId, fillEmployeeInfo(tavern.getEmployees()), KeyboardService.EMPLOYEE_KEYBOARD);

            case VIEW_SCHEDULE_SETTINGS -> MessageService.configureMessage(chatId, scheduleService.fillSchedulesInfo(tavern.getSchedules()), KeyboardService.SCHEDULE_KEYBOARD);

            case VIEW_TABLE_SETTINGS -> MessageService.configureMessage(chatId, fillTables(tavern.getTables()), KeyboardService.TABLE_KEYBOARD);


            default -> new SendMessage();
        };
    }

    public String fillTables(Set<TableEntity> tables) {
        if (CollectionUtils.isEmpty(tables)) {
            return "Столы не добавлены.";
        }

        return "Столы:" + System.lineSeparator() +
                tables.stream()
                        .sorted(Comparator.comparing(TableEntity::getLabel, Comparator.naturalOrder()))
                        .map(table -> "<b>" + table.getLabel() + "</b>: мест - " + table.getNumberSeats())
                        .collect(Collectors.joining(System.lineSeparator()));
    }

    public String fillCategory(Category category) {
        return Optional.ofNullable(category)
                .map(Category::getDescription)
                .map(description -> "Категория: <b>" + description + "</b>")
                .orElse("Категория не выбрана.");
    }

    public String fillEmployeeInfo(Set<UserEntity> employees) {
        return employees.stream()
                .sorted(Comparator.comparing(UserEntity::getId))
                .map(employee -> String.format(
                        "ID: <b>%d</b>, Имя: <b>%s</b>, %s",
                        employee.getId(),
                        employee.getName(),
                        fillRoleInfo(employee.getRoles())
                ))
                .collect(Collectors.joining(System.lineSeparator()));
    }

    private SendMessage configureMessage(UserEntity user, Long chatId, SubState subState, String text) {
        userService.updateSubState(user, subState);
        return MessageService.configureMessage(chatId, text, KeyboardService.CANCEL_KEYBOARD);
    }

    private SendMessage configureMessage(UserEntity user, Long chatId, SubState subState, String text, ReplyKeyboardMarkup keyboard) {
        SendMessage sendMessage = configureMessage(user, chatId, subState, text);
        sendMessage.setReplyMarkup(keyboard);
        return sendMessage;
    }

    private SendMessage configureDeleteContacts(UserEntity user,
                                                SubState subState,
                                                Long chatId,
                                                ReplyKeyboardMarkup keyboard) {
        Set<ContactEntity> contacts = user.getTavern().getContacts();
        if (CollectionUtils.isEmpty(contacts)) {
            return MessageService.configureMessage(chatId, "Нечего удалять.", keyboard);
        }

        userService.updateSubState(user, subState);

        ReplyKeyboardMarkup contactKeyboard = new ReplyKeyboardMarkup();
        List<KeyboardRow> rows = new ArrayList<>();

        contacts.forEach(contact ->
                rows.add(new KeyboardRow(List.of(new KeyboardButton(contact.getValue()))))
        );

        rows.add(new KeyboardRow(List.of(new KeyboardButton(Button.CANCEL.getText()))));

        contactKeyboard.setKeyboard(rows);
        contactKeyboard.setResizeKeyboard(true);

        return MessageService.configureMessage(chatId, "Выберите номер телефона, который хотите удалить.", contactKeyboard);
    }

    private SendMessage configureDeleteEmployees(UserEntity user,
                                                 Long chatId) {
        Set<UserEntity> employees = user.getTavern().getEmployees();
        if (CollectionUtils.isEmpty(employees)) {
            return MessageService.configureMessage(chatId, "Некого удалять.", KeyboardService.EMPLOYEE_KEYBOARD);
        }

        userService.updateSubState(user, SubState.DELETE_EMPLOYEE_SETTINGS);

        ReplyKeyboardMarkup employeesKeyboard = new ReplyKeyboardMarkup();
        List<KeyboardRow> rows = new ArrayList<>();
        employees.forEach(employee ->
                rows.add(new KeyboardRow(List.of(new KeyboardButton("ID: " + employee.getId() + " " + employee.getName()))))
        );

        rows.add(new KeyboardRow(List.of(new KeyboardButton(Button.CANCEL.getText()))));

        employeesKeyboard.setKeyboard(rows);
        employeesKeyboard.setResizeKeyboard(true);

        return MessageService.configureMessage(chatId, "Выберите сотрудника, которого хотите удалить.", employeesKeyboard);
    }

    private SendMessage configureDeleteTables(UserEntity user,
                                              Long chatId) {
        Set<TableEntity> tables = user.getTavern().getTables();
        if (CollectionUtils.isEmpty(tables)) {
            return MessageService.configureMessage(chatId, "Нечего удалять.", KeyboardService.TABLE_KEYBOARD);
        }

        userService.updateSubState(user, SubState.DELETE_TABLE_SETTINGS);

        ReplyKeyboardMarkup tablesKeyboard = new ReplyKeyboardMarkup();
        List<KeyboardRow> rows = new ArrayList<>();
        tables.forEach(table ->
                rows.add(new KeyboardRow(List.of(new KeyboardButton("ID: " + table.getId() + " " + table.getLabel()))))
        );

        rows.add(new KeyboardRow(List.of(new KeyboardButton(Button.CANCEL.getText()))));

        tablesKeyboard.setKeyboard(rows);
        tablesKeyboard.setResizeKeyboard(true);

        return MessageService.configureMessage(chatId, "Выберите стол, который хотите удалить.", tablesKeyboard);
    }

    private SendMessage configureDeleteSchedule(UserEntity user,
                                                Long chatId) {
        Set<ScheduleEntity> schedules = user.getTavern().getSchedules();
        if (CollectionUtils.isEmpty(schedules)) {
            return MessageService.configureMessage(chatId, "Нечего удалять.", KeyboardService.SCHEDULE_KEYBOARD);
        }

        userService.updateSubState(user, SubState.DELETE_SCHEDULE_SETTINGS);

        ReplyKeyboardMarkup employeesKeyboard = new ReplyKeyboardMarkup();
        List<KeyboardRow> rows = new ArrayList<>();
        Map<DayWeek, List<ScheduleEntity>> groupingSchedules = schedules.stream()
                .collect(Collectors.groupingBy(ScheduleEntity::getDayWeek));

        for (DayWeek dayWeek : DayWeek.SORTED_DAY_WEEK_LIST) {
            List<ScheduleEntity> schedulesByDayWeek = groupingSchedules.get(dayWeek);
            if (CollectionUtils.isEmpty(schedulesByDayWeek)) {
                continue;
            }

            schedulesByDayWeek.stream()
                    .sorted(Comparator.comparing(ScheduleEntity::getStartPeriod))
                    .forEach(schedule ->
                            rows.add(new KeyboardRow(List.of(new KeyboardButton(
                                    String.format(
                                            "ID: %s %s %s - %s %sр.",
                                            schedule.getId(),
                                            schedule.getDayWeek().getFullName(),
                                            schedule.getStartPeriod(),
                                            schedule.getEndPeriod(),
                                            schedule.getPrice()
                                    )
                            ))))
                    );
        }

        rows.add(new KeyboardRow(List.of(new KeyboardButton(Button.CANCEL.getText()))));

        employeesKeyboard.setKeyboard(rows);
        employeesKeyboard.setResizeKeyboard(true);

        return MessageService.configureMessage(chatId, "Выберите запись, которую хотите удалить.", employeesKeyboard);
    }

    public String fillProfileInfo(UserEntity user) {
        return fillUserInfo(user.getName()) + System.lineSeparator() + System.lineSeparator() +
                fillRoleInfo(user.getRoles()) + System.lineSeparator() + System.lineSeparator() +
                fillContactInfo(user.getContacts()) + System.lineSeparator() + System.lineSeparator();
    }

    public String fillGeneralInfo(TavernEntity tavern) {
        return fillTavernInfo(tavern.getName()) + System.lineSeparator() + System.lineSeparator() +
                fillCategory(tavern.getCategory()) + System.lineSeparator() + System.lineSeparator() +
                fillContactInfo(tavern.getContacts()) + System.lineSeparator() + System.lineSeparator() +
                fillAddressInfo(tavern.getAddress());
    }

    public String fillTavernInfo(String name) {
        return "Название вашего заведения: <b>" + name + "</b>";
    }

    public String fillUserInfo(String name) {
        return "Ваше имя: <b>" + name + "</b>";
    }

    public String fillRoleInfo(Set<Role> roles) {
        String rolesString = roles.stream()
                .map(Role::getDescription)
                .collect(Collectors.joining(System.lineSeparator()));

        return "Роль: <b>" + rolesString + "</b>";
    }

    public String fillAddressInfo(AddressEntity address) {
        if (address == null) {
            return "Информация об адресе отсутствует.";
        }

        return String.format(
                "Адресная информация:%s<b>Город</b> - %s%s<b>Адрес</b> - %s",
                System.lineSeparator(),
                address.getCity().getDescription(),
                System.lineSeparator(),
                address.getStreet()
        );
    }

    public String fillContactInfo(Set<ContactEntity> contacts) {
        if (CollectionUtils.isEmpty(contacts)) {
            return "Контактная информация отсутствует.";
        }

        return "Контактная информация:" + System.lineSeparator() + contacts.stream()
                .map(contact -> contact.getType().getDescription() + " - <b>" + contact.getValue() + "</b>")
                .collect(Collectors.joining(System.lineSeparator()));
    }
}
