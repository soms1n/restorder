package ru.privetdruk.restorder.repository;

import org.springframework.data.repository.CrudRepository;
import ru.privetdruk.restorder.model.entity.AddressEntity;

public interface AddressRepository extends CrudRepository<AddressEntity, Long> {
}
