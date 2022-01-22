package ru.privetdruk.restorder.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import ru.privetdruk.restorder.model.entity.AddressEntity;
import ru.privetdruk.restorder.model.entity.TavernEntity;
import ru.privetdruk.restorder.repository.TavernRepository;

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
                || !StringUtils.hasText(address.getStreet())
                || !StringUtils.hasText(address.getBuilding())) {
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
}
