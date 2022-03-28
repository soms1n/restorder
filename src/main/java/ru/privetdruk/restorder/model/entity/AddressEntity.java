package ru.privetdruk.restorder.model.entity;

import lombok.Builder;
import lombok.Data;
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

    @Builder
    public AddressEntity(TavernEntity tavern, City city) {
        tavern.setAddress(this);
        this.city = city;
    }
}
