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
import ru.privetdruk.restorder.model.entity.*;
import ru.privetdruk.restorder.model.enums.*;
import ru.privetdruk.restorder.service.KeyboardService;
import ru.privetdruk.restorder.service.MessageService;
import ru.privetdruk.restorder.service.ReserveService;
import ru.privetdruk.restorder.service.UserService;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ReserveHandler implements MessageHandler {
    private final UserService userService;
    private final MessageService messageService;
    private final MainMenuHandler mainMenuHandler;
    private final ReserveService reserveService;

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
                    case ADD_RESERVE_CHOICE_DATE, ADD_RESERVE_CHOICE_TABLE -> {
                        return returnToMainMenu(user, message, callback);
                    }
                }

                user.setSubState(subState.getParentSubState());
            }
            case RETURN_MAIN_MENU -> {
                return returnToMainMenu(user, message, callback);
            }
            case DELETE_RESERVE -> {
                if (user.getSubState() == SubState.VIEW_RESERVE_LIST) {
                    return configureDeleteReserve(user, chatId);
                }
            }
        }

        // обновление состояния
        if (button != Button.BACK && button != Button.CANCEL && button != Button.NO) {
            switch (user.getSubState()) {
                case VIEW_MAIN_MENU -> {
                    if (button == Button.RESERVE_LIST) {
                        user.setState(State.RESERVE);
                        userService.updateSubState(user, SubState.VIEW_RESERVE_LIST);
                    } else if (button == Button.RESERVE) {
                        user.setState(State.RESERVE);
                        userService.updateSubState(user, SubState.ADD_RESERVE_CHOICE_DATE);
                        return messageService.configureMessage(chatId, "Введите дату резерва <i>(в формате дд.мм.гггг - 24.12.2001)</i>:", KeyboardService.TODAY_TOMORROW_CANCEL_KEYBOARD);
                    }
                }
                case DELETE_RESERVE_CHOICE_DATE -> {
                    LocalDate date;
                    try {
                        date = LocalDate.parse(messageText, Constant.DD_MM_YYYY_FORMATTER);
                    } catch (DateTimeParseException e) {
                        userService.updateSubState(user, user.getSubState().getParentSubState());
                        return messageService.configureMessage(chatId, "Вы ввели некорректное значение. Операция отменяется.", KeyboardService.RESERVE_LIST_KEYBOARD);
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
                        return messageService.configureMessage(chatId, "Нет резервов за выбранную дату.", KeyboardService.RESERVE_LIST_KEYBOARD);
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

                    return messageService.configureMessage(chatId, "Выберите резерв для удаления.", reservesDatesKeyboard);
                }
                case DELETE_RESERVE_CHOICE_TABLE -> {
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
                        }
                    }


                    userService.updateSubState(user, user.getSubState().getParentSubState());
                }
                case ADD_RESERVE_CHOICE_DATE -> {
                    LocalDate date;
                    if (button == Button.TODAY) {
                        date = LocalDate.now();
                    } else if (button == Button.TOMORROW) {
                        date = LocalDate.now().plusDays(1);
                    } else {
                        try {
                            date = LocalDate.parse(messageText, Constant.DD_MM_YYYY_FORMATTER);
                        } catch (DateTimeParseException e) {
                            return messageService.configureMessage(chatId, "Дата не соответствует формату. Повторите попытку.", KeyboardService.TODAY_TOMORROW_CANCEL_KEYBOARD);
                        }
                    }

                    ReserveEntity newReserve = new ReserveEntity();
                    newReserve.setDate(date);
                    newReserve.setManualMode(true);
                    newReserve.setUser(user);

                    addReservesTemporary.put(user, newReserve);

                    userService.updateSubState(user, SubState.ADD_RESERVE_CHOICE_TABLE);

                    return configureChoiceTable(chatId, tavern);
                }
                case ADD_RESERVE_CHOICE_TABLE -> {
                    String label = messageText.split(" ")[0];
                    TableEntity reserveTable = tavern.getTables().stream()
                            .filter(table -> table.getLabel().equals(label))
                            .findFirst()
                            .orElse(null);

                    if (reserveTable == null) {
                        return configureChoiceTable(chatId, tavern);
                    }

                    addReservesTemporary.get(user)
                            .setTable(reserveTable);
                }
            }
        }

        // отрисовка меню
        return switch (user.getSubState()) {
            case VIEW_RESERVE_LIST -> messageService.configureMessage(chatId, fillReservesList(tavern.getTables()), KeyboardService.RESERVE_LIST_KEYBOARD);

            default -> new SendMessage();
        };
    }

    private SendMessage configureChoiceTable(Long chatId, TavernEntity tavern) {
        ReplyKeyboardMarkup tablesKeyboard = new ReplyKeyboardMarkup();
        List<KeyboardRow> rows = new ArrayList<>();
        tavern.getTables().forEach(table ->
                rows.add(new KeyboardRow(List.of(new KeyboardButton(table.getLabel() + " " + table.getNumberSeats() + " мест"))))
        );

        rows.add(new KeyboardRow(List.of(new KeyboardButton(Button.CANCEL.getText()))));

        tablesKeyboard.setKeyboard(rows);
        tablesKeyboard.setResizeKeyboard(true);

        return messageService.configureMessage(chatId, "Выберите стол.", tablesKeyboard);
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
            return messageService.configureMessage(chatId, "Нечего удалять.", KeyboardService.RESERVE_LIST_KEYBOARD);
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

        return messageService.configureMessage(chatId, "Выберите дату, за которую хотите удалить резерв.", reservesDatesKeyboard);
    }

    private String fillReservesList(Set<TableEntity> tables) {
        if (CollectionUtils.isEmpty(tables)) {
            return "Список бронирований пуст.";
        }

        Map<LocalDate, List<ReserveEntity>> reserves = tables.stream()
                .map(TableEntity::getReserves)
                .flatMap(Collection::stream)
                .filter(reserve -> reserve.getStatus() == ReserveStatus.ACTIVE)
                .collect(Collectors.groupingBy(ReserveEntity::getDate));

        List<LocalDate> sortedDate = reserves.keySet().stream()
                .sorted(LocalDate::compareTo)
                .collect(Collectors.toList());

        LocalDate tomorrow = LocalDate.now().plusDays(1);

        StringBuilder reservesDescription = new StringBuilder();
        for (LocalDate date : sortedDate) {
            String reservesList = reserves.get(date).stream()
                    .sorted(Comparator.comparing(ReserveEntity::getTime, LocalTime::compareTo))
                    .map(reserve -> String.format(
                            "<i>%s</i> <b>%s</b> %s %s",
                            reserve.getTime(),
                            reserve.getTable().getLabel(),
                            reserve.getUser().getName(),
                            reserve.getUser().findContact(ContractType.MOBILE)
                                    .map(ContactEntity::getValue)
                                    .orElse("")
                    ))
                    .collect(Collectors.joining(System.lineSeparator()));

            reservesDescription
                    .append("<b>\uD83D\uDDD3 ")
                    .append(date.format(Constant.DD_MM_YYYY_FORMATTER))
                    .append("</b>")
                    .append(date.isEqual(LocalDate.now()) ? " (<i>сегодня</i>)" : "")
                    .append(date.isEqual(tomorrow) ? " (<i>завтра</i>)" : "")
                    .append(System.lineSeparator())
                    .append(reservesList)
                    .append(System.lineSeparator())
                    .append(System.lineSeparator());
        }

        return reservesDescription.toString();
    }
}
