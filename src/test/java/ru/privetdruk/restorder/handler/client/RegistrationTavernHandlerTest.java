package ru.privetdruk.restorder.handler.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import ru.privetdruk.restorder.model.consts.MessageText;
import ru.privetdruk.restorder.model.entity.UserEntity;
import ru.privetdruk.restorder.model.enums.Button;
import ru.privetdruk.restorder.model.enums.State;
import ru.privetdruk.restorder.repository.UserRepository;
import ru.privetdruk.restorder.service.UserService;


import java.util.List;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
@DisplayName("Registration service testing")
@SpringBootTest
@Disabled
class RegistrationTavernHandlerTest {
    @Mock
    RegistrationTavernHandler registrationTavernHandler;
    @Autowired
    UserService userService;

    @Autowired
    UserRepository userRepository;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @BeforeAll
    @DisplayName("Presets")
    static void beforeAll() {
       /* user = UserEntity.builder()
                .telegramId(1L)
                .state(State.REGISTRATION)
                .subState(State.REGISTRATION.getInitialSubState())
                .build();*/
    }

    @Test
    @DisplayName("Checking the registration logic")
    void testRegistration() throws JsonProcessingException, InterruptedException {
        // String url = "https://api.telegram.org/bot" + botClientToken + "/sendMessage?";

        //chatIds.stream()
        //         .map(chatId -> url + "chat_id=" + chatId + "&text=" + text)
        //         .forEach();

        WebClient client = WebClient.create();

        ReplyKeyboardMarkup keyboardMarkup = ReplyKeyboardMarkup
                .builder()
                .keyboard(List.of(
                        new KeyboardRow(List.of(
                                new KeyboardButton(Button.REGISTRATION_ACCEPT.getText())))))
                .build();

        SendMessage sendMessage = new SendMessage();
        sendMessage.setReplyMarkup(keyboardMarkup);

        ObjectMapper objectMapper = new ObjectMapper();
        String payload = objectMapper.writeValueAsString(sendMessage);
       // String payload = objectMapper.writeValueAsString(ReplyKeyboardRemove.builder().removeKeyboard(true).build());
        client.post()
                .uri(uriBuilder -> UriComponentsBuilder
                        .newInstance()
                        .scheme("https")
                        .host("api.telegram.org")
                        .path("/bot" + "5126027313:AAEM4Jqymy_uwI8qooAyRkhgNJ45peLqK4U")
                        .path("/sendMessage")
                        .query("chat_id={chatId}")
                        .query("text={text}")
                        .buildAndExpand("239635087", MessageText.YOUR_CLAIM_WAS_APPROVED)
                        /*.buildAndExpand("239635087",
                                "Пользователь с telegramId " + 9879797 + " запросил подтверждение регистрации. " + System.lineSeparator() + System.lineSeparator()
                                + "Данные пользователя " + System.lineSeparator() + System.lineSeparator()
                                        + "Имя пользователя: " + "Сергей" + System.lineSeparator()
                                        + "Город: " + "Брянск" + System.lineSeparator()
                                        + "Название заведения: " + "Августин" + System.lineSeparator()
                                        + "Адрес: " + "ул. Почтовая, д. 136" + System.lineSeparator() + System.lineSeparator()
                                + "Необходимо проверить адрес на валидность и подтвердить регистрацию"
                        )*/
                        .encode()
                        .toUri()
                )
                .contentType(MediaType.APPLICATION_JSON)
               // .bodyValue(payload)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(String.class)
                        .subscribe();
        Thread.sleep(10000);

/*
        UserEntity user = UserEntity.builder()
                .telegramId(1L)
                .state(State.REGISTRATION)
                .subState(SubState.SHOW_REGISTER_BUTTON)
                .build();

        SendMessage sendMessage = new SendMessage("1", "Пройдите регистрацию, чтобы получить доступ к функционалу бота.");

        when(registrationHandler.handle(eq(user), any(), any())).thenReturn(sendMessage);
        registrationHandler.handle(user, new Message(), new CallbackQuery());


        switch (user.getSubState()) {
            case SHOW_REGISTER_BUTTON: {

            }
        }*/
    }

    @Test
    @Transactional(readOnly = false)
    void testRepo() {
        Optional<UserEntity> byTelegramIdWithLock = userService.findByTelegramIdWithLock(239635087L);
        byTelegramIdWithLock.get().setState(State.MAIN_MENU);
        userService.save(byTelegramIdWithLock.get());
    }
}