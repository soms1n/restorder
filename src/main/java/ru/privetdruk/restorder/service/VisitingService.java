package ru.privetdruk.restorder.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.privetdruk.restorder.model.entity.TavernEntity;
import ru.privetdruk.restorder.model.entity.UserEntity;
import ru.privetdruk.restorder.model.entity.VisitingEntity;
import ru.privetdruk.restorder.repository.VisitingRepository;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class VisitingService {
    private final VisitingRepository visitingRepository;

    public Optional<VisitingEntity> findByUserAndTavern(UserEntity user, TavernEntity tavern) {
        return visitingRepository.findByUserAndTavern(user, tavern);
    }

    public Optional<VisitingEntity> findByPhoneNumberAndTavern(String phoneNumber, TavernEntity tavern) {
        return visitingRepository.findByPhoneNumberAndTavern(phoneNumber, tavern);
    }

    /**
     * Сохранить
     *
     * @param visiting Посещение
     */
    public void save(VisitingEntity visiting) {
        visitingRepository.save(visiting);
    }
}
