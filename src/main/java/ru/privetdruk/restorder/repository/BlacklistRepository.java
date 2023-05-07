package ru.privetdruk.restorder.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import ru.privetdruk.restorder.model.entity.BlacklistEntity;
import ru.privetdruk.restorder.model.entity.TavernEntity;

import java.util.List;

@Repository
public interface BlacklistRepository extends CrudRepository<BlacklistEntity, Long> {
    @EntityGraph(attributePaths = "user")
    List<BlacklistEntity> findByTavernOrderById(TavernEntity tavern);
}
