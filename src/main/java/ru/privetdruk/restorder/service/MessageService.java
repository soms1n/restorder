package ru.privetdruk.restorder.service;

import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;

@Service
public class MessageService {
    /**
     * Сконфигурировать ответное сообщение
     *
     * @param chatId  Идентификатор чата
     * @param message Сообщение
     * @return Ответное сообщение
     */
    public SendMessage configureMessage(Long chatId, String message) {
        return new SendMessage(chatId.toString(), message);
    }

    /**
     * Сконфигурировать ответное сообщение
     *
     * @param chatId   Идентификатор чата
     * @param message  Сообщение
     * @param keyboard Меню
     * @return Ответное сообщение
     */
    public SendMessage configureMessage(Long chatId, String message, ReplyKeyboardMarkup keyboard) {
        SendMessage sendMessage = new SendMessage(chatId.toString(), message);
        sendMessage.setReplyMarkup(keyboard);
        sendMessage.enableHtml(true);
        return sendMessage;
    }
}
