package ru.privetdruk.restorder.service.util;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Сервис для работы со строками
 */
@Service
@RequiredArgsConstructor
public class StringService {
    public static final String[] SEATS_WORDS = new String[]{"мест", "места", "место"};

    /**
     * Склонение слов
     *
     * @param value        Склоняемое значение
     * @param declinations Склонения
     * @return Склоненное слово
     */
    public String declensionWords(long value, String[] declinations) {
        if (value == 0) {
            return declinations[0];
        }

        long remainderDivision = Math.abs(value) % 100;
        long additionalRemainder = remainderDivision % 10;

        if (remainderDivision > 10 && remainderDivision < 20) {
            return declinations[0];
        } else if (additionalRemainder > 1 && additionalRemainder < 5) {
            return declinations[1];
        } else if (additionalRemainder == 1) {
            return declinations[2];
        } else {
            return declinations[0];
        }
    }
}
