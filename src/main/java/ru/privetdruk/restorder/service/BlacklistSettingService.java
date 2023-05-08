package ru.privetdruk.restorder.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.privetdruk.restorder.model.entity.BlacklistSettingEntity;
import ru.privetdruk.restorder.model.entity.TavernEntity;
import ru.privetdruk.restorder.repository.BlacklistSettingRepository;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class BlacklistSettingService {
    private final BlacklistSettingRepository blacklistSettingRepository;

    public Optional<BlacklistSettingEntity> findByTavern(TavernEntity tavern) {
        return blacklistSettingRepository.findByTavern(tavern);
    }

    /**
     * Сохранить
     *
     * @param blacklistSetting Настройка
     */
    public void save(BlacklistSettingEntity blacklistSetting) {
        blacklistSettingRepository.save(blacklistSetting);
    }
}
