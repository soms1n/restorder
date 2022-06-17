package ru.privetdruk.restorder.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
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
        try {
            Matcher matcher = Pattern.compile("^ID: [0-9]+").matcher(messageText);

            if (matcher.find()) {
                return Long.valueOf(matcher.group().substring(matcher.group().indexOf(' ') + 1));
            } else {
                log.error(String.format("Parsing error, ID is not recognized. Source message text: %s", messageText));
                return null;
            }
        } catch (NumberFormatException e) {
            log.error(String.format("Parsing exception, ID is not recognized. Source message text: %s", messageText), e);
            return null;
        }
    }
}
