package ru.privetdruk.restorder.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.MediaType;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;

import java.util.Set;

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

    public void sendMessageToUsers(Set<Long> telegramIds, String text, String botClientToken, @Nullable ReplyKeyboard replyKeyboard) {
        WebClient client = WebClient.create();

        telegramIds.forEach(chatId -> {
            WebClient.RequestBodySpec requestBodySpec = client.post()
                    .uri(uriBuilder -> UriComponentsBuilder
                            .newInstance()
                            .scheme("https")
                            .host("api.telegram.org")
                            .path("/bot" + botClientToken)
                            .path("/sendMessage")
                            .query("chat_id={chatId}")
                            .query("text={text}")
                            .buildAndExpand(chatId, text)
                            .encode()
                            .toUri()
                    )
                    .contentType(MediaType.APPLICATION_JSON);

            if (replyKeyboard != null) {
                SendMessage sendMessage = new SendMessage();
                sendMessage.setReplyMarkup(replyKeyboard);

                ObjectMapper objectMapper = new ObjectMapper();

                try {
                    String payload = objectMapper.writeValueAsString(sendMessage);
                    requestBodySpec.bodyValue(payload);
                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                }
            }

            requestBodySpec.accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .bodyToMono(String.class)
                    .subscribe();
        });
    }

    public void sendMessageToUser(Long telegramId, String text, String botClientToken) {
        sendMessageToUsers(Set.of(telegramId), text, botClientToken, null);
    }
}
