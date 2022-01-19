package ru.privetdruk.restorder.model.entity;

import lombok.Data;
import ru.privetdruk.restorder.model.enums.ContractType;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * Контакт пользователя
 */
@Data
@Entity
@Table(name = "contact")
public class Contact {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Владелец
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    /**
     * Тип
     */
    @Column(name = "type")
    private ContractType type;

    /**
     * Значение
     */
    @Column(name = "value")
    private String value;

    /**
     * Признак главного
     */
    @Column(name = "main")
    private Boolean main;

    /**
     * Признак действующего
     */
    @Column(name = "active")
    private Boolean active;

    /**
     * Дата и время создания
     */
    @Column(name = "create_date")
    private LocalDateTime createDate = LocalDateTime.now();
}
