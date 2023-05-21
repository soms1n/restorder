package ru.privetdruk.restorder.handler.client;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Contact;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;
import reactor.core.publisher.Mono;
import ru.privetdruk.restorder.AbstractTest;
import ru.privetdruk.restorder.model.consts.MessageText;
import ru.privetdruk.restorder.model.entity.AddressEntity;
import ru.privetdruk.restorder.model.entity.ContactEntity;
import ru.privetdruk.restorder.model.entity.TavernEntity;
import ru.privetdruk.restorder.model.entity.UserEntity;
import ru.privetdruk.restorder.model.enums.*;
import ru.privetdruk.restorder.service.ContactService;
import ru.privetdruk.restorder.service.TavernService;
import ru.privetdruk.restorder.service.TelegramApiService;
import ru.privetdruk.restorder.service.UserService;
import ru.privetdruk.restorder.service.util.ValidationService;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static ru.privetdruk.restorder.model.consts.MessageText.CLAIM_APPROVE_WAIT;
import static ru.privetdruk.restorder.model.consts.MessageText.SELECT_ELEMENT_FOR_EDIT;
import static ru.privetdruk.restorder.model.enums.City.YOSHKAR_OLA;

@DisplayName("Checking the registration logic")
class RegistrationTavernHandlerTest extends AbstractTest {
    @Mock
    Message message;
    @Mock
    CallbackQuery callback;
    @Mock
    UserService userService;
    @InjectMocks
    RegistrationTavernHandler registrationTavernHandler;
    @Mock
    TelegramApiService telegramApiService;
    @Mock
    TavernService tavernService;
    @Mock
    ValidationService validationService;
    @Mock
    ContactService contactService;

    @BeforeEach
    @DisplayName("Presets")
    void beforeEach() {
        Mockito.when(message.getChatId()).thenReturn(1L);
    }

    @Test
    void client_in_SHOW_REGISTER_BUTTON_SUB_STATE_and_does_not_click_registration_button() {
        UserEntity user = generateTestUser(State.REGISTRATION_TAVERN, SubState.SHOW_REGISTER_BUTTON);
        Mockito.when(message.getText()).thenReturn("Any string");

        SendMessage sendMessage = registrationTavernHandler.handle(
                user,
                message,
                null
        );

        assertEquals(State.REGISTRATION_TAVERN, user.getState());
        assertEquals(SubState.SHOW_REGISTER_BUTTON, user.getSubState());
        assertAll("sendMessage",
                () -> assertEquals(MessageText.REGISTER, sendMessage.getText()),
                () -> assertEquals(1,
                        ((ReplyKeyboardMarkup) sendMessage.getReplyMarkup()).getKeyboard().size()),
                () -> assertEquals(1,
                        ((ReplyKeyboardMarkup) sendMessage.getReplyMarkup()).getKeyboard().get(0).size()),
                () -> assertEquals(Button.REGISTRATION.getText(),
                        ((ReplyKeyboardMarkup) sendMessage.getReplyMarkup()).getKeyboard().get(0).get(0).getText()));
    }

    @Test
    void client_in_SHOW_REGISTER_BUTTON_SUB_STATE_and_click_registration_button() {
        UserEntity user = generateTestUser(State.REGISTRATION_TAVERN, SubState.SHOW_REGISTER_BUTTON);
        Mockito.when(message.getText()).thenReturn(Button.REGISTRATION.getText());
        doAnswer(invocation -> user.setSubState(SubState.ENTER_FULL_NAME))
                .when(userService).updateSubState(any(), any());

        SendMessage sendMessage = registrationTavernHandler.handle(
                user,
                message,
                callback
        );

        assertEquals(State.REGISTRATION_TAVERN, user.getState());
        assertEquals(SubState.ENTER_FULL_NAME, user.getSubState());
        assertEquals(SubState.ENTER_FULL_NAME.getMessage(), sendMessage.getText());
    }

    @Test
    void client_in_ENTER_FULL_NAME_SUB_STATE_and_entered_your_name() {
        UserEntity user = generateTestUser(State.REGISTRATION_TAVERN, SubState.ENTER_FULL_NAME);
        Mockito.when(message.getText()).thenReturn("Any string");
        doAnswer(invocation -> user.setSubState(SubState.ENTER_TAVERN_NAME))
                .when(userService).updateSubState(any(), any());

        SendMessage sendMessage = registrationTavernHandler.handle(user, message, callback);

        assertEquals(State.REGISTRATION_TAVERN, user.getState());
        assertEquals(SubState.ENTER_TAVERN_NAME, user.getSubState());
        assertEquals(SubState.ENTER_TAVERN_NAME.getMessage(), sendMessage.getText());
    }

    @Test
    void client_in_ENTER_TAVERN_NAME_SUB_STATE_and_entered_tavern_name() {
        String tavernName = "Супер ресторан";

        TavernEntity tavern = new TavernEntity();
        tavern.setName(tavernName);
        tavern.setAddress(new AddressEntity());
        tavern.getAddress().setStreet("Дерибасовская 34А");
        tavern.getAddress().setCity(YOSHKAR_OLA);

        Mockito.when(tavernService.save(Mockito.any(TavernEntity.class))).thenReturn(tavern);
        Mockito.when(message.getText()).thenReturn(tavernName);

        UserEntity user = generateTestUser(State.REGISTRATION_TAVERN, SubState.ENTER_TAVERN_NAME);

        doAnswer(invocation -> user.setSubState(SubState.ENTER_TAVERN_DESCRIPTION))
                .when(userService).updateSubState(any(), any());

        SendMessage sendMessage = registrationTavernHandler.handle(
                user,
                message,
                callback
        );

        assertEquals(State.REGISTRATION_TAVERN, user.getState());
        assertEquals(SubState.ENTER_TAVERN_DESCRIPTION, user.getSubState());

        assertAll("sendMessage",
                () -> assertEquals(MessageText.ENTER_TAVERN_DESCRIPTION, sendMessage.getText()),
                () -> assertEquals(1, ((ReplyKeyboardMarkup) sendMessage.getReplyMarkup()).getKeyboard().size()),
                () -> assertEquals(Button.WITHOUT_DESCRIPTION.getText(),
                        ((ReplyKeyboardMarkup) sendMessage.getReplyMarkup()).getKeyboard()
                                .get(0)
                                .get(0)
                                .getText()));
    }

    @Test
    void client_in_ENTER_TAVERN_DESCRIPTION_SUB_STATE_and_entered_tavern_description() {
        Mockito.when(tavernService.save(Mockito.any(TavernEntity.class))).thenReturn(new TavernEntity());
        Mockito.when(message.getText()).thenReturn("Какое-то описание");

        UserEntity user = generateTestUser(State.REGISTRATION_TAVERN, SubState.ENTER_TAVERN_DESCRIPTION);

        doAnswer(invocation -> user.setSubState(SubState.CHOICE_CITY)).when(userService).updateSubState(any(), any());

        SendMessage sendMessage = registrationTavernHandler.handle(user, message, callback);

        assertEquals(State.REGISTRATION_TAVERN, user.getState());
        assertEquals(SubState.CHOICE_CITY, user.getSubState());

        assertAll("sendMessage",
                () -> assertEquals(MessageText.CHOICE_CITY, sendMessage.getText()),
                () -> assertEquals(1, ((ReplyKeyboardMarkup) sendMessage.getReplyMarkup()).getKeyboard().size()),
                () -> assertEquals(YOSHKAR_OLA.getDescription(),
                        ((ReplyKeyboardMarkup) sendMessage.getReplyMarkup()).getKeyboard()
                                .get(0)
                                .get(0)
                                .getText()));
    }

    @Test
    void client_in_ENTER_TAVERN_DESCRIPTION_SUB_STATE_and_press_button() {
        Mockito.when(tavernService.save(Mockito.any(TavernEntity.class))).thenReturn(new TavernEntity());
        Mockito.when(message.getText()).thenReturn(Button.WITHOUT_DESCRIPTION.getText());

        UserEntity user = generateTestUser(State.REGISTRATION_TAVERN, SubState.ENTER_TAVERN_DESCRIPTION);

        doAnswer(invocation -> user.setSubState(SubState.CHOICE_CITY)).when(userService).updateSubState(any(), any());

        SendMessage sendMessage = registrationTavernHandler.handle(user, message, callback);

        Mockito.verifyNoMoreInteractions(tavernService);

        assertEquals(State.REGISTRATION_TAVERN, user.getState());
        assertEquals(SubState.CHOICE_CITY, user.getSubState());

        assertAll("sendMessage",
                () -> assertEquals(MessageText.CHOICE_CITY, sendMessage.getText()),
                () -> assertEquals(1, ((ReplyKeyboardMarkup) sendMessage.getReplyMarkup()).getKeyboard().size()),
                () -> assertEquals(YOSHKAR_OLA.getDescription(),
                        ((ReplyKeyboardMarkup) sendMessage.getReplyMarkup()).getKeyboard()
                                .get(0)
                                .get(0)
                                .getText()));
    }

    @Test
    void client_in_CHOICE_CITY_SUB_STATE_and_entered_text_which_not_equals_available_city() {
        Mockito.when(tavernService.save(Mockito.any(TavernEntity.class))).thenReturn(new TavernEntity());
        Mockito.when(message.getText()).thenReturn("Что-то случайное");

        UserEntity user = generateTestUser(State.REGISTRATION_TAVERN, SubState.CHOICE_CITY);

        SendMessage sendMessage = registrationTavernHandler.handle(
                user,
                message,
                null
        );

        Mockito.verifyNoMoreInteractions(tavernService);

        assertEquals(State.REGISTRATION_TAVERN, user.getState());
        assertEquals(SubState.CHOICE_CITY, user.getSubState());

        assertAll("sendMessage",
                () -> assertEquals(MessageText.CHOICE_CITY, sendMessage.getText()),
                () -> assertEquals(1, ((ReplyKeyboardMarkup) sendMessage.getReplyMarkup()).getKeyboard().size()),
                () -> assertEquals(YOSHKAR_OLA.getDescription(),
                        ((ReplyKeyboardMarkup) sendMessage.getReplyMarkup()).getKeyboard()
                                .get(0)
                                .get(0)
                                .getText()));
    }

    @Test
    void client_in_CHOICE_CITY_SUB_STATE_and_entered_text_which_equals_available_city_or_select_city() {
        Mockito.when(tavernService.save(Mockito.any(TavernEntity.class))).thenReturn(new TavernEntity());
        Mockito.when(message.getText()).thenReturn(YOSHKAR_OLA.getDescription());

        UserEntity user = generateTestUser(State.REGISTRATION_TAVERN, SubState.CHOICE_CITY);
        doAnswer(invocation -> user.setSubState(SubState.ENTER_ADDRESS)).when(userService).updateSubState(any(), any());

        SendMessage sendMessage = registrationTavernHandler.handle(user, message, null);

        assertEquals(State.REGISTRATION_TAVERN, user.getState());
        assertEquals(SubState.ENTER_ADDRESS, user.getSubState());

        assertAll("sendMessage",
                () -> assertEquals(MessageText.ENTER_ADDRESS, sendMessage.getText()),
                () -> assertTrue(((ReplyKeyboardRemove) sendMessage.getReplyMarkup()).getRemoveKeyboard()));
    }

    @Test
    void client_in_ENTER_ADDRESS_SUB_STATE_and_entered_your_address() {
        Mockito.when(tavernService.save(Mockito.any(TavernEntity.class))).thenReturn(new TavernEntity());
        Mockito.when(message.getText()).thenReturn("Улица Ульянова, д. 132");

        UserEntity user = generateTestUser(State.REGISTRATION_TAVERN, SubState.ENTER_ADDRESS);
        doAnswer(invocation -> user.setSubState(SubState.ENTER_PHONE_NUMBER))
                .when(userService).updateSubState(any(), any());

        SendMessage sendMessage = registrationTavernHandler.handle(user, message, null);

        assertEquals(State.REGISTRATION_TAVERN, user.getState());
        assertEquals(SubState.ENTER_PHONE_NUMBER, user.getSubState());

        assertAll("sendMessage",
                () -> assertEquals(MessageText.SHARE_PHONE_NUMBER, sendMessage.getText()),
                () -> assertEquals(1, ((ReplyKeyboardMarkup) sendMessage.getReplyMarkup()).getKeyboard().size()),
                () -> assertEquals(Button.SHARE_PHONE.getText(),
                        ((ReplyKeyboardMarkup) sendMessage.getReplyMarkup()).getKeyboard()
                                .get(0)
                                .get(0)
                                .getText()),
                () -> assertTrue(((ReplyKeyboardMarkup) sendMessage.getReplyMarkup()).getKeyboard()
                        .get(0)
                        .get(0)
                        .getRequestContact())
        );
    }

    @Test
    void client_in_ENTER_PHONE_NUMBER_SUB_STATE_and_entered_any_text() {
        Mockito.when(message.getText()).thenReturn("lkjljl");

        UserEntity user = generateTestUser(State.REGISTRATION_TAVERN, SubState.ENTER_PHONE_NUMBER);
        user.getContacts().clear();

        SendMessage sendMessage = registrationTavernHandler.handle(user, message, null);

        assertEquals(State.REGISTRATION_TAVERN, user.getState());
        assertEquals(SubState.ENTER_PHONE_NUMBER, user.getSubState());

        assertAll("sendMessage",
                () -> assertEquals(MessageText.SHARE_PHONE_NUMBER, sendMessage.getText()),
                () -> assertEquals(1, ((ReplyKeyboardMarkup) sendMessage.getReplyMarkup()).getKeyboard().size()),
                () -> assertEquals(Button.SHARE_PHONE.getText(),
                        ((ReplyKeyboardMarkup) sendMessage.getReplyMarkup()).getKeyboard()
                                .get(0)
                                .get(0)
                                .getText()),
                () -> assertTrue(((ReplyKeyboardMarkup) sendMessage.getReplyMarkup()).getKeyboard()
                        .get(0)
                        .get(0)
                        .getRequestContact())
        );
    }

    @Test
    void client_in_ENTER_PHONE_NUMBER_SUB_STATE_and_shared_incorrect_phone_number() {
        Contact contact = new Contact();
        contact.setPhoneNumber("+792085958678");
        Mockito.when(message.getContact()).thenReturn(contact);

        Mockito.when(validationService.isNotValidPhone(Mockito.any())).thenReturn(true);
        UserEntity user = generateTestUser(State.REGISTRATION_TAVERN, SubState.ENTER_PHONE_NUMBER);

        SendMessage sendMessage = registrationTavernHandler.handle(user, message, null);

        assertEquals(State.REGISTRATION_TAVERN, user.getState());
        assertEquals(SubState.ENTER_PHONE_NUMBER, user.getSubState());

        assertAll("sendMessage",
                () -> assertEquals(MessageText.INCORRECT_ENTER_PHONE_NUMBER, sendMessage.getText()),
                () -> assertEquals(1, ((ReplyKeyboardMarkup) sendMessage.getReplyMarkup()).getKeyboard().size()),
                () -> assertEquals(Button.SHARE_PHONE.getText(),
                        ((ReplyKeyboardMarkup) sendMessage.getReplyMarkup()).getKeyboard()
                                .get(0)
                                .get(0)
                                .getText()),
                () -> assertTrue(((ReplyKeyboardMarkup) sendMessage.getReplyMarkup()).getKeyboard()
                        .get(0)
                        .get(0)
                        .getRequestContact())
        );
    }

    @Test
    void client_in_ENTER_PHONE_NUMBER_SUB_STATE_and_share_correct_phone_number() {
        Contact contact = new Contact();
        contact.setPhoneNumber("+79208595867");
        Mockito.when(message.getContact()).thenReturn(contact);
        Mockito.when(validationService.isNotValidPhone(Mockito.anyString())).thenReturn(false);

        String newPhoneNumber = "89208595867";
        when(contactService.preparePhoneNumber(any())).thenReturn(newPhoneNumber);

        UserEntity user = generateTestUser(State.REGISTRATION_TAVERN, SubState.ENTER_PHONE_NUMBER);
        user.getContacts().clear();
        when(contactService.findByUser(any())).thenReturn(user.getContacts());
        doAnswer(invocation ->
                user.getContacts().add(new ContactEntity(user.getTavern(), user, ContractType.MOBILE, newPhoneNumber)))
                .when(contactService).save(any());
        doAnswer(invocation -> user.setSubState(SubState.REGISTRATION_APPROVING)).when(userService).updateSubState(any(), any());

        SendMessage sendMessage = registrationTavernHandler.handle(user, message, null);

        String expectedMessageText = "<b>Ваши данные</b>" + System.lineSeparator() +
                "Имя: <i>Иванов Иван Иванович</i>" + System.lineSeparator() +
                "Заведение: <i>Августин</i>" + System.lineSeparator() +
                "Описание: <i>отсутствует</i>" + System.lineSeparator() +
                "Адрес: <i>Почтовая 136, д. 12</i>" + System.lineSeparator() +
                "Номер телефона: <i>" + newPhoneNumber +"</i>" + System.lineSeparator();

        assertEquals(State.REGISTRATION_TAVERN, user.getState());
        assertEquals(SubState.REGISTRATION_APPROVING, user.getSubState());

        assertAll("sendMessage",
                () -> assertEquals(expectedMessageText, sendMessage.getText()),
                () -> assertEquals(1,
                        ((ReplyKeyboardMarkup) sendMessage.getReplyMarkup()).getKeyboard().size()),
                () -> assertEquals(2,
                        ((ReplyKeyboardMarkup) sendMessage.getReplyMarkup()).getKeyboard().get(0).size()),
                () -> assertEquals(Button.EDIT.getText(), ((ReplyKeyboardMarkup) sendMessage.getReplyMarkup())
                        .getKeyboard().get(0).get(0).getText()),
                () -> assertEquals(Button.APPROVE.getText(), ((ReplyKeyboardMarkup) sendMessage.getReplyMarkup())
                        .getKeyboard().get(0).get(1).getText())
        );
    }

    @Test
    void client_in_REGISTRATION_APPROVING_SUB_STATE_and_entered_any_text() {
        UserEntity user = generateTestUser(State.REGISTRATION_TAVERN, SubState.REGISTRATION_APPROVING);
        when(contactService.findByUser(any())).thenReturn(user.getContacts());

        String expectedMessageText = "<b>Ваши данные</b>" + System.lineSeparator() +
                "Имя: <i>Иванов Иван Иванович</i>" + System.lineSeparator() +
                "Заведение: <i>Августин</i>" + System.lineSeparator() +
                "Описание: <i>отсутствует</i>" + System.lineSeparator() +
                "Адрес: <i>Почтовая 136, д. 12</i>" + System.lineSeparator() +
                "Номер телефона: <i>+79208586754</i>" + System.lineSeparator();

        SendMessage sendMessage = registrationTavernHandler.handle(
                user,
                message,
                null
        );

        assertEquals(user.getSubState(), SubState.REGISTRATION_APPROVING);
        assertEquals(sendMessage.getText(), expectedMessageText);

        assertAll("keyboard",
                () -> assertEquals(1,
                        ((ReplyKeyboardMarkup) sendMessage.getReplyMarkup()).getKeyboard().size()),
                () -> assertEquals(2,
                        ((ReplyKeyboardMarkup) sendMessage.getReplyMarkup()).getKeyboard().get(0).size()),
                () -> assertEquals(Button.EDIT.getText(), ((ReplyKeyboardMarkup) sendMessage.getReplyMarkup())
                        .getKeyboard().get(0).get(0).getText()),
                () -> assertEquals(Button.APPROVE.getText(), ((ReplyKeyboardMarkup) sendMessage.getReplyMarkup())
                        .getKeyboard().get(0).get(1).getText()));
    }

    @Test
    void client_in_REGISTRATION_APPROVING_SUB_STATE_and_click_approve_button() {
        UserEntity user = generateTestUser(State.REGISTRATION_TAVERN, SubState.REGISTRATION_APPROVING);
        Mockito.when(telegramApiService.prepareSendMessage(Mockito.any(), Mockito.any(), Mockito.anyBoolean(), Mockito.any())).thenReturn(Mono.never());
        Mockito.when(userService.findUsersByRole(Mockito.any(Role.class))).thenReturn(Collections.singletonList(user));
        Mockito.when(message.getText()).thenReturn(Button.APPROVE.getText());
        doAnswer(invocation -> user.setSubState(SubState.WAITING_APPROVE_APPLICATION)).when(userService).updateSubState(any(), any());

        SendMessage sendMessage = registrationTavernHandler.handle(user, message, callback);

        assertEquals(user.getSubState(), SubState.WAITING_APPROVE_APPLICATION);
        assertEquals(sendMessage.getText(), MessageText.WAITING_APPROVE_APPLICATION);
    }

    @Test
    void client_in_REGISTRATION_APPROVING_SUB_STATE_and_click_edit_button() {
        UserEntity user = generateTestUser(State.REGISTRATION_TAVERN, SubState.REGISTRATION_APPROVING);
        Mockito.when(message.getText()).thenReturn(Button.EDIT.getText());

        SendMessage sendMessage = registrationTavernHandler.handle(
                user,
                message,
                callback
        );

        assertEquals(user.getSubState(), SubState.EDIT_PERSONAL_DATA);
        assertEquals(sendMessage.getText(), SELECT_ELEMENT_FOR_EDIT);

        assertNotNull(sendMessage.getReplyMarkup());
    }

    @Test
    void client_in_EDIT_PERSONAL_DATA_SUB_STATE_and_entered_any_text() {
        UserEntity user = generateTestUser(State.REGISTRATION_TAVERN, SubState.EDIT_PERSONAL_DATA);

        SendMessage sendMessage = registrationTavernHandler.handle(
                user,
                message,
                null
        );

        assertEquals(user.getSubState(), SubState.EDIT_PERSONAL_DATA);
        assertEquals(sendMessage.getText(), SELECT_ELEMENT_FOR_EDIT);
    }

    @Test
    void client_in_EDIT_PERSONAL_DATA_SUB_STATE_and_click_edit_name() {
        UserEntity user = generateTestUser(State.REGISTRATION_TAVERN, SubState.EDIT_PERSONAL_DATA);
        when(message.getText()).thenReturn(Button.NAME.getText());
        doAnswer(invocation -> user.setSubState(SubState.EDIT_NAME)).when(userService).updateSubState(any(), any());

        SendMessage sendMessage = registrationTavernHandler.handle(user, message, null);

        assertEquals(user.getSubState(), SubState.EDIT_NAME);
        assertEquals(sendMessage.getText(), SubState.ENTER_FULL_NAME.getMessage());

        assertAll("keyboard",
                () -> assertEquals(1,
                        ((ReplyKeyboardMarkup) sendMessage.getReplyMarkup()).getKeyboard().size()),
                () -> assertEquals(2,
                        ((ReplyKeyboardMarkup) sendMessage.getReplyMarkup()).getKeyboard().get(0).size()),
                () -> assertEquals(Button.EDIT_MENU.getText(), ((ReplyKeyboardMarkup) sendMessage.getReplyMarkup())
                        .getKeyboard().get(0).get(0).getText()),
                () -> assertEquals(Button.COMPLETE_REGISTRATION.getText(), ((ReplyKeyboardMarkup) sendMessage.getReplyMarkup())
                        .getKeyboard().get(0).get(1).getText()));
    }

    @Test
    void client_in_EDIT_PERSONAL_DATA_SUB_STATE_and_click_edit_tavern_name() {
        UserEntity user = generateTestUser(State.REGISTRATION_TAVERN, SubState.EDIT_PERSONAL_DATA);
        when(message.getText()).thenReturn(Button.TAVERN_NAME.getText());
        doAnswer(invocation -> user.setSubState(SubState.EDIT_TAVERN)).when(userService).updateSubState(any(), any());

        SendMessage sendMessage = registrationTavernHandler.handle(user, message, null);


        assertEquals(user.getSubState(), SubState.EDIT_TAVERN);
        assertEquals(sendMessage.getText(), SubState.ENTER_TAVERN_NAME.getMessage());

        assertAll("keyboard",
                () -> assertEquals(1,
                        ((ReplyKeyboardMarkup) sendMessage.getReplyMarkup()).getKeyboard().size()),
                () -> assertEquals(2,
                        ((ReplyKeyboardMarkup) sendMessage.getReplyMarkup()).getKeyboard().get(0).size()),
                () -> assertEquals(Button.EDIT_MENU.getText(), ((ReplyKeyboardMarkup) sendMessage.getReplyMarkup())
                        .getKeyboard().get(0).get(0).getText()),
                () -> assertEquals(Button.COMPLETE_REGISTRATION.getText(), ((ReplyKeyboardMarkup) sendMessage.getReplyMarkup())
                        .getKeyboard().get(0).get(1).getText()));
    }

    @Test
    void client_in_EDIT_PERSONAL_DATA_SUB_STATE_and_click_edit_description() {
        UserEntity user = generateTestUser(State.REGISTRATION_TAVERN, SubState.EDIT_PERSONAL_DATA);
        when(message.getText()).thenReturn(Button.DESCRIPTION.getText());
        doAnswer(invocation -> user.setSubState(SubState.EDIT_DESCRIPTION)).when(userService).updateSubState(any(), any());

        SendMessage sendMessage = registrationTavernHandler.handle(user, message, null);

        assertEquals(user.getSubState(), SubState.EDIT_DESCRIPTION);
        assertEquals(sendMessage.getText(), SubState.ENTER_TAVERN_DESCRIPTION.getMessage());

        assertAll("keyboard",
                () -> assertEquals(1,
                        ((ReplyKeyboardMarkup) sendMessage.getReplyMarkup()).getKeyboard().size()),
                () -> assertEquals(2,
                        ((ReplyKeyboardMarkup) sendMessage.getReplyMarkup()).getKeyboard().get(0).size()),
                () -> assertEquals(Button.EDIT_MENU.getText(), ((ReplyKeyboardMarkup) sendMessage.getReplyMarkup())
                        .getKeyboard().get(0).get(0).getText()),
                () -> assertEquals(Button.COMPLETE_REGISTRATION.getText(), ((ReplyKeyboardMarkup) sendMessage.getReplyMarkup())
                        .getKeyboard().get(0).get(1).getText()));
    }

    @Test
    void client_in_EDIT_PERSONAL_DATA_SUB_STATE_and_click_edit_address() {
        UserEntity user = generateTestUser(State.REGISTRATION_TAVERN, SubState.EDIT_PERSONAL_DATA);
        when(message.getText()).thenReturn(Button.TAVERN_ADDRESS.getText());
        doAnswer(invocation -> user.setSubState(SubState.EDIT_ADDRESS)).when(userService).updateSubState(any(), any());

        SendMessage sendMessage = registrationTavernHandler.handle(user, message, null);

        assertEquals(user.getSubState(), SubState.EDIT_ADDRESS);
        assertEquals(sendMessage.getText(), SubState.ENTER_ADDRESS.getMessage());

        assertAll("keyboard",
                () -> assertEquals(1,
                        ((ReplyKeyboardMarkup) sendMessage.getReplyMarkup()).getKeyboard().size()),
                () -> assertEquals(2,
                        ((ReplyKeyboardMarkup) sendMessage.getReplyMarkup()).getKeyboard().get(0).size()),
                () -> assertEquals(Button.EDIT_MENU.getText(), ((ReplyKeyboardMarkup) sendMessage.getReplyMarkup())
                        .getKeyboard().get(0).get(0).getText()),
                () -> assertEquals(Button.COMPLETE_REGISTRATION.getText(), ((ReplyKeyboardMarkup) sendMessage.getReplyMarkup())
                        .getKeyboard().get(0).get(1).getText()));
    }

    @Test
    void client_in_EDIT_PERSONAL_DATA_SUB_STATE_and_click_complete_registration() {
        UserEntity user = generateTestUser(State.REGISTRATION_TAVERN, SubState.EDIT_PERSONAL_DATA);
        Mockito.when(message.getText()).thenReturn(Button.COMPLETE_REGISTRATION.getText());
        Mockito.when(telegramApiService.prepareSendMessage(Mockito.any(), Mockito.any(), Mockito.anyBoolean(), Mockito.any())).thenReturn(Mono.never());
        Mockito.when(userService.findUsersByRole(Mockito.any(Role.class))).thenReturn(Collections.singletonList(user));
        doAnswer(invocation -> user.setSubState(SubState.WAITING_APPROVE_APPLICATION)).when(userService).updateSubState(any(), any());

        SendMessage sendMessage = registrationTavernHandler.handle(user, message, null);

        Assertions.assertAll(
                () -> assertEquals(SubState.WAITING_APPROVE_APPLICATION, user.getSubState()),
                () -> assertEquals(SubState.WAITING_APPROVE_APPLICATION.getMessage(), sendMessage.getText()),
                () -> assertTrue(((ReplyKeyboardRemove) sendMessage.getReplyMarkup()).getRemoveKeyboard())
        );
    }

    @Test
    void client_in_EDIT_NAME_SUB_STATE_and_entered_any_text() {
        UserEntity user = generateTestUser(State.REGISTRATION_TAVERN, SubState.EDIT_NAME);
        Mockito.when(message.getText()).thenReturn("Some name");

        registrationTavernHandler.handle(user, message, null);

        assertAll("user",
                () -> assertEquals(user.getSubState(), SubState.EDIT_NAME),
                () -> assertEquals(user.getName(), message.getText()));
    }

    @Test
    void client_in_EDIT_NAME_SUB_STATE_and_press_menu_button() {
        UserEntity user = generateTestUser(State.REGISTRATION_TAVERN, SubState.EDIT_NAME);
        Mockito.when(message.getText()).thenReturn(Button.EDIT_MENU.getText());
        doAnswer(invocation -> user.setSubState(SubState.EDIT_PERSONAL_DATA)).when(userService).updateSubState(any(), any());

        SendMessage sendMessage = registrationTavernHandler.handle(user, message, null);

        assertAll("user",
                () -> assertEquals(user.getSubState(), SubState.EDIT_PERSONAL_DATA),
                () -> assertEquals(SELECT_ELEMENT_FOR_EDIT, sendMessage.getText()));

        assertAll("keyboard",
                () -> assertEquals(3, ((ReplyKeyboardMarkup) sendMessage.getReplyMarkup()).getKeyboard().size()),
                () -> assertEquals(2, ((ReplyKeyboardMarkup) sendMessage.getReplyMarkup()).getKeyboard().get(0).size()),
                () -> assertEquals(Button.NAME.getText(), ((ReplyKeyboardMarkup) sendMessage.getReplyMarkup()).getKeyboard().get(0).get(0).getText()),
                () -> assertEquals(Button.TAVERN_NAME.getText(), ((ReplyKeyboardMarkup) sendMessage.getReplyMarkup()).getKeyboard().get(0).get(1).getText()),
                () -> assertEquals(2, ((ReplyKeyboardMarkup) sendMessage.getReplyMarkup()).getKeyboard().get(1).size()),
                () -> assertEquals(Button.DESCRIPTION.getText(), ((ReplyKeyboardMarkup) sendMessage.getReplyMarkup()).getKeyboard().get(1).get(0).getText()),
                () -> assertEquals(Button.TAVERN_ADDRESS.getText(), ((ReplyKeyboardMarkup) sendMessage.getReplyMarkup()).getKeyboard().get(1).get(1).getText()),
                () -> assertEquals(1, ((ReplyKeyboardMarkup) sendMessage.getReplyMarkup()).getKeyboard().get(2).size()),
                () -> assertEquals(Button.COMPLETE_REGISTRATION.getText(), ((ReplyKeyboardMarkup) sendMessage.getReplyMarkup()).getKeyboard().get(2).get(0).getText())
        );
    }

    @Test
    void client_in_EDIT_NAME_SUB_STATE_and_press_any_button() {
        UserEntity user = generateTestUser(State.REGISTRATION_TAVERN, SubState.EDIT_NAME);
        Mockito.when(message.getText()).thenReturn(Button.REGISTRATION.getText());

        SendMessage sendMessage = registrationTavernHandler.handle(user, message, null);

        assertEquals(SubState.EDIT_NAME, user.getSubState());

        assertAll("keyboard",
                () -> assertEquals(1,
                        ((ReplyKeyboardMarkup) sendMessage.getReplyMarkup()).getKeyboard().size()),
                () -> assertEquals(2,
                        ((ReplyKeyboardMarkup) sendMessage.getReplyMarkup()).getKeyboard().get(0).size()),
                () -> assertEquals(Button.EDIT_MENU.getText(), ((ReplyKeyboardMarkup) sendMessage.getReplyMarkup())
                        .getKeyboard().get(0).get(0).getText()),
                () -> assertEquals(Button.COMPLETE_REGISTRATION.getText(), ((ReplyKeyboardMarkup) sendMessage.getReplyMarkup())
                        .getKeyboard().get(0).get(1).getText()));
    }

    @Test
    void client_in_EDIT_TAVERN_SUB_STATE_and_entered_any_text() {
        UserEntity user = generateTestUser(State.REGISTRATION_TAVERN, SubState.EDIT_TAVERN);
        String newTavernName = "Some tavern name";
        Mockito.when(message.getText()).thenReturn(newTavernName);

        registrationTavernHandler.handle(user, message, null);

        assertAll("user",
                () -> assertEquals(user.getSubState(), SubState.EDIT_TAVERN),
                () -> assertEquals(newTavernName, user.getTavern().getName()));
    }

    @Test
    void client_in_EDIT_TAVERN_SUB_STATE_and_press_menu_button() {
        UserEntity user = generateTestUser(State.REGISTRATION_TAVERN, SubState.EDIT_TAVERN);
        Mockito.when(message.getText()).thenReturn(Button.EDIT_MENU.getText());
        doAnswer(invocation -> user.setSubState(SubState.EDIT_PERSONAL_DATA)).when(userService).updateSubState(any(), any());

        SendMessage sendMessage = registrationTavernHandler.handle(user, message, null);

        assertAll("user",
                () -> assertEquals(SubState.EDIT_PERSONAL_DATA, user.getSubState()),
                () -> assertEquals(SELECT_ELEMENT_FOR_EDIT, sendMessage.getText()));

        assertAll("keyboard",
                () -> assertEquals(3, ((ReplyKeyboardMarkup) sendMessage.getReplyMarkup()).getKeyboard().size()),
                () -> assertEquals(2, ((ReplyKeyboardMarkup) sendMessage.getReplyMarkup()).getKeyboard().get(0).size()),
                () -> assertEquals(Button.NAME.getText(), ((ReplyKeyboardMarkup) sendMessage.getReplyMarkup()).getKeyboard().get(0).get(0).getText()),
                () -> assertEquals(Button.TAVERN_NAME.getText(), ((ReplyKeyboardMarkup) sendMessage.getReplyMarkup()).getKeyboard().get(0).get(1).getText()),
                () -> assertEquals(2, ((ReplyKeyboardMarkup) sendMessage.getReplyMarkup()).getKeyboard().get(1).size()),
                () -> assertEquals(Button.DESCRIPTION.getText(), ((ReplyKeyboardMarkup) sendMessage.getReplyMarkup()).getKeyboard().get(1).get(0).getText()),
                () -> assertEquals(Button.TAVERN_ADDRESS.getText(), ((ReplyKeyboardMarkup) sendMessage.getReplyMarkup()).getKeyboard().get(1).get(1).getText()),
                () -> assertEquals(1, ((ReplyKeyboardMarkup) sendMessage.getReplyMarkup()).getKeyboard().get(2).size()),
                () -> assertEquals(Button.COMPLETE_REGISTRATION.getText(), ((ReplyKeyboardMarkup) sendMessage.getReplyMarkup()).getKeyboard().get(2).get(0).getText())
        );
    }

    @Test
    void client_in_EDIT_DESCRIPTION_SUB_STATE_and_entered_any_text() {
        UserEntity user = generateTestUser(State.REGISTRATION_TAVERN, SubState.EDIT_DESCRIPTION);
        String newTavernDescription = "Some new tavern description";
        Mockito.when(message.getText()).thenReturn(newTavernDescription);

        registrationTavernHandler.handle(user, message, null);

        assertAll("user",
                () -> assertEquals(user.getSubState(), SubState.EDIT_DESCRIPTION),
                () -> assertEquals(newTavernDescription, user.getTavern().getDescription()));
    }

    @Test
    void client_in_EDIT_DESCRIPTION_SUB_STATE_and_press_menu_button() {
        UserEntity user = generateTestUser(State.REGISTRATION_TAVERN, SubState.EDIT_DESCRIPTION);
        Mockito.when(message.getText()).thenReturn(Button.EDIT_MENU.getText());
        doAnswer(invocation -> user.setSubState(SubState.EDIT_PERSONAL_DATA)).when(userService).updateSubState(any(), any());

        SendMessage sendMessage = registrationTavernHandler.handle(user, message, null);

        assertAll("user",
                () -> assertEquals(SubState.EDIT_PERSONAL_DATA, user.getSubState()),
                () -> assertEquals(SELECT_ELEMENT_FOR_EDIT, sendMessage.getText()));

        assertAll("keyboard",
                () -> assertEquals(3, ((ReplyKeyboardMarkup) sendMessage.getReplyMarkup()).getKeyboard().size()),
                () -> assertEquals(2, ((ReplyKeyboardMarkup) sendMessage.getReplyMarkup()).getKeyboard().get(0).size()),
                () -> assertEquals(Button.NAME.getText(), ((ReplyKeyboardMarkup) sendMessage.getReplyMarkup()).getKeyboard().get(0).get(0).getText()),
                () -> assertEquals(Button.TAVERN_NAME.getText(), ((ReplyKeyboardMarkup) sendMessage.getReplyMarkup()).getKeyboard().get(0).get(1).getText()),
                () -> assertEquals(2, ((ReplyKeyboardMarkup) sendMessage.getReplyMarkup()).getKeyboard().get(1).size()),
                () -> assertEquals(Button.DESCRIPTION.getText(), ((ReplyKeyboardMarkup) sendMessage.getReplyMarkup()).getKeyboard().get(1).get(0).getText()),
                () -> assertEquals(Button.TAVERN_ADDRESS.getText(), ((ReplyKeyboardMarkup) sendMessage.getReplyMarkup()).getKeyboard().get(1).get(1).getText()),
                () -> assertEquals(1, ((ReplyKeyboardMarkup) sendMessage.getReplyMarkup()).getKeyboard().get(2).size()),
                () -> assertEquals(Button.COMPLETE_REGISTRATION.getText(), ((ReplyKeyboardMarkup) sendMessage.getReplyMarkup()).getKeyboard().get(2).get(0).getText())
        );
    }

    @Test
    void client_in_EDIT_ADDRESS_SUB_STATE_and_entered_any_text() {
        UserEntity user = generateTestUser(State.REGISTRATION_TAVERN, SubState.EDIT_ADDRESS);
        String newTavernAddress = "Some new tavern address";
        Mockito.when(message.getText()).thenReturn(newTavernAddress);

        registrationTavernHandler.handle(user, message, null);

        assertAll("user",
                () -> assertEquals(user.getSubState(), SubState.EDIT_ADDRESS),
                () -> assertEquals(newTavernAddress, user.getTavern().getAddress().getStreet()));
    }

    @Test
    void client_in_EDIT_ADDRESS_SUB_STATE_and_press_menu_button() {
        UserEntity user = generateTestUser(State.REGISTRATION_TAVERN, SubState.EDIT_ADDRESS);
        Mockito.when(message.getText()).thenReturn(Button.EDIT_MENU.getText());
        doAnswer(invocation -> user.setSubState(SubState.EDIT_PERSONAL_DATA)).when(userService).updateSubState(any(), any());

        SendMessage sendMessage = registrationTavernHandler.handle(user, message, null);

        assertAll("user",
                () -> assertEquals(SubState.EDIT_PERSONAL_DATA, user.getSubState()),
                () -> assertEquals(SELECT_ELEMENT_FOR_EDIT, sendMessage.getText()));

        assertAll("keyboard",
                () -> assertEquals(3, ((ReplyKeyboardMarkup) sendMessage.getReplyMarkup()).getKeyboard().size()),
                () -> assertEquals(2, ((ReplyKeyboardMarkup) sendMessage.getReplyMarkup()).getKeyboard().get(0).size()),
                () -> assertEquals(Button.NAME.getText(), ((ReplyKeyboardMarkup) sendMessage.getReplyMarkup()).getKeyboard().get(0).get(0).getText()),
                () -> assertEquals(Button.TAVERN_NAME.getText(), ((ReplyKeyboardMarkup) sendMessage.getReplyMarkup()).getKeyboard().get(0).get(1).getText()),
                () -> assertEquals(2, ((ReplyKeyboardMarkup) sendMessage.getReplyMarkup()).getKeyboard().get(1).size()),
                () -> assertEquals(Button.DESCRIPTION.getText(), ((ReplyKeyboardMarkup) sendMessage.getReplyMarkup()).getKeyboard().get(1).get(0).getText()),
                () -> assertEquals(Button.TAVERN_ADDRESS.getText(), ((ReplyKeyboardMarkup) sendMessage.getReplyMarkup()).getKeyboard().get(1).get(1).getText()),
                () -> assertEquals(1, ((ReplyKeyboardMarkup) sendMessage.getReplyMarkup()).getKeyboard().get(2).size()),
                () -> assertEquals(Button.COMPLETE_REGISTRATION.getText(), ((ReplyKeyboardMarkup) sendMessage.getReplyMarkup()).getKeyboard().get(2).get(0).getText())
        );
    }

    @Test
    void client_in_EDIT_ADDRESS_SUB_STATE_and_press_complete_registration_button() {
        UserEntity user = generateTestUser(State.REGISTRATION_TAVERN, SubState.EDIT_ADDRESS);
        Mockito.when(message.getText()).thenReturn(Button.COMPLETE_REGISTRATION.getText());
        Mockito.when(telegramApiService.prepareSendMessage(Mockito.any(), Mockito.any(), Mockito.anyBoolean(), Mockito.any())).thenReturn(Mono.never());
        Mockito.when(userService.findUsersByRole(Mockito.any(Role.class))).thenReturn(Collections.singletonList(user));
        doAnswer(invocation -> user.setSubState(SubState.WAITING_APPROVE_APPLICATION)).when(userService).updateSubState(any(), any());

        SendMessage sendMessage = registrationTavernHandler.handle(user, message, null);

        assertAll("user",
                () -> assertEquals(SubState.WAITING_APPROVE_APPLICATION, user.getSubState()),
                () -> assertEquals(SubState.WAITING_APPROVE_APPLICATION.getMessage(), sendMessage.getText()),
                () -> assertTrue(((ReplyKeyboardRemove) sendMessage.getReplyMarkup()).getRemoveKeyboard()));
    }

    @Test
    void client_in_WAITING_APPROVE_APPLICATION_SUB_STATE_and_entered_any_text() {
        UserEntity user = generateTestUser(State.REGISTRATION_TAVERN, SubState.WAITING_APPROVE_APPLICATION);
        Mockito.when(message.getText()).thenReturn("Some text");

        SendMessage sendMessage = registrationTavernHandler.handle(user, message, null);

        assertAll("user",
                () -> assertEquals(user.getSubState(), SubState.WAITING_APPROVE_APPLICATION),
                () -> assertEquals(CLAIM_APPROVE_WAIT, sendMessage.getText()));
    }

    public static UserEntity generateTestUser(State state, SubState subState) {
        UserEntity user = new UserEntity(1L, state, subState, UserType.CLIENT);
        user.setName("Иванов Иван Иванович");
        user.setRoles(Collections.singleton(Role.CLIENT_ADMIN));
        user.setTavern(new TavernEntity());
        user.getTavern().setName("Августин");

        user.getTavern().setAddress(new AddressEntity());
        user.getTavern().getAddress().setStreet("Почтовая 136, д. 12");
        user.getTavern().getAddress().setCity(YOSHKAR_OLA);

        ContactEntity contact = new ContactEntity();
        contact.setUser(user);
        contact.setTavern(user.getTavern());
        contact.setActive(true);
        contact.setType(ContractType.MOBILE);
        contact.setValue("+79208586754");
        user.getContacts().add(contact);

        return user;
    }
}
