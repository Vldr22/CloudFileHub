package org.resume.s3filemanager.repository;

import org.resume.s3filemanager.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long>, QuerydslPredicateExecutor<AuditLog> {

}
