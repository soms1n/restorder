package ru.privetdruk.restorder.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import ru.privetdruk.restorder.model.entity.ScheduleEntity;

@Repository
public interface ScheduleRepository extends CrudRepository<ScheduleEntity, Long> {
}
