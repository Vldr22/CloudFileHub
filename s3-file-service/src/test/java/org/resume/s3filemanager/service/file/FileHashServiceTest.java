package org.resume.s3filemanager.service.file;

import com.github.javafaker.Faker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.resume.s3filemanager.exception.DuplicateFileException;
import org.resume.s3filemanager.repository.FileMetadataRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("FileHashService — вычисление хеша и обнаружение дубликатов")
class FileHashServiceTest {

    private static final Faker FAKER = new Faker();
    private static final String HELLO_CONTENT = "hello";
    private static final String HELLO_MD5 = "5d41402abc4b2a76b9719d911017c592";

    @Mock
    private FileMetadataRepository fileMetadataRepository;

    @InjectMocks
    private FileHashService fileHashService;

    private Long userId;

    @BeforeEach
    void setUp() {
        userId = FAKER.number().randomNumber();
    }

    // calculateMD5

    /**
     * Проверяет корректность MD5 хеша для известного содержимого.
     * MD5 от строки "hello" должен возвращать фиксированное значение
     */
    @Test
    void shouldCalculateMD5_forKnownContent() {
        byte[] content = HELLO_CONTENT.getBytes();

        String hash = fileHashService.calculateMD5(content);

        assertThat(hash).isEqualTo(HELLO_MD5);
    }

    /**
     * Проверяет что одинаковое содержимое всегда даёт одинаковый хеш
     */
    @Test
    void shouldReturnSameHash_forSameContent() {
        byte[] content = FAKER.lorem().sentence().getBytes();

        String hash1 = fileHashService.calculateMD5(content);
        String hash2 = fileHashService.calculateMD5(content);

        assertThat(hash1).isEqualTo(hash2);
    }

    /**
     * Проверяет что разное содержимое даёт разные хеши.
     */
    @Test
    void shouldReturnDifferentHashes_forDifferentContent() {
        byte[] content1 = FAKER.lorem().sentence().getBytes();
        byte[] content2 = FAKER.lorem().sentence().getBytes();

        String hash1 = fileHashService.calculateMD5(content1);
        String hash2 = fileHashService.calculateMD5(content2);

        assertThat(hash1).isNotEqualTo(hash2);
    }

    /**
     * Проверяет что MD5 хеш возвращается в виде hex-строки длиной 32 символа.
     */
    @Test
    void shouldReturnHexString_ofLength32() {
        byte[] content = FAKER.lorem().sentence().getBytes();

        String hash = fileHashService.calculateMD5(content);

        assertThat(hash).hasSize(32);
        assertThat(hash).containsPattern("[0-9a-f]+");
    }

    // checkDuplicateInDatabase

    /**
     * Проверяет что метод завершается без исключений, если у пользователя нет файла с таким хешем.
     */
    @Test
    void shouldNotThrow_whenNoDuplicateExists() {
        String fileHash = FAKER.crypto().md5();
        when(fileMetadataRepository.existsByFileHashAndUserId(fileHash, userId)).thenReturn(false);

        fileHashService.checkDuplicateInDatabase(fileHash, userId);

        verify(fileMetadataRepository).existsByFileHashAndUserId(fileHash, userId);
    }

    /**
     * Проверяет что выбрасывается DuplicateFileException, если у пользователя
     * уже есть файл с таким же MD5 хешем.
     */
    @Test
    void shouldThrowDuplicateFileException_whenFileHashAlreadyExists() {
        String fileHash = FAKER.crypto().md5();
        when(fileMetadataRepository.existsByFileHashAndUserId(fileHash, userId)).thenReturn(true);

        assertThatThrownBy(() -> fileHashService.checkDuplicateInDatabase(fileHash, userId))
                .isInstanceOf(DuplicateFileException.class);

        verify(fileMetadataRepository).existsByFileHashAndUserId(fileHash, userId);
    }

    /**
     * Проверяет что дубликат проверяется только в рамках конкретного пользователя —
     * один и тот же хеш у разных пользователей не является дубликатом.
     */
    @Test
    void shouldCheckDuplicatePerUser_notGlobally() {
        String fileHash = FAKER.crypto().md5();
        Long anotherUserId = userId + 1;

        when(fileMetadataRepository.existsByFileHashAndUserId(fileHash, userId)).thenReturn(false);
        when(fileMetadataRepository.existsByFileHashAndUserId(fileHash, anotherUserId)).thenReturn(true);

        fileHashService.checkDuplicateInDatabase(fileHash, userId);

        assertThatThrownBy(() -> fileHashService.checkDuplicateInDatabase(fileHash, anotherUserId))
                .isInstanceOf(DuplicateFileException.class);
    }
}