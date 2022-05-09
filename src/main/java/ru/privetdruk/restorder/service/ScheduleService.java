package ru.privetdruk.restorder.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import ru.privetdruk.restorder.model.entity.ScheduleEntity;
import ru.privetdruk.restorder.model.enums.DayWeek;
import ru.privetdruk.restorder.repository.ScheduleRepository;

import java.time.LocalTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
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

    /**
     * Заполнить информацию по графику
     *
     * @param schedules Графики
     * @return Информацию по графику работы
     */
    public String fillSchedulesInfo(Set<ScheduleEntity> schedules) {
        if (CollectionUtils.isEmpty(schedules)) {
            return "График работы не установлен.";
        }

        StringBuilder scheduleDescription = new StringBuilder();

        Map<DayWeek, List<ScheduleEntity>> groupingSchedules = schedules.stream()
                .collect(Collectors.groupingBy(ScheduleEntity::getDayWeek));

        for (DayWeek dayWeek : DayWeek.SORTED_DAY_WEEK_LIST) {
            List<ScheduleEntity> schedulesByDayWeek = groupingSchedules.get(dayWeek);
            if (CollectionUtils.isEmpty(schedulesByDayWeek)) {
                continue;
            }

            String groupingSchedule = schedulesByDayWeek.stream()
                    .sorted(Comparator.comparing(ScheduleEntity::getStartPeriod))
                    .map(schedule -> String.format(
                            "    %s - %s %s",
                            schedule.getStartPeriod(),
                            schedule.getEndPeriod(),
                            schedule.getPrice() == 0 ? "бесплатно" : schedule.getPrice() + "р."
                    ))
                    .collect(Collectors.joining(System.lineSeparator()));

            scheduleDescription
                    .append(dayWeek.getShortName())
                    .append(": ")
                    .append(groupingSchedule.substring(4))
                    .append(System.lineSeparator());
        }

        return "<b>\uD83D\uDCC5 График работы:</b>" +
                System.lineSeparator() +
                "<pre>" + scheduleDescription + "</pre>";
    }
}
