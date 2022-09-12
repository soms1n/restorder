package ru.privetdruk.restorder.controller;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.privetdruk.restorder.bot.ClientBot;
import ru.privetdruk.restorder.bot.UserBot;
import ru.privetdruk.restorder.service.TelegramApiService;
import ru.privetdruk.restorder.service.user.UserBotService;

@Disabled
@WebFluxTest(controllers = WebHookController.class)
@AutoConfigureWebTestClient(timeout = "36000")
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

    @MockBean
    UserBotService userBotService;

    @MockBean
    TelegramApiService telegramApiService;

    @Autowired
    private WebTestClient webClient;

    @Value("${bot.user.rest}")
    private String userEndpoint;

    @Test
    void onUserUpdateReceivedTest() throws Exception {
        Update update = new Update();

        webClient.post().uri(userEndpoint)
                .bodyValue(update)
                .exchange()
                .expectStatus().isOk();
       /* mockMvc.perform(MockMvcRequestBuilders.post("${bot.user.rest}"))
                .andExpect(status().isOk());*/
    }

    @Test
    void onClientUpdateReceivedTest() throws Exception {
       /* mockMvc.perform(MockMvcRequestBuilders.post("${bot.client.rest}"))
                .andExpect(status().isOk());*/
    }
}