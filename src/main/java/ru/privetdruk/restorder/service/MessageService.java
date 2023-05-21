package ru.privetdruk.restorder.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import ru.privetdruk.restorder.model.enums.SubState;

import static ru.privetdruk.restorder.model.consts.Constant.LEFT_SQUARE_BRACKET;
import static ru.privetdruk.restorder.model.consts.Constant.RIGHT_SQUARE_BRACKET;

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
    public static SendMessage toMessage(Long chatId, String message) {
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
    public static SendMessage toMessage(Long chatId, String message, ReplyKeyboard keyboard) {
        SendMessage sendMessage = new SendMessage(chatId.toString(), message);
        sendMessage.setReplyMarkup(keyboard);
        sendMessage.enableHtml(true);
        return sendMessage;
    }

    /**
     * Сконфигурировать ответное сообщение
     *
     * @param chatId   Идентификатор чата
     * @param subState State сообщения
     * @param keyboard Меню
     * @return Ответное сообщение
     */
    public static SendMessage toMessage(Long chatId, SubState subState, ReplyKeyboard keyboard) {
        SendMessage sendMessage = new SendMessage(chatId.toString(), subState.getMessage());
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
     */
    public static void toMessage(SendMessage sendMessage, Long chatId, String message, ReplyKeyboard keyboard) {
        sendMessage.setChatId(chatId.toString());
        sendMessage.setText(message);
        sendMessage.setReplyMarkup(keyboard);
        sendMessage.enableHtml(true);
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
            return Long.valueOf(
                    messageText.substring(
                            messageText.indexOf(LEFT_SQUARE_BRACKET) + 1, messageText.indexOf(RIGHT_SQUARE_BRACKET)
                    )
            );
        } catch (Exception exception) {
            return null;
        }
    }
}
