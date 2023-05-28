package ru.privetdruk.restorder.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.privetdruk.restorder.model.consts.Constant;
import ru.privetdruk.restorder.model.consts.MessageText;
import ru.privetdruk.restorder.model.dto.BookingDto;
import ru.privetdruk.restorder.model.entity.*;
import ru.privetdruk.restorder.model.enums.Category;
import ru.privetdruk.restorder.model.enums.ContractType;
import ru.privetdruk.restorder.model.enums.DayWeek;
import ru.privetdruk.restorder.model.enums.Role;
import ru.privetdruk.restorder.service.util.StringService;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static java.lang.System.lineSeparator;
import static java.util.Comparator.comparing;
import static java.util.Comparator.naturalOrder;
import static java.util.Optional.ofNullable;
import static org.springframework.util.CollectionUtils.isEmpty;
import static org.springframework.util.StringUtils.hasText;
import static ru.privetdruk.restorder.model.consts.Constant.*;

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

    private final String LABEL_SEATS_WORD = "<b>%s</b> на <i>%s</i> %s";
    private final String MISSING = "отсутствует";
    private final String TAVERN_INFORMATION = "<b>Информация о вашем заведении</b>";
    private final String APPROVE_INFORMATION = """
            Зарегистрировано новое заведение.
                       
            TelegramId: %s
            Имя пользователя: %s
            Город: %s
            Заведение: %s
            Адрес: %s
                       
            Необходимо проверить адрес на валидность и подтвердить регистрацию.
            """;
    private final String PERSONAL_DATA = """
            <b>Ваши данные</b>
            Имя: <i>%s</i>
            Заведение: <i>%s</i>
            Описание: <i>%s</i>
            Адрес: <i>%s</i>
            Номер телефона: <i>%s</i>
            """;
    private final String RESERVE_INFO_FROM_ENTITY = """
            <b>Информация о бронировании</b>
            Дата: <i>%s</i>
            Время: <i>%s</i>
            Стол: <i>%s</i>
            Кол-во персон: <i>%s</i>
            Имя: <i>%s</i>
            Телефон: <i>%s</i>
            """;
    private final String RESERVE_INFO = """
            <b>Информация о бронировании</b>
            %s
            Дата: <i>%s</i>
            Время: <i>%s</i>
            Кол-во персон: <i>%s</i>""";

    private final String SCHEDULE_INFO = """
            <b>\uD83D\uDCC5 График работы:</b>
            <pre>День Время       Вход
            %s</pre>""";
    private final String BLACKLIST_USER_INFO = """
            Вы добавлены в чёрный список
            Заведение: %s
            Причина: %s
            Дата блокировки: %s
            Дата снятия блокировки: %s
            """;
    private final String NAME_PHONE = "%s %s";
    private final String TAVERN = "Заведение: <i>%s</i>";
    private final String BEFORE_TIME = lineSeparator() + "Примечание: <i>стол нужно будет освободить до %s</i>";
    private final String UNSPECIFIED = "не указан";
    private final String UNSPECIFIED_O = "не указано";
    private final String SCHEDULE_IS_NOT_SET = "График работы не установлен." + lineSeparator();
    private final String START_END_PRICE = "     %s-%s %s";
    private final String FREE = "бесплатно";
    private final String SHORT_RUB = "р.";
    private final String SPACES_3 = "   ";
    private final String AUTO_BLOCK_DISABLED = "Автоматическая блокировка выключена. Для включения настройте параметр \"Кол-во раз\", когда человек не пришел в заведение (0 отключить).";
    private final String FOREVER = "навсегда";
    private final String BLACKLIST_SETTINGS = """
            Настройки автоматической блокировки
            <b>Кол-во раз до блокировки (когда человек не пришел в заведение):</b> %s
            <b>Кол-во дней блокировки:</b> %s
            """;
    private final String BLACKLIST_INFO = """
            Информация о блокировке
            <b>Номер:</b> %s
            <b>Имя:</b> %s
            <b>Причина:</b> %s
            <b>Дата блокировки:</b> %s
            <b>Заблокирован до:</b> %s
            """;
    private final String TAVERN_NAME = "™ <b>Заведение:</b> ";
    private final String DESCRIPTION_IS_EMPTY = "Описание отсутствует.";
    private final String DESCRIPTION = "\uD83D\uDCC3 <b>Описание:</b> ";
    private final String YOUR_NAME = "<b>Ваше имя:</b> ";
    private final String ROLE = "Роль: <b>%s</b>";
    private final String ADDRESS_IS_NOT_SET = "Информация об адресе отсутствует.";
    private final String ADDRESS_INFO = "\uD83D\uDDFA <b>Адрес:</b> г.%s %s";
    private final String CONTACT_IS_NOT_SET = "Контактная информация отсутствует.";
    private final String CONTACT_INFO = "\uD83D\uDCDE <b>Контакты:</b>" + lineSeparator();
    private final String TABLES_IS_NOT_SET = "Столы не добавлены.";
    private final String TABLES_INFO = "<b>Столы:</b>" + lineSeparator();
    private final String CATEGORY = "\uD83C\uDFA8 <b>Категория:</b> ";
    private final String EMPLOYEES_INFO = "<b>Сотрудники:</b>" + lineSeparator();
    private final String NAME_ROLE_ID = "Имя: <b>%s</b>, %s [%s]";
    private final String LINK_IS_NOT_SET = """
            Ссылка на схему расположения столов не добавлена.
            Добавив её, вы позволите вашим клиентам самим выбирать свободный стол для бронирования, иначе стол будет подобран автоматически.""";
    private final String LINK_INFO = "*Схема расположения столов:* [\u200B](%s)";

    public String fillBlacklistSettings(TavernEntity tavern) {
        return blacklistSettingService.findByTavern(tavern)
                .map(setting -> {
                    if (!setting.enabled()) {
                        return AUTO_BLOCK_DISABLED;
                    }

                    return format(BLACKLIST_SETTINGS, setting.getTimes(), setting.getDays() <= 0 ? FOREVER : setting.getDays());
                })
                .orElse(AUTO_BLOCK_DISABLED);
    }

    public String fillBlacklist(BlacklistEntity blacklist) {
        return format(
                BLACKLIST_INFO,
                blacklist.getPhoneNumber(),
                blacklist.getUser() == null ? UNSPECIFIED_O : blacklist.getUser().getName(),
                blacklist.getReason(),
                blacklist.getLockDate().format(Constant.DD_MM_YYYY_FORMATTER),
                blacklist.getUnlockDate().format(Constant.DD_MM_YYYY_FORMATTER)
        );
    }

    public String fillUserBlacklist(BlacklistEntity blacklist) {
        return format(
                BLACKLIST_USER_INFO,
                blacklist.getTavern().getName(),
                blacklist.getReason(),
                blacklist.getLockDate(),
                blacklist.getUnlockDate()
        );
    }

    public String fillProfile(UserEntity user) {
        return fillUser(user.getName()) + lineSeparator() + lineSeparator() +
                fillRole(user.getRoles()) + lineSeparator() + lineSeparator() +
                fillContact(user) + lineSeparator() + lineSeparator();
    }

    public String fillGeneralWithLoadData(TavernEntity tavern) {
        return fillGeneral(tavernService.findWithContactsAddressSchedules(tavern));
    }

    public String fillGeneral(TavernEntity tavern) {
        return fillTavernName(tavern.getName()) + lineSeparator() + lineSeparator() +
                fillTavernDescription(tavern.getDescription()) + lineSeparator() + lineSeparator() +
                fillCategory(tavern.getCategory()) + lineSeparator() + lineSeparator() +
                fillContact(tavern.getContacts()) + lineSeparator() + lineSeparator() +
                fillAddress(tavern.getAddress()) + lineSeparator() + lineSeparator() +
                fillSchedules(tavern.getSchedules());
    }

    public String fillTavernName(String name) {
        return TAVERN_NAME + name;
    }

    public String fillTavernDescription(String description) {
        return !hasText(description) ? DESCRIPTION_IS_EMPTY : DESCRIPTION + description;
    }

    public String fillUser(String name) {
        return YOUR_NAME + name;
    }

    public String fillRole(Collection<Role> roles) {
        String rolesString = roles.stream()
                .map(Role::getDescription)
                .collect(Collectors.joining(lineSeparator()));

        return format(ROLE, rolesString);
    }

    public String fillAddress(AddressEntity address) {
        if (address == null) {
            return ADDRESS_IS_NOT_SET;
        }

        return format(ADDRESS_INFO, address.getCity().getDescription(), address.getStreet());
    }

    public String fillContact(UserEntity user) {
        return fillContact(contactService.findByUser(user));
    }

    public String fillContact(TavernEntity tavern) {
        return fillContact(contactService.findByTavern(tavern));
    }

    private String fillContact(Collection<ContactEntity> contacts) {
        if (isEmpty(contacts)) {
            return CONTACT_IS_NOT_SET;
        }

        return CONTACT_INFO + contacts.stream()
                .map(contact -> contact.getType().getDescription() + SPACE + contact.getValue())
                .collect(Collectors.joining(lineSeparator()));
    }

    public String fillTables(TavernEntity tavern) {
        return fillTables(tavernService.findWithTables(tavern).getTables());
    }

    public String fillTables(Collection<TableEntity> tables) {
        if (isEmpty(tables)) {
            return TABLES_IS_NOT_SET;
        }

        return TABLES_INFO + tables.stream()
                .sorted(comparing(TableEntity::getLabel, naturalOrder()))
                .map(table -> format(
                        LABEL_SEATS_WORD,
                        table.getLabel(),
                        table.getNumberSeats(),
                        stringService.declensionWords(table.getNumberSeats(), StringService.SEATS_WORDS)
                ))
                .collect(Collectors.joining(lineSeparator()));
    }

    public String fillCategory(Category category) {
        return ofNullable(category)
                .map(Category::getDescription)
                .map(description -> CATEGORY + description)
                .orElse(MessageText.CATEGORY_IS_NOT_SET);
    }

    public String fillEmployee(TavernEntity tavern) {
        return fillEmployee(tavernService.findWithEmployees(tavern).getEmployees());
    }

    public String fillEmployee(Collection<UserEntity> employees) {
        return EMPLOYEES_INFO + employees.stream()
                .sorted(comparing(UserEntity::getId))
                .map(employee -> format(NAME_ROLE_ID, employee.getName(), fillRole(employee.getRoles()), employee.getId()))
                .collect(Collectors.joining(lineSeparator()));
    }

    public String fillSchedules(TavernEntity tavern) {
        return fillSchedules(tavernService.findWithSchedules(tavern).getSchedules());
    }

    public String fillReserveInfo(BookingDto booking, boolean fillTavernName) {
        return format(
                RESERVE_INFO,
                fillTavernName ?
                        format(TAVERN, booking.getTavern().getName()) : format(NAME_PHONE, booking.getName(), booking.getPhoneNumber()),
                booking.getDate().format(DD_MM_YYYY_FORMATTER),
                booking.getTime().format(HH_MM_FORMATTER),
                booking.getPersons()
        ) + (booking.getBeforeTime() == null ? EMPTY_STRING : format(BEFORE_TIME, booking.getBeforeTime().format(HH_MM_FORMATTER)));
    }

    public String fillReserveInfo(ReserveEntity reserve) {
        return format(
                RESERVE_INFO_FROM_ENTITY,
                reserve.getDate().format(DD_MM_YYYY_FORMATTER),
                reserve.getTime().format(HH_MM_FORMATTER),
                reserve.getTable().getLabel(),
                reserve.getNumberPeople(),
                reserve.getName(),
                ofNullable(reserve.getPhoneNumber())
                        .orElse(UNSPECIFIED)
        );
    }

    /**
     * Заполнить информацию по графику
     *
     * @param schedules Графики
     * @return Информацию по графику работы
     */
    public String fillSchedules(Collection<ScheduleEntity> schedules) {
        if (isEmpty(schedules)) {
            return SCHEDULE_IS_NOT_SET;
        }

        StringBuilder scheduleDescription = new StringBuilder();

        Map<DayWeek, List<ScheduleEntity>> groupingSchedules = schedules.stream()
                .collect(Collectors.groupingBy(ScheduleEntity::getDayWeek));

        for (DayWeek dayWeek : DayWeek.SORTED_DAY_WEEK_LIST) {
            List<ScheduleEntity> schedulesByDayWeek = groupingSchedules.get(dayWeek);
            if (isEmpty(schedulesByDayWeek)) {
                continue;
            }

            String groupingSchedule = schedulesByDayWeek.stream()
                    .sorted(comparing(ScheduleEntity::getStartPeriod))
                    .map(schedule -> format(
                            START_END_PRICE,
                            schedule.getStartPeriod(),
                            schedule.getEndPeriod(),
                            schedule.getPrice() == 0 ? FREE : schedule.getPrice() + SHORT_RUB
                    ))
                    .collect(Collectors.joining(lineSeparator()));

            scheduleDescription
                    .append(dayWeek.getShortName())
                    .append(SPACES_3)
                    .append(groupingSchedule.substring(5))
                    .append(lineSeparator());
        }

        return format(SCHEDULE_INFO, scheduleDescription);
    }

    public String fillTavernLinkTableLayout(String linkTableLayout) {
        if (!hasText(linkTableLayout)) {
            return LINK_IS_NOT_SET;
        }

        return format(LINK_INFO, linkTableLayout);
    }

    /**
     * Заполнить информацию о заведении
     *
     * @param tavern Заведение
     * @return Информация о заведении
     */
    public String fillTavern(TavernEntity tavern) {
        return TAVERN_INFORMATION + lineSeparator()
                + fillGeneral(tavern) + lineSeparator()
                + fillEmployee(tavern.getEmployees()) + lineSeparator() + lineSeparator()
                + fillTables(tavern.getTables());
    }

    public String fillApprove(UserEntity user) {
        return format(
                APPROVE_INFORMATION,
                user.getTelegramId(),
                user.getName(),
                user.getTavern().getAddress().getCity().getDescription(),
                user.getTavern().getName(),
                user.getTavern().getAddress().getStreet()
        );
    }

    public String fillPersonalData(UserEntity user) {
        TavernEntity tavern = user.getTavern();

        String phoneNumber = contactService.findByUser(user).stream()
                .filter(this::isMobile)
                .map(ContactEntity::getValue)
                .findFirst()
                .orElse(EMPTY_STRING);

        String description = ofNullable(tavern.getDescription())
                .orElse(MISSING);

        return format(
                PERSONAL_DATA,
                user.getName(),
                tavern.getName(),
                description,
                tavern.getAddress().getStreet(),
                phoneNumber
        );
    }

    private boolean isMobile(ContactEntity contactEntity) {
        return contactEntity.getType() == ContractType.MOBILE;
    }
}
