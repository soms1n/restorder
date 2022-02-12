package ru.privetdruk.restorder.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.privetdruk.restorder.model.entity.UserEntity;
import ru.privetdruk.restorder.model.enums.Role;
import ru.privetdruk.restorder.model.enums.State;
import ru.privetdruk.restorder.repository.UserRepository;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    /**
     * Поиск пользователя
     * @param telegramId Идентификатор в телеграм
     * @return Найденного пользователя
     */
    @Transactional(readOnly = true)
    public Optional<UserEntity> findByTelegramId(Long telegramId) {
        return userRepository.findByTelegramId(telegramId);
    }

    @Transactional
    public Optional<UserEntity> findByTelegramIdWithLock(Long telegramId) {
        return Optional.ofNullable(userRepository.getByTelegramId(telegramId));
    }

    /**
     * Сохранить пользователя
     * @param user Пользователь
     */
    @Transactional
    public void save(UserEntity user) {
        userRepository.save(user);
    }

    /**
     * Создать пользователя
     *
     * @param telegramId Идентификатор в телеграм
     * @param role Роль
     * @return Созданного пользователя
     */
    @Transactional
    public UserEntity create(Long telegramId, Role role) {
        UserEntity user = UserEntity.builder()
                .telegramId(telegramId)
                .state(State.REGISTRATION_TAVERN)
                .subState(State.REGISTRATION_TAVERN.getInitialSubState())
                .build();

        user.addRole(role);

        return userRepository.save(user);
    }

    /**
     * Создать пользователя
     *
     * @param telegramId Идентификатор в телеграм
     * @return Созданного пользователя
     */
    @Transactional
    public UserEntity create(Long telegramId) {
        UserEntity user = UserEntity.builder()
                .telegramId(telegramId)
                .state(State.REGISTRATION_TAVERN)
                .subState(State.REGISTRATION_TAVERN.getInitialSubState())
                .build();

        return userRepository.save(user);
    }

    /**
     * Поиск пользователей по ролям
     * @param role Роли
     * @return Список найденных пользователей
     */
    public List<UserEntity> findUsersByRole(Role role) {
        return userRepository.findByRolesIsAndBlockedFalse(role);
    }

    /**
     * Удалить пользователя
     *
     * @param user Пользователь
     */
    @Transactional
    public void delete(UserEntity user) {
        userRepository.delete(user);
    }
}
