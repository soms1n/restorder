package ru.privetdruk.restorder.repository;

import org.springframework.data.repository.CrudRepository;
import ru.privetdruk.restorder.model.entity.ApplicationEntity;

public interface ApplicationRepository extends CrudRepository<ApplicationEntity, Long> {
}
