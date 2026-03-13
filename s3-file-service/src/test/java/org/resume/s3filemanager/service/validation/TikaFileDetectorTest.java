package org.resume.s3filemanager.service.validation;

import org.apache.tika.Tika;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.resume.s3filemanager.validation.TikaFileDetector;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("TikaFileDetector — определение реального типа файла по сигнатуре")
class TikaFileDetectorTest {


    private static final byte[] PDF_MAGIC_BYTES = {0x25, 0x50, 0x44, 0x46, 0x2D, 0x31, 0x2E};
    private static final byte[] PNG_MAGIC_BYTES = {(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A};

    private static final String PDF_FILENAME = "document.pdf";
    private static final String PDF_CONTENT_TYPE = "application/pdf";

    private TikaFileDetector tikaFileDetector;

    @BeforeEach
    void setUp() {
        tikaFileDetector = new TikaFileDetector(new Tika());
    }

    // detectContentType

    /**
     * Tika читает магические байты PDF и возвращает корректный MIME-тип.
     */
    @Test
    void shouldDetectPdf_byMagicBytes() {
        String detected = tikaFileDetector.detectContentType(PDF_MAGIC_BYTES, "document.pdf");

        assertThat(detected).isEqualTo("application/pdf");
    }

    /**
     * Tika читает магические байты PNG и возвращает корректный MIME-тип.
     */
    @Test
    void shouldDetectPng_byMagicBytes() {
        String detected = tikaFileDetector.detectContentType(PNG_MAGIC_BYTES, "image.png");

        assertThat(detected).isEqualTo("image/png");
    }

    // verifyContentType

    /**
     * Сигнатура файла совпадает с заявленным типом — файл валиден.
     */
    @Test
    void shouldReturnTrue_whenSignatureMatchesDeclaredType() {
        boolean result = tikaFileDetector.verifyContentType(PDF_MAGIC_BYTES, PDF_FILENAME, PDF_CONTENT_TYPE);

        assertThat(result).isTrue();
    }

    /**
     * PNG байты объявлены как PDF — классический случай переименования. Tika видит реальную сигнатуру и возвращает false.
     */
    @Test
    void shouldReturnFalse_whenPngBytesAreDeclaredAsPdf() {
        boolean result = tikaFileDetector.verifyContentType(PNG_MAGIC_BYTES, PDF_FILENAME, PDF_CONTENT_TYPE);

        assertThat(result).isFalse();
    }

    /**
     * Сравнение типов нечувствительно к регистру — "application/PDF" принимается как "application/pdf".
     */
    @Test
    void shouldReturnTrue_whenContentTypeIsUpperCase() {
        boolean result = tikaFileDetector.verifyContentType(PDF_MAGIC_BYTES, PDF_FILENAME, "application/PDF");

        assertThat(result).isTrue();
    }
}