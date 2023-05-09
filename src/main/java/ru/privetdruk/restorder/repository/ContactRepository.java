package ru.privetdruk.restorder.repository;

import org.springframework.data.repository.CrudRepository;
import ru.privetdruk.restorder.model.entity.ContactEntity;
import ru.privetdruk.restorder.model.entity.TavernEntity;
import ru.privetdruk.restorder.model.entity.UserEntity;

import java.util.List;

public interface ContactRepository extends CrudRepository<ContactEntity, Long> {
    void deleteByTavernAndValue(TavernEntity tavern, String value);

    List<ContactEntity> findByUser(UserEntity user);

    List<ContactEntity> findByTavern(TavernEntity tavern);
}
