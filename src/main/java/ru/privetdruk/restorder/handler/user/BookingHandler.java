package ru.privetdruk.restorder.handler.user;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
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

import static ru.privetdruk.restorder.service.MessageService.configureMessage;

@Component
@RequiredArgsConstructor
public class BookingHandler implements MessageHandler {
    private final ReserveService reserveService;
    private final ScheduleService scheduleService;
    private final StringService stringService;
    private final TavernService tavernService;
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
            case BACK, CANCEL, NO -> user.setSubState(user.getSubState().getParentSubState());
            case RETURN_MAIN_MENU -> returnToMainMenu(user);
            case CANCEL_RESERVE -> {
                return configureDeleteReserve(user, chatId);
            }
        }

        // обновление/обработка состояния
        if (button != Button.BACK && button != Button.CANCEL && button != Button.NO) {
            switch (user.getSubState()) {
                case VIEW_MAIN_MENU -> {
                    switch (button) {
                        case MY_RESERVE -> userService.updateSubState(user, SubState.VIEW_RESERVE_LIST);
                        case HOOKAHS, RESTAURANTS -> {
                            Category category = button == Button.HOOKAHS ? Category.HOOKAH_BAR : Category.RESTAURANT;
                            bookings.put(user, new BookingDto(category));
                            userService.updateSubState(user, SubState.VIEW_TAVERN_LIST);
                        }
                    }
                }
                case VIEW_TAVERN -> {
                    if (button == Button.RESERVE) {
                        userService.updateSubState(user, SubState.BOOKING_CHOICE_DATE);

                        return configureMessage(chatId, "Введите дату в формате ДД.ММ.ГГГГ:", KeyboardService.BOOKING_CHOICE_DATE_KEYBOARD);
                    }
                }
                case BOOKING_CHOICE_DATE -> {
                    try {
                        LocalDate date = switch (button) {
                            case TODAY -> LocalDate.now();
                            case TOMORROW -> LocalDate.now().plusDays(1);
                            default -> LocalDate.parse(messageText, Constant.DD_MM_YYYY_FORMATTER);
                        };

                        bookings.get(user)
                                .setDate(date);

                        userService.updateSubState(user, SubState.BOOKING_CHOICE_TIME);

                        return configureMessage(chatId, "Введите время в формате ЧЧ:ММ", KeyboardService.BOOKING_CHOICE_TIME_KEYBOARD);
                    } catch (Throwable t) {
                        return configureMessage(
                                chatId,
                                "Введенная дата не соответствует формату. Пример - 05.09.2022. Повторите попытку:",
                                KeyboardService.BOOKING_CHOICE_DATE_KEYBOARD
                        );
                    }
                }
                case BOOKING_CHOICE_TIME -> {
                    try {
                        LocalTime time = LocalTime.parse(messageText);

                        bookings.get(user)
                                .setTime(time);

                        userService.updateSubState(user, SubState.BOOKING_CHOICE_PERSONS);

                        return configureMessage(chatId, "Введите кол-во персон:", KeyboardService.NUMBERS_KEYBOARD);
                    } catch (Throwable t) {
                        return configureMessage(
                                chatId,
                                "Введенное время не соответствует формату. Пример - 03:05. Повторите попытку:",
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

                        DayWeek dayWeek = DayWeek.fromDate(booking.getDate());

                        TavernEntity tavern = tavernService.findByIdWithSchedulesAndReserves(booking.getTavern().getId());

                        boolean isWork = tavern.getSchedules().stream()
                                .filter(schedule -> schedule.getDayWeek() == dayWeek)
                                .anyMatch(schedule -> !(booking.getTime().isBefore(schedule.getStartPeriod())
                                        || booking.getTime().isAfter(schedule.getEndPeriod())));

                        // TODO перенести на пару этапов выше с возвратом ввода новых значений
                        if (!isWork) {
                            return toMainMenu(user, "В выбранный день нельзя забронировать место.");
                        }

                        List<TableEntity> tables = tavern.getTables().stream()
                                .filter(table -> table.getNumberSeats() >= booking.getPersons())
                                .sorted(Comparator.comparingInt(TableEntity::getNumberSeats))
                                .collect(Collectors.toList());

                        if (CollectionUtils.isEmpty(tables)) {
                            return toMainMenu(
                                    user,
                                    "Не удалось найти подходящий столик для "
                                            + booking.getPersons()
                                            + " "
                                            + stringService.declensionWords(booking.getPersons(), StringService.SEATS_WORDS) + "."
                            );
                        }

                        TableEntity reservedTable = null;

                        for (TableEntity table : tables) {
                            Set<ReserveEntity> reserves = table.getReserves().stream()
                                    .filter(reserve -> reserve.getStatus() == ReserveStatus.ACTIVE)
                                    .collect(Collectors.toSet());

                            if (CollectionUtils.isEmpty(reserves)) {
                                reservedTable = table;
                                break;
                            }
                        }

                        if (reservedTable == null) {
                            return toMainMenu(user, "Все столы заняты.");
                        }

                        booking.setTable(reservedTable);

                        userService.updateSubState(user, SubState.BOOKING_APPROVE);

                        return configureMessage(chatId, "Информация о бронировании: заполнить.", KeyboardService.APPROVE_KEYBOARD);
                    } catch (Throwable t) {
                        return configureMessage(
                                chatId,
                                "Вы ввели некорректное значение. Повторите попытку.",
                                KeyboardService.NUMBERS_KEYBOARD
                        );
                    }
                }
                case BOOKING_APPROVE -> {
                    if (button == Button.APPROVE) {
                        BookingDto booking = bookings.get(user);

                        ReserveEntity reserve = new ReserveEntity();
                        reserve.setUser(user);
                        reserve.setDate(booking.getDate());
                        reserve.setTime(booking.getTime());
                        reserve.setTable(booking.getTable());
                        reserve.setNumberPeople(booking.getPersons());

                        reserveService.save(reserve);

                        return toMainMenu(user, "Вы успешно забронировали столик!");
                    }

                    return toMainMenu(user, "Операция отменена. Возврат в главное меню.");
                }
                case CHOICE_TAVERN -> {
                    Long tavernId = parseId(messageText);
                    if (tavernId == null) {
                        return toMainMenu(user, "Вы не выбрали заведение.");
                    }

                    TavernEntity tavern = tavernService.find(tavernId, user.getCity());
                    if (tavern == null) {
                        return toMainMenu(user, "Нельзя забронировать столик в выбранном заведении.");
                    }

                    bookings.get(user)
                            .setTavern(tavern);

                    userService.updateSubState(user, SubState.VIEW_TAVERN);

                    return configureMessage(chatId, fillTavernInfo(tavern), KeyboardService.TAVERN_INFO_KEYBOARD);
                }
                case DELETE_RESERVE_CHOICE_TAVERN -> {
                    Long id = parseId(messageText);
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
            case VIEW_MAIN_MENU -> configureMessage(chatId, "Открываем главное меню...", KeyboardService.USER_MAIN_MENU);
            case VIEW_RESERVE_LIST -> configureMessage(chatId, fillReserves(user.getReserves()), KeyboardService.USER_RESERVE_LIST_KEYBOARD);
            case VIEW_TAVERN_LIST -> fillTaverns(user);

            default -> new SendMessage();
        };
    }

    private String fillTavernInfo(TavernEntity tavern) {
        return "<b>™️" + tavern.getName() + "</b>"
                + System.lineSeparator()
                + System.lineSeparator()
                + scheduleService.fillSchedulesInfo(tavern.getSchedules());
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
        userService.updateState(user, State.BOOKING);
        return configureMessage(user.getTelegramId(), message, KeyboardService.USER_MAIN_MENU);
    }

    private Long parseId(String messageText) {
        try {
            return Long.valueOf(messageText.substring(messageText.indexOf('[') + 1, messageText.indexOf(']')));
        } catch (NumberFormatException e) {
            return null;
        }
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
                        "™️ <b>Заведение:</b> <i>%s</i>\n\uD83D\uDDD3 <b>Дата и время:</b> <i>%s</i> в <i>%s</i>\n\uD83D\uDC65 <b>Кол-во персон:</b> <i>%s</i>",
                        reserve.getTable().getTavern().getName(),
                        reserve.getDate().format(Constant.DD_MM_YYYY_FORMATTER),
                        reserve.getTime(),
                        reserve.getNumberPeople()
                ))
                .collect(Collectors.joining("\n\n"));
    }
}
