package ru.privetdruk.restorder.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import ru.privetdruk.restorder.model.entity.UserEntity;
import ru.privetdruk.restorder.model.enums.Role;

import java.util.List;

@Repository
public interface UserRepository extends CrudRepository<UserEntity, Long> {
    @EntityGraph(attributePaths = {"contacts", "roles", "tavern.address", "tavern.employees", "tavern.schedules", "tavern.contacts", "tavern.tables"})
    UserEntity findByTelegramId(Long telegramId);

    List<UserEntity> getUserEntitiesByRolesIsAndBlockedFalse(Role role);
}
