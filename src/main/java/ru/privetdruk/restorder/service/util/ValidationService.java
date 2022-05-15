package ru.privetdruk.restorder.service.util;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import ru.privetdruk.restorder.model.dto.ValidateTavernResult;
import ru.privetdruk.restorder.model.entity.AddressEntity;
import ru.privetdruk.restorder.model.entity.TavernEntity;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class ValidationService {
    private final Pattern MOBILE_PHONE_PATTERN = Pattern.compile("^((8|\\+7)[\\- ]?)?(\\(?\\d{3}\\)?[\\- ]?)?[\\d\\- ]{7,10}$");

    public boolean isNotValidPhone(String phone) {
        Matcher matcher = MOBILE_PHONE_PATTERN.matcher(phone);

        return !matcher.matches();
    }

    /**
     * Валидация параметров заведения
     *
     * @param tavern Заведение
     * @return true - прошел валидацию
     */
    @Transactional(readOnly = true)
    public ValidateTavernResult validate(TavernEntity tavern) {
        ValidateTavernResult result = new ValidateTavernResult();
        if (tavern == null) {
            result.addMessage("Что-то пошло не так...");

            return result;
        }

        AddressEntity address = tavern.getAddress();
        if (address == null) {
            result.addMessage("- отсутствует адрес");
        } else {
            if (address.getCity() == null) {
                result.addMessage("- не выбран город");
            }

            if (!StringUtils.hasText(address.getStreet())) {
                result.addMessage("- не заполнен адрес");
            }
        }

        if (tavern.getCategory() == null) {
            result.addMessage("- не выбрана категория");
        }

        if (CollectionUtils.isEmpty(tavern.getSchedules())) {
            result.addMessage("- не заполнен график работы");
        }

        if (CollectionUtils.isEmpty(tavern.getContacts())) {
            result.addMessage("- не заполнены контакты");
        }

        if (CollectionUtils.isEmpty(tavern.getTables())) {
            result.addMessage("- не добавлены столы");
        }

        if (!StringUtils.hasText(tavern.getName())) {
            result.addMessage("- не добавлены столы");
        }

        if (CollectionUtils.isEmpty(result.getMessages())) {
            result.setValid(true);
        }

        return result;
    }
}
