package ru.privetdruk.restorder.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import reactor.core.publisher.Mono;
import ru.privetdruk.restorder.model.consts.MessageText;
import ru.privetdruk.restorder.model.dto.telegram.SendMessageResponse;
import ru.privetdruk.restorder.model.dto.telegram.UpdateWebhookResponse;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static reactor.core.scheduler.Schedulers.boundedElastic;

@Slf4j
@Service
@RequiredArgsConstructor
public class TelegramApiService {
    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    private final String URL = "url";
    private final String TELEGRAM_API_URL = "https://api.telegram.org";
    private final String BOT_TOKEN_PATH = "bot{token}";
    private final String SET_WEBHOOK_PATH = "/setWebhook";
    private final String SEND_MESSAGE_PATH = "/sendMessage";
    private final String CHAT_ID = "chat_id";
    private final String TEXT = "text";

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
                .queryParam(URL, webHookPath)
                .buildAndExpand(token)
                .toUriString();

        return webClient
                .get()
                .uri(updateWebhookUri)
                .retrieve()
                .bodyToMono(UpdateWebhookResponse.class);
    }

    /**
     * Отправить сообщение (в отдельном потоке)
     *
     * @param chatId        Идентификатор телеграм
     * @param text          Сообщение
     * @param client        Клиентский бот
     * @param replyKeyboard Keyboard
     */
    public void sendMessage(Long chatId, String text, boolean client, ReplyKeyboard replyKeyboard) {
        prepareSendMessage(chatId, text, client, replyKeyboard)
                .subscribeOn(boundedElastic())
                .subscribe();
    }

    /**
     * Отправить сообщение (в отдельном потоке)
     *
     * @param chatId Идентификатор телеграм
     * @param text   Сообщение
     * @param client Клиентский бот
     */
    public void sendMessage(Long chatId, String text, boolean client) {
        prepareSendMessage(chatId, text, client)
                .subscribeOn(boundedElastic())
                .subscribe();
    }

    /**
     * Подготовить отправку сообщения
     *
     * @param chatId        Идентификатор телеграм
     * @param text          Сообщение
     * @param client        Клиентский бот
     * @param replyKeyboard Keyboard
     * @return Результат отправки
     */
    public Mono<SendMessageResponse> prepareSendMessage(Long chatId, String text, boolean client, ReplyKeyboard replyKeyboard) {
        String uri = UriComponentsBuilder
                .fromHttpUrl(TELEGRAM_API_URL)
                .path(BOT_TOKEN_PATH)
                .path(SEND_MESSAGE_PATH)
                .queryParam(CHAT_ID, chatId)
                .queryParam(TEXT, text)
                .buildAndExpand(client ? botClientToken : botUserToken)
                .toUriString();

        WebClient.RequestBodySpec requestBodySpec = webClient.post()
                .uri(uri)
                .contentType(APPLICATION_JSON);

        if (replyKeyboard != null) {
            SendMessage sendMessage = new SendMessage();
            sendMessage.setReplyMarkup(replyKeyboard);

            try {
                requestBodySpec.bodyValue(objectMapper.writeValueAsString(sendMessage));
            } catch (JsonProcessingException exception) {
                log.error(MessageText.UNEXPECTED_ERROR, exception);
            }
        }

        return requestBodySpec
                .accept(APPLICATION_JSON)
                .retrieve()
                .bodyToMono(SendMessageResponse.class);
    }

    /**
     * Подготовить отправку сообщения
     *
     * @param chatId Идентификатор телеграм
     * @param text   Сообщение
     * @param client Клиентский бот
     * @return Результат отправки
     */
    public Mono<SendMessageResponse> prepareSendMessage(Long chatId, String text, boolean client) {
        String uri = UriComponentsBuilder
                .fromHttpUrl(TELEGRAM_API_URL)
                .path(BOT_TOKEN_PATH)
                .path(SEND_MESSAGE_PATH)
                .queryParam(CHAT_ID, chatId)
                .buildAndExpand(client ? botClientToken : botUserToken)
                .toUriString();

        WebClient.RequestBodySpec requestBodySpec = webClient.post()
                .uri(uri)
                .contentType(APPLICATION_JSON);

        SendMessage sendMessage = new SendMessage();
        sendMessage.setText(text);
        sendMessage.enableHtml(true);

        try {
            requestBodySpec.bodyValue(objectMapper.writeValueAsString(sendMessage));
        } catch (JsonProcessingException exception) {
            log.error(MessageText.UNEXPECTED_ERROR, exception);
        }

        return requestBodySpec
                .accept(APPLICATION_JSON)
                .retrieve()
                .bodyToMono(SendMessageResponse.class);
    }
}
