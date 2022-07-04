package ru.privetdruk.restorder.service.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.privetdruk.restorder.model.entity.UserEntity;
import ru.privetdruk.restorder.model.enums.*;
import ru.privetdruk.restorder.service.AbstractBotService;
import ru.privetdruk.restorder.service.BotHandler;
import ru.privetdruk.restorder.service.UserService;

import java.util.Optional;

@Slf4j
@Service
public class ClientBotService extends AbstractBotService {

    public ClientBotService(UserService userService,
                            @Qualifier("clientHandlerService") BotHandler handlerService) {
        super(userService, handlerService);
    }

    @Override
    public SendMessage handleUpdate(Update update) {
        ShortUpdate shortUpdate = ShortUpdate.builder()
                .callback(update.getCallbackQuery())
                .message(update.getMessage())
                .build();

        Optional<SendMessage> sendMessage = prepareUpdate(shortUpdate);

        if (sendMessage.isPresent()) {
            return sendMessage.get();
        }

        UserEntity user = userService.findByTelegramId(shortUpdate.getTelegramUserId(), UserType.CLIENT)
                .orElseGet(() -> userService.create(
                        shortUpdate.getTelegramUserId(),
                        State.REGISTRATION_TAVERN,
                        UserType.CLIENT
                ));

        State state = prepareState(user, shortUpdate);

        return getSendMessage(shortUpdate, user, handlers.get(state));
    }

    private State prepareState(UserEntity user, ShortUpdate shortUpdate) {
        if (!StringUtils.hasText(shortUpdate.getMessage().getText())) {
            return user.getState();
        }

        String[] messageSplit = shortUpdate.getMessage().getText().split(" ");
        Command command = Command.fromCommand(messageSplit[Command.MESSAGE_INDEX]);
        if (command == Command.START && messageSplit.length == 2) {
            return State.EVENT;
        } else if (user.isRegistered() && command == Command.MAIN_MENU) {
            userService.updateState(user, State.MAIN_MENU);
        }

        if (shortUpdate.getCallback() != null && StringUtils.hasText(shortUpdate.getCallback().getData())) {
            String[] callbackData = shortUpdate.getCallback().getData().split(" ");

            Button button = Button.fromName(callbackData[0]);
            if (button == Button.ACCEPT && callbackData.length == 2) {
                userService.update(user, State.ADMIN, SubState.APPROVE_TAVERN);
            }
        }

        return user.getState();
    }
}
