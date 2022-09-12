package ru.privetdruk.restorder.handler.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
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
import ru.privetdruk.restorder.model.entity.ReserveEntity;
import ru.privetdruk.restorder.model.entity.TableEntity;
import ru.privetdruk.restorder.model.entity.TavernEntity;
import ru.privetdruk.restorder.model.entity.UserEntity;
import ru.privetdruk.restorder.model.enums.*;
import ru.privetdruk.restorder.service.*;
import ru.privetdruk.restorder.service.util.StringService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static ru.privetdruk.restorder.service.MessageService.configureMarkdownMessage;
import static ru.privetdruk.restorder.service.MessageService.configureMessage;

@Slf4j
@Component
@RequiredArgsConstructor
public class BookingHandler implements MessageHandler {
    private final InfoService infoService;
    private final MessageService messageService;
    private final ReserveService reserveService;
    private final StringService stringService;
    private final TavernService tavernService;
    private final TelegramApiService telegramApiService;
    private final UserService userService;

    private final Map<UserEntity, BookingDto> bookings = new HashMap<>();

    @Override
    @Transactional
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

                        return configureMessage(
                                chatId,
                                "Введите дату в формате ДДММГГГГ <i>(пример: 24052022)</i>:",
                                KeyboardService.BOOKING_CHOICE_DATE_KEYBOARD
                        );
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
                                return configureMessage(
                                        chatId,
                                        "Дата бронирования должна быть больше, либо равна текущей дате. Повторите попытку:",
                                        KeyboardService.BOOKING_CHOICE_DATE_KEYBOARD
                                );
                            }

                            bookings.get(user)
                                    .setDate(date);
                        }

                        userService.updateSubState(user, SubState.BOOKING_CHOICE_TIME);

                        return configureMessage(chatId, "Введите время в формате ЧЧММ:", KeyboardService.BOOKING_CHOICE_TIME_KEYBOARD);
                    } catch (Throwable t) {
                        return configureMessage(
                                chatId,
                                "Введенная дата не соответствует формату. Пример - 05092022 <i>(5 сентября 2022 года)</i>. Повторите попытку:",
                                KeyboardService.BOOKING_CHOICE_DATE_KEYBOARD
                        );
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
                                return configureMessage(
                                        chatId,
                                        "В указанное время заведение не работает. Введите другое время:",
                                        KeyboardService.BOOKING_CHOICE_TIME_KEYBOARD
                                );
                            }

                            if (booking.getDate().isEqual(LocalDate.now()) && time.isBefore(LocalTime.now())) {
                                return configureMessage(
                                        chatId,
                                        "Время бронирования должно быть больше, либо равно текущему времени.",
                                        KeyboardService.BOOKING_CHOICE_TIME_KEYBOARD
                                );
                            }

                            booking.setTime(time);
                        }

                        userService.updateSubState(user, SubState.BOOKING_CHOICE_PERSONS);

                        return configureMessage(chatId, "Введите кол-во персон:", KeyboardService.NUMBERS_KEYBOARD);
                    } catch (Throwable t) {
                        return configureMessage(
                                chatId,
                                "Введенное время не соответствует формату. Пример - 0305 (3 часа 5 минут). Повторите попытку:",
                                KeyboardService.BOOKING_CHOICE_TIME_KEYBOARD
                        );
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

                        TavernEntity tavern = tavernService.findByIdWithSchedulesAndReserves(booking.getTavern().getId());
                        booking.setTavern(tavern);

                        if (StringUtils.hasText(tavern.getLinkTableLayout())) {
                            userService.updateSubState(user, SubState.BOOKING_CHOICE_TABLE_ANSWER);
                            return configureMessage(
                                    chatId,
                                    "Хотите сами выбрать место или мы подберем его автоматически?",
                                    KeyboardService.BOOKING_CHOICE_TABLE_ANSWER_KEYBOARD
                            );
                        }

                        return choiceTableAutomatic(user, chatId, booking);
                    } catch (Throwable t) {
                        return configureMessage(chatId,
                                "Вы ввели некорректное значение. Повторите попытку.",
                                KeyboardService.NUMBERS_KEYBOARD
                        );
                    }
                }
                case BOOKING_CHOICE_TABLE_ANSWER -> {
                    if (button == Button.MANUALLY) {
                        return choiceTableManually(user, chatId, bookings.get(user));
                    } else if (button == Button.AUTOMATIC) {
                        return choiceTableAutomatic(user, chatId, bookings.get(user));
                    } else {
                        return configureMessage(
                                chatId,
                                "Вы ввели некорректное значение. Повторите попытку.",
                                KeyboardService.BOOKING_CHOICE_TABLE_ANSWER_KEYBOARD
                        );
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
                        return toMainMenu(user, "Выбрано некорректное значение.");
                    }

                    int beforeIndex = messageText.indexOf(" до ");
                    if (beforeIndex != -1) {
                        booking.setBeforeTime(LocalTime.parse(messageText.substring(beforeIndex + 4, beforeIndex + 9)));
                    }

                    booking.setTable(reserveTable);

                    userService.updateSubState(user, SubState.BOOKING_APPROVE);

                    return configureMessage(chatId, fillReserveInfo(bookings.get(user), true), KeyboardService.APPROVE_KEYBOARD);
                }
                case BOOKING_APPROVE_BEFORE -> {
                    if (button == Button.ACCEPT) {
                        userService.updateSubState(user, SubState.BOOKING_APPROVE);

                        return configureMessage(chatId, fillReserveInfo(bookings.get(user), true), KeyboardService.APPROVE_KEYBOARD);
                    } else {
                        bookings.remove(user);
                        return toMainMenu(user, "Вы отменили бронирование.");
                    }
                }
                case BOOKING_APPROVE -> {
                    if (button == Button.ACCEPT) {
                        BookingDto booking = bookings.get(user);

                        ReserveEntity reserve = new ReserveEntity();
                        reserve.setUser(user);
                        reserve.setDate(booking.getDate());
                        reserve.setTime(booking.getTime());
                        reserve.setTable(booking.getTable());
                        reserve.setNumberPeople(booking.getPersons());

                        reserveService.save(reserve);

                        bookings.remove(user);

                        String messageForAdmin = "Только что забронировали новый столик."
                                + System.lineSeparator() + System.lineSeparator()
                                + fillReserveInfo(booking, false);

                        Flux.fromIterable(
                                        booking.getTavern().getEmployees().stream()
                                                .filter(employee -> employee.getRoles().contains(Role.CLIENT_EMPLOYEE))
                                                .collect(Collectors.toSet())
                                )
                                .flatMap(employee -> telegramApiService.sendMessage(employee.getTelegramId(), messageForAdmin, true))
                                .subscribeOn(Schedulers.boundedElastic())
                                .subscribe();

                        return toMainMenu(user, "Вы успешно забронировали столик!");
                    }

                    bookings.remove(user);

                    return toMainMenu(user, "Операция отменена. Возврат в главное меню.");
                }
                case CHOICE_TAVERN -> {
                    TavernEntity tavern;
                    if (button == Button.BACK) {
                        tavern = bookings.get(user)
                                .getTavern();
                    } else {
                        Long tavernId = messageService.parseId(messageText);
                        if (tavernId == null) {
                            return toMainMenu(user, "Вы не выбрали заведение.");
                        }

                        tavern = tavernService.find(tavernId, user.getCity());
                        if (tavern == null) {
                            return toMainMenu(user, "Нельзя забронировать столик в выбранном заведении.");
                        }

                        bookings.get(user)
                                .setTavern(tavern);
                    }

                    userService.updateSubState(user, SubState.VIEW_TAVERN);

                    return configureMessage(chatId, infoService.fillGeneral(tavern), KeyboardService.TAVERN_INFO_KEYBOARD);
                }
                case DELETE_RESERVE_CHOICE_TAVERN -> {
                    Long id = messageService.parseId(messageText);
                    if (id == null) {
                        return configureMessage(chatId, MessageText.INCORRECT_VALUE_CANCELLED, KeyboardService.USER_MAIN_MENU);
                    }

                    ReserveEntity cancelledReserve = user.getReserves().stream()
                            .filter(reserve -> reserve.getId().equals(id))
                            .peek(reserve -> reserve.setStatus(ReserveStatus.CANCELLED))
                            .findFirst()
                            .orElse(null);

                    user.getReserves().add(cancelledReserve);

                    userService.save(user);

                    returnToMainMenu(user);
                }
            }
        }

        // отрисовка меню
        return switch (user.getSubState()) {
            case VIEW_MAIN_MENU -> configureMainMenu(chatId, user);
            case VIEW_RESERVE_LIST -> configureMessage(chatId, fillReserves(user.getReserves()), KeyboardService.USER_RESERVE_LIST_KEYBOARD);
            case VIEW_TAVERN_LIST -> fillTaverns(user);

            default -> new SendMessage();
        };
    }

    private SendMessage configureMainMenu(Long chatId, UserEntity user) {
        bookings.remove(user);
        return configureMessage(chatId, "Открываю главное меню.", KeyboardService.USER_MAIN_MENU);
    }

    private SendMessage choiceTableManually(UserEntity user, Long chatId, BookingDto booking) {
        TavernEntity tavern = booking.getTavern();

        List<TableEntity> tables = tavern.getTables().stream()
                .filter(table -> table.getNumberSeats() >= booking.getPersons())
                .sorted(Comparator.comparingInt(TableEntity::getNumberSeats))
                .collect(Collectors.toList());

        if (CollectionUtils.isEmpty(tables)) {
            return toMainMenu(user, tableFullMessage(booking));
        }

        Set<TableEntity> freeTables = new HashSet<>();

        for (TableEntity table : tables) {
            Set<ReserveEntity> reserves = table.getReserves().stream()
                    .filter(reserve -> reserve.getDate().equals(booking.getDate())
                            && reserve.getStatus() == ReserveStatus.ACTIVE)
                    .collect(Collectors.toSet());

            if (CollectionUtils.isEmpty(reserves)) {
                freeTables.add(table);
            }
        }

        LocalTime time = booking.getTime();

        Map<LocalTime, TableEntity> tablesTime = new HashMap<>();
        for (TableEntity table : tables) {
            ReserveEntity foundReserve = table.getReserves().stream()
                    .filter(reserve -> reserve.getDate().equals(booking.getDate())
                            && reserve.getStatus() == ReserveStatus.ACTIVE)
                    .min(Comparator.comparing(ReserveEntity::getTime))
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
                            table.getLabel() + " " + table.getNumberSeats() + " "
                                    + stringService.declensionWords(table.getNumberSeats(), StringService.SEATS_WORDS)
                                    + " [" + table.getId() + "]"
                    ))))
            );
        }

        if (!tablesTime.isEmpty()) {
            for (var entry : tablesTime.entrySet()) {
                TableEntity table = entry.getValue();
                rows.add(new KeyboardRow(List.of(new KeyboardButton(
                        table.getLabel() + " " + table.getNumberSeats() + " "
                                + stringService.declensionWords(table.getNumberSeats(), StringService.SEATS_WORDS)
                                + ", освободить до " + entry.getKey().minusMinutes(10L) + " [" + table.getId() + "]"
                ))));
            }
        }

        rows.add(new KeyboardRow(List.of(new KeyboardButton(Button.CANCEL.getText()))));

        tablesKeyboard.setKeyboard(rows);
        tablesKeyboard.setResizeKeyboard(true);

        userService.updateSubState(user, SubState.BOOKING_CHOICE_TABLE_MANUALLY);

        return configureMarkdownMessage(chatId, "Выберите стол.\n\nСхема расположения столов: [\u200B](" + tavern.getLinkTableLayout() + ")", tablesKeyboard);
    }

    private SendMessage choiceTableAutomatic(UserEntity user, Long chatId, BookingDto booking) {
        TavernEntity tavern = booking.getTavern();

        List<TableEntity> tables = tavern.getTables().stream()
                .filter(table -> table.getNumberSeats() >= booking.getPersons())
                .sorted(Comparator.comparingInt(TableEntity::getNumberSeats))
                .collect(Collectors.toList());

        if (CollectionUtils.isEmpty(tables)) {
            return toMainMenu(user, tableFullMessage(booking));
        }

        for (TableEntity table : tables) {
            Set<ReserveEntity> reserves = table.getReserves().stream()
                    .filter(reserve -> reserve.getDate().equals(booking.getDate())
                            && reserve.getStatus() == ReserveStatus.ACTIVE)
                    .collect(Collectors.toSet());

            if (CollectionUtils.isEmpty(reserves)) {
                booking.setTable(table);

                userService.updateSubState(user, SubState.BOOKING_APPROVE);

                return configureMessage(chatId, fillReserveInfo(booking, true), KeyboardService.APPROVE_KEYBOARD);
            }
        }

        LocalTime time = booking.getTime();

        Map<LocalTime, TableEntity> tablesTime = new HashMap<>();
        for (TableEntity table : tables) {
            ReserveEntity foundReserve = table.getReserves().stream()
                    .filter(reserve -> reserve.getDate().equals(booking.getDate())
                            && reserve.getStatus() == ReserveStatus.ACTIVE)
                    .min(Comparator.comparing(ReserveEntity::getTime))
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

        return configureMessage(
                chatId,
                "В указанное вами время столик свободен до "
                        + maxTime.format(Constant.HH_MM_FORMATTER)
                        + ". Данный столик нужно будет освободить не позднее "
                        + maxTime.minusMinutes(10L) + ". Подтверждаете бронирование?",
                KeyboardService.APPROVE_BEFORE_KEYBOARD
        );
    }

    private String tableFullMessage(BookingDto booking) {
        return "К сожалению не удалось найти подходящий столик на "
                + booking.getPersons()
                + " "
                + stringService.declensionWords(booking.getPersons(), StringService.SEATS_WORDS)
                + " в выбранную дату и время.";
    }

    private String fillReserveInfo(BookingDto booking, boolean fillTavernName) {
        return "<b>Информация о бронировании</b>"
                + System.lineSeparator()
                + (fillTavernName ? "Заведение: <i>" + booking.getTavern().getName() + "</i>" + System.lineSeparator() : "")
                + "Дата: <i>" + booking.getDate().format(Constant.DD_MM_YYYY_FORMATTER) + "</i>"
                + System.lineSeparator()
                + "Время: <i>" + booking.getTime().format(Constant.HH_MM_FORMATTER) + "</i>"
                + System.lineSeparator()
                + "Кол-во персон: <i>" + booking.getPersons() + "</i>"
                + (booking.getBeforeTime() == null ? ""
                : System.lineSeparator()
                + "Примечание: <i>стол нужно будет освободить до " + booking.getBeforeTime().format(Constant.HH_MM_FORMATTER) + "</i>");
    }

    private SendMessage fillTaverns(UserEntity user) {
        Category category = bookings.get(user)
                .getCategory();

        if (category == null) {
            return toMainMenu(user, "Категория не выбрана.");
        }

        List<TavernEntity> taverns = tavernService.find(user.getCity(), category);

        if (CollectionUtils.isEmpty(taverns)) {
            return toMainMenu(user, "Не удалось найти заведения для выбранной категории.");
        }

        userService.updateSubState(user, SubState.CHOICE_TAVERN);

        final AtomicInteger counter = new AtomicInteger(0);

        List<KeyboardRow> keyboardRows = new ArrayList<>(
                taverns.stream()
                        .map(tavern -> new KeyboardButton(tavern.getName() + " [" + tavern.getId() + "]"))
                        .collect(Collectors.groupingBy(e -> counter.getAndIncrement() / 2))
                        .values())
                .stream()
                .map(KeyboardRow::new)
                .collect(Collectors.toList());

        keyboardRows.add(new KeyboardRow(List.of(new KeyboardButton(Button.RETURN_MAIN_MENU.getText()))));

        ReplyKeyboardMarkup keyboard = ReplyKeyboardMarkup.builder()
                .keyboard(keyboardRows)
                .build();

        keyboard.setResizeKeyboard(true);

        return configureMessage(user.getTelegramId(), "Выберите заведение.", keyboard);
    }

    private SendMessage toMainMenu(UserEntity user, String message) {
        bookings.remove(user);
        userService.updateState(user, State.BOOKING);
        return configureMessage(user.getTelegramId(), message, KeyboardService.USER_MAIN_MENU);
    }

    private void returnToMainMenu(UserEntity user) {
        userService.updateState(user, State.BOOKING);
    }

    private SendMessage configureDeleteReserve(UserEntity user, Long chatId) {
        Set<ReserveEntity> activeReserves = user.getReserves().stream()
                .filter(reserve -> reserve.getStatus() == ReserveStatus.ACTIVE)
                .collect(Collectors.toSet());

        if (CollectionUtils.isEmpty(activeReserves)) {
            return configureMessage(chatId, "Нечего отменять.", KeyboardService.USER_RESERVE_LIST_KEYBOARD);
        }

        userService.updateSubState(user, SubState.DELETE_RESERVE_CHOICE_TAVERN);

        ReplyKeyboardMarkup reservesKeyboard = new ReplyKeyboardMarkup();
        List<KeyboardRow> rows = new ArrayList<>();

        activeReserves.stream()
                .sorted(Comparator.comparing(o -> LocalDateTime.of(o.getDate(), o.getTime())))
                .filter(reserve -> reserve.getStatus() == ReserveStatus.ACTIVE)
                .map(reserve -> String.format(
                        "%s %s %s [%s]",
                        reserve.getDate().format(Constant.DD_MM_YYYY_FORMATTER),
                        reserve.getTime(),
                        reserve.getTable().getTavern().getName(),
                        reserve.getId()
                ))
                .forEach(reserve -> rows.add(new KeyboardRow(List.of(new KeyboardButton(reserve)))));

        rows.add(new KeyboardRow(List.of(new KeyboardButton(Button.CANCEL.getText()))));

        reservesKeyboard.setKeyboard(rows);
        reservesKeyboard.setResizeKeyboard(true);

        return configureMessage(chatId, "Выберите бронь, которую хотите отменить.", reservesKeyboard);
    }

    private String fillReserves(Set<ReserveEntity> reserves) {
        Set<ReserveEntity> activeReserves = reserves.stream()
                .filter(reserve -> reserve.getStatus() == ReserveStatus.ACTIVE)
                .collect(Collectors.toSet());

        if (CollectionUtils.isEmpty(activeReserves)) {
            return "У вас нет активных бронирований.";
        }

        return activeReserves.stream()
                .filter(reserve -> reserve.getStatus() == ReserveStatus.ACTIVE)
                .sorted(Comparator.comparing(o -> LocalDateTime.of(o.getDate(), o.getTime())))
                .map(reserve -> String.format(
                        """
                                ™️ <b>Заведение:</b> <i>%s</i>
                                \uD83D\uDDD3 <b>Дата и время:</b> <i>%s</i> в <i>%s</i>
                                \uD83D\uDC65 <b>Кол-во персон:</b> <i>%s</i>""",
                        reserve.getTable().getTavern().getName(),
                        reserve.getDate().format(Constant.DD_MM_YYYY_FORMATTER),
                        reserve.getTime(),
                        reserve.getNumberPeople()
                ))
                .collect(Collectors.joining("\n\n"));
    }
}
