package ru.privetdruk.restorder.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import ru.privetdruk.restorder.model.entity.BlacklistSettingEntity;
import ru.privetdruk.restorder.model.entity.TavernEntity;

import java.util.Optional;

@Repository
public interface BlacklistSettingRepository extends CrudRepository<BlacklistSettingEntity, Long> {
    Optional<BlacklistSettingEntity> findByTavern(TavernEntity tavern);
}
