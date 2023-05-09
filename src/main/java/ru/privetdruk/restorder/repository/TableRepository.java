package ru.privetdruk.restorder.repository;

import org.springframework.data.repository.CrudRepository;
import ru.privetdruk.restorder.model.entity.TableEntity;

public interface TableRepository extends CrudRepository<TableEntity, Long> {
}
