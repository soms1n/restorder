package ru.privetdruk.restorder.model.entity;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import ru.privetdruk.restorder.model.enums.City;

import javax.persistence.*;

/**
 * Адрес заведения
 */
@Data
@NoArgsConstructor
@Entity
@Table(name = "address")
public class AddressEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Заведение
     */
    @EqualsAndHashCode.Exclude
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tavern_id")
    private TavernEntity tavern;

    /**
     * Город
     */
    @Column(name = "city")
    @Enumerated(EnumType.STRING)
    private City city;

    /**
     * Улица
     */
    @Column(name = "street")
    private String street;

    /**
     * Строение (номер дома)
     */
    @Column(name = "building")
    private String building;

    @Builder
    public AddressEntity(TavernEntity tavern, City city) {
        this.tavern = tavern;
        this.city = city;
    }
}
