package ru.privetdruk.restorder.bot;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramWebhookBot;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.privetdruk.restorder.service.ClientBotService;

@Component
public class ClientBot extends TelegramWebhookBot {
    private final ClientBotService clientBotService;

    @Value("${bot.client.username}")
    private String username;

    @Value("${bot.client.token}")
    private String token;

    @Value("${bot.client.web-hook-path}")
    private String webHookPath;

    public ClientBot(ClientBotService clientBotService) {
        this.clientBotService = clientBotService;
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
        return clientBotService.handleUpdate(update);
    }

    @Override
    public String getBotPath() {
        return webHookPath;
    }
}
