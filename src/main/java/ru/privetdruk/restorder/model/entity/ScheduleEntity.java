package ru.privetdruk.restorder.model.entity;

import com.vladmihalcea.hibernate.type.interval.PostgreSQLIntervalType;
import lombok.Data;
import org.hibernate.annotations.TypeDef;
import ru.privetdruk.restorder.model.enums.DayWeek;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.Duration;

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
     * Время работы
     */
    @Column(name = "time_interval")
    private Duration interval;

    /**
     * Цена за вход
     */
    @Column(name = "price")
    private BigDecimal price;
}
