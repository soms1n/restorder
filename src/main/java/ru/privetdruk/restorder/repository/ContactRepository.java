package ru.privetdruk.restorder.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import ru.privetdruk.restorder.model.entity.ContactEntity;

@Repository
public interface ContactRepository extends CrudRepository<ContactEntity, Long> {
}
