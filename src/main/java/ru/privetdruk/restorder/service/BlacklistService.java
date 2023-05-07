package ru.privetdruk.restorder.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.privetdruk.restorder.model.entity.BlacklistEntity;
import ru.privetdruk.restorder.model.entity.TavernEntity;
import ru.privetdruk.restorder.repository.BlacklistRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BlacklistService {
    private final BlacklistRepository blacklistRepository;

    /**
     * Сохранить контакт
     *
     * @param blacklist Контакт
     */
    public void save(BlacklistEntity blacklist) {
        blacklistRepository.save(blacklist);
    }

    public List<BlacklistEntity> findActiveByTavern(TavernEntity tavern) {
        return blacklistRepository.findByTavernOrderById(tavern);
    }
}
