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
    public static final String[] TIMES_WORDS = new String[]{"раз", "раза"};

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

    public String declensionTimes(long value) {
        if (value == 1 || value >= 5 && value <= 20 || value % 10 >= 5 || value % 10 == 0 || value % 10 == 1) {
            return TIMES_WORDS[0];
        } else {
            return TIMES_WORDS[1];
        }
    }
}
