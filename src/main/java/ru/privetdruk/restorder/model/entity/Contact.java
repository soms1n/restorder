package ru.privetdruk.restorder.model.entity;

import lombok.Data;
import ru.privetdruk.restorder.model.enums.ContractType;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * Контакт
 */
@Data
@Entity
@Table(name = "contact")
public class Contact {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Владелец (пользователь)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    /**
     * Владелец (заведение)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tavern_id")
    private Tavern tavern;

    /**
     * Тип
     */
    @Column(name = "type")
    @Enumerated(EnumType.STRING)
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
