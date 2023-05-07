package ru.privetdruk.restorder.model.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.privetdruk.restorder.model.enums.ReserveStatus;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Objects;

/**
 * Резервы столов
 */
@Getter
@Setter
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
    private UserEntity user;

    /**
     * Стол
     */
    @ManyToOne(fetch = FetchType.LAZY)
    private TableEntity table;

    /**
     * Кол-во персон
     */
    private Integer numberPeople;

    /**
     * Статус
     */
    @Enumerated(EnumType.STRING)
    private ReserveStatus status = ReserveStatus.ACTIVE;

    /**
     * Дата
     */
    private LocalDate date;

    /**
     * Время
     */
    private LocalTime time;

    /**
     * Забронировано в ручном режиме (оператором)
     */
    private Boolean manualMode = false;

    /**
     * Имя (для ручной брони)
     */
    private String name;

    /**
     * Номер телефона (для ручной брони)
     */
    private String phoneNumber;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ReserveEntity that = (ReserveEntity) o;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
