package org.resume.s3filemanager.entity;

import io.hypersistence.utils.hibernate.type.json.JsonBinaryType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Type;
import org.resume.s3filemanager.enums.OutboxEventType;
import org.resume.s3filemanager.enums.OutboxStatus;

import java.time.Instant;

/**
 * Событие в Outbox таблице.
 * <p>
 * Гарантирует доставку событий в Kafka через паттерн Transactional Outbox.
 * Scheduler периодически читает PENDING события и отправляет их в Kafka.
 */

@Getter
@Setter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "outbox_events")
public class OutboxEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OutboxEventType eventType;

    @Type(JsonBinaryType.class)
    @Column(nullable = false, columnDefinition = "jsonb")
    private String payload;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OutboxStatus status;

    @Column(nullable = false)
    private int retryCount;

    @Column(nullable = false)
    private Instant nextRetryAt;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    private Instant sentAt;

    public OutboxEvent(OutboxEventType eventType, String payload) {
        this.eventType = eventType;
        this.payload = payload;
        this.status = OutboxStatus.PENDING;
        this.retryCount = 0;
        this.nextRetryAt = Instant.now();
        this.createdAt = Instant.now();
    }

}
