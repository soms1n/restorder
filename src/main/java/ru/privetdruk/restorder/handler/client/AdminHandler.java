package ru.privetdruk.restorder.handler.client;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import reactor.core.scheduler.Schedulers;
import ru.privetdruk.restorder.handler.MessageHandler;
import ru.privetdruk.restorder.model.entity.UserEntity;
import ru.privetdruk.restorder.model.enums.Role;
import ru.privetdruk.restorder.model.enums.State;
import ru.privetdruk.restorder.model.enums.SubState;
import ru.privetdruk.restorder.model.enums.UserType;
import ru.privetdruk.restorder.service.KeyboardService;
import ru.privetdruk.restorder.service.TelegramApiService;
import ru.privetdruk.restorder.service.UserService;

import static ru.privetdruk.restorder.model.consts.MessageText.*;
import static ru.privetdruk.restorder.service.MessageService.configureMessage;

@Component
@RequiredArgsConstructor
public class AdminHandler implements MessageHandler {
    private final TelegramApiService telegramApiService;
    private final UserService userService;

    @Override
    public SendMessage handle(UserEntity admin, Message message, CallbackQuery callback) {
        Long chatId = message.getChatId();

        if (admin.getSubState() == SubState.APPROVE_TAVERN) {
            Long userId = Long.valueOf(callback.getData().split(" ")[1]);

            UserEntity client = userService.findByTelegramId(userId, UserType.CLIENT).orElse(null);

            if (client == null) {
                return configureMessageForAdmin(admin, chatId, USER_NOT_FOUND);
            }

            if (client.getSubState() != SubState.WAITING_APPROVE_APPLICATION) {
                return configureMessageForAdmin(admin, chatId, APPLICATION_ALREADY_CONFIRMED);
            }

            client.setRegistered(true);
            if (CollectionUtils.isEmpty(client.getRoles())) {
                client.getRoles().add(Role.CLIENT_ADMIN);
            }

            userService.updateState(client, State.MAIN_MENU);

            telegramApiService.sendMessage(
                            client.getTelegramId(),
                            "Добрый день. Ваша заявка подтверждена. Приятной работы!",
                            true,
                            KeyboardService.CLIENT_MAIN_MENU
                    )
                    .subscribeOn(Schedulers.boundedElastic())
                    .subscribe();

            return configureMessageForAdmin(admin, chatId, "Заявка подтверждена");
        }

        return configureMessage(chatId, SOMETHING_WENT_WRONG);
    }

    private SendMessage configureMessageForAdmin(UserEntity user, Long chatId, String message) {
        userService.updateState(user, State.MAIN_MENU);
        return configureMessage(chatId, message, KeyboardService.CLIENT_MAIN_MENU);
    }
}
