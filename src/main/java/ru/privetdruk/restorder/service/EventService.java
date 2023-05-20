package ru.privetdruk.restorder.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.privetdruk.restorder.model.entity.EventEntity;
import ru.privetdruk.restorder.repository.EventRepository;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EventService {
    private final EventRepository eventRepository;

    /**
     * Поиск события
     *
     * @param uuid Идентификатор события
     * @return Найденное событие
     */
    public EventEntity find(UUID uuid) {
        return eventRepository.findByUuid(uuid);
    }

    /**
     * Сохранить событие
     *
     * @param event Событие
     */
    public EventEntity save(EventEntity event) {
        return eventRepository.save(event);
    }

    /**
     * Завершить событие
     *
     * @param event Событие
     */
    public EventEntity complete(EventEntity event) {
        event.setAvailable(false);

        return eventRepository.save(event);
    }
}
