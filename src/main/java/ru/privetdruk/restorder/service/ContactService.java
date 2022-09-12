package ru.privetdruk.restorder.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.privetdruk.restorder.model.entity.ContactEntity;
import ru.privetdruk.restorder.model.entity.TavernEntity;
import ru.privetdruk.restorder.repository.ContactRepository;

@Service
@RequiredArgsConstructor
public class ContactService {
    private final ContactRepository contactRepository;

    /**
     * Сохранить контакт
     *
     * @param contact Контакт
     */
    @Transactional
    public void save(ContactEntity contact) {
        contactRepository.save(contact);
    }

    /**
     * Удалить контакт у заведения по значению
     *
     * @param tavern Заведение
     * @param value  Значение
     */
    @Transactional
    public void delete(TavernEntity tavern, String value) {
        contactRepository.deleteByTavernAndValue(tavern, value);
    }
}
