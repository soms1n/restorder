package ru.privetdruk.restorder.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import ru.privetdruk.restorder.model.entity.ReserveEntity;

@Repository
public interface ReserveRepository extends CrudRepository<ReserveEntity, Long> {
}
