package ru.privetdruk.restorder.model.consts;

import java.time.format.DateTimeFormatter;

public interface Constant {
    DateTimeFormatter DD_MM_YYYY_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    DateTimeFormatter HH_MM_FORMATTER = DateTimeFormatter.ofPattern("hh:mm");
}
