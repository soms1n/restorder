package ru.privetdruk.restorder.model.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import ru.privetdruk.restorder.model.enums.ReserveStatus;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Резервы столов
 */
@Data
@Entity
@Table(name = "reserve")
@NoArgsConstructor
public class ReserveEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Пользователь
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private UserEntity user;

    /**
     * Стол
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "table_id")
    private TableEntity table;

    /**
     * Кол-во персон
     */
    @Column(name = "number_people")
    private Integer numberPeople;

    /**
     * Статус
     */
    @Column(name = "status")
    private ReserveStatus status = ReserveStatus.ACTIVE;

    /**
     * Дата
     */
    @Column(name = "date")
    private LocalDate date;

    /**
     * Время
     */
    @Column(name = "time")
    private LocalTime time;
}
