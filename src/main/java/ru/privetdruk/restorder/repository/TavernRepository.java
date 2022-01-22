package ru.privetdruk.restorder.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import ru.privetdruk.restorder.model.entity.TavernEntity;

@Repository
public interface TavernRepository extends CrudRepository<TavernEntity, Long> {
}
