package ru.privetdruk.restorder.model.entity;

import lombok.Data;
import ru.privetdruk.restorder.model.enums.City;

import javax.persistence.*;

/**
 * Адрес заведения
 */
@Data
@Entity
@Table(name = "address")
public class AddressEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Заведение
     */
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
}
