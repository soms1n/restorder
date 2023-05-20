package ru.privetdruk.restorder.model.entity;

import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import ru.privetdruk.restorder.model.enums.ContractType;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * Контакт
 */
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(of = {"value", "type"})
@ToString(of = {"value", "type"})
@Entity
@Table(name = "contact")
public class ContactEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Владелец (пользователь)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    private UserEntity user;

    /**
     * Владелец (заведение)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    private TavernEntity tavern;

    /**
     * Тип
     */
    @Enumerated(EnumType.STRING)
    private ContractType type;

    /**
     * Значение
     */
    private String value;

    /**
     * Признак главного
     */
    private Boolean main = true;

    /**
     * Признак действующего
     */
    private Boolean active = true;

    /**
     * Дата и время создания
     */
    @CreationTimestamp
    private LocalDateTime createDate;

    @Builder
    public ContactEntity(TavernEntity tavern, UserEntity user, ContractType type, String value) {
        this.tavern = tavern;
        this.user = user;
        this.type = type;
        this.value = value;
    }
}
