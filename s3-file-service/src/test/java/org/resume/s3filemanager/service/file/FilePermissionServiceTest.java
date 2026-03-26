package org.resume.s3filemanager.service.file;

import com.github.javafaker.Faker;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.resume.s3filemanager.entity.FileMetadata;
import org.resume.s3filemanager.entity.User;
import org.resume.s3filemanager.enums.FileUploadStatus;
import org.resume.s3filemanager.enums.UserRole;
import org.resume.s3filemanager.exception.FileAccessDeniedException;
import org.resume.s3filemanager.exception.FileUploadLimitException;
import org.resume.s3filemanager.exception.UserNotFoundException;
import org.resume.s3filemanager.repository.UserRepository;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;

import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("FilePermissionService — проверка прав на загрузку и удаление файлов")
class FilePermissionServiceTest {

    private static final Faker FAKER = new Faker();

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private FilePermissionService filePermissionService;

    private String username;
    private User user;

    @BeforeEach
    void setUp() {
        username = FAKER.name().username();
        user = new User();
        user.setId(FAKER.number().randomNumber());
        user.setUsername(username);
        user.setRole(UserRole.USER);
        user.setUploadStatus(FileUploadStatus.NOT_UPLOADED);

        var auth = new UsernamePasswordAuthenticationToken(username, null, Collections.emptyList());
        SecurityContextHolder.setContext(new SecurityContextImpl(auth));
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    // checkUploadPermission

    /**
     * Проверяет что пользователь со статусом NOT_UPLOADED получает разрешение
     * на загрузку и возвращается корректный объект пользователя.
     */
    @Test
    void shouldAllowUpload_whenStatusIsNotUploaded() {
        user.setUploadStatus(FileUploadStatus.NOT_UPLOADED);
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));

        User result = filePermissionService.checkUploadPermission();

        assertThat(result).isEqualTo(user);
    }

    /**
     * Проверяет что пользователь со статусом UNLIMITED может загружать файлы
     * без каких-либо ограничений.
     */
    @Test
    void shouldAllowUpload_whenStatusIsUnlimited() {
        user.setUploadStatus(FileUploadStatus.UNLIMITED);
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));

        User result = filePermissionService.checkUploadPermission();

        assertThat(result).isEqualTo(user);
    }

    /**
     * Проверяет что пользователь со статусом FILE_UPLOADED не может загрузить
     * повторный файл — выбрасывается FileUploadLimitException.
     */
    @Test
    void shouldThrowFileUploadLimitException_whenStatusIsFileUploaded() {
        user.setUploadStatus(FileUploadStatus.FILE_UPLOADED);
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> filePermissionService.checkUploadPermission())
                .isInstanceOf(FileUploadLimitException.class);
    }

    /**
     * Проверяет что при отсутствии пользователя в БД выбрасывается UserNotFoundException.
     */
    @Test
    void shouldThrowUserNotFoundException_whenUserNotFoundInDatabase() {
        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> filePermissionService.checkUploadPermission())
                .isInstanceOf(UserNotFoundException.class);
    }

    // checkDeletePermission

    /**
     * Проверяет что владелец файла может удалить свой файл.
     */
    @Test
    void shouldAllowDelete_whenUserIsOwner() {
        FileMetadata file = buildFileMetadata(user);

        filePermissionService.checkDeletePermission(user, file);

        verify(userRepository, never()).findByUsername(any());
    }

    /**
     * Проверяет что администратор может удалить файл любого пользователя,
     * не являясь его владельцем.
     */
    @Test
    void shouldAllowDelete_whenUserIsAdmin() {
        User admin = new User();
        admin.setId(FAKER.number().randomNumber());
        admin.setRole(UserRole.ADMIN);

        User otherUser = new User();
        otherUser.setId(FAKER.number().randomNumber());

        FileMetadata file = buildFileMetadata(otherUser);

        filePermissionService.checkDeletePermission(admin, file);

        verify(userRepository, never()).findByUsername(any());
    }

    /**
     * Проверяет что обычный пользователь не может удалить чужой файл —
     * выбрасывается FileAccessDeniedException.
     */
    @Test
    void shouldThrowFileAccessDeniedException_whenUserTriesToDeleteOthersFile() {
        User otherUser = new User();
        otherUser.setId(FAKER.number().randomNumber());
        user.setUsername(username);
        user.setRole(UserRole.USER);

        FileMetadata file = buildFileMetadata(otherUser);

        assertThatThrownBy(() -> filePermissionService.checkDeletePermission(user, file))
                .isInstanceOf(FileAccessDeniedException.class);
    }

    /**
     * Проверяет что администратор не получает исключение даже при попытке
     * удалить собственный файл (базовый случай для ADMIN-роли).
     */
    @Test
    void shouldAllowDelete_whenAdminDeletesOwnFile() {
        User admin = new User();
        admin.setId(FAKER.number().randomNumber());
        admin.setRole(UserRole.ADMIN);

        FileMetadata file = buildFileMetadata(admin);

        filePermissionService.checkDeletePermission(admin, file);
    }

    // getCurrentUser

    /**
     * Проверяет что getCurrentUser корректно возвращает пользователя
     * из SecurityContext по имени.
     */
    @Test
    void shouldReturnCurrentUser_fromSecurityContext() {
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));

        User result = filePermissionService.getCurrentUser();

        assertThat(result.getUsername()).isEqualTo(username);
        verify(userRepository).findByUsername(username);
    }

    // markFileUploaded()

    /**
     * Проверяет что статус пользователя меняется с NOT_UPLOADED на FILE_UPLOADED
     * и пользователь сохраняется в БД.
     */
    @Test
    void shouldMarkFileUploaded_whenStatusIsNotUploaded() {
        user.setUploadStatus(FileUploadStatus.NOT_UPLOADED);
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));

        filePermissionService.markFileUploaded();

        assertThat(user.getUploadStatus()).isEqualTo(FileUploadStatus.FILE_UPLOADED);
        verify(userRepository).save(user);
    }

    /**
     * Проверяет что пользователь со статусом UNLIMITED не изменяется
     * и сохранение в БД не происходит.
     */
    @Test
    void shouldNotChangeStatus_whenStatusIsUnlimited() {
        user.setUploadStatus(FileUploadStatus.UNLIMITED);
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));

        filePermissionService.markFileUploaded();

        assertThat(user.getUploadStatus()).isEqualTo(FileUploadStatus.UNLIMITED);
        verify(userRepository, never()).save(any());
    }

    // === Helpers methods ===
    private FileMetadata buildFileMetadata(User owner) {
        return FileMetadata.builder()
                .id(FAKER.number().randomNumber())
                .uniqueName(FAKER.internet().uuid())
                .originalName(FAKER.file().fileName())
                .fileHash(FAKER.crypto().md5())
                .size(FAKER.number().numberBetween(1024L, 10_000_000L))
                .user(owner)
                .build();
    }
}