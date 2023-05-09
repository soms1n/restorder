package ru.privetdruk.restorder.handler.client;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import reactor.core.scheduler.Schedulers;
import ru.privetdruk.restorder.handler.MessageHandler;
import ru.privetdruk.restorder.model.consts.Constant;
import ru.privetdruk.restorder.model.consts.MessageText;
import ru.privetdruk.restorder.model.dto.ReserveDto;
import ru.privetdruk.restorder.model.dto.ValidateTavernResult;
import ru.privetdruk.restorder.model.entity.*;
import ru.privetdruk.restorder.model.enums.*;
import ru.privetdruk.restorder.service.*;
import ru.privetdruk.restorder.service.util.StringService;
import ru.privetdruk.restorder.service.util.ValidationService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;

import static org.springframework.util.CollectionUtils.isEmpty;
import static ru.privetdruk.restorder.model.consts.Constant.DD_MM_YYYY_FORMATTER;
import static ru.privetdruk.restorder.model.consts.Constant.HH_MM_FORMATTER;
import static ru.privetdruk.restorder.model.consts.MessageText.NOTIFY_USER_BLOCK;
import static ru.privetdruk.restorder.model.consts.MessageText.NOTIFY_USER_RESERVE_CANCELLED;
import static ru.privetdruk.restorder.service.KeyboardService.RESERVE_LIST_KEYBOARD;
import static ru.privetdruk.restorder.service.KeyboardService.YES_NO_KEYBOARD;
import static ru.privetdruk.restorder.service.MessageService.configureMessage;

@Component
@RequiredArgsConstructor
public class ReserveHandler implements MessageHandler {
    private final BlacklistService blacklistService;
    private final BlacklistSettingService blacklistSettingService;
    private final ContactService contactService;
    private final MessageService messageService;
    private final MainMenuHandler mainMenuHandler;
    private final ReserveService reserveService;
    private final StringService stringService;
    private final TavernService tavernService;
    private final TelegramApiService telegramApiService;
    private final UserService userService;
    private final ValidationService validationService;
    private final VisitingService visitingService;

    private final Map<UserEntity, ReserveDto> completionReservesCache = new HashMap<>();
    private final Map<UserEntity, ReserveEntity> addReservesCache = new HashMap<>();

    @Override
    public SendMessage handle(UserEntity user, Message message, CallbackQuery callback) {
        SubState subState = user.getSubState();
        String messageText = message.getText();
        Button button = Button.fromText(messageText)
                .orElse(Button.NOTHING);
        Long chatId = message.getChatId();

        if (!user.getTavern().isValid()) {
            TavernEntity tavern = tavernService.findWithDataWithoutEmployees(user.getTavern());

            ValidateTavernResult validate = validationService.validate(tavern);

            tavern.setValid(validate.isValid());
            tavernService.save(tavern);

            if (!validate.isValid()) {
                return configureMessage(
                        chatId,
                        "Чтобы воспользоваться бронированием, выполните настройку заведения:"
                                + System.lineSeparator()
                                + validate.printMessages(),
                        KeyboardService.CLIENT_MAIN_MENU
                );
            }
        }

        // обработка функциональных клавиш
        switch (button) {
            case BACK, CANCEL, NO -> {
                switch (subState) {
                    case ADD_RESERVE_CHOICE_DATE, ADD_RESERVE_CHOICE_TIME, ADD_RESERVE_CHOICE_PERSONS, ADD_RESERVE_CHOICE_TABLE, ADD_RESERVE_INFO -> {
                        return returnToMainMenu(user, message, callback);
                    }
                }

                if (subState != SubState.BLOCK_CLIENT_RESERVE && subState != SubState.CONFIRM_CLIENT_RESERVE) {
                    userService.updateSubState(user, subState.getParentSubState());
                }
            }
            case RETURN_MAIN_MENU -> {
                return returnToMainMenu(user, message, callback);
            }
            case CANCEL_RESERVE -> {
                if (user.getSubState() == SubState.VIEW_RESERVE_LIST) {
                    return configureDeleteReserve(user, chatId);
                }
            }
            case RESERVE -> {
                if (user.getSubState() == SubState.VIEW_RESERVE_LIST) {
                    return configureAddReserve(user, chatId);
                }
            }
        }

        subState = user.getSubState();

        // обновление состояния
        if (button != Button.CANCEL && (button != Button.NO
                || (subState == SubState.CONFIRM_CLIENT_RESERVE || subState == SubState.BLOCK_CLIENT_RESERVE))) {
            switch (subState) {
                case VIEW_MAIN_MENU -> {
                    if (button == Button.RESERVE_LIST) {
                        user.setState(State.RESERVE);
                        userService.updateSubState(user, SubState.VIEW_RESERVE_LIST);
                    } else if (button == Button.RESERVE) {
                        return configureAddReserve(user, chatId);
                    }
                }
                case DELETE_RESERVE_CHOICE_DATE -> {
                    LocalDate date;
                    try {
                        date = LocalDate.parse(messageText, DD_MM_YYYY_FORMATTER);
                    } catch (DateTimeParseException e) {
                        userService.updateSubState(user, user.getSubState().getParentSubState());
                        return configureMessage(
                                chatId,
                                "Вы ввели некорректное значение. Операция отменяется.",
                                RESERVE_LIST_KEYBOARD
                        );
                    }

                    List<ReserveEntity> reserves = reserveService.findActiveByTavernWithTableUser(user.getTavern(), date).stream()
                            .sorted(Comparator.comparing(ReserveEntity::getTime))
                            .toList();

                    if (isEmpty(reserves)) {
                        userService.updateSubState(user, user.getSubState().getParentSubState());
                        return configureMessage(chatId, "У Вас нет бронирований за выбранную дату.", RESERVE_LIST_KEYBOARD);
                    }

                    userService.updateSubState(user, SubState.DELETE_RESERVE_CHOICE_TABLE);

                    ReserveDto reserveDto = ReserveDto.builder()
                            .date(date)
                            .build();
                    completionReservesCache.put(user, reserveDto);

                    ReplyKeyboardMarkup reservesDatesKeyboard = new ReplyKeyboardMarkup();
                    List<KeyboardRow> rows = new ArrayList<>();

                    rows.add(new KeyboardRow(List.of(new KeyboardButton(Button.PICK_ALL.getText()))));

                    reserves.forEach(reserve ->
                            rows.add(new KeyboardRow(List.of(new KeyboardButton(String.format(
                                    "%s %s %s [%s]",
                                    reserve.getTime(),
                                    reserve.getTable().getLabel(),
                                    reserve.getUser() == null ? reserve.getName() : reserve.getUser().getName(),
                                    reserve.getId()
                            )))))
                    );

                    rows.add(new KeyboardRow(List.of(new KeyboardButton(Button.CANCEL.getText()))));

                    reservesDatesKeyboard.setKeyboard(rows);
                    reservesDatesKeyboard.setResizeKeyboard(true);

                    return configureMessage(chatId, "Выберите бронирование для завершения.", reservesDatesKeyboard);
                }
                case DELETE_RESERVE_CHOICE_TABLE -> {
                    ReserveDto reserve = completionReservesCache.get(user);

                    if (button == Button.PICK_ALL) {
                        userService.updateSubState(user, user.getSubState().getParentSubState());

                        if (reserve.getDate() != null) {
                            List<ReserveEntity> reserves = reserveService.findActiveByTavern(user.getTavern(), reserve.getDate());

                            reserveService.updateStatus(reserves, ReserveStatus.COMPLETED);

                            completionReservesCache.remove(user);

                            String textMessage = "Все бронирования были завершены."
                                    + System.lineSeparator()
                                    + System.lineSeparator()
                                    + fillReservesList(user.getTavern());

                            return configureMessage(chatId, textMessage, RESERVE_LIST_KEYBOARD);
                        }
                    }

                    Long reserveId = messageService.parseId(messageText);
                    if (reserveId == null) {
                        userService.updateSubState(user, user.getSubState().getParentSubState());
                    } else {
                        reserve.setId(reserveId);

                        userService.updateSubState(user, SubState.CONFIRM_CLIENT_RESERVE);

                        return configureMessage(chatId, "Клиент пришёл?", YES_NO_KEYBOARD);
                    }
                }
                case CONFIRM_CLIENT_RESERVE -> {
                    ReserveDto reserveCache = completionReservesCache.get(user);

                    if (reserveCache.getId() == null) {
                        userService.updateSubState(user, user.getSubState().getParentSubState());
                        break;
                    }

                    ReserveEntity reserve = reserveService.findActiveByIdWithTableUserTavern(reserveCache.getId(), user.getTavern());
                    UserEntity reserveUser = reserve.getUser();

                    if (button == Button.NO) {
                        BlacklistSettingEntity blacklistSetting = blacklistSettingService.findByTavern(user.getTavern())
                                .orElse(null);

                        if (blacklistSetting != null && blacklistSetting.enabled()) {
                            VisitingEntity visiting = visitingService.findByPhoneNumberAndTavern(reserve.getPhoneNumber(), user.getTavern())
                                    .orElse(null);

                            if (visiting == null && reserveUser == null) {
                                visiting = VisitingEntity.builder()
                                        .phoneNumber(reserve.getPhoneNumber())
                                        .tavern(user.getTavern())
                                        .times(1)
                                        .build();
                            } else if (visiting == null) {
                                visiting = VisitingEntity.builder()
                                        .phoneNumber(reserve.getPhoneNumber())
                                        .user(reserveUser)
                                        .tavern(user.getTavern())
                                        .times(1)
                                        .build();
                            } else {
                                if (reserveUser != null && visiting.getUser() == null) {
                                    visiting.setUser(reserveUser);
                                }

                                visiting.setTimes(visiting.getTimes() + 1);
                            }

                            visitingService.save(visiting);

                            BlacklistEntity blacklist = blacklistService.findActiveByPhoneNumber(user.getTavern(), visiting.getPhoneNumber());

                            if (blacklist == null && visiting.getTimes() >= blacklistSetting.getTimes()) {
                                if (visiting.getUser() != null) {
                                    reserveCache.setUser(reserveUser);
                                    reserveCache.setReserve(reserve);
                                    reserveCache.setBlockDays(blacklistSetting.getDays());
                                    reserveCache.setPhoneNumber(visiting.getPhoneNumber());
                                }

                                userService.updateSubState(user, SubState.BLOCK_CLIENT_RESERVE);

                                String text = "Клиент не пришел по бронированию уже "
                                        + visiting.getTimes() + " " + stringService.declensionTimes(visiting.getTimes())
                                        + ". Заблокировать его?";
                                return configureMessage(chatId, text, YES_NO_KEYBOARD);
                            }
                        }
                    }

                    return completionReserve(user, chatId, reserve);
                }
                case BLOCK_CLIENT_RESERVE -> {
                    ReserveDto reserveCache = completionReservesCache.get(user);

                    if (button == Button.NOTHING) {
                        break;
                    }

                    if (button == Button.YES) {
                        UserEntity reserveUser = reserveCache.getUser();
                        LocalDateTime unlockDate = reserveCache.getBlockDays() > 0 ? LocalDateTime.now().plusDays(reserveCache.getBlockDays()) : LocalDateTime.of(9999, 12, 12, 0, 0, 0, 0);

                        BlacklistEntity blacklist = BlacklistEntity.builder()
                                .tavern(user.getTavern())
                                .user(reserveUser)
                                .phoneNumber(reserveCache.getPhoneNumber())
                                .unlockDate(unlockDate)
                                .reason(Button.DOESNT_COME.getText())
                                .build();

                        if (reserveUser != null) {
                            telegramApiService.sendMessage(
                                            reserveUser.getTelegramId(),
                                            NOTIFY_USER_BLOCK + user.getTavern().getName(),
                                            false
                                    )
                                    .subscribeOn(Schedulers.boundedElastic())
                                    .subscribe();
                        }

                        blacklistService.save(blacklist);
                    }

                    return completionReserve(user, chatId, reserveCache.getReserve());
                }
                case ADD_RESERVE_CHOICE_DATE -> {
                    LocalDate date;
                    switch (button) {
                        case TODAY -> date = LocalDate.now();
                        case TOMORROW -> date = LocalDate.now().plusDays(1);
                        default -> {
                            try {
                                date = LocalDate.parse(messageText, Constant.DD_MM_YYYY_WITHOUT_DOT_FORMATTER);
                            } catch (DateTimeParseException exception) {
                                return configureMessage(
                                        chatId,
                                        "Дата не соответствует формату. Повторите попытку:",
                                        KeyboardService.TODAY_TOMORROW_CANCEL_KEYBOARD
                                );
                            }
                        }
                    }

                    if (date.isBefore(LocalDate.now())) {
                        return configureMessage(
                                chatId,
                                "Дата бронирования должна быть больше, либо равна текущей дате. Повторите попытку:",
                                KeyboardService.TODAY_TOMORROW_CANCEL_KEYBOARD
                        );
                    }

                    ReserveEntity newReserve = new ReserveEntity();
                    newReserve.setDate(date);

                    addReservesCache.put(user, newReserve);

                    userService.updateSubState(user, SubState.ADD_RESERVE_CHOICE_TABLE);

                    return configureChoiceTable(user, chatId);
                }
                case ADD_RESERVE_CHOICE_TABLE -> {
                    TavernEntity tavern = tavernService.findWithTables(user.getTavern());

                    String label = messageText.split(" ")[0];
                    TableEntity reserveTable = tavern.getTables().stream()
                            .filter(table -> table.getLabel().equals(label))
                            .findFirst()
                            .orElse(null);

                    if (reserveTable == null) {
                        return configureChoiceTable(user, chatId);
                    }

                    addReservesCache.get(user)
                            .setTable(reserveTable);

                    userService.updateSubState(user, SubState.ADD_RESERVE_CHOICE_TIME);

                    return configureChoiceTime(chatId);
                }

                case ADD_RESERVE_CHOICE_TIME -> {
                    if (messageText == null || (button != Button.NOW && messageText.length() != 4)) {
                        return configureChoiceTime(chatId);
                    }

                    try {
                        LocalTime now = LocalTime.now();

                        LocalTime time = button == Button.NOW ? now : LocalTime.parse(messageText, Constant.HH_MM_WITHOUT_DOT_FORMATTER);

                        ReserveEntity reserve = addReservesCache.get(user);

                        if (reserve.getDate().isEqual(LocalDate.now()) && time.isBefore(now)) {
                            return configureMessage(
                                    chatId,
                                    "Время бронирования должно быть больше, либо равно текущему времени.",
                                    KeyboardService.TODAY_TOMORROW_CANCEL_KEYBOARD
                            );
                        }

                        reserve.setTime(time);

                        userService.updateSubState(user, SubState.ADD_RESERVE_CHOICE_PERSONS);

                        return configureChoicePersons(chatId);
                    } catch (NumberFormatException e) {
                        return configureChoiceTime(chatId);
                    }
                }
                case ADD_RESERVE_CHOICE_PERSONS -> {
                    int numberPeople;

                    if (button.getNumber() != null) {
                        numberPeople = button.getNumber();
                    } else {
                        try {
                            numberPeople = Integer.parseInt(messageText);
                        } catch (NumberFormatException e) {
                            return configureChoicePersons(chatId);
                        }
                    }

                    addReservesCache.get(user)
                            .setNumberPeople(numberPeople);

                    userService.updateSubState(user, SubState.ADD_RESERVE_CHOICE_NAME);

                    return configureMessage(chatId, "Введите имя:", KeyboardService.CANCEL_KEYBOARD);
                }
                case ADD_RESERVE_CHOICE_NAME -> {
                    addReservesCache.get(user)
                            .setName(messageText);

                    userService.updateSubState(user, SubState.ADD_RESERVE_CHOICE_PHONE);

                    return configureMessage(chatId, MessageText.ENTER_PHONE_NUMBER, KeyboardService.WITHOUT_PHONE_KEYBOARD);
                }
                case ADD_RESERVE_CHOICE_PHONE -> {
                    if (button != Button.WITHOUT_PHONE) {
                        String phoneNumber = contactService.preparePhoneNumber(messageText);

                        if (validationService.isNotValidPhone(phoneNumber)) {
                            return configureMessage(
                                    chatId,
                                    MessageText.INCORRECT_PHONE_NUMBER,
                                    KeyboardService.SHARE_PHONE_KEYBOARD
                            );
                        }

                        ReserveEntity reserve = addReservesCache.get(user);
                        reserve.setPhoneNumber(phoneNumber);
                        reserve.setUser(userService.findByPhoneNumber(phoneNumber));
                    }

                    userService.updateSubState(user, SubState.ADD_RESERVE_INFO);

                    return configureMessage(chatId, fillReserveInfo(addReservesCache.get(user)), KeyboardService.APPROVE_KEYBOARD);
                }
                case ADD_RESERVE_INFO -> {
                    user.setState(State.MAIN_MENU);
                    userService.updateSubState(user, SubState.VIEW_MAIN_MENU);

                    if (button == Button.ACCEPT) {
                        ReserveEntity reserve = addReservesCache.get(user);
                        reserveService.save(reserve);
                        addReservesCache.remove(user);

                        return configureMessage(chatId, "Столик забронирован.", KeyboardService.CLIENT_MAIN_MENU);
                    }

                    return configureMessage(chatId, "Столик не удалось забронировать.", KeyboardService.CLIENT_MAIN_MENU);
                }
            }
        }

        // отрисовка меню
        return switch (user.getSubState()) {
            case VIEW_RESERVE_LIST -> configureMessage(chatId, fillReservesList(user.getTavern()), RESERVE_LIST_KEYBOARD);

            default -> new SendMessage();
        }

                ;
    }

    private SendMessage completionReserve(UserEntity user, Long chatId, ReserveEntity reserve) {
        UserEntity reserveUser = reserve.getUser();

        reserveService.updateStatus(reserve, ReserveStatus.COMPLETED);

        if (reserveUser != null) {
            telegramApiService.sendMessage(
                            reserveUser.getTelegramId(),
                            String.format(
                                    NOTIFY_USER_RESERVE_CANCELLED,
                                    reserve.getDate().format(DD_MM_YYYY_FORMATTER),
                                    reserve.getTime().format(HH_MM_FORMATTER),
                                    user.getTavern().getName()
                            ),
                            false
                    )
                    .subscribeOn(Schedulers.boundedElastic())
                    .subscribe();
        }

        String textMessage = "Выбранное бронирование завершено."
                + System.lineSeparator()
                + System.lineSeparator()
                + fillReservesList(user.getTavern());

        return configureMessage(chatId, textMessage, RESERVE_LIST_KEYBOARD);
    }

    private SendMessage configureAddReserve(UserEntity user, Long chatId) {
        userService.update(user, State.RESERVE, SubState.ADD_RESERVE_CHOICE_DATE);

        return configureMessage(
                chatId,
                "Введите дату бронирования в формате ДДММГГГГ <i>(пример: 24052022)</i>:",
                KeyboardService.TODAY_TOMORROW_CANCEL_KEYBOARD
        );
    }

    private String fillReserveInfo(ReserveEntity reserve) {
        return String.format(
                """
                        <b>Информация о бронировании</b>
                        Дата: <i>%s</i>
                        Время: <i>%s</i>
                        Стол: <i>%s</i>
                        Кол-во персон: <i>%s</i>
                        Имя: <i>%s</i>
                        Телефон: <i>%s</i>""",
                reserve.getDate().format(DD_MM_YYYY_FORMATTER),
                reserve.getTime().format(HH_MM_FORMATTER),
                reserve.getTable().getLabel(),
                reserve.getNumberPeople(),
                reserve.getName(),
                Optional.ofNullable(reserve.getPhoneNumber())
                        .orElse("не указан")
        );
    }

    private SendMessage configureChoiceTime(Long chatId) {
        return configureMessage(
                chatId,
                "Введите время бронирования <i>(в формате ЧЧММ, пример: 1830 или 0215)</i>:",
                KeyboardService.RESERVE_CHOICE_TIME_KEYBOARD
        );
    }

    private SendMessage configureChoicePersons(Long chatId) {
        return configureMessage(chatId, "Введите или выберите кол-во персон:", KeyboardService.NUMBERS_CANCEL_KEYBOARD);
    }

    private SendMessage configureChoiceTable(UserEntity user, Long chatId) {
        LocalDate date = addReservesCache.get(user)
                .getDate();

        TavernEntity tavern = tavernService.findWithTables(user.getTavern());

        ReplyKeyboardMarkup tablesKeyboard = new ReplyKeyboardMarkup();
        List<KeyboardRow> rows = new ArrayList<>();
        tavern.getTables().stream()
                .sorted(Comparator.comparing(TableEntity::getNumberSeats))
                .forEach(table -> {
                            String reserveTimes = reserveService.findActiveByTable(table).stream()
                                    .filter(reserve -> date.isEqual(reserve.getDate()))
                                    .map(ReserveEntity::getTime)
                                    .sorted(Comparator.naturalOrder())
                                    .map(time -> time.format(HH_MM_FORMATTER))
                                    .collect(Collectors.joining(","));

                            String foundReserve = "занято с " + reserveTimes;

                            rows.add(new KeyboardRow(List.of(new KeyboardButton(String.format(
                                    "%s на %s %s %s",
                                    table.getLabel(),
                                    table.getNumberSeats(),
                                    stringService.declensionWords(table.getNumberSeats(), StringService.SEATS_WORDS),
                                    StringUtils.hasLength(reserveTimes) ? foundReserve : ""
                            )))));
                        }
                );

        rows.add(new KeyboardRow(List.of(new KeyboardButton(Button.CANCEL.getText()))));

        tablesKeyboard.setKeyboard(rows);
        tablesKeyboard.setResizeKeyboard(true);

        return configureMessage(chatId, "Введите метку стола или выберите нужный в меню:", tablesKeyboard);
    }

    private SendMessage returnToMainMenu(UserEntity user, Message message, CallbackQuery callback) {
        user.setState(State.MAIN_MENU);
        userService.updateSubState(user, SubState.VIEW_MAIN_MENU);

        return mainMenuHandler.handle(user, message, callback);
    }

    private SendMessage configureDeleteReserve(UserEntity user, Long chatId) {
        List<LocalDate> reservesDates = reserveService.findActiveByTavernWithTable(user.getTavern()).stream()
                .map(ReserveEntity::getDate)
                .distinct()
                .sorted(LocalDate::compareTo)
                .toList();

        if (isEmpty(reservesDates)) {
            return configureMessage(chatId, "У Вас нет активных бронирований.", RESERVE_LIST_KEYBOARD);
        }

        userService.updateSubState(user, SubState.DELETE_RESERVE_CHOICE_DATE);

        ReplyKeyboardMarkup reservesDatesKeyboard = new ReplyKeyboardMarkup();
        List<KeyboardRow> rows = new ArrayList<>();
        reservesDates.forEach(date ->
                rows.add(new KeyboardRow(List.of(new KeyboardButton(date.format(DD_MM_YYYY_FORMATTER)))))
        );

        rows.add(new KeyboardRow(List.of(new KeyboardButton(Button.CANCEL.getText()))));

        reservesDatesKeyboard.setKeyboard(rows);
        reservesDatesKeyboard.setResizeKeyboard(true);

        return configureMessage(chatId, "Выберите дату, за которую хотите завершить бронирование.", reservesDatesKeyboard);
    }

    private String fillReservesList(TavernEntity tavern) {
        Map<LocalDate, List<ReserveEntity>> groupingReserves = reserveService.findActiveByTavernWithTableUser(tavern).stream()
                .collect(Collectors.groupingBy(ReserveEntity::getDate));

        if (isEmpty(groupingReserves)) {
            return "Список бронирований пуст.";
        }

        List<LocalDate> sortedDate = groupingReserves.keySet().stream()
                .sorted(LocalDate::compareTo)
                .toList();

        LocalDate tomorrow = LocalDate.now().plusDays(1);

        StringBuilder reservesDescription = new StringBuilder();
        for (LocalDate date : sortedDate) {
            String reservesList = groupingReserves.get(date).stream()
                    .sorted(Comparator.comparing(ReserveEntity::getTime, LocalTime::compareTo))
                    .map(reserve -> String.format(
                            "<i>%s</i> <b>%s</b> %s %s %s",
                            reserve.getTime().format(HH_MM_FORMATTER),
                            reserve.getTable().getLabel(),
                            reserve.getNumberPeople(),
                            reserve.getUser() == null ? Optional.ofNullable(reserve.getName())
                                    .orElse("")
                                    : reserve.getUser().getName(),
                            reserve.getUser() == null ? Optional.ofNullable(reserve.getPhoneNumber())
                                    .orElse("")
                                    : reserve.getUser().findContact(ContractType.MOBILE)
                                    .map(ContactEntity::getValue)
                                    .orElse("")
                    ))
                    .collect(Collectors.joining(System.lineSeparator()));

            reservesDescription
                    .append("<b>\uD83D\uDDD3 ")
                    .append(date.format(DD_MM_YYYY_FORMATTER))
                    .append("</b>")
                    .append(date.isEqual(LocalDate.now()) ? " <i>(сегодня)</i>" : "")
                    .append(date.isEqual(tomorrow) ? " <i>(завтра)</i>" : "")
                    .append(System.lineSeparator())
                    .append(reservesList)
                    .append(System.lineSeparator())
                    .append(System.lineSeparator());
        }

        return reservesDescription.toString();
    }
}
