package ru.privetdruk.restorder.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.privetdruk.restorder.model.entity.ReserveEntity;
import ru.privetdruk.restorder.model.entity.TableEntity;
import ru.privetdruk.restorder.model.entity.TavernEntity;
import ru.privetdruk.restorder.model.entity.UserEntity;
import ru.privetdruk.restorder.model.enums.ReserveStatus;
import ru.privetdruk.restorder.repository.ReserveRepository;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

import static ru.privetdruk.restorder.model.enums.ReserveStatus.ACTIVE;

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
    public void updateStatus(Collection<ReserveEntity> reserves, ReserveStatus status) {
        List<ReserveEntity> updatedReserves = reserves.stream()
                .peek(reserve -> reserve.setStatus(status))
                .toList();

        repository.saveAll(updatedReserves);
    }

    /**
     * Обновить статус
     *
     * @param reserve Резерв
     * @param status  Статус
     */
    public void updateStatus(ReserveEntity reserve, ReserveStatus status) {
        reserve.setStatus(status);

        repository.save(reserve);
    }

    /**
     * Сохранить
     *
     * @param reserve Резерв
     */
    public void save(ReserveEntity reserve) {
        repository.save(reserve);
    }

    public List<ReserveEntity> findActiveByTavern(TavernEntity tavern, LocalDate date) {
        return repository.findByTavernAndStatus(tavern, date, ACTIVE);
    }

    public List<ReserveEntity> findActiveByTavernWithTable(TavernEntity tavern) {
        return repository.findByTavernAndStatusWithTable(tavern, ACTIVE);
    }

    public List<ReserveEntity> findActiveByTavernWithTableUser(TavernEntity tavern) {
        return repository.findByTavernAndStatusWithTableUser(tavern, ACTIVE);
    }

    public ReserveEntity findActiveByIdWithTableUserTavern(Long reserveId, TavernEntity tavern) {
        return repository.findByIdAndTavernAndStatusWithTableUserTavern(reserveId, tavern, ACTIVE);
    }

    public List<ReserveEntity> findActiveByTavernWithTableUser(TavernEntity tavern, LocalDate date) {
        return repository.findByTavernAndStatusWithTableUser(tavern, date, ACTIVE);
    }

    public List<ReserveEntity> findActiveByTable(TableEntity table, LocalDate date) {
        return repository.findByTableAndDateAndStatus(table, date, ACTIVE);
    }

    public List<ReserveEntity> findActiveByUser(UserEntity user) {
        return repository.findByUserAndStatus(user, ACTIVE);
    }

    public ReserveEntity findById(Long id) {
        return repository.findById(id).orElse(null);
    }
}
