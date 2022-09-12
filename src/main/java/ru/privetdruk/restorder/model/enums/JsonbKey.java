package ru.privetdruk.restorder.model.enums;

import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * Ключи для JSONB полей БД
 */
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum JsonbKey {
    TAVERN_ID("tavernId");

    private final String key;

    JsonbKey(String key) {
        this.key = key;
    }

    public static JsonbKey fromName(String name) {
        try {
            return JsonbKey.valueOf(name);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public String getName() {
        return name();
    }

    public String getKey() {
        return key;
    }
}
