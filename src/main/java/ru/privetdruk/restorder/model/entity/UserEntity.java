package ru.privetdruk.restorder.model.entity;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import ru.privetdruk.restorder.model.enums.Role;
import ru.privetdruk.restorder.model.enums.State;
import ru.privetdruk.restorder.model.enums.SubState;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Пользователь системы
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "users")
@Cacheable
public class UserEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Имя
     */
    @Column(name = "first_name")
    private String firstName;

    /**
     * Контакты
     */
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
    //TODO тут надо разобраться со связью. Без каскадирования не работает, с каскадированием создает лишние записи
    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinTable(name = "tavern_to_employee",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "tavern_id"))
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

    @Builder
    public UserEntity(Long telegramId, State state, SubState subState) {
        this.telegramId = telegramId;
        this.state = state;
        this.subState = subState;
    }

    public void addRole(Role role) {
        getRoles().add(role);
    }

    public void addContact(ContactEntity contact) {
        getContacts().add(contact);
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", firstName='" + firstName + '\'' +
                ", telegramId=" + telegramId +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserEntity user = (UserEntity) o;
        return Objects.equals(telegramId, user.telegramId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(telegramId);
    }
}
