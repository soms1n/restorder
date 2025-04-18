package ru.privetdruk.restorder.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.privetdruk.restorder.model.entity.BlacklistEntity;
import ru.privetdruk.restorder.model.entity.TavernEntity;
import ru.privetdruk.restorder.model.entity.UserEntity;

@Service
@RequiredArgsConstructor
public class NotificationService {
    private final InfoService infoService;
    private final TelegramApiService telegramApiService;

    private final String UNLOCK = "Вы удалены из чёрного списка в заведении ";

    public void notifyBlockUser(BlacklistEntity blacklist) {
        if (blacklist == null || blacklist.getUser() == null) {
            return;
        }

        telegramApiService.sendMessage(
                blacklist.getUser().getTelegramId(),
                infoService.fillUserBlacklist(blacklist),
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
