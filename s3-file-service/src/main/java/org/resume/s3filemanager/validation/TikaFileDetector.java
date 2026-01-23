package org.resume.s3filemanager.validation;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.Tika;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

/**
 * Детектор реального типа файлов на основе Apache Tika.
 * <p>
 * Анализирует магические байты (file signature) для определения
 * фактического типа файла, защищая от подмены через переименование.
 *
 * @see Tika
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TikaFileDetector {

    private final Tika tika;

    /**
     * Определяет реальный MIME-тип файла по его содержимому.
     *
     * @param fileBytes содержимое файла
     * @param fileName имя файла (используется как подсказка для Tika)
     * @return определенный MIME-тип
     */
    public String detectContentType(byte[] fileBytes, String fileName) {
        String detectedType = tika.detect(fileBytes, fileName);
        log.debug("Detected content type: {} for file: {}", detectedType, fileName);
        return detectedType;
    }

    /**
     * Проверяет соответствие реального типа файла заявленному.
     * <p>
     * Сравнивает тип, определенный через Tika, с MIME-типом,
     * указанным клиентом, после нормализации обоих значений.
     *
     * @param fileBytes содержимое файла
     * @param fileName имя файла
     * @param declaredContentType MIME-тип, заявленный клиентом
     * @return true если типы совпадают, false при несоответствии
     */
    public boolean verifyContentType(byte[] fileBytes, String fileName, String declaredContentType) {
        String normalizedRealType = normalizeContentType(detectContentType(fileBytes, fileName));
        String normalizedDeclaredType = normalizeContentType(declaredContentType);

        if (normalizedRealType.equalsIgnoreCase(normalizedDeclaredType)) {
            return true;
        }

        log.warn("Content type mismatch for file {}: real={}, declared={}",
                fileName, normalizedRealType, normalizedDeclaredType);
        return false;

    }

    private String normalizeContentType(String contentType) {
        MediaType mediaType = MediaType.parseMediaType(contentType);
        return String.format("%s/%s", mediaType.getType(), mediaType.getSubtype());
    }
}
