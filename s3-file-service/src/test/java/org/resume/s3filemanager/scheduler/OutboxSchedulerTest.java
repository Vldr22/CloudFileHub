package org.resume.s3filemanager.scheduler;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.resume.s3filemanager.service.kafka.OutboxService;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class OutboxSchedulerTest {

    @Mock
    private OutboxService outboxService;

    @InjectMocks
    private OutboxScheduler outboxScheduler;

    /**
     * Scheduler вызывает processPendingEvents при каждом срабатывании.
     */
    @Test
    void shouldCallProcessPendingEvents_whenSchedulerTriggered() {
        outboxScheduler.processOutboxEvents();

        verify(outboxService).processPendingEvents();
    }
}