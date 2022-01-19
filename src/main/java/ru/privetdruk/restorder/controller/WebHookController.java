package ru.privetdruk.restorder.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Update;
import reactor.core.publisher.Mono;
import ru.privetdruk.restorder.bot.ClientBot;

@RestController
public class WebHookController {
    private final ClientBot clientBot;

    public WebHookController(ClientBot clientBot) {
        this.clientBot = clientBot;
    }

    @PostMapping(value = "/")
    public Mono<BotApiMethod<?>> onUpdateReceived(@RequestBody Update update) {
        return Mono.just(clientBot.onWebhookUpdateReceived(update));
    }
}
