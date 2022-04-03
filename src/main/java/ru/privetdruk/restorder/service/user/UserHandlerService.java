package ru.privetdruk.restorder.service.user;

import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import ru.privetdruk.restorder.handler.MessageHandler;
import ru.privetdruk.restorder.handler.user.BookingHandler;
import ru.privetdruk.restorder.handler.user.RegistrationHandler;
import ru.privetdruk.restorder.model.enums.State;

import java.util.Map;

@Service
public class UserHandlerService {
    private final RegistrationHandler registrationHandler;
    private final BookingHandler bookingHandler;

    public UserHandlerService(@Lazy RegistrationHandler registrationHandler,
                              @Lazy BookingHandler bookingHandler) {
        this.registrationHandler = registrationHandler;
        this.bookingHandler = bookingHandler;
    }

    /**
     * Загрузить обработчики
     *
     * @return Обработчики
     */
    public Map<State, MessageHandler> loadHandlers() {
        return Map.of(
                State.REGISTRATION_USER, registrationHandler,
                State.BOOKING, bookingHandler
        );
    }
}
