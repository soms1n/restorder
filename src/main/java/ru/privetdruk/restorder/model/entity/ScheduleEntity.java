package ru.privetdruk.restorder.model.entity;

import com.vladmihalcea.hibernate.type.interval.PostgreSQLIntervalType;
import lombok.Data;
import org.hibernate.annotations.TypeDef;
import ru.privetdruk.restorder.model.enums.DayWeek;

import javax.persistence.*;
import java.time.Duration;
import java.time.LocalTime;

/**
 * График работы
 */
@TypeDef(
        typeClass = PostgreSQLIntervalType.class,
        defaultForType = Duration.class
)
@Data
@Entity
@Table(name = "schedule")
public class ScheduleEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Заведение
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tavern_id")
    private TavernEntity tavern;

    /**
     * День недели
     */
    @Column(name = "day_week")
    @Enumerated(EnumType.STRING)
    private DayWeek dayWeek;

    /**
     * Начало работы
     */
    @Column(name = "start_period")
    private LocalTime startPeriod;

    /**
     * Окончание работы
     */
    @Column(name = "end_period")
    private LocalTime endPeriod;

    /**
     * Цена за вход
     */
    @Column(name = "price")
    private Integer price;
}
