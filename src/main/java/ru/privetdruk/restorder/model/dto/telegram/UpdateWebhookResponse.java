package ru.privetdruk.restorder.model.dto.telegram;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateWebhookResponse {
    private Boolean ok;
    private Boolean result;
    private String description;

    @Override
    public String toString() {
        return "UpdateWebhookResponse{" +
                "ok=" + ok +
                ", result=" + result +
                ", description='" + description + '\'' +
                '}';
    }
}
