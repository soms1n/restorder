package ru.privetdruk.restorder.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.privetdruk.restorder.model.entity.ContactEntity;
import ru.privetdruk.restorder.model.entity.TavernEntity;
import ru.privetdruk.restorder.model.entity.UserEntity;
import ru.privetdruk.restorder.repository.ContactRepository;

import java.util.List;

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

    public List<ContactEntity> findByUser(UserEntity user) {
        return contactRepository.findByUser(user);
    }

    public List<ContactEntity> findByTavern(TavernEntity tavern) {
        return contactRepository.findByTavern(tavern);
    }

    public void delete(ContactEntity contact) {
        contactRepository.delete(contact);
    }

    public String preparePhoneNumber(String phoneNumber) {
        String convertedPhone = phoneNumber.replace("+", "");
        if (convertedPhone.charAt(0) == '7') {
            convertedPhone = convertedPhone.replaceFirst("7", "8");
        }
        return convertedPhone;
    }
}
