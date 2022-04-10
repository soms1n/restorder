package ru.privetdruk.restorder.handler.user;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import ru.privetdruk.restorder.handler.MessageHandler;
import ru.privetdruk.restorder.model.consts.MessageText;
import ru.privetdruk.restorder.model.entity.TavernEntity;
import ru.privetdruk.restorder.model.entity.UserEntity;
import ru.privetdruk.restorder.model.enums.*;
import ru.privetdruk.restorder.service.KeyboardService;
import ru.privetdruk.restorder.service.MessageService;
import ru.privetdruk.restorder.service.TavernService;
import ru.privetdruk.restorder.service.UserService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static java.util.stream.Collectors.toMap;
import static ru.privetdruk.restorder.model.consts.MessageText.GREETING;

@Component
@RequiredArgsConstructor
public class RegistrationHandler implements MessageHandler {
    private final static int MAX_BUTTONS_PER_ROW = 4;

    private final MessageService messageService;
    private final KeyboardService keyboardService;
    private final UserService userService;
    private final TavernService tavernService;

    @Override
    @Transactional
    public SendMessage handle(UserEntity user, Message message, CallbackQuery callback) {
        String messageText = message.getText();
        SubState subState = user.getSubState();
        SendMessage sendMessage = new SendMessage();
        Long chatId = message.getChatId();

        switch (subState) {
            case GREETING -> {
                if (callback != null) {
                    String data = callback.getData();
                    City city = City.fromName(data);
                    user.setCity(city);

                    sendMessage = messageService.configureMessage(chatId, changeState(user, subState).getMessage());
                    attachMainMenu(sendMessage, user.isRegistered());
                } else {
                    sendMessage = messageService.configureMessage(chatId, GREETING);
                    sendMessage.setReplyMarkup(
                            InlineKeyboardMarkup.builder()
                                    .keyboard(keyboardService.createButtonList(Arrays.stream(City.values())
                                            .collect(toMap(City::getDescription, City::getName)), MAX_BUTTONS_PER_ROW))
                                    .build()
                    );
                }
            }
            case USER_BOT_MAIN_MENU -> {
                if (callback != null) {
                    String data = callback.getData();
                    Button button = Button.fromName(data);
                    sendMessage = messageService.configureMessage(message.getChatId(), "");

                    if (button != null) {
                        switch (button) {
                            case RETURN_MAIN_MENU -> {
                                user.setState(State.BOOKING);
                                user.setSubState(SubState.USER_BOT_MAIN_MENU);
                                attachMainMenu(sendMessage, user.isRegistered());
                            }
                            case MY_RESERVE -> {

                            }
                            default -> {
                            }
                        }
                    } else {
                        Category category = Category.fromName(data);

                        if (category != null) {
                            List<TavernEntity> taverns = tavernService.findAllByAddressCityAndCategory(user.getCity(), category);

                            //TODO тут надо взять локальное время и от него отсчитать до закрытия
                            sendMessage.setReplyMarkup(
                                    InlineKeyboardMarkup.builder()
                                            .keyboard(keyboardService.createButtonList(Arrays.stream(Category.values())
                                                    .collect(toMap(Category::getDescription, Category::getName)), 1))
                                            .build());
                        } else {

                        }
                    }
                } else {
                    sendMessage = messageService.configureMessage(chatId, MessageText.CHOICE_TAVERN_TYPE);
                    attachMainMenu(sendMessage, user.isRegistered());
                }
            }
        }

        return sendMessage;
    }

    private void attachMainMenu(SendMessage sendMessage, boolean isRegistered) {
        List<List<InlineKeyboardButton>> buttonList = new ArrayList<>(
                keyboardService.createButtonList(Arrays.stream(Category.values())
                        .collect(toMap(Category::getDescription, Category::getName)), MAX_BUTTONS_PER_ROW));

        if (isRegistered) {
            buttonList.add(List.of(
                    InlineKeyboardButton.builder()
                            .callbackData(Button.MY_RESERVE.getName())
                            .text(Button.MY_RESERVE.getText())
                            .build()));
        }

        buttonList.add(List.of(
                InlineKeyboardButton.builder()
                        .callbackData(Button.RETURN_MAIN_MENU.getName())
                        .text(Button.RETURN_MAIN_MENU.getText())
                        .build()));

        sendMessage.setReplyMarkup(
                InlineKeyboardMarkup.builder()
                        .keyboard(buttonList)
                        .build());
    }

    private SubState changeState(UserEntity user, SubState subState) {
        SubState nextSubState = subState.getNextSubState();
        user.setState(nextSubState.getState());
        user.setSubState(nextSubState);

        userService.save(user);

        return nextSubState;
    }
}
