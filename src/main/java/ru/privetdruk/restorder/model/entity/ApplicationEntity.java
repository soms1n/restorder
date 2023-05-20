package ru.privetdruk.restorder.model.entity;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * Заявка на подключение заведения
 */
@Getter
@Setter
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
    private UserEntity user;

    /**
     * Заведение
     */
    @OneToOne(fetch = FetchType.LAZY)
    private TavernEntity tavern;

    /**
     * Признак одобренной заявки
     */
    private Boolean approved;

    /**
     * Дата и время создания
     */
    @CreationTimestamp
    private LocalDateTime createDate;
}
