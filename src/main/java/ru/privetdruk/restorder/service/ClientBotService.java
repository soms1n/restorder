package ru.privetdruk.restorder.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

@Slf4j
@Service
@RequiredArgsConstructor
public class ClientBotService {
    public SendMessage handleUpdate(Update update) {
        Message message = update.getMessage();

        if (message != null) {
            log.info("user: " + message.getFrom());
            log.info("message: " + message.getText());
        }

        return new SendMessage();
    }
}
