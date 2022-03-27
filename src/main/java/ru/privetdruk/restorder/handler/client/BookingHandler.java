package ru.privetdruk.restorder.handler.client;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import ru.privetdruk.restorder.handler.MessageHandler;
import ru.privetdruk.restorder.model.entity.AddressEntity;
import ru.privetdruk.restorder.model.entity.TavernEntity;
import ru.privetdruk.restorder.model.entity.UserEntity;
import ru.privetdruk.restorder.model.enums.Button;
import ru.privetdruk.restorder.model.enums.City;
import ru.privetdruk.restorder.model.enums.SubState;
import ru.privetdruk.restorder.service.KeyboardService;
import ru.privetdruk.restorder.service.MessageService;

import java.util.Arrays;
import java.util.List;

import static java.util.stream.Collectors.toMap;
import static ru.privetdruk.restorder.model.consts.MessageText.GREETING;

@Component
@RequiredArgsConstructor
public class BookingHandler implements MessageHandler {
    private final static int MAX_BUTTONS_PER_ROW = 8;

    private final MessageService messageService;
    private final KeyboardService keyboardService;

    @Override
    public SendMessage handle(UserEntity user, Message message, CallbackQuery callback) {
        String messageText = message.getText();
        SubState subState = user.getSubState();
        SendMessage sendMessage = new SendMessage();
        Long chatId = message.getChatId();

        switch (subState) {
            case GREETING -> {
                sendMessage = messageService.configureMessage(chatId, GREETING);
                sendMessage.setReplyMarkup(
                        InlineKeyboardMarkup.builder()
                                .keyboard(keyboardService.createButtonList(Arrays.stream(City.values())
                                        .collect(toMap(City::getDescription, City::getName)), MAX_BUTTONS_PER_ROW))
                                .build()
                );
            }
            case CHOICE_CITY -> {
                if (callback != null) {
                    String data = callback.getData();
                    City city = City.fromName(data);

                   // sendMessage = messageService.configureMessage(chatId, changeState(user, subState).getMessage());
                } else {
                    sendMessage = messageService.configureMessage(chatId, subState.getMessage());
                    sendMessage.setReplyMarkup(InlineKeyboardMarkup.builder()
                            .keyboard(keyboardService.createButtonList(Arrays.stream(City.values())
                                    .collect(toMap(City::getDescription, City::getName)), MAX_BUTTONS_PER_ROW))
                            .build());
                }
            }
        }

        return sendMessage;
    }
}
