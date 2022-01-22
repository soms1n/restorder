package ru.privetdruk.restorder.model.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import ru.privetdruk.restorder.model.enums.Role;

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
public class UserEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Фамилия
     */
    @Column(name = "last_name")
    private String lastName;

    /**
     * Имя
     */
    @Column(name = "first_name")
    private String firstName;

    /**
     * Отчество
     */
    @Column(name = "middle_name")
    private String middleName;

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
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinTable(name = "tavern_to_employee",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "tavern_id"))
    private TavernEntity tavern;

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
