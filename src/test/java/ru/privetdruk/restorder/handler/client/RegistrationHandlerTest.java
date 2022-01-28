package ru.privetdruk.restorder.handler.client;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import ru.privetdruk.restorder.model.entity.UserEntity;
import ru.privetdruk.restorder.model.enums.State;
import ru.privetdruk.restorder.model.enums.SubState;


import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Registration service testing")
@Disabled
class RegistrationHandlerTest {
    @Mock
    RegistrationHandler registrationHandler;

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
    void testRegistration() {
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
        }
    }
}