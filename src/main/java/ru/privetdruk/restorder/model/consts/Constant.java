package ru.privetdruk.restorder.model.consts;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public interface Constant {
    LocalDateTime MAX_DATE_TIME = LocalDateTime.of(9999, 12, 12, 0, 0, 0, 0);
    DateTimeFormatter DD_MM_YYYY_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    DateTimeFormatter DD_MM_YYYY_WITHOUT_DOT_FORMATTER = DateTimeFormatter.ofPattern("ddMMyyyy");
    DateTimeFormatter HH_MM_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
    DateTimeFormatter HH_MM_WITHOUT_DOT_FORMATTER = DateTimeFormatter.ofPattern("HHmm");

    int FIRST_INDEX = 0;
    char SEVEN_CHAR = '7';
    String SEVEN_STRING = "7";
    String EIGHT_STRING = "8";
    String EMPTY_STRING = "";
    String SPACE = " ";
    String PLUS = "+";
    String COMMA = ",";
    String TODAY = " <i>(сегодня)</i>";
    String TOMORROW = " <i>(завтра)</i>";

    String LEFT_SQUARE_BRACKET = "[";
    String LEFT_SQUARE_BRACKET_WITH_SPACE = " [";
    String RIGHT_SQUARE_BRACKET = "]";
}
