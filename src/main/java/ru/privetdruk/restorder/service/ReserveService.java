package ru.privetdruk.restorder.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.privetdruk.restorder.repository.ReserveRepository;

/**
 * Сервис бронирования
 */
@Service
@RequiredArgsConstructor
public class ReserveService {
    private final ReserveRepository repository;
}
