package ru.privetdruk.restorder.repository;

import org.springframework.data.repository.CrudRepository;
import ru.privetdruk.restorder.model.entity.ScheduleEntity;

public interface ScheduleRepository extends CrudRepository<ScheduleEntity, Long> {
}
