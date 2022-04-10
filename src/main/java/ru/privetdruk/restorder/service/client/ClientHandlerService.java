package ru.privetdruk.restorder.service.client;

import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import ru.privetdruk.restorder.handler.MessageHandler;
import ru.privetdruk.restorder.handler.client.*;
import ru.privetdruk.restorder.model.enums.State;

import java.util.Map;

@Service
public class ClientHandlerService {
    private final RegistrationEmployeeHandler registrationEmployeeHandler;
    private final RegistrationTavernHandler registrationTavernHandler;
    private final MainMenuHandler mainMenuHandler;
    private final SettingsHandler settingsHandler;
    private final EventHandler eventHandler;
    private final ReserveHandler reserveHandler;

    public ClientHandlerService(@Lazy RegistrationEmployeeHandler registrationEmployeeHandler,
                                @Lazy RegistrationTavernHandler registrationTavernHandler,
                                @Lazy MainMenuHandler mainMenuHandler,
                                @Lazy SettingsHandler settingsHandler,
                                @Lazy EventHandler eventHandler,
                                @Lazy ReserveHandler reserveHandler) {
        this.registrationEmployeeHandler = registrationEmployeeHandler;
        this.registrationTavernHandler = registrationTavernHandler;
        this.mainMenuHandler = mainMenuHandler;
        this.settingsHandler = settingsHandler;
        this.eventHandler = eventHandler;
        this.reserveHandler = reserveHandler;
    }

    /**
     * Загрузить обработчики
     *
     * @return Обработчики
     */
    public Map<State, MessageHandler> loadHandlers() {
        return Map.of(
                State.REGISTRATION_EMPLOYEE, registrationEmployeeHandler,
                State.REGISTRATION_TAVERN, registrationTavernHandler,
                State.SETTINGS, settingsHandler,
                State.MAIN_MENU, mainMenuHandler,
                State.EVENT, eventHandler,
                State.RESERVE, reserveHandler
        );
    }
}
