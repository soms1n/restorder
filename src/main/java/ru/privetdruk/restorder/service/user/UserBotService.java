package ru.privetdruk.restorder.service.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.privetdruk.restorder.model.entity.UserEntity;
import ru.privetdruk.restorder.model.enums.Command;
import ru.privetdruk.restorder.model.enums.Role;
import ru.privetdruk.restorder.model.enums.State;
import ru.privetdruk.restorder.model.enums.UserType;
import ru.privetdruk.restorder.service.AbstractBotService;
import ru.privetdruk.restorder.service.BotHandler;
import ru.privetdruk.restorder.service.UserService;

import java.util.Optional;

@Service
@Slf4j
public class UserBotService extends AbstractBotService {

    public UserBotService(UserService userService,
                          @Qualifier("userHandlerService") BotHandler handlerService) {
        super(userService, handlerService);
    }

    public SendMessage handleUpdate(Update update) {
        ShortUpdate shortUpdate = ShortUpdate.builder()
                .callback(update.getCallbackQuery())
                .message(update.getMessage())
                .build();

        Optional<SendMessage> sendMessage = prepareUpdate(shortUpdate);

        if (sendMessage.isPresent()) {
            return sendMessage.get();
        }

        UserEntity user = userService.findByTelegramId(shortUpdate.getTelegramUserId(), UserType.USER)
                .orElseGet(() -> userService.create(
                        shortUpdate.getTelegramUserId(),
                        State.REGISTRATION_USER,
                        Role.USER,
                        UserType.USER
                ));

        prepareState(shortUpdate.getMessage(), user);

        return getSendMessage(shortUpdate, user, handlers.get(user.getState()));
    }

    private void prepareState(Message message, UserEntity user) {
        if (!user.isRegistered() && user.getState() != State.REGISTRATION_USER) {
            userService.updateState(user, State.REGISTRATION_USER);
            return;
        }

        if (user.isRegistered()) {
            String[] messageSplit = message.getText().split(" ");
            Command command = Command.fromCommand(messageSplit[Command.MESSAGE_INDEX]);
            if (command == Command.MAIN_MENU) {
                userService.updateState(user, State.BOOKING);
            }
        }
    }
}
