package ru.privetdruk.restorder.model.entity;

import lombok.*;

import javax.persistence.*;

/**
 * Настройка блокировки
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "blacklist_setting")
public class BlacklistSettingEntity {
    @Id
    private Long id;

    /**
     * Заведение
     */
    @MapsId
    @OneToOne(fetch = FetchType.LAZY)
    private TavernEntity tavern;

    /**
     * Кол-во раз, когда человек не пришел в заведение (0 отключить)
     */
    private Integer times;

    /**
     * Кол-во дней блокировки (0 навсегда)
     */
    private Integer days;

    public boolean enabled() {
        return times != null && times > 0;
    }
}
