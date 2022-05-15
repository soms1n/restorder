package ru.privetdruk.restorder.bot;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.TestPropertySource;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import ru.privetdruk.restorder.AbstractIntegrationTest;
import ru.privetdruk.restorder.handler.MessageHandler;
import ru.privetdruk.restorder.handler.client.RegistrationTavernHandler;
import ru.privetdruk.restorder.model.consts.MessageText;
import ru.privetdruk.restorder.model.enums.State;
import ru.privetdruk.restorder.service.client.ClientHandlerService;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Test Client bot logic")
@TestPropertySource(properties = {
        "bot.client.username = user4",
        "bot.client.token = test_token",
        "bot.client.web-hook-path = http://test"
})
class ClientBotTest extends AbstractIntegrationTest {
    @Autowired
    private ClientBot clientBot;

    @Mock
    private static ClientHandlerService clientHandlerService;

    @BeforeAll
    static void beforeAll() {
        clientHandlerService = Mockito.mock(ClientHandlerService.class);
        RegistrationTavernHandler registrationTavernHandler = Mockito.mock(RegistrationTavernHandler.class);
        Map<State, MessageHandler> map = new HashMap<>();
        map.put(State.REGISTRATION_TAVERN, registrationTavernHandler);
        Mockito.when(clientHandlerService.loadHandlers()).thenReturn(map);
    }

    @Test
    void test_client_bot_bean_init() {
        assertAll(
                () -> assertEquals("user4", clientBot.getBotUsername()),
                () -> assertEquals("test_token", clientBot.getBotToken()),
                () -> assertEquals("http://test", clientBot.getBotPath()));
    }

    @Test
    void when_onWebhookUpdateReceived_throw_exception_then_return_empty_message() {
        BotApiMethod<?> botApiMethod = clientBot.onWebhookUpdateReceived(null);

        assertAll(
                () -> assertInstanceOf(SendMessage.class, botApiMethod),
                () -> assertNull(((SendMessage) botApiMethod).getEntities()),
                () -> assertNull(((SendMessage) botApiMethod).getAllowSendingWithoutReply())
        );
    }

    @Test
    void when_onWebhookUpdateReceived_then_return_message() {
        Update update = new Update();
        Message message = new Message();
        message.setText("test message");
        User user = new User(111L, "test first name", false);
        message.setFrom(user);
        message.setChat(new Chat(555L, "test Type"));
        update.setMessage(message);

        assertEquals(MessageText.REGISTER, ((SendMessage) clientBot.onWebhookUpdateReceived(update)).getText());
    }
}