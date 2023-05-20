package ru.privetdruk.restorder.model.entity;

import com.vladmihalcea.hibernate.type.json.JsonBinaryType;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import ru.privetdruk.restorder.model.enums.EventType;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Событие для обработки
 */
@Getter
@Setter
@Entity
@Table(name = "event")
@NoArgsConstructor
@TypeDef(name = "jsonb", typeClass = JsonBinaryType.class)
public class EventEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * UUID записи для передачи в сообщении
     */
    private UUID uuid;

    /**
     * Тип
     */
    @Enumerated(EnumType.STRING)
    private EventType type;

    /**
     * Дата и время создания
     */
    @CreationTimestamp
    private LocalDateTime createDate;

    /**
     * Дата и время истечения события
     */
    private LocalDateTime expirationDate;

    /**
     * Доступность события
     */
    private Boolean available = true;

    /**
     * Дополнительные параметры
     */
    @Type(type = "jsonb")
    @Column(name = "params", columnDefinition = "jsonb")
    private Map<String, Object> params = new HashMap<>();

    @Builder
    public EventEntity(EventType type, LocalDateTime expirationDate, Map<String, Object> params) {
        this.type = type;
        this.expirationDate = expirationDate;
        this.params = params;
    }

    @Override
    public String toString() {
        return "EventEntity{" +
                "uuid=" + uuid +
                ", type=" + type +
                ", available=" + available +
                '}';
    }

    @PrePersist
    protected void prePersistUuid() {
        if (this.uuid == null) {
            this.uuid = UUID.randomUUID();
        }
    }
}
