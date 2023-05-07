package ru.privetdruk.restorder.model.entity;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.privetdruk.restorder.model.enums.*;

import javax.persistence.*;
import java.util.*;

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
    private List<ContactEntity> contacts = new ArrayList<>();

    /**
     * Идентификатор в telegram
     */
    private Long telegramId;

    /**
     * Признак блокировки
     */
    private Boolean blocked = false;

    /**
     * Роли
     */
    @ElementCollection(targetClass = Role.class)
    @CollectionTable(name = "user_to_role", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "role")
    @Enumerated(EnumType.STRING)
    private Set<Role> roles = new HashSet<>();

    /**
     * Заведение
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinTable(
            name = "tavern_to_employee",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "tavern_id")
    )
    private TavernEntity tavern;

    /**
     * Состояние
     */
    @Enumerated(EnumType.STRING)
    private State state;

    /**
     * Подсостояние
     */
    @Enumerated(EnumType.STRING)
    private SubState subState;

    @Enumerated(EnumType.STRING)
    private City city;

    /**
     * Контакты
     */
    @OneToMany(mappedBy = "user")
    private List<ReserveEntity> reserves = new ArrayList<>();

    /**
     * Признак зарегистрированного пользователя
     */
    private boolean registered;

    /**
     * Тип
     */
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
        return "{" +
                "id=" + id +
                ", telegramId=" + telegramId +
                ", state=" + state +
                ", subState=" + subState +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        UserEntity that = (UserEntity) o;

        return Objects.equals(telegramId, that.telegramId) && type == that.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(telegramId, type);
    }
}
