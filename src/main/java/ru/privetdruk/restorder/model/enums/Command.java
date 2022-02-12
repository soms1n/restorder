package ru.privetdruk.restorder.model.enums;

import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.util.StringUtils;

/**
 * Команды
 */
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum Command {
    START("/start");

    private final String command;

    public static final int MESSAGE_INDEX = 0;
    public static final int MESSAGE_EVENT_ID_INDEX = 1;

    Command(String command) {
        this.command = command;
    }

    public static Command fromCommand(String command) {
        if (!StringUtils.hasText(command)) {
            return null;
        }

        for (Command value : Command.values()) {
            if (command.equalsIgnoreCase(value.getCommand())) {
                return value;
            }
        }

        return null;
    }

    public static ContractType fromName(String name) {
        try {
            return ContractType.valueOf(name);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public String getName() {
        return name();
    }

    public String getCommand() {
        return command;
    }
}
