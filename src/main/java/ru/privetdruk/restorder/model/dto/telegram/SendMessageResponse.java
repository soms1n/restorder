package ru.privetdruk.restorder.model.dto.telegram;

import lombok.Getter;
import lombok.Setter;
import org.telegram.telegrambots.meta.api.objects.Message;

@Getter
@Setter
public class SendMessageResponse {
    private Boolean ok;
    private Message result;

    @Override
    public String toString() {
        return "SendMessageResponse{" +
                "ok=" + ok +
                ", result=" + result +
                '}';
    }
}
