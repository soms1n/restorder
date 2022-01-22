package ru.privetdruk.restorder.model.entity;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Заведение
 */
@Getter
@Setter
@Entity
@Table(name = "tavern")
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
            mappedBy = "tavern",
            fetch = FetchType.LAZY,
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            optional = false
    )
    private AddressEntity address;

    /**
     * График работы
     */
    @OneToMany(mappedBy = "tavern", orphanRemoval = true)
    private Set<ScheduleEntity> schedules = new HashSet<>();

    /**
     * Контакты
     */
    @OneToMany(mappedBy = "tavern", orphanRemoval = true)
    private Set<ContactEntity> contacts = new HashSet<>();

    /**
     * Столы
     */
    @OneToMany(mappedBy = "tavern", orphanRemoval = true)
    private Set<TableEntity> tables = new HashSet<>();

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
