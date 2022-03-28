package ru.privetdruk.restorder.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Update;
import reactor.core.publisher.Mono;
import ru.privetdruk.restorder.bot.ClientBot;
import ru.privetdruk.restorder.bot.UserBot;

@RestController
public class WebHookController {
    private final ClientBot clientBot;
    private final UserBot userBot;

    @Value("${bot.user.enabled}")
    private boolean userBotEnabled;

    public boolean isUserBotEnabled() {
        return userBotEnabled;
    }

    public WebHookController(ClientBot clientBot, UserBot userBot) {
        this.clientBot = clientBot;
        this.userBot = userBot;
    }

    @PostMapping(value = "/")
    public Mono<BotApiMethod<?>> onUpdateReceived(@RequestBody Update update) {
        if (isUserBotEnabled()) {
            return Mono.just(userBot.onWebhookUpdateReceived(update));
        } else {
            return Mono.just(clientBot.onWebhookUpdateReceived(update));
        }
    }
}
