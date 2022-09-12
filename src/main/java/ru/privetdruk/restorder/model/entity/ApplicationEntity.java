package ru.privetdruk.restorder.model.entity;

import lombok.Data;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * Заявка на подключение заведения
 */
@Data
@Entity
@Table(name = "application")
public class ApplicationEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Владелец заявки
     */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private UserEntity user;

    /**
     * Заведение
     */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tavern_id")
    private TavernEntity tavern;

    /**
     * Признак одобренной заявки
     */
    @Column(name = "approved")
    private Boolean approved;

    /**
     * Дата и время создания
     */
    @Column(name = "create_date")
    private LocalDateTime createDate = LocalDateTime.now();
}
