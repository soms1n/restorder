package ru.privetdruk.restorder.service;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;

@DisplayName("Checking the update processing logic")
@ExtendWith(MockitoExtension.class)
@DisplayNameGeneration(value = DisplayNameGenerator.ReplaceUnderscores.class)
@SpringBootTest
@Disabled
class UserBotServiceTest {
   /* @Mock
    UserService userService;
    @Mock
    TelegramApiService telegramApiService;
    @Mock
    StringService stringService;
    @Mock
    ContactService contactService;
    @Mock
    TavernService tavernService;
    @Mock
    BlacklistService blacklistService;
    @InjectMocks
    UserBotService userBotService;
    @InjectMocks
    ReserveRepository reserveRepository;
    @InjectMocks
    TavernRepository tavernRepository;

    @BeforeEach
    @DisplayName("Presets")
    void beforeEach() {
        BookingHandler bookingHandler = new BookingHandler(
                contactService,
                new InfoService(blacklistService, contactService, stringService, tavernService),
                new MessageService(),
                new ReserveService(reserveRepository),
                new StringService(),
                new TavernService(tavernRepository),
                telegramApiService,
                userService
        );

        userBotService = new UserBotService(
                userService,
                new UserHandlerService(
                        new RegistrationHandler(
                                bookingHandler,
                                contactService,
                                userService,
                                new ValidationService()
                        ),
                        bookingHandler
                )
        );
    }

    @Test
    void handleUpdate_with_message_and_without_callback() {
        Update update = new Update();
        update.setCallbackQuery(null);

        Message message = new Message();
        message.setFrom(new User(1L, "Sergey", false));
        Long chatId = 111_111L;
        message.setChat(new Chat(chatId, "some type"));
        update.setMessage(message);

        UserEntity user = new UserEntity(1L, State.BOOKING, State.BOOKING.getInitialSubState(), UserType.USER);

        Mockito.when(userService.findByTelegramId(1L, UserType.USER))
                .thenReturn(Optional.of(user));

        SendMessage sendMessage = userBotService.handleUpdate(update);

        Assertions.assertEquals(sendMessage.getChatId(), String.valueOf(chatId));
        Assertions.assertEquals(sendMessage.getText(), MessageText.GREETING);
    }

    @Test
    void handleUpdate_with_callback_and_without_message() {
        Update update = new Update();

        Message message = new Message();
        Long chatId = 111_111L;
        message.setChat(new Chat(chatId, "some type"));
        CallbackQuery callbackQuery = new CallbackQuery();
        callbackQuery.setMessage(message);

        update.setCallbackQuery(callbackQuery);
        UserEntity user = new UserEntity(1L, State.BOOKING, State.BOOKING.getInitialSubState(), UserType.USER);

        Mockito.when(userService.findByTelegramId(chatId, UserType.USER)).thenReturn(Optional.empty());
        Mockito.when(userService.create(chatId, State.BOOKING, Role.USER, UserType.USER)).thenReturn(user);

        SendMessage sendMessage = userBotService.handleUpdate(update);

        Assertions.assertEquals(sendMessage.getChatId(), String.valueOf(chatId));
        Assertions.assertEquals(sendMessage.getText(), MessageText.GREETING);
    }*/
}