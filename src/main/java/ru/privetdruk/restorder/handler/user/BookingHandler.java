package ru.privetdruk.restorder.handler.user;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import ru.privetdruk.restorder.handler.MessageHandler;
import ru.privetdruk.restorder.model.consts.Constant;
import ru.privetdruk.restorder.model.entity.ReserveEntity;
import ru.privetdruk.restorder.model.entity.UserEntity;
import ru.privetdruk.restorder.model.enums.Button;
import ru.privetdruk.restorder.model.enums.SubState;
import ru.privetdruk.restorder.service.KeyboardService;
import ru.privetdruk.restorder.service.MessageService;
import ru.privetdruk.restorder.service.UserService;

import java.time.LocalDateTime;
import java.util.Comparator;
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

        // обновление состояния
        if (button != Button.BACK && button != Button.CANCEL && button != Button.NO) {
            switch (user.getSubState()) {
                case VIEW_MAIN_MENU -> {
                    if (button == Button.MY_RESERVE) {
                        userService.updateSubState(user, SubState.VIEW_RESERVE_LIST);
                    }
                }
            }
        }

        // отрисовка меню
        return switch (user.getSubState()) {
            case VIEW_MAIN_MENU -> messageService.configureMessage(chatId, "Открываем главное меню...", KeyboardService.USER_MAIN_MENU);
            case VIEW_RESERVE_LIST -> messageService.configureMessage(chatId, fillReserves(user.getReserves()), KeyboardService.USER_MAIN_MENU);

            default -> new SendMessage();
        };
    }

    private String fillReserves(Set<ReserveEntity> reserves) {
        if (CollectionUtils.isEmpty(reserves)) {
            return "Вы ничего не бронировали.";
        }

        return reserves.stream()
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
