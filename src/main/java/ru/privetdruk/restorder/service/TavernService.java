package ru.privetdruk.restorder.service;

import org.springframework.stereotype.Service;
import ru.privetdruk.restorder.repository.TavernRepository;

@Service
public class TavernService {
    private final TavernRepository tavernRepository;

    public TavernService(TavernRepository tavernRepository) {
        this.tavernRepository = tavernRepository;
    }
}
