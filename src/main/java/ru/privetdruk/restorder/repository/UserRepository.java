package ru.privetdruk.restorder.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import ru.privetdruk.restorder.model.entity.UserEntity;
import ru.privetdruk.restorder.model.enums.Role;
import ru.privetdruk.restorder.model.enums.UserType;

import javax.persistence.LockModeType;
import javax.persistence.QueryHint;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends CrudRepository<UserEntity, Long> {
    @EntityGraph(attributePaths = {"contacts", "reserves.table.tavern", "roles", "tavern.address", "tavern.employees.roles", "tavern.schedules", "tavern.contacts", "tavern.tables.reserves"})
    Optional<UserEntity> findByTelegramIdAndType(Long telegramId, UserType type);

    List<UserEntity> findByRolesIsAndBlockedFalse(Role role);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints({@QueryHint(name = "javax.persistence.lock.timeout", value = "0")})
    UserEntity getByTelegramId(Long telegramId);
}
