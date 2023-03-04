package ru.privetdruk.restorder.controller;


import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Update;
import reactor.core.publisher.Mono;
import ru.privetdruk.restorder.bot.ClientBot;
import ru.privetdruk.restorder.bot.UserBot;

@RequiredArgsConstructor
@RestController
public class WebHookController {
    private final ClientBot clientBot;
    private final UserBot userBot;

    @PostMapping(value = "${bot.user.rest}")
    public Mono<BotApiMethod<?>> onUserUpdateReceived(@RequestBody Update update) {
        return Mono.just(userBot.onWebhookUpdateReceived(update));
    }

    @PostMapping(value = "${bot.client.rest}")
    public Mono<BotApiMethod<?>> onClientUpdateReceived(@RequestBody Update update) {
        return Mono.just(clientBot.onWebhookUpdateReceived(update));
    }
}
