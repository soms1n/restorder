package ru.privetdruk.restorder.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import ru.privetdruk.restorder.model.entity.Tavern;

@Repository
public interface TavernRepository extends CrudRepository<Tavern, Long> {
}
