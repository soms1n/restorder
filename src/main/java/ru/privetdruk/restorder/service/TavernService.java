package ru.privetdruk.restorder.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.privetdruk.restorder.model.entity.TavernEntity;
import ru.privetdruk.restorder.model.enums.Category;
import ru.privetdruk.restorder.model.enums.City;
import ru.privetdruk.restorder.repository.TavernRepository;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TavernService {
    private final TavernRepository tavernRepository;

    /**
     * Сохранить заведение
     *
     * @param tavern Заведение
     */
    public TavernEntity save(TavernEntity tavern) {
        return tavernRepository.save(tavern);
    }

    /**
     * Удалить заведение
     *
     * @param tavern Заведение
     */
    public void delete(TavernEntity tavern) {
        tavernRepository.delete(tavern);
    }

    /**
     * Найди заведение (с загрузкой сотрудников и ролей)
     *
     * @param id Идентификатор заведения
     * @return Найденное заведение
     */
    public Optional<TavernEntity> findByIdWithEmployee(Long id) {
        return tavernRepository.findById(id);
    }

    /**
     * Найди заведение (без загрузки связей)
     *
     * @param id Идентификатор заведения
     * @return Найденное заведение
     */
    public TavernEntity findByIdWithoutAllData(Long id) {
        return tavernRepository.findByIdWithoutAllData(id);
    }

    /**
     * Найти
     *
     * @param city     Город
     * @param category Категория
     * @return Список заведений
     */
    public List<TavernEntity> find(City city, Category category) {
        return tavernRepository.findAllByValidAndAddressCityAndCategoryOrderByName(true, city, category);
    }

    @Transactional
    public TavernEntity findWithDataWithoutEmployees(TavernEntity tavern) {
        Long tavernId = tavern.getId();

        TavernEntity foundedTavern = tavernRepository.findByIdWithContacts(tavernId);

        tavernRepository.findByIdWithAddress(tavernId);
        tavernRepository.findByIdWithTables(tavernId);
        tavernRepository.findByIdWithSchedules(tavernId);

        return foundedTavern;
    }

    @Transactional
    public TavernEntity findWithContactsAddressSchedules(TavernEntity tavern) {
        Long tavernId = tavern.getId();

        TavernEntity foundedTavern = tavernRepository.findByIdWithContacts(tavernId);

        tavernRepository.findByIdWithAddress(tavernId);
        tavernRepository.findByIdWithSchedules(tavernId);

        return foundedTavern;
    }

    @Transactional
    public TavernEntity findWithAllData(Long tavernId) {
        TavernEntity foundedTavern = tavernRepository.findByIdWithContacts(tavernId);

        tavernRepository.findByIdWithAddress(tavernId);
        tavernRepository.findByIdWithTables(tavernId);
        tavernRepository.findByIdWithSchedules(tavernId);
        tavernRepository.findByIdWithEmployees(tavernId);

        return foundedTavern;
    }

    @Transactional
    public TavernEntity findWithAllData(TavernEntity tavern) {
        return findWithAllData(tavern.getId());
    }

    public TavernEntity findWithEmployeesTables(TavernEntity tavern) {
        Long tavernId = tavern.getId();

        TavernEntity foundedTavern = tavernRepository.findByIdWithEmployees(tavernId);
        tavernRepository.findByIdWithTables(tavernId);
        return foundedTavern;
    }

    public TavernEntity findWithEmployees(TavernEntity tavern) {
        return tavernRepository.findByIdWithEmployees(tavern.getId());
    }

    public TavernEntity findWithTables(TavernEntity tavern) {
        return tavernRepository.findByIdWithTables(tavern.getId());
    }

    public TavernEntity findWithSchedules(TavernEntity tavern) {
        return tavernRepository.findByIdWithSchedules(tavern.getId());
    }
}
