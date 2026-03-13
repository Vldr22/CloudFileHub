package org.resume.s3filemanager.kafka;

import com.github.javafaker.Faker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.resume.common.model.FileScanResult;
import org.resume.common.model.ScanStatus;
import org.resume.s3filemanager.BaseIntegrationTest;
import org.resume.s3filemanager.entity.FileMetadata;
import org.resume.s3filemanager.entity.User;
import org.resume.s3filemanager.enums.FileUploadStatus;
import org.resume.s3filemanager.enums.UserRole;
import org.resume.s3filemanager.enums.UserStatus;
import org.resume.s3filemanager.repository.FileMetadataRepository;
import org.resume.s3filemanager.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

class FileScanResultConsumerIT extends BaseIntegrationTest {

    private static final Faker FAKER = new Faker();

    @Autowired
    private KafkaTemplate<String, FileScanResult> kafkaTemplate;

    @Autowired
    private FileMetadataRepository fileMetadataRepository;

    @Autowired
    private UserRepository userRepository;

    @Value("${kafka.topics.file-scan-results}")
    private String topic;

    private String uniqueName;
    private FileMetadata fileMetadata;

    @BeforeEach
    void setUp() {
        fileMetadataRepository.deleteAll();
        userRepository.deleteAll();

        uniqueName = FAKER.internet().uuid() + ".pdf";

        User user = new User();
        user.setUsername(FAKER.name().username());
        user.setPassword(FAKER.internet().password());
        user.setRole(UserRole.USER);
        user.setUploadStatus(FileUploadStatus.NOT_UPLOADED);
        user.setStatus(UserStatus.ACTIVE);
        user = userRepository.save(user);

        fileMetadata = fileMetadataRepository.save(FileMetadata.builder()
                .uniqueName(uniqueName)
                .originalName(FAKER.file().fileName())
                .type("application/pdf")
                .size(FAKER.number().randomNumber())
                .fileHash(FAKER.internet().uuid())
                .scanStatus(ScanStatus.PENDING_SCAN)
                .user(user)
                .build());
    }

    /**
     * Проверяет что сообщение CLEAN из Kafka обновляет scanStatus файла в БД.
     */
    @Test
    void shouldUpdateScanStatus_whenCleanResultReceived() {
        FileScanResult result = new FileScanResult();
        result.setS3Key(uniqueName);
        result.setStatus(ScanStatus.CLEAN);

        kafkaTemplate.send(topic, uniqueName, result);

        await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
            FileMetadata updated = fileMetadataRepository.findByUniqueName(uniqueName).orElseThrow();
            assertThat(updated.getScanStatus()).isEqualTo(ScanStatus.CLEAN);
        });
    }

    /**
     * Проверяет что сообщение INFECTED из Kafka обновляет scanStatus файла в БД.
     */
    @Test
    void shouldUpdateScanStatus_whenInfectedResultReceived() {
        FileScanResult result = new FileScanResult();
        result.setS3Key(uniqueName);
        result.setStatus(ScanStatus.INFECTED);
        result.setVirusName(FAKER.lorem().word());

        kafkaTemplate.send(topic, uniqueName, result);

        await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
            FileMetadata updated = fileMetadataRepository.findByUniqueName(uniqueName).orElseThrow();
            assertThat(updated.getScanStatus()).isEqualTo(ScanStatus.INFECTED);
        });
    }
}