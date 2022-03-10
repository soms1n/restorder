package ru.privetdruk.restorder.service;

import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

@Service
public class UserBotService {
    public SendMessage handleUpdate(Update update) {
        return null;
    }
}
