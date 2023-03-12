package ru.privetdruk.restorder.model.entity;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;
import ru.privetdruk.restorder.model.enums.Category;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Заведение
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "tavern")
@Cacheable
public class TavernEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Сотрудники
     */
    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinTable(
            name = "tavern_to_employee",
            joinColumns = @JoinColumn(name = "tavern_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    @Fetch(FetchMode.SUBSELECT)
    private Set<UserEntity> employees = new HashSet<>();

    /**
     * Название
     */
    @Column
    private String name;

    /**
     * Описание
     */
    @Column
    private String description;

    /**
     * Адрес
     */
    @OneToOne(
            fetch = FetchType.LAZY,
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @JoinColumn
    private AddressEntity address;

    /**
     * График работы
     */
    @OneToMany(mappedBy = "tavern", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<ScheduleEntity> schedules = new HashSet<>();

    /**
     * Контакты
     */
    @OneToMany(mappedBy = "tavern", cascade = CascadeType.ALL, orphanRemoval = true)
    @NotFound(action = NotFoundAction.IGNORE)
    private Set<ContactEntity> contacts = new HashSet<>();

    /**
     * Столы
     */
    @OneToMany(mappedBy = "tavern", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<TableEntity> tables = new HashSet<>();

    /**
     * Категория заведения
     */
    @Column
    @Enumerated(EnumType.STRING)
    private Category category;

    /**
     * Признак валидного заведения
     */
    @Column
    private Boolean valid = false;

    /**
     * Ссылка на схему столов
     */
    @Column
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
        if (this == o) return true;
        if (o == null) return false;
        if (!(o instanceof TavernEntity tavern)) return false;
        return Objects.equals(name, tavern.name) && Objects.equals(address, tavern.address);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, address);
    }

    @Override
    public String toString() {
        return "Tavern{" +
                "id=" + id +
                ", name='" + name + '\'' +
                '}';
    }

    public Boolean isValid() {
        return valid;
    }
}
