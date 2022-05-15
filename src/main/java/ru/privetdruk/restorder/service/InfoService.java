package ru.privetdruk.restorder.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import ru.privetdruk.restorder.model.entity.*;
import ru.privetdruk.restorder.model.enums.Category;
import ru.privetdruk.restorder.model.enums.DayWeek;
import ru.privetdruk.restorder.model.enums.Role;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Информационный сервис
 */
@Service
@RequiredArgsConstructor
public class InfoService {
    public String fillProfile(UserEntity user) {
        return fillUser(user.getName()) + System.lineSeparator() + System.lineSeparator() +
                fillRoleInfo(user.getRoles()) + System.lineSeparator() + System.lineSeparator() +
                fillContact(user.getContacts()) + System.lineSeparator() + System.lineSeparator();
    }

    public String fillGeneral(TavernEntity tavern) {
        return fillTavernName(tavern.getName()) + System.lineSeparator() + System.lineSeparator() +
                fillTavernDescription(tavern.getDescription()) + System.lineSeparator() + System.lineSeparator() +
                fillCategory(tavern.getCategory()) + System.lineSeparator() + System.lineSeparator() +
                fillContact(tavern.getContacts()) + System.lineSeparator() + System.lineSeparator() +
                fillAddress(tavern.getAddress()) + System.lineSeparator() + System.lineSeparator() +
                fillSchedules(tavern.getSchedules());
    }

    public String fillTavernName(String name) {
        return "™ <b>Заведение:</b> " + name;
    }

    public String fillTavernDescription(String description) {
        return !StringUtils.hasText(description) ? "Описание отсутствует." : "\uD83D\uDCC3 <b>Описание:</b> " + description;

    }

    public String fillUser(String name) {
        return "<b>Ваше имя:</b> " + name + "";
    }

    public String fillRoleInfo(Set<Role> roles) {
        String rolesString = roles.stream()
                .map(Role::getDescription)
                .collect(Collectors.joining(System.lineSeparator()));

        return "Роль: <b>" + rolesString + "</b>";
    }

    public String fillAddress(AddressEntity address) {
        if (address == null) {
            return "Информация об адресе отсутствует.";
        }

        return String.format(
                "\uD83D\uDDFA <b>Адрес:</b> г.%s %s",
                address.getCity().getDescription(),
                address.getStreet()
        );
    }

    public String fillContact(Set<ContactEntity> contacts) {
        if (CollectionUtils.isEmpty(contacts)) {
            return "Контактная информация отсутствует.";
        }

        return "\uD83D\uDCDE <b>Контакты:</b>"
                + System.lineSeparator()
                + contacts.stream()
                .map(contact -> contact.getType().getDescription() + " " + contact.getValue())
                .collect(Collectors.joining(System.lineSeparator()));
    }

    public String fillTables(Set<TableEntity> tables) {
        if (CollectionUtils.isEmpty(tables)) {
            return "Столы не добавлены.";
        }

        return "<b>Столы:</b>" + System.lineSeparator() +
                tables.stream()
                        .sorted(Comparator.comparing(TableEntity::getLabel, Comparator.naturalOrder()))
                        .map(table -> "<b>" + table.getLabel() + "</b> на <i>" + table.getNumberSeats() + "</i> мест")
                        .collect(Collectors.joining(System.lineSeparator()));
    }

    public String fillCategory(Category category) {
        return Optional.ofNullable(category)
                .map(Category::getDescription)
                .map(description -> "\uD83C\uDFA8 <b>Категория:</b> " + description + "")
                .orElse("Категория не выбрана.");
    }

    public String fillEmployee(Set<UserEntity> employees) {
        return "<b>Сотрудники:</b>"
                + System.lineSeparator()
                + employees.stream()
                .sorted(Comparator.comparing(UserEntity::getId))
                .map(employee -> String.format(
                        "ID: %d, Имя: <b>%s</b>, %s",
                        employee.getId(),
                        employee.getName(),
                        fillRoleInfo(employee.getRoles())
                ))
                .collect(Collectors.joining(System.lineSeparator()));
    }

    /**
     * Заполнить информацию по графику
     *
     * @param schedules Графики
     * @return Информацию по графику работы
     */
    public String fillSchedules(Set<ScheduleEntity> schedules) {
        if (CollectionUtils.isEmpty(schedules)) {
            return "График работы не установлен." + System.lineSeparator();
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
                            "     %s-%s %s",
                            schedule.getStartPeriod(),
                            schedule.getEndPeriod(),
                            schedule.getPrice() == 0 ? "бесплатно" : schedule.getPrice() + "р."
                    ))
                    .collect(Collectors.joining(System.lineSeparator()));

            scheduleDescription
                    .append(dayWeek.getShortName())
                    .append("   ")
                    .append(groupingSchedule.substring(5))
                    .append(System.lineSeparator());
        }

        return "<b>\uD83D\uDCC5 График работы:</b>"
                + System.lineSeparator()
                + "<pre>День Время       Вход"
                + System.lineSeparator()
                + scheduleDescription + "</pre>";
    }
}
