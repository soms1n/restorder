package ru.privetdruk.restorder.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import ru.privetdruk.restorder.model.consts.Constant;
import ru.privetdruk.restorder.model.entity.*;
import ru.privetdruk.restorder.model.enums.Category;
import ru.privetdruk.restorder.model.enums.DayWeek;
import ru.privetdruk.restorder.model.enums.Role;
import ru.privetdruk.restorder.service.util.StringService;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Информационный сервис
 */
@Service
@RequiredArgsConstructor
public class InfoService {
    private final BlacklistSettingService blacklistSettingService;
    private final ContactService contactService;
    private final StringService stringService;
    private final TavernService tavernService;

    public String fillBlacklistSettings(TavernEntity tavern) {
        return blacklistSettingService.findByTavern(tavern)
                .map(setting -> {
                    if (setting.getTimes() <= 0) {
                        return "Автоматическая блокировка выключена. Для включения, как минимум настройте параметр \"Кол-во раз\", когда человек не пришел в заведение (0 отключить).";
                    }

                    return String.format("""
                                    Настройки автоматической блокировки
                                    <b>Кол-во раз до блокировки (когда человек не пришел в заведение):</b> %s
                                    <b>Кол-во дней блокировки:</b> %s
                                    """,
                            setting.getTimes(),
                            setting.getDays() <= 0 ? "навсегда" : setting.getDays()
                    );
                })
                .orElse("Автоматическая блокировка выключена. Для включения, как минимум настройте параметр \"Кол-во раз\", когда человек не пришел в заведение (0 отключить).");
    }

    public String fillBlacklist(BlacklistEntity blacklist) {
        return String.format(
                """
                        Информация о блокировке
                        <b>Номер:</b> %s
                        <b>Имя:</b> %s
                        <b>Причина:</b> %s
                        <b>Дата блокировки:</b> %s
                        <b>Заблокирован до:</b> %s
                        """,
                blacklist.getPhoneNumber(),
                blacklist.getUser() == null ? "не указано" : blacklist.getUser().getName(),
                blacklist.getReason(),
                blacklist.getLockDate().format(Constant.DD_MM_YYYY_FORMATTER),
                blacklist.getUnlockDate().format(Constant.DD_MM_YYYY_FORMATTER)
        );
    }

    public String fillProfile(UserEntity user) {
        return fillUser(user.getName()) + System.lineSeparator() + System.lineSeparator() +
                fillRoleInfo(user.getRoles()) + System.lineSeparator() + System.lineSeparator() +
                fillContact(user) + System.lineSeparator() + System.lineSeparator();
    }

    public String fillGeneralWithLoadData(TavernEntity tavern) {
        return fillGeneral(tavernService.findWithContactsAddressSchedules(tavern));
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

    public String fillRoleInfo(Collection<Role> roles) {
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

    public String fillContact(UserEntity user) {
        return fillContact(contactService.findByUser(user));
    }

    public String fillContact(TavernEntity tavern) {
        return fillContact(contactService.findByTavern(tavern));
    }

    private String fillContact(Collection<ContactEntity> contacts) {
        if (CollectionUtils.isEmpty(contacts)) {
            return "Контактная информация отсутствует.";
        }

        return "\uD83D\uDCDE <b>Контакты:</b>"
                + System.lineSeparator()
                + contacts.stream()
                .map(contact -> contact.getType().getDescription() + " " + contact.getValue())
                .collect(Collectors.joining(System.lineSeparator()));
    }

    public String fillTables(TavernEntity tavern) {
        return fillTables(tavernService.findWithTables(tavern).getTables());
    }

    public String fillTables(Collection<TableEntity> tables) {
        if (CollectionUtils.isEmpty(tables)) {
            return "Столы не добавлены.";
        }

        return "<b>Столы:</b>" + System.lineSeparator() +
                tables.stream()
                        .sorted(Comparator.comparing(TableEntity::getLabel, Comparator.naturalOrder()))
                        .map(table -> "<b>" + table.getLabel() + "</b> на <i>" + table.getNumberSeats() + "</i> " + stringService.declensionWords(table.getNumberSeats(), StringService.SEATS_WORDS))
                        .collect(Collectors.joining(System.lineSeparator()));
    }

    public String fillCategory(Category category) {
        return Optional.ofNullable(category)
                .map(Category::getDescription)
                .map(description -> "\uD83C\uDFA8 <b>Категория:</b> " + description + "")
                .orElse("Категория не выбрана.");
    }

    public String fillEmployee(TavernEntity tavern) {
        return fillEmployee(tavernService.findWithEmployees(tavern).getEmployees());
    }

    public String fillEmployee(Collection<UserEntity> employees) {
        return "<b>Сотрудники:</b>"
                + System.lineSeparator()
                + employees.stream()
                .sorted(Comparator.comparing(UserEntity::getId))
                .map(employee -> String.format(
                        "Имя: <b>%s</b>, %s [%s]",
                        employee.getName(),
                        fillRoleInfo(employee.getRoles()),
                        employee.getId()
                ))
                .collect(Collectors.joining(System.lineSeparator()));
    }

    public String fillSchedules(TavernEntity tavern) {
        return fillSchedules(tavernService.findWithSchedules(tavern).getSchedules());
    }

    /**
     * Заполнить информацию по графику
     *
     * @param schedules Графики
     * @return Информацию по графику работы
     */
    public String fillSchedules(Collection<ScheduleEntity> schedules) {
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

    public String fillTavernLinkTableLayout(String linkTableLayout) {
        if (!StringUtils.hasText(linkTableLayout)) {
            return "Ссылка на схему расположения столов не добавлена."
                    + System.lineSeparator()
                    + "Добавив её, вы позволите вашим клиентам самим выбирать свободный стол для бронирования, иначе стол будет подобран автоматически.";
        }

        return "*Схема расположения столов:* [\u200B](" + linkTableLayout + ")";
    }
}
