package ru.privetdruk.restorder.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.privetdruk.restorder.bot.ClientBot;
import ru.privetdruk.restorder.bot.UserBot;

@WebFluxTest(controllers = WebHookController.class)
@DisplayName("Test controller")
@TestPropertySource(
        properties = {
                "bot.client.rest = /client",
                "bot.user.rest = /user"
        })
class WebHookControllerTest {
    @MockBean
    private ClientBot clientBot;

    @MockBean
    private UserBot userBot;

    @Autowired
    private WebTestClient webClient;

    @Value("${bot.user.rest}")
    private String userEndpoint;

    @Value("${bot.client.rest}")
    private String clientEndpoint;

    @Test
    void onUserUpdateReceivedTest() {
        Update update = new Update();

        String testMessage = "test message";
        SendMessage sendMessage = new SendMessage();
        sendMessage.setText(testMessage);

        Mockito.when(userBot.onWebhookUpdateReceived(update)).thenAnswer((Answer<SendMessage>) invocationOnMock -> sendMessage);

        webClient.post().uri(userEndpoint)
                .bodyValue(update)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.text").isEqualTo(testMessage);
    }

    @Test
    void onClientUpdateReceivedTest() {
        Update update = new Update();

        String testMessage = "test message";
        SendMessage sendMessage = new SendMessage();
        sendMessage.setText(testMessage);

        Mockito.when(clientBot.onWebhookUpdateReceived(update)).thenAnswer((Answer<SendMessage>) invocationOnMock -> sendMessage);

        webClient.post().uri(clientEndpoint)
                .bodyValue(update)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.text").isEqualTo(testMessage);
    }
}