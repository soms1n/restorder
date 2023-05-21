package ru.privetdruk.restorder.service.util;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import ru.privetdruk.restorder.model.consts.MessageText;
import ru.privetdruk.restorder.model.dto.ValidateTavernResult;
import ru.privetdruk.restorder.model.entity.AddressEntity;
import ru.privetdruk.restorder.model.entity.TavernEntity;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class ValidationService {
    public static final String ADDRESS_IS_NOT_SET = "- отсутствует адрес";
    public static final String CITI_IS_NOT_SET = "- не выбран город";
    public static final String ADDRESS_IS_NOT_SET_2 = "- не заполнен адрес";
    public static final String CATEGORY_IS_NOT_SET = "- не выбрана категория";
    public static final String SCHEDULE_IS_NOT_SET = "- не заполнен график работы";
    public static final String CONTACT_IS_NOT_SET = "- не заполнены контакты";
    public static final String TABLES_IS_NOT_SET = "- не добавлены столы";
    public static final String TAVERN_NAME_IS_NOT_SET = "- не заполнено название заведения";
    private final Pattern MOBILE_PHONE_PATTERN = Pattern.compile("^8\\d{10}$");
    private final Pattern NAME_PATTERN = Pattern.compile("^(?=.{1,40}$)[а-яёА-ЯЁ]+(?:[-' ][а-яёА-ЯЁ]+)*$");

    public boolean isNotValidPhone(String phone) {
        Matcher matcher = MOBILE_PHONE_PATTERN.matcher(phone);

        return !matcher.matches();
    }

    public boolean isNotValidName(String name) {
        Matcher matcher = NAME_PATTERN.matcher(name);

        return !matcher.matches();
    }


    /**
     * Валидация параметров заведения
     *
     * @param tavern Заведение
     * @return true - прошел валидацию
     */
    public ValidateTavernResult validate(TavernEntity tavern) {
        ValidateTavernResult result = new ValidateTavernResult();
        if (tavern == null) {
            result.addMessage(MessageText.UNEXPECTED_ERROR);

            return result;
        }

        AddressEntity address = tavern.getAddress();
        if (address == null) {
            result.addMessage(ADDRESS_IS_NOT_SET);
        } else {
            if (address.getCity() == null) {
                result.addMessage(CITI_IS_NOT_SET);
            }

            if (!StringUtils.hasText(address.getStreet())) {
                result.addMessage(ADDRESS_IS_NOT_SET_2);
            }
        }

        if (tavern.getCategory() == null) {
            result.addMessage(CATEGORY_IS_NOT_SET);
        }

        if (CollectionUtils.isEmpty(tavern.getSchedules())) {
            result.addMessage(SCHEDULE_IS_NOT_SET);
        }

        if (CollectionUtils.isEmpty(tavern.getContacts())) {
            result.addMessage(CONTACT_IS_NOT_SET);
        }

        if (CollectionUtils.isEmpty(tavern.getTables())) {
            result.addMessage(TABLES_IS_NOT_SET);
        }

        if (!StringUtils.hasText(tavern.getName())) {
            result.addMessage(TAVERN_NAME_IS_NOT_SET);
        }

        if (CollectionUtils.isEmpty(result.getMessages())) {
            result.setValid(true);
        }

        return result;
    }
}
