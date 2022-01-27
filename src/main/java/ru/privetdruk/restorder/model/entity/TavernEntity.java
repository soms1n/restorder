package ru.privetdruk.restorder.model.entity;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;

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
     * Владелец
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private UserEntity owner;

    /**
     * Сотрудники
     */
    @OneToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "tavern_to_employee",
            joinColumns = @JoinColumn(name = "tavern_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id"))
    @Fetch(FetchMode.SUBSELECT)
    private Set<UserEntity> employees = new HashSet<>();

    /**
     * Название
     */
    @Column(name = "name")
    private String name;

    /**
     * Адрес
     */
    @OneToOne(
            fetch = FetchType.LAZY,
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @JoinColumn(name = "address_id")
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

    @Builder
    public TavernEntity(UserEntity owner, String name) {
        this.owner = owner;
        this.name = name;
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
        if (o == null || getClass() != o.getClass()) return false;
        TavernEntity tavern = (TavernEntity) o;
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
}
