package ru.privetdruk.restorder.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import ru.privetdruk.restorder.model.entity.EventEntity;

import java.util.UUID;

@Repository
public interface EventRepository extends CrudRepository<EventEntity, Long> {
    EventEntity findByUuid(UUID uuid);
}
