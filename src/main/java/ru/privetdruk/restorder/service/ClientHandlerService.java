package ru.privetdruk.restorder.service;

import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import ru.privetdruk.restorder.handler.MessageHandler;
import ru.privetdruk.restorder.handler.client.RegistrationHandler;
import ru.privetdruk.restorder.model.enums.State;

import java.util.Map;

@Service
public class ClientHandlerService {
    private final RegistrationHandler registrationHandler;

    public ClientHandlerService(@Lazy RegistrationHandler registrationHandler) {
        this.registrationHandler = registrationHandler;
    }

    /**
     * Загрузить обработчики
     *
     * @return Обработчики
     */
    public Map<State, MessageHandler> loadHandlers() {
        return Map.of(State.REGISTRATION, registrationHandler);
    }
}
