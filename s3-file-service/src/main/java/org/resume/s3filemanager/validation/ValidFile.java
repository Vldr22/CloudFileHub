package org.resume.s3filemanager.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import org.resume.s3filemanager.constant.ValidationMessages;
import org.springframework.web.multipart.MultipartFile;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Аннотация для валидации загружаемых файлов.
 * <p>
 * Применяется к параметрам типа {@link MultipartFile} для проверки:
 * <ul>
 *   <li>Является ли тип файла разрешенным</li>
 *   <li>Соответствует ли реальная сигнатура заявленному типу</li>
 * </ul>
 * Обработка выполняется {@link FileValidator}.
 *
 * @see FileValidator
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = FileValidator.class)
public @interface ValidFile {

    String message() default ValidationMessages.INVALID_FILE_TYPE;

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

}

