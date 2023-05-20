package ru.privetdruk.restorder.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.privetdruk.restorder.model.entity.UserEntity;
import ru.privetdruk.restorder.model.enums.Role;
import ru.privetdruk.restorder.model.enums.State;
import ru.privetdruk.restorder.model.enums.SubState;
import ru.privetdruk.restorder.model.enums.UserType;
import ru.privetdruk.restorder.repository.UserRepository;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    /**
     * Поиск пользователя
     *
     * @param telegramId Идентификатор в телеграм
     * @param type       Тип пользователя
     * @return Найденного пользователя
     */
    public Optional<UserEntity> findByTelegramId(Long telegramId, UserType type) {
        return userRepository.findByTelegramIdAndType(telegramId, type);
    }

    public Optional<UserEntity> findByTelegramIdWithLock(Long telegramId) {
        return Optional.ofNullable(userRepository.getByTelegramId(telegramId));
    }

    /**
     * Сохранить пользователя
     *
     * @param user Пользователь
     */
    public UserEntity save(UserEntity user) {
        return userRepository.save(user);
    }

    /**
     * Создать пользователя
     *
     * @param telegramId Идентификатор в телеграм
     * @param role       Роль
     * @return Созданного пользователя
     */
    public UserEntity create(Long telegramId, State state, Role role, UserType type) {
        UserEntity user = createUserWithState(telegramId, state, type);

        user.addRole(role);

        return userRepository.save(user);
    }

    /**
     * Создать пользователя
     *
     * @param telegramId Идентификатор в телеграм
     * @return Созданного пользователя
     */
    public UserEntity create(Long telegramId, State state, UserType type) {
        UserEntity user = createUserWithState(telegramId, state, type);

        return userRepository.save(user);
    }

    private UserEntity createUserWithState(Long telegramId, State state, UserType type) {
        return UserEntity.builder()
                .telegramId(telegramId)
                .type(type)
                .state(state)
                .subState(state.getInitialSubState())
                .build();
    }

    /**
     * Поиск пользователей по ролям
     *
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
    public void delete(UserEntity user) {
        userRepository.delete(user);
    }

    /**
     * Обновит подсостояние пользователя
     *
     * @param user     Пользователь
     * @param subState Подсостояние
     */
    public void updateSubState(UserEntity user, SubState subState) {
        user.setSubState(subState);
        save(user);
    }

    /**
     * Обновит состояние пользователя
     *
     * @param user  Пользователь
     * @param state Состояние
     */
    public void updateState(UserEntity user, State state) {
        user.setState(state);
        user.setSubState(state.getInitialSubState());
        save(user);
    }

    /**
     * Обновит состояние пользователя
     *
     * @param user     Пользователь
     * @param state    Состояние
     * @param subState Подсостояние
     */
    public void update(UserEntity user, State state, SubState subState) {
        user.setState(state);
        user.setSubState(subState);
        save(user);
    }

    public UserEntity findByPhoneNumber(String phoneNumber) {
        return userRepository.findByPhoneNumber(phoneNumber);
    }

    public UserEntity findById(Long userId) {
        return userRepository.findById(userId).orElse(null);
    }
}
