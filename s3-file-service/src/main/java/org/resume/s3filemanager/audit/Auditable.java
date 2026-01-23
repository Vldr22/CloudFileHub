package org.resume.s3filemanager.audit;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Аннотация для маркировки методов, требующих аудита.
 * <p>
 * Аспект автоматически логирует выполнение метода:
 * кто, что, когда, результат (SUCCESS/ERROR).
 * <p>
 * Операции которые логируются:
 * @see AuditOperation
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Auditable {

    AuditOperation operation();

    ResourceType resourceType() default ResourceType.UNKNOWN;

}
