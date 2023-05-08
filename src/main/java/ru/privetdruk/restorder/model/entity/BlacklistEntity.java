package ru.privetdruk.restorder.model.entity;

import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * Блокировка
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "blacklist")
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
    @Builder.Default
    private boolean active = true;

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
