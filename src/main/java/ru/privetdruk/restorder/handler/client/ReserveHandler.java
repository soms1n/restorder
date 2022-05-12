package ru.privetdruk.restorder.handler.client;

import lombok.RequiredArgsConstructor;
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
import ru.privetdruk.restorder.model.consts.Constant;
import ru.privetdruk.restorder.model.consts.MessageText;
import ru.privetdruk.restorder.model.entity.*;
import ru.privetdruk.restorder.model.enums.*;
import ru.privetdruk.restorder.service.KeyboardService;
import ru.privetdruk.restorder.service.MessageService;
import ru.privetdruk.restorder.service.ReserveService;
import ru.privetdruk.restorder.service.UserService;
import ru.privetdruk.restorder.service.util.StringService;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;

import static ru.privetdruk.restorder.service.MessageService.configureMessage;

@Component
@RequiredArgsConstructor
public class ReserveHandler implements MessageHandler {
    private final UserService userService;
    private final MessageService messageService;
    private final MainMenuHandler mainMenuHandler;
    private final ReserveService reserveService;
    private final StringService stringService;

    private final Map<UserEntity, LocalDate> deleteReservesTemporary = new HashMap<>();
    private final Map<UserEntity, ReserveEntity> addReservesTemporary = new HashMap<>();

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
            case BACK, CANCEL, NO -> {
                switch (subState) {
                    case ADD_RESERVE_CHOICE_DATE, ADD_RESERVE_CHOICE_TIME, ADD_RESERVE_CHOICE_PERSONS, ADD_RESERVE_CHOICE_TABLE, ADD_RESERVE_INFO -> {
                        return returnToMainMenu(user, message, callback);
                    }
                }

                user.setSubState(subState.getParentSubState());
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

                    List<ReserveEntity> reserves = user.getTavern().getTables().stream()
                            .map(TableEntity::getReserves)
                            .flatMap(Collection::stream)
                            .filter(reserve -> reserve.getStatus() == ReserveStatus.ACTIVE)
                            .filter(reserve -> reserve.getDate().equals(date))
                            .sorted(Comparator.comparing(ReserveEntity::getTime))
                            .collect(Collectors.toList());

                    if (CollectionUtils.isEmpty(reserves)) {
                        userService.updateSubState(user, user.getSubState().getParentSubState());
                        return configureMessage(chatId, "Нет резервов за выбранную дату.", KeyboardService.RESERVE_LIST_KEYBOARD);
                    }

                    userService.updateSubState(user, SubState.DELETE_RESERVE_CHOICE_TABLE);

                    deleteReservesTemporary.put(user, date);

                    ReplyKeyboardMarkup reservesDatesKeyboard = new ReplyKeyboardMarkup();
                    List<KeyboardRow> rows = new ArrayList<>();

                    rows.add(new KeyboardRow(List.of(new KeyboardButton(Button.PICK_ALL.getText()))));

                    reserves.forEach(reserve ->
                            rows.add(new KeyboardRow(List.of(new KeyboardButton(String.format(
                                    "ID: %s %s %s %s",
                                    reserve.getId(),
                                    reserve.getTime(),
                                    reserve.getTable().getLabel(),
                                    reserve.getUser().getName()
                            )))))
                    );

                    rows.add(new KeyboardRow(List.of(new KeyboardButton(Button.CANCEL.getText()))));

                    reservesDatesKeyboard.setKeyboard(rows);
                    reservesDatesKeyboard.setResizeKeyboard(true);

                    return configureMessage(chatId, "Выберите резерв для удаления.", reservesDatesKeyboard);
                }
                case DELETE_RESERVE_CHOICE_TABLE -> {
                    userService.updateSubState(user, user.getSubState().getParentSubState());

                    if (button == Button.PICK_ALL) {
                        LocalDate date = deleteReservesTemporary.get(user);
                        if (date != null) {
                            Set<ReserveEntity> reserves = user.getTavern().getTables().stream()
                                    .map(TableEntity::getReserves)
                                    .flatMap(Collection::stream)
                                    .filter(reserve -> reserve.getStatus() == ReserveStatus.ACTIVE)
                                    .filter(reserve -> reserve.getDate().equals(date))
                                    .collect(Collectors.toSet());

                            reserveService.updateStatus(reserves, ReserveStatus.COMPLETED);

                            return configureMessage(chatId, "Все резервы были завершены.", KeyboardService.RESERVE_LIST_KEYBOARD);
                        }
                    } else {
                        Long id = messageService.parseId(messageText);
                        if (id != null) {
                            user.getTavern().getTables().stream()
                                    .map(TableEntity::getReserves)
                                    .flatMap(Collection::stream)
                                    .filter(reserve -> reserve.getStatus() == ReserveStatus.ACTIVE)
                                    .filter(reserve -> reserve.getId().equals(id))
                                    .findFirst()
                                    .ifPresent(foundReserve -> reserveService.updateStatus(foundReserve, ReserveStatus.COMPLETED));

                            return configureMessage(chatId, "Выбранный резерв завершен.", KeyboardService.RESERVE_LIST_KEYBOARD);
                        }
                    }
                }
                case ADD_RESERVE_CHOICE_DATE -> {
                    LocalDate date;
                    if (button == Button.TODAY) {
                        date = LocalDate.now();
                    } else if (button == Button.TOMORROW) {
                        date = LocalDate.now().plusDays(1);
                    } else {
                        try {
                            date = LocalDate.parse(messageText, Constant.DD_MM_YYYY_WITHOUT_DOT_FORMATTER);
                        } catch (DateTimeParseException e) {
                            return configureMessage(
                                    chatId,
                                    "Дата не соответствует формату. Повторите попытку:",
                                    KeyboardService.TODAY_TOMORROW_CANCEL_KEYBOARD
                            );
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

                    if (button == Button.APPROVE) {
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
            case VIEW_RESERVE_LIST -> configureMessage(chatId, fillReservesList(tavern.getTables()), KeyboardService.RESERVE_LIST_KEYBOARD);

            default -> new SendMessage();
        };
    }

    private SendMessage configureAddReserve(UserEntity user, Long chatId) {
        userService.update(user, State.RESERVE, SubState.ADD_RESERVE_CHOICE_DATE);

        return configureMessage(
                chatId,
                "Введите дату резерва в формате ДДММГГГГ <i>(пример: 24052022)</i>:",
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
                "Введите время резерва <i>(в формате ЧЧММ, пример: 1830 или 0215)</i>:",
                KeyboardService.RESERVE_CHOICE_TIME_KEYBOARD
        );
    }

    private SendMessage configureChoicePersons(Long chatId) {
        return configureMessage(chatId, "Введите или выберите кол-во персон:", KeyboardService.NUMBERS_KEYBOARD_WITH_CANCEL);
    }

    private SendMessage configureChoiceTable(UserEntity user, Long chatId) {
        LocalDate date = addReservesTemporary.get(user)
                .getDate();

        ReplyKeyboardMarkup tablesKeyboard = new ReplyKeyboardMarkup();
        List<KeyboardRow> rows = new ArrayList<>();
        user.getTavern().getTables().stream()
                .sorted(Comparator.comparing(TableEntity::getNumberSeats))
                .forEach(table -> {
                            String reserveTimes = table.getReserves().stream()
                                    .filter(reserve -> reserve.getStatus() == ReserveStatus.ACTIVE)
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
        List<LocalDate> reservesDates = user.getTavern().getTables().stream()
                .map(TableEntity::getReserves)
                .flatMap(Collection::stream)
                .filter(reserve -> reserve.getStatus() == ReserveStatus.ACTIVE)
                .map(ReserveEntity::getDate)
                .distinct()
                .sorted(LocalDate::compareTo)
                .collect(Collectors.toList());

        if (CollectionUtils.isEmpty(reservesDates)) {
            return configureMessage(chatId, "Нечего удалять.", KeyboardService.RESERVE_LIST_KEYBOARD);
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

        return configureMessage(chatId, "Выберите дату, за которую хотите удалить резерв.", reservesDatesKeyboard);
    }

    private String fillReservesList(Set<TableEntity> tables) {
        Map<LocalDate, List<ReserveEntity>> reserves = tables.stream()
                .map(TableEntity::getReserves)
                .flatMap(Collection::stream)
                .filter(reserve -> reserve.getStatus() == ReserveStatus.ACTIVE)
                .collect(Collectors.groupingBy(ReserveEntity::getDate));

        if (CollectionUtils.isEmpty(reserves)) {
            return "Список бронирований пуст.";
        }

        List<LocalDate> sortedDate = reserves.keySet().stream()
                .sorted(LocalDate::compareTo)
                .collect(Collectors.toList());

        LocalDate tomorrow = LocalDate.now().plusDays(1);

        StringBuilder reservesDescription = new StringBuilder();
        for (LocalDate date : sortedDate) {
            String reservesList = reserves.get(date).stream()
                    .sorted(Comparator.comparing(ReserveEntity::getTime, LocalTime::compareTo))
                    .map(reserve -> String.format(
                            "<i>%s</i> <b>%s</b> %s %s %s",
                            reserve.getTime(),
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
