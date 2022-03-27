package ru.privetdruk.restorder.handler.client;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import ru.privetdruk.restorder.handler.MessageHandler;
import ru.privetdruk.restorder.model.entity.ReserveEntity;
import ru.privetdruk.restorder.model.entity.TableEntity;
import ru.privetdruk.restorder.model.entity.TavernEntity;
import ru.privetdruk.restorder.model.entity.UserEntity;
import ru.privetdruk.restorder.model.enums.Button;
import ru.privetdruk.restorder.model.enums.State;
import ru.privetdruk.restorder.model.enums.SubState;
import ru.privetdruk.restorder.service.KeyboardService;
import ru.privetdruk.restorder.service.MessageService;
import ru.privetdruk.restorder.service.UserService;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ReserveHandler implements MessageHandler {
    private final UserService userService;
    private final MessageService messageService;
    private final MainMenuHandler mainMenuHandler;

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
            case BACK, CANCEL, NO -> user.setSubState(subState.getParentSubState());
            case RETURN_MAIN_MENU -> {
                user.setState(State.MAIN_MENU);
                userService.updateSubState(user, SubState.VIEW_MAIN_MENU);

                return mainMenuHandler.handle(user, message, callback);
            }
        }

        // обновление состояния
        if (button != Button.BACK && button != Button.CANCEL && button != Button.NO) {
            switch (user.getSubState()) {
                case VIEW_MAIN_MENU -> {
                    if (button == Button.RESERVE_LIST) {
                        user.setState(State.RESERVE);
                        userService.updateSubState(user, SubState.VIEW_RESERVE_LIST);
                    }
                }
            }
        }

        // отрисовка меню
        return switch (user.getSubState()) {
            case VIEW_RESERVE_LIST -> messageService.configureMessage(chatId, fillReservesList(tavern.getTables()), KeyboardService.RESERVE_LIST_KEYBOARD);

            default -> new SendMessage();
        };
    }

    private String fillReservesList(Set<TableEntity> tables) {
        if (CollectionUtils.isEmpty(tables)) {
            return "Список бронирований пуст.";
        }

        Set<ReserveEntity> reserves = tables.stream()
                .map(TableEntity::getReserves)
                .flatMap(Collection::stream)
                .collect(Collectors.toSet());

        return reserves.stream()
                .map(reserve -> String.format(
                        "%s %s стол %s на %s персон 89102344977",
                        reserve.getDate(),
                        reserve.getTime(),
                        reserve.getTable().getLabel(),
                        reserve.getTable().getNumberSeats()
                ))
                .collect(Collectors.joining(System.lineSeparator()));
    }
}
