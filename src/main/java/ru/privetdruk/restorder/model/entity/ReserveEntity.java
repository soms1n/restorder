package ru.privetdruk.restorder.model.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import ru.privetdruk.restorder.model.enums.ReserveStatus;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Objects;

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
    @Enumerated(EnumType.STRING)
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

    /**
     * Забронировано в ручном режиме (оператором)
     */
    @Column(name = "manual_mode")
    private Boolean manualMode = false;

    /**
     * Имя (для ручной брони)
     */
    @Column(name = "name")
    private String name;

    /**
     * Номер телефона (для ручной брони)
     */
    @Column(name = "phone_number")
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
