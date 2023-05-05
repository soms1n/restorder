package ru.privetdruk.restorder.service.user;

import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import ru.privetdruk.restorder.handler.MessageHandler;
import ru.privetdruk.restorder.handler.client.EventHandler;
import ru.privetdruk.restorder.handler.user.BookingHandler;
import ru.privetdruk.restorder.handler.user.RegistrationHandler;
import ru.privetdruk.restorder.model.enums.State;
import ru.privetdruk.restorder.service.BotHandler;

import java.util.Map;

@Service
public class UserHandlerService implements BotHandler {
    private final RegistrationHandler registrationHandler;
    private final BookingHandler bookingHandler;
    private final EventHandler eventHandler;

    public UserHandlerService(@Lazy RegistrationHandler registrationHandler,
                              @Lazy BookingHandler bookingHandler,
                              @Lazy EventHandler eventHandler) {
        this.registrationHandler = registrationHandler;
        this.bookingHandler = bookingHandler;
        this.eventHandler = eventHandler;
    }

    /**
     * Загрузить обработчики
     *
     * @return Обработчики
     */
    @Override
    public Map<State, MessageHandler> loadHandlers() {
        return Map.of(
                State.REGISTRATION_USER, registrationHandler,
                State.BOOKING, bookingHandler,
                State.EVENT, eventHandler
        );
    }
}
