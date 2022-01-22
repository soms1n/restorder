package ru.privetdruk.restorder.model.entity;

import lombok.Builder;
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
    private UserEntity owner;

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

    @Builder
    public Tavern(UserEntity owner, String name) {
        this.owner = owner;
        this.name = name;
    }
}
