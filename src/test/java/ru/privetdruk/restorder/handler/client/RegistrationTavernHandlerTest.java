package ru.privetdruk.restorder.handler.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import reactor.core.publisher.Mono;
import ru.privetdruk.restorder.AbstractTest;
import ru.privetdruk.restorder.model.consts.MessageText;
import ru.privetdruk.restorder.model.entity.AddressEntity;
import ru.privetdruk.restorder.model.entity.ContactEntity;
import ru.privetdruk.restorder.model.entity.TavernEntity;
import ru.privetdruk.restorder.model.entity.UserEntity;
import ru.privetdruk.restorder.model.enums.*;
import ru.privetdruk.restorder.service.*;


import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static ru.privetdruk.restorder.model.consts.MessageText.SELECT_ELEMENT_FOR_EDIT;

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

    @BeforeEach
    @DisplayName("Presets")
    void beforeEach() {
        Mockito.when(message.getChatId()).thenReturn(1L);
        registrationTavernHandler = new RegistrationTavernHandler(new KeyboardService(), new MessageService(), userService, telegramApiService, tavernService);
    }

    @Test
    void client_In_ShowRegistrationButton_SubState_And_Does_Not_Click_Registration_Button() {
        UserEntity user = generateTestUser(SubState.SHOW_REGISTER_BUTTON);
        Mockito.when(message.getText()).thenReturn("Any string");

        SendMessage sendMessage = registrationTavernHandler.handle(
                user,
                message,
                null
        );

        assertEquals(user.getSubState(), SubState.SHOW_REGISTER_BUTTON);
        assertAll("sendMessage",
                () -> assertEquals(sendMessage.getText(), MessageText.REGISTER),
                () -> assertEquals(((InlineKeyboardMarkup)sendMessage.getReplyMarkup()).getKeyboard().size(), 1),
                () -> assertEquals(((InlineKeyboardMarkup)sendMessage.getReplyMarkup()).getKeyboard().get(0).size(), 1),
                () -> assertEquals(((InlineKeyboardMarkup)sendMessage.getReplyMarkup()).getKeyboard().get(0).get(0).getCallbackData(), Button.REGISTRATION.getName()),
                () -> assertEquals(((InlineKeyboardMarkup)sendMessage.getReplyMarkup()).getKeyboard().get(0).get(0).getText(), Button.REGISTRATION.getText()));

    }

    @Test
    void client_In_ShowRegistrationButton_SubState_And_Click_Registration_Button() {
        Mockito.doNothing().when(userService).save(Mockito.any(UserEntity.class));
        UserEntity user = generateTestUser(SubState.SHOW_REGISTER_BUTTON);

        SendMessage sendMessage = registrationTavernHandler.handle(
                user,
                message,
                callback
        );

        assertEquals(user.getSubState(), SubState.ENTER_FULL_NAME);
        assertEquals(sendMessage.getText(), SubState.ENTER_FULL_NAME.getMessage());
    }

    @Test
    void client_In_EnterFullName_SubState_And_Entered_Your_Name() {
        Mockito.doNothing().when(userService).save(Mockito.any(UserEntity.class));
        UserEntity user = generateTestUser(SubState.ENTER_FULL_NAME);

        SendMessage sendMessage = registrationTavernHandler.handle(
                user,
                message,
                callback
        );

        assertEquals(user.getSubState(), SubState.ENTER_TAVERN_NAME);
        assertEquals(sendMessage.getText(), SubState.ENTER_TAVERN_NAME.getMessage());
    }

    @Test
    void client_In_EnterTavernName_SubState_And_Entered_Tavern_Name() {
        Mockito.doNothing().when(userService).save(Mockito.any(UserEntity.class));
        UserEntity user = generateTestUser(SubState.ENTER_TAVERN_NAME);

        SendMessage sendMessage = registrationTavernHandler.handle(
                user,
                message,
                callback
        );

        assertEquals(user.getSubState(), SubState.CHOICE_CITY);

        assertAll("sendMessage",
                () -> assertEquals(sendMessage.getText(), MessageText.CHOICE_CITY),
                () -> assertEquals(((InlineKeyboardMarkup)sendMessage.getReplyMarkup()).getKeyboard().size(), 1),
                () -> assertEquals(((InlineKeyboardMarkup)sendMessage.getReplyMarkup()).getKeyboard().get(0).size(), City.values().length));
    }

    @Test
    void client_In_ChoiceCity_SubState_And_Entered_Any_Text() {
        UserEntity user = generateTestUser(SubState.CHOICE_CITY);

        SendMessage sendMessage = registrationTavernHandler.handle(
                user,
                message,
                null
        );

        assertEquals(user.getSubState(), SubState.CHOICE_CITY);
        assertAll("sendMessage",
                () -> assertEquals(sendMessage.getText(), MessageText.CHOICE_CITY),
                () -> assertEquals(((InlineKeyboardMarkup)sendMessage.getReplyMarkup()).getKeyboard().size(), 1),
                () -> assertEquals(((InlineKeyboardMarkup)sendMessage.getReplyMarkup()).getKeyboard().get(0).size(), City.values().length));
    }

    @Test
    void client_In_ChoiceCity_SubState_And_Selected_Any_City() {
        Mockito.doNothing().when(userService).save(Mockito.any(UserEntity.class));
        Mockito.when(callback.getData()).thenReturn(City.BRYANSK.getName());

        UserEntity user = generateTestUser(SubState.CHOICE_CITY);

        SendMessage sendMessage = registrationTavernHandler.handle(
                user,
                message,
                callback
        );

        assertEquals(user.getSubState(), SubState.ENTER_ADDRESS);
        assertEquals(sendMessage.getText(), SubState.ENTER_ADDRESS.getMessage());
    }

    @Test
    void client_In_EnterAddress_SubState_And_Entered_Your_Address() {
        UserEntity user = generateTestUser(SubState.ENTER_ADDRESS);

        SendMessage sendMessage = registrationTavernHandler.handle(
                user,
                message,
                null
        );

        assertEquals(user.getSubState(), SubState.ENTER_PHONE_NUMBER);
        assertEquals(sendMessage.getText(), SubState.ENTER_PHONE_NUMBER.getMessage());
    }

    @Test
    void client_In_EnterPhoneNumber_SubState_And_Entered_Your_Phone_Number() {
        UserEntity user = generateTestUser(SubState.ENTER_PHONE_NUMBER);
        String messageText =  "Ваши данные:" + System.lineSeparator() +
                "Имя: " + user.getName() + System.lineSeparator() +
                "Заведение: " + user.getTavern().getName() + System.lineSeparator() +
                "Адрес: " + user.getTavern().getAddress().getStreet() + System.lineSeparator() +
                "Номер телефона: " + user.getContacts()
                .stream()
                .filter(contactEntity -> contactEntity.getType() == ContractType.MOBILE)
                .map(ContactEntity::getValue)
                .findFirst()
                .orElse("") + System.lineSeparator();
        user.getContacts().clear();

        Mockito.when(message.getText()).thenReturn("+79208586754");

        Mockito.doNothing().when(userService).save(Mockito.any(UserEntity.class));

        SendMessage sendMessage = registrationTavernHandler.handle(
                user,
                message,
                null
        );

        assertEquals(user.getSubState(), SubState.REGISTRATION_APPROVING);
        assertEquals(sendMessage.getText(), messageText);

        assertAll("keyboard",
                () -> assertEquals(((InlineKeyboardMarkup)sendMessage.getReplyMarkup()).getKeyboard().size(), 1),
                () -> assertEquals(((InlineKeyboardMarkup)sendMessage.getReplyMarkup()).getKeyboard().get(0).size(), 2));
    }

    @Test
    void client_In_RegistrationApproving_SubState_And_Entered_Any_Text() {
        UserEntity user = generateTestUser(SubState.REGISTRATION_APPROVING);

        String messageText =  "Ваши данные:" + System.lineSeparator() +
                "Имя: " + user.getName() + System.lineSeparator() +
                "Заведение: " + user.getTavern().getName() + System.lineSeparator() +
                "Адрес: " + user.getTavern().getAddress().getStreet() + System.lineSeparator() +
                "Номер телефона: " + user.getContacts()
                .stream()
                .filter(contactEntity -> contactEntity.getType() == ContractType.MOBILE)
                .map(ContactEntity::getValue)
                .findFirst()
                .orElse("") + System.lineSeparator();

        SendMessage sendMessage = registrationTavernHandler.handle(
                user,
                message,
                null
        );

        assertEquals(user.getSubState(), SubState.REGISTRATION_APPROVING);
        assertEquals(sendMessage.getText(), messageText);

        assertAll("keyboard",
                () -> assertEquals(((InlineKeyboardMarkup)sendMessage.getReplyMarkup()).getKeyboard().size(), 1),
                () -> assertEquals(((InlineKeyboardMarkup)sendMessage.getReplyMarkup()).getKeyboard().get(0).size(), 2));
    }

    @Test
    void client_In_RegistrationApproving_SubState_And_Click_Approve_Button() {
        UserEntity user = generateTestUser(SubState.REGISTRATION_APPROVING);
        Mockito.doNothing().when(userService).save(Mockito.any(UserEntity.class));
        Mockito.when(telegramApiService.sendMessage(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(Mono.never());
        Mockito.when(userService.findUsersByRole(Mockito.any(Role.class))).thenReturn(Collections.singletonList(user));
        Mockito.when(callback.getData()).thenReturn(Button.APPROVE.getName());

        SendMessage sendMessage = registrationTavernHandler.handle(
                user,
                message,
                callback
        );

        assertEquals(user.getSubState(), SubState.WAITING_APPROVE_APPLICATION);
        assertEquals(sendMessage.getText(), SubState.WAITING_APPROVE_APPLICATION.getMessage());
    }

    @Test
    void client_In_RegistrationApproving_SubState_And_Click_Edit_Button() {
        UserEntity user = generateTestUser(SubState.REGISTRATION_APPROVING);
        Mockito.doNothing().when(userService).save(Mockito.any(UserEntity.class));
        Mockito.when(callback.getData()).thenReturn(Button.EDIT.getName());

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
    void client_In_EditPersonalData_SubState_And_Entered_Any_Text() {
        UserEntity user = generateTestUser(SubState.EDIT_PERSONAL_DATA);

        SendMessage sendMessage = registrationTavernHandler.handle(
                user,
                message,
                null
        );

        assertEquals(user.getSubState(), SubState.EDIT_PERSONAL_DATA);
        assertEquals(sendMessage.getText(), SELECT_ELEMENT_FOR_EDIT);
    }

    @Test
    void client_In_EditPersonalData_SubState_And_Click_Edit_Name() {
        Mockito.doNothing().when(userService).save(Mockito.any(UserEntity.class));
        UserEntity user = generateTestUser(SubState.EDIT_PERSONAL_DATA);
        Mockito.when(message.getText()).thenReturn(Button.NAME.getText());

        SendMessage sendMessage = registrationTavernHandler.handle(
                user,
                message,
                null
        );

        assertEquals(user.getSubState(), SubState.EDIT_NAME);
        assertEquals(sendMessage.getText(), SubState.ENTER_FULL_NAME.getMessage());

        assertNotNull(sendMessage.getReplyMarkup());
    }

    @Test
    void client_In_EditPersonalData_SubState_And_Click_Edit_Tavern() {
        Mockito.doNothing().when(userService).save(Mockito.any(UserEntity.class));
        UserEntity user = generateTestUser(SubState.EDIT_PERSONAL_DATA);
        Mockito.when(message.getText()).thenReturn(Button.TAVERN.getText());

        SendMessage sendMessage = registrationTavernHandler.handle(
                user,
                message,
                null
        );

        assertEquals(user.getSubState(), SubState.EDIT_TAVERN);
        assertEquals(sendMessage.getText(), SubState.ENTER_TAVERN_NAME.getMessage());
    }

    @Test
    void client_In_EditPersonalData_SubState_And_Click_Edit_Phone_Number() {
        Mockito.doNothing().when(userService).save(Mockito.any(UserEntity.class));
        UserEntity user = generateTestUser(SubState.EDIT_PERSONAL_DATA);
        Mockito.when(message.getText()).thenReturn(Button.PHONE_NUMBER.getText());

        SendMessage sendMessage = registrationTavernHandler.handle(
                user,
                message,
                null
        );

        assertEquals(user.getSubState(), SubState.EDIT_PHONE_NUMBER);
        assertEquals(sendMessage.getText(), SubState.ENTER_PHONE_NUMBER.getMessage());
    }

    @Test
    void client_In_EditPersonalData_SubState_And_Click_Edit_Address() {
        Mockito.doNothing().when(userService).save(Mockito.any(UserEntity.class));
        UserEntity user = generateTestUser(SubState.EDIT_PERSONAL_DATA);
        Mockito.when(message.getText()).thenReturn(Button.ADDRESS.getText());

        SendMessage sendMessage = registrationTavernHandler.handle(
                user,
                message,
                null
        );

        assertEquals(user.getSubState(), SubState.EDIT_ADDRESS);
        assertEquals(sendMessage.getText(), SubState.ENTER_ADDRESS.getMessage());
    }

    @Test
    void client_In_EditPersonalData_SubState_And_Click_Edit_City() {
        Mockito.doNothing().when(userService).save(Mockito.any(UserEntity.class));
        UserEntity user = generateTestUser(SubState.EDIT_PERSONAL_DATA);
        Mockito.when(message.getText()).thenReturn(Button.CITY.getText());


        SendMessage sendMessage = registrationTavernHandler.handle(
                user,
                message,
                null
        );

        assertEquals(user.getSubState(), SubState.EDIT_CITY);
        assertEquals(sendMessage.getText(), SubState.CHOICE_CITY.getMessage());
    }

    @Test
    void client_In_EditPersonalData_SubState_And_Click_Complete_Registration() {
        Mockito.doNothing().when(userService).save(Mockito.any(UserEntity.class));
        UserEntity user = generateTestUser(SubState.EDIT_PERSONAL_DATA);
        Mockito.when(message.getText()).thenReturn(Button.COMPLETE_REGISTRATION.getText());
        Mockito.when(telegramApiService.sendMessage(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(Mono.never());
        Mockito.when(userService.findUsersByRole(Mockito.any(Role.class))).thenReturn(Collections.singletonList(user));

        SendMessage sendMessage = registrationTavernHandler.handle(
                user,
                message,
                null
        );

        assertEquals(user.getSubState(), SubState.WAITING_APPROVE_APPLICATION);
        assertEquals(sendMessage.getText(), SubState.WAITING_APPROVE_APPLICATION.getMessage());
    }

    @Test
    void client_In_EditName_SubState_And_Entered_Any_Text() {
        UserEntity user = generateTestUser(SubState.EDIT_NAME);
        Mockito.when(message.getText()).thenReturn("Some name");

        registrationTavernHandler.handle(user, message, null);

        assertAll("user",
                () -> assertEquals(user.getSubState(), SubState.EDIT_NAME),
                () -> assertEquals(user.getName(), message.getText()));
    }

    private UserEntity generateTestUser(SubState subState) {
        UserEntity user = new UserEntity(1L, State.REGISTRATION_TAVERN, subState);
        user.setName("Иванов Иван Иванович");
        user.setRoles(Collections.singleton(Role.CLIENT_ADMIN));
        user.setTavern(new TavernEntity());
        user.getTavern().setName("Августин");

        user.getTavern().setAddress(new AddressEntity());
        user.getTavern().getAddress().setStreet("Почтовая 136, д. 12");
        user.getTavern().getAddress().setCity(City.YOSHKAR_OLA);

        ContactEntity contact = new ContactEntity();
        contact.setUser(user);
        contact.setTavern(user.getTavern());
        contact.setActive(true);
        contact.setType(ContractType.MOBILE);
        contact.setValue("+79208586754");
        user.getContacts().add(contact);

        return user;
    }

    @Disabled
    @Transactional()
    @Test
    void testRepo() {
        Optional<UserEntity> byTelegramIdWithLock = userService.findByTelegramIdWithLock(239_635_087L);
        byTelegramIdWithLock.get().setState(State.MAIN_MENU);
        userService.save(byTelegramIdWithLock.get());
    }

    @Disabled
    @Test
    void ttt() throws InterruptedException, JsonProcessingException {
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
                .state(State.REGISTRATION_TAVERN)
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
}
