package ru.privetdruk.restorder.model.entity;

import lombok.Data;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

/**
 * Пользователь системы
 */
@Data
@Entity
@Table(name = "users")
public class User {
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
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    @Fetch(FetchMode.SUBSELECT)
    private Set<Contact> contacts = new HashSet<>();

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
}
