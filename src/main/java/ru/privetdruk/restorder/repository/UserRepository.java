package ru.privetdruk.restorder.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import ru.privetdruk.restorder.model.entity.User;

@Repository
public interface UserRepository extends CrudRepository<User, Long> {
    User findByTelegramId(Long telegramId);
}
