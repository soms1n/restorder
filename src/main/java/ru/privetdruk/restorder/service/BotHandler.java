package ru.privetdruk.restorder.service;

import ru.privetdruk.restorder.handler.MessageHandler;
import ru.privetdruk.restorder.model.enums.State;

import java.util.Map;

public interface BotHandler {
    Map<State, MessageHandler> loadHandlers();
}
