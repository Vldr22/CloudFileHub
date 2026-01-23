package org.resume.s3filemanager.service.file;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.resume.s3filemanager.entity.FileMetadata;
import org.resume.s3filemanager.entity.User;
import org.resume.s3filemanager.enums.FileUploadStatus;
import org.resume.s3filemanager.exception.FileNotFoundException;
import org.resume.s3filemanager.repository.FileMetadataRepository;
import org.resume.s3filemanager.service.auth.UserService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/**
 * Сервис для управления метаданными файлов в базе данных.
 * <p>
 * Обрабатывает CRUD операции для метаданных файлов и координирует работу
 * с сервисом пользователей для обновления статусов загрузки.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FileMetadataService {

    private final FileMetadataRepository fileMetadataRepository;
    private final FilePermissionService fileUploadPermissionService;
    private final UserService userService;

    /**
     * Сохраняет метаданные файла в базу данных с привязкой к пользователю.
     * <p>
     * Обновляет статус загрузки пользователя на FILE_UPLOADED.
     *
     * @param file загруженный файл
     * @param uniqueFileName сгенерированное уникальное имя файла (на основе UUID)
     * @param fileHash MD5 хеш содержимого файла
     * @param user пользователь, загрузивший файл
     */
    public void saveDatabaseMetadata(MultipartFile file, String uniqueFileName,
                                     String fileHash, User user) {
        FileMetadata metadata = FileMetadata.builder()
                .uniqueName(uniqueFileName)
                .originalName(file.getOriginalFilename())
                .type(file.getContentType())
                .size(file.getSize())
                .fileHash(fileHash)
                .user(user)
                .build();

        fileMetadataRepository.save(metadata);
    }

    /**
     * Удаляет метаданные файла и обновляет статус загрузки пользователя.
     * <p>
     * Сбрасывает статус пользователя на NOT_UPLOADED, позволяя загружать новые файлы.
     *
     * @param file метаданные файла для удаления
     */
    @Transactional
    public void deleteFileAndUpdateUserStatus(FileMetadata file) {
        deleteDatabaseMetadata(file.getUniqueName());
        userService.updateUploadStatus(file.getUser(), FileUploadStatus.NOT_UPLOADED);
    }

    @Transactional
    public void saveFileWithPermission(MultipartFile file, String uniqueFileName, String fileHash, User user) {
        saveDatabaseMetadata(file, uniqueFileName, fileHash, user);
        fileUploadPermissionService.markFileUploaded();
    }

    @Transactional
    public void deleteDatabaseMetadata(String uniqueFileName) {
        int deleted = fileMetadataRepository.deleteByUniqueName(uniqueFileName);
        if (deleted == 0) {
            log.warn("File metadata not found for deletion: {}", uniqueFileName);
            throw new FileNotFoundException(uniqueFileName);
        }
    }

    public FileMetadata findByUniqueName(String uniqueFileName) {
        return fileMetadataRepository.findByUniqueName(uniqueFileName)
                .orElseThrow(() -> {
            log.warn("File not found: {}", uniqueFileName);
            return new FileNotFoundException(uniqueFileName);
        });
    }
}
