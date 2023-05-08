package ru.privetdruk.restorder.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import ru.privetdruk.restorder.model.entity.BlacklistEntity;
import ru.privetdruk.restorder.model.entity.TavernEntity;

import java.util.List;

@Repository
public interface BlacklistRepository extends CrudRepository<BlacklistEntity, Long> {
    @Query("""
            SELECT blacklist
             FROM BlacklistEntity blacklist
             WHERE blacklist.tavern = :tavern
                   AND blacklist.active IS TRUE
             ORDER BY blacklist.phoneNumber
            """)
    List<BlacklistEntity> findActiveByTavern(TavernEntity tavern);

    @EntityGraph(attributePaths = "user")
    BlacklistEntity findByTavernAndPhoneNumberAndActive(TavernEntity tavern, String phoneNumber, boolean active);
}
