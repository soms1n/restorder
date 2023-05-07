package ru.privetdruk.restorder.model.entity;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import ru.privetdruk.restorder.model.enums.*;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * Пользователь системы
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "users")
public class UserEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "first_name")
    private String name;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @Fetch(FetchMode.SUBSELECT)
    private Set<ContactEntity> contacts = new HashSet<>();

    /**
     * Идентификатор в telegram
     */
    @Column(name = "telegram_id")
    private Long telegramId;

    /**
     * Признак блокировки
     */
    @Column(name = "blocked")
    private Boolean blocked = false;

    /**
     * Роли
     */
    @ElementCollection(targetClass = Role.class)
    @CollectionTable(name = "user_to_role", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "role")
    @Enumerated(EnumType.STRING)
    @Fetch(FetchMode.SUBSELECT)
    private Set<Role> roles = new HashSet<>();

    /**
     * Заведение
     */
    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinTable(
            name = "tavern_to_employee",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "tavern_id")
    )
    private TavernEntity tavern;

    /**
     * Состояние
     */
    @Column(name = "state")
    @Enumerated(EnumType.STRING)
    private State state;

    /**
     * Подсостояние
     */
    @Column(name = "sub_state")
    @Enumerated(EnumType.STRING)
    private SubState subState;

    @Column(name = "city")
    @Enumerated(EnumType.STRING)
    private City city;

    /**
     * Контакты
     */
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    @Fetch(FetchMode.SUBSELECT)
    private Set<ReserveEntity> reserves = new HashSet<>();

    /**
     * Признак зарегистрированного пользователя
     */
    @Column(name = "registered")
    private boolean registered;

    /**
     * Тип
     */
    @Column(name = "type")
    @Enumerated(EnumType.STRING)
    private UserType type;

    @Builder
    public UserEntity(Long telegramId, State state, SubState subState, UserType type) {
        this.telegramId = telegramId;
        this.state = state;
        this.subState = subState;
        this.type = type;
    }

    public void addRole(Role role) {
        getRoles().add(role);
    }

    public void addContact(ContactEntity contact) {
        getContacts().add(contact);
    }

    public Optional<ContactEntity> findContact(ContractType contractType) {
        return contacts.stream()
                .filter(contact -> contact.getType() == contractType)
                .findFirst();
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", telegramId=" + telegramId +
                ", state=" + state +
                ", subState=" + subState +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserEntity that = (UserEntity) o;
        return Objects.equals(telegramId, that.telegramId) && type == that.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(telegramId, type);
    }
}
