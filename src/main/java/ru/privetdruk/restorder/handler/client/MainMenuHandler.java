package ru.privetdruk.restorder.handler.client;

import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import ru.privetdruk.restorder.handler.MessageHandler;
import ru.privetdruk.restorder.model.entity.TavernEntity;
import ru.privetdruk.restorder.model.entity.UserEntity;
import ru.privetdruk.restorder.model.enums.Button;
import ru.privetdruk.restorder.service.InfoService;
import ru.privetdruk.restorder.service.KeyboardService;

import static ru.privetdruk.restorder.service.MessageService.configureMessage;

@Component
public class MainMenuHandler implements MessageHandler {
    private final InfoService infoService;
    private final SettingsHandler settingsHandler;
    private final ReserveHandler reserveHandler;

    public MainMenuHandler(InfoService infoService,
                           @Lazy SettingsHandler settingsHandler,
                           @Lazy ReserveHandler reserveHandler) {
        this.infoService = infoService;
        this.settingsHandler = settingsHandler;
        this.reserveHandler = reserveHandler;
    }

    @Override
    public SendMessage handle(UserEntity user, Message message, CallbackQuery callback) {
        SendMessage sendMessage = configureMessage(message.getChatId(), "Открываю главное меню.");

        Button button = Button.fromText(message.getText())
                .orElse(Button.NOTHING);

        if (button != Button.NOTHING) {
            switch (button) {
                case SETTINGS -> {
                    return settingsHandler.handle(user, message, callback);
                }
                case INFORMATION -> {
                    TavernEntity tavern = user.getTavern();
                    String description = "<b>Информация о вашем заведении</b>"
                            + System.lineSeparator()
                            + infoService.fillGeneral(tavern)
                            + System.lineSeparator()
                            + infoService.fillEmployee(tavern.getEmployees())
                            + System.lineSeparator() + System.lineSeparator()
                            + infoService.fillTables(tavern.getTables());
                    sendMessage.setText(description);
                    sendMessage.enableHtml(true);
                }
                case RESERVE, RESERVE_LIST -> {
                    return reserveHandler.handle(user, message, callback);
                }
            }
        }

        sendMessage.setReplyMarkup(KeyboardService.CLIENT_MAIN_MENU);

        return sendMessage;
    }
}
