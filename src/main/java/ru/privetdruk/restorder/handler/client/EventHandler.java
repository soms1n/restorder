package ru.privetdruk.restorder.handler.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import ru.privetdruk.restorder.handler.MessageHandler;
import ru.privetdruk.restorder.model.consts.MessageText;
import ru.privetdruk.restorder.model.entity.EventEntity;
import ru.privetdruk.restorder.model.entity.TavernEntity;
import ru.privetdruk.restorder.model.entity.UserEntity;
import ru.privetdruk.restorder.model.enums.Command;
import ru.privetdruk.restorder.model.enums.EventType;
import ru.privetdruk.restorder.model.enums.JsonbKey;
import ru.privetdruk.restorder.model.enums.Role;
import ru.privetdruk.restorder.service.EventService;
import ru.privetdruk.restorder.service.TavernService;
import ru.privetdruk.restorder.service.UserService;

import java.time.LocalDateTime;
import java.util.UUID;

import static java.lang.String.format;
import static ru.privetdruk.restorder.model.consts.Constant.SPACE;
import static ru.privetdruk.restorder.model.consts.MessageText.UNEXPECTED_ERROR;
import static ru.privetdruk.restorder.service.MessageService.toMessage;
import static ru.privetdruk.restorder.model.consts.MessageText.SOMETHING_WENT_WRONG;
import static ru.privetdruk.restorder.model.consts.MessageText.SUPPORT_MESSAGE;

@Component
@RequiredArgsConstructor
public class EventHandler implements MessageHandler {
    private final EventService eventService;
    private final ObjectMapper objectMapper;
    private final TavernService tavernService;
    private final UserService userService;
    private final RegistrationEmployeeHandler registrationEmployeeHandler;

    @Override
    public SendMessage handle(UserEntity user, Message message, CallbackQuery callback) {
        Long chatId = message.getChatId();

        String[] messageSplit = message.getText().split(SPACE);
        Command command = Command.fromCommand(messageSplit[Command.MESSAGE_INDEX]);

        if (command == Command.HELP) {
            return toMessage(chatId, MessageText.SUPPORT_MESSAGE);
        }

        if (command != Command.START || messageSplit.length != 2) {
            return toMessage(chatId, SOMETHING_WENT_WRONG);
        }

        EventEntity event = eventService.find(parseEventUuid(messageSplit));

        if (event == null) {
            return toMessage(chatId, SOMETHING_WENT_WRONG);
        }

        if (!event.getAvailable() || LocalDateTime.now().isAfter(event.getExpirationDate())) {
            return toMessage(chatId, MessageText.LINK_NOT_AVAILABLE);
        }

        EventType eventType = event.getType();
        if (eventType == EventType.REGISTER_EMPLOYEE) {
            Long tavernId = objectMapper.convertValue(event.getParams().get(JsonbKey.TAVERN_ID.getKey()), Long.TYPE);
            TavernEntity tavern = tavernService.findByIdWithEmployee(tavernId)
                    .orElse(null);

            if (tavern == null) {
                eventService.complete(event);

                return toMessage(chatId, MessageText.LINK_IS_INVALID);
            }

            if (checkOwner(user, tavern)) {
                return toMessage(chatId, format(MessageText.YOU_ALREADY_HAVE_TAVERN, tavern.getName()));
            }

            if (CollectionUtils.isEmpty(user.getRoles()) || user.getTavern() == null) {
                user.getRoles().clear();
                user.addRole(Role.CLIENT_EMPLOYEE);
                user.setRegistered(true);
                user.setState(eventType.getState());
                user.setSubState(eventType.getSubState());
                user.setTavern(tavern);
                user = userService.save(user);

                eventService.complete(event);

                return registrationEmployeeHandler.handle(user, message, callback);
            } else if (checkOwner(user, tavern)) {
                return toMessage(chatId, format(MessageText.YOU_ALREADY_HAVE_TAVERN, tavern.getName()));
            }
        }

        return toMessage(chatId, UNEXPECTED_ERROR);
    }

    private UUID parseEventUuid(String[] messageSplit) {
        return UUID.fromString(messageSplit[Command.MESSAGE_EVENT_ID_INDEX]);
    }

    private boolean checkOwner(UserEntity user, TavernEntity tavern) {
        return user.getTavern() != null && user.getTavern() != tavern;
    }
}
