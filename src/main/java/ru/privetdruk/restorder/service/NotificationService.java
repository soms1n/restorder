package ru.privetdruk.restorder.service;

import lombok.RequiredArgsConstructor;
import org.jvnet.hk2.annotations.Service;
import ru.privetdruk.restorder.model.entity.BlacklistEntity;
import ru.privetdruk.restorder.model.entity.TavernEntity;
import ru.privetdruk.restorder.model.entity.UserEntity;

import static java.lang.String.format;
import static ru.privetdruk.restorder.model.consts.MessageText.NOTIFY_USER_BLOCK;

@Service
@RequiredArgsConstructor
public class NotificationService {
    private final TelegramApiService telegramApiService;

    private final String UNLOCK = "Вы удалены из чёрного списка в заведении ";

    public void notifyBlockUser(BlacklistEntity blacklist) {
        if (blacklist == null || blacklist.getUser() == null) {
            return;
        }

        telegramApiService.sendMessage(
                blacklist.getUser().getTelegramId(),
                format(
                        NOTIFY_USER_BLOCK,
                        blacklist.getTavern().getName(),
                        blacklist.getReason(),
                        blacklist.getLockDate(),
                        blacklist.getUnlockDate()
                ),
                false
        );
    }

    public void notifyUnlockUser(UserEntity user, TavernEntity tavern) {
        if (user == null) {
            return;
        }

        telegramApiService.sendMessage(
                user.getTelegramId(),
                UNLOCK + tavern.getName(),
                false
        );
    }
}
