package ru.privetdruk.restorder.model.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.Objects;

/**
 * Стол в заведение
 */
@Getter
@Setter
@Entity
@Table(name = "tables")
public class TableEntity {
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
     * Кол-во мест
     */
    @Column(name = "number_seats")
    private Integer numberSeats;

    /**
     * Признак зарезервированного
     */
    @Column(name = "reserved")
    private Boolean reserved = false;

    /**
     * Метка
     */
    @Column(name = "label")
    private String label;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TableEntity that = (TableEntity) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "TableEntity{" +
                "id=" + id +
                ", label='" + label + '\'' +
                ", numberSeats=" + numberSeats +
                ", reserved=" + reserved +
                ", tavern=" + tavern +
                '}';
    }
}
