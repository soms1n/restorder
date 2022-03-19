package ru.privetdruk.restorder.bot;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramWebhookBot;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.privetdruk.restorder.service.TelegramApiService;
import ru.privetdruk.restorder.service.UserBotService;

import javax.annotation.PostConstruct;


@RequiredArgsConstructor
@Slf4j
public class UserBot extends TelegramWebhookBot {
    private final UserBotService userBotService;
    private final TelegramApiService telegramApiService;

    @Value("${bot.user.username}")
    private String username;

    @Value("${bot.user.token}")
    private String token;

    @Value("${bot.user.web-hook-path}")
    private String webHookPath;

    @Override
    public String getBotToken() {
        return token;
    }

    @Override
    public BotApiMethod<?> onWebhookUpdateReceived(Update update) {
        try {
            return userBotService.handleUpdate(update);
        } catch (Throwable t) {
            log.error(t.getMessage(), t);
            return new SendMessage();
        }
    }

    @PostConstruct
    private void postConstruct() {
        telegramApiService.updateWebhook(token, webHookPath)
                .subscribe(response -> log.info(response.toString()));
    }

    @Override
    public String getBotPath() {
        return webHookPath;
    }

    @Override
    public String getBotUsername() {
        return username;
    }
}
