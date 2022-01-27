package ru.privetdruk.restorder.handler.client;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import ru.privetdruk.restorder.handler.MessageHandler;
import ru.privetdruk.restorder.model.entity.UserEntity;

@RequiredArgsConstructor
@Component
public class WaitingApplicationApproveHandler implements MessageHandler {
    @Override
    public SendMessage handle(UserEntity user, Message message, CallbackQuery callback) {
        return null;
    }
}
