package org.resume.s3filemanager.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.resume.s3filemanager.service.kafka.OutboxService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Scheduler для обработки Outbox событий.
 * <p>
 * Периодически читает PENDING события и отправляет их в Kafka.
 * Интервал настраивается через app.outbox.scheduler-interval-ms.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OutboxScheduler {

    private final OutboxService outboxService;

    @Scheduled(fixedDelayString = "${app.outbox.scheduler-interval-ms}")
    public void processOutboxEvents() {
        log.debug("Outbox scheduler triggered");
        outboxService.processPendingEvents();
    }
}