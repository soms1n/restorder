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
import ru.privetdruk.restorder.model.dto.ValidateTavernResult;
import ru.privetdruk.restorder.model.entity.*;
import ru.privetdruk.restorder.model.enums.*;
import ru.privetdruk.restorder.service.*;
import ru.privetdruk.restorder.service.util.StringService;
import ru.privetdruk.restorder.service.util.ValidationService;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;

import static org.springframework.util.CollectionUtils.isEmpty;
import static ru.privetdruk.restorder.model.consts.MessageText.NOTIFY_USER_RESERVE_CANCELLED;
import static ru.privetdruk.restorder.service.MessageService.configureMessage;

@Component
@RequiredArgsConstructor
public class ReserveHandler implements MessageHandler {
    private final MessageService messageService;
    private final MainMenuHandler mainMenuHandler;
    private final ReserveService reserveService;
    private final StringService stringService;
    private final TavernService tavernService;
    private final TelegramApiService telegramApiService;
    private final UserService userService;
    private final ValidationService validationService;

    private final Map<UserEntity, LocalDate> deleteReservesTemporary = new HashMap<>();
    private final Map<UserEntity, ReserveEntity> addReservesTemporary = new HashMap<>();

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

                userService.updateSubState(user, subState.getParentSubState());
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

        // обновление состояния
        if (button != Button.CANCEL && button != Button.NO) {
            switch (user.getSubState()) {
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
                        date = LocalDate.parse(messageText, Constant.DD_MM_YYYY_FORMATTER);
                    } catch (DateTimeParseException e) {
                        userService.updateSubState(user, user.getSubState().getParentSubState());
                        return configureMessage(
                                chatId,
                                "Вы ввели некорректное значение. Операция отменяется.",
                                KeyboardService.RESERVE_LIST_KEYBOARD
                        );
                    }

                    List<ReserveEntity> reserves = reserveService.findActiveByTavernWithTableUser(user.getTavern(), date).stream()
                            .sorted(Comparator.comparing(ReserveEntity::getTime))
                            .toList();

                    if (isEmpty(reserves)) {
                        userService.updateSubState(user, user.getSubState().getParentSubState());
                        return configureMessage(chatId, "У Вас нет бронирований за выбранную дату.", KeyboardService.RESERVE_LIST_KEYBOARD);
                    }

                    userService.updateSubState(user, SubState.DELETE_RESERVE_CHOICE_TABLE);

                    deleteReservesTemporary.put(user, date);

                    ReplyKeyboardMarkup reservesDatesKeyboard = new ReplyKeyboardMarkup();
                    List<KeyboardRow> rows = new ArrayList<>();

                    rows.add(new KeyboardRow(List.of(new KeyboardButton(Button.PICK_ALL.getText()))));

                    reserves.forEach(reserve ->
                            rows.add(new KeyboardRow(List.of(new KeyboardButton(String.format(
                                    "%s %s %s [%s]",
                                    reserve.getTime(),
                                    reserve.getTable().getLabel(),
                                    reserve.getManualMode() ? reserve.getName() : reserve.getUser().getName(),
                                    reserve.getId()
                            )))))
                    );

                    rows.add(new KeyboardRow(List.of(new KeyboardButton(Button.CANCEL.getText()))));

                    reservesDatesKeyboard.setKeyboard(rows);
                    reservesDatesKeyboard.setResizeKeyboard(true);

                    return configureMessage(chatId, "Выберите бронирование для завершения.", reservesDatesKeyboard);
                }
                case DELETE_RESERVE_CHOICE_TABLE -> {
                    userService.updateSubState(user, user.getSubState().getParentSubState());

                    if (button == Button.PICK_ALL) {
                        LocalDate date = deleteReservesTemporary.get(user);
                        if (date != null) {
                            List<ReserveEntity> reserves = reserveService.findActiveByTavern(user.getTavern(), date);

                            reserveService.updateStatus(reserves, ReserveStatus.COMPLETED);

                            String textMessage = "Все бронирования были завершены."
                                    + System.lineSeparator()
                                    + System.lineSeparator()
                                    + fillReservesList(user.getTavern());

                            return configureMessage(chatId, textMessage, KeyboardService.RESERVE_LIST_KEYBOARD);
                        }
                    } else {
                        Long id = messageService.parseId(messageText);
                        if (id != null) {
                            List<ReserveEntity> reserves = reserveService.findActiveByTavernWithTableUserTavern(user.getTavern());

                            reserves.stream()
                                    .filter(reserve -> reserve.getId().equals(id))
                                    .findFirst()
                                    .ifPresent(foundReserve -> {
                                        reserveService.updateStatus(foundReserve, ReserveStatus.COMPLETED);

                                        UserEntity reserveUser = foundReserve.getUser();
                                        if (reserveUser != null && !reserveUser.equals(user)) {
                                            telegramApiService.sendMessage(
                                                            reserveUser.getTelegramId(),
                                                            String.format(
                                                                    NOTIFY_USER_RESERVE_CANCELLED,
                                                                    foundReserve.getDate().format(Constant.DD_MM_YYYY_FORMATTER),
                                                                    foundReserve.getTime().format(Constant.HH_MM_FORMATTER),
                                                                    foundReserve.getTable().getTavern().getName()
                                                            ),
                                                            false
                                                    )
                                                    .subscribeOn(Schedulers.boundedElastic())
                                                    .subscribe();
                                        }
                                    });

                            String textMessage = "Выбранное бронирование завершено."
                                    + System.lineSeparator()
                                    + System.lineSeparator()
                                    + fillReservesList(user.getTavern());

                            return configureMessage(chatId, textMessage, KeyboardService.RESERVE_LIST_KEYBOARD);
                        }
                    }
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
                    newReserve.setManualMode(true);
                    newReserve.setUser(user);

                    addReservesTemporary.put(user, newReserve);

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

                    addReservesTemporary.get(user)
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

                        ReserveEntity reserve = addReservesTemporary.get(user);

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

                    addReservesTemporary.get(user)
                            .setNumberPeople(numberPeople);

                    userService.updateSubState(user, SubState.ADD_RESERVE_CHOICE_NAME);

                    return configureMessage(chatId, "Введите имя:", KeyboardService.CANCEL_KEYBOARD);
                }
                case ADD_RESERVE_CHOICE_NAME -> {
                    addReservesTemporary.get(user)
                            .setName(messageText);

                    userService.updateSubState(user, SubState.ADD_RESERVE_CHOICE_PHONE);

                    return configureMessage(chatId, MessageText.ENTER_PHONE_NUMBER, KeyboardService.WITHOUT_PHONE_KEYBOARD);
                }
                case ADD_RESERVE_CHOICE_PHONE -> {
                    if (button != Button.WITHOUT_PHONE) {
                        addReservesTemporary.get(user)
                                .setPhoneNumber(messageText);
                    }

                    userService.updateSubState(user, SubState.ADD_RESERVE_INFO);

                    return configureMessage(chatId, fillReserveInfo(addReservesTemporary.get(user)), KeyboardService.APPROVE_KEYBOARD);
                }
                case ADD_RESERVE_INFO -> {
                    user.setState(State.MAIN_MENU);
                    userService.updateSubState(user, SubState.VIEW_MAIN_MENU);

                    if (button == Button.ACCEPT) {
                        ReserveEntity reserve = addReservesTemporary.get(user);
                        reserveService.save(reserve);
                        addReservesTemporary.remove(user);

                        return configureMessage(chatId, "Столик забронирован.", KeyboardService.CLIENT_MAIN_MENU);
                    }

                    return configureMessage(chatId, "Столик не удалось забронировать.", KeyboardService.CLIENT_MAIN_MENU);
                }
            }
        }

        // отрисовка меню
        return switch (user.getSubState()) {
            case VIEW_RESERVE_LIST -> configureMessage(chatId, fillReservesList(user.getTavern()), KeyboardService.RESERVE_LIST_KEYBOARD);

            default -> new SendMessage();
        };
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
                reserve.getDate().format(Constant.DD_MM_YYYY_FORMATTER),
                reserve.getTime().format(Constant.HH_MM_FORMATTER),
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
        return configureMessage(chatId, "Введите или выберите кол-во персон:", KeyboardService.NUMBERS_KEYBOARD_WITH_CANCEL);
    }

    private SendMessage configureChoiceTable(UserEntity user, Long chatId) {
        LocalDate date = addReservesTemporary.get(user)
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
                            .map(time -> time.format(Constant.HH_MM_FORMATTER))
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
            return configureMessage(chatId, "У Вас нет активных бронирований.", KeyboardService.RESERVE_LIST_KEYBOARD);
        }

        userService.updateSubState(user, SubState.DELETE_RESERVE_CHOICE_DATE);

        ReplyKeyboardMarkup reservesDatesKeyboard = new ReplyKeyboardMarkup();
        List<KeyboardRow> rows = new ArrayList<>();
        reservesDates.forEach(date ->
                rows.add(new KeyboardRow(List.of(new KeyboardButton(date.format(Constant.DD_MM_YYYY_FORMATTER)))))
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
                            reserve.getTime().format(Constant.HH_MM_FORMATTER),
                            reserve.getTable().getLabel(),
                            reserve.getNumberPeople(),
                            reserve.getManualMode() ? Optional.ofNullable(reserve.getName())
                                    .orElse("")
                                    : reserve.getUser().getName(),
                            reserve.getManualMode() ? Optional.ofNullable(reserve.getPhoneNumber())
                                    .orElse("")
                                    : reserve.getUser().findContact(ContractType.MOBILE)
                                    .map(ContactEntity::getValue)
                                    .orElse("")
                    ))
                    .collect(Collectors.joining(System.lineSeparator()));

            reservesDescription
                    .append("<b>\uD83D\uDDD3 ")
                    .append(date.format(Constant.DD_MM_YYYY_FORMATTER))
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
