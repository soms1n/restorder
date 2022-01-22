package ru.privetdruk.restorder.service;

import org.springframework.stereotype.Service;
import ru.privetdruk.restorder.repository.UserRepository;

@Service
public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
}
