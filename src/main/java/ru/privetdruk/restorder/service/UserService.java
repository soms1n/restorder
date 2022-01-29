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

    @Transactional(readOnly = true)
    public Optional<UserEntity> findByTelegramId(Long telegramId) {
        return Optional.ofNullable(userRepository.findByTelegramId(telegramId));
    }

    @Transactional
    public void save(UserEntity user) {
        userRepository.save(user);
    }

    /**
     * Создать администратора заведения
     *
     * @param telegramId Идентификатор в телеграм
     * @return Созданного пользователя
     */
    @Transactional
    public UserEntity createClientAdmin(Long telegramId) {
        UserEntity user = UserEntity.builder()
                .telegramId(telegramId)
                .state(State.REGISTRATION)
                .subState(State.REGISTRATION.getInitialSubState())
                .build();

        user.addRole(Role.CLIENT_ADMIN);

        return userRepository.save(user);
    }

    public List<UserEntity> getUsersByRole(Role role) {
        return userRepository.getUserEntitiesByRolesIsAndBlockedFalse(role);
    }
}
