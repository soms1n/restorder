package ru.privetdruk.restorder.model.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import ru.privetdruk.restorder.model.enums.DayWeek;

import java.time.LocalTime;

@Data
@NoArgsConstructor
public class ScheduleDto {
    private boolean isWeekdays;
    private boolean isWeekends;
    private DayWeek dayWeek;
    private LocalTime startPeriod;
    private LocalTime endPeriod;
    private Integer price;

    public ScheduleDto(DayWeek dayWeek, ScheduleDto schedule) {
        this.dayWeek = dayWeek;
        this.startPeriod = schedule.getStartPeriod();
        this.endPeriod = schedule.getEndPeriod();
        this.price = schedule.getPrice();
    }
}
