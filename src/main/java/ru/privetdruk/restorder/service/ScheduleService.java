package ru.privetdruk.restorder.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import ru.privetdruk.restorder.model.entity.ScheduleEntity;
import ru.privetdruk.restorder.model.enums.DayWeek;
import ru.privetdruk.restorder.repository.ScheduleRepository;

import java.time.LocalTime;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ScheduleService {
    private final ScheduleRepository repository;

    /**
     * Сохранить график
     *
     * @param schedule График
     */
    @Transactional
    public void save(ScheduleEntity schedule) {
        repository.save(schedule);
    }

    /**
     * Сохранить графики
     *
     * @param schedules Графики
     */
    @Transactional
    public void save(Set<ScheduleEntity> schedules) {
        repository.saveAll(schedules);
    }

    /**
     * Проверить доступность времени периода
     *
     * @param schedules   График
     * @param dayWeek     День недели
     * @param startPeriod Начало периода
     * @param endPeriod   Конец периода
     * @return Признак доступности
     */
    public boolean checkTimePeriodAvailability(Set<ScheduleEntity> schedules, DayWeek dayWeek, LocalTime startPeriod, LocalTime endPeriod) {
        Set<ScheduleEntity> foundSchedules = schedules.stream()
                .filter(schedule -> schedule.getDayWeek() == dayWeek)
                .collect(Collectors.toSet());

        if (CollectionUtils.isEmpty(foundSchedules)) {
            return true;
        }

        return foundSchedules.stream()
                .noneMatch(
                        schedule -> (startPeriod.isAfter(schedule.getStartPeriod()) && startPeriod.isBefore(schedule.getEndPeriod()))
                                || (endPeriod.isAfter(schedule.getStartPeriod()) && endPeriod.isBefore(schedule.getEndPeriod()))
                );
    }
}
