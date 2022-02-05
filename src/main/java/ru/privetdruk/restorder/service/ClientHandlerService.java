package ru.privetdruk.restorder.service;

import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import ru.privetdruk.restorder.handler.MessageHandler;
import ru.privetdruk.restorder.handler.client.MainMenuHandler;
import ru.privetdruk.restorder.handler.client.RegistrationHandler;
import ru.privetdruk.restorder.handler.client.SettingsHandler;
import ru.privetdruk.restorder.handler.client.WaitingApplicationApproveHandler;
import ru.privetdruk.restorder.model.enums.State;

import java.util.Map;

@Service
public class ClientHandlerService {
    private final RegistrationHandler registrationHandler;
    private final MainMenuHandler mainMenuHandler;
    private final SettingsHandler settingsHandler;
    private final WaitingApplicationApproveHandler waitingApplicationApproveHandler;

    public ClientHandlerService(@Lazy RegistrationHandler registrationHandler,
                                @Lazy MainMenuHandler mainMenuHandler,
                                @Lazy SettingsHandler settingsHandler,
                                @Lazy WaitingApplicationApproveHandler waitingApplicationApproveHandler) {
        this.registrationHandler = registrationHandler;
        this.mainMenuHandler = mainMenuHandler;
        this.settingsHandler = settingsHandler;
        this.waitingApplicationApproveHandler = waitingApplicationApproveHandler;
    }

    /**
     * Загрузить обработчики
     *
     * @return Обработчики
     */
    public Map<State, MessageHandler> loadHandlers() {
        return Map.of(
                State.REGISTRATION, registrationHandler,
                State.SETTINGS, settingsHandler,
                State.MAIN_MENU, mainMenuHandler
        );
    }
}
