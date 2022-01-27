package ru.privetdruk.restorder.handler.client;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import ru.privetdruk.restorder.handler.MessageHandler;
import ru.privetdruk.restorder.model.consts.MessageText;
import ru.privetdruk.restorder.model.entity.UserEntity;
import ru.privetdruk.restorder.model.enums.Button;
import ru.privetdruk.restorder.service.KeyboardService;
import ru.privetdruk.restorder.service.MessageService;
import ru.privetdruk.restorder.service.TavernService;
import ru.privetdruk.restorder.service.UserService;

import java.util.List;

@Component
public class MainMenuHandler implements MessageHandler {
    private final MessageService messageService;
    private final TavernService tavernService;
    private final ReplyKeyboardMarkup keyboard;
    private final SettingsHandler settingsHandler;
    private final UserService userService;

    public MainMenuHandler(MessageService messageService, TavernService tavernService, KeyboardService keyboardService, SettingsHandler settingsHandler, UserService userService) {
        this.messageService = messageService;
        this.tavernService = tavernService;
        this.settingsHandler = settingsHandler;
        this.userService = userService;
        this.keyboard = new ReplyKeyboardMarkup();
        this.keyboard.setKeyboard(List.of(new KeyboardRow(List.of(
                new KeyboardButton(Button.SETTINGS.getText()),
                new KeyboardButton(Button.INFORMATION.getText()))
        )));
        this.keyboard.setResizeKeyboard(true);
    }

    @Override
    public SendMessage handle(UserEntity user, Message message, CallbackQuery callback) {
        SendMessage sendMessage = messageService.configureMessage(message.getChatId(), "");

        boolean validTavern = tavernService.isValid(user.getTavern());
        if (!validTavern) {
            sendMessage.setText(MessageText.TAVERN_INVALID);
        }

        Button button = Button.fromText(message.getText());
        if (button != null) {
            switch (button) {
                case SETTINGS: {
                    return settingsHandler.handle(user, message, callback);
                }
                case INFORMATION: {
                    sendMessage.setText("<b>Информация о вашем заведении:</b>");
                }
            }
        }

        sendMessage.setReplyMarkup(keyboard);

        return sendMessage;
    }
}
