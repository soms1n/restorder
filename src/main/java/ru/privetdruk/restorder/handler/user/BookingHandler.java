package ru.privetdruk.restorder.handler.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;
import ru.privetdruk.restorder.handler.MessageHandler;
import ru.privetdruk.restorder.model.consts.Constant;
import ru.privetdruk.restorder.model.consts.MessageText;
import ru.privetdruk.restorder.model.dto.BookingDto;
import ru.privetdruk.restorder.model.entity.*;
import ru.privetdruk.restorder.model.enums.*;
import ru.privetdruk.restorder.service.*;
import ru.privetdruk.restorder.service.util.StringService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static java.util.Comparator.comparing;
import static java.util.Comparator.comparingInt;
import static org.springframework.util.CollectionUtils.isEmpty;
import static ru.privetdruk.restorder.model.consts.Constant.*;
import static ru.privetdruk.restorder.service.KeyboardService.BOOKING_CHOICE_TIME_KEYBOARD;
import static ru.privetdruk.restorder.service.MessageService.configureMarkdownMessage;
import static ru.privetdruk.restorder.service.MessageService.toMessage;

@Slf4j
@Component
@RequiredArgsConstructor
public class BookingHandler implements MessageHandler {
    public static final String BEFORE = " до ";
    public static final String FREE_UP = ", освободить до ";
    public static final long FREE_UP_MINUTES = 10L;
    private final ContactService contactService;
    private final InfoService infoService;
    private final MessageService messageService;
    private final ReserveService reserveService;
    private final StringService stringService;
    private final TavernService tavernService;
    private final TelegramApiService telegramApiService;
    private final UserService userService;

    private final Map<UserEntity, BookingDto> bookings = new HashMap<>();

    @Override
    public SendMessage handle(UserEntity user, Message message, CallbackQuery callback) {
        String messageText = message.getText();
        Button button = Button.fromText(messageText)
                .orElse(Button.NOTHING);
        Long chatId = message.getChatId();

        // обработка функциональных клавиш
        switch (button) {
            case BACK, CANCEL, NO -> userService.updateSubState(user, user.getSubState().getParentSubState());
            case RETURN_MAIN_MENU -> returnToMainMenu(user);
            case CANCEL_RESERVE -> {
                return configureDeleteReserve(user, chatId);
            }
        }

        // обновление/обработка состояния
        if (button != Button.CANCEL && button != Button.NO) {
            switch (user.getSubState()) {
                case VIEW_MAIN_MENU -> {
                    switch (button) {
                        case MY_RESERVE -> userService.updateSubState(user, SubState.VIEW_RESERVE_LIST);
                        case HOOKAH_BAR, CAFE_BAR_RESTAURANT, NIGHT_CLUB, BILLIARDS, BOWLING -> {
                            Category category = Category.fromButton(button);
                            bookings.put(user, new BookingDto(category));
                            userService.updateSubState(user, SubState.VIEW_TAVERN_LIST);
                        }
                    }
                }
                case VIEW_TAVERN -> {
                    if (button == Button.RESERVE || button == Button.BACK) {
                        userService.updateSubState(user, SubState.BOOKING_CHOICE_DATE);

                        return toMessage(chatId, MessageText.ENTER_RESERVE_DATE, KeyboardService.BOOKING_CHOICE_DATE_KEYBOARD);
                    }
                }
                case BOOKING_CHOICE_DATE -> {
                    try {
                        if (button != Button.BACK) {
                            LocalDate now = LocalDate.now();

                            LocalDate date = switch (button) {
                                case TODAY -> now;
                                case TOMORROW -> now.plusDays(1);
                                default -> LocalDate.parse(messageText, Constant.DD_MM_YYYY_WITHOUT_DOT_FORMATTER);
                            };

                            if (date.isBefore(now)) {
                                return toMessage(chatId, MessageText.INCORRECT_MORE_DATE_RETRY, KeyboardService.BOOKING_CHOICE_DATE_KEYBOARD);
                            }

                            bookings.get(user)
                                    .setDate(date);
                        }

                        userService.updateSubState(user, SubState.BOOKING_CHOICE_TIME);

                        return toMessage(chatId, MessageText.ENTER_RESERVE_TIME, BOOKING_CHOICE_TIME_KEYBOARD);
                    } catch (Exception exception) {
                        return toMessage(chatId, MessageText.INCORRECT_DATE_RETRY, KeyboardService.BOOKING_CHOICE_DATE_KEYBOARD);
                    }
                }
                case BOOKING_CHOICE_TIME -> {
                    try {
                        if (button != Button.BACK) {
                            LocalTime time = LocalTime.parse(messageText, Constant.HH_MM_WITHOUT_DOT_FORMATTER);

                            BookingDto booking = bookings.get(user);

                            DayWeek dayWeek = DayWeek.fromDate(booking.getDate());

                            boolean available = booking.getTavern().getSchedules().stream()
                                    .filter(schedule -> schedule.getDayWeek() == dayWeek)
                                    .anyMatch(schedule -> {
                                        LocalTime start = schedule.getStartPeriod();
                                        LocalTime end = schedule.getEndPeriod();

                                        return ((time.isAfter(start) || time.equals(start))
                                                && (end.isBefore(start) || (end.isAfter(start) && time.isBefore(end))))
                                                || (time.isBefore(start) && (time.isBefore(end) || time.equals(end)));
                                    });

                            if (!available) {
                                return toMessage(chatId, MessageText.TAVERN_IS_NOT_WORKING, BOOKING_CHOICE_TIME_KEYBOARD);
                            }

                            if (booking.getDate().isEqual(LocalDate.now()) && time.isBefore(LocalTime.now())) {
                                return toMessage(chatId, MessageText.INCORRECT_MORE_TIME_RETRY, BOOKING_CHOICE_TIME_KEYBOARD);
                            }

                            booking.setTime(time);
                        }

                        userService.updateSubState(user, SubState.BOOKING_CHOICE_PERSONS);

                        return toMessage(chatId, MessageText.ENTER_NUMBER_PERSONS, KeyboardService.NUMBERS_KEYBOARD);
                    } catch (Exception exception) {
                        return toMessage(chatId, MessageText.INCORRECT_TIME_RETRY, BOOKING_CHOICE_TIME_KEYBOARD);
                    }
                }
                case BOOKING_CHOICE_PERSONS -> {
                    try {
                        Integer persons = button.getNumber();
                        if (persons == null) {
                            persons = Integer.parseInt(messageText);
                        }

                        BookingDto booking = bookings.get(user);
                        booking.setPersons(persons);

                        if (StringUtils.hasText(booking.getTavern().getLinkTableLayout())) {
                            userService.updateSubState(user, SubState.BOOKING_CHOICE_TABLE_ANSWER);
                            return toMessage(chatId, MessageText.MANUAL_OR_AUTO, KeyboardService.BOOKING_CHOICE_TABLE_ANSWER_KEYBOARD);
                        }

                        return choiceTableAutomatic(user, chatId, booking);
                    } catch (Exception exception) {
                        if (!(exception instanceof NumberFormatException)) {
                            log.error(MessageText.UNEXPECTED_ERROR, exception);
                        }

                        return toMessage(chatId, MessageText.INCORRECT_VALUE_TRY_AGAIN, KeyboardService.NUMBERS_KEYBOARD);
                    }
                }
                case BOOKING_CHOICE_TABLE_ANSWER -> {
                    if (button == Button.MANUALLY) {
                        return choiceTableManually(user, chatId, bookings.get(user));
                    } else if (button == Button.AUTOMATIC) {
                        return choiceTableAutomatic(user, chatId, bookings.get(user));
                    } else {
                        return toMessage(chatId, MessageText.INCORRECT_VALUE_TRY_AGAIN, KeyboardService.BOOKING_CHOICE_TABLE_ANSWER_KEYBOARD);
                    }
                }
                case BOOKING_CHOICE_TABLE_MANUALLY -> {
                    Long tableId = messageService.parseId(messageText);
                    BookingDto booking = bookings.get(user);

                    TableEntity reserveTable = booking.getTavern().getTables().stream()
                            .filter(table -> table.getId().equals(tableId))
                            .findFirst()
                            .orElse(null);

                    if (reserveTable == null) {
                        return toMainMenu(user, MessageText.INCORRECT_VALUE_CANCELLED);
                    }

                    int beforeIndex = messageText.indexOf(BEFORE);
                    if (beforeIndex != -1) {
                        booking.setBeforeTime(LocalTime.parse(messageText.substring(beforeIndex + 4, beforeIndex + 9)));
                    }

                    booking.setTable(reserveTable);

                    userService.updateSubState(user, SubState.BOOKING_APPROVE);

                    return toMessage(chatId, infoService.fillReserveInfo(bookings.get(user), true), KeyboardService.APPROVE_KEYBOARD);
                }
                case BOOKING_APPROVE_BEFORE -> {
                    if (button == Button.ACCEPT) {
                        userService.updateSubState(user, SubState.BOOKING_APPROVE);

                        return toMessage(chatId, infoService.fillReserveInfo(bookings.get(user), true), KeyboardService.APPROVE_KEYBOARD);
                    } else {
                        bookings.remove(user);
                        return toMainMenu(user, MessageText.RESERVE_CANCEL);
                    }
                }
                case BOOKING_APPROVE -> {
                    if (button == Button.ACCEPT) {
                        BookingDto booking = bookings.get(user);
                        booking.setName(user.getName());

                        booking.setPhoneNumber(
                                contactService.findByUser(user).stream()
                                        .filter(ContactEntity::getActive)
                                        .map(ContactEntity::getValue)
                                        .findFirst()
                                        .orElse(null)
                        );

                        ReserveEntity reserve = new ReserveEntity();
                        reserve.setUser(user);
                        reserve.setName(user.getName());
                        reserve.setPhoneNumber(booking.getPhoneNumber());
                        reserve.setDate(booking.getDate());
                        reserve.setTime(booking.getTime());
                        reserve.setTable(booking.getTable());
                        reserve.setNumberPeople(booking.getPersons());

                        reserveService.save(reserve);

                        bookings.remove(user);

                        String messageForAdmin = "Только что забронировали новый столик."
                                + System.lineSeparator() + System.lineSeparator()
                                + infoService.fillReserveInfo(booking, false);

                        Flux.fromIterable(
                                        booking.getTavern().getEmployees().stream()
                                                .filter(employee -> employee.getRoles().contains(Role.CLIENT_EMPLOYEE))
                                                .collect(Collectors.toSet())
                                )
                                .flatMap(employee -> telegramApiService.prepareSendMessage(employee.getTelegramId(), messageForAdmin, true))
                                .subscribeOn(Schedulers.boundedElastic())
                                .subscribe();

                        return toMainMenu(user, MessageText.RESERVE_SUCCESS);
                    }

                    bookings.remove(user);

                    return toMainMenu(user, MessageText.CANCEL_OPERATION_RETURN_TO_MENU);
                }
                case CHOICE_TAVERN -> {
                    TavernEntity tavern;
                    if (button == Button.BACK) {
                        tavern = bookings.get(user)
                                .getTavern();
                    } else {
                        Long tavernId = messageService.parseId(messageText);
                        if (tavernId == null) {
                            return toMainMenu(user, MessageText.YOU_DONT_CHOICE_TAVERN);
                        }

                        tavern = tavernService.findWithAllData(tavernId);
                        if (tavern == null) {
                            return toMainMenu(user, MessageText.CANT_MAKE_RESERVATION);
                        }

                        bookings.get(user)
                                .setTavern(tavern);
                    }

                    userService.updateSubState(user, SubState.VIEW_TAVERN);

                    return toMessage(chatId, infoService.fillGeneral(tavern), KeyboardService.TAVERN_INFO_KEYBOARD);
                }
                case DELETE_RESERVE_CHOICE_TAVERN -> {
                    Long id = messageService.parseId(messageText);
                    if (id == null) {
                        return toMessage(chatId, MessageText.INCORRECT_VALUE_CANCELLED, KeyboardService.USER_MAIN_MENU_KEYBOARD);
                    }

                    ReserveEntity reserve = reserveService.findById(id);
                    reserve.setStatus(ReserveStatus.CANCELLED);
                    reserveService.save(reserve);

                    returnToMainMenu(user);
                }
            }
        }

        // отрисовка меню
        return switch (user.getSubState()) {
            case VIEW_MAIN_MENU -> configureMainMenu(chatId, user);
            case VIEW_RESERVE_LIST -> toMessage(chatId, fillReserves(user), KeyboardService.USER_RESERVE_LIST_KEYBOARD);
            case VIEW_TAVERN_LIST -> fillTaverns(user);

            default -> new SendMessage();
        };
    }

    private SendMessage configureMainMenu(Long chatId, UserEntity user) {
        bookings.remove(user);
        return toMessage(chatId, MessageText.OPEN_MENU, KeyboardService.USER_MAIN_MENU_KEYBOARD);
    }

    private SendMessage choiceTableManually(UserEntity user, Long chatId, BookingDto booking) {
        TavernEntity tavern = booking.getTavern();

        List<TableEntity> tables = tavern.getTables().stream()
                .filter(table -> table.getNumberSeats() >= booking.getPersons())
                .sorted(comparingInt(TableEntity::getNumberSeats))
                .toList();

        if (isEmpty(tables)) {
            return toMainMenu(user, tableFullMessage(booking));
        }

        List<TableEntity> freeTables = new ArrayList<>();

        for (TableEntity table : tables) {
            List<ReserveEntity> reserves = reserveService.findActiveByTable(table, booking.getDate());

            if (isEmpty(reserves)) {
                freeTables.add(table);
            }
        }

        LocalTime time = booking.getTime();

        Map<LocalTime, TableEntity> tablesTime = new HashMap<>();
        for (TableEntity table : tables) {
            ReserveEntity foundReserve = reserveService.findActiveByTable(table, booking.getDate()).stream()
                    .min(comparing(ReserveEntity::getTime))
                    .orElse(null);

            if (foundReserve == null || (foundReserve.getTime().isBefore(time)
                    || (foundReserve.getTime().getHour() == time.getHour() && foundReserve.getTime().getMinute() == time.getMinute()))) {
                continue;
            }

            tablesTime.putIfAbsent(foundReserve.getTime(), table);
        }

        if (freeTables.isEmpty() && tablesTime.isEmpty()) {
            return toMainMenu(user, tableFullMessage(booking));
        }

        ReplyKeyboardMarkup tablesKeyboard = new ReplyKeyboardMarkup();
        List<KeyboardRow> rows = new ArrayList<>();

        if (!freeTables.isEmpty()) {
            freeTables.forEach(table ->
                    rows.add(new KeyboardRow(List.of(new KeyboardButton(
                            table.getLabel() + SPACE + table.getNumberSeats() + SPACE
                                    + stringService.declensionWords(table.getNumberSeats(), StringService.SEATS_WORDS)
                                    + LEFT_SQUARE_BRACKET_WITH_SPACE + table.getId() + RIGHT_SQUARE_BRACKET
                    ))))
            );
        }

        if (!tablesTime.isEmpty()) {
            for (var entry : tablesTime.entrySet()) {
                TableEntity table = entry.getValue();
                rows.add(new KeyboardRow(List.of(new KeyboardButton(
                        table.getLabel() + SPACE + table.getNumberSeats() + SPACE
                                + stringService.declensionWords(table.getNumberSeats(), StringService.SEATS_WORDS)
                                + FREE_UP + entry.getKey().minusMinutes(FREE_UP_MINUTES)
                                + LEFT_SQUARE_BRACKET_WITH_SPACE + table.getId() + RIGHT_SQUARE_BRACKET
                ))));
            }
        }

        rows.add(new KeyboardRow(List.of(new KeyboardButton(Button.CANCEL.getText()))));

        tablesKeyboard.setKeyboard(rows);
        tablesKeyboard.setResizeKeyboard(true);

        userService.updateSubState(user, SubState.BOOKING_CHOICE_TABLE_MANUALLY);

        return configureMarkdownMessage(chatId, format(MessageText.CHOICE_TABLE_WITH_LINK, tavern.getLinkTableLayout()), tablesKeyboard);
    }

    private SendMessage choiceTableAutomatic(UserEntity user, Long chatId, BookingDto booking) {
        TavernEntity tavern = tavernService.findWithTables(booking.getTavern());

        List<TableEntity> tables = tavern.getTables().stream()
                .filter(table -> table.getNumberSeats() >= booking.getPersons())
                .sorted(comparingInt(TableEntity::getNumberSeats))
                .toList();

        if (isEmpty(tables)) {
            return toMainMenu(user, tableFullMessage(booking));
        }

        for (TableEntity table : tables) {
            List<ReserveEntity> reserves = reserveService.findActiveByTable(table, booking.getDate());

            if (isEmpty(reserves)) {
                booking.setTable(table);

                userService.updateSubState(user, SubState.BOOKING_APPROVE);

                return toMessage(chatId, infoService.fillReserveInfo(booking, true), KeyboardService.APPROVE_KEYBOARD);
            }
        }

        LocalTime time = booking.getTime();

        Map<LocalTime, TableEntity> tablesTime = new HashMap<>();
        for (TableEntity table : tables) {
            ReserveEntity foundReserve = reserveService.findActiveByTable(table, booking.getDate()).stream()
                    .min(comparing(ReserveEntity::getTime))
                    .orElse(null);

            if (foundReserve == null || (foundReserve.getTime().isBefore(time)
                    || (foundReserve.getTime().getHour() == time.getHour() && foundReserve.getTime().getMinute() == time.getMinute()))) {
                continue;
            }

            tablesTime.putIfAbsent(foundReserve.getTime(), table);
        }

        if (tablesTime.isEmpty()) {
            bookings.remove(user);

            return toMainMenu(user, tableFullMessage(booking));
        }

        LocalTime maxTime = tablesTime.keySet().stream()
                .max(LocalTime::compareTo)
                .orElse(null);

        booking.setTable(tablesTime.get(maxTime));

        userService.updateSubState(user, SubState.BOOKING_APPROVE_BEFORE);

        return toMessage(
                chatId,
                format(MessageText.TABLE_FREE_UP, maxTime.format(HH_MM_FORMATTER), maxTime.minusMinutes(FREE_UP_MINUTES)),
                KeyboardService.APPROVE_BEFORE_KEYBOARD
        );
    }

    private String tableFullMessage(BookingDto booking) {
        return format(
                MessageText.TABLE_NOT_FOUND,
                booking.getPersons(),
                stringService.declensionWords(booking.getPersons(), StringService.SEATS_WORDS)
        );
    }

    private SendMessage fillTaverns(UserEntity user) {
        Category category = bookings.get(user)
                .getCategory();

        if (category == null) {
            return toMainMenu(user, "Категория не выбрана.");
        }

        List<TavernEntity> taverns = tavernService.find(user.getCity(), category);

        if (isEmpty(taverns)) {
            return toMainMenu(user, "К сожалению в выбранной категории ещё нет заведений.");
        }

        userService.updateSubState(user, SubState.CHOICE_TAVERN);

        final AtomicInteger counter = new AtomicInteger(0);

        List<KeyboardRow> keyboardRows = new ArrayList<>(
                taverns.stream()
                        .map(tavern -> new KeyboardButton(tavern.getName() + LEFT_SQUARE_BRACKET_WITH_SPACE + tavern.getId() + RIGHT_SQUARE_BRACKET))
                        .collect(Collectors.groupingBy(button -> counter.getAndIncrement() / 2))
                        .values())
                .stream()
                .map(KeyboardRow::new)
                .collect(Collectors.toList());

        keyboardRows.add(KeyboardService.RETURN_MAIN_MENU_ROW);

        ReplyKeyboardMarkup keyboard = ReplyKeyboardMarkup.builder()
                .keyboard(keyboardRows)
                .build();

        keyboard.setResizeKeyboard(true);

        return toMessage(user.getTelegramId(), MessageText.CHOICE_TAVERN, keyboard);
    }

    private SendMessage toMainMenu(UserEntity user, String message) {
        bookings.remove(user);
        userService.updateState(user, State.BOOKING);
        return toMessage(user.getTelegramId(), message, KeyboardService.USER_MAIN_MENU_KEYBOARD);
    }

    private void returnToMainMenu(UserEntity user) {
        userService.updateState(user, State.BOOKING);
    }

    private SendMessage configureDeleteReserve(UserEntity user, Long chatId) {
        List<ReserveEntity> activeReserves = reserveService.findActiveByUser(user);

        if (isEmpty(activeReserves)) {
            return toMessage(chatId, MessageText.ACTIVE_RESERVES_NOT_FOUND, KeyboardService.USER_RESERVE_LIST_KEYBOARD);
        }

        userService.updateSubState(user, SubState.DELETE_RESERVE_CHOICE_TAVERN);

        ReplyKeyboardMarkup reservesKeyboard = new ReplyKeyboardMarkup();
        List<KeyboardRow> rows = new ArrayList<>();

        activeReserves.stream()
                .sorted(comparing(o -> LocalDateTime.of(o.getDate(), o.getTime())))
                .filter(reserve -> reserve.getStatus() == ReserveStatus.ACTIVE)
                .map(reserve -> format(
                        "%s %s %s [%s]",
                        reserve.getDate().format(DD_MM_YYYY_FORMATTER),
                        reserve.getTime(),
                        reserve.getTable().getTavern().getName(),
                        reserve.getId()
                ))
                .forEach(reserve -> rows.add(new KeyboardRow(List.of(new KeyboardButton(reserve)))));

        rows.add(new KeyboardRow(List.of(new KeyboardButton(Button.CANCEL.getText()))));

        reservesKeyboard.setKeyboard(rows);
        reservesKeyboard.setResizeKeyboard(true);

        return toMessage(chatId, "Выберите бронь, которую хотите отменить.", reservesKeyboard);
    }

    private String fillReserves(UserEntity user) {
        List<ReserveEntity> activeReserves = reserveService.findActiveByUser(user);

        if (isEmpty(activeReserves)) {
            return MessageText.ACTIVE_RESERVES_NOT_FOUND;
        }

        return activeReserves.stream()
                .filter(reserve -> reserve.getStatus() == ReserveStatus.ACTIVE)
                .sorted(comparing(o -> LocalDateTime.of(o.getDate(), o.getTime())))
                .map(reserve -> format(
                        """
                                ™️ <b>Заведение:</b> <i>%s</i>
                                \uD83D\uDDD3 <b>Дата и время:</b> <i>%s</i> в <i>%s</i>
                                \uD83D\uDC65 <b>Кол-во персон:</b> <i>%s</i>""",
                        reserve.getTable().getTavern().getName(),
                        reserve.getDate().format(DD_MM_YYYY_FORMATTER),
                        reserve.getTime().format(HH_MM_FORMATTER),
                        reserve.getNumberPeople()
                ))
                .collect(Collectors.joining(System.lineSeparator() + System.lineSeparator()));
    }
}
