package ru.privetdruk.restorder.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.privetdruk.restorder.model.entity.ReserveEntity;
import ru.privetdruk.restorder.model.enums.ReserveStatus;
import ru.privetdruk.restorder.repository.ReserveRepository;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * Сервис бронирования
 */
@Service
@RequiredArgsConstructor
public class ReserveService {
    private final ReserveRepository repository;

    /**
     * Обновить статус
     *
     * @param reserves Резервы
     * @param status   Статус
     */
    @Transactional
    public void updateStatus(Set<ReserveEntity> reserves, ReserveStatus status) {
        Set<ReserveEntity> updatedReserves = reserves.stream()
                .peek(reserve -> reserve.setStatus(status))
                .collect(Collectors.toSet());

        repository.saveAll(updatedReserves);
    }

    /**
     * Обновить статус
     *
     * @param reserve Резерв
     * @param status  Статус
     */
    @Transactional
    public void updateStatus(ReserveEntity reserve, ReserveStatus status) {
        reserve.setStatus(status);

        repository.save(reserve);
    }

    /**
     * Сохранить
     *
     * @param reserve Резерв
     */
    @Transactional
    public void save(ReserveEntity reserve) {
        repository.save(reserve);
    }
}
