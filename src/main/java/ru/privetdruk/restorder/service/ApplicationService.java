package ru.privetdruk.restorder.service;

import org.springframework.stereotype.Service;
import ru.privetdruk.restorder.repository.ApplicationRepository;

@Service
public class ApplicationService {
    private final ApplicationRepository applicationRepository;

    public ApplicationService(ApplicationRepository applicationRepository) {
        this.applicationRepository = applicationRepository;
    }
}
