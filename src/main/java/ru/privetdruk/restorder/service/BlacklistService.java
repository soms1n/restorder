package ru.privetdruk.restorder.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.privetdruk.restorder.model.consts.Constant;
import ru.privetdruk.restorder.model.entity.BlacklistEntity;
import ru.privetdruk.restorder.model.entity.TavernEntity;
import ru.privetdruk.restorder.repository.BlacklistRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BlacklistService {
    private final BlacklistRepository blacklistRepository;

    /**
     * Сохранить
     *
     * @param blacklist Блокировка
     */
    public void save(BlacklistEntity blacklist) {
        blacklistRepository.save(blacklist);
    }

    public void unlock(BlacklistEntity blacklist) {
        blacklist.setActive(false);
        blacklist.setUnlockDate(LocalDateTime.now());
        blacklistRepository.save(blacklist);
    }

    public List<BlacklistEntity> findActiveByTavern(TavernEntity tavern) {
        return blacklistRepository.findActiveByTavern(tavern);
    }

    public BlacklistEntity findActiveByPhoneNumber(TavernEntity tavern, String phoneNumber) {
        return blacklistRepository.findByTavernAndPhoneNumberAndActive(tavern, phoneNumber, true);
    }

    public LocalDateTime calculateUnlockDate(int blockDays) {
        return blockDays > 0 ? LocalDateTime.now().plusDays(blockDays) : Constant.MAX_DATE_TIME;
    }
}
