package ru.privetdruk.restorder.handler.client;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import ru.privetdruk.restorder.handler.MessageHandler;
import ru.privetdruk.restorder.model.entity.UserEntity;
import ru.privetdruk.restorder.model.enums.State;
import ru.privetdruk.restorder.model.enums.SubState;
import ru.privetdruk.restorder.service.KeyboardService;
import ru.privetdruk.restorder.service.TelegramApiService;
import ru.privetdruk.restorder.service.UserService;

import static ru.privetdruk.restorder.service.MessageService.configureMessage;

@Component
@RequiredArgsConstructor
public class AdminHandler implements MessageHandler {
    private final TelegramApiService telegramApiService;
    private final UserService userService;

    @Override
    public SendMessage handle(UserEntity user, Message message, CallbackQuery callback) {
        Long chatId = message.getChatId();

        if (user.getSubState() == SubState.APPROVE_TAVERN) {
            Long userId = Long.valueOf(callback.getData().split(" ")[1]);

            UserEntity foundUser = userService.findByTelegramId(userId)
                    .orElse(null);

            if (foundUser == null) {
                return configureMessageForAdmin(user, chatId, "Не удалось найти пользователя.");
            }

            if (foundUser.getSubState() != SubState.WAITING_APPROVE_APPLICATION) {
                return configureMessageForAdmin(user, chatId, "Заявка уже подтверждена.");
            }

            userService.updateState(foundUser, State.MAIN_MENU);

            telegramApiService.sendMessage(
                            foundUser.getTelegramId(),
                            "Добрый день. Ваша заявка подтверждена. Приятной работы!",
                            true,
                            KeyboardService.CLIENT_MAIN_MENU
                    )
                    .subscribe();

            return configureMessageForAdmin(user, chatId, "Заявка подтверждена.");
        }

        return configureMessage(chatId, "Что-то пошло не так...");
    }

    private SendMessage configureMessageForAdmin(UserEntity user, Long chatId, String s) {
        userService.updateState(user, State.MAIN_MENU);
        return configureMessage(chatId, s, KeyboardService.CLIENT_MAIN_MENU);
    }
}
