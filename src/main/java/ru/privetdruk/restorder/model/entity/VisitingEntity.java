package ru.privetdruk.restorder.model.entity;

import lombok.*;

import javax.persistence.*;

/**
 * Учёт посещений
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "visiting")
public class VisitingEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Пользователь
     */
    @ManyToOne(fetch = FetchType.LAZY)
    private UserEntity user;

    /**
     * Заведение
     */
    @ManyToOne(fetch = FetchType.LAZY)
    private TavernEntity tavern;

    /**
     * Номер телефона
     */
    private String phoneNumber;

    /**
     * Кол-во раз, когда человек не пришел в заведение
     */
    private Integer times;
}
