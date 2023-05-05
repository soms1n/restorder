package ru.privetdruk.restorder.service.client;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.*;
import ru.privetdruk.restorder.AbstractTest;
import ru.privetdruk.restorder.handler.client.AdminHandler;
import ru.privetdruk.restorder.handler.client.EventHandler;
import ru.privetdruk.restorder.handler.client.MainMenuHandler;
import ru.privetdruk.restorder.handler.client.RegistrationTavernHandler;
import ru.privetdruk.restorder.model.entity.UserEntity;
import ru.privetdruk.restorder.model.enums.Command;
import ru.privetdruk.restorder.model.enums.State;
import ru.privetdruk.restorder.model.enums.SubState;
import ru.privetdruk.restorder.model.enums.UserType;
import ru.privetdruk.restorder.service.*;

import java.util.Optional;

@DisplayName(value = "Test Client bot service logic")
@SpringBootTest
class ClientBotServiceTest extends AbstractTest {
  /*  @Autowired
    ClientBotService clientBotService;
    @MockBean
    UserService userService;
    @MockBean
    RegistrationTavernHandler registrationTavernHandler;
    @MockBean
    EventHandler eventHandler;
    @MockBean
    MainMenuHandler mainMenuHandler;
    @MockBean
    AdminHandler adminHandler;

    @Test
    void when_update_empty_then_return_empty_message() {
        SendMessage sendMessage = clientBotService.handleUpdate(new Update());
        Assertions.assertNull(sendMessage.getChatId());
    }

    @Test
    void when_user_does_not_exists_create_new() {
        UserEntity userEntity = new UserEntity(1L, State.REGISTRATION_TAVERN,
                State.REGISTRATION_TAVERN.getInitialSubState(),
                UserType.CLIENT);

        SendMessage sendMessage = new SendMessage();
        sendMessage.setText("Test sendMessage");

        Mockito.when(userService.create(Mockito.anyLong(), Mockito.any(State.class), Mockito.any(UserType.class))).thenReturn(userEntity);
        Mockito.when(userService.findByTelegramId(Mockito.anyLong(), Mockito.any(UserType.class))).thenReturn(Optional.empty());
        Mockito.when(registrationTavernHandler.handle(Mockito.any(UserEntity.class), Mockito.any(Message.class), Mockito.any())).thenReturn(sendMessage);

        Update update = new Update();
        Message message = new Message();
        message.setText("test");
        message.setFrom(new User(1L, "test first name", false));
        update.setMessage(message);
        SendMessage resultSendMessage = clientBotService.handleUpdate(update);

        Assertions.assertEquals("Test sendMessage", resultSendMessage.getText());
        Mockito.verify(userService, Mockito.times(1))
                .create(Mockito.anyLong(), Mockito.any(State.class), Mockito.any(UserType.class));
    }

    @Test
    void when_message_does_not_exists_text_then_return_current_state() {
        UserEntity userEntity = new UserEntity(1L, State.REGISTRATION_TAVERN,
                State.REGISTRATION_TAVERN.getInitialSubState(),
                UserType.CLIENT);

        SendMessage sendMessage = new SendMessage();
        sendMessage.setText("Test sendMessage");

        Mockito.when(userService.create(Mockito.anyLong(), Mockito.any(State.class), Mockito.any(UserType.class))).thenReturn(userEntity);
        Mockito.when(userService.findByTelegramId(Mockito.anyLong(), Mockito.any(UserType.class))).thenReturn(Optional.empty());
        Mockito.when(registrationTavernHandler.handle(Mockito.any(UserEntity.class), Mockito.any(Message.class), Mockito.any())).thenReturn(sendMessage);

        Update update = new Update();
        Message message = new Message();
        message.setFrom(new User(1L, "test first name", false));
        update.setMessage(message);
        CallbackQuery callbackQuery = new CallbackQuery();
        Message callbackMessage = new Message();
        callbackMessage.setChat(new Chat(1L, "SomeType"));
        callbackQuery.setMessage(callbackMessage);
        update.setCallbackQuery(callbackQuery);
        SendMessage resultSendMessage = clientBotService.handleUpdate(update);

        Assertions.assertEquals("Test sendMessage", resultSendMessage.getText());
        Assertions.assertEquals(userEntity.getState(), State.REGISTRATION_TAVERN);
    }

    @Test
    void when_message_include_start_command_then_user_set_in_event_state() {
        UserEntity userEntity = new UserEntity(1L, State.REGISTRATION_TAVERN,
                State.REGISTRATION_TAVERN.getInitialSubState(),
                UserType.CLIENT);

        SendMessage sendMessage = new SendMessage();
        sendMessage.setText("Test sendMessage");

        Mockito.when(userService.create(Mockito.anyLong(), Mockito.any(State.class), Mockito.any(UserType.class)))
                .thenReturn(userEntity);
        Mockito.when(userService.findByTelegramId(Mockito.anyLong(), Mockito.any(UserType.class)))
                .thenReturn(Optional.empty());
        Mockito.when(eventHandler.handle(Mockito.any(UserEntity.class), Mockito.any(Message.class), Mockito.any()))
                .thenReturn(sendMessage);

        Update update = new Update();
        Message message = new Message();
        message.setText(Command.START.getCommand() + " " + "kjlkjlj");
        message.setFrom(new User(1L, "test first name", false));
        update.setMessage(message);
        SendMessage resultSendMessage = clientBotService.handleUpdate(update);

        Assertions.assertEquals("Test sendMessage", resultSendMessage.getText());
        Assertions.assertEquals(userEntity.getState(), State.REGISTRATION_TAVERN);

        Mockito.verify(eventHandler).handle(Mockito.any(UserEntity.class), Mockito.any(Message.class), Mockito.any());
    }

    @Test
    void when_message_include_menu_command_then_user_set_in_menu_state() {
        UserEntity userEntity = new UserEntity(1L, State.REGISTRATION_TAVERN,
                State.REGISTRATION_TAVERN.getInitialSubState(),
                UserType.CLIENT);
        userEntity.setRegistered(true);

        SendMessage sendMessage = new SendMessage();
        sendMessage.setText("Test sendMessage");

        Mockito.when(userService.create(Mockito.anyLong(), Mockito.any(State.class), Mockito.any(UserType.class)))
                .thenReturn(userEntity);
        Mockito.when(userService.findByTelegramId(Mockito.anyLong(), Mockito.any(UserType.class)))
                .thenReturn(Optional.empty());

        Mockito.doAnswer(answer -> {
            userEntity.setState(State.MAIN_MENU);
            userEntity.setSubState(State.MAIN_MENU.getInitialSubState());
            return userEntity;
        }).when(userService).updateState(Mockito.any(UserEntity.class), Mockito.any(State.class));

        Mockito.when(mainMenuHandler.handle(Mockito.any(UserEntity.class), Mockito.any(Message.class), Mockito.any()))
                .thenReturn(sendMessage);

        Update update = new Update();
        Message message = new Message();
        message.setText(Command.MAIN_MENU.getCommand() + " " + "kjlkjlj");
        message.setFrom(new User(1L, "test first name", false));
        update.setMessage(message);
        SendMessage resultSendMessage = clientBotService.handleUpdate(update);

        Assertions.assertEquals("Test sendMessage", resultSendMessage.getText());
        Assertions.assertEquals(userEntity.getState(), State.MAIN_MENU);
        Assertions.assertEquals(userEntity.getSubState(), State.MAIN_MENU.getInitialSubState());

        Mockito.verify(userService).updateState(Mockito.any(UserEntity.class), Mockito.any(State.class));
        Mockito.verify(mainMenuHandler).handle(Mockito.any(UserEntity.class), Mockito.any(Message.class), Mockito.any());
    }

    @Test
    void when_message_include_callback_with_data_without_buttons() {
        UserEntity userEntity = new UserEntity(1L, State.REGISTRATION_TAVERN,
                State.REGISTRATION_TAVERN.getInitialSubState(),
                UserType.CLIENT);

        SendMessage sendMessage = new SendMessage();
        sendMessage.setText("Test sendMessage");

        Mockito.when(userService.create(Mockito.anyLong(), Mockito.any(State.class), Mockito.any(UserType.class)))
                .thenReturn(userEntity);
        Mockito.when(userService.findByTelegramId(Mockito.anyLong(), Mockito.any(UserType.class)))
                .thenReturn(Optional.empty());

        Mockito.when(registrationTavernHandler.handle(Mockito.any(UserEntity.class), Mockito.any(Message.class), Mockito.any()))
                .thenReturn(sendMessage);

        Update update = new Update();
        Message message = new Message();
        message.setText(Command.MAIN_MENU.getCommand() + " " + "kjlkjlj");
        message.setFrom(new User(1L, "test first name", false));
        update.setMessage(message);
        CallbackQuery callbackQuery = new CallbackQuery();
        Message callbackMessage = new Message();
        callbackMessage.setChat(new Chat(1L, "SomeType"));
        callbackQuery.setMessage(callbackMessage);
        callbackQuery.setData("TEST BUTTON");
        update.setCallbackQuery(callbackQuery);
        SendMessage resultSendMessage = clientBotService.handleUpdate(update);

        Assertions.assertEquals("Test sendMessage", resultSendMessage.getText());
        Assertions.assertEquals(userEntity.getState(), State.REGISTRATION_TAVERN);
        Assertions.assertEquals(userEntity.getSubState(), State.REGISTRATION_TAVERN.getInitialSubState());
    }

    @Test
    void when_message_include_callback_with_data_then_set_admin_state() {
        UserEntity userEntity = new UserEntity(1L, State.REGISTRATION_TAVERN,
                State.REGISTRATION_TAVERN.getInitialSubState(),
                UserType.CLIENT);

        SendMessage sendMessage = new SendMessage();
        sendMessage.setText("Test sendMessage");

        Mockito.when(userService.create(Mockito.anyLong(), Mockito.any(State.class), Mockito.any(UserType.class)))
                .thenReturn(userEntity);
        Mockito.when(userService.findByTelegramId(Mockito.anyLong(), Mockito.any(UserType.class)))
                .thenReturn(Optional.empty());
        Mockito.doAnswer(answer -> {
            userEntity.setState(State.ADMIN);
            userEntity.setSubState(SubState.APPROVE_TAVERN);
            return userEntity;
        }).when(userService).update(Mockito.any(UserEntity.class), Mockito.any(State.class), Mockito.any(SubState.class));

        Mockito.when(adminHandler.handle(Mockito.any(UserEntity.class), Mockito.any(Message.class), Mockito.any()))
                .thenReturn(sendMessage);

        Update update = new Update();
        Message message = new Message();
        message.setText(Command.MAIN_MENU.getCommand() + " " + "kjlkjlj");
        message.setFrom(new User(1L, "test first name", false));
        update.setMessage(message);
        CallbackQuery callbackQuery = new CallbackQuery();
        Message callbackMessage = new Message();
        callbackMessage.setChat(new Chat(1L, "SomeType"));
        callbackQuery.setMessage(callbackMessage);
        callbackQuery.setData("ACCEPT hgfhf");
        update.setCallbackQuery(callbackQuery);
        SendMessage resultSendMessage = clientBotService.handleUpdate(update);

        Assertions.assertEquals("Test sendMessage", resultSendMessage.getText());
        Assertions.assertEquals(userEntity.getState(), State.ADMIN);
        Assertions.assertEquals(userEntity.getSubState(), SubState.APPROVE_TAVERN);
    }*/
}