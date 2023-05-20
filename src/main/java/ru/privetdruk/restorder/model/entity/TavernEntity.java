package ru.privetdruk.restorder.model.entity;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;
import ru.privetdruk.restorder.model.enums.Category;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Заведение
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "tavern")
public class TavernEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Сотрудники
     */
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinTable(
            name = "tavern_to_employee",
            joinColumns = @JoinColumn(name = "tavern_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private List<UserEntity> employees = new ArrayList<>();

    /**
     * Название
     */
    private String name;

    /**
     * Описание
     */
    private String description;

    /**
     * Адрес
     */
    @OneToOne(
            fetch = FetchType.LAZY,
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private AddressEntity address;

    /**
     * График работы
     */
    @OneToMany(mappedBy = "tavern", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ScheduleEntity> schedules = new ArrayList<>();

    /**
     * Контакты
     */
    @OneToMany(mappedBy = "tavern", cascade = CascadeType.ALL, orphanRemoval = true)
    @NotFound(action = NotFoundAction.IGNORE)
    private List<ContactEntity> contacts = new ArrayList<>();

    /**
     * Столы
     */
    @OneToMany(mappedBy = "tavern", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TableEntity> tables = new ArrayList<>();

    /**
     * Чёрный список
     */
    @OneToMany(mappedBy = "tavern", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<BlacklistEntity> blacklist = new ArrayList<>();

    /**
     * Настройка чёрного списка
     */
    @OneToOne(mappedBy = "tavern", fetch = FetchType.LAZY, cascade = CascadeType.REMOVE, orphanRemoval = true)
    private BlacklistSettingEntity blacklistSetting;

    /**
     * Учёт посещений
     */
    @OneToMany(mappedBy = "tavern", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<VisitingEntity> visits = new ArrayList<>();

    /**
     * Категория заведения
     */
    @Enumerated(EnumType.STRING)
    private Category category;

    /**
     * Признак валидного заведения
     */
    private Boolean valid = false;

    /**
     * Ссылка на схему столов
     */
    private String linkTableLayout;

    @Builder
    public TavernEntity(UserEntity owner, String name) {
        this.name = name;

        addEmployee(owner);
    }

    public void addEmployee(UserEntity employee) {
        getEmployees().add(employee);
    }

    public void addContact(ContactEntity contact) {
        getContacts().add(contact);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null) {
            return false;
        }

        if (!(o instanceof TavernEntity tavern)) {
            return false;
        }

        return Objects.equals(name, tavern.name); // TODO нужно чекнуть в разрезе города
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    @Override
    public String toString() {
        return "{" +
                "id=" + id +
                ", name='" + name + '\'' +
                '}';
    }

    public Boolean isValid() {
        return valid;
    }
}
