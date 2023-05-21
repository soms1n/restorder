package ru.privetdruk.restorder.handler.client;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import ru.privetdruk.restorder.handler.MessageHandler;
import ru.privetdruk.restorder.model.consts.MessageText;
import ru.privetdruk.restorder.model.entity.UserEntity;
import ru.privetdruk.restorder.model.enums.UserType;
import ru.privetdruk.restorder.service.TelegramApiService;
import ru.privetdruk.restorder.service.UserService;

import static org.springframework.util.CollectionUtils.isEmpty;
import static ru.privetdruk.restorder.model.consts.Constant.SPACE;
import static ru.privetdruk.restorder.model.consts.MessageText.UNEXPECTED_ERROR;
import static ru.privetdruk.restorder.model.enums.Role.CLIENT_ADMIN;
import static ru.privetdruk.restorder.model.enums.State.MAIN_MENU;
import static ru.privetdruk.restorder.model.enums.SubState.APPROVE_TAVERN;
import static ru.privetdruk.restorder.model.enums.SubState.WAITING_APPROVE_APPLICATION;
import static ru.privetdruk.restorder.service.KeyboardService.CLIENT_MAIN_MENU_KEYBOARD;
import static ru.privetdruk.restorder.service.MessageService.toMessage;

@Component
@RequiredArgsConstructor
public class AdminHandler implements MessageHandler {
    private final TelegramApiService telegramApiService;
    private final UserService userService;

    @Override
    public SendMessage handle(UserEntity user, Message message, CallbackQuery callback) {
        Long chatId = message.getChatId();

        if (user.getSubState() == APPROVE_TAVERN) {
            Long userId = parseId(callback);

            UserEntity foundUser = userService.findByTelegramId(userId, UserType.CLIENT)
                    .orElse(null);

            if (foundUser == null) {
                return configureMessageForAdmin(user, chatId, MessageText.USER_NOT_FOUND);
            }

            if (foundUser.getSubState() != WAITING_APPROVE_APPLICATION) {
                return configureMessageForAdmin(user, chatId, MessageText.APPLICATION_ALREADY_APPROVED);
            }

            foundUser.setRegistered(true);
            if (isEmpty(foundUser.getRoles())) {
                foundUser.getRoles().add(CLIENT_ADMIN);
            }

            userService.updateState(foundUser, MAIN_MENU);

            telegramApiService.sendMessage(
                    foundUser.getTelegramId(),
                    MessageText.HI_APPLICATION_APPROVED,
                    true,
                    CLIENT_MAIN_MENU_KEYBOARD
            );

            return configureMessageForAdmin(user, chatId, MessageText.APPLICATION_APPROVED);
        }

        return toMessage(chatId, UNEXPECTED_ERROR);
    }

    private Long parseId(CallbackQuery callback) {
        return Long.valueOf(callback.getData().split(SPACE)[1]);
    }

    private SendMessage configureMessageForAdmin(UserEntity user, Long chatId, String message) {
        userService.updateState(user, MAIN_MENU);
        return toMessage(chatId, message, CLIENT_MAIN_MENU_KEYBOARD);
    }
}
