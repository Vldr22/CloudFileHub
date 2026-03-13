package org.resume.s3filemanager.kafka;

import com.github.javafaker.Faker;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.common.TopicPartition;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.resume.common.model.FileUploadEvent;
import org.resume.common.model.ScanStatus;
import org.resume.common.properties.KafkaProperties;
import org.resume.s3filemanager.entity.FileMetadata;
import org.resume.s3filemanager.repository.FileMetadataRepository;
import org.resume.s3filemanager.service.kafka.FileEventProducer;
import org.resume.s3filemanager.service.kafka.RetryDLTService;
import org.springframework.kafka.core.ConsumerFactory;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RetryDLTServiceTest {

    private static final Faker FAKER = new Faker();
    private static final String DLT_TOPIC = "file-upload-events.DLT";

    @Mock
    private ConsumerFactory<String, FileUploadEvent> consumerFactory;

    @Mock
    private KafkaProperties kafkaProperties;

    @Mock
    private FileEventProducer fileEventProducer;

    @Mock
    private FileMetadataRepository fileMetadataRepository;

    @Mock
    private Consumer<String, FileUploadEvent> consumer;

    @InjectMocks
    private RetryDLTService retryDLTService;

    private FileUploadEvent event;
    private String s3Key;

    @BeforeEach
    void setUp() {
        s3Key = FAKER.internet().uuid() + ".pdf";

        event = FileUploadEvent.builder()
                .fileId(FAKER.number().randomNumber())
                .s3Key(s3Key)
                .bucketName("test-bucket")
                .originalFileName(FAKER.file().fileName())
                .build();

        KafkaProperties.KafkaTopics topics = new KafkaProperties.KafkaTopics();
        topics.setFileUploadEventsDlt(DLT_TOPIC);

        when(kafkaProperties.getTopics()).thenReturn(topics);
        when(consumerFactory.createConsumer(any(), any())).thenReturn(consumer);
        when(consumer.assignment()).thenReturn(java.util.Set.of());
    }

    private ConsumerRecords<String, FileUploadEvent> recordsOf(FileUploadEvent event) {
        TopicPartition tp = new TopicPartition(DLT_TOPIC, 0);
        ConsumerRecord<String, FileUploadEvent> record =
                new ConsumerRecord<>(DLT_TOPIC, 0, 0L, s3Key, event);
        return new ConsumerRecords<>(Map.of(tp, List.of(record)));
    }

    /**
     * Файл в статусе ERROR — сообщение переотправляется, retried=1.
     */
    @Test
    void shouldRetryEvent_whenFileHasErrorStatus() {
        FileMetadata file = new FileMetadata();
        file.setScanStatus(ScanStatus.ERROR);

        when(fileMetadataRepository.findByUniqueName(s3Key)).thenReturn(Optional.of(file));
        when(consumer.poll(any(Duration.class)))
                .thenReturn(ConsumerRecords.empty())
                .thenReturn(recordsOf(event))
                .thenReturn(ConsumerRecords.empty());

        int retried = retryDLTService.retryFailedEvents();

        assertThat(retried).isEqualTo(1);
        verify(fileEventProducer).sendFileUploadEvent(event);
    }

    /**
     * Файл в статусе CLEAN — сообщение пропускается, retried=0.
     */
    @Test
    void shouldSkipEvent_whenFileIsClean() {
        FileMetadata file = new FileMetadata();
        file.setScanStatus(ScanStatus.CLEAN);

        when(fileMetadataRepository.findByUniqueName(s3Key)).thenReturn(Optional.of(file));
        when(consumer.poll(any(Duration.class)))
                .thenReturn(ConsumerRecords.empty())
                .thenReturn(recordsOf(event))
                .thenReturn(ConsumerRecords.empty());

        int retried = retryDLTService.retryFailedEvents();

        assertThat(retried).isEqualTo(0);
        verifyNoInteractions(fileEventProducer);
    }

    /**
     * Файл не найден в БД — сообщение пропускается.
     */
    @Test
    void shouldSkipEvent_whenFileNotFoundInDatabase() {
        when(fileMetadataRepository.findByUniqueName(s3Key)).thenReturn(Optional.empty());
        when(consumer.poll(any(Duration.class)))
                .thenReturn(ConsumerRecords.empty())
                .thenReturn(recordsOf(event))
                .thenReturn(ConsumerRecords.empty());

        int retried = retryDLTService.retryFailedEvents();

        assertThat(retried).isEqualTo(0);
        verifyNoInteractions(fileEventProducer);
    }

    /**
     * DLT пустой — ничего не переотправляется.
     */
    @Test
    void shouldReturnZero_whenDltIsEmpty() {
        when(consumer.poll(any(Duration.class))).thenReturn(ConsumerRecords.empty());

        int retried = retryDLTService.retryFailedEvents();

        assertThat(retried).isEqualTo(0);
        verifyNoInteractions(fileEventProducer);
    }
}
