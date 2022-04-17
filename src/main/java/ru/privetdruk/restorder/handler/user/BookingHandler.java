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
import ru.privetdruk.restorder.model.entity.ReserveEntity;
import ru.privetdruk.restorder.model.entity.UserEntity;
import ru.privetdruk.restorder.model.enums.Button;
import ru.privetdruk.restorder.model.enums.ReserveStatus;
import ru.privetdruk.restorder.model.enums.State;
import ru.privetdruk.restorder.model.enums.SubState;
import ru.privetdruk.restorder.service.KeyboardService;
import ru.privetdruk.restorder.service.MessageService;
import ru.privetdruk.restorder.service.UserService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class BookingHandler implements MessageHandler {
    private final MessageService messageService;
    private final UserService userService;

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
                return configureDeleteReserve(user, chatId, KeyboardService.USER_RESERVE_LIST_KEYBOARD);
            }
        }

        // обновление/обработка состояния
        if (button != Button.BACK && button != Button.CANCEL && button != Button.NO) {
            switch (user.getSubState()) {
                case VIEW_MAIN_MENU -> {
                    if (button == Button.MY_RESERVE) {
                        userService.updateSubState(user, SubState.VIEW_RESERVE_LIST);
                    }
                }
                case DELETE_RESERVE_CHOICE_TAVERN -> {
                    Long id = parseId(messageText);
                    if (id == null) {
                        return messageService.configureMessage(chatId, MessageText.INCORRECT_VALUE_CANCELLED, KeyboardService.USER_MAIN_MENU);
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
            case VIEW_MAIN_MENU -> messageService.configureMessage(chatId, "Открываем главное меню...", KeyboardService.USER_MAIN_MENU);
            case VIEW_RESERVE_LIST -> messageService.configureMessage(chatId, fillReserves(user.getReserves()), KeyboardService.USER_RESERVE_LIST_KEYBOARD);

            default -> new SendMessage();
        };
    }

    private Long parseId(String messageText) {
        try {
            return Long.valueOf(messageText.substring(messageText.indexOf('[') + 1, messageText.indexOf(']')));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private void returnToMainMenu(UserEntity user) {
        userService.updateState(user, State.REGISTRATION_USER);
    }

    private SendMessage configureDeleteReserve(UserEntity user,
                                               Long chatId,
                                               ReplyKeyboardMarkup keyboard) {
        Set<ReserveEntity> activeReserves = user.getReserves().stream()
                .filter(reserve -> reserve.getStatus() == ReserveStatus.ACTIVE)
                .collect(Collectors.toSet());

        if (CollectionUtils.isEmpty(activeReserves)) {
            return messageService.configureMessage(chatId, "Нечего отменять.", keyboard);
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

        return messageService.configureMessage(chatId, "Выберите бронь, которую хотите отменить.", reservesKeyboard);
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
