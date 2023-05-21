package ru.privetdruk.restorder.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.privetdruk.restorder.model.entity.ContactEntity;
import ru.privetdruk.restorder.model.entity.TavernEntity;
import ru.privetdruk.restorder.model.entity.UserEntity;
import ru.privetdruk.restorder.repository.ContactRepository;

import java.util.List;

import static ru.privetdruk.restorder.model.consts.Constant.*;

/**
 * Сервис контактов
 */
@Service
@RequiredArgsConstructor
public class ContactService {
    private final ContactRepository contactRepository;

    /**
     * Сохранить контакт
     *
     * @param contact Контакт
     */
    public void save(ContactEntity contact) {
        contactRepository.save(contact);
    }

    /**
     * Найти
     *
     * @param user Пользователь
     * @return Контактная информация пользователя
     */
    public List<ContactEntity> findByUser(UserEntity user) {
        return contactRepository.findByUser(user);
    }

    /**
     * Найти
     *
     * @param tavern Заведение
     * @return Контактная информация заведения
     */
    public List<ContactEntity> findByTavern(TavernEntity tavern) {
        return contactRepository.findByTavern(tavern);
    }

    /**
     * Удалить
     *
     * @param contact Контакт
     */
    public void delete(ContactEntity contact) {
        contactRepository.delete(contact);
    }

    /**
     * Подготовить номер телефона для проверок и сохранения
     *
     * @param phoneNumber Номер телефона
     * @return Вместо +7 | 7 return 8
     */
    public String preparePhoneNumber(String phoneNumber) {
        String convertedPhone = phoneNumber.replace(PLUS, EMPTY_STRING);
        if (convertedPhone.charAt(FIRST_INDEX) == SEVEN_CHAR) {
            convertedPhone = convertedPhone.replaceFirst(SEVEN_STRING, EIGHT_STRING);
        }
        return convertedPhone;
    }
}
