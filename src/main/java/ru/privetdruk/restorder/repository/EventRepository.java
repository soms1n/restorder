package ru.privetdruk.restorder.repository;

import org.springframework.data.repository.CrudRepository;
import ru.privetdruk.restorder.model.entity.EventEntity;

import java.util.UUID;

public interface EventRepository extends CrudRepository<EventEntity, Long> {
    EventEntity findByUuid(UUID uuid);
}
