package ru.privetdruk.restorder.service;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.*;
import ru.privetdruk.restorder.handler.user.BookingHandler;
import ru.privetdruk.restorder.handler.user.RegistrationHandler;
import ru.privetdruk.restorder.model.consts.MessageText;
import ru.privetdruk.restorder.model.entity.UserEntity;
import ru.privetdruk.restorder.model.enums.Role;
import ru.privetdruk.restorder.model.enums.State;
import ru.privetdruk.restorder.model.enums.UserType;
import ru.privetdruk.restorder.repository.ReserveRepository;
import ru.privetdruk.restorder.repository.ScheduleRepository;
import ru.privetdruk.restorder.repository.TavernRepository;
import ru.privetdruk.restorder.service.user.UserBotService;
import ru.privetdruk.restorder.service.user.UserHandlerService;
import ru.privetdruk.restorder.service.util.StringService;
import ru.privetdruk.restorder.service.util.ValidationService;

import java.util.Optional;

@DisplayName("Checking the update processing logic")
@ExtendWith(MockitoExtension.class)
@DisplayNameGeneration(value = DisplayNameGenerator.ReplaceUnderscores.class)
@SpringBootTest
@Disabled
class UserBotServiceTest {
    @Mock
    UserService userService;
    @InjectMocks
    UserBotService userBotService;
    @InjectMocks
    ReserveRepository reserveRepository;
    @InjectMocks
    ScheduleRepository scheduleRepository;
    @InjectMocks
    TavernRepository tavernRepository;

    @BeforeEach
    @DisplayName("Presets")
    void beforeEach() {
        BookingHandler bookingHandler = new BookingHandler(
                new ReserveService(reserveRepository),
                new ScheduleService(scheduleRepository),
                new StringService(),
                new TavernService(tavernRepository),
                userService
        );

        userBotService = new UserBotService(
                userService,
                new UserHandlerService(
                        new RegistrationHandler(
                                bookingHandler,
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
    }
}