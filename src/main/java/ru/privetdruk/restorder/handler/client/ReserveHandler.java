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

import static java.lang.String.format;
import static java.lang.System.lineSeparator;
import static java.util.Optional.ofNullable;
import static org.springframework.util.CollectionUtils.isEmpty;
import static ru.privetdruk.restorder.model.consts.Constant.*;
import static ru.privetdruk.restorder.model.consts.MessageText.NOTIFY_USER_RESERVE_CANCELLED;
import static ru.privetdruk.restorder.service.KeyboardService.*;
import static ru.privetdruk.restorder.service.MessageService.toMessage;

@Component
@RequiredArgsConstructor
public class ReserveHandler implements MessageHandler {
    private final BlacklistService blacklistService;
    private final BlacklistSettingService blacklistSettingService;
    private final ContactService contactService;
    private final InfoService infoService;
    private final MessageService messageService;
    private final MainMenuHandler mainMenuHandler;
    private final NotificationService notificationService;
    private final ReserveService reserveService;
    private final StringService stringService;
    private final TavernService tavernService;
    private final TelegramApiService telegramApiService;
    private final UserService userService;
    private final ValidationService validationService;
    private final VisitingService visitingService;

    private final Map<UserEntity, ReserveEntity> addReservesCache = new HashMap<>();
    private final Map<UserEntity, ReserveDto> completionReservesCache = new HashMap<>();

    private final String BUSY_BY = "занято с ";
    private final String DATE_DAY_RESERVES = """
            <b>\uD83D\uDDD3 %s</b> %s
            %s
                                
            """;
    private final String LABEL_SEATS_WORD_BUSY = "%s на %s %s %s";
    private final String TIME_LABEL_PERSONS_NAME_PHONE = "<i>%s</i> <b>%s</b> %s %s %s";
    private final String TIME_TABLE_USER_ID = "%s %s %s [%s]";

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
                return toMessage(chatId, MessageText.TAVERN_SET_UP + validate.printMessages(), CLIENT_MAIN_MENU_KEYBOARD);
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
        if (button != Button.CANCEL && (button != Button.NO || isNoExclusion(subState))) {
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
                        return toMessage(chatId, MessageText.INCORRECT_VALUE_CANCELLED, RESERVE_LIST_KEYBOARD);
                    }

                    List<ReserveEntity> reserves = reserveService.findActiveByTavernWithTableUser(user.getTavern(), date).stream()
                            .sorted(Comparator.comparing(ReserveEntity::getTime))
                            .toList();

                    if (isEmpty(reserves)) {
                        userService.updateSubState(user, user.getSubState().getParentSubState());
                        return toMessage(chatId, MessageText.RESERVES_NOT_FOUND, RESERVE_LIST_KEYBOARD);
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
                            rows.add(new KeyboardRow(List.of(new KeyboardButton(format(
                                    TIME_TABLE_USER_ID,
                                    reserve.getTime(),
                                    reserve.getTable().getLabel(),
                                    reserve.getName(),
                                    reserve.getId()
                            )))))
                    );

                    rows.add(new KeyboardRow(List.of(new KeyboardButton(Button.CANCEL.getText()))));

                    reservesDatesKeyboard.setKeyboard(rows);
                    reservesDatesKeyboard.setResizeKeyboard(true);

                    return toMessage(chatId, MessageText.CHOICE_RESERVE_FOR_CONFIRM, reservesDatesKeyboard);
                }
                case DELETE_RESERVE_CHOICE_TABLE -> {
                    ReserveDto reserve = completionReservesCache.get(user);

                    if (button == Button.PICK_ALL) {
                        userService.updateSubState(user, user.getSubState().getParentSubState());

                        if (reserve.getDate() != null) {
                            List<ReserveEntity> reserves = reserveService.findActiveByTavern(user.getTavern(), reserve.getDate());

                            reserveService.updateStatus(reserves, ReserveStatus.COMPLETED);

                            completionReservesCache.remove(user);

                            String textMessage = MessageText.ALL_RESERVES_WILL_BE_CONFIRM + fillReservesList(user.getTavern());

                            return toMessage(chatId, textMessage, RESERVE_LIST_KEYBOARD);
                        }
                    }

                    Long reserveId = messageService.parseId(messageText);
                    if (reserveId == null) {
                        userService.updateSubState(user, user.getSubState().getParentSubState());
                    } else {
                        reserve.setId(reserveId);

                        userService.updateSubState(user, SubState.CONFIRM_CLIENT_RESERVE);

                        return toMessage(chatId, MessageText.IS_CLIENT_HERE, YES_NO_KEYBOARD);
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

                                String text = format(
                                        MessageText.DOES_BLOCK_USER,
                                        visiting.getTimes(),
                                        stringService.declensionTimes(visiting.getTimes())
                                );

                                return toMessage(chatId, text, YES_NO_KEYBOARD);
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
                        LocalDateTime unlockDate = blacklistService.calculateUnlockDate(reserveCache.getBlockDays());

                        BlacklistEntity blacklist = BlacklistEntity.builder()
                                .tavern(user.getTavern())
                                .user(reserveUser)
                                .phoneNumber(reserveCache.getPhoneNumber())
                                .unlockDate(unlockDate)
                                .reason(Button.DOESNT_COME.getText())
                                .build();

                        blacklistService.save(blacklist);

                        notificationService.notifyBlockUser(blacklist);
                    }

                    return completionReserve(user, chatId, reserveCache.getReserve());
                }
                case ADD_RESERVE_CHOICE_DATE -> {
                    LocalDate now = LocalDate.now();
                    LocalDate date;

                    switch (button) {
                        case TODAY -> date = now;
                        case TOMORROW -> date = now.plusDays(1);
                        default -> {
                            try {
                                date = LocalDate.parse(messageText, Constant.DD_MM_YYYY_WITHOUT_DOT_FORMATTER);
                            } catch (DateTimeParseException exception) {
                                return toMessage(chatId, MessageText.INCORRECT_DATE_RETRY, TODAY_TOMORROW_CANCEL_KEYBOARD);
                            }
                        }
                    }

                    if (date.isBefore(now)) {
                        return toMessage(chatId, MessageText.INCORRECT_MORE_DATE_RETRY, TODAY_TOMORROW_CANCEL_KEYBOARD);
                    }

                    ReserveEntity newReserve = new ReserveEntity();
                    newReserve.setDate(date);

                    addReservesCache.put(user, newReserve);

                    userService.updateSubState(user, SubState.ADD_RESERVE_CHOICE_TABLE);

                    return configureChoiceTable(user, chatId);
                }
                case ADD_RESERVE_CHOICE_TABLE -> {
                    TavernEntity tavern = tavernService.findWithTables(user.getTavern());

                    String label = messageText.split(Constant.SPACE)[Constant.FIRST_INDEX];
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
                            return toMessage(chatId, MessageText.INCORRECT_MORE_TIME_RETRY, TODAY_TOMORROW_CANCEL_KEYBOARD);
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
                        } catch (NumberFormatException exception) {
                            return configureChoicePersons(chatId);
                        }
                    }

                    addReservesCache.get(user)
                            .setNumberPeople(numberPeople);

                    userService.updateSubState(user, SubState.ADD_RESERVE_CHOICE_NAME);

                    return toMessage(chatId, MessageText.ENTER_NAME, CANCEL_KEYBOARD);
                }
                case ADD_RESERVE_CHOICE_NAME -> {
                    addReservesCache.get(user)
                            .setName(messageText);

                    userService.updateSubState(user, SubState.ADD_RESERVE_CHOICE_PHONE);

                    return toMessage(chatId, MessageText.ENTER_PHONE_NUMBER, KeyboardService.WITHOUT_PHONE_KEYBOARD);
                }
                case ADD_RESERVE_CHOICE_PHONE -> {
                    if (button != Button.WITHOUT_PHONE) {
                        String phoneNumber = contactService.preparePhoneNumber(messageText);

                        if (validationService.isNotValidPhone(phoneNumber)) {
                            return toMessage(chatId, MessageText.INCORRECT_PHONE_NUMBER, KeyboardService.WITHOUT_PHONE_KEYBOARD);
                        }

                        ReserveEntity reserve = addReservesCache.get(user);
                        reserve.setPhoneNumber(phoneNumber);
                        reserve.setUser(userService.findByPhoneNumberFromUser(phoneNumber));
                    }

                    userService.updateSubState(user, SubState.ADD_RESERVE_INFO);

                    return toMessage(chatId, infoService.fillReserveInfo(addReservesCache.get(user)), KeyboardService.APPROVE_KEYBOARD);
                }
                case ADD_RESERVE_INFO -> {
                    user.setState(State.MAIN_MENU);
                    userService.updateSubState(user, SubState.VIEW_MAIN_MENU);

                    if (button == Button.ACCEPT) {
                        ReserveEntity reserve = addReservesCache.get(user);
                        reserveService.save(reserve);
                        addReservesCache.remove(user);

                        return toMessage(chatId, MessageText.RESERVE_TABLE_SUCCESS, CLIENT_MAIN_MENU_KEYBOARD);
                    }

                    return toMessage(chatId, MessageText.RESERVE_TABLE_ERROR, CLIENT_MAIN_MENU_KEYBOARD);
                }
            }
        }

        // отрисовка меню
        return switch (user.getSubState()) {
            case VIEW_RESERVE_LIST -> toMessage(chatId, fillReservesList(user.getTavern()), RESERVE_LIST_KEYBOARD);

            default -> new SendMessage();
        };
    }

    private boolean isNoExclusion(SubState subState) {
        return subState == SubState.CONFIRM_CLIENT_RESERVE || subState == SubState.BLOCK_CLIENT_RESERVE;
    }

    private SendMessage completionReserve(UserEntity user, Long chatId, ReserveEntity reserve) {
        UserEntity reserveUser = reserve.getUser();

        reserveService.updateStatus(reserve, ReserveStatus.COMPLETED);

        if (reserveUser != null) {
            telegramApiService.sendMessage(
                    reserveUser.getTelegramId(),
                    format(
                            NOTIFY_USER_RESERVE_CANCELLED,
                            reserve.getDate().format(DD_MM_YYYY_FORMATTER),
                            reserve.getTime().format(HH_MM_FORMATTER),
                            user.getTavern().getName()
                    ),
                    false
            );
        }

        String textMessage = MessageText.RESERVE_CONFIRMED + fillReservesList(user.getTavern());

        return toMessage(chatId, textMessage, RESERVE_LIST_KEYBOARD);
    }

    private SendMessage configureAddReserve(UserEntity user, Long chatId) {
        userService.update(user, State.RESERVE, SubState.ADD_RESERVE_CHOICE_DATE);

        return toMessage(chatId, MessageText.ENTER_RESERVE_DATE, TODAY_TOMORROW_CANCEL_KEYBOARD);
    }

    private SendMessage configureChoiceTime(Long chatId) {
        return toMessage(chatId, MessageText.ENTER_RESERVE_TIME, KeyboardService.RESERVE_CHOICE_TIME_KEYBOARD);
    }

    private SendMessage configureChoicePersons(Long chatId) {
        return toMessage(chatId, MessageText.ENTER_OR_CHOICE_PERSONS, KeyboardService.NUMBERS_CANCEL_KEYBOARD);
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
                            String reserveTimes = reserveService.findActiveByTable(table, date).stream()
                                    .map(ReserveEntity::getTime)
                                    .sorted(Comparator.naturalOrder())
                                    .map(time -> time.format(HH_MM_FORMATTER))
                                    .collect(Collectors.joining(Constant.COMMA));

                            String foundReserve = BUSY_BY + reserveTimes;

                            rows.add(new KeyboardRow(List.of(new KeyboardButton(format(
                                    LABEL_SEATS_WORD_BUSY,
                                    table.getLabel(),
                                    table.getNumberSeats(),
                                    stringService.declensionWords(table.getNumberSeats(), StringService.SEATS_WORDS),
                                    StringUtils.hasLength(reserveTimes) ? foundReserve : EMPTY_STRING
                            )))));
                        }
                );

        rows.add(new KeyboardRow(List.of(new KeyboardButton(Button.CANCEL.getText()))));

        tablesKeyboard.setKeyboard(rows);
        tablesKeyboard.setResizeKeyboard(true);

        return toMessage(chatId, MessageText.ENTER_OR_CHOICE_LABEL, tablesKeyboard);
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
            return toMessage(chatId, MessageText.ACTIVE_RESERVES_NOT_FOUND, RESERVE_LIST_KEYBOARD);
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

        return toMessage(chatId, MessageText.CHOICE_RESERVE_DATE_FOR_CONFIRMED, reservesDatesKeyboard);
    }

    private String fillReservesList(TavernEntity tavern) {
        Map<LocalDate, List<ReserveEntity>> groupingReserves = reserveService.findActiveByTavernWithTableUser(tavern).stream()
                .collect(Collectors.groupingBy(ReserveEntity::getDate));

        if (isEmpty(groupingReserves)) {
            return MessageText.RESERVES_EMPTY;
        }

        List<LocalDate> sortedDate = groupingReserves.keySet().stream()
                .sorted(LocalDate::compareTo)
                .toList();

        StringBuilder reservesDescription = new StringBuilder();
        for (LocalDate date : sortedDate) {
            String reservesList = groupingReserves.get(date).stream()
                    .sorted(Comparator.comparing(ReserveEntity::getTime, LocalTime::compareTo))
                    .map(reserve -> {
                        String userName = reserve.getUser() == null ? ofNullable(reserve.getName())
                                .orElse(EMPTY_STRING)
                                : reserve.getUser().getName();

                        String phoneNumber = reserve.getUser() == null ? ofNullable(reserve.getPhoneNumber())
                                .orElse(EMPTY_STRING)
                                : reserve.getUser().findContact(ContractType.MOBILE)
                                .map(ContactEntity::getValue)
                                .orElse(EMPTY_STRING);

                        return format(
                                TIME_LABEL_PERSONS_NAME_PHONE,
                                reserve.getTime().format(HH_MM_FORMATTER),
                                reserve.getTable().getLabel(),
                                reserve.getNumberPeople(),
                                userName,
                                phoneNumber
                        );
                    })
                    .collect(Collectors.joining(lineSeparator()));

            reservesDescription.append(format(DATE_DAY_RESERVES, date.format(DD_MM_YYYY_FORMATTER), defineDay(date), reservesList));
        }

        return reservesDescription.toString();
    }

    private String defineDay(LocalDate date) {
        LocalDate now = LocalDate.now();

        if (date.isEqual(now)) {
            return Constant.TODAY;
        } else if (date.isEqual(now.plusDays(1))) {
            return Constant.TOMORROW;
        } else {
            return EMPTY_STRING;
        }
    }
}
