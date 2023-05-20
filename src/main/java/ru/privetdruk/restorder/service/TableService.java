package ru.privetdruk.restorder.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.privetdruk.restorder.model.entity.TableEntity;
import ru.privetdruk.restorder.repository.TableRepository;

import java.util.Collection;

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
    public TableEntity save(TableEntity table) {
        return repository.save(table);
    }

    /**
     * Сохранить столы
     *
     * @param tables Столы
     */
    public void save(Collection<TableEntity> tables) {
        repository.saveAll(tables);
    }
}
