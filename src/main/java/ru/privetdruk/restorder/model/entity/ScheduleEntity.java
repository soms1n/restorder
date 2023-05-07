package ru.privetdruk.restorder.model.entity;

import com.vladmihalcea.hibernate.type.interval.PostgreSQLIntervalType;
import lombok.Getter;
import lombok.Setter;
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
@Getter
@Setter
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
    private TavernEntity tavern;

    /**
     * День недели
     */
    @Enumerated(EnumType.STRING)
    private DayWeek dayWeek;

    /**
     * Начало работы
     */
    private LocalTime startPeriod;

    /**
     * Окончание работы
     */
    private LocalTime endPeriod;

    /**
     * Цена за вход
     */
    private Integer price;
}
