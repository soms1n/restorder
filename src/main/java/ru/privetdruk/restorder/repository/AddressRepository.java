package ru.privetdruk.restorder.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import ru.privetdruk.restorder.model.entity.Address;

@Repository
public interface AddressRepository extends CrudRepository<Address, Long> {
}
