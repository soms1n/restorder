package ru.privetdruk.restorder.handler.client;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import reactor.core.publisher.Mono;
import ru.privetdruk.restorder.AbstractTest;
import ru.privetdruk.restorder.model.entity.UserEntity;
import ru.privetdruk.restorder.model.enums.Role;
import ru.privetdruk.restorder.model.enums.State;
import ru.privetdruk.restorder.model.enums.SubState;
import ru.privetdruk.restorder.service.TelegramApiService;
import ru.privetdruk.restorder.service.UserService;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static ru.privetdruk.restorder.handler.client.RegistrationTavernHandlerTest.generateTestUser;
import static ru.privetdruk.restorder.model.consts.MessageText.*;

@DisplayName("Checking the admin logic")
class AdminHandlerTest extends AbstractTest {
    @Mock
    TelegramApiService telegramApiService;
    @Mock
    UserService userService;
    @Mock
    Message message;
    @InjectMocks
    AdminHandler adminHandler;

    @Test
    void admin_not_in_APPROVE_TAVERN_SUB_STATE_and_was_passed_any_text() {
        UserEntity admin = generateTestUser(State.REGISTRATION_TAVERN, SubState.WAITING_APPROVE_APPLICATION);
        when(message.getText()).thenReturn("some text");

        doAnswer(invocation -> admin.setState(State.MAIN_MENU)).when(userService).updateState(any(), any());

        SendMessage sendMessage = adminHandler.handle(admin, message, null);

        Assertions.assertEquals(SOMETHING_WENT_WRONG, sendMessage.getText());
    }

    @Test
    void admin_in_APPROVE_TAVERN_SUB_STATE_and_client_not_in_WAITING_APPROVE_APPLICATION_SUB_STATE() {
        UserEntity admin = generateTestUser(State.ADMIN, SubState.APPROVE_TAVERN);
        UserEntity client = generateTestUser(State.REGISTRATION_TAVERN, SubState.EDIT_DESCRIPTION);
        when(message.getText()).thenReturn("some text");
        when(userService.findByTelegramId(any(), any())).thenReturn(Optional.of(client));

        String callbackData = "ACCEPT 12345678";
        CallbackQuery callbackQuery = new CallbackQuery();
        callbackQuery.setData(callbackData);

        SendMessage sendMessage = adminHandler.handle(admin, message, callbackQuery);

        Assertions.assertEquals(APPLICATION_ALREADY_CONFIRMED, sendMessage.getText());
    }

    @Test
    void admin_in_APPROVE_TAVERN_SUB_STATE_and_client_not_found() {
        UserEntity admin = generateTestUser(State.ADMIN, SubState.APPROVE_TAVERN);
        when(message.getText()).thenReturn("some text");
        when(userService.findByTelegramId(any(), any())).thenReturn(Optional.empty());

        String callbackData = "ACCEPT 12345678";
        CallbackQuery callbackQuery = new CallbackQuery();
        callbackQuery.setData(callbackData);

        SendMessage sendMessage = adminHandler.handle(admin, message, callbackQuery);

        Assertions.assertEquals(USER_NOT_FOUND, sendMessage.getText());
    }

    @Test
    void admin_in_APPROVE_TAVERN_SUB_STATE_and_successfully_accept_request_and_got_it_CLIENT_ADMIN_role() {
        UserEntity admin = generateTestUser(State.ADMIN, SubState.APPROVE_TAVERN);
        UserEntity client = generateTestUser(State.REGISTRATION_TAVERN, SubState.WAITING_APPROVE_APPLICATION);
        client.setRoles(new HashSet<>());

        when(message.getText()).thenReturn("some text");
        when(userService.findByTelegramId(any(), any())).thenReturn(Optional.of(client));
        when(telegramApiService.prepareSendMessage(any(), any(), anyBoolean(), any())).thenReturn(Mono.never());
        doAnswer(invocation -> client.setState(State.MAIN_MENU)).when(userService).updateState(any(), any());

        String callbackData = "ACCEPT 12345678";
        CallbackQuery callbackQuery = new CallbackQuery();
        callbackQuery.setData(callbackData);

        SendMessage sendMessage = adminHandler.handle(admin, message, callbackQuery);

        Assertions.assertAll(
                () -> assertEquals(APPLICATION_CONFIRMED, sendMessage.getText()),
                () -> assertTrue(client.getRoles().contains(Role.CLIENT_ADMIN)),
                () -> assertTrue(client.isRegistered()),
                () -> assertEquals(State.MAIN_MENU, client.getState())
        );
    }

    @Test
    void admin_in_APPROVE_TAVERN_SUB_STATE_and_successfully_accept_request_and_did_not_got_it_CLIENT_ADMIN_role() {
        UserEntity admin = generateTestUser(State.ADMIN, SubState.APPROVE_TAVERN);
        UserEntity client = generateTestUser(State.REGISTRATION_TAVERN, SubState.WAITING_APPROVE_APPLICATION);
        client.setRoles(new HashSet<>(List.of(Role.USER)));

        when(message.getText()).thenReturn("some text");
        when(userService.findByTelegramId(any(), any())).thenReturn(Optional.of(client));
        when(telegramApiService.prepareSendMessage(any(), any(), anyBoolean(), any())).thenReturn(Mono.never());
        doAnswer(invocation -> client.setState(State.MAIN_MENU)).when(userService).updateState(any(), any());

        String callbackData = "ACCEPT 12345678";
        CallbackQuery callbackQuery = new CallbackQuery();
        callbackQuery.setData(callbackData);

        SendMessage sendMessage = adminHandler.handle(admin, message, callbackQuery);

        Assertions.assertAll(
                () -> assertEquals(APPLICATION_CONFIRMED, sendMessage.getText()),
                () -> assertFalse(client.getRoles().contains(Role.CLIENT_ADMIN)),
                () -> assertTrue(client.getRoles().contains(Role.USER)),
                () -> assertTrue(client.isRegistered()),
                () -> assertEquals(State.MAIN_MENU, client.getState())
        );
    }
}