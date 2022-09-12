package ru.privetdruk.restorder.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import ru.privetdruk.restorder.model.entity.TableEntity;

@Repository
public interface TableRepository extends CrudRepository<TableEntity, Long> {
}
