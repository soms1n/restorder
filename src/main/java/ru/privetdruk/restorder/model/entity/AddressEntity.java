package ru.privetdruk.restorder.model.entity;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.privetdruk.restorder.model.enums.City;

import javax.persistence.*;

/**
 * Адрес заведения
 */
@Getter
@Setter
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
    @Enumerated(EnumType.STRING)
    private City city;

    /**
     * Улица
     */
    private String street;

    @Builder
    public AddressEntity(TavernEntity tavern, City city) {
        tavern.setAddress(this);
        this.city = city;
    }
}
