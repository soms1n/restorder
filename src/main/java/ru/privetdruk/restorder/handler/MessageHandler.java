package ru.privetdruk.restorder.handler;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import ru.privetdruk.restorder.model.entity.UserEntity;

public interface MessageHandler {
    SendMessage handle(UserEntity user, Message message);
}
