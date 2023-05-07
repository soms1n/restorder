package ru.privetdruk.restorder.model.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * Резервы столов
 */
@Getter
@Setter
@Entity
@Table(name = "blacklist")
@NoArgsConstructor
public class BlacklistEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Заведение
     */
    @ManyToOne(fetch = FetchType.LAZY)
    private TavernEntity tavern;

    /**
     * Пользователь
     */
    @ManyToOne(fetch = FetchType.LAZY)
    private UserEntity user;

    /**
     * Номер заблокированного телефона
     */
    private String phoneNumber;

    /**
     * Причина
     */
    private String reason;

    /**
     * Признак активной блокировки
     */
    private String active;

    /**
     * Дата блокировки
     */
    @CreationTimestamp
    private LocalDateTime lockDate;

    /**
     * Дата снятия блокировки
     */
    private LocalDateTime unlockDate;
}
