package ru.privetdruk.restorder.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import ru.privetdruk.restorder.model.entity.ContactEntity;
import ru.privetdruk.restorder.model.entity.TavernEntity;

@Repository
public interface ContactRepository extends CrudRepository<ContactEntity, Long> {
    void deleteByTavernAndValue(TavernEntity tavern, String value);
}
