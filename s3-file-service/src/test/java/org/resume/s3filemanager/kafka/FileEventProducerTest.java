package org.resume.s3filemanager.kafka;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.resume.common.model.FileUploadEvent;
import org.resume.common.properties.KafkaProperties;
import org.resume.s3filemanager.service.kafka.FileEventProducer;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.concurrent.CompletableFuture;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FileEventProducerTest {

    private static final String TOPIC = "file-upload-events";

    @Mock
    private KafkaTemplate<String, FileUploadEvent> kafkaTemplate;

    @Mock
    private KafkaProperties kafkaProperties;

    @InjectMocks
    private FileEventProducer fileEventProducer;

    @Mock
    private KafkaProperties.KafkaTopics topics;

    private FileUploadEvent event;

    @BeforeEach
    void setUp() {
        event = FileUploadEvent.builder()
                .fileId(1L)
                .userId(2L)
                .s3Key("test-key.pdf")
                .bucketName("test-bucket")
                .originalFileName("document.pdf")
                .build();

        when(kafkaProperties.getTopics()).thenReturn(topics);
        when(topics.getFileUploadEvents()).thenReturn(TOPIC);
        when(kafkaTemplate.send(any(), any(), any()))
                .thenReturn(CompletableFuture.completedFuture(null));
    }

    /**
     * Проверяет что событие отправляется в правильный topic с правильным ключом.
     */
    @Test
    void shouldSendEventToCorrectTopicWithFileIdAsKey() {
        fileEventProducer.sendFileUploadEvent(event);

        verify(kafkaTemplate).send(eq(TOPIC), eq("1"), eq(event));
    }
}