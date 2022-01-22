package ru.privetdruk.restorder.model.entity;

import lombok.Data;

import javax.persistence.*;

/**
 * Заведение
 */
@Data
@Entity
@Table(name = "tavern")
public class Tavern {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Владелец
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User owner;

    /**
     * Название
     */
    @Column(name = "name")
    private String name;

    /**
     * Адрес
     */
    @OneToOne(
            mappedBy = "tavern",
            fetch = FetchType.LAZY,
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            optional = false
    )
    private Address address;
}
