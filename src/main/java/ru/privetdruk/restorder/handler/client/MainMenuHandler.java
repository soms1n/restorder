package ru.privetdruk.restorder.handler.client;

import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import ru.privetdruk.restorder.handler.MessageHandler;
import ru.privetdruk.restorder.model.consts.MessageText;
import ru.privetdruk.restorder.model.entity.TavernEntity;
import ru.privetdruk.restorder.model.entity.UserEntity;
import ru.privetdruk.restorder.model.enums.Button;
import ru.privetdruk.restorder.service.InfoService;
import ru.privetdruk.restorder.service.TavernService;

import static ru.privetdruk.restorder.service.KeyboardService.CLIENT_MAIN_MENU_KEYBOARD;
import static ru.privetdruk.restorder.service.MessageService.toMessage;

@Component
public class MainMenuHandler implements MessageHandler {
    private final InfoService infoService;
    private final ReserveHandler reserveHandler;
    private final SettingsHandler settingsHandler;
    private final TavernService tavernService;

    public MainMenuHandler(InfoService infoService,
                           TavernService tavernService,
                           @Lazy SettingsHandler settingsHandler,
                           @Lazy ReserveHandler reserveHandler) {
        this.infoService = infoService;
        this.tavernService = tavernService;
        this.settingsHandler = settingsHandler;
        this.reserveHandler = reserveHandler;
    }

    @Override
    public SendMessage handle(UserEntity user, Message message, CallbackQuery callback) {
        Button button = Button.fromText(message.getText())
                .orElse(Button.NOTHING);

        switch (button) {
            case RESERVE, RESERVE_LIST -> {
                return reserveHandler.handle(user, message, callback);
            }
            case SETTINGS -> {
                return settingsHandler.handle(user, message, callback);
            }
            case INFORMATION -> {
                TavernEntity tavern = tavernService.findWithAllData(user.getTavern());

                return toMessage(message.getChatId(), infoService.fillTavern(tavern), CLIENT_MAIN_MENU_KEYBOARD);
            }
            default -> {
                return toMessage(message.getChatId(), MessageText.OPEN_MENU, CLIENT_MAIN_MENU_KEYBOARD);
            }
        }
    }
}
