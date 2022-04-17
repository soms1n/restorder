package ru.privetdruk.restorder.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import ru.privetdruk.restorder.model.entity.AddressEntity;
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
     * Валидация параметров заведения
     *
     * @param tavern Заведение
     * @return true - прошел валидацию
     */
    @Transactional(readOnly = true)
    public boolean isValid(TavernEntity tavern) {
        if (tavern == null) {
            return false;
        }

        AddressEntity address = tavern.getAddress();
        if (address == null
                || address.getCity() == null
                || !StringUtils.hasText(address.getStreet())) {
            return false;
        }

        if (CollectionUtils.isEmpty(tavern.getSchedules())
                || CollectionUtils.isEmpty(tavern.getContacts())
                || CollectionUtils.isEmpty(tavern.getTables())) {
            return false;
        }

        if (!StringUtils.hasText(tavern.getName())) {
            return false;
        }

        return true;
    }

    /**
     * Сохранить заведение
     *
     * @param tavern Заведение
     */
    @Transactional
    public TavernEntity save(TavernEntity tavern) {
        return tavernRepository.save(tavern);
    }

    /**
     * Удалить заведение
     *
     * @param tavern Заведение
     */
    @Transactional
    public void delete(TavernEntity tavern) {
        tavernRepository.delete(tavern);
    }

    /**
     * Найди заведение
     *
     * @param id Идентификатор заведения
     * @return Найденное заведение
     */
    public Optional<TavernEntity> find(Long id) {
        return tavernRepository.findById(id);
    }

    public List<TavernEntity> findAllByAddressCityAndCategory(City city, Category category) {
        return tavernRepository.findAllByAddress_CityAndCategoryOrderByName(city, category);
    }
}
