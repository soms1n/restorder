package ru.privetdruk.restorder.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.privetdruk.restorder.model.entity.TableEntity;
import ru.privetdruk.restorder.repository.TableRepository;

import java.util.Set;

/**
 * Сервис столов
 */
@Service
@RequiredArgsConstructor
public class TableService {
    private final TableRepository repository;

    /**
     * Сохранить стол
     *
     * @param table Стол
     * @return Сохраненный стол
     */
    @Transactional
    public TableEntity save(TableEntity table) {
        return repository.save(table);
    }

    /**
     * Сохранить столы
     *
     * @param tables Столы
     */
    @Transactional
    public void save(Set<TableEntity> tables) {
        repository.saveAll(tables);
    }
}
