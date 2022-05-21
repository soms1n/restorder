package ru.privetdruk.restorder.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;

@Service
@RequiredArgsConstructor
public class MessageService {
    /**
     * Сконфигурировать ответное сообщение
     *
     * @param chatId  Идентификатор чата
     * @param message Сообщение
     * @return Ответное сообщение
     */
    public static SendMessage configureMessage(Long chatId, String message) {
        SendMessage sendMessage = new SendMessage(chatId.toString(), message);
        sendMessage.enableHtml(true);
        return sendMessage;
    }

    /**
     * Сконфигурировать ответное сообщение
     *
     * @param chatId  Идентификатор чата
     * @param message Сообщение
     * @return Ответное сообщение
     */
    public static SendMessage configureHtmlMessage(Long chatId, String message) {
        SendMessage sendMessage = new SendMessage(chatId.toString(), message);
        sendMessage.enableHtml(true);
        return sendMessage;
    }

    /**
     * Сконфигурировать ответное сообщение
     *
     * @param chatId   Идентификатор чата
     * @param message  Сообщение
     * @param keyboard Меню
     * @return Ответное сообщение
     */
    public static SendMessage configureMessage(Long chatId, String message, ReplyKeyboard keyboard) {
        SendMessage sendMessage = new SendMessage(chatId.toString(), message);
        sendMessage.setReplyMarkup(keyboard);
        sendMessage.enableHtml(true);
        return sendMessage;
    }

    /**
     * Сконфигурировать ответное сообщение
     *
     * @param chatId   Идентификатор чата
     * @param message  Сообщение
     * @param keyboard Меню
     * @return Ответное сообщение
     */
    public static SendMessage configureMarkdownMessage(Long chatId, String message, ReplyKeyboard keyboard) {
        SendMessage sendMessage = new SendMessage(chatId.toString(), message);
        sendMessage.setReplyMarkup(keyboard);
        sendMessage.enableMarkdown(true);
        return sendMessage;
    }

    /**
     * Распарсить идентификатор
     *
     * @param messageText Текст сообщения
     * @return Идентификатор
     */
    public Long parseId(String messageText) {
        Long id;

        try {
            id = Long.valueOf(messageText.split(" ")[1]);
        } catch (Throwable t) {
            id = null;
        }

        return id;
    }
}
