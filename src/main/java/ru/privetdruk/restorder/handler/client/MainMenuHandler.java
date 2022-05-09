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
import ru.privetdruk.restorder.service.KeyboardService;
import ru.privetdruk.restorder.service.MessageService;
import ru.privetdruk.restorder.service.ScheduleService;
import ru.privetdruk.restorder.service.TavernService;

import static ru.privetdruk.restorder.service.MessageService.configureMessage;

@Component
public class MainMenuHandler implements MessageHandler {
    private final MessageService messageService;
    private final TavernService tavernService;
    private final SettingsHandler settingsHandler;
    private final ScheduleService scheduleService;
    private final ReserveHandler reserveHandler;

    public MainMenuHandler(MessageService messageService,
                           TavernService tavernService,
                           @Lazy SettingsHandler settingsHandler,
                           @Lazy ReserveHandler reserveHandler,
                           @Lazy ScheduleService scheduleService) {
        this.messageService = messageService;
        this.tavernService = tavernService;
        this.settingsHandler = settingsHandler;
        this.reserveHandler = reserveHandler;
        this.scheduleService = scheduleService;
    }

    @Override
    public SendMessage handle(UserEntity user, Message message, CallbackQuery callback) {
        SendMessage sendMessage = configureMessage(message.getChatId(), "Открываю главное меню.");

        boolean validTavern = tavernService.isValid(user.getTavern());
        if (!validTavern) {
            sendMessage.setText(MessageText.TAVERN_INVALID);
        }

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
                            + settingsHandler.fillProfileInfo(user)
                            + settingsHandler.fillTavernInfo(tavern.getName())
                            + System.lineSeparator()
                            + System.lineSeparator()
                            + settingsHandler.fillAddressInfo(tavern.getAddress())
                            + System.lineSeparator()
                            + System.lineSeparator()
                            + settingsHandler.fillCategory(tavern.getCategory())
                            + System.lineSeparator()
                            + System.lineSeparator()
                            + settingsHandler.fillContactInfo(tavern.getContacts())
                            + System.lineSeparator()
                            + System.lineSeparator()
                            + settingsHandler.fillEmployeeInfo(tavern.getEmployees())
                            + System.lineSeparator()
                            + System.lineSeparator()
                            + settingsHandler.fillTables(tavern.getTables())
                            + System.lineSeparator()
                            + System.lineSeparator()
                            + scheduleService.fillSchedulesInfo(tavern.getSchedules());
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
