package org.resume.s3filemanager.entity;

import io.hypersistence.utils.hibernate.type.basic.Inet;
import io.hypersistence.utils.hibernate.type.basic.PostgreSQLInetType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Type;
import org.resume.s3filemanager.audit.AuditOperation;
import org.resume.s3filemanager.audit.ResourceType;
import org.resume.s3filemanager.enums.CommonResponseStatus;

import java.time.Instant;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "audit_logs")
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 36)
    private String requestId;

    private String username;

    @Type(PostgreSQLInetType.class)
    @Column(columnDefinition = "INET")
    private Inet ipAddress;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private AuditOperation operation;

    @Enumerated(EnumType.STRING)
    @Column(length = 50)
    private ResourceType resourceType;

    private String resourceId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CommonResponseStatus status;

    @Column(columnDefinition = "TEXT")
    private String details;

    @Column(nullable = false)
    private Instant timestamp;
}
