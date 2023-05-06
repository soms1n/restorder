package ru.privetdruk.restorder.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import reactor.core.publisher.Mono;
import ru.privetdruk.restorder.model.dto.telegram.SendMessageResponse;
import ru.privetdruk.restorder.model.dto.telegram.UpdateWebhookResponse;

@RequiredArgsConstructor
@Slf4j
@Service
public class TelegramApiService {
    public static final String TELEGRAM_API_URL = "https://api.telegram.org";
    public static final String BOT_TOKEN_PATH = "bot{token}";
    public static final String SET_WEBHOOK_PATH = "/setWebhook";
    public static final String SEND_MESSAGE_PATH = "/sendMessage";

    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    @Value("${bot.client.token}")
    private String botClientToken;

    @Value("${bot.user.token}")
    private String botUserToken;

    /**
     * Обновить webhook
     *
     * @return Результат обновления
     */
    public Mono<UpdateWebhookResponse> updateWebhook(String token, String webHookPath) {
        String updateWebhookUri = UriComponentsBuilder
                .fromHttpUrl(TELEGRAM_API_URL)
                .path(BOT_TOKEN_PATH)
                .path(SET_WEBHOOK_PATH)
                .queryParam("url", webHookPath)
                .buildAndExpand(token)
                .toUriString();

        return webClient
                .get()
                .uri(updateWebhookUri)
                .retrieve()
                .bodyToMono(UpdateWebhookResponse.class);
    }

    /**
     * Отправить сообщение
     *
     * @param chatId        Идентификатор телеграм
     * @param text          Сообщение
     * @param client        Клиентский бот
     * @param replyKeyboard Keyboard
     * @return Результат отправки
     */
    public Mono<SendMessageResponse> sendMessage(Long chatId, String text, boolean client, ReplyKeyboard replyKeyboard) {
        String uri = UriComponentsBuilder
                .fromHttpUrl(TELEGRAM_API_URL)
                .path(BOT_TOKEN_PATH)
                .path(SEND_MESSAGE_PATH)
                .queryParam("chat_id", chatId)
                .queryParam("text", text)
                .buildAndExpand(client ? botClientToken : botUserToken)
                .toUriString();

        WebClient.RequestBodySpec requestBodySpec = webClient.post()
                .uri(uri)
                .contentType(MediaType.APPLICATION_JSON);

        if (replyKeyboard != null) {
            SendMessage sendMessage = new SendMessage();
            sendMessage.setReplyMarkup(replyKeyboard);

            try {
                String payload = objectMapper.writeValueAsString(sendMessage);
                requestBodySpec.bodyValue(payload);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        }

        return requestBodySpec
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(SendMessageResponse.class);
    }

    /**
     * Отправить сообщение
     *
     * @param chatId Идентификатор телеграм
     * @param text   Сообщение
     * @param client Клиентский бот
     * @return Результат отправки
     */
    public Mono<SendMessageResponse> sendMessage(Long chatId, String text, boolean client) {
        String uri = UriComponentsBuilder
                .fromHttpUrl(TELEGRAM_API_URL)
                .path(BOT_TOKEN_PATH)
                .path(SEND_MESSAGE_PATH)
                .queryParam("chat_id", chatId)
                .buildAndExpand(client ? botClientToken : botUserToken)
                .toUriString();

        WebClient.RequestBodySpec requestBodySpec = webClient.post()
                .uri(uri)
                .contentType(MediaType.APPLICATION_JSON);

        SendMessage sendMessage = new SendMessage();
        sendMessage.setText(text);
        sendMessage.enableHtml(true);

        try {
            String payload = new ObjectMapper().writeValueAsString(sendMessage);
            requestBodySpec.bodyValue(payload);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        return requestBodySpec
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(SendMessageResponse.class);
    }
}
