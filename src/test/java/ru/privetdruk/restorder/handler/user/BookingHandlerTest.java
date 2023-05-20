package ru.privetdruk.restorder.handler.user;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import ru.privetdruk.restorder.AbstractTest;
import ru.privetdruk.restorder.model.consts.MessageText;
import ru.privetdruk.restorder.model.entity.UserEntity;
import ru.privetdruk.restorder.model.enums.Button;
import ru.privetdruk.restorder.model.enums.City;
import ru.privetdruk.restorder.model.enums.Role;
import ru.privetdruk.restorder.model.enums.State;
import ru.privetdruk.restorder.service.ContactService;
import ru.privetdruk.restorder.service.UserService;
import ru.privetdruk.restorder.service.util.ValidationService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@DisplayName("Checking the booking logic")
@Disabled
class BookingHandlerTest extends AbstractTest {

    @InjectMocks
    RegistrationHandler registrationHandler;

    @Mock
    UserService userService;

    @Mock
    ContactService contactService;

    @Mock
    BookingHandler bookingHandler;

    @Test
    void user_in_greeting_substate_and_entered_any_text() {
        registrationHandler = new RegistrationHandler(bookingHandler, contactService, userService, new ValidationService());

        UserEntity user = UserEntity.builder()
                .telegramId(1L)
                .state(State.BOOKING)
                .subState(State.BOOKING.getInitialSubState())
                .build();

        user.addRole(Role.USER);

        Message message = new Message();
        Chat chat = new Chat();
        chat.setId(1L);
        message.setChat(chat);
        message.setText("Some text");

        SendMessage sendMessage = registrationHandler.handle(user, message, null);
        assertNotNull(sendMessage);
        assertEquals(sendMessage.getText(), MessageText.GREETING);
        assertEquals(Long.valueOf(sendMessage.getChatId()), chat.getId());
        assertEquals(((InlineKeyboardMarkup) sendMessage.getReplyMarkup()).getKeyboard().size(), 1);
        assertEquals(((InlineKeyboardMarkup) sendMessage.getReplyMarkup()).getKeyboard().get(0).size(), 2);
    }

    @Test
    void not_registered_user_in_greeting_substate_and_select_city() {
        Mockito.doNothing().when(userService).save(Mockito.any());
        registrationHandler = new RegistrationHandler(bookingHandler, contactService, userService, new ValidationService());

        UserEntity user = UserEntity.builder()
                .telegramId(1L)
                .state(State.BOOKING)
                .subState(State.BOOKING.getInitialSubState())
                .build();

        user.addRole(Role.USER);

        Message message = new Message();
        Chat chat = new Chat();
        chat.setId(1L);
        message.setChat(chat);
        message.setText("Some text");

        CallbackQuery callbackQuery = new CallbackQuery();
        callbackQuery.setData(City.YOSHKAR_OLA.name());

        SendMessage sendMessage = registrationHandler.handle(user, message, callbackQuery);
        assertNotNull(sendMessage);
        assertEquals(sendMessage.getText(), MessageText.CHOICE_TAVERN_TYPE);
        assertEquals(user.getCity().getName(), City.YOSHKAR_OLA.getName());
        assertEquals(Long.valueOf(sendMessage.getChatId()), chat.getId());
        //assertEquals(user.getSubState(), SubState.USER_BOT_MAIN_MENU);
        assertEquals(((InlineKeyboardMarkup) sendMessage.getReplyMarkup()).getKeyboard().size(), 2);
        assertEquals(((InlineKeyboardMarkup) sendMessage.getReplyMarkup()).getKeyboard().get(0).size(), 2);
        assertEquals(((InlineKeyboardMarkup) sendMessage.getReplyMarkup()).getKeyboard().get(1).size(), 1);
    }

    @Test
    void registered_user_in_greeting_substate_and_select_city() {
        Mockito.doNothing().when(userService).save(Mockito.any());
        registrationHandler = new RegistrationHandler(bookingHandler, contactService, userService, new ValidationService());

        UserEntity user = UserEntity.builder()
                .telegramId(1L)
                .state(State.BOOKING)
                .subState(State.BOOKING.getInitialSubState())
                .build();

        user.addRole(Role.USER);
        user.setRegistered(true);

        Message message = new Message();
        Chat chat = new Chat();
        chat.setId(1L);
        message.setChat(chat);
        message.setText("Some text");

        CallbackQuery callbackQuery = new CallbackQuery();
        callbackQuery.setData(City.YOSHKAR_OLA.name());

        SendMessage sendMessage = registrationHandler.handle(user, message, callbackQuery);
        assertNotNull(sendMessage);
        assertEquals(sendMessage.getText(), MessageText.CHOICE_TAVERN_TYPE);
        assertEquals(user.getCity().getName(), City.YOSHKAR_OLA.getName());
        assertEquals(Long.valueOf(sendMessage.getChatId()), chat.getId());
      //  assertEquals(user.getSubState(), SubState.USER_BOT_MAIN_MENU);
        assertEquals(((InlineKeyboardMarkup) sendMessage.getReplyMarkup()).getKeyboard().size(), 3);
        assertEquals(((InlineKeyboardMarkup) sendMessage.getReplyMarkup()).getKeyboard().get(0).size(), 2);
        assertEquals(((InlineKeyboardMarkup) sendMessage.getReplyMarkup()).getKeyboard().get(1).size(), 1);
        assertEquals(((InlineKeyboardMarkup) sendMessage.getReplyMarkup()).getKeyboard().get(2).size(), 1);
    }

    @Test
    void registered_user_in_userBotMainMenu_substate_and_entered_any_text() {
        Mockito.doNothing().when(userService).save(Mockito.any());
        registrationHandler = new RegistrationHandler(bookingHandler, contactService, userService, new ValidationService());

        UserEntity user = UserEntity.builder()
                .telegramId(1L)
                .state(State.BOOKING)
             //   .subState(SubState.USER_BOT_MAIN_MENU)
                .build();

        user.addRole(Role.USER);
        user.setRegistered(true);

        Message message = new Message();
        Chat chat = new Chat();
        chat.setId(1L);
        message.setChat(chat);
        message.setText("Some text");

        SendMessage sendMessage = registrationHandler.handle(user, message, null);
        assertNotNull(sendMessage);
        assertEquals(sendMessage.getText(), MessageText.CHOICE_TAVERN_TYPE);
        assertEquals(Long.valueOf(sendMessage.getChatId()), chat.getId());
        //assertEquals(user.getSubState(), SubState.USER_BOT_MAIN_MENU);
        assertEquals(((InlineKeyboardMarkup) sendMessage.getReplyMarkup()).getKeyboard().size(), 3);
        assertEquals(((InlineKeyboardMarkup) sendMessage.getReplyMarkup()).getKeyboard().get(0).size(), 2);
        assertEquals(((InlineKeyboardMarkup) sendMessage.getReplyMarkup()).getKeyboard().get(1).size(), 1);
    }

    @Test
    void registered_user_in_userBotMainMenu_substate_and_click_main_menu_button() {
        Mockito.doNothing().when(userService).save(Mockito.any());
        registrationHandler = new RegistrationHandler(bookingHandler, contactService, userService, new ValidationService());

        UserEntity user = UserEntity.builder()
                .telegramId(1L)
                .state(State.BOOKING)
             //   .subState(SubState.USER_BOT_MAIN_MENU)
                .build();

        user.addRole(Role.USER);
        user.setRegistered(true);

        Message message = new Message();
        Chat chat = new Chat();
        chat.setId(1L);
        message.setChat(chat);
        message.setText("Some text");

        CallbackQuery callbackQuery = new CallbackQuery();
        callbackQuery.setData(Button.RETURN_MAIN_MENU.getName());

        SendMessage sendMessage = registrationHandler.handle(user, message, callbackQuery);
        assertNotNull(sendMessage);
        assertEquals(sendMessage.getText(), MessageText.CHOICE_TAVERN_TYPE);
        assertEquals(Long.valueOf(sendMessage.getChatId()), chat.getId());
     //   assertEquals(user.getSubState(), SubState.USER_BOT_MAIN_MENU);
        assertEquals(((InlineKeyboardMarkup) sendMessage.getReplyMarkup()).getKeyboard().size(), 3);
        assertEquals(((InlineKeyboardMarkup) sendMessage.getReplyMarkup()).getKeyboard().get(0).size(), 2);
        assertEquals(((InlineKeyboardMarkup) sendMessage.getReplyMarkup()).getKeyboard().get(1).size(), 1);
    }
}
