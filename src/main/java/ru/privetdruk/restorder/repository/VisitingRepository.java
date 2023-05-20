package ru.privetdruk.restorder.repository;

import org.springframework.data.repository.CrudRepository;
import ru.privetdruk.restorder.model.entity.TavernEntity;
import ru.privetdruk.restorder.model.entity.UserEntity;
import ru.privetdruk.restorder.model.entity.VisitingEntity;

import java.util.Optional;

public interface VisitingRepository extends CrudRepository<VisitingEntity, Long> {
    Optional<VisitingEntity> findByUserAndTavern(UserEntity user, TavernEntity tavern);

    Optional<VisitingEntity> findByPhoneNumberAndTavern(String phoneNumber, TavernEntity tavern);
}
