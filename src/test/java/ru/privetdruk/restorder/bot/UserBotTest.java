package ru.privetdruk.restorder.bot;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import ru.privetdruk.restorder.AbstractTest;
import ru.privetdruk.restorder.model.consts.MessageText;
import ru.privetdruk.restorder.model.entity.UserEntity;
import ru.privetdruk.restorder.model.enums.State;
import ru.privetdruk.restorder.model.enums.UserType;
import ru.privetdruk.restorder.service.UserService;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Test User bot logic")
@TestPropertySource(properties = {
        "bot.user.username = user5",
        "bot.user.token = test_token",
        "bot.user.web-hook-path = http://test"
})
class UserBotTest extends AbstractTest {
    @Autowired
    UserBot userBot;
    @MockBean
    private UserService userService;

    @Test
    void test_user_bot_bean_init() {
        assertAll(
                () -> assertEquals("user5", userBot.getBotUsername()),
                () -> assertEquals("test_token", userBot.getBotToken()),
                () -> assertEquals("http://test", userBot.getBotPath()));
    }

    @Test
    void when_onWebhookUpdateReceived_then_return_message() {
        long telegramId = 111;

        Update update = new Update();
        Message message = new Message();
        message.setText("test message");
        User user = new User(telegramId, "test first name", false);
        message.setFrom(user);
        message.setChat(new Chat(555L, "test Type"));
        update.setMessage(message);

        UserEntity userEntity = new UserEntity(
                telegramId,
                State.REGISTRATION_USER,
                State.REGISTRATION_USER.getInitialSubState(),
                UserType.USER);

        Mockito.when(userService.findByTelegramId(Mockito.anyLong(), Mockito.any(UserType.class))).thenReturn(Optional.of(userEntity));

        assertEquals(MessageText.GREETING, ((SendMessage) userBot.onWebhookUpdateReceived(update)).getText());
    }
}