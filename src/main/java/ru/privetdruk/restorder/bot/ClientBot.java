package ru.privetdruk.restorder.bot;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramWebhookBot;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.privetdruk.restorder.service.TelegramApiService;
import ru.privetdruk.restorder.service.client.ClientBotService;

import javax.annotation.PostConstruct;

@Slf4j
@Component
public class ClientBot extends TelegramWebhookBot {
    private final ClientBotService clientBotService;
    private final TelegramApiService telegramApiService;

    @Value("${bot.client.username}")
    private String username;

    @Value("${bot.client.token}")
    private String token;

    @Value("${bot.client.web-hook-path}")
    private String webHookPath;

    public ClientBot(ClientBotService clientBotService, TelegramApiService telegramApiService) {
        this.clientBotService = clientBotService;
        this.telegramApiService = telegramApiService;
    }

    @PostConstruct
    private void postConstruct() {
        telegramApiService.updateWebhook(token, webHookPath)
                .subscribe(response -> log.info(response.toString()));
    }

    @Override
    public String getBotUsername() {
        return username;
    }

    @Override
    public String getBotToken() {
        return token;
    }

    @Override
    public BotApiMethod<?> onWebhookUpdateReceived(Update update) {
        try {
            return clientBotService.handleUpdate(update);
        } catch (Throwable t) {
            log.error(t.getMessage(), t);
            return new SendMessage();
        }
    }

    @Override
    public String getBotPath() {
        return webHookPath;
    }
}
