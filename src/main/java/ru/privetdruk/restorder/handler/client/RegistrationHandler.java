package ru.privetdruk.restorder.handler.client;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import ru.privetdruk.restorder.handler.MessageHandler;
import ru.privetdruk.restorder.model.consts.MessageText;
import ru.privetdruk.restorder.model.entity.Tavern;
import ru.privetdruk.restorder.model.entity.UserEntity;
import ru.privetdruk.restorder.model.enums.SubState;
import ru.privetdruk.restorder.service.MessageService;

@RequiredArgsConstructor
@Component
public class RegistrationHandler implements MessageHandler {
    private final static int LAST_NAME_INDEX = 0;
    private final static int FIRST_NAME_INDEX = 1;
    private final static int MIDDLE_NAME_INDEX = 2;

    private final MessageService messageService;

    @Override
    public SendMessage handle(UserEntity user, Message message) {
        String messageText = message.getText();
        Long chatId = message.getChatId();
        SubState subState = user.getSubState();

        switch (subState) {
            case ENTER_FULL_NAME:
                if (!StringUtils.hasText(messageText)) {
                    return messageService.configureMessage(chatId, MessageText.ENTER_EMPTY_VALUE);
                }

                String[] fullName = messageText.split(" ");

                if (fullName.length == 1) {
                    return messageService.configureMessage(chatId, MessageText.FIRST_MIDDLE_NAME_IS_EMPTY);
                } else if (fullName.length == 2) {
                    return messageService.configureMessage(chatId, MessageText.MIDDLE_NAME_IS_EMPTY);
                }

                user.setLastName(fullName[LAST_NAME_INDEX]);
                user.setFirstName(fullName[FIRST_NAME_INDEX]);
                user.setMiddleName(fullName[MIDDLE_NAME_INDEX]);

                break;
            case ENTER_TAVERN_NAME:
                if (!StringUtils.hasText(messageText)) {
                    return messageService.configureMessage(chatId, MessageText.ENTER_EMPTY_VALUE);
                }

                Tavern tavern = Tavern.builder()
                        .name(messageText)
                        .owner(user)
                        .build();
        }

        SubState nextSubState = subState.getNextSubState();

        return messageService.configureMessage(chatId, nextSubState.getMessage());
    }
}
