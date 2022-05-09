package ru.privetdruk.restorder.model.dto;

import lombok.Getter;
import lombok.Setter;
import ru.privetdruk.restorder.model.entity.TableEntity;
import ru.privetdruk.restorder.model.entity.TavernEntity;
import ru.privetdruk.restorder.model.enums.Category;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
public class BookingDto {
    private Category category;
    private TavernEntity tavern;
    private LocalDate date;
    private LocalTime time;
    private int persons;
    private TableEntity table;

    public BookingDto(Category category) {
        this.category = category;
    }
}
