package ru.privetdruk.restorder.model.consts;

import java.time.format.DateTimeFormatter;

public interface Constant {
    DateTimeFormatter DD_MM_YYYY_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    DateTimeFormatter DD_MM_YYYY_WITHOUT_DOT_FORMATTER = DateTimeFormatter.ofPattern("ddMMyyyy");
    DateTimeFormatter HH_MM_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
    DateTimeFormatter HH_MM_WITHOUT_DOT_FORMATTER = DateTimeFormatter.ofPattern("HHmm");
}
